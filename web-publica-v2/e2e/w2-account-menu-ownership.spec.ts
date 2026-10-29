import {expect,test} from '@playwright/test';
import fs from 'node:fs';
import path from 'node:path';
const root=process.cwd();
const auth=fs.readFileSync(path.join(root,'src','scripts','autenticacion.ts'),'utf8');
const menu=fs.readFileSync(path.join(root,'src','scripts','menu-cuenta-global.ts'),'utf8');
test('H3 concentra apertura posicionamiento y teclado en menu-cuenta-global',()=>{expect(menu).toContain('W2_ACCOUNT_MENU_AUTHORITATIVE_H3');expect(menu).toContain("trigger?.addEventListener('click'");expect(menu).toContain('positionDesktop');expect(menu).toMatch(/event\.key\s*===\s*'ArrowDown'/);expect(menu).toContain("addEventListener('scroll'")});
test('8Q autenticacion delega cierres y no conserva listeners historicos',()=>{expect(auth).toContain('w2-account-menu-close');expect(auth).not.toContain('botonCuenta.addEventListener');expect(auth).not.toContain('posicionarMenuCuenta');expect(auth).not.toContain('itemsMenuCuenta');expect(auth).not.toContain('function abrirMenu')});
