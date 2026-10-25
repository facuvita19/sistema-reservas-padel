import {expect,test} from '@playwright/test';
import fs from 'node:fs';
import path from 'node:path';

const root=path.resolve(process.cwd());
const styles=path.join(root,'src','w2','styles');
const layout=path.join(root,'src','w2','layouts','W2Layout.astro');
const authoritative='w2-header-authoritative.css';
const historical=["w2-global-header.css","w2-header-concept-v2.css","w2-header-concept-v3.css","w2-header-concept-v4.css","w2-header-concept-v5.css","w2-header-concept-v7.css"];

test('7J la cabecera global tiene una unica hoja autoritativa',()=>{
 const source=fs.readFileSync(layout,'utf8');
 expect(source).toContain(`import '../styles/${authoritative}';`);
 for(const name of historical)expect(source).not.toContain(`import '../styles/${name}';`);
 expect(fs.existsSync(path.join(styles,authoritative))).toBe(true);
});

test('7J los respaldos historicos ya no existen en el arbol fuente',()=>{
 for(const name of historical)expect(fs.existsSync(path.join(styles,name)),name).toBe(false);
});

test('7J la hoja autoritativa conserva procedencia y cierres validados',()=>{
 const source=fs.readFileSync(path.join(styles,authoritative),'utf8');
 expect(source).toContain('CABECERA W2 AUTORITATIVA 7H');
 expect(source).toContain('limpieza importance-aware de cabecera global 7D');
 expect(source).toContain('limpieza de declaraciones parciales de cabecera global 7F');
 for(const name of historical)expect(source).toContain(`Fuente convergida: ${name}`);
});
