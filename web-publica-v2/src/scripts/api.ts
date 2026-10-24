const API_BASE = '/api/publica';
export class ApiError extends Error { constructor(message:string,public readonly status:number,public readonly codigo:string|null=null,public readonly operacionId:string|null=null,public readonly retryAfterMs:number|null=null){super(message);this.name='ApiError'} }

type ApiOptions=RequestInit&{cacheMs?:number;forceRefresh?:boolean};
type CacheEntry={expires:number,data:unknown};
const CACHE_KEY='w2.api.cache.v2',RATE_KEY='w2.api.rate-limit.v2';
type ApiRuntime={inflight:Map<string,Promise<unknown>>;memory:Map<string,CacheEntry>};
const runtimeKey='__w2ApiRuntimeV2';
const scope=globalThis as typeof globalThis&{[runtimeKey]?:ApiRuntime};
const runtime=scope[runtimeKey]??=( {inflight:new Map<string,Promise<unknown>>(),memory:new Map<string,CacheEntry>()} );
const inflight=runtime.inflight;
const memory=runtime.memory;
const defaultTtl=(route:string)=>route==='/complejo'?300_000:route==='/rankings/categorias'?120_000:route==='/canchas'?45_000:route==='/torneos'?60_000:route==='/auth/sesion'?0:route==='/cliente/comunidad/notificaciones'?30_000:route.startsWith('/comunidad/jugadores?')||route.startsWith('/comunidad/convocatorias?')?15_000:0;
function cacheKey(route:string){return 'GET '+route}
function loadSession(key:string){try{const all=JSON.parse(sessionStorage.getItem(CACHE_KEY)||'{}') as Record<string,CacheEntry>;const item=all[key];if(item&&item.expires>Date.now())return item;if(item){delete all[key];sessionStorage.setItem(CACHE_KEY,JSON.stringify(all))}}catch{}return null}
function saveSession(key:string,item:CacheEntry){try{const all=JSON.parse(sessionStorage.getItem(CACHE_KEY)||'{}') as Record<string,CacheEntry>;all[key]=item;const now=Date.now();for(const k of Object.keys(all))if(!all[k]||all[k].expires<=now)delete all[k];sessionStorage.setItem(CACHE_KEY,JSON.stringify(all))}catch{}}
function clearSession(){try{sessionStorage.removeItem(CACHE_KEY)}catch{}}
function retryAfter(response:Response){const raw=response.headers.get('Retry-After');if(!raw)return 5_000;const seconds=Number(raw);if(Number.isFinite(seconds))return Math.max(1_000,Math.min(120_000,seconds*1_000));const date=Date.parse(raw);return Number.isFinite(date)?Math.max(1_000,Math.min(120_000,date-Date.now())):5_000}
function rateLimits(){try{return JSON.parse(sessionStorage.getItem(RATE_KEY)||'{}') as Record<string,number>}catch{return{}}}
function rateKey(route:string){try{const url=new URL(route,location.origin);return url.pathname}catch{return route.split('?')[0]}}
function blockedUntil(route:string){const limits=rateLimits(),key=rateKey(route),until=Number(limits[key]||0)||0;if(until<=Date.now()){if(key in limits){delete limits[key];try{sessionStorage.setItem(RATE_KEY,JSON.stringify(limits))}catch{}}return 0}return until}
function setBlockedUntil(route:string,value:number){const limits=rateLimits();limits[rateKey(route)]=value;try{sessionStorage.setItem(RATE_KEY,JSON.stringify(limits))}catch{}}
export function invalidarCacheApi(){memory.clear();inflight.clear();clearSession()}

async function request<T>(ruta:string,opciones:ApiOptions):Promise<T>{
  const {cacheMs,forceRefresh,...fetchOptions}=opciones;
  const controlador=new AbortController(),externa=fetchOptions.signal;let vencio=false;
  const cancelar=()=>controlador.abort(externa?.reason);
  if(externa?.aborted)cancelar();else externa?.addEventListener('abort',cancelar,{once:true});
  const temporizador=window.setTimeout(()=>{vencio=true;controlador.abort()},15_000);
  try{
    const espera=blockedUntil(ruta)-Date.now();
    if(espera>0)throw new ApiError('Estamos regulando temporalmente las consultas. Intentá nuevamente en unos segundos.',429,'CLIENT_RATE_LIMIT',null,espera);
    const respuesta=await fetch(`${API_BASE}${ruta}`,{...fetchOptions,credentials:'include',headers:{'Content-Type':'application/json',...(fetchOptions.headers||{})},signal:controlador.signal});
    let datos:unknown;try{datos=await respuesta.json()}catch{datos={mensaje:'La respuesta del servidor no es válida.'}}
    if(!respuesta.ok){
      const mensaje=typeof datos==='object'&&datos&&'mensaje' in datos?String((datos as {mensaje:unknown}).mensaje):'No se pudo completar la operación.';
      const pausa=respuesta.status===429?retryAfter(respuesta):null;if(pausa)setBlockedUntil(ruta,Date.now()+pausa);
      throw new ApiError(mensaje,respuesta.status,typeof datos==='object'&&datos&&'codigo' in datos?String((datos as {codigo:unknown}).codigo):null,respuesta.headers.get('X-Operacion-Id'),pausa);
    }
    return datos as T;
  }catch(error){if(error instanceof DOMException&&error.name==='AbortError'){if(vencio)throw new Error('La consulta demoró demasiado. Intentá nuevamente.');throw error}throw error}
  finally{clearTimeout(temporizador);externa?.removeEventListener('abort',cancelar)}
}

export async function api<T>(ruta:string,opciones:ApiOptions={}):Promise<T>{
  const method=(opciones.method||'GET').toUpperCase();
  if(method!=='GET'){const result=await request<T>(ruta,opciones);invalidarCacheApi();return result}
  const ttl=opciones.cacheMs??defaultTtl(ruta),key=cacheKey(ruta),now=Date.now();
  if(!opciones.forceRefresh&&ttl>0){const local=memory.get(key)||loadSession(key);if(local&&local.expires>now){memory.set(key,local);return local.data as T}}
  const shareable=!opciones.signal;
  if(shareable&&inflight.has(key))return inflight.get(key) as Promise<T>;
  const pending=request<T>(ruta,opciones).then(data=>{if(ttl>0){const item={expires:Date.now()+ttl,data};memory.set(key,item);saveSession(key,item)}return data}).finally(()=>{if(shareable)inflight.delete(key)});
  if(shareable)inflight.set(key,pending);
  return pending;
}

