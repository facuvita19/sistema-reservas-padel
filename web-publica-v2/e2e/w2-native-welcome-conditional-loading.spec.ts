import {expect,test} from '@playwright/test';
import fs from 'node:fs';
import path from 'node:path';

const root=process.cwd();
const layout=fs.readFileSync(path.join(root,'src','w2','layouts','W2Layout.astro'),'utf8');
const entry=fs.readFileSync(path.join(root,'public','w2','native-entry-head.js'),'utf8');

test('7S la web ordinaria no declara recursos de bienvenida nativa',async({page})=>{
  expect(layout).not.toContain('<link rel="stylesheet" href="/w2/native-welcome.css"/>');
  expect(layout).not.toContain('<script type="module" src="/w2/native-welcome.js"></script>');
  await page.goto('/w2/');
  await expect(page.locator('link[href="/w2/native-welcome.css"]')).toHaveCount(0);
  await expect(page.locator('script[src="/w2/native-welcome.js"]')).toHaveCount(0);
  await expect(page.locator('#w2NativeWelcome')).toHaveCount(0);
});

test('7S el entry conserva orden y carga exclusiva del host nativo',()=>{
  expect(entry).toContain('carga exclusiva de bienvenida para host nativo 7S');
  expect(entry).toContain("if(!native)return");
  expect(entry).toContain("root.classList.add('w2-native-welcome-pending')");
  expect(entry).toContain("['/w2/native-welcome.css','welcome']");
  expect(entry).toContain("script.src='/w2/native-welcome.js'");
  expect(entry.indexOf("'/w2/native-welcome.css'")).toBeLessThan(entry.indexOf("script.src='/w2/native-welcome.js'"));
});

test('7S los recursos nativos siguen publicados',async({request})=>{
  expect((await request.get('/w2/native-welcome.css')).ok()).toBeTruthy();
  expect((await request.get('/w2/native-welcome.js')).ok()).toBeTruthy();
});
