import { api, type TorneoDetalle, type TorneoResumen, type CompetenciaCategoriaPublica, type CuadroCategoriaPublico, type PartidoCuadroPublico } from './api';
import { abrirCuadroTorneo } from './cuadro-torneo';
import {etiqueta, fecha, fechaHora, moneda, rangoFechas } from './formatos';

type Perfil = { nombre?: string; apellido?: string; telefono?: string };
type SolicitudCreada = { inscripcionId: number; estado: string; mensaje: string };

const escapeHtml = (valor: unknown) => String(valor ?? '').replace(/[&<>"']/g, caracter => ({
  '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;',
}[caracter] || caracter));

const campo = (id: string) => document.getElementById(id) as HTMLInputElement | HTMLTextAreaElement;
const modal = document.getElementById('modalInscripcion') as HTMLElement;
const formulario = document.getElementById('formInscripcion') as HTMLFormElement;
const errorCaja = document.getElementById('errorInscripcion') as HTMLElement;
const exitoCaja = document.getElementById('exitoInscripcion') as HTMLElement;
const enviar = document.getElementById('enviarInscripcion') as HTMLButtonElement;
let ultimoFoco: HTMLElement | null = null;
let enviandoInscripcion = false;
let inscripcionCompletada = false;

function disponibilidad(torneo: TorneoDetalle) {
  if (torneo.inscripcionDisponible) return { texto: 'Inscripción abierta', clase: 'text-emerald-300 border-emerald-500/30 bg-emerald-500/10' };
  const ahora = new Date();
  const desde = new Date(torneo.inscripcionDesde);
  const hasta = new Date(torneo.inscripcionHasta);
  if (ahora < desde) return { texto: `Abre ${fechaHora(torneo.inscripcionDesde)}`, clase: 'text-amber-200 border-amber-500/30 bg-amber-500/10' };
  if (ahora > hasta) return { texto: 'Inscripción finalizada', clase: 'text-slate-300 border-slate-500/30 bg-slate-500/10' };
  return { texto: etiqueta(torneo.estado), clase: 'text-slate-300 border-slate-500/30 bg-slate-500/10' };
}

function precioCategoria(valor:number){return Number(valor)===0?'Gratis':moneda(valor)}
function motivoInscripcion(torneo:TorneoDetalle,categoria:TorneoDetalle['categorias'][number]){if(torneo.estado==='FINALIZADO')return 'Torneo finalizado';if(categoria.cuposDisponibles<=0)return 'Cupos completos';if(!torneo.inscripcionDisponible)return 'Inscripción cerrada';return 'No disponible'}
function formatoCategoria(valor:string){return valor==='GRUPOS_ELIMINACION'?'Grupos + eliminación':'Eliminación directa'}
function etapaTexto(valor:string){return ({PREPARACION:'Preparación',GRUPOS_EN_PREPARACION:'Grupos en preparación',GRUPOS_EN_CURSO:'Fase de grupos',GRUPOS_FINALIZADOS:'Grupos finalizados',DESEMPATE_PENDIENTE:'Desempate pendiente',ELIMINATORIAS:'Etapa eliminatoria',FINALIZADA:'Competencia finalizada'} as Record<string,string>)[valor]||etiqueta(valor)}
function estadoCompetencia(etapa:string){if(etapa==='FINALIZADA')return'complete';if(etapa==='DESEMPATE_PENDIENTE')return'warning';if(etapa==='ELIMINATORIAS'||etapa==='GRUPOS_EN_CURSO'||etapa==='GRUPOS_FINALIZADOS')return'live';return'pending'}

