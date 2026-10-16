import { etiqueta, moneda } from '../formatos';

/** Vocabulario y formato compartidos por catálogo, detalle e inscripción de Torneos W2. */
export function precioCategoria(valor:number){return Number(valor)===0?'Gratis':moneda(valor)}

export function etapaTexto(valor:string){return ({PREPARACION:'Competencia en preparación',GRUPOS_EN_PREPARACION:'Grupos en preparación',GRUPOS_EN_CURSO:'Fase de grupos',GRUPOS_FINALIZADOS:'Grupos finalizados',DESEMPATE_PENDIENTE:'Desempate pendiente',ELIMINATORIAS:'Etapa eliminatoria',FINALIZADA:'Competencia finalizada'} as Record<string,string>)[valor]||etiqueta(valor)}

export function ramaPublica(valor:string){const rama=String(valor||'').toUpperCase();return rama==='LIBRE'||rama==='MASCULINA'||rama==='MASCULINO'||rama==='ABIERTO'?'Libre':rama==='FEMENINA'||rama==='FEMENINO'?'Femenino':rama==='MIXTA'||rama==='MIXTO'?'Mixto':etiqueta(valor)}

export function textoFormato(valor:string){return valor==='GRUPOS_ELIMINACION'?'Grupos + eliminación':valor?etiqueta(valor):'Sin especificar'}

export function normalizarBusquedaTorneo(valor:unknown){return String(valor??'').normalize('NFD').replace(/[\u0300-\u036f]/g,'').toLowerCase().replace(/\s+/g,' ').trim()}
