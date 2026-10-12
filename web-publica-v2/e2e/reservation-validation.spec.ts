import { test, expect } from '@playwright/test';
import { mockPublicApi } from './fixtures/api-mock';

test.beforeEach(async({page})=>mockPublicApi(page));

test('reserva permite seleccionar cancha y horario',async({page})=>{
 await page.goto('/reservar');
 const court=page.locator('.reserve-court').first();
 await expect(court).toBeVisible();await court.click();
 await page.locator('#buscarHorarios').click();
 const time=page.locator('.reserve-time').first();
 await expect(time).toBeVisible();await time.click();
 await expect(page.locator('[data-panel="3"]')).not.toHaveClass(/hidden/);
});

test('primer error de reserva queda enfocado y visible',async({page})=>{
 await page.goto('/reservar');
 await page.locator('.reserve-court').first().click();
 await page.locator('#buscarHorarios').click();
 await page.locator('.reserve-time').first().click();
 await page.locator('#enviarReserva').click();
 const input=page.locator('#clienteNombre');
 await expect(input).toBeFocused();
 await expect(input).toHaveAttribute('aria-invalid','true');
 await expect(page.locator('#error-clienteNombre')).toContainText('nombre');
 const box=await input.locator('xpath=..').boundingBox();
 const header=await page.locator('[data-site-header]').boundingBox();
 expect(box&&header&&box.y>=header.height-1).toBeTruthy();
});
