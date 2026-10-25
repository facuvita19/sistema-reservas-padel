import {expect,test} from '@playwright/test';
import fs from 'node:fs';
import path from 'node:path';

const source=fs.readFileSync(path.join(process.cwd(),'src','w2','scripts','notificaciones.ts'),'utf8');

test('8D programa la precarga autenticada en tiempo ocioso con fallback',()=>{
 expect(source).toContain('precarga ociosa cancelable de notificaciones 8D');
 expect(source).toContain("'requestIdleCallback' in window");
 expect(source).toContain('window.requestIdleCallback(run,{timeout:1800})');
 expect(source).toContain('window.setTimeout(run,600)');
 expect(source).toContain("addEventListener('sesion-cliente-resuelta'");
  expect(source).toContain('if(detail?.autenticado)schedulePreload()');
  expect(source).not.toContain('if(s.autenticado)schedulePreload()');
});

test('8D cancela la precarga y reinicia vigencia al cerrar sesion',()=>{
 expect(source).toContain('cancelIdleCallback');
 expect(source).toContain('clearTimeout(preloadTimer)');
 expect(source).toContain('const resetNotifications=()=>{cancelPreload();revision++;loadedAt=0');
});

test('8D mantiene carga inmediata al abrir el centro y DOMPurify lazy',()=>{
 expect(source).toMatch(/function open\(\)[\s\S]*?void load\(false\)/);
 expect(source).toContain("import('dompurify')");
 expect(source).toContain('Date.now()-loadedAt<30_000');
});
