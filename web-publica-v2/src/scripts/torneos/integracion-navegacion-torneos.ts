import type { TorneoDetalle } from '../api';

type VistaCentro='resumen'|'posiciones'|'partidos'|'cuadro';
type EventoRetornoCentro=CustomEvent<{torneo:TorneoDetalle}>;
type Dependencias={
 abrirInscripcion:(boton:HTMLButtonElement)=>Promise<void>;
 torneoActual:()=>TorneoDetalle|null;
 manejarCentroClick:(target:HTMLElement)=>boolean;
 abrirCentro:(torneo:TorneoDetalle,categoriaId:number,vista:VistaCentro,actualizarUrl?:boolean)=>Promise<void>;
 manejarDetalleClick:(target:HTMLElement)=>boolean;
 capturarRetorno:(boton:HTMLButtonElement)=>void;
 abrirTorneo:(id:number,actualizarUrl?:boolean)=>Promise<void>;
 volverCatalogo:(actualizarUrl?:boolean)=>void;
 centroActivo:()=>boolean;
 restaurarCentroDesdeUrl:()=>boolean;
 restaurarCatalogoDesdeHistorial:(restaurar:()=>void)=>void;
 restaurarEstadoCatalogo:()=>void;
 restaurarDetalleDesdeCentro:(torneo:TorneoDetalle)=>void;
 manejarCentroTeclado:(evento:KeyboardEvent)=>boolean;
 precargarCentro:(categoriaId:number)=>void;
};

export function crearIntegracionNavegacionTorneos(d:Dependencias){
 const abrirInscripcion=(boton:HTMLButtonElement)=>{
  if(!boton.dataset.resumen){
   const torneo=boton.dataset.torneoNombre||d.torneoActual()?.nombre||'Torneo';
   const categoria=boton.dataset.categoriaNombre||'Categoría';
   boton.dataset.resumen=torneo+' · '+categoria;
  }
  void d.abrirInscripcion(boton);
 };
 const manejarClickGlobal=(evento:MouseEvent)=>{
  const target=evento.target as HTMLElement;
  const inscripcion=target.closest<HTMLButtonElement>('.inscribir-pareja');
  if(inscripcion&&!inscripcion.disabled){abrirInscripcion(inscripcion);return}
  if(d.manejarCentroClick(target))return;
  const competencia=target.closest<HTMLButtonElement>('[data-open-competition]');
  const torneo=d.torneoActual();
  if(competencia&&torneo){void d.abrirCentro(torneo,Number(competencia.dataset.openCompetition),'resumen');return}
  d.manejarDetalleClick(target);
 };
 const manejarClickCatalogo=(evento:MouseEvent)=>{
  const target=evento.target as HTMLElement;
  const abrir=target.closest<HTMLButtonElement>('.catalog-open');
  if(abrir){d.capturarRetorno(abrir);void d.abrirTorneo(Number(abrir.dataset.torneoId));return}
  const retry=target.closest<HTMLButtonElement>('[data-retry-torneo]');
  if(retry)void d.abrirTorneo(Number(retry.dataset.retryTorneo),false);
 };
 const restaurarHistorial=async()=>{
  const parametros=new URLSearchParams(location.search);
  const torneoId=Number(parametros.get('torneo'));
  const categoriaId=Number(parametros.get('categoria'));
  if(categoriaId>0&&d.centroActivo()&&d.restaurarCentroDesdeUrl())return;
  if(torneoId>0){
   await d.abrirTorneo(torneoId,false);
   const torneo=d.torneoActual();
   if(categoriaId>0&&torneo)await d.abrirCentro(torneo,categoriaId,(parametros.get('vista')||'resumen') as VistaCentro,false);
   return;
  }
  d.restaurarCatalogoDesdeHistorial(()=>{d.restaurarEstadoCatalogo();d.volverCatalogo(false)});
 };
 const manejarPrecarga=(evento:PointerEvent)=>{
  const boton=(evento.target as HTMLElement).closest<HTMLElement>('[data-open-competition],[data-f4-category]');
  const categoriaId=Number(boton?.dataset.openCompetition||boton?.dataset.f4Category);
  if(categoriaId>0)d.precargarCentro(categoriaId);
 };
 document.addEventListener('click',manejarClickGlobal);
 document.getElementById('listaTorneos')?.addEventListener('click',manejarClickCatalogo);
 document.getElementById('volverCatalogoTorneos')?.addEventListener('click',()=>d.volverCatalogo());
 addEventListener('popstate',()=>void restaurarHistorial());
 addEventListener('f4-back',evento=>d.restaurarDetalleDesdeCentro((evento as EventoRetornoCentro).detail.torneo));
 document.addEventListener('keydown',evento=>{d.manejarCentroTeclado(evento)});
 document.addEventListener('pointerover',manejarPrecarga,{passive:true});
 return {restaurarHistorial};
}
