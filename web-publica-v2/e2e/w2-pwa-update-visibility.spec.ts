import {expect,test} from '@playwright/test';
import fs from 'node:fs';
import path from 'node:path';

const source=fs.readFileSync(path.join(process.cwd(),'public','w2','pwa-register.js'),'utf8');

test('7M pausa comprobaciones PWA cuando el documento queda oculto',()=>{
 expect(source).toContain('actualizaciones PWA condicionadas por visibilidad 7M');
 expect(source).toContain("document.addEventListener('visibilitychange'");
 expect(source).toContain("document.visibilityState === 'visible'");
 expect(source).toContain('clearInterval(updateTimer)');
});

test('7M mantiene un unico intervalo horario sin tocar el service worker',()=>{
 expect((source.match(/setInterval\s*\(/g)||[]).length).toBe(1);
 expect(source).toContain('60 * 60 * 1000');
 expect(source).toContain('registration.update()');
 const worker=fs.readFileSync(path.join(process.cwd(),'public','w2','sw.js'),'utf8');
 expect(worker).toContain("self.addEventListener('fetch'");
});
