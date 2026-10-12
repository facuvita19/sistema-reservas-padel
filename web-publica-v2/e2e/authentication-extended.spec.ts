import { test, expect, type Page } from '@playwright/test';
import { mockPublicApi } from './fixtures/api-mock';

async function abrirRegistro(page:Page,isMobile:boolean){
 await page.goto('/');
 if(isMobile){await page.locator('#menuButton').click();await page.locator('#abrirAutenticacionMovil').click();}
 else await page.locator('#abrirAutenticacion').click();
 await page.locator('#tabRegistro').click();
 await expect(page.locator('#registroNombre')).toBeFocused();
}
async function completarPaso1(page:Page){
 await page.locator('#registroNombre').fill('Ana');
 await page.locator('#registroApellido').fill('Prueba');
 await page.locator('#registroDocumento').fill('30123456');
 await page.locator('#registroTelefono').fill('11 1234 5678');
 await page.locator('#registroEmail').fill('ana.e2e@example.test');
}

test.beforeEach(async({page})=>mockPublicApi(page));

test('registro valida el primer dato pendiente en orden',async({page,isMobile})=>{
 await abrirRegistro(page,isMobile);
 const continuar=page.locator('#registroContinuar');
 await continuar.click();await expect(page.locator('#registroNombre')).toBeFocused();
 await expect(page.locator('#errorAutenticacion')).toContainText('nombre');
 await page.locator('#registroNombre').fill('Ana');await continuar.click();
 await expect(page.locator('#registroApellido')).toBeFocused();
 await page.locator('#registroApellido').fill('Prueba');await continuar.click();
 await expect(page.locator('#registroDocumento')).toBeFocused();
 await page.locator('#registroDocumento').fill('30123456');await continuar.click();
 await expect(page.locator('#registroTelefono')).toBeFocused();
 await page.locator('#registroTelefono').fill('11 1234 5678');await continuar.click();
 await expect(page.locator('#registroEmail')).toBeFocused();
 await page.locator('#registroEmail').fill('correo-invalido');await continuar.click();
 await expect(page.locator('#errorAutenticacion')).toContainText('correo válido');
});

test('registro conserva datos al volver y resume la posicion',async({page,isMobile})=>{
 await abrirRegistro(page,isMobile);await completarPaso1(page);
 await page.locator('#registroContinuar').click();
 await expect(page.locator('#registroPaso2')).not.toHaveClass(/hidden/);
 await expect(page.locator('#registroResumenNombre')).toHaveText('Ana Prueba');
 const drive=page.locator('input[name="registroPosicionVisual"][value="DRIVE"]');
 await drive.locator('xpath=ancestor::label[1]').click();
 await expect(drive).toBeChecked();
 await expect(page.locator('#registroResumenPosicion')).toHaveText('Drive');
 await page.locator('#registroAnterior').click();
 await expect(page.locator('#registroNombre')).toHaveValue('Ana');
 await expect(page.locator('#registroEmail')).toHaveValue('ana.e2e@example.test');
});

test('registro controla contrasenas y botones mostrar ocultar',async({page,isMobile})=>{
 await abrirRegistro(page,isMobile);await completarPaso1(page);await page.locator('#registroContinuar').click();
 const pass=page.locator('#registroPassword'),repeat=page.locator('#registroPasswordRepetido'),submit=page.locator('#enviarRegistro');
 await pass.fill('corta');await expect(submit).toBeDisabled();
 await expect(page.locator('#reglaRegistroPassword')).toContainText('8 caracteres');
 await pass.fill('ClaveE2E123');await repeat.fill('OtraClave123');
 await expect(page.locator('#coincidenciaRegistroPassword')).toContainText('no coinciden');await expect(submit).toBeDisabled();
 await repeat.fill('ClaveE2E123');await expect(submit).toBeEnabled();
 const toggle=page.locator('[data-password="registroPassword"]');await toggle.click();
 await expect(pass).toHaveAttribute('type','text');await expect(toggle).toHaveAttribute('aria-pressed','true');
 await toggle.click();await expect(pass).toHaveAttribute('type','password');
});

test('recuperacion valida correo y ofrece enlace de prueba',async({page,isMobile})=>{
 await page.goto('/');
 if(isMobile){await page.locator('#menuButton').click();await page.locator('#abrirAutenticacionMovil').click();}
 else await page.locator('#abrirAutenticacion').click();
 await page.locator('#abrirRecuperacion').click();
 await page.locator('#recuperacionEmail').fill('correo-invalido');await page.locator('#enviarRecuperacion').click();
 await expect(page.locator('#recuperacionEmail')).toHaveAttribute('aria-invalid','true');
 await page.locator('#recuperacionEmail').fill('ana.e2e@example.test');await page.locator('#enviarRecuperacion').click();
 await expect(page.locator('#resultadoRecuperacion')).toContainText('recibirás instrucciones');
 await expect(page.locator('#resultadoRecuperacion a')).toHaveAttribute('href','/restablecer?token=token-valido-e2e');
});

test('restablecimiento distingue token invalido y token vigente',async({page})=>{
 await page.goto('/restablecer?token=token-invalido');
 await expect(page.locator('#tokenInvalido')).not.toHaveClass(/hidden/);
 await page.goto('/restablecer?token=token-valido-e2e');
 await expect(page.locator('#formRestablecer')).not.toHaveClass(/hidden/);
 await expect(page.locator('#restablecerPassword')).toBeFocused();
});

test('restablecimiento exige coincidencia y finaliza correctamente',async({page})=>{
 await page.goto('/restablecer?token=token-valido-e2e');
 const pass=page.locator('#restablecerPassword'),repeat=page.locator('#restablecerRepetido'),submit=page.locator('#enviarRestablecer');
 await pass.fill('ClaveNueva123');await repeat.fill('Distinta123');
 await expect(page.locator('#coincidenciaRestablecer')).toContainText('no coinciden');await expect(submit).toBeDisabled();
 await repeat.fill('ClaveNueva123');await expect(submit).toBeEnabled();
 await submit.click();await expect(page.locator('#exitoRestablecer')).not.toHaveClass(/hidden/);
 await expect(page.locator('#formRestablecer')).toHaveClass(/hidden/);
});
