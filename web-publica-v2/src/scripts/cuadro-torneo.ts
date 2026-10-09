import { api, type CuadroCategoriaPublico, type PartidoCuadroPublico, type ParejaCuadroPublica } from './api';
import { etiqueta, fecha, rangoFechas } from './formatos';

const modal = document.getElementById('modalCuadroTorneo') as HTMLElement | null;
const contenido = document.getElementById('contenidoCuadroTorneo') as HTMLElement | null;
const titulo = document.getElementById('tituloCuadroTorneo') as HTMLElement | null;
const subtitulo = document.getElementById('subtituloCuadroTorneo') as HTMLElement | null;
let ultimoFoco: HTMLElement | null = null;
let escala = 1;
let observador: ResizeObserver | null = null;
let zoomEnCurso = false;
let zoomFrame = 0;
let entradasPendientes = new Map<string,string>();
// cuadro-por-rondas-movil-v36
// pulido-torneos-movil-v37
let indiceRondaMovil=0;
const esVistaMovil=()=>window.matchMedia('(max-width: 760px)').matches;
const movimientoReducido=()=>window.matchMedia('(prefers-reduced-motion: reduce)').matches;


const escapeHtml = (valor: unknown) => String(valor ?? '').replace(/[&<>"']/g, c => ({ '&':'&amp;', '<':'&lt;', '>':'&gt;', '"':'&quot;', "'":'&#39;' }[c] || c));
const faseColor = (fase: string) => fase.startsWith('ACCESO_') ? 'access' : fase === 'DIECISEISAVOS' ? 'round32' : fase === 'OCTAVOS' ? 'round16' : fase === 'CUARTOS' ? 'quarter' : fase === 'SEMIFINAL' ? 'semi' : 'final';
const faseTitulo = (fase: string) => fase.startsWith('ACCESO_') ? `Acceso · Ronda ${fase.slice(-1)}` : etiqueta(fase);
const integrantes = (pareja?: ParejaCuadroPublica | null) => pareja?.jugadores?.length ? pareja.jugadores.map(j => `<span>${escapeHtml(j)}</span>`).join('') : '';
const resumenPareja = (pareja?: ParejaCuadroPublica | null) => pareja?.jugadores?.length ? pareja.jugadores.map(escapeHtml).join(' / ') : 'Por definir';
const agenda = (p: PartidoCuadroPublico) => p.bye ? 'Clasificación directa por BYE' : p.fecha ? `${fecha(p.fecha)}${p.horaInicio ? ' · ' + p.horaInicio.slice(0,5) : ''}${p.cancha ? ' · ' + escapeHtml(p.cancha) : ''}` : 'Horario por definir';
const claveEntrada = (partidoId: number, lado: 1|2) => `${partidoId}-${lado}`;

function construirReferencias(cuadro: CuadroCategoriaPublico) {
  entradasPendientes = new Map();
  const partidos = cuadro.fases.flatMap(f => f.partidos);
  partidos.forEach(origen => {
    if (!origen.partidoSiguienteId) return;
    const lado: 1|2 = origen.posicionSiguiente === 'PAREJA_2' ? 2 : 1;
    entradasPendientes.set(claveEntrada(origen.partidoSiguienteId, lado), `Ganador de ${faseTitulo(origen.fase)} · Partido ${origen.orden}`);
  });
}

function equipo(p: PartidoCuadroPublico, lado: 1 | 2) {
  const valor = lado === 1 ? p.pareja1 : p.pareja2;
  const ganador = Boolean(valor && p.ganadoraInscripcionId === valor.inscripcionId);
  const referencia = entradasPendientes.get(claveEntrada(p.id,lado));
  const contenidoEquipo = valor ? integrantes(valor) : `<span class="bracket-source">${escapeHtml(referencia || 'Entrada directa por definir')}</span>`;
  return `<div class="bracket-team ${ganador ? 'winner' : ''} ${valor ? '' : 'pending-slot'} ${referencia ? 'linked-slot' : 'direct-slot'}" data-team-position="${lado}"><b>${ganador ? '★' : ''}<span>${contenidoEquipo}</span></b>${ganador ? '<em>Ganó</em>' : ''}${referencia ? '<i class="bracket-team-anchor" aria-hidden="true"></i>' : ''}</div>`;
}

function piePartido(p: PartidoCuadroPublico) {
  if (p.bye) return '<footer class="bracket-card-status bye"><strong>Clasificación directa</strong></footer>';
  if (p.estado === 'FINALIZADO') return `<footer><strong>${escapeHtml(p.resultado || 'Resultado registrado')}</strong><small>${agenda(p)}</small></footer>`;
  if (p.estado === 'PROGRAMADO') return `<footer class="bracket-card-status scheduled"><strong>${agenda(p)}</strong></footer>`;
  if (p.estado === 'EN_CURSO') return `<footer class="bracket-card-status live"><strong>${escapeHtml(p.resultado || 'Partido en curso')}</strong>${p.fecha ? `<small>${agenda(p)}</small>` : ''}</footer>`;
  const tieneParejas = Boolean(p.pareja1 || p.pareja2);
  return tieneParejas ? '<footer class="bracket-card-status pending"><strong>Pendiente de programación</strong></footer>' : '';
}

function tarjeta(p: PartidoCuadroPublico) {
  return `<article class="bracket-card state-${p.estado.toLowerCase()}" id="bracket-match-${p.id}" data-match-id="${p.id}" data-next-id="${p.partidoSiguienteId || ''}" data-next-position="${p.posicionSiguiente || ''}"><header><span>${escapeHtml(faseTitulo(p.fase))} · Partido ${p.orden}</span><i>${escapeHtml(etiqueta(p.estado))}</i></header><div class="bracket-teams">${equipo(p,1)}${equipo(p,2)}</div>${piePartido(p)}<i class="bracket-anchor-out" aria-hidden="true"></i></article>`;
}

function columnas(cuadro: CuadroCategoriaPublico) {
  return cuadro.fases.map(f => `<section class="bracket-round ${faseColor(f.nombre)}" data-round="${escapeHtml(f.nombre)}"><header><h3>${escapeHtml(faseTitulo(f.nombre))}</h3><span>${f.partidos.length} ${f.partidos.length === 1 ? 'partido' : 'partidos'}</span></header><div class="bracket-round-body">${f.partidos.map(tarjeta).join('')}</div></section>`).join('');
}

function campeones(cuadro: CuadroCategoriaPublico) {
  if (!cuadro.campeona) return '<section class="bracket-progress"><div><small>Competencia en curso</small><h3>La pareja campeona se definirá al finalizar el cuadro.</h3></div></section>';
  return `<section class="bracket-champion"><div><small>★ Campeones</small><h3>${escapeHtml(resumenPareja(cuadro.campeona))}</h3><p>Final ${escapeHtml(cuadro.campeona.resultadoFinal || 'sin resultado')}</p></div><button id="enfocarFinal" type="button">Ver final</button></section>`;
}

function controles() {
  return `<div class="bracket-view-controls">
    <button id="alternarControlesLlave" class="bracket-controls-toggle" type="button" aria-expanded="false" aria-controls="panelControlesLlave" aria-label="Abrir controles de vista" title="Controles de vista">
      <span aria-hidden="true">⌗</span>
    </button>
    <div id="panelControlesLlave" class="bracket-controls-popover hidden" role="group" aria-label="Controles de vista de la llave">
      <div class="bracket-controls-head"><strong>Vista de la llave</strong><button id="cerrarControlesLlave" type="button" aria-label="Cerrar controles">×</button></div>
      <button id="ajustarLlave" type="button">Ajustar</button>
      <div class="bracket-zoom"><button id="alejarLlave" type="button" aria-label="Alejar">−</button><b id="escalaLlave">100%</b><button id="acercarLlave" type="button" aria-label="Acercar">+</button></div>
      <button id="restaurarLlave" type="button">Restablecer</button>
      <small>Arrastrá el fondo para mover la llave.</small>
    </div>
  </div>`;
}

function render(cuadro: CuadroCategoriaPublico) {
  escala = 1;
  if (titulo) titulo.textContent = `${cuadro.categoria.nombre} · ${etiqueta(cuadro.categoria.rama)}`;
  if (subtitulo) subtitulo.innerHTML = `<span class="bracket-tournament-name">${escapeHtml(cuadro.torneo.nombre)}</span><span class="bracket-tournament-dates">${escapeHtml(rangoFechas(cuadro.torneo.fechaInicio,cuadro.torneo.fechaFin))}</span>`;
  if (!contenido) return;
  const fases = cuadro.fases.filter(f => f.nombre !== 'GRUPOS');
  const limpio = { ...cuadro, fases };
  construirReferencias(limpio);
  if (!fases.length) {
    contenido.innerHTML = `${campeones(limpio)}<div class="bracket-empty">El cuadro eliminatorio todavía no fue generado.</div>`;
    return;
  }
  contenido.innerHTML = `${campeones(limpio)}<div class="bracket-mobile-toolbar"><button id="alternarVistaCuadroMovil" type="button">Ver llave completa</button></div><div class="bracket-mobile" role="tablist" aria-label="Rondas del cuadro">${fases.map((f,i) => `<button type="button" role="tab" aria-selected="${i===0}" class="${i===0?'active':''}" data-mobile-round="${escapeHtml(f.nombre)}">${escapeHtml(faseTitulo(f.nombre))}</button>`).join('')}</div><div class="bracket-canvas-wrap">${controles()}<div class="bracket-viewport"><div class="bracket-size-shell"><div class="bracket-stage"><svg class="bracket-links" aria-hidden="true"></svg><div class="bracket-columns">${columnas(limpio)}</div></div></div></div></div><nav class="bracket-round-navigation" aria-label="Navegacion entre rondas"><button id="rondaAnterior" type="button">‹ Anterior</button><span id="estadoRondaMovil" aria-live="polite"></span><button id="rondaSiguiente" type="button">Siguiente ›</button></nav>`;
  configurarInteracciones();
  requestAnimationFrame(() => { distribuirEstructuralmente(); dibujarConexiones(); ajustar(true); });
}

function activarRondaMovil(indice:number,enfocar=false){
  if(!contenido)return;
  const botones=[...contenido.querySelectorAll<HTMLButtonElement>('[data-mobile-round]')];
  if(!botones.length)return;
  indiceRondaMovil=Math.max(0,Math.min(botones.length-1,indice));
  botones.forEach((boton,i)=>{const activo=i===indiceRondaMovil;boton.classList.toggle('active',activo);boton.setAttribute('aria-selected',String(activo))});
  const nombre=botones[indiceRondaMovil].dataset.mobileRound;
  contenido.querySelectorAll<HTMLElement>('.bracket-round').forEach(r=>r.classList.toggle('mobile-hidden',r.dataset.round!==nombre));
  contenido.querySelector<HTMLButtonElement>('#rondaAnterior')!.disabled=indiceRondaMovil===0;
  contenido.querySelector<HTMLButtonElement>('#rondaSiguiente')!.disabled=indiceRondaMovil===botones.length-1;
  const estado=contenido.querySelector<HTMLElement>('#estadoRondaMovil');if(estado)estado.textContent=botones[indiceRondaMovil].textContent||'';
  botones[indiceRondaMovil].scrollIntoView({behavior:movimientoReducido()?'auto':'smooth',block:'nearest',inline:'center'});
  if(enfocar)botones[indiceRondaMovil].focus();
}
function mostrarAyudaArrastre(){
  if(!contenido)return;
  contenido.querySelector('.bracket-drag-hint')?.remove();
  const ayuda=document.createElement('div');ayuda.className='bracket-drag-hint';ayuda.textContent='Arrastra para recorrer la llave';
  contenido.querySelector('.bracket-canvas-wrap')?.appendChild(ayuda);
  requestAnimationFrame(()=>ayuda.classList.add('visible'));
  window.setTimeout(()=>{ayuda.classList.remove('visible');window.setTimeout(()=>ayuda.remove(),220)},2400);
}
function alternarVistaMovil(){
  if(!contenido)return;
  const completa=contenido.classList.toggle('mobile-full-bracket');
  contenido.classList.toggle('mobile-round-bracket',!completa);
  const boton=contenido.querySelector<HTMLButtonElement>('#alternarVistaCuadroMovil');if(boton)boton.textContent=completa?'Volver a vista por rondas':'Ver llave completa';
  if(completa){requestAnimationFrame(()=>{distribuirEstructuralmente();dibujarConexiones();ajustar(true);requestAnimationFrame(()=>{centrarVista();mostrarAyudaArrastre()})})}else{aplicarEscala(1,false);activarRondaMovil(indiceRondaMovil)}
}

function configurarInteracciones() {
  contenido?.querySelector('#acercarLlave')?.addEventListener('click', () => cambiarEscala(.1));
  contenido?.querySelector('#alejarLlave')?.addEventListener('click', () => cambiarEscala(-.1));
  contenido?.querySelector('#restaurarLlave')?.addEventListener('click', () => restablecerVista());
  contenido?.querySelector('#ajustarLlave')?.addEventListener('click', ajustar);
  const toggle=contenido?.querySelector<HTMLButtonElement>('#alternarControlesLlave');
  const panel=contenido?.querySelector<HTMLElement>('#panelControlesLlave');
  const cerrarPanel=()=>{if(!toggle||!panel)return;panel.classList.add('hidden');toggle.setAttribute('aria-expanded','false');toggle.setAttribute('aria-label','Abrir controles de vista');};
  const alternarPanel=()=>{if(!toggle||!panel)return;const abrir=panel.classList.contains('hidden');panel.classList.toggle('hidden',!abrir);toggle.setAttribute('aria-expanded',String(abrir));toggle.setAttribute('aria-label',abrir?'Cerrar controles de vista':'Abrir controles de vista');};
  toggle?.addEventListener('click',event=>{event.stopPropagation();alternarPanel();});
  contenido?.querySelector('#cerrarControlesLlave')?.addEventListener('click',event=>{event.stopPropagation();cerrarPanel();toggle?.focus();});
  panel?.addEventListener('click',event=>event.stopPropagation());
  contenido?.addEventListener('click',cerrarPanel);
  configurarArrastre();
  contenido?.querySelector('#enfocarFinal')?.addEventListener('click', () => contenido?.querySelector('[data-round="FINAL"]')?.scrollIntoView({ behavior:'smooth', inline:'center', block:'nearest' }));
  const botonesRonda=[...contenido?.querySelectorAll<HTMLButtonElement>('[data-mobile-round]')||[]];
  botonesRonda.forEach((b,i)=>b.addEventListener('click',()=>activarRondaMovil(i)));
  contenido?.querySelector('#rondaAnterior')?.addEventListener('click',()=>activarRondaMovil(indiceRondaMovil-1,true));
  contenido?.querySelector('#rondaSiguiente')?.addEventListener('click',()=>activarRondaMovil(indiceRondaMovil+1,true));
  contenido?.querySelector('#alternarVistaCuadroMovil')?.addEventListener('click',alternarVistaMovil);
  const canvas=contenido?.querySelector<HTMLElement>('.bracket-canvas-wrap');let inicioToque=0;
  canvas?.addEventListener('touchstart',e=>{if(contenido?.classList.contains('mobile-round-bracket'))inicioToque=e.touches[0]?.clientX||0},{passive:true});
  canvas?.addEventListener('touchend',e=>{if(!contenido?.classList.contains('mobile-round-bracket')||!inicioToque)return;const delta=(e.changedTouches[0]?.clientX||0)-inicioToque;if(Math.abs(delta)>65)activarRondaMovil(indiceRondaMovil+(delta<0?1:-1),true);inicioToque=0},{passive:true});
  if(esVistaMovil()){contenido?.classList.add('mobile-round-bracket');contenido?.classList.remove('mobile-full-bracket');activarRondaMovil(0)}else{contenido?.classList.remove('mobile-round-bracket','mobile-full-bracket')}
  observador?.disconnect();
  const viewport = contenido?.querySelector('.bracket-viewport');
  if (viewport) {
    const heads = contenido?.querySelector<HTMLElement>('.bracket-headings-viewport');
    viewport.addEventListener('scroll', () => { if(heads) heads.scrollLeft = viewport.scrollLeft; }, { passive:true });
    observador = new ResizeObserver(() => { if(!zoomEnCurso) requestAnimationFrame(dibujarConexiones); }); observador.observe(viewport);
  }
}

function actualizarShell() {
  const stage = contenido?.querySelector<HTMLElement>('.bracket-stage');
  const shell = contenido?.querySelector<HTMLElement>('.bracket-size-shell');
  if (!stage || !shell) return;
  shell.style.width = `${Math.ceil(stage.scrollWidth * escala)}px`;
  shell.style.height = `${Math.ceil(stage.scrollHeight * escala)}px`;
}
function configurarArrastre() {
  const viewport=contenido?.querySelector<HTMLElement>('.bracket-viewport');
  if(!viewport||!contenido)return;
  let activo=false,inicioX=0,inicioY=0,scrollX=0,scrollY=0,movido=false;
  const esControl=(target:EventTarget|null)=>target instanceof Element && Boolean(target.closest('button,a,input,select,textarea,.bracket-view-controls'));
  viewport.addEventListener('dragstart',event=>event.preventDefault());
  viewport.addEventListener('pointerdown',event=>{
    if(event.button!==0||esControl(event.target))return;
    activo=true;movido=false;inicioX=event.clientX;inicioY=event.clientY;scrollX=viewport.scrollLeft;scrollY=contenido.scrollTop;
    viewport.setPointerCapture(event.pointerId);viewport.classList.add('is-dragging');
    document.body.classList.add('bracket-drag-active');
    event.preventDefault();
  });
  viewport.addEventListener('pointermove',event=>{
    if(!activo)return;
    const dx=event.clientX-inicioX,dy=event.clientY-inicioY;
    if(Math.abs(dx)>2||Math.abs(dy)>2)movido=true;
    viewport.scrollLeft=scrollX-dx;
    contenido.scrollTop=scrollY-dy;
    event.preventDefault();
  });
  const terminar=(event:PointerEvent)=>{
    if(!activo)return;
    activo=false;viewport.classList.remove('is-dragging');document.body.classList.remove('bracket-drag-active');
    if(viewport.hasPointerCapture(event.pointerId))viewport.releasePointerCapture(event.pointerId);
  };
  viewport.addEventListener('pointerup',terminar);
  viewport.addEventListener('pointercancel',terminar);
  viewport.addEventListener('lostpointercapture',()=>{activo=false;viewport.classList.remove('is-dragging');document.body.classList.remove('bracket-drag-active');});
  viewport.addEventListener('click',event=>{if(movido){event.preventDefault();event.stopPropagation();movido=false;}},true);
}

function centroVisible() {
  const viewport=contenido?.querySelector<HTMLElement>('.bracket-viewport');
  if(!viewport)return {x:0,y:0};
  return {x:viewport.scrollLeft+viewport.clientWidth/2,y:viewport.scrollTop+viewport.clientHeight/2};
}
function centrarVista() {
  const viewport=contenido?.querySelector<HTMLElement>('.bracket-viewport');
  const shell=contenido?.querySelector<HTMLElement>('.bracket-size-shell');
  if(!viewport||!shell)return;
  viewport.scrollLeft=Math.max(0,(shell.offsetWidth-viewport.clientWidth)/2);
  viewport.scrollTop=Math.max(0,(shell.offsetHeight-viewport.clientHeight)/2);
}
function aplicarEscala(valor: number, conservarCentro=true) {
  const viewport=contenido?.querySelector<HTMLElement>('.bracket-viewport');
  const stage=contenido?.querySelector<HTMLElement>('.bracket-stage');
  const label=contenido?.querySelector('#escalaLlave');
  if(!viewport||!stage)return;
  const anterior=escala;
  const centroX=(viewport.scrollLeft+viewport.clientWidth/2)/Math.max(anterior,.01);
  const centroY=(viewport.scrollTop+viewport.clientHeight/2)/Math.max(anterior,.01);
  const nueva=Math.max(.82,Math.min(1.35,valor));
  if(Math.abs(nueva-anterior)<.001){if(label)label.textContent=`${Math.round(nueva*100)}%`;return;}
  zoomEnCurso=true;
  if(zoomFrame)cancelAnimationFrame(zoomFrame);
  viewport.classList.add('zoom-updating');
  viewport.style.scrollBehavior='auto';
  escala=nueva;
  stage.style.transform=`scale(${escala})`;
  if(label)label.textContent=`${Math.round(escala*100)}%`;
  actualizarShell();
  zoomFrame=requestAnimationFrame(()=>{
    if(conservarCentro){
      viewport.scrollLeft=Math.max(0,centroX*escala-viewport.clientWidth/2);
      viewport.scrollTop=Math.max(0,centroY*escala-viewport.clientHeight/2);
    }
    zoomFrame=requestAnimationFrame(()=>{
      dibujarConexiones();
      viewport.classList.remove('zoom-updating');
      viewport.style.removeProperty('scroll-behavior');
      zoomEnCurso=false;
      zoomFrame=0;
    });
  });
}
function cambiarEscala(delta: number) { aplicarEscala(Math.round((escala + delta) * 10) / 10,true); }
function ajustar(recentrar=false) {
  const viewport = contenido?.querySelector<HTMLElement>('.bracket-viewport');
  const columns = contenido?.querySelector<HTMLElement>('.bracket-columns');
  if (!viewport || !columns) return;
  const disponible = Math.max(300, viewport.clientWidth - 28);
  const altoDisponible=Math.max(440,window.innerHeight-285);
  const porAncho=disponible/columns.scrollWidth;
  const porAlto=altoDisponible/columns.scrollHeight;
  const maxPartidos=Math.max(...[...contenido?.querySelectorAll<HTMLElement>('.bracket-round')||[]].map(r=>r.querySelectorAll('.bracket-card').length),1);
  const minimo=maxPartidos>=8?.82:.86;
  aplicarEscala(Math.min(1,Math.max(minimo,Math.min(porAncho,porAlto))),false);
  if(recentrar)requestAnimationFrame(centrarVista);
}
function restablecerVista(){
  ajustar(true);
  const panel=contenido?.querySelector<HTMLElement>('#panelControlesLlave');
  const toggle=contenido?.querySelector<HTMLButtonElement>('#alternarControlesLlave');
  panel?.classList.add('hidden');toggle?.setAttribute('aria-expanded','false');
}

function top(card: HTMLElement) { return Number(card.style.top.replace('px','')) || 0; }
function center(card: HTMLElement) { return top(card) + card.offsetHeight / 2; }
function setCenter(card: HTMLElement, value: number) { card.style.top = `${Math.max(0, value - card.offsetHeight / 2)}px`; }
function resolverColisiones(cards: HTMLElement[], gap: number) {
  if (!cards.length) return;
  cards.sort((a,b) => top(a) - top(b));
  for (let i=1;i<cards.length;i++) {
    const minimo = top(cards[i-1]) + cards[i-1].offsetHeight + gap;
    if (top(cards[i]) < minimo) cards[i].style.top = `${minimo}px`;
  }
  for (let i=cards.length-2;i>=0;i--) {
    const maximo = top(cards[i+1]) - gap - cards[i].offsetHeight;
    if (top(cards[i]) > maximo) cards[i].style.top = `${Math.max(0,maximo)}px`;
  }
  for (let i=1;i<cards.length;i++) {
    const minimo = top(cards[i-1]) + cards[i-1].offsetHeight + gap;
    if (top(cards[i]) < minimo) cards[i].style.top = `${minimo}px`;
  }
}

function distribuirEstructuralmente() {
  const rounds = [...(contenido?.querySelectorAll<HTMLElement>('.bracket-round') || [])];
  if (!rounds.length) return;
  const roundCards = rounds.map(r => [...r.querySelectorAll<HTMLElement>('.bracket-card')]);
  const all = roundCards.flat();
  const mayorRonda=Math.max(...roundCards.map(cards=>cards.length),1);
  const gap=mayorRonda>=8?8:12;
  const baseIndex = roundCards.reduce((best,cards,i,arr) => cards.length > arr[best].length ? i : best, 0);
  const base = roundCards[baseIndex];
  let cursor = mayorRonda>=8?8:10;
  base.forEach(card => { card.style.top = `${cursor}px`; cursor += card.offsetHeight + gap; });

  for (let i=baseIndex+1;i<roundCards.length;i++) {
    const cards = roundCards[i];
    cards.forEach((card,index) => {
      const origins = all.filter(source => source.dataset.nextId === card.dataset.matchId);
      const ideal = origins.length ? origins.reduce((sum,source) => sum + center(source),0)/origins.length : (index+1) * (cursor/(cards.length+1));
      setCenter(card,ideal);
    });
    resolverColisiones(cards,gap);
  }

  for (let i=baseIndex-1;i>=0;i--) {
    const cards = roundCards[i];
    const groups = new Map<string,HTMLElement[]>();
    cards.forEach(card => { const key=card.dataset.nextId || `free-${card.dataset.matchId}`; const list=groups.get(key)||[]; list.push(card); groups.set(key,list); });
    groups.forEach((sources,key) => {
      const target = key.startsWith('free-') ? null : all.find(card => card.dataset.matchId === key);
      const targetCenter = target ? center(target) : cursor/2;
      const spread = Math.max(0,(sources.length-1)*(sources[0].offsetHeight+gap));
      sources.sort((a,b)=>(Number(a.dataset.matchId)||0)-(Number(b.dataset.matchId)||0)).forEach((source,index) => setCenter(source,targetCenter-spread/2+index*(source.offsetHeight+gap)));
    });
    resolverColisiones(cards,gap);
  }

  let minTop = Math.min(...all.map(top));
  const margenSuperior=mayorRonda>=8?8:10;
  if (minTop < margenSuperior) all.forEach(card => card.style.top = `${top(card) + margenSuperior - minTop}px`);
  let maxBottom = Math.max(...all.map(card => top(card)+card.offsetHeight));
  const bodyHeight = maxBottom + (mayorRonda>=8?8:10);
  rounds.forEach(round => { const body=round.querySelector<HTMLElement>('.bracket-round-body'); if(body)body.style.height=`${bodyHeight}px`; });
  const columns = contenido?.querySelector<HTMLElement>('.bracket-columns');
  const stage = contenido?.querySelector<HTMLElement>('.bracket-stage');
  if (columns && stage) { stage.style.width=`${columns.scrollWidth}px`; stage.style.height=`${columns.scrollHeight}px`; }
  actualizarShell();
}

function punto(elemento: HTMLElement, stage: HTMLElement, salida: boolean) {
  let x=elemento.offsetLeft,y=elemento.offsetTop,parent=elemento.offsetParent as HTMLElement|null;
  while(parent&&parent!==stage){x+=parent.offsetLeft;y+=parent.offsetTop;parent=parent.offsetParent as HTMLElement|null;}
  return {x:x+(salida?elemento.offsetWidth:0),y:y+elemento.offsetHeight/2};
}
function dibujarConexiones() {
  const stage=contenido?.querySelector<HTMLElement>('.bracket-stage'),svg=contenido?.querySelector<SVGSVGElement>('.bracket-links');
  if(!stage||!svg)return;
  const width=stage.scrollWidth,height=stage.scrollHeight;
  svg.setAttribute('viewBox',`0 0 ${width} ${height}`);svg.setAttribute('width',String(width));svg.setAttribute('height',String(height));svg.querySelectorAll('.bracket-path').forEach(x=>x.remove());
  contenido?.querySelectorAll<HTMLElement>('.bracket-card[data-next-id]:not([data-next-id=""])').forEach(origen=>{
    const destino=contenido.querySelector<HTMLElement>(`#bracket-match-${origen.dataset.nextId}`);if(!destino)return;
    const fila=destino.querySelector<HTMLElement>(origen.dataset.nextPosition==='PAREJA_2'?'[data-team-position="2"]':'[data-team-position="1"]');if(!fila)return;
    const a=punto(origen,stage,true),b=punto(fila,stage,false),mid=a.x+Math.max(28,(b.x-a.x)*.5),definido=Boolean(origen.querySelector('.bracket-team.winner'));
    const path=document.createElementNS('http://www.w3.org/2000/svg','path');path.setAttribute('class',`bracket-path ${definido?'resolved':'pending'}`);path.setAttribute('d',`M ${a.x} ${a.y} H ${mid} V ${b.y} H ${b.x}`);path.setAttribute('marker-end','url(#bracketArrow)');svg.appendChild(path);
  });
}

export async function abrirCuadroTorneo(categoriaId:number,disparador?:HTMLElement){if(!modal||!contenido)return;ultimoFoco=disparador||null;contenido.innerHTML='<div class="bracket-loading">Cargando cuadro...</div>';modal.classList.remove('hidden');modal.classList.add('flex');document.body.style.overflow='hidden';try{render(await api<CuadroCategoriaPublico>(`/torneos/categorias/${categoriaId}/cuadro`));}catch(error){contenido.innerHTML=`<div class="bracket-error">${escapeHtml(error instanceof Error?error.message:'No se pudo cargar el cuadro.')}</div>`;}}
function cerrar(){if(!modal)return;observador?.disconnect();modal.classList.add('hidden');modal.classList.remove('flex');document.body.style.overflow='';ultimoFoco?.focus();}
document.getElementById('cerrarCuadroTorneo')?.addEventListener('click',cerrar);document.addEventListener('keydown',e=>{if(e.key!=='Escape'||!modal||modal.classList.contains('hidden'))return;const panel=contenido?.querySelector<HTMLElement>('#panelControlesLlave');const toggle=contenido?.querySelector<HTMLButtonElement>('#alternarControlesLlave');if(panel&&!panel.classList.contains('hidden')){panel.classList.add('hidden');toggle?.setAttribute('aria-expanded','false');toggle?.focus();return;}cerrar();});
