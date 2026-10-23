import fs from 'node:fs';
import { spawnSync } from 'node:child_process';
import path from 'node:path';
const modo=['dev','device'].includes(process.argv[2])?process.argv[2]:'prod';
const host=process.argv[3]?.trim();
if(modo==='device'&&!/^(?:10\.|192\.168\.|172\.(?:1[6-9]|2\d|3[01])\.)\d{1,3}\.\d{1,3}$/.test(host||''))throw new Error('Indica una IPv4 privada valida para el modo device.');
const env={...process.env};
if(modo==='dev'||modo==='device')env.CAPACITOR_W2_DEV='1';else delete env.CAPACITOR_W2_DEV;
if(modo==='device')env.CAPACITOR_W2_DEVICE_HOST=host;else delete env.CAPACITOR_W2_DEVICE_HOST;
const r=spawnSync(process.execPath,[path.join(process.cwd(),'node_modules','@capacitor','cli','bin','capacitor'),'sync','android'],{stdio:'inherit',env,shell:false});
if(r.error)throw r.error;
if(r.status!==0)process.exit(r.status??1);
if(modo==='device'){
  const network=path.join(process.cwd(),'android','app','src','main','res','xml','network_security_config.xml');
  const xml=`<?xml version="1.0" encoding="utf-8"?>\n<network-security-config>\n    <base-config cleartextTrafficPermitted="false" />\n    <domain-config cleartextTrafficPermitted="true">\n        <domain includeSubdomains="false">${host}</domain>\n    </domain-config>\n</network-security-config>\n`;
  fs.writeFileSync(network,xml,'utf8');
  console.log(`CAPACITOR_W2_DISPOSITIVO_HOST_${host}_OK`);
}
console.log(modo==='dev'?'CAPACITOR_W2_DESARROLLO_SINCRONIZADO_OK':modo==='device'?'CAPACITOR_W2_DISPOSITIVO_SINCRONIZADO_OK':'CAPACITOR_W2_PRODUCCION_SINCRONIZADA_OK');
