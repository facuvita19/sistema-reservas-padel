import type { TorneoDetalle, TorneoResumen } from '../api';
import type { VistaCentro } from '../centro-competitivo-premium';
import type { SolicitudTorneos } from './datos-torneos';
import type { EstadoCatalogoTorneos } from './estado-catalogo-torneos';

type Dependencias={
 catalogo:EstadoCatalogoTorneos<TorneoResumen>;
 nuevaSolicitud:()=>SolicitudTorneos;
 obtener:(signal:AbortSignal,forzar?:boolean)=>Promise<TorneoResumen[]>;
 vigente:(version:number)=>boolean;
 abortada:(error:unknown)=>boolean;
 leerEstado:()=>void;
 renderPortada:()=>void;
 poblarSelects:()=>void;
 inicializarSelects:()=>void;
 reflejarEstado:()=>void;
 renderCatalogo:()=>void;
 escribirEstado:()=>void;
 abrirTorneo:(id:number,actualizarUrl?:boolean)=>Promise<void>;
 torneoActual:()=>TorneoDetalle|null;
 abrirCentro:(torneo:TorneoDetalle,categoria:number,vista:VistaCentro,actualizarUrl?:boolean)=>Promise<void>;
 escapeHtml:(valor:unknown)=>string;
};

export function crearArranquePortalTorneos(d:Dependencias){
 async function cargar(forzar=false){
  const contenedor=document.getElementById('listaTorneos');
  if(!contenedor)return;
  const solicitud=d.nuevaSolicitud();
  contenedor.setAttribute('aria-busy','true');
  contenedor.innerHTML=Array.from({length:6},()=>'<div class="w2t-skeleton" aria-hidden="true"></div>').join('');
  try{
   const torneos=await d.obtener(solicitud.controlador.signal,forzar);
   if(!d.vigente(solicitud.version))return;
   d.catalogo.torneos=torneos;
   contenedor.removeAttribute('aria-busy');
   d.leerEstado();
   d.renderPortada();
   d.poblarSelects();
   d.inicializarSelects();
   d.reflejarEstado();
   d.renderCatalogo();
   d.escribirEstado();
   const parametros=new URLSearchParams(location.search);
   const torneoId=Number(parametros.get('torneo'));
   if(!Number.isInteger(torneoId)||torneoId<=0)return;
   await d.abrirTorneo(torneoId,false);
   const categoriaId=Number(parametros.get('categoria'));
   const torneo=d.torneoActual();
   if(categoriaId>0&&torneo)await d.abrirCentro(torneo,categoriaId,(parametros.get('vista')||'resumen') as VistaCentro,false);
  }catch(error){
   if(!d.vigente(solicitud.version)||d.abortada(error))return;
   contenedor.removeAttribute('aria-busy');
   contenedor.innerHTML=`<div class="w2t-error"><b>No pudimos cargar los torneos</b><p>${d.escapeHtml(error instanceof Error?error.message:'Revisá la conexión e intentá nuevamente.')}</p><button id="reintentarTorneos" class="btn btn-secondary" type="button">Reintentar</button></div>`;
   document.getElementById('reintentarTorneos')?.addEventListener('click',()=>void cargar(true),{once:true});
  }
 }
 return {cargar};
}
