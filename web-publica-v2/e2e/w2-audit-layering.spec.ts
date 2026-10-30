import{test,expect}from'@playwright/test';

test.describe('T1D auditoria por encima de la cabecera',()=>{
 test('invitado puede cerrar auditoria directa sin intercepcion',async({page})=>{
  await page.goto('/w2/rankings?categoria=61&buscar=ana&auditoria=7001&auditoriaNombre=Ana%20Vitale&auditoriaIniciales=AV');
  const drawer=page.locator('#w2rAuditDrawer'),close=page.locator('#w2rAuditClose');
  await expect(drawer).toBeVisible();
  const layers=await page.evaluate(()=>({
   drawer:Number.parseInt(getComputedStyle(document.querySelector('#w2rAuditDrawer')!).zIndex,10),
   header:Number.parseInt(getComputedStyle(document.querySelector('.w2-header')!).zIndex,10),
   headerPointer:getComputedStyle(document.querySelector('.w2-header')!).pointerEvents
  }));
  expect(layers.drawer).toBeGreaterThan(layers.header);
  expect(layers.headerPointer).toBe('none');
  await close.click();
  await expect(page).toHaveURL(/\/w2\/rankings\?categoria=\d+&buscar=ana$/);
  expect(new URL(page.url()).searchParams.has('auditoria')).toBeFalsy();
 });

 test('cierre heredado vuelve a Mi cuenta',async({page})=>{
  await page.goto('/w2/rankings?categoria=63&auditoria=7999&auditoriaNombre=Facundo%20Vitale&auditoriaIniciales=FV&volver=%2Fw2%2Fcuenta%3Fseccion%3Ddeporte');
  await expect(page.locator('#w2rAuditDrawer')).toBeVisible();
  await page.locator('#w2rAuditClose').click();
  await expect(page).toHaveURL('/w2/cuenta?seccion=deporte');
 });

 test('contrato CSS mantiene la geometria de cabecera al abrir Cuenta',async({page})=>{
  await page.goto('/w2/comunidad');
  const contract=await page.evaluate(()=>{
   document.body.classList.add('account-menu-open');
   const header=document.querySelector<HTMLElement>('.w2-header')!;
   const style=getComputedStyle(header),rect=header.getBoundingClientRect();
   document.body.classList.remove('account-menu-open');
   return{position:style.position,top:style.top,paddingTop:style.paddingTop,paddingBottom:style.paddingBottom,height:rect.height};
  });
  expect(contract).toEqual({position:'sticky',top:'0px',paddingTop:'10px',paddingBottom:'8px',height:94});
 });
});
