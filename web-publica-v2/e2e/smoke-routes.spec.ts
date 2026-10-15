import { test, expect } from '@playwright/test';
import { mockPublicApi } from './fixtures/api-mock';
import { monitorPage } from './fixtures/quality';

const routes=[
 ['Inicio','/'],['Reservar','/reservar'],['Mis reservas','/mis-reservas'],['Mi cuenta','/mis-datos'],['Torneos W2','/w2/torneos'],['Puente Torneos','/torneos'],['Restablecer','/restablecer?token=e2e-token'],['404','/ruta-e2e-inexistente']
] as const;

test.beforeEach(async({page})=>mockPublicApi(page));
for(const [name,url] of routes){
 test(name+' carga sin errores',{tag:'@smoke'},async({page},testInfo)=>{
  const assertNoErrors=monitorPage(page,testInfo);
  const response=await page.goto(url,{waitUntil:'networkidle'});
  expect(response?.status(),name+' debe responder').toBeLessThan(500);
  await expect(page.locator('body')).toBeVisible();
  await expect(page.locator('h1').first()).toBeVisible();
  await expect(page.locator('html')).not.toHaveCSS('overflow-x','scroll');
  await assertNoErrors();
 });
}