// responsive-torneos-v36
// pulido-torneos-movil-v37
const movimientoReducido=()=>window.matchMedia('(prefers-reduced-motion: reduce)').matches;
function centrarActivo(carrusel:HTMLElement,boton?:HTMLElement|null){
  const activo=boton||carrusel.querySelector<HTMLElement>('.active');
  if(!activo)return;
  requestAnimationFrame(()=>activo.scrollIntoView({behavior:movimientoReducido()?'auto':'smooth',block:'nearest',inline:'center'}));
}
function prepararCarrusel(carrusel:HTMLElement|null){
  if(!carrusel)return;
  carrusel.classList.add('competition-mobile-carousel');
  const actualizar=()=>{const max=carrusel.scrollWidth-carrusel.clientWidth;carrusel.classList.toggle('at-start',carrusel.scrollLeft<5);carrusel.classList.toggle('at-end',max-carrusel.scrollLeft<5)};
  if(carrusel.dataset.carouselReady!=='true'){
    carrusel.dataset.carouselReady='true';
    carrusel.addEventListener('scroll',actualizar,{passive:true});
    carrusel.addEventListener('keydown',evento=>{if(evento.key!=='ArrowRight'&&evento.key!=='ArrowLeft')return;const botones=[...carrusel.querySelectorAll<HTMLButtonElement>('button:not([disabled])')];const actual=Math.max(0,botones.indexOf(document.activeElement as HTMLButtonElement));const siguiente=evento.key==='ArrowRight'?Math.min(botones.length-1,actual+1):Math.max(0,actual-1);evento.preventDefault();botones[siguiente]?.focus();centrarActivo(carrusel,botones[siguiente])});
    new ResizeObserver(actualizar).observe(carrusel);
  }
  actualizar();centrarActivo(carrusel);
}
function tarjetaPosicionMovil(p:CompetenciaCategoriaPublica['grupos'][number]['posiciones'][number]){
  const clase=p.estado==='CLASIFICADO'?'qualified':p.estado==='DESEMPATE_PENDIENTE'?'tie':'';
  return `<article class="competition-position-card ${clase}"><header><b>${p.posicion}</b><div><strong>${escapeHtml(p.pareja)}</strong><span>PJ ${p.partidosJugados} · PG ${p.partidosGanados} · PP ${p.partidosPerdidos}</span></div><i>${escapeHtml(etiqueta(p.estado))}</i></header><details><summary>Ver estadísticas</summary><dl><div><dt>Sets</dt><dd>${p.setsGanados}-${p.setsPerdidos}</dd></div><div><dt>Diferencia de sets</dt><dd>${p.diferenciaSets}</dd></div><div><dt>Games</dt><dd>${p.gamesGanados}-${p.gamesPerdidos}</dd></div><div><dt>Diferencia de games</dt><dd>${p.diferenciaGames}</dd></div></dl></details></article>`;
}

