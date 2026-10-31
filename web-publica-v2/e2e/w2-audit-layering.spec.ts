import{test,expect}from'@playwright/test';import type{Page}from'@playwright/test';

async function mockAuditoriaRankings(page:Page){
 const categorias=[
  {id:61,codigo:'5TA',nombre:'5ta',color:'#d7192d',participantes:1},
  {id:63,codigo:'4TA',nombre:'4ta',color:'#d7192d',participantes:1}
 ];
 await page.route('**/api/publica/rankings/categorias',route=>route.fulfill({json:categorias}));
 await page.route('**/api/publica/rankings?**',route=>{
  const id=Number(new URL(route.request().url()).searchParams.get('categoriaId'))||61;
  const facundo=id===63;
  const fila={posicion:1,perfilId:facundo?7999:7001,jugador:facundo?'Facundo Vitale':'Ana Vitale',iniciales:facundo?'FV':'AV',puntos:24,partidos:6,victorias:5,derrotas:1,efectividad:83,diferenciaSets:8,diferenciaGames:21};
  const categoria=categorias.find(x=>x.id===id)||categorias[0];
  return route.fulfill({json:{categoria,participantes:1,partidosAuditados:6,ultimoResultado:'2026-10-30T12:00:00Z',posiciones:[fila]}});
 });
}


test.describe('T1D auditoria por encima de la cabecera',()=>{
 test('invitado puede cerrar auditoria directa sin intercepcion',async({page})=>{
  await mockAuditoriaRankings(page);
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
  await mockAuditoriaRankings(page);
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
