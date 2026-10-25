import {expect,test} from '@playwright/test';
import fs from 'node:fs';
import path from 'node:path';

const root=process.cwd();
const layout=fs.readFileSync(path.join(root,'src','w2','layouts','W2Layout.astro'),'utf8');
const entry=fs.readFileSync(path.join(root,'public','w2','native-entry-head.js'),'utf8');

test('7V la web ordinaria no carga estilos exclusivos del host nativo',async({page})=>{
 expect(layout).not.toContain('/w2/native-mobile.css');
 expect(layout).not.toContain('/w2/cap3b-native.css');
 await page.goto('/w2/');
 await expect(page.locator('link[href="/w2/native-mobile.css"]')).toHaveCount(0);
 await expect(page.locator('link[href="/w2/cap3b-native.css"]')).toHaveCount(0);
});

test('7V conserva el orden completo de la cascada nativa',()=>{
 const resources=['/w2/native-mobile.css','/w2/cap3b-native.css','/w2/native-welcome.css','/w2/native-welcome.js'];
 const positions=resources.map(resource=>entry.indexOf(resource));
 expect(positions.every(position=>position>=0)).toBe(true);
 expect(positions).toEqual([...positions].sort((a,b)=>a-b));
 expect(entry).toContain('carga exclusiva de estilos nativos 7V');
});

test('7V mantiene publicados los estilos nativos',async({request})=>{
 expect((await request.get('/w2/native-mobile.css')).ok()).toBeTruthy();
 expect((await request.get('/w2/cap3b-native.css')).ok()).toBeTruthy();
});
