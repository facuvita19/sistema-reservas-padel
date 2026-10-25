import {expect,test} from '@playwright/test';
import fs from 'node:fs';
import path from 'node:path';

const root=process.cwd();
const read=(relative:string)=>fs.readFileSync(path.join(root,relative),'utf8');
const layout=read('src/w2/layouts/W2Layout.astro');
const owners=['src/pages/w2/torneos.astro','src/pages/w2/comunidad.astro'];

test('7Z W2Layout deja de cargar filtros transversalmente',()=>{
 expect(layout).not.toContain('/w2/filter-system.css');
 expect(layout).not.toContain("import '../scripts/filter-system'");
});

test('7Z Torneos y Comunidad son propietarios explicitos del sistema',()=>{
 for(const owner of owners){
  const source=read(owner);
  expect(source,owner).toContain('W2_FILTER_SYSTEM_ROUTE_OWNER_7Z');
  expect(source,owner).toContain('/w2/filter-system.css');
  expect(source,owner).toContain("import '../../w2/scripts/filter-system'");
 }
});

test('7Z las rutas sin hosts no declaran recursos de filtros',async({page})=>{
 for(const route of ['/w2/','/w2/reservar/','/w2/rankings/','/w2/cuenta/']){
  await page.goto(route);
  await expect(page.locator('link[href="/w2/filter-system.css"]'),route).toHaveCount(0);
 }
});

test('7Z Torneos y Comunidad conservan CSS y runtime de filtros',async({page})=>{
 for(const route of ['/w2/torneos/','/w2/comunidad/']){
  await page.goto(route);
  await expect(page.locator('link[href="/w2/filter-system.css"]'),route).toHaveCount(1);
 }
});