function tarjeta(torneo: TorneoDetalle) {
 const estado=disponibilidad(torneo);
 const categorias=torneo.categorias.map(c=>{const habilitada=torneo.inscripcionDisponible&&c.disponible&&c.cuposDisponibles>0;const porcentaje=Math.min(100,c.cupoParejas?c.parejasConfirmadas/c.cupoParejas*100:0);return `<article class="tournament-category" data-category="${c.id}"><header><div><span>${escapeHtml(etiqueta(c.rama))}</span><h3>${escapeHtml(c.nombre)}</h3></div><b>${precioCategoria(c.precioInscripcion)}</b></header><p class="tournament-format">${escapeHtml(formatoCategoria(c.formatoCompetencia))}</p><div class="tournament-occupancy"><i style="width:${porcentaje}%"></i></div><div class="tournament-capacity"><span><b>${c.parejasConfirmadas}</b> confirmadas</span><span><b>${c.cuposDisponibles}</b> cupos libres</span></div><div class="tournament-category-actions"><button class="competition-open btn btn-secondary" type="button" data-categoria-id="${c.id}">Seguir competencia</button><button class="${habilitada?'inscribir-pareja btn btn-primary':'btn btn-ghost'}" ${habilitada?'':'disabled'} type="button" data-categoria-id="${c.id}" data-resumen="${escapeHtml(`${torneo.nombre} · ${c.nombre} · ${etiqueta(c.rama)} · ${precioCategoria(c.precioInscripcion)}`)}">${habilitada?'Inscribir pareja':motivoInscripcion(torneo,c)}</button></div></article>`}).join('');
 return `<article class="tournament-card"><header class="tournament-card-head"><div><span class="tournament-status ${torneo.inscripcionDisponible?'open':'closed'}">${escapeHtml(estado.texto)}</span><h2>${escapeHtml(torneo.nombre)}</h2><p>${escapeHtml(torneo.descripcion||'Competencia de pádel')}</p></div><div class="tournament-dates"><small>Calendario</small><strong>${rangoFechas(torneo.fechaInicio,torneo.fechaFin)}</strong><span>Calendario completo</span></div></header><div class="tournament-card-meta"><span><small>Estado</small><b>${escapeHtml(etiqueta(torneo.estado))}</b></span><span><small>Categorías</small><b>${torneo.categorias.length}</b></span><span><small>Inscripciones hasta</small><b>${fechaHora(torneo.inscripcionHasta)}</b></span></div><div class="tournament-categories">${categorias}</div><div class="competition-slot hidden" id="competencia-torneo-${torneo.id}"></div>${torneo.reglamento?`<details class="tournament-rules"><summary>Reglamento del torneo</summary><p>${escapeHtml(torneo.reglamento)}</p></details>`:''}</article>`;
}
function parejaCompetencia(p?:{jugadores:string[]}|null){return p?.jugadores?.length?p.jugadores.map(escapeHtml).join(' / '):'Por definir'}
function abreviaturas(){return '<p class="competition-legend"><b>PJ</b>: jugados · <b>PG</b>: ganados · <b>PP</b>: perdidos · <b>DS</b>: diferencia de sets · <b>DG</b>: diferencia de games</p>'}
function tablaPosiciones(g:CompetenciaCategoriaPublica['grupos'][number]){const filas=g.posiciones.map(p=>`<tr class="${p.estado==='CLASIFICADO'?'qualified':p.estado==='DESEMPATE_PENDIENTE'?'tie':''}"><td><b>${p.posicion}</b></td><td>${escapeHtml(p.pareja)}</td><td>${p.partidosJugados}</td><td>${p.partidosGanados}</td><td>${p.partidosPerdidos}</td><td>${p.setsGanados}-${p.setsPerdidos}</td><td>${p.diferenciaSets}</td><td>${p.gamesGanados}-${p.gamesPerdidos}</td><td>${p.diferenciaGames}</td><td><span>${escapeHtml(etiqueta(p.estado))}</span></td></tr>`).join('');const movil=g.posiciones.map(tarjetaPosicionMovil).join('');return `<div class="competition-table-desktop"><div class="competition-table-wrap"><table><thead><tr><th>#</th><th>Pareja</th><th>PJ</th><th>PG</th><th>PP</th><th>Sets</th><th>DS</th><th>Games</th><th>DG</th><th>Estado</th></tr></thead><tbody>${filas}</tbody></table></div>${abreviaturas()}</div><div class="competition-positions-mobile">${movil}</div>`}
function integrantesPareja(p?:{jugadores:string[]}|null){if(!p?.jugadores?.length)return '<span>Por definir</span>';return p.jugadores.map(j=>`<span>${escapeHtml(j)}</span>`).join('')}
function etiquetaPartidoGrupo(p:CompetenciaCategoriaPublica['grupos'][number]['partidos'][number],indice:number){const tipo=p.tipo||'';if(tipo==='DEFINICION_PRIMERO_SEGUNDO')return 'Definición 1.º y 2.º';if(tipo==='DEFINICION_TERCERO_CUARTO')return 'Definición 3.º y 4.º';if(tipo==='CRUCE_INICIAL')return `Cruce inicial · Partido ${indice+1}`;return `Partido ${indice+1}`}
function partidoGrupo(p:CompetenciaCategoriaPublica['grupos'][number]['partidos'][number],indice=0){const ganador=p.ganadoraInscripcionId;const g1=Boolean(p.pareja1&&ganador===p.pareja1.inscripcionId),g2=Boolean(p.pareja2&&ganador===p.pareja2.inscripcionId);const agenda=p.fecha?fecha(p.fecha)+(p.horaInicio?' · '+p.horaInicio.slice(0,5):'')+(p.cancha?' · '+escapeHtml(p.cancha):''):'Horario por definir';return `<article class="competition-match group-match"><span class="competition-match-title">${escapeHtml(etiquetaPartidoGrupo(p,indice))}</span><div class="competition-scoreboard"><div class="${g1?'winner':''}"><b>${g1?'★':''}</b><span>${integrantesPareja(p.pareja1)}</span></div><div class="${g2?'winner':''}"><b>${g2?'★':''}</b><span>${integrantesPareja(p.pareja2)}</span></div></div><footer class="competition-match-footer"><strong>${escapeHtml(p.resultado||etiqueta(p.estado))}</strong><small>${agenda}</small></footer></article>`}
function vistaGrupo(g:CompetenciaCategoriaPublica['grupos'][number]){return `<section class="competition-group-view"><header><div><p class="site-kicker">Grupo seleccionado</p><h4>${escapeHtml(g.nombre)}</h4><p>${g.capacidad} parejas · clasifican ${g.clasifican}</p></div><span class="competition-table-state">${g.desempatePendiente?'Desempate pendiente':g.posicionesDefinitivas?'Posiciones definitivas':'Posiciones provisorias'}</span></header>${tablaPosiciones(g)}<div class="competition-matches wide">${g.partidos.map((p,i)=>partidoGrupo(p,i)).join('')||'<p class="competition-empty">Los partidos todavía no fueron generados.</p>'}</div></section>`}
function resumenCompetencia(c:CompetenciaCategoriaPublica){return `<div class="competition-overview"><article><small>Etapa actual</small><strong>${escapeHtml(etapaTexto(c.resumen.etapa))}</strong><p>${c.resumen.desempatePendiente?'Existe un desempate administrativo pendiente.':c.resumen.posicionesDefinitivas?'Las posiciones grupales ya son definitivas.':'La competencia se actualiza con cada resultado.'}</p></article><article><small>Formato</small><strong>${escapeHtml(formatoCategoria(c.categoria.formatoCompetencia))}</strong><p>${c.grupos.length?c.grupos.length+' grupos publicados y '+c.categoria.clasificadosProyectados+' clasificados previstos.':'La categoría avanza directamente por la llave eliminatoria.'}</p></article><article><small>Progreso</small><strong>${c.resumen.partidosGruposFinalizados+c.resumen.partidosEliminatoriosFinalizados} de ${c.resumen.partidosGrupos+c.resumen.partidosEliminatorios} partidos</strong><p>Resultados finalizados sobre el total de partidos generados.</p></article></div>`}
function controlesGrupos(c:CompetenciaCategoriaPublica){return c.grupos.length?`<div class="competition-group-tabs" role="tablist">${c.grupos.map((g,i)=>`<button class="competition-group-tab ${i===0?'active':''}" type="button" data-group="${g.id}">${escapeHtml(g.nombre)}</button>`).join('')}</div><div class="competition-group-host">${vistaGrupo(c.grupos[0])}</div>`:'<div class="competition-empty">Esta categoría no utiliza una fase de grupos publicada.</div>'}
function referenciaPlaza(cuadro:CuadroCategoriaPublico,partidoId:number,lado:1|2){const posicion=lado===2?'PAREJA_2':'PAREJA_1';const origen=cuadro.fases.flatMap(f=>f.partidos).find(p=>p.partidoSiguienteId===partidoId&&p.posicionSiguiente===posicion);return origen?`Ganador de ${etiqueta(origen.fase)} · Partido ${origen.orden}`:'Entrada directa por definir'}
function integrantesEliminatoria(p:{jugadores:string[]}|null|undefined,referencia:string){return p?.jugadores?.length?p.jugadores.map(j=>`<span>${escapeHtml(j)}</span>`).join(''): `<span class="competition-source">${escapeHtml(referencia)}</span>`}
function partidoEliminatorio(p:PartidoCuadroPublico,cuadro:CuadroCategoriaPublico){const g1=Boolean(p.pareja1&&p.ganadoraInscripcionId===p.pareja1.inscripcionId),g2=Boolean(p.pareja2&&p.ganadoraInscripcionId===p.pareja2.inscripcionId);const agenda=p.fecha?fecha(p.fecha)+(p.horaInicio?' · '+p.horaInicio.slice(0,5):'')+(p.cancha?' · '+escapeHtml(p.cancha):''):'';const ref1=p.fase==='FINAL'?'Por definir':referenciaPlaza(cuadro,p.id,1),ref2=p.fase==='FINAL'?'Por definir':referenciaPlaza(cuadro,p.id,2);const titulo=p.fase==='FINAL'?'Final':escapeHtml(etiqueta(p.fase))+' · Partido '+p.orden;const pie=p.resultado||p.bye||p.fecha?`<footer class="competition-match-footer">${p.resultado?`<strong>${escapeHtml(p.resultado)}</strong>`:p.bye?'<strong>Clasificación directa</strong>':''}${p.fecha?`<small>${agenda}</small>`:''}</footer>`:'';return `<article class="competition-match elimination-match"><header class="elimination-match-header"><span>${titulo}</span><i class="competition-match-state state-${p.estado.toLowerCase()}">${escapeHtml(etiqueta(p.estado))}</i></header><div class="competition-scoreboard"><div class="${g1?'winner':''} ${p.pareja1?'':'pending-source'}"><b>${g1?'★':''}</b><span>${integrantesEliminatoria(p.pareja1,ref1)}</span></div><div class="${g2?'winner':''} ${p.pareja2?'':'pending-source'}"><b>${g2?'★':''}</b><span>${integrantesEliminatoria(p.pareja2,ref2)}</span></div></div>${pie}</article>`}
function claveFase(fase:string){return 'fase-'+fase.toLowerCase().replace(/_/g,'-')}
function todosPartidos(c:CompetenciaCategoriaPublica,cuadro?:CuadroCategoriaPublico|null){const grupales=c.grupos.flatMap(g=>g.partidos.map((p,i)=>({tipo:'grupo' as const,filtro:'grupo-'+g.id,etiqueta:g.nombre,p,indice:i})));const fases=(cuadro?.fases||[]).filter(f=>f.nombre!=='GRUPOS'&&f.partidos.length);const eliminatorios=fases.flatMap(f=>f.partidos.map(p=>({tipo:'fase' as const,filtro:claveFase(f.nombre),etiqueta:etiqueta(f.nombre),p})));const total=grupales.length+eliminatorios.length;if(!total)return '<div class="competition-empty">Todavía no hay partidos publicados para esta categoría.</div>';const filtros=[`<button class="active" type="button" data-match-group="all">Todos <span>${total}</span></button>`,...c.grupos.filter(g=>g.partidos.length).map(g=>`<button type="button" data-match-group="grupo-${g.id}">${escapeHtml(g.nombre)} <span>${g.partidos.length}</span></button>`),...fases.map(f=>`<button type="button" data-match-group="${claveFase(f.nombre)}">${escapeHtml(etiqueta(f.nombre))} <span>${f.partidos.length}</span></button>`)].join('');const tarjetas=[...grupales.map(x=>`<div class="competition-match-wrap" data-match-card="${x.filtro}"><small>${escapeHtml(x.etiqueta)}</small>${partidoGrupo(x.p,x.indice)}</div>`),...eliminatorios.map(x=>`<div class="competition-match-wrap elimination" data-match-card="${x.filtro}" data-phase="${x.p.fase}">${partidoEliminatorio(x.p,cuadro!)}</div>`)].join('');return `<div class="competition-match-filters" role="tablist">${filtros}</div><div class="competition-all-matches">${tarjetas}</div>`}
async function cargarPartidosCompletos(host:HTMLElement,c:CompetenciaCategoriaPublica){host.innerHTML='<div class="competition-loading">Cargando todos los partidos...</div>';let cuadro:CuadroCategoriaPublico|null=null;if(c.resumen.eliminatoriasGeneradas||c.resumen.partidosEliminatorios>0){try{cuadro=await api<CuadroCategoriaPublico>(`/torneos/categorias/${c.categoria.id}/cuadro`)}catch(error){console.warn('No se pudo cargar el cuadro eliminatorio para Partidos.',error)}}host.innerHTML=todosPartidos(c,cuadro);const filtros=host.querySelector<HTMLElement>('.competition-match-filters');prepararCarrusel(filtros);host.querySelectorAll<HTMLButtonElement>('[data-match-group]').forEach(b=>b.addEventListener('click',()=>{host.querySelectorAll('[data-match-group]').forEach(x=>x.classList.remove('active'));b.classList.add('active');centrarActivo(filtros!,b);const filtro=b.dataset.matchGroup||'all';host.classList.toggle('competition-specific-group',filtro.startsWith('grupo-'));host.querySelectorAll<HTMLElement>('[data-match-card]').forEach(card=>card.classList.toggle('hidden',filtro!=='all'&&card.dataset.matchCard!==filtro))}))}

