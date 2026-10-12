import { test, expect } from '@playwright/test';
import { mockPublicApi } from './fixtures/api-mock';

test.beforeEach(async({page})=>mockPublicApi(page));

test('modal de acceso abre, valida y restaura el foco',async({page,isMobile})=>{
 await page.goto('/');
 const open=isMobile?page.locator('#abrirAutenticacionMovil'):page.locator('#abrirAutenticacion');
 if(isMobile){await page.locator('#menuButton').click();}
 await open.click();
 await expect(page.locator('#modalAutenticacion')).toHaveAttribute('aria-modal','true');
 await expect(page.locator('#loginEmail')).toBeFocused();
 await page.locator('#enviarLogin').click();
 await expect(page.locator('#errorLoginEmail')).toContainText('correo');
 await page.locator('#cerrarAutenticacion').click();
 await expect(page.locator('#modalAutenticacion')).toHaveClass(/hidden/);
});

test('pestañas de autenticación responden a flechas',async({page,isMobile})=>{
 await page.goto('/');
 if(isMobile){await page.locator('#menuButton').click();await page.locator('#abrirAutenticacionMovil').click();}
 else await page.locator('#abrirAutenticacion').click();
 const login=page.locator('#tabLogin'),registro=page.locator('#tabRegistro');
 await login.focus();await login.press('ArrowRight');
 await expect(registro).toHaveAttribute('aria-selected','true');
 await expect(page.locator('#registroNombre')).toBeFocused();
});
