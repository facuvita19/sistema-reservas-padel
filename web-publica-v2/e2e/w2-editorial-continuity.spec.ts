import{test,expect}from'@playwright/test';

test.describe('T1C continuidad de cabecera y reveal editorial',()=>{
 test('Inicio conserva el contrato cinetico de referencia',async({page})=>{
  await page.goto('/w2/');
  const node=page.locator('[data-reveal]').first();
  await expect(node).toHaveClass(/revealed/);
  const contract=await node.evaluate(element=>{
   const style=getComputedStyle(element);
   return{
    distance:getComputedStyle(document.documentElement).getPropertyValue('--w2-reveal-distance').trim(),
    duration:getComputedStyle(document.documentElement).getPropertyValue('--w2-reveal-duration').trim(),
    easing:getComputedStyle(document.documentElement).getPropertyValue('--w2-reveal-ease').trim(),
    opacity:style.opacity,
    transform:style.transform
   };
  });
  expect(contract.distance).toBe('28px');
  expect(contract.duration).toBe('650ms');
  expect(contract.easing).toBe('cubic-bezier(.2,.75,.25,1)');
  expect(contract.opacity).toBe('1');
  expect(contract.transform).toBe('none');
 });

 test('Rankings recibe reveal sin tocar cabecera ni formularios',async({page})=>{
  await page.goto('/w2/rankings');
  await expect(page.locator('.w2-header [data-w2-reveal],.w2-header [data-reveal]')).toHaveCount(0);
  await expect(page.locator('form [data-w2-reveal],form [data-reveal]')).toHaveCount(0);
  const hosts=page.locator('[data-w2-reveal]');
  expect(await hosts.count()).toBeGreaterThan(0);
  await expect(hosts.first()).toHaveClass(/revealed/);
 });

 test('Comunidad comparte el contrato cinetico de Inicio',async({page})=>{
  await page.goto('/w2/comunidad');
  const node=page.locator('[data-w2-reveal]').first();
  await expect(node).toHaveClass(/revealed/);
  const contract=await node.evaluate(()=>({
   distance:getComputedStyle(document.documentElement).getPropertyValue('--w2-reveal-distance').trim(),
   duration:getComputedStyle(document.documentElement).getPropertyValue('--w2-reveal-duration').trim()
  }));
  expect(contract).toEqual({distance:'28px',duration:'650ms'});
 });

 test('movimiento reducido revela sin transicion',async({page})=>{
  await page.emulateMedia({reducedMotion:'reduce'});
  await page.goto('/w2/comunidad');
  const node=page.locator('[data-w2-reveal]').first();
  await expect(node).toHaveClass(/revealed/);
  const style=await node.evaluate(element=>getComputedStyle(element));
  expect(style.opacity).toBe('1');
  expect(style.transform).toBe('none');
  expect(style.transitionDuration).toBe('0s');
 });
});