function eliminatorias(c:CompetenciaCategoriaPublica){if(!c.resumen.eliminatoriasGeneradas)return '<div class="competition-empty">El cuadro eliminatorio se publicará cuando la organización confirme la estructura.</div>';const finalizada=c.resumen.etapa==='FINALIZADA';return `<div class="competition-elimination-ready"><div><p class="site-kicker">${finalizada?'Competencia finalizada':'Llave confirmada'}</p><h4>${finalizada?'Cuadro y campeones disponibles':'Etapa eliminatoria en curso'}</h4><p>${finalizada?'Consultá el recorrido completo, los resultados y la pareja campeona.':'Consultá cruces, programación y resultados actualizados.'}</p></div><button class="ver-cuadro btn btn-primary" type="button" data-categoria-id="${c.categoria.id}">Ver cuadro eliminatorio</button></div>`}
async function activarPestana(panel:HTMLElement,c:CompetenciaCategoriaPublica,nombre:string){panel.querySelectorAll('.competition-nav button').forEach(x=>x.classList.toggle('active',(x as HTMLElement).dataset.section===nombre));const host=panel.querySelector<HTMLElement>('.competition-section-host');if(!host)return;prepararCarrusel(panel.querySelector<HTMLElement>('.competition-nav'));if(nombre==='partidos'){await cargarPartidosCompletos(host,c);centrarActivo(panel.querySelector<HTMLElement>('.competition-nav')!);return}host.innerHTML=nombre==='resumen'?resumenCompetencia(c):nombre==='grupos'?controlesGrupos(c):eliminatorias(c);if(nombre==='grupos'){const tabs=host.querySelector<HTMLElement>('.competition-group-tabs');prepararCarrusel(tabs);host.querySelectorAll<HTMLButtonElement>('.competition-group-tab').forEach(b=>b.addEventListener('click',()=>{host.querySelectorAll('.competition-group-tab').forEach(x=>x.classList.remove('active'));b.classList.add('active');const g=c.grupos.find(x=>x.id===Number(b.dataset.group));const destino=host.querySelector<HTMLElement>('.competition-group-host');if(g&&destino)destino.innerHTML=vistaGrupo(g);centrarActivo(tabs!,b)}))}}

