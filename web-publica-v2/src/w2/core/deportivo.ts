export type CategoriaDeportiva={id:number;nombre:string;color:string;desde:string};
export type RankingDeportivo={posicion:number;puntos:number;partidos:number;victorias:number;derrotas:number;efectividad:number;diferenciaSets:number;diferenciaGames:number};
export type PartidoDeportivo={partidoId:number;resultado:string;torneo:string;categoria:string;fase:string;companero:string|null;rivales:string|null;marcador:string|null;fecha:string|null};
export type MovimientoCategoria={categoria:string;estado:string;desde:string;hasta:string|null};
export type PerfilDeportivo={perfilId:number;nombre:string;iniciales:string;posicion:string|null;disponibleParaJugar:boolean;aceptaInvitaciones:boolean;categoria:CategoriaDeportiva|null;ranking:RankingDeportivo|null;partidos:number;victorias:number;derrotas:number;efectividad:number;historial:PartidoDeportivo[];categorias:MovimientoCategoria[]};
export const escaparHtml=(value:unknown)=>String(value??'').replace(/[&<>"']/g,char=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[char]||char));
export const formatearPuntos=(value:number)=>new Intl.NumberFormat('es-AR').format(Number(value||0));
export const formatearFechaDeportiva=(value:string|null)=>value?new Intl.DateTimeFormat('es-AR',{day:'2-digit',month:'short',year:'numeric'}).format(new Date(value+'T12:00:00')):'Sin fecha';
export const formatearFase=(value:string)=>String(value||'').replaceAll('_',' ').toLowerCase().replace(/(^|\s)\S/g,char=>char.toUpperCase());
export const formatearPosicion=(value:string|null)=>value==='REVES'?'REVÉS':value==='DRIVE'?'DRIVE':'FLEXIBLE';
export const etiquetaResultado=(value:string)=>value==='GANADOR'?'Victoria':value==='PERDEDOR'?'Derrota':'Otro resultado';
export const esVictoria=(value:string)=>value==='GANADOR';
export const periodoCategoria=(movimiento:MovimientoCategoria)=>movimiento.hasta?formatearFechaDeportiva(movimiento.desde)+' · '+formatearFechaDeportiva(movimiento.hasta):'Desde el '+formatearFechaDeportiva(movimiento.desde);
export const categoriaVigente=(movimiento:MovimientoCategoria,index:number)=>index===0&&movimiento.estado==='VIGENTE';
export const resumenPartidos=(perfil:Pick<PerfilDeportivo,'partidos'|'victorias'|'derrotas'>)=>perfil.partidos?perfil.victorias+' victorias · '+perfil.derrotas+' derrotas':'Sin partidos oficiales';
export const clasificarLongitudNombre=(nombre:string)=>{const length=Array.from(nombre.trim()).length;return length>28?'extra-long':length>20?'long':length>14?'medium':'short'};
export function renderMovimientoCategoria(movimiento:MovimientoCategoria,index:number,headClass=''){const vigente=categoriaVigente(movimiento,index);return '<li class="'+(vigente?'active':'')+'"><div class="'+headClass+'"><b>'+escaparHtml(movimiento.categoria)+'</b>'+(vigente?'<em>VIGENTE</em>':'')+'</div><span>'+periodoCategoria(movimiento)+'</span></li>'}
