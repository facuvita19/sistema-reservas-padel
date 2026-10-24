import {expect,test} from '@playwright/test';

test.skip(({isMobile})=>isMobile,'Contrato de ancho para escritorio.');

const snapshot={autenticado:true,vencimiento:'2099-12-31T23:59:59Z',perfil:{clienteId:77,nombre:'Facundo',apellido:'Vitale',email:'facundo@example.com'}};

test.beforeEach(async({page})=>{await page.addInitScript(s=>{localStorage.setItem('w2-theme','dark');sessionStorage.setItem('padel.sesion.snapshot.v1',JSON.stringify(s))},snapshot)});

test('Inicio y Torneos conservan el mismo ancho global',async({page})=>{
 let reference:null|{html:number;body:number;client:number;navX:number}=null;
 for(const route of ['/w2/','/w2/torneos']){
  await page.goto(route,{waitUntil:'networkidle'});
  const current=await page.evaluate(()=>{const nav=document.querySelector('.w2-nav')!.getBoundingClientRect();return{html:document.documentElement.getBoundingClientRect().width,body:document.body.getBoundingClientRect().width,client:document.documentElement.clientWidth,navX:nav.x}});
  if(!reference)reference=current;
  else{
   expect(current.html,route+' html').toBe(reference.html);
   expect(current.body,route+' body').toBe(reference.body);
   expect(current.client,route+' client').toBe(reference.client);
   expect(Math.abs(current.navX-reference.navX),route+' nav').toBeLessThanOrEqual(.5);
  }
 }
});

test('Torneos no modifica la scrollbar del documento',async({page})=>{
 await page.goto('/w2/torneos',{waitUntil:'networkidle'});
 const values=await page.evaluate(()=>({html:getComputedStyle(document.documentElement).scrollbarWidth,body:getComputedStyle(document.body).scrollbarWidth}));
 expect(values.html).not.toBe('thin');
 expect(values.body).not.toBe('thin');
});

test('los paneles internos conservan scrollbar acotada',async({page})=>{
 await page.goto('/w2/torneos',{waitUntil:'domcontentloaded'});
 const css=await page.locator('body').evaluate(()=>[...document.styleSheets].flatMap(sheet=>{try{return [...sheet.cssRules].map(r=>r.cssText)}catch{return[]}}).join('\n'));
 expect(css).toContain('#modalCuadroTorneo');
 expect(css).toContain('scrollbar-width: thin');
});