async function abrirCompetencia(boton:HTMLButtonElement){const id=Number(boton.dataset.categoriaId),card=boton.closest<HTMLElement>('.tournament-card'),slot=card?.querySelector<HTMLElement>('.competition-slot');if(!Number.isInteger(id)||id<=0){if(slot){slot.classList.remove('hidden');slot.innerHTML='<div class="competition-error">No se pudo identificar la categoría seleccionada.</div>'}return}if(!slot)return;const misma=slot.dataset.category===String(id)&&!slot.classList.contains('hidden');document.querySelectorAll('.competition-slot').forEach(x=>{x.classList.add('hidden');(x as HTMLElement).dataset.category=''});document.querySelectorAll('.competition-open').forEach(x=>x.textContent='Seguir competencia');if(misma)return;slot.dataset.category=String(id);slot.classList.remove('hidden');boton.textContent='Ocultar competencia';slot.innerHTML='<div class="competition-loading">Cargando competencia...</div>';try{const c=await api<CompetenciaCategoriaPublica>(`/torneos/categorias/${id}/competencia`);const tono=estadoCompetencia(c.resumen.etapa);slot.innerHTML=`<section class="competition-center"><header><div><p class="site-kicker">Centro competitivo</p><h3>${escapeHtml(c.categoria.nombre)} · ${escapeHtml(etiqueta(c.categoria.rama))}</h3><p>${escapeHtml(formatoCategoria(c.categoria.formatoCompetencia))}</p></div><span class="competition-stage ${tono}">${escapeHtml(etapaTexto(c.resumen.etapa))}</span></header>${c.categoria.formatoCompetencia==='ELIMINACION_DIRECTA'?`<div class="competition-metrics"><span><small>Formato</small><b>Eliminación directa</b></span><span><small>Partidos del cuadro</small><b>${c.resumen.partidosEliminatorios}</b></span><span><small>Finalizados</small><b>${c.resumen.partidosEliminatoriosFinalizados}</b></span><span><small>Estado</small><b>${escapeHtml(etapaTexto(c.resumen.etapa))}</b></span></div>`:`<div class="competition-metrics"><span><small>Grupos</small><b>${c.grupos.length}</b></span><span><small>Partidos de grupos</small><b>${c.resumen.partidosGruposFinalizados}/${c.resumen.partidosGrupos}</b></span><span><small>Clasificados previstos</small><b>${c.categoria.clasificadosProyectados}</b></span><span><small>Partidos eliminatorios</small><b>${c.resumen.partidosEliminatoriosFinalizados}/${c.resumen.partidosEliminatorios}</b></span></div>`}<nav class="competition-nav" aria-label="Secciones de competencia"><button class="active" type="button" data-section="resumen">Resumen</button><button type="button" data-section="grupos">Grupos</button><button type="button" data-section="partidos">Partidos</button><button type="button" data-section="eliminatorias">Eliminatorias</button></nav><div class="competition-section-host"></div></section>`;const panel=slot.querySelector<HTMLElement>('.competition-center');if(panel){panel.querySelectorAll<HTMLButtonElement>('.competition-nav button').forEach(b=>b.addEventListener('click',()=>activarPestana(panel,c,b.dataset.section||'resumen')));activarPestana(panel,c,'resumen')}slot.scrollIntoView({behavior:'smooth',block:'start'})}catch(error){slot.innerHTML=`<div class="competition-error">${escapeHtml(error instanceof Error?error.message:'No se pudo cargar la competencia.')}</div>`}}

