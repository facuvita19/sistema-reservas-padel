import { spawnSync } from 'node:child_process';
import path from 'node:path';
const modo=process.argv[2]==='dev'?'dev':'prod';
const env={...process.env};
if(modo==='dev')env.CAPACITOR_W2_DEV='1';else delete env.CAPACITOR_W2_DEV;
const r=spawnSync(process.execPath,[path.join(process.cwd(),'node_modules','@capacitor','cli','bin','capacitor'),'sync','android'],{stdio:'inherit',env,shell:false});
if(r.error)throw r.error;
if(r.status!==0)process.exit(r.status??1);
console.log(modo==='dev'?'CAPACITOR_W2_DESARROLLO_SINCRONIZADO_OK':'CAPACITOR_W2_PRODUCCION_SINCRONIZADA_OK');
