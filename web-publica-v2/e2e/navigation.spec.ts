import { test, expect } from '@playwright/test';
import { mockPublicApi } from './fixtures/api-mock';

test.beforeEach(async({page})=>mockPublicApi(page));

test('navegación principal recorre las rutas públicas',async({page,isMobile})=>{
 await page.goto('/');
 if(isMobile){
  await page.locator('#menuButton').click();
  await expect(page.locator('#mainMenu')).toHaveAttribute('aria-hidden','false');
  await page.locator('#mainMenu a[href="/torneos"]').click();
 }else{
  await page.locator('.site-nav-v59 a[href="/torneos"]').click();
 }
 await expect(page).toHaveURL(/\/torneos$/);
 await expect(page.locator('h1')).toContainText('competencia');
});

test('menú móvil contiene el foco y cierra con Escape',async({page,isMobile})=>{
 test.skip(!isMobile,'Comportamiento exclusivo del menú móvil');
 await page.goto('/');
 const trigger=page.locator('#menuButton');
 await trigger.focus();await trigger.press('Enter');
 await expect(page.locator('#mainMenu')).toHaveAttribute('aria-hidden','false');
 await expect(page.locator('#cerrarMenuMovil')).toBeFocused();
 await page.keyboard.press('Escape');
 await expect(page.locator('#mainMenu')).toHaveAttribute('aria-hidden','true');
 await expect(trigger).toBeFocused();
});
