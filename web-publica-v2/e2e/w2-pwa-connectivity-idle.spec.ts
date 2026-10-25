import {expect,test} from '@playwright/test';
import fs from 'node:fs';
import path from 'node:path';

const source=fs.readFileSync(path.join(process.cwd(),'public','w2','pwa-register.js'),'utf8');

test('7P difiere la resincronizacion online hasta tiempo ocioso con fallback',()=>{
  expect(source).toContain('sincronizacion de conectividad en tiempo ocioso 7P');
  expect(source).toContain("'requestIdleCallback' in window");
  expect(source).toContain('window.requestIdleCallback(syncConnection, { timeout: 2600 })');
  expect(source).toContain('setTimeout(syncConnection, 2600)');
});

test('7P conserva un unico listener online y offline',()=>{
  expect((source.match(/addEventListener\(['"]online['"]/g)||[]).length).toBe(1);
  expect((source.match(/addEventListener\(['"]offline['"]/g)||[]).length).toBe(1);
});

test('7P preserva actualizaciones por visibilidad y service worker',()=>{
  expect(source).toContain('actualizaciones PWA condicionadas por visibilidad 7M');
  expect(source).toContain("document.addEventListener('visibilitychange'");
  expect(source).toContain('navigator.serviceWorker.register');
  const worker=fs.readFileSync(path.join(process.cwd(),'public','w2','sw.js'),'utf8');
  expect(worker).toContain("self.addEventListener('fetch'");
});
