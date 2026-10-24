import {expect,test} from '@playwright/test';

test.skip(({isMobile})=>isMobile,'Contrato de viewport de escritorio.');

test('el ancho util no cambia durante navegaciones completas',async({page})=>{
 const routes=['/w2/','/w2/reservar','/w2/torneos','/w2/rankings','/w2/comunidad'];
 let reference:number|null=null;
 for(const route of routes){
  await page.goto(route,{waitUntil:'domcontentloaded'});
  const first=await page.evaluate(()=>document.documentElement.clientWidth);
  await page.waitForTimeout(500);
  const settled=await page.evaluate(()=>document.documentElement.clientWidth);
  expect(settled,route).toBe(first);
  if(reference===null)reference=settled;else expect(settled,route).toBe(reference);
 }
});

test('la cabecera no cambia de centro al aparecer el contenido largo',async({page})=>{
 await page.goto('/w2/comunidad',{waitUntil:'domcontentloaded'});
 const first=await page.locator('.w2-nav').boundingBox();
 await page.waitForTimeout(900);
 const settled=await page.locator('.w2-nav').boundingBox();
 expect(first&&settled).toBeTruthy();
 expect(Math.abs(settled!.x-first!.x)).toBeLessThanOrEqual(.5);
 expect(Math.abs(settled!.width-first!.width)).toBeLessThanOrEqual(.5);
});
