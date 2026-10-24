import {expect,test} from '@playwright/test';

test('un 429 de sesion no bloque catalogos publicos',async({page,isMobile})=>{
 test.skip(isMobile,'Contrato de red autoritativo en Chromium de escritorio.');
 let sesiones=0,canchas=0,torneos=0;
 await page.route('**/api/publica/auth/sesion',route=>{sesiones++;return route.fulfill({status:429,headers:{'Retry-After':'30'},json:{mensaje:'Pausa de sesion'}})});
 await page.route('**/api/publica/complejo',route=>route.fulfill({json:{nombreComercial:'Club',moneda:'ARS',porcentajeSenia:25,anticipacionMinimaHoras:1,minutosReservaPendiente:10,pagoTransferenciaDisponible:false}}));
 await page.route('**/api/publica/canchas',route=>{canchas++;return route.fulfill({json:[]})});
 await page.route('**/api/publica/torneos',route=>{torneos++;return route.fulfill({json:[]})});
 await page.goto('/w2/reservar');
 await page.waitForTimeout(500);
 expect(sesiones).toBe(1);
 expect(canchas).toBeGreaterThan(0);
 await page.goto('/w2/torneos');
 await page.waitForTimeout(500);
 expect(torneos).toBeGreaterThan(0);
});
