import{test,expect}from'@playwright/test';

test.describe('T1A transiciones nativas W2',()=>{
 test('declara navegacion MPA y nombres estables',async({page})=>{
  await page.goto('/w2/');
  await expect(page.locator('[data-w2-route-shell]')).toHaveCount(1);
  await expect(page.locator('[data-w2-route-content]')).toHaveCount(1);
  await expect(page.locator('[data-w2-route-footer]')).toHaveCount(1);
  const state=await page.evaluate(()=>({
   header:getComputedStyle(document.querySelector('.w2-header')!).viewTransitionName,
   content:getComputedStyle(document.querySelector('[data-w2-route-content]')!).viewTransitionName,
   footer:getComputedStyle(document.querySelector('[data-w2-route-footer]')!).viewTransitionName
  }));
  expect(state).toEqual({header:'w2-header',content:'w2-route-content',footer:'w2-route-footer'});
 });

 test('navega entre rutas reales sin romper URL ni contenido',async({page})=>{
  await page.goto('/w2/');
  await page.locator('.w2-links a[href="/w2/torneos"]').click();
  await expect(page).toHaveURL(/\/w2\/torneos\/?$/);
  await expect(page.locator('#w2-content')).toBeVisible();
  await page.goBack();
  await expect(page).toHaveURL(/\/w2\/?$/);
  await expect(page.locator('#w2-content')).toBeVisible();
 });

 test('mantiene navegacion usable con movimiento reducido',async({page})=>{
  await page.emulateMedia({reducedMotion:'reduce'});
  await page.goto('/w2/');
  await page.locator('.w2-links a[href="/w2/rankings"]').click();
  await expect(page).toHaveURL(/\/w2\/rankings/);
  await expect(page.locator('#w2-content')).toBeVisible();
 });
});
