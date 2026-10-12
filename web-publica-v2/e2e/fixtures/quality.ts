import { expect, type Page, type TestInfo } from '@playwright/test';

export function monitorPage(page:Page,testInfo:TestInfo){
 const errors:string[]=[];
 const http:string[]=[];
 page.on('pageerror',e=>errors.push('pageerror: '+e.message));
 page.on('console',m=>{
  if(m.type()!=='error')return;
  const texto=m.text();
  if(/^Failed to load resource: the server responded with a status of (?:4\d\d)/.test(texto))return;
  errors.push('console.error: '+texto);
 });
 page.on('response',response=>{
  if(response.status()>=500)errors.push('http '+response.status()+': '+response.url());
  else if(response.status()>=400)http.push('http '+response.status()+': '+response.url());
 });
 return async()=>{
  await testInfo.attach('browser-errors',{body:errors.join('\n')||'Sin errores JavaScript ni respuestas 5xx',contentType:'text/plain'});
  await testInfo.attach('http-4xx-observados',{body:http.join('\n')||'Sin respuestas 4xx',contentType:'text/plain'});
  expect(errors,'La página no debe emitir errores JavaScript ni respuestas HTTP 5xx').toEqual([]);
 };
}
