export type CargadorCatalogo<T>=(signal:AbortSignal)=>Promise<T[]>;
export type CargadorDetalle<D>=(id:number,signal:AbortSignal)=>Promise<D>;

type EntradaCache<T>={dato:T;vence:number};
export type SolicitudTorneos={controlador:AbortController;version:number};

export function crearDatosTorneos<T,D>(config:{cargarCatalogo:CargadorCatalogo<T>;cargarDetalle:CargadorDetalle<D>;duracionCacheMs?:number}){
 const duracion=config.duracionCacheMs??60_000;
 let catalogo:EntradaCache<T[]>|null=null;
 const detalles=new Map<number,EntradaCache<D>>();
 let controladorCatalogo:AbortController|null=null,controladorDetalle:AbortController|null=null;
 let versionCatalogo=0,versionDetalle=0;
 function nuevaSolicitudCatalogo():SolicitudTorneos{controladorCatalogo?.abort();controladorCatalogo=new AbortController();return {controlador:controladorCatalogo,version:++versionCatalogo}}
 function nuevaSolicitudDetalle():SolicitudTorneos{controladorDetalle?.abort();controladorDetalle=new AbortController();return {controlador:controladorDetalle,version:++versionDetalle}}
 function esSolicitudCatalogoVigente(version:number){return version===versionCatalogo}
 function esSolicitudDetalleVigente(version:number){return version===versionDetalle}
 async function obtenerCatalogo(signal:AbortSignal,forzar=false){if(!forzar&&catalogo&&catalogo.vence>Date.now())return catalogo.dato;const dato=await config.cargarCatalogo(signal);catalogo={dato,vence:Date.now()+duracion};return dato}
 async function obtenerDetalle(id:number,signal:AbortSignal){const entrada=detalles.get(id);if(entrada&&entrada.vence>Date.now())return entrada.dato;const dato=await config.cargarDetalle(id,signal);detalles.set(id,{dato,vence:Date.now()+duracion});return dato}
 function invalidar(detalleId?:number){catalogo=null;if(detalleId!==undefined)detalles.delete(detalleId)}
 function esAbortada(error:unknown){return error instanceof DOMException&&error.name==='AbortError'}
 return {nuevaSolicitudCatalogo,nuevaSolicitudDetalle,esSolicitudCatalogoVigente,esSolicitudDetalleVigente,obtenerCatalogo,obtenerDetalle,invalidar,esAbortada};
}