async function completarPerfil() {
  try {
    const perfil = await api<Perfil>('/cliente/perfil');
    campo('responsableNombre').value = perfil.nombre || '';
    campo('responsableApellido').value = perfil.apellido || '';
    campo('responsableTelefono').value = perfil.telefono || '';
  } catch { /* La inscripción también admite visitantes. */ }
}

const cuerpoModalInscripcion=()=>formulario.querySelector<HTMLElement>('.registration-modal-body')||formulario;
const contenidoFormulario=()=>document.getElementById('contenidoFormularioInscripcion') as HTMLElement;
const botonCancelar=()=>document.getElementById('cancelarInscripcion') as HTMLButtonElement;
const botonCerrarExito=()=>document.getElementById('cerrarExitoInscripcion') as HTMLButtonElement;
const idsCampos=['responsableNombre','responsableApellido','responsableTelefono','parejaNombre','parejaApellido','parejaTelefono','comentariosInscripcion'];

function limpiarErroresInscripcion(){
  errorCaja.classList.add('hidden');errorCaja.textContent='';
  idsCampos.forEach(id=>{
    const input=campo(id);input.removeAttribute('aria-invalid');input.classList.remove('field-invalid');
    const mensaje=document.getElementById('error-'+id);if(mensaje){mensaje.textContent='';mensaje.classList.add('hidden')}
  });
}
function errorCampo(id:string,mensaje:string){
  const input=campo(id);input.setAttribute('aria-invalid','true');input.classList.add('field-invalid');
  const caja=document.getElementById('error-'+id);if(caja){caja.textContent=mensaje;caja.classList.remove('hidden')}
}
function primerCampoVacio(){return ['responsableNombre','responsableApellido','responsableTelefono','parejaNombre','parejaApellido','parejaTelefono'].map(campo).find(x=>!x.value.trim())||campo('parejaNombre')}
function actualizarContadorComentarios(){document.getElementById('contadorComentarios')!.textContent=`${campo('comentariosInscripcion').value.length}/500`}
function telefonoNormalizado(valor:string){return valor.replace(/\D/g,'')}
function telefonosIguales(){const a=telefonoNormalizado(campo('responsableTelefono').value),b=telefonoNormalizado(campo('parejaTelefono').value);return Boolean(a&&b&&a===b)}
function validarCampo(id:string){
  const valor=campo(id).value.trim();let mensaje='';
  const nombres:Record<string,string>={responsableNombre:'Ingresá el nombre del primer integrante.',responsableApellido:'Ingresá el apellido del primer integrante.',responsableTelefono:'Ingresá el teléfono del primer integrante.',parejaNombre:'Ingresá el nombre del segundo integrante.',parejaApellido:'Ingresá el apellido del segundo integrante.',parejaTelefono:'Ingresá el teléfono del segundo integrante.'};
  if(id!=='comentariosInscripcion'&&!valor)mensaje=nombres[id]||'Completá este campo.';
  if(id==='parejaTelefono'&&valor&&telefonosIguales())mensaje='Este teléfono coincide con el del primer integrante.';
  if(id==='comentariosInscripcion'&&valor.length>500)mensaje='Los comentarios no pueden superar 500 caracteres.';
  const caja=document.getElementById('error-'+id);campo(id).classList.toggle('field-invalid',Boolean(mensaje));campo(id).toggleAttribute('aria-invalid',Boolean(mensaje));if(caja){caja.textContent=mensaje;caja.classList.toggle('hidden',!mensaje)}
  return !mensaje;
}
function validar(){
  limpiarErroresInscripcion();
  const requeridos=['responsableNombre','responsableApellido','responsableTelefono','parejaNombre','parejaApellido','parejaTelefono'];
  const validos=requeridos.map(validarCampo);validos.push(validarCampo('comentariosInscripcion'));
  const primero=requeridos.find((_,i)=>!validos[i]);if(primero){campo(primero).focus();return false}return validos.every(Boolean);
}
function establecerEnviando(valor:boolean){
  enviandoInscripcion=valor;enviar.disabled=valor;botonCancelar().disabled=valor;
  enviar.innerHTML=valor?'<span class="registration-spinner" aria-hidden="true"></span> Enviando inscripción...':'Enviar inscripción';
  idsCampos.forEach(id=>campo(id).disabled=valor);
  document.getElementById('cerrarInscripcion')?.toggleAttribute('disabled',valor);
}
function mostrarFormulario(){
  inscripcionCompletada=false;contenidoFormulario().classList.remove('hidden');exitoCaja.classList.add('hidden');
  botonCancelar().classList.remove('hidden');enviar.classList.remove('hidden');botonCerrarExito().classList.add('hidden');
}
function mostrarExito(creada:SolicitudCreada){
  inscripcionCompletada=true;contenidoFormulario().classList.add('hidden');errorCaja.classList.add('hidden');
  exitoCaja.innerHTML=`<div class="registration-success-icon" aria-hidden="true">✓</div><p class="site-kicker">Solicitud recibida</p><h3>Inscripción enviada</h3><div class="registration-success-data"><span><small>Número de solicitud</small><b>#${creada.inscripcionId}</b></span><span><small>Estado</small><b>Pendiente de revisión</b></span></div><p>La organización revisará los datos de la pareja antes de confirmar la inscripción.</p>`;
  exitoCaja.classList.remove('hidden');botonCancelar().classList.add('hidden');enviar.classList.add('hidden');botonCerrarExito().classList.remove('hidden');
  cuerpoModalInscripcion().scrollTop=0;requestAnimationFrame(()=>{exitoCaja.focus();botonCerrarExito().focus()});
}
async function abrirInscripcion(boton:HTMLButtonElement){
  ultimoFoco=boton;formulario.reset();limpiarErroresInscripcion();mostrarFormulario();establecerEnviando(false);
  campo('torneoCategoriaId').value=boton.dataset.categoriaId||'';
  const resumen=document.getElementById('resumenInscripcion');if(resumen)resumen.textContent=boton.dataset.resumen||'';
  actualizarContadorComentarios();modal.classList.remove('hidden');modal.classList.add('flex');document.body.style.overflow='hidden';
  formulario.scrollTop=0;await completarPerfil();formulario.scrollTop=0;requestAnimationFrame(()=>primerCampoVacio().focus());
}
function cerrarInscripcion(){
  if(enviandoInscripcion)return;
  modal.classList.add('hidden');modal.classList.remove('flex');document.body.style.overflow='';
  limpiarErroresInscripcion();ultimoFoco?.focus();
}
function elementosEnfocables(){return [...modal.querySelectorAll<HTMLElement>('button:not([disabled]):not(.hidden),input:not([disabled]),textarea:not([disabled]),[tabindex="0"]')].filter(x=>x.offsetParent!==null)}
function atraparFoco(evento:KeyboardEvent){if(evento.key!=='Tab')return;const elementos=elementosEnfocables();if(!elementos.length)return;const primero=elementos[0],ultimo=elementos[elementos.length-1];if(evento.shiftKey&&document.activeElement===primero){evento.preventDefault();ultimo.focus()}else if(!evento.shiftKey&&document.activeElement===ultimo){evento.preventDefault();primero.focus()}}

