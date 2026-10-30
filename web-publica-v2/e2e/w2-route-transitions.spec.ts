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

test.describe('T1B navegacion segura',()=>{
 test('muestra indicador solo tras una demora real',async({page})=>{
  await page.goto('/w2/');
  await page.evaluate(()=>{
   document.addEventListener('click',event=>{
    const anchor=(event.target as Element | null)?.closest('a[href="/w2/torneos"]');
    if(anchor)event.preventDefault();
   },{once:true});
  });
  await page.locator('.w2-links a[href="/w2/torneos"]').click();
  await expect(page.locator('html')).toHaveAttribute('data-w2-navigation','pending',{timeout:700});
  await expect(page).toHaveURL(/\/w2\/?$/);
 });

 test('excluye enlaces externos, nueva pestana, descargas y anclas',async({page})=>{
  await page.goto('/w2/');
  const excluded:[string,string,string][]=[
   ['t1bExternal','https://example.com',''],
   ['t1bBlank','/w2/rankings','blank'],
   ['t1bDownload','/w2/rankings','download'],
   ['t1bHash','#w2-content','']
  ];
  for(const[id,href,extra]of excluded){
   await page.evaluate(({id,href,extra}:{id:string;href:string;extra:string})=>{
    const a=document.createElement('a');a.id=id;a.href=href;a.textContent=id;
    if(extra==='blank')a.target='_blank';
    if(extra==='download')a.setAttribute('download','');
    a.addEventListener('click',event=>event.preventDefault());
    document.body.append(a);
   },{id,href,extra});
   await page.locator('#'+id).click();
   await page.waitForTimeout(230);
   await expect(page.locator('html')).not.toHaveAttribute('data-w2-navigation','pending');
  }
 });

 test('ignora una navegacion interna cancelada por otro componente',async({page})=>{
  await page.goto('/w2/');
  await page.evaluate(()=>{
   const a=document.createElement('a');a.id='t1bCancelled';a.href='/w2/comunidad';a.textContent='Cancelado';
   a.addEventListener('click',event=>event.preventDefault());document.body.append(a);
  });
  await page.locator('#t1bCancelled').click();
  await page.waitForTimeout(230);
  await expect(page).toHaveURL(/\/w2\/?$/);
  await expect(page.locator('html')).not.toHaveAttribute('data-w2-navigation');
 });
});
