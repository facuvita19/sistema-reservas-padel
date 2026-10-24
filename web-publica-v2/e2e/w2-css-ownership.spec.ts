import {expect,test} from '@playwright/test';
import fs from 'node:fs';
import path from 'node:path';
const root=process.cwd();
const monoliths=['src/w2/styles/w2.css','src/w2/styles/w2-auth.css'];
const forbidden=['.w2-header','.w2-nav','.w2-brand','.w2-links','.w2-theme-toggle','.w2-cta','.w2-menu-button','.w2-auth-slot','.w2-session-check','.w2-login','.w2-notifications','.w2-notification','.w2-account','.w2-menu','.w2-mobile-account'];
test('los monolitos ya no gobiernan contratos globales',()=>{for(const file of monoliths){const css=fs.readFileSync(path.join(root,file),'utf8');for(const selector of forbidden)expect(css.includes(selector),file+' conserva '+selector).toBe(false)}});
test('la cabecera no recibe transformaciones GPU historicas',()=>{const css=fs.readFileSync(path.join(root,'src/w2/styles/w2-tournaments.css'),'utf8');expect(css).not.toContain('header[data-w2-header]');expect(css).not.toContain('header[data-site-header]')});