async function cargar() {
  const contenedor = document.getElementById('listaTorneos');
  if (!contenedor) return;
  contenedor.innerHTML = '<div class="surface-card p-8 text-slate-400">Cargando torneos...</div>';
  try {
    const resumenes = await api<TorneoResumen[]>('/torneos');
    if (!resumenes.length) { contenedor.innerHTML = '<div class="surface-card p-8 text-slate-400">No hay torneos publicados.</div>'; return; }
    const detalles = await Promise.all(resumenes.map(torneo => api<TorneoDetalle>(`/torneos/${torneo.id}`)));
    contenedor.innerHTML = detalles.map(tarjeta).join('');
    const resumen=document.getElementById('resumenTorneos');if(resumen){const categorias=detalles.reduce((n,t)=>n+t.categorias.length,0);const abiertas=detalles.filter(t=>t.inscripcionDisponible).length;resumen.innerHTML=`<span><small>Torneos publicados</small><b>${detalles.length}</b></span><span><small>Con inscripción abierta</small><b>${abiertas}</b></span><span><small>Categorías activas</small><b>${categorias}</b></span>`;resumen.classList.remove('hidden');}
  } catch (error) {
    contenedor.innerHTML = `<div class="surface-card p-8"><p class="text-rose-300">${escapeHtml(error instanceof Error ? error.message : 'No se pudieron cargar los torneos.')}</p><button id="reintentarTorneos" class="mt-4 rounded-lg border border-brand-300 px-4 py-2 font-bold">Reintentar</button></div>`;
    document.getElementById('reintentarTorneos')?.addEventListener('click', cargar, { once: true });
  }
}