export interface Complejo {
  nombreComercial: string; direccion?: string; telefono?: string; whatsapp?: string; email?: string; instagram?: string;
  moneda: string; porcentajeSenia: number; anticipacionMinimaHoras: number; minutosReservaPendiente: number;
  colorPrincipal?: string; pagoAlias?: string; pagoTitular?: string; pagoEntidad?: string; pagoInstrucciones?: string;
  pagoTransferenciaDisponible: boolean; pagoOnlineDisponible?: boolean;
}
export interface Cancha { id:number; nombre:string; descripcion?:string; tipo?:string; superficie?:string; tieneIluminacion:boolean; horaApertura:string; horaCierre:string; duracionMinutos:number; precio:number; importeSenia:number; diasDisponibles:string[]; }
export interface CategoriaTorneo { id:number; nombre:string; rama:string; cupoParejas:number; parejasConfirmadas:number; cuposDisponibles:number; precioInscripcion:number; premioCampeon?:number|null; premioSubcampeon?:number|null; premioDescripcion?:string|null; disponible:boolean; formatoCompetencia:string; cantidadGruposTres:number; cantidadGruposCuatro:number; clasificadosProyectados:number; }
export interface TorneoResumen { id:number; nombre:string; descripcion?:string; fechaInicio:string; fechaFin:string; inscripcionDesde:string; inscripcionHasta:string; estado:string; estadoPortal:'PROXIMO'|'EN_CURSO'|'FINALIZADO'; inscripcionDisponible:boolean; cantidadCategorias:number; categoriasDisponibles:number; cupoTotal:number; parejasConfirmadas:number; cuposDisponibles:number; precioMinimo:number; precioMaximo:number; categorias:string[]; ramas:string[]; formatos:string[]; etapaCompetitiva:string; partidosTotales:number; partidosFinalizados:number; partidosCancelados:number; resultadosDisponibles:boolean; campeonesDisponibles:boolean; }
export interface TorneoDetalle extends Omit<TorneoResumen,'cantidadCategorias'|'categorias'> { reglamento?:string; categorias:CategoriaTorneo[]; }
export interface ParejaCuadroPublica { inscripcionId:number; jugadores:string[]; }
export interface SetCuadroPublico { numero:number; tipo:string; puntosPareja1:number; puntosPareja2:number; }
export interface PartidoCuadroPublico { id:number; fase:string; orden:number; estado:string; bye:boolean; pareja1?:ParejaCuadroPublica|null; pareja2?:ParejaCuadroPublica|null; ganadoraInscripcionId?:number|null; resultado?:string|null; sets:SetCuadroPublico[]; fecha?:string|null; horaInicio?:string|null; horaFin?:string|null; canchaId?:number|null; cancha?:string|null; fechaFinalizacion?:string|null; partidoSiguienteId?:number|null; posicionSiguiente?:string|null; }
export interface FaseCuadroPublica { nombre:string; partidos:PartidoCuadroPublico[]; }
export interface CampeonaCuadroPublica extends ParejaCuadroPublica { resultadoFinal?:string|null; }
export interface CuadroCategoriaPublico { torneo:{id:number;nombre:string;estado:string;fechaInicio:string;fechaFin:string}; categoria:{id:number;nombre:string;rama:string}; campeona?:CampeonaCuadroPublica|null; fases:FaseCuadroPublica[]; }
export interface IntegranteGrupoPublico { inscripcionId:number; pareja:string; cabezaSerie:boolean; ordenSorteo:number; }
export interface PosicionGrupoPublica { posicion:number; inscripcionId:number; pareja:string; partidosJugados:number; partidosGanados:number; partidosPerdidos:number; setsGanados:number; setsPerdidos:number; diferenciaSets:number; gamesGanados:number; gamesPerdidos:number; diferenciaGames:number; estado:string; }
export interface PartidoGrupoPublico { id:number; tipo?:string|null; orden:number; estado:string; pareja1?:ParejaCuadroPublica|null; pareja2?:ParejaCuadroPublica|null; ganadoraInscripcionId?:number|null; resultado?:string|null; sets:SetCuadroPublico[]; fecha?:string|null; horaInicio?:string|null; horaFin?:string|null; canchaId?:number|null; cancha?:string|null; fechaFinalizacion?:string|null; }
export interface GrupoTorneoPublico { id:number; nombre:string; orden:number; capacidad:number; modoAsignacion:string; confirmado:boolean; clasifican:number; posicionesDefinitivas:boolean; desempatePendiente:boolean; integrantes:IntegranteGrupoPublico[]; posiciones:PosicionGrupoPublica[]; partidos:PartidoGrupoPublico[]; }
export interface CompetenciaCategoriaPublica { torneo:{id:number;nombre:string;estado:string;fechaInicio:string;fechaFin:string}; categoria:{id:number;nombre:string;rama:string;formatoCompetencia:string;cupoParejas:number;cantidadGruposTres:number;cantidadGruposCuatro:number;clasificadosProyectados:number}; resumen:{etapa:string;gruposConfigurados:boolean;gruposConfirmados:boolean;posicionesDefinitivas:boolean;desempatePendiente:boolean;eliminatoriasGeneradas:boolean;partidosGrupos:number;partidosGruposFinalizados:number;partidosEliminatorios:number;partidosEliminatoriosFinalizados:number}; grupos:GrupoTorneoPublico[]; eliminatorias?:CuadroCategoriaPublico|null; }
export interface SolicitudReservaCreada { solicitudId:number; codigoSeguimiento:string; estado:string; cancha:string; fecha:string; horaInicio:string; horaFin:string; precioTotal:number; importeSenia:number; vencimiento:string; minutosParaPagar:number; moneda:string; mensaje:string; }
export interface OrdenPagoPublica { ordenId?:number; reservaId?:number; proveedor?:'MANUAL'|'MERCADO_PAGO'|string; metodo?:'TRANSFERENCIA_MANUAL'|'PAGO_ONLINE'|string; estado?:string; importe?:number; moneda?:string; fechaExpiracion?:string|null; segundosRestantes?:number; puedePagarOnline?:boolean; puedeInformarTransferencia?:boolean; puedeReintentar?:boolean; requiereActualizacion?:boolean; checkoutUrl?:string|null; mensaje?:string; }
export type MetodoOrdenPago='TRANSFERENCIA_MANUAL'|'PAGO_ONLINE';
export const obtenerOrdenPago=(reservaId:number)=>api<OrdenPagoPublica>(`/cliente/reservas/${reservaId}/pago`);
export const crearOrdenPago=(reservaId:number,metodo:MetodoOrdenPago='TRANSFERENCIA_MANUAL')=>api<OrdenPagoPublica>(`/cliente/reservas/${reservaId}/pago`,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({metodo})});
