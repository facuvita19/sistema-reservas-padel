import type { TorneoResumen } from '../api';

export type EstadoPortalTorneoPublico='PROXIMO'|'EN_CURSO'|'FINALIZADO';

const listaSegura=(valor:unknown):string[]=>Array.isArray(valor)?valor.filter((item):item is string=>typeof item==='string'&&item.trim().length>0):[];
const numeroSeguro=(valor:unknown)=>Number.isFinite(Number(valor))?Number(valor):0;

/** Frontera tolerante entre la API pública y el catálogo visual de Torneos W2. */
export function adaptarResumenTorneo(raw:any):TorneoResumen{
 const estado=String(raw?.estado??'PUBLICADO');
 const hoy=new Date();hoy.setHours(0,0,0,0);
 const inicio=new Date(String(raw?.fechaInicio||'')+'T00:00:00'),fin=new Date(String(raw?.fechaFin||'')+'T23:59:59');
 const estadoDeclarado:EstadoPortalTorneoPublico=raw?.estadoPortal==='EN_CURSO'||estado==='EN_CURSO'?'EN_CURSO':raw?.estadoPortal==='FINALIZADO'||estado==='FINALIZADO'?'FINALIZADO':'PROXIMO';
 const estadoPortal:EstadoPortalTorneoPublico=Number.isNaN(fin.getTime())?estadoDeclarado:fin<hoy?'FINALIZADO':!Number.isNaN(inicio.getTime())&&inicio<=hoy?'EN_CURSO':estadoDeclarado;
 const categorias=listaSegura(raw?.categorias),ramas=listaSegura(raw?.ramas),formatos=listaSegura(raw?.formatos);
 return {...raw,descripcion:String(raw?.descripcion??''),estado,estadoPortal,
  inscripcionDisponible:Boolean(raw?.inscripcionDisponible),categorias,ramas,formatos,
  cantidadCategorias:numeroSeguro(raw?.cantidadCategorias),categoriasDisponibles:numeroSeguro(raw?.categoriasDisponibles),
  cupoTotal:numeroSeguro(raw?.cupoTotal),parejasConfirmadas:numeroSeguro(raw?.parejasConfirmadas),cuposDisponibles:numeroSeguro(raw?.cuposDisponibles),
  precioMinimo:numeroSeguro(raw?.precioMinimo),precioMaximo:numeroSeguro(raw?.precioMaximo),
  etapaCompetitiva:String(raw?.etapaCompetitiva??(estadoPortal==='EN_CURSO'?'PREPARACION':estadoPortal==='FINALIZADO'?'FINALIZADA':'PREPARACION')),
  partidosTotales:numeroSeguro(raw?.partidosTotales),partidosFinalizados:numeroSeguro(raw?.partidosFinalizados),partidosCancelados:numeroSeguro(raw?.partidosCancelados),
  resultadosDisponibles:Boolean(raw?.resultadosDisponibles),campeonesDisponibles:Boolean(raw?.campeonesDisponibles)} as TorneoResumen;
}