document.getElementById('listaTorneos')?.addEventListener('click', evento => {
  const boton = (evento.target as HTMLElement).closest<HTMLButtonElement>('.inscribir-pareja');
  if (boton && !boton.disabled) { abrirInscripcion(boton); return; }
  const competencia = (evento.target as HTMLElement).closest<HTMLButtonElement>('.competition-open');
  if (competencia) { abrirCompetencia(competencia); return; }
  const cuadro = (evento.target as HTMLElement).closest<HTMLButtonElement>('.ver-cuadro');
  if (cuadro) abrirCuadroTorneo(Number(cuadro.dataset.categoriaId), cuadro);
});

document.getElementById('cerrarInscripcion')?.addEventListener('click',cerrarInscripcion);
document.getElementById('cancelarInscripcion')?.addEventListener('click',cerrarInscripcion);
document.getElementById('cerrarExitoInscripcion')?.addEventListener('click',cerrarInscripcion);
// El fondo no cierra el modal: evita cierres accidentales al seleccionar texto o soltar el mouse fuera.
document.addEventListener('keydown',evento=>{
  if(modal.classList.contains('hidden'))return;
  if(evento.key==='Escape'){evento.preventDefault();cerrarInscripcion();return}
  atraparFoco(evento);
});
idsCampos.forEach(id=>{
  campo(id).addEventListener('blur',()=>validarCampo(id));
  campo(id).addEventListener('input',()=>{if(campo(id).hasAttribute('aria-invalid'))validarCampo(id);if(id==='responsableTelefono'||id==='parejaTelefono'){if(campo('parejaTelefono').value)validarCampo('parejaTelefono')}if(id==='comentariosInscripcion')actualizarContadorComentarios()});
});
formulario.addEventListener('submit',async evento=>{
  evento.preventDefault();if(enviandoInscripcion||inscripcionCompletada)return;
  errorCaja.classList.add('hidden');exitoCaja.classList.add('hidden');
  if(!validar())return;
  try{
    establecerEnviando(true);
    const creada=await api<SolicitudCreada>('/torneos/inscripciones',{method:'POST',body:JSON.stringify({
      torneoCategoriaId:Number(campo('torneoCategoriaId').value),
      responsable:{nombre:campo('responsableNombre').value.trim(),apellido:campo('responsableApellido').value.trim(),telefono:campo('responsableTelefono').value.trim()},
      pareja:{nombre:campo('parejaNombre').value.trim(),apellido:campo('parejaApellido').value.trim(),telefono:campo('parejaTelefono').value.trim()},
      comentarios:campo('comentariosInscripcion').value.trim()||null,
    })});
    establecerEnviando(false);mostrarExito(creada);await cargar();
  }catch(error){
    establecerEnviando(false);errorCaja.textContent=error instanceof Error?error.message:'No se pudo enviar la inscripción. Revisá los datos e intentá nuevamente.';
    errorCaja.classList.remove('hidden');errorCaja.focus();errorCaja.scrollIntoView({behavior:'smooth',block:'center'});
  }
});

cargar();