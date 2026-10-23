import {expect,test} from '@playwright/test';

test('anti-429 deduplica datos globales y no reintenta 429 de sesion',async({page,isMobile})=>{
 test.skip(isMobile,'La medicion autoritativa de red se ejecuta una vez en Chromium de escritorio.');
 let sesiones=0,complejos=0;
 await page.route('**/api/publica/auth/sesion',r=>{sesiones++;return r.fulfill({status:429,headers:{'Retry-After':'5'},json:{mensaje:'Pausa'}})});
 await page.route('**/api/publica/complejo',r=>{complejos++;return r.fulfill({json:{nombreComercial:'Club',moneda:'ARS',porcentajeSenia:25,anticipacionMinimaHoras:1,minutosReservaPendiente:10,pagoTransferenciaDisponible:false}})});
 await page.goto('/w2/reservar');
 await page.waitForTimeout(1200);
 expect(sesiones).toBe(1);
 expect(complejos).toBeLessThanOrEqual(1);
});
