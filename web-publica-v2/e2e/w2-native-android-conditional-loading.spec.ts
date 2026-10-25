import {test,expect} from '@playwright/test';
import fs from 'node:fs';
import path from 'node:path';

const root=path.resolve(process.cwd());
const layoutPath=path.join(root,'src','w2','layouts','W2Layout.astro');

test('8M W2Layout conserva entry sincrono y separa runtime Android',()=>{
 const source=fs.readFileSync(layoutPath,'utf8');
 expect(source).toContain('/w2/native-entry-head.js');
 expect(source).not.toContain("import '../scripts/native-android';");
 expect(source).toContain("void import('../scripts/native-android')");
 expect(source).toContain("location.protocol==='capacitor:'");
 expect(source).toContain("location.hostname==='10.0.2.2'");
});

test('8M la web ordinaria no solicita el runtime Android',async({page})=>{
 const requests:string[]=[];
 page.on('request',request=>requests.push(new URL(request.url()).pathname));
 await page.goto('/w2/',{waitUntil:'networkidle'});
 expect(requests.some(value=>value.includes('native-android'))).toBeFalsy();
});

test('8M el host WebView solicita el runtime Android de forma condicional',async({page})=>{
 await page.addInitScript(()=>{
  const original=navigator.userAgent;
  Object.defineProperty(navigator,'userAgent',{configurable:true,get:()=>original+'; wv)'});
 });
 const requests:string[]=[];
 page.on('request',request=>requests.push(new URL(request.url()).pathname));
 await page.goto('/w2/',{waitUntil:'networkidle'});
 await expect.poll(()=>requests.some(value=>value.includes('native-android'))).toBeTruthy();
});
