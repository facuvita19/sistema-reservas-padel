export type PerfilSesion={clienteId:number;nombre:string;apellido:string;email?:string;telefono?:string;posicionPreferida?:'DRIVE'|'REVES'|null};
export type SesionGlobal={autenticado:boolean;vencimiento?:string|null;perfil?:PerfilSesion|null};
type Estado={sesion:SesionGlobal|null;cargando:boolean;error:boolean;version:number};
const KEY='padel.sesion.snapshot.v1';
const state:Estado={sesion:null,cargando:true,error:false,version:0};
try{const raw=sessionStorage.getItem(KEY);if(raw){const cached=JSON.parse(raw) as SesionGlobal;const vence=cached.vencimiento?Date.parse(cached.vencimiento):NaN;if(cached.autenticado&&cached.perfil&&(!Number.isFinite(vence)||vence>Date.now()))state.sesion=cached;else sessionStorage.removeItem(KEY)}}catch{try{sessionStorage.removeItem(KEY)}catch{}}
function same(a:SesionGlobal|null,b:SesionGlobal){return Boolean(a&&a.autenticado===b.autenticado&&a.perfil?.clienteId===b.perfil?.clienteId&&a.perfil?.nombre===b.perfil?.nombre&&a.perfil?.apellido===b.perfil?.apellido&&a.perfil?.email===b.perfil?.email)}
export function estadoSesion(){return {...state}}
export function publicarSesion(s:SesionGlobal,force=false){const changed=!same(state.sesion,s);state.sesion=s;state.cargando=false;state.error=false;if(s.autenticado)sessionStorage.setItem(KEY,JSON.stringify(s));else sessionStorage.removeItem(KEY);if(changed||force){state.version++;dispatchEvent(new CustomEvent('sesion-cliente-cambiada',{detail:s}))}document.documentElement.dataset.session=s.autenticado?'authenticated':'guest';return s}
export function marcarErrorSesion(){state.cargando=false;state.error=true;dispatchEvent(new CustomEvent('sesion-cliente-error',{detail:{sesion:state.sesion,preservada:Boolean(state.sesion?.autenticado)}}))}
export function escucharSesion(listener:(s:SesionGlobal)=>void){const handler=(e:Event)=>listener((e as CustomEvent<SesionGlobal>).detail);addEventListener('sesion-cliente-cambiada',handler);if(state.sesion)queueMicrotask(()=>listener(state.sesion!));return()=>removeEventListener('sesion-cliente-cambiada',handler)}
export function actualizarPerfilSesion(perfil:PerfilSesion){if(!state.sesion?.autenticado)return;publicarSesion({...state.sesion,perfil:{...state.sesion.perfil,...perfil}},true)}
