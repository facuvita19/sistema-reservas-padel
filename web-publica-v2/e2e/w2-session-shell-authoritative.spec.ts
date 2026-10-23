import {test,expect} from '@playwright/test';

const snapshot={autenticado:true,vencimiento:'2099-12-31T23:59:59Z',perfil:{clienteId:77,nombre:'Facundo',apellido:'Vitale',email:'facundo@example.com'}};

test.skip(({isMobile})=>isMobile,'El contrato horizontal del shell corresponde a escritorio.');

test.beforeEach(async({page})=>{await page.addInitScript(s=>sessionStorage.setItem('padel.sesion.snapshot.v1',JSON.stringify(s)),snapshot)});

test('R3.2 muestra la cuenta recordada desde el primer render sin mover el menu',async({page})=>{
 await page.route('**/api/publica/auth/sesion',async r=>{await new Promise(x=>setTimeout(x,500));await r.fulfill({json:snapshot})});
 await page.goto('/w2/torneos',{waitUntil:'domcontentloaded'});
 const menu=page.locator('.w2-links'),cuenta=page.locator('#menuUsuario');
 await expect(cuenta).toBeVisible();await expect(page.locator('#w2SessionCheck')).toBeHidden();
 const inicial=await menu.boundingBox();await page.waitForTimeout(700);const estable=await menu.boundingBox();
 expect(inicial&&estable).toBeTruthy();expect(Math.abs(estable!.x-inicial!.x)).toBeLessThanOrEqual(.5);
 await expect(page.locator('#nombreUsuario')).toHaveText('Facundo');
});

test('R3.2 conserva la ultima sesion valida ante un error temporal',async({page})=>{
 await page.route('**/api/publica/auth/sesion',r=>r.fulfill({status:503,json:{mensaje:'Temporal'}}));
 await page.goto('/w2/comunidad');await page.waitForTimeout(2100);
 await expect(page.locator('#menuUsuario')).toBeVisible();
 await expect(page.locator('#nombreUsuario')).toHaveText('Facundo');
 await expect(page.getByText('Sesión no disponible',{exact:true})).toHaveCount(0);
 await expect(page.locator('html')).toHaveAttribute('data-session','authenticated');
});

test('R3.2 mantiene separadas reserva notificaciones y cuenta',async({page})=>{
 await page.route('**/api/publica/auth/sesion',r=>r.fulfill({json:snapshot}));
 await page.route('**/api/publica/cliente/comunidad/notificaciones',r=>r.fulfill({json:{notificaciones:[],noLeidas:0,total:0}}));
 await page.goto('/w2',{waitUntil:'networkidle'});
 const reserva=await page.locator('.w2-cta').boundingBox(),notificaciones=await page.locator('#w2Notifications').boundingBox(),cuenta=await page.locator('.w2-auth-slot').boundingBox();
 expect(reserva&&notificaciones&&cuenta).toBeTruthy();expect(notificaciones!.x-(reserva!.x+reserva!.width)).toBeGreaterThanOrEqual(8);expect(cuenta!.x-(notificaciones!.x+notificaciones!.width)).toBeGreaterThanOrEqual(8);
});
