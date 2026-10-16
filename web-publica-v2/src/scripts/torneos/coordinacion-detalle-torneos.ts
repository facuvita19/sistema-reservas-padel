import type { TorneoDetalle } from '../api';
import type { SolicitudTorneos } from './datos-torneos';
import { detallePremium, renderPanelDetalleTorneo, type PestanaDetalle } from './presentacion-detalle-torneo';

type Dependencias={
 nuevaSolicitud:()=>SolicitudTorneos;
 obtener:(id:number,signal:AbortSignal)=>Promise<TorneoDetalle>;
 vigente:(version:number)=>boolean;
 abortada:(error:unknown)=>boolean;
 restaurarCatalogo:()=>void;
 movimientoReducido:()=>boolean;
 escapeHtml:(valor:unknown)=>string;
};
type EstadoScrollDetalle={mover:boolean;y:number};
const pestanas:PestanaDetalle[]=['informacion','categorias','reglamento'];

export function crearCoordinacionDetalleTorneos(d:Dependencias){
 let actual:TorneoDetalle|null=null;
 let pestana:PestanaDetalle='informacion';
 const torneoActual=()=>actual;
 const renderPanel=()=>{if(actual)renderPanelDetalleTorneo(actual,pestana)};
 const seleccionarPestana=(tab:PestanaDetalle,actualizarUrl=true)=>{if(!actual)return;pestana=pestanas.includes(tab)?tab:'informacion';renderPanel();if(actualizarUrl){const u=new URL(location.href);u.searchParams.set('seccion',pestana);history.replaceState(history.state,'',u)}requestAnimationFrame(()=>document.getElementById('premiumDetailTabs')?.scrollIntoView({behavior:d.movimientoReducido()?'auto':'smooth',block:'start'}))};
 const compartir=async()=>{try{if(navigator.share)await navigator.share({title:actual?.nombre||'Torneo',url:location.href});else{await navigator.clipboard.writeText(location.href);const boton=document.querySelector<HTMLButtonElement>('[data-share-tournament]');if(boton){const original=boton.textContent;boton.textContent='Enlace copiado';setTimeout(()=>boton.textContent=original,1800)}}}catch{}};
 const abrir=async(id:number,actualizarUrl=true)=>{const catalogo=document.getElementById('vistaCatalogoTorneos')!,detalle=document.getElementById('vistaDetalleTorneo')!,host=document.getElementById('detalleTorneoHost')!;catalogo.classList.add('hidden');detalle.classList.remove('hidden');host.setAttribute('aria-busy','true');host.innerHTML='<div class="competition-loading">Cargando torneo...</div>';if(actualizarUrl){const u=new URL(location.href);u.searchParams.set('torneo',String(id));['seccion','categoria','vista','grupo','instancia','ronda'].forEach(x=>u.searchParams.delete(x));history.pushState({nivel:'detalle',torneo:id},'',u)}const solicitud=d.nuevaSolicitud();try{const torneo=await d.obtener(id,solicitud.controlador.signal);if(!d.vigente(solicitud.version))return;actual=torneo;const solicitada=new URLSearchParams(location.search).get('seccion') as PestanaDetalle|null;pestana=solicitada&&pestanas.includes(solicitada)?solicitada:'informacion';host.removeAttribute('aria-busy');host.innerHTML=detallePremium(torneo,pestana);document.title=`${torneo.nombre} | Plataforma Deportiva W2`;scrollTo({top:0,behavior:d.movimientoReducido()?'auto':'smooth'})}catch(error){if(!d.vigente(solicitud.version)||d.abortada(error))return;host.removeAttribute('aria-busy');host.innerHTML=`<div class="competition-error"><b>No pudimos abrir el torneo</b><p>${d.escapeHtml(error instanceof Error?error.message:'Intentá nuevamente.')}</p><button class="btn btn-secondary" type="button" data-retry-torneo="${id}">Reintentar</button></div>`}};
 const volver=(actualizarUrl=true)=>{actual=null;document.getElementById('vistaDetalleTorneo')?.classList.add('hidden');document.getElementById('vistaCatalogoTorneos')?.classList.remove('hidden');document.title='Torneos | Plataforma Deportiva W2';if(actualizarUrl){const u=new URL(location.href);['torneo','seccion','categoria','vista','grupo','instancia','ronda'].forEach(x=>u.searchParams.delete(x));history.pushState({},'',u)}d.restaurarCatalogo()};
 const estadoScroll=():EstadoScrollDetalle=>{const contenido=document.getElementById('premiumDetailContent'),tabs=document.querySelector<HTMLElement>('.premium-detail-tabs'),y=scrollY;if(!contenido||!tabs)return {mover:false,y};const rTabs=tabs.getBoundingClientRect(),rContenido=contenido.getBoundingClientRect(),top=parseFloat(getComputedStyle(tabs).top)||0;return {mover:rTabs.top<=top+3&&rContenido.top<rTabs.bottom-Math.max(32,rTabs.height*.55),y}};
 const estabilizarScroll=(estado:EstadoScrollDetalle)=>requestAnimationFrame(()=>requestAnimationFrame(()=>{if(!estado.mover){scrollTo({top:estado.y,behavior:'auto'});return}const contenido=document.getElementById('premiumDetailContent'),tabs=document.querySelector<HTMLElement>('.premium-detail-tabs');if(!contenido||!tabs)return;const destino=Math.max(0,contenido.getBoundingClientRect().top+scrollY-tabs.offsetHeight-8);scrollTo({top:destino,behavior:d.movimientoReducido()?'auto':'smooth'})}));
 const irAlContenido=()=>requestAnimationFrame(()=>requestAnimationFrame(()=>{const contenido=document.getElementById('premiumDetailContent'),tabs=document.querySelector<HTMLElement>('.w2td-tabs,.premium-detail-tabs'),cabecera=document.querySelector<HTMLElement>('[data-w2-header],header[data-site-header]');if(!contenido)return;const destino=Math.max(0,contenido.getBoundingClientRect().top+scrollY-(cabecera?.getBoundingClientRect().height||0)-(tabs?.getBoundingClientRect().height||0)-12);scrollTo({top:destino,behavior:d.movimientoReducido()?'auto':'smooth'})}));
 const manejarClick=(target:HTMLElement)=>{if(target.closest('[data-back-catalog]')){volver();return true}if(target.closest('[data-share-tournament]')){void compartir();return true}if(target.closest('[data-go-categories]')){seleccionarPestana('categorias');irAlContenido();return true}if(target.closest('[data-go-rules]')){seleccionarPestana('reglamento');irAlContenido();return true}const tab=target.closest<HTMLButtonElement>('[data-detail-tab]');if(tab){const estado=estadoScroll();seleccionarPestana(tab.dataset.detailTab as PestanaDetalle);estabilizarScroll(estado);return true}return false};
 const restaurarDesdeCentro=(torneo:TorneoDetalle)=>{actual=torneo;pestana='categorias';document.getElementById('detalleTorneoHost')!.innerHTML=detallePremium(torneo,pestana);seleccionarPestana('categorias',false)};
 return {abrir,volver,manejarClick,restaurarDesdeCentro,torneoActual,seleccionarPestana};
}
