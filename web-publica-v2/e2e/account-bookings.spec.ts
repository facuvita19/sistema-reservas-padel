import { test, expect } from '@playwright/test';
import { mockPublicApi } from './fixtures/api-mock';

test.beforeEach(async({page})=>mockPublicApi(page,{authenticated:true}));

test('Mi cuenta carga el perfil autenticado y permite descartar cambios',async({page})=>{
 await page.goto('/mis-datos');
 await expect(page.locator('#contenidoDatos')).not.toHaveClass(/hidden/);
 await expect(page.locator('#resumenLectura')).toContainText('Ana');
 await page.locator('#editarDatos').click();
 const telefono=page.locator('#datosTelefono');
 await telefono.fill('11 9999 9999');
 await expect(page.locator('#guardarDatos')).toBeEnabled();
 await page.locator('#descartarDatos').click();
 await expect(page.locator('#editorDatos')).toHaveClass(/hidden/);
 await page.locator('#editarDatos').click();
 await expect(telefono).toHaveValue('1112345678');
});

test('Mi cuenta valida y guarda cambios de perfil',async({page})=>{
 await page.goto('/mis-datos');await page.locator('#editarDatos').click();
 await page.locator('#datosNombre').fill('');await page.locator('#guardarDatos').click();
 await expect(page.locator('#datosNombre')).toBeFocused();
 await expect(page.locator('#datosNombre')).toHaveAttribute('aria-invalid','true');
 await page.locator('#datosNombre').fill('Analia');
 await page.locator('#datosTelefono').fill('11 2222 3333');
 await page.locator('#guardarDatos').click();
 await expect(page.locator('#mensajeDatos')).toContainText('guardaron correctamente');
 await expect(page.locator('#resumenLectura')).toContainText('Analia');
});

test('Seguridad valida y actualiza la contrasena',async({page})=>{
 await page.goto('/mis-datos');await page.locator('#alternarSeguridad').click();
 await expect(page.locator('#formPassword')).not.toHaveClass(/hidden/);
 const actual=page.locator('#passwordActual'),nueva=page.locator('#passwordNuevo'),repetida=page.locator('#passwordRepetido'),guardar=page.locator('#guardarPassword');
 await nueva.fill('ClaveNueva123');await repetida.fill('ClaveNueva123');
 await page.locator('#formPassword').evaluate((form:HTMLFormElement)=>form.requestSubmit());
 await expect(actual).toBeFocused();
 await expect(actual).toHaveAttribute('aria-invalid','true');
 await actual.fill('ClaveActual123');await repetida.fill('Diferente123');
 await expect(page.locator('#coincidenciaPassword')).toContainText('no coinciden');
 await repetida.fill('ClaveNueva123');await expect(guardar).toBeEnabled();
 const mostrarNueva=page.locator('[data-password="passwordNuevo"]');
 await mostrarNueva.evaluate((boton:HTMLButtonElement)=>boton.click());
 await expect(nueva).toHaveAttribute('type','text');
 await expect(mostrarNueva).toHaveAttribute('aria-pressed','true');
 await guardar.click();await expect(page.locator('#mensajePassword')).toContainText('actualizó correctamente');
});

test('Mis reservas muestra metricas y cambia entre proximas e historial',async({page})=>{
 await page.goto('/mis-reservas');
 await expect(page.locator('#modoAutenticado')).not.toHaveClass(/hidden/);
 await expect(page.locator('#saludoCliente')).toContainText('Ana');
 await expect(page.locator('#cantidadProximas')).toHaveText('2');
 await expect(page.locator('#cantidadAnteriores')).toHaveText('2');
 await expect(page.locator('#metricasReservas')).toContainText('Señas pendientes');
 const historial=page.locator('[data-tab="anteriores"]');await historial.click();
 await expect(historial).toHaveAttribute('aria-selected','true');
 await expect(page.locator('#tituloVistaReservas')).toContainText('Historial');
 await expect(page.locator('#filtrosHistorial')).not.toHaveClass(/hidden/);
});

test('Historial filtra por cancha y limpia criterios',async({page})=>{
 await page.goto('/mis-reservas');await page.locator('[data-tab="anteriores"]').click();
 const buscar=page.locator('#buscarCanchaHistorial');await buscar.fill('Norte');
 await expect(page.locator('#cantidadResultadosHistorial')).toHaveText('1');
 await expect(page.locator('#reservasCuenta')).toContainText('Cancha Norte');
 await expect(page.locator('#filtrosActivosHistorial')).not.toHaveClass(/hidden/);
 await page.locator('#limpiarFiltrosHistorial').click();
 await expect(buscar).toHaveValue('');await expect(page.locator('#cantidadResultadosHistorial')).toHaveText('2');
});

test('Consulta publica por codigo muestra una reserva conocida',async({page})=>{
 await page.unroute('**/api/publica/**');
 await mockPublicApi(page);
 await page.goto('/mis-reservas',{waitUntil:'networkidle'});
 await expect(page.locator('#modoVisitante')).not.toHaveClass(/hidden/);
 const codigo='11111111-2222-4333-8444-555555555555';
 await page.locator('#codigoSeguimiento').fill(codigo);
 const respuesta=page.waitForResponse(r=>r.url().includes('/api/publica/solicitudes/'+codigo)&&r.status()===200);
 await page.locator('#formSeguimiento').evaluate((form:HTMLFormElement)=>form.requestSubmit());
 await respuesta;
 await expect(page.locator('#resultadoSeguimiento')).not.toHaveClass(/hidden/);
 await expect(page.locator('#resultadoSeguimiento')).toContainText('Cancha E2E');
 await expect(page.locator('#resultadoSeguimiento')).toContainText('Esperando seña');
});
