/* W2_AUTH_LAZY_8V: nucleo global de sesion y cargador interactivo. */
import { publicarSesion, marcarErrorSesion, actualizarPerfilSesion, estadoSesion } from './sesion-global';
import { api } from './api';
export type Perfil={clienteId:number;nombre:string;apellido:string;email:string;telefono:string;posicionPreferida?:'DRIVE'|'REVES'|null};
export type Sesion={autenticado:boolean;vencimiento?:string|null;perfil?:Perfil|null};
const e=<T extends HTMLElement>(id:string)=>document.getElementById(id) as T;
export const cerrarMenuCuenta=(focus=false)=>dispatchEvent(new CustomEvent('w2-account-menu-close',{detail:{focus}}));
export const resolverEntrada=(sesion:Sesion,origen:'restauracion'|'login'|'registro'|'logout',error=false)=>{document.documentElement.dataset.w2SessionResolved='true';dispatchEvent(new CustomEvent('sesion-cliente-resuelta',{detail:{...sesion,origen,error}}))};
export function actualizarSesion(s:Sesion){const login=e('abrirAutenticacion'),menu=e('menuUsuario'),movil=e('abrirAutenticacionMovil'),cuentaMovil=document.getElementById('cuentaUsuarioMovil');const autenticado=Boolean(s.autenticado&&s.perfil);login.classList.toggle('hidden',autenticado);movil.classList.toggle('hidden',autenticado);menu.classList.toggle('hidden',!autenticado);cuentaMovil?.classList.toggle('hidden',!autenticado);if(!autenticado)cerrarMenuCuenta(false);if(autenticado&&s.perfil){const p=s.perfil,nombre=p.nombre||'Cuenta',iniciales=((p.nombre?.[0]||'')+(p.apellido?.[0]||'')).toUpperCase()||'PR';['nombreUsuario','nombreUsuarioMovil','nombreUsuarioMenu'].forEach(id=>{const x=document.getElementById(id);if(x)x.textContent=nombre});['avatarUsuario','avatarUsuarioMovil','avatarUsuarioMenu'].forEach(id=>{const x=document.getElementById(id);if(x)x.textContent=iniciales});const correo=document.getElementById('correoUsuarioMenu');if(correo)correo.textContent=p.email||'Tu perfil de jugador';const correoMovil=document.getElementById('correoUsuarioMovil');if(correoMovil)correoMovil.textContent=p.email||'Sesión activa'}publicarSesion(s)}
let verificacionSesion:Promise<void>|null=null;
async function verificarSesion(){let ultimo:unknown;try{const respuesta=await api<Sesion>('/auth/sesion',{cacheMs:0,forceRefresh:true});actualizarSesion(respuesta);resolverEntrada(respuesta,'restauracion');return}catch(error){ultimo=error}const previa=estadoSesion().sesion;marcarErrorSesion();if(previa?.autenticado&&previa.perfil){actualizarSesion(previa as Sesion);resolverEntrada(previa as Sesion,'restauracion',true);return}const invitado:Sesion={autenticado:false};actualizarSesion(invitado);resolverEntrada(invitado,'restauracion',true);console.warn('No se pudo verificar la sesión.',ultimo)}
function sesion(){return verificacionSesion??=verificarSesion().finally(()=>{verificacionSesion=null})}
let modalPromise:Promise<typeof import('./autenticacion-modal')>|null=null;
const cargarModal=()=>modalPromise??=import('./autenticacion-modal');
const solicitarApertura=(event:Event)=>{event.preventDefault();const trigger=event.currentTarget as HTMLElement;void cargarModal().then(modulo=>modulo.abrirAutenticacion(trigger))};
e('abrirAutenticacion').addEventListener('click',solicitarApertura);
e('abrirAutenticacionMovil').addEventListener('click',solicitarApertura);
async function cerrarSesionCliente(button:HTMLButtonElement){try{button.disabled=true;button.textContent='Cerrando...';await api('/auth/logout',{method:'POST'});const salida:Sesion={autenticado:false};actualizarSesion(salida);resolverEntrada(salida,'logout');try{sessionStorage.removeItem('w2.native.guest.runtime')}catch{}cerrarMenuCuenta(false);location.href=document.documentElement.dataset.logoutRedirect||'/'}catch{button.disabled=false;button.textContent='Reintentar cierre de sesión'}}
e('cerrarSesion').addEventListener('click',()=>cerrarSesionCliente(e<HTMLButtonElement>('cerrarSesion')));
document.getElementById('cerrarSesionMovil')?.addEventListener('click',()=>cerrarSesionCliente(e<HTMLButtonElement>('cerrarSesionMovil')));
const snapshot=estadoSesion().sesion as Sesion|null;if(snapshot?.autenticado&&snapshot.perfil)actualizarSesion(snapshot);
const recuperar=new URLSearchParams(location.search).get('recuperar');if(recuperar)location.replace('/restablecer?token='+encodeURIComponent(recuperar));else void sesion();
addEventListener('perfil-cliente-actualizado',(event:any)=>{if(event.detail){actualizarPerfilSesion(event.detail);actualizarSesion(estadoSesion().sesion as Sesion)}});
