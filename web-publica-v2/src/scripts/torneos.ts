import { api, type TorneoDetalle, type TorneoResumen } from './api';
import { abrirCentroCompetitivo, manejarCentroCompetitivoClick, manejarCentroCompetitivoTeclado, precargarCentroCompetitivo, centroActivo, restaurarCentroDesdeUrl, type VistaCentro } from './centro-competitivo-premium';
import { etiqueta, moneda, rangoFechas } from './formatos';

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

function precioCategoria(valor:number){return Number(valor)===0?'Gratis':moneda(valor)}
function etapaTexto(valor:string){return ({PREPARACION:'Preparación',GRUPOS_EN_PREPARACION:'Grupos en preparación',GRUPOS_EN_CURSO:'Fase de grupos',GRUPOS_FINALIZADOS:'Grupos finalizados',DESEMPATE_PENDIENTE:'Desempate pendiente',ELIMINATORIAS:'Etapa eliminatoria',FINALIZADA:'Competencia finalizada'} as Record<string,string>)[valor]||etiqueta(valor)}
const movimientoReducido=()=>window.matchMedia('(prefers-reduced-motion: reduce)').matches;
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

type EstadoPortal='PROXIMO'|'EN_CURSO'|'FINALIZADO';
const ramaPublica=(v:string)=>v==='MASCULINA'||v==='ABIERTO'?'Abierto':v==='FEMENINA'||v==='FEMENINO'?'Femenino':v==='MIXTA'||v==='MIXTO'?'Mixto':etiqueta(v);
const catalogo={torneos:[] as TorneoResumen[],estado:'PROXIMO' as EstadoPortal,busqueda:'',rama:'',formato:'',soloInscripcion:false,visibles:9};
const listaSegura=(v:unknown):string[]=>Array.isArray(v)?v.filter((x):x is string=>typeof x==='string'&&x.trim().length>0):[];
const numeroSeguro=(v:unknown)=>Number.isFinite(Number(v))?Number(v):0;
function adaptarResumen(raw:any):TorneoResumen{
  const estado=String(raw?.estado??'PUBLICADO');
  const hoy=new Date();hoy.setHours(0,0,0,0);const inicio=new Date(String(raw?.fechaInicio||'')+'T00:00:00'),fin=new Date(String(raw?.fechaFin||'')+'T23:59:59');
  const estadoDeclarado:EstadoPortal=raw?.estadoPortal==='EN_CURSO'||estado==='EN_CURSO'?'EN_CURSO':raw?.estadoPortal==='FINALIZADO'||estado==='FINALIZADO'?'FINALIZADO':'PROXIMO';
  const estadoPortal:EstadoPortal=Number.isNaN(fin.getTime())?estadoDeclarado:fin<hoy?'FINALIZADO':!Number.isNaN(inicio.getTime())&&inicio<=hoy?'EN_CURSO':estadoDeclarado;
  const categorias=listaSegura(raw?.categorias);
  const ramas=listaSegura(raw?.ramas);
  const formatos=listaSegura(raw?.formatos);
  return {...raw,descripcion:String(raw?.descripcion??''),estado,estadoPortal,
    inscripcionDisponible:Boolean(raw?.inscripcionDisponible),categorias,ramas,formatos,
    cantidadCategorias:numeroSeguro(raw?.cantidadCategorias),categoriasDisponibles:numeroSeguro(raw?.categoriasDisponibles),
    cupoTotal:numeroSeguro(raw?.cupoTotal),parejasConfirmadas:numeroSeguro(raw?.parejasConfirmadas),cuposDisponibles:numeroSeguro(raw?.cuposDisponibles),
    precioMinimo:numeroSeguro(raw?.precioMinimo),precioMaximo:numeroSeguro(raw?.precioMaximo),
    etapaCompetitiva:String(raw?.etapaCompetitiva??(estadoPortal==='EN_CURSO'?'PREPARACION':estadoPortal==='FINALIZADO'?'FINALIZADA':'PREPARACION')),
    partidosTotales:numeroSeguro(raw?.partidosTotales),partidosFinalizados:numeroSeguro(raw?.partidosFinalizados),partidosCancelados:numeroSeguro(raw?.partidosCancelados),
    resultadosDisponibles:Boolean(raw?.resultadosDisponibles),campeonesDisponibles:Boolean(raw?.campeonesDisponibles)} as TorneoResumen;
}
const normalizar=(v:unknown)=>String(v??'').normalize('NFD').replace(/[\u0300-\u036f]/g,'').toLowerCase().replace(/\s+/g,' ').trim();
const tituloEstado=(e:EstadoPortal)=>e==='PROXIMO'?'Próximos':e==='EN_CURSO'?'En curso':'Finalizados';
const textoFormato=(v:string)=>v==='GRUPOS_ELIMINACION'?'Grupos + eliminación':v?etiqueta(v):'Sin especificar';
const valorPrecio=(t:TorneoResumen)=>t.precioMinimo===t.precioMaximo?precioCategoria(t.precioMinimo):'Desde '+precioCategoria(t.precioMinimo);
function resultadosCatalogo(){const q=normalizar(catalogo.busqueda);return catalogo.torneos.filter(t=>t.estadoPortal===catalogo.estado&&(!q||normalizar([t.nombre,t.descripcion,...t.categorias,...t.ramas,...t.formatos].join(' ')).includes(q))&&(!catalogo.rama||t.ramas.includes(catalogo.rama))&&(!catalogo.formato||t.formatos.includes(catalogo.formato))&&(!catalogo.soloInscripcion||t.inscripcionDisponible));}
// portada-torneos-premium-v51-20261011
function descripcionCatalogo(v:unknown){const x=String(v??'').trim();return x.length>=12?x:'';}
function porcentajeOcupacion(t:TorneoResumen){return t.cupoTotal?Math.min(100,Math.round(t.parejasConfirmadas/t.cupoTotal*100)):0;}
function estadoVisualCatalogo(t:TorneoResumen){if(t.inscripcionDisponible)return {texto:'Inscripción abierta',clase:'open'};if(t.estadoPortal==='EN_CURSO')return {texto:etapaTexto(t.etapaCompetitiva),clase:'live'};if(t.estadoPortal==='FINALIZADO')return {texto:'Finalizado',clase:'finished'};return {texto:'Próximamente',clase:'upcoming'};}
function accionCatalogo(t:TorneoResumen){if(t.inscripcionDisponible)return 'Ver categorías';if(t.estadoPortal==='EN_CURSO')return 'Seguir competencia';if(t.estadoPortal==='FINALIZADO')return 'Ver resultados';return 'Ver torneo';}
function tarjetaCatalogo(t:TorneoResumen){
 const progreso=t.partidosTotales?Math.round(t.partidosFinalizados/t.partidosTotales*100):0,ocupacion=porcentajeOcupacion(t),estado=estadoVisualCatalogo(t),descripcion=descripcionCatalogo(t.descripcion);
 const ramas=t.ramas.slice(0,2).map(x=>`<span>${escapeHtml(ramaPublica(x))}</span>`).join(''),formatos=t.formatos.slice(0,1).map(x=>`<span>${escapeHtml(textoFormato(x))}</span>`).join('');
 const progresoHtml=t.estadoPortal==='EN_CURSO'&&t.partidosTotales?`<div class="w2t-progress"><header><small>Progreso competitivo</small><b>${progreso}%</b></header><div><span style="width:${progreso}%"></span></div><small>${t.partidosFinalizados} de ${t.partidosTotales} partidos finalizados</small></div>`:t.estadoPortal==='PROXIMO'&&t.cupoTotal?`<div class="w2t-progress"><header><small>Ocupación por parejas</small><b>${ocupacion}%</b></header><div><span style="width:${ocupacion}%"></span></div><small>${t.parejasConfirmadas} de ${t.cupoTotal} parejas confirmadas</small></div>`:'';
 return `<article class="w2t-card state-${t.estadoPortal.toLowerCase()} status-${estado.clase}"><header><span class="w2t-status"><i></i>${escapeHtml(estado.texto)}</span><time>${escapeHtml(rangoFechas(t.fechaInicio,t.fechaFin))}</time></header><div class="w2t-card-body"><div class="w2t-card-heading"><small>${t.cantidadCategorias} ${t.cantidadCategorias===1?'categoría':'categorías'}</small><h3>${escapeHtml(t.nombre)}</h3>${descripcion?`<p>${escapeHtml(descripcion)}</p>`:''}</div><div class="w2t-tags">${ramas}${formatos}</div><div class="w2t-facts"><span><small>Inscripción por pareja</small><b>${valorPrecio(t)}</b></span><span><small>Disponibilidad</small><b>${t.cuposDisponibles>0?t.cuposDisponibles+' cupos':'Sin cupos'}</b></span></div>${progresoHtml}</div><footer><button class="catalog-open" type="button" data-torneo-id="${t.id}">${accionCatalogo(t)} <span aria-hidden="true">→</span></button></footer></article>`;
}
function poblarSelect(id:string,valores:string[],formato:(x:string)=>string){const select=document.getElementById(id) as HTMLSelectElement;if(!select)return;const inicial=select.options[0]?.outerHTML||'';select.innerHTML=inicial+[...new Set(valores)].sort().map(x=>`<option value="${escapeHtml(x)}">${escapeHtml(formato(x))}</option>`).join('');}
function renderCatalogo(){const filtrados=resultadosCatalogo();const lista=document.getElementById('listaTorneos')!,cantidad=Math.min(catalogo.visibles,filtrados.length);lista.classList.remove('results-1','results-2','results-many');lista.classList.add(filtrados.length===1?'results-1':filtrados.length===2?'results-2':'results-many');lista.innerHTML=filtrados.slice(0,cantidad).map(tarjetaCatalogo).join('')||`<div class="w2t-empty"><span>Sin coincidencias</span><b>No encontramos torneos con esos criterios</b><p>Probá otra búsqueda o eliminá los filtros activos.</p><button id="vaciarFiltrosTorneos" type="button">Ver todos los torneos</button></div>`;const meta=document.getElementById('estadoCatalogoTorneos');if(meta){const titulo=catalogo.estado==='PROXIMO'?'Próximas competencias':catalogo.estado==='EN_CURSO'?'Competencias en curso':'Competencias finalizadas';meta.innerHTML=`<strong>${titulo}</strong><span>${filtrados.length} ${filtrados.length===1?'torneo disponible':'torneos disponibles'}${cantidad<filtrados.length?' · mostrando '+cantidad:''}</span>`;}document.getElementById('mostrarMasTorneos')?.classList.toggle('hidden',cantidad>=filtrados.length);document.getElementById('vaciarFiltrosTorneos')?.addEventListener('click',limpiarFiltros);renderPestanas();actualizarFiltrosActivos();}
function renderPestanas(){const host=document.getElementById('pestanasTorneos')!;host.innerHTML=(['PROXIMO','EN_CURSO','FINALIZADO'] as EstadoPortal[]).map(e=>`<button class="${catalogo.estado===e?'active':''}" type="button" data-catalog-state="${e}"><span>${tituloEstado(e)}</span><b>${catalogo.torneos.filter(t=>t.estadoPortal===e).length}</b></button>`).join('');}
function actualizarFiltrosActivos(){const activos:Array<[string,string]>=[];if(catalogo.soloInscripcion)activos.push(['inscripcion','Inscripción abierta']);if(catalogo.rama)activos.push(['rama',ramaPublica(catalogo.rama)]);if(catalogo.formato)activos.push(['formato',textoFormato(catalogo.formato)]);const n=activos.length;const c=document.getElementById('cantidadFiltrosTorneos')!;c.textContent=String(n);c.classList.toggle('hidden',!n);const host=document.getElementById('chipsFiltrosTorneos');if(host)host.innerHTML=activos.map(([tipo,texto])=>`<button type="button" data-remove-filter="${tipo}">${escapeHtml(texto)} <span aria-hidden="true">×</span></button>`).join('');const limpiar=document.getElementById('limpiarFiltrosTorneos');const hayCriterios=Boolean(n||catalogo.busqueda);limpiar?.classList.toggle('hidden',!hayCriterios);document.getElementById('limpiarFiltrosTorneosInline')?.classList.toggle('hidden',!hayCriterios);document.querySelectorAll<HTMLElement>('[data-summary-state],[data-summary-open]').forEach(b=>{const activo=b.hasAttribute('data-summary-open')?catalogo.soloInscripcion:b.dataset.summaryState===catalogo.estado&&!catalogo.soloInscripcion;b.classList.toggle('active',Boolean(activo))});}
function limpiarFiltros(){catalogo.busqueda='';catalogo.rama='';catalogo.formato='';catalogo.soloInscripcion=false;catalogo.visibles=9;(document.getElementById('buscarTorneo') as HTMLInputElement).value='';(document.getElementById('filtroRama') as HTMLSelectElement).value='';(document.getElementById('filtroFormato') as HTMLSelectElement).value='';(document.getElementById('filtroInscripcion') as HTMLInputElement).checked=false;renderCatalogo();}
let origenPanelFiltros:ParentNode|null=null;
let siguientePanelFiltros:ChildNode|null=null;
function montarPortalFiltros(panel:HTMLElement,fondo:HTMLElement){if(!origenPanelFiltros){origenPanelFiltros=panel.parentNode;siguientePanelFiltros=panel.nextSibling}if(fondo.parentNode!==document.body)document.body.appendChild(fondo);if(panel.parentNode!==document.body)document.body.appendChild(panel);panel.classList.add('tournament-filter-portal')}
function restaurarPanelFiltros(panel:HTMLElement){if(origenPanelFiltros&&panel.parentNode!==origenPanelFiltros){if(siguientePanelFiltros&&siguientePanelFiltros.parentNode===origenPanelFiltros)origenPanelFiltros.insertBefore(panel,siguientePanelFiltros);else origenPanelFiltros.appendChild(panel)}panel.classList.remove('tournament-filter-portal')}
function panelFiltros(abierto:boolean){const panel=document.getElementById('panelFiltrosTorneos'),fondo=document.getElementById('fondoFiltrosTorneos'),disparador=document.getElementById('abrirFiltrosTorneos') as HTMLButtonElement|null,movil=matchMedia('(max-width: 899px)').matches;if(!panel||!fondo)return;if(abierto&&movil)montarPortalFiltros(panel,fondo);panel.classList.toggle('open',abierto);panel.classList.toggle('filters-visible',abierto);panel.setAttribute('aria-hidden',String(!abierto));fondo.classList.toggle('hidden',!abierto);fondo.classList.toggle('filters-visible',abierto);disparador?.setAttribute('aria-expanded',String(abierto));document.body.classList.toggle('tournament-filters-open',abierto&&movil);if(abierto&&movil)requestAnimationFrame(()=>{panel.scrollTop=0;const cerrar=panel.querySelector<HTMLElement>('#cerrarFiltrosTorneos');(cerrar||panel.querySelector<HTMLElement>('select,input,button'))?.focus({preventScroll:true})});else if(!abierto){disparador?.focus({preventScroll:true});requestAnimationFrame(()=>{if(!matchMedia('(max-width: 899px)').matches)restaurarPanelFiltros(panel)})}}
addEventListener('resize',()=>{const panel=document.getElementById('panelFiltrosTorneos');if(!panel)return;if(!matchMedia('(max-width: 899px)').matches){panelFiltros(false);restaurarPanelFiltros(panel)}},{passive:true});
type PestanaDetalle='informacion'|'categorias'|'reglamento';
let detalleActual:TorneoDetalle|null=null;
let pestanaDetalle:PestanaDetalle='informacion';
const categoriaDisponible=(c:TorneoDetalle['categorias'][number],t:TorneoDetalle)=>Boolean(t.inscripcionDisponible&&c.disponible&&c.cuposDisponibles>0);
function descripcionValida(v:unknown){const x=String(v??'').trim();return x.length>=12?x:'';}
function resumenDetalle(t:TorneoDetalle){const cupo=t.categorias.reduce((a,c)=>a+c.cupoParejas,0),confirmadas=t.categorias.reduce((a,c)=>a+c.parejasConfirmadas,0),libres=t.categorias.reduce((a,c)=>a+c.cuposDisponibles,0);return {cupo,confirmadas,libres};}
function precioRangoDetalle(t:TorneoDetalle){const precios=t.categorias.map(c=>Number(c.precioInscripcion||0));if(!precios.length)return 'Sin categorías';const min=Math.min(...precios),max=Math.max(...precios);return min===max?precioCategoria(min):`${precioCategoria(min)} a ${precioCategoria(max)}`;}
// F4.1C: detalle de torneo nativo de Plataforma Deportiva W2.
function estadoTemporalDetalle(t:TorneoDetalle){const hoy=new Date();hoy.setHours(0,0,0,0);const inicio=new Date(t.fechaInicio+'T00:00:00'),fin=new Date(t.fechaFin+'T23:59:59');if(!Number.isNaN(fin.getTime())&&fin<hoy)return 'FINALIZADO';if(!Number.isNaN(inicio.getTime())&&inicio<=hoy)return 'EN_CURSO';return t.estado}
function textoEstadoDetalle(t:TorneoDetalle){const e=estadoTemporalDetalle(t);return e==='FINALIZADO'?'Finalizado':e==='EN_CURSO'?'En curso':t.inscripcionDisponible?'Inscripción abierta':e==='INSCRIPCION_CERRADA'?'Inscripción cerrada':'Próximamente'}
function navegacionDetalle(t:TorneoDetalle){return `<nav class="w2td-tabs premium-detail-tabs" aria-label="Secciones del torneo" role="tablist">${([['informacion','Información'],['categorias',`Categorías <b>${t.categorias.length}</b>`],['reglamento','Reglamento']] as const).map(([id,label])=>`<button type="button" role="tab" aria-selected="${pestanaDetalle===id}" class="${pestanaDetalle===id?'active':''}" data-detail-tab="${id}">${label}</button>`).join('')}</nav>`}
function panelInformacion(t:TorneoDetalle){const r=resumenDetalle(t),descripcion=descripcionValida(t.descripcion),estado=estadoTemporalDetalle(t);return `<section class="w2td-panel w2td-information premium-detail-panel premium-information-panel"><div class="w2td-intro"><p class="w2-kicker">Sobre la competencia</p><h2>${escapeHtml(estado==='FINALIZADO'?'Una competencia que ya dejó su historia':estado==='EN_CURSO'?'La competencia está en marcha':t.inscripcionDisponible?'Todo listo para competir':'Prepará tu participación')}</h2><p>${escapeHtml(descripcion||'Consultá las categorías, revisá las condiciones y elegí junto a tu pareja la competencia adecuada.')}</p></div><dl class="w2td-metrics"><div><dt>Categorías</dt><dd>${t.categorias.length}</dd></div><div><dt>Parejas confirmadas</dt><dd>${r.confirmadas}</dd></div><div><dt>Cupos libres</dt><dd>${r.libres}</dd></div><div><dt>Inscripción por pareja</dt><dd>${precioRangoDetalle(t)}</dd></div></dl><ol class="w2td-timeline"><li class="${t.inscripcionDisponible?'active':'done'}"><i>01</i><b>Inscripción</b><small>${t.inscripcionDisponible?'Abierta ahora':'Etapa completada'}</small></li><li class="${estado==='EN_CURSO'?'active':estado==='FINALIZADO'?'done':''}"><i>02</i><b>Competencia</b><small>${estado==='EN_CURSO'?'En desarrollo':'Grupos y partidos'}</small></li><li class="${estado==='FINALIZADO'?'done':''}"><i>03</i><b>Definición</b><small>${estado==='FINALIZADO'?'Etapa completada':'Cuadro y campeones'}</small></li></ol></section>`}
function tarjetaCategoriaDetalle(c:TorneoDetalle['categorias'][number],t:TorneoDetalle){const disponible=categoriaDisponible(c,t),ocupacion=c.cupoParejas?Math.min(100,Math.round(c.parejasConfirmadas/c.cupoParejas*100)):0;return `<article class="w2td-category premium-category-card ${disponible?'available':''}"><header><div><span>${escapeHtml(ramaPublica(c.rama))}</span><h3>${escapeHtml(c.nombre)}</h3></div><em>${escapeHtml(textoFormato(c.formatoCompetencia))}</em></header><dl><div><dt>Inscripción por pareja</dt><dd>${precioCategoria(c.precioInscripcion)}</dd></div><div><dt>Disponibilidad</dt><dd>${c.cuposDisponibles>0?c.cuposDisponibles+' cupos':'Sin cupos'}</dd></div></dl><div class="w2td-occupancy"><p><span>Ocupación</span><b>${ocupacion}%</b></p><div><i style="width:${ocupacion}%"></i></div><small>${c.parejasConfirmadas} de ${c.cupoParejas} parejas confirmadas</small></div>${c.premioDescripcion?`<p class="w2td-prize"><small>Premios</small>${escapeHtml(c.premioDescripcion)}</p>`:''}<footer class="premium-category-actions-v4"><button type="button" data-open-competition="${c.id}">Ver competencia</button>${disponible?`<button class="primary inscribir-pareja" type="button" data-categoria-id="${c.id}" data-categoria-nombre="${escapeHtml(c.nombre)}" data-torneo-nombre="${escapeHtml(t.nombre)}">Inscribir pareja</button>`:`<span>${estadoTemporalDetalle(t)==='FINALIZADO'?'Competencia finalizada':'Inscripción no disponible'}</span>`}</footer></article>`}
function panelCategorias(t:TorneoDetalle){return `<section class="w2td-panel premium-detail-panel"><header class="w2td-section-head"><div><p class="w2-kicker">Elegí dónde competir</p><h2>Categorías del torneo</h2></div><p>Compará rama, formato, valor y disponibilidad. Todas las inscripciones se realizan por pareja.</p></header><div class="w2td-category-grid">${t.categorias.map(c=>tarjetaCategoriaDetalle(c,t)).join('')||'<div class="w2td-empty">Todavía no hay categorías públicas.</div>'}</div></section>`}
function panelReglamento(t:TorneoDetalle){const texto=String(t.reglamento??'').trim();return `<section class="w2td-panel w2td-rules premium-detail-panel premium-rules-panel"><header class="w2td-section-head"><div><p class="w2-kicker">Condiciones de juego</p><h2>Reglamento</h2></div><p>Leé las condiciones antes de enviar la inscripción de tu pareja.</p></header>${texto?`<div class="w2td-rules-content">${escapeHtml(texto).replace(/\n/g,'<br>')}</div>`:'<div class="w2td-empty"><b>Reglamento pendiente de publicación</b><p>La organización todavía no publicó las condiciones completas de esta competencia.</p></div>'}</section>`}
function detallePremium(t:TorneoDetalle){const r=resumenDetalle(t),descripcion=descripcionValida(t.descripcion),hayCupo=t.inscripcionDisponible&&t.categorias.some(c=>categoriaDisponible(c,t));return `<article class="w2td-detail premium-tournament-detail"><div class="w2td-topbar"><button type="button" data-back-catalog>← Volver a competencias</button><button type="button" data-share-tournament>Compartir torneo</button></div><header class="w2td-hero"><div class="w2td-hero-copy"><span class="w2td-status">${escapeHtml(textoEstadoDetalle(t))}</span><p class="w2-kicker">Competencia oficial W2</p><h1>${escapeHtml(t.nombre)}</h1><p>${escapeHtml(descripcion||'Viví cada etapa del torneo y encontrá toda la información oficial en un único lugar.')}</p><div class="w2td-actions">${hayCupo?'<button class="primary" type="button" data-go-categories>Ver categorías disponibles</button>':''}<button type="button" data-go-rules>Consultar reglamento</button></div></div><aside class="w2td-summary"><span class="wide"><small>Fechas</small><b>${escapeHtml(rangoFechas(t.fechaInicio,t.fechaFin))}</b></span><span><small>Categorías</small><b>${t.categorias.length}</b></span><span><small>Parejas</small><b>${r.confirmadas} / ${r.cupo}</b></span><span><small>Cupos libres</small><b>${r.libres}</b></span></aside></header><div id="premiumDetailTabs">${navegacionDetalle(t)}</div><div id="premiumDetailContent">${panelInformacion(t)}</div></article>`}
function renderPanelDetalle(t:TorneoDetalle){const host=document.getElementById('premiumDetailContent');if(!host)return;host.innerHTML=pestanaDetalle==='categorias'?panelCategorias(t):pestanaDetalle==='reglamento'?panelReglamento(t):panelInformacion(t);const tabs=document.getElementById('premiumDetailTabs');if(tabs)tabs.innerHTML=navegacionDetalle(t)}
function seleccionarPestanaDetalle(tab:PestanaDetalle,actualizarUrl=true){if(!detalleActual)return;pestanaDetalle=tab;renderPanelDetalle(detalleActual);if(actualizarUrl){const u=new URL(location.href);u.searchParams.set('seccion',tab);history.replaceState(history.state,'',u);}requestAnimationFrame(()=>document.getElementById('premiumDetailTabs')?.scrollIntoView({behavior:movimientoReducido()?'auto':'smooth',block:'start'}));}
async function compartirTorneo(){try{if(navigator.share)await navigator.share({title:detalleActual?.nombre||'Torneo',url:location.href});else{await navigator.clipboard.writeText(location.href);const b=document.querySelector<HTMLButtonElement>('[data-share-tournament]');if(b){const original=b.textContent;b.textContent='Enlace copiado';setTimeout(()=>b.textContent=original,1800);}}}catch{}}
async function abrirTorneo(id:number,actualizarUrl=true){const catalog=document.getElementById('vistaCatalogoTorneos')!,detail=document.getElementById('vistaDetalleTorneo')!,host=document.getElementById('detalleTorneoHost')!;catalog.classList.add('hidden');detail.classList.remove('hidden');host.innerHTML='<div class="competition-loading">Cargando torneo...</div>';if(actualizarUrl){const u=new URL(location.href);u.searchParams.set('torneo',String(id));['categoria','vista','grupo','instancia','ronda'].forEach(x=>u.searchParams.delete(x));history.pushState({torneo:id},'',u)}try{const t=await api<TorneoDetalle>(`/torneos/${id}`);detalleActual=t;pestanaDetalle=(new URLSearchParams(location.search).get('seccion') as PestanaDetalle)||'informacion';if(!['informacion','categorias','reglamento'].includes(pestanaDetalle))pestanaDetalle='informacion';host.innerHTML=detallePremium(t);document.title=`${t.nombre} | Pádel Reservas`;scrollTo({top:0,behavior:movimientoReducido()?'auto':'smooth'});}catch(error){host.innerHTML=`<div class="competition-error"><b>No pudimos abrir el torneo</b><p>${escapeHtml(error instanceof Error?error.message:'Intentá nuevamente.')}</p><button class="btn btn-secondary" type="button" data-retry-torneo="${id}">Reintentar</button></div>`;}}
function volverCatalogo(actualizarUrl=true){detalleActual=null;document.getElementById('vistaDetalleTorneo')?.classList.add('hidden');document.getElementById('vistaCatalogoTorneos')?.classList.remove('hidden');document.title='Torneos | Plataforma Deportiva W2';if(actualizarUrl){const u=new URL(location.href);['torneo','seccion','categoria','vista','grupo','instancia','ronda'].forEach(x=>u.searchParams.delete(x));history.pushState({},'',u)}requestAnimationFrame(()=>scrollTo({top:Number(sessionStorage.getItem('torneos-scroll')||0),behavior:'auto'}));}
// cierre-catalogo-movil-v80h-20261011
function limpiarInscripcionAlCambiarEstado(){
 if(!catalogo.soloInscripcion)return;
 catalogo.soloInscripcion=false;
 const input=document.getElementById('filtroInscripcion') as HTMLInputElement|null;
 if(input)input.checked=false;
}
function actualizarPlaceholderTorneos(){
 const input=document.getElementById('buscarTorneo') as HTMLInputElement|null;
 if(!input)return;
 input.placeholder=matchMedia('(max-width: 639px)').matches?'Buscar torneo o categoría':'Buscar por torneo, categoría, rama o formato';
}
document.addEventListener('click',evento=>{
 const tab=(evento.target as HTMLElement).closest<HTMLButtonElement>('[data-catalog-state]');
 if(!tab)return;
 limpiarInscripcionAlCambiarEstado();
},true);
addEventListener('resize',actualizarPlaceholderTorneos,{passive:true});
if(document.readyState==='loading')addEventListener('DOMContentLoaded',actualizarPlaceholderTorneos,{once:true});else actualizarPlaceholderTorneos();

// accesos-rapidos-y-filtros-nativos-v80e-20261011
function sincronizarFiltroInscripcion(valor:boolean){catalogo.soloInscripcion=valor;const input=document.getElementById('filtroInscripcion') as HTMLInputElement|null;if(input)input.checked=valor}
function irAResultadosCatalogo(){requestAnimationFrame(()=>document.getElementById('pestanasTorneos')?.scrollIntoView({behavior:movimientoReducido()?'auto':'smooth',block:'start'}))}
document.addEventListener('click',evento=>{const target=evento.target as HTMLElement;const estado=target.closest<HTMLButtonElement>('[data-summary-state]');const abiertas=target.closest<HTMLButtonElement>('[data-summary-open]');if(estado){evento.preventDefault();evento.stopImmediatePropagation();catalogo.estado=estado.dataset.summaryState as EstadoPortal;sincronizarFiltroInscripcion(false);catalogo.visibles=9;renderCatalogo();irAResultadosCatalogo();return}if(abiertas){evento.preventDefault();evento.stopImmediatePropagation();catalogo.estado='PROXIMO';sincronizarFiltroInscripcion(true);catalogo.visibles=9;renderCatalogo();irAResultadosCatalogo();return}},true);
document.addEventListener('click',evento=>{const target=evento.target as HTMLElement;if(target.closest('#abrirFiltrosTorneos')){evento.preventDefault();panelFiltros(true);return}if(target.closest('#cerrarFiltrosTorneos,#aplicarFiltrosTorneos,#fondoFiltrosTorneos')){evento.preventDefault();panelFiltros(false)}},true);
document.addEventListener('keydown',evento=>{if(evento.key==='Escape'&&document.body.classList.contains('tournament-filters-open')){evento.preventDefault();panelFiltros(false)}});

// perfeccion-portada-torneos-v52b-20261011
// cierre-portada-torneos-v53-20261011
// refinamiento-interaccion-torneos-v54-20261011
// cierre-estructural-torneos-v55-20261011
// pulido-final-portada-torneos-v56-20261011
// refinamiento-interaccion-torneos-v54-20261011
function renderPortadaTorneos(){
 const resumen=document.getElementById('resumenTorneos'),destacado=document.getElementById('torneoDestacado');
 const abiertos=catalogo.torneos.filter(t=>t.inscripcionDisponible),curso=catalogo.torneos.filter(t=>t.estadoPortal==='EN_CURSO'),proximos=catalogo.torneos.filter(t=>t.estadoPortal==='PROXIMO');
 if(resumen)resumen.innerHTML=`<button type="button" data-summary-state="PROXIMO"><b>${proximos.length}</b><span>${proximos.length===1?'próximo':'próximos'}</span></button><button type="button" data-summary-open><b>${abiertos.length}</b><span>${abiertos.length===1?'inscripción abierta':'inscripciones abiertas'}</span></button><button type="button" data-summary-state="EN_CURSO"><b>${curso.length}</b><span>en curso</span></button>`;
 const t=abiertos[0]||proximos[0]||curso[0]||catalogo.torneos[0];if(!destacado)return;
 if(!t){destacado.innerHTML='<div class="w2t-featured-loading"><b>Nuevas competencias en preparación</b><small>El calendario oficial aparecerá cuando la organización publique el próximo torneo.</small></div>';return}
 const estado=t.inscripcionDisponible?'Inscripción abierta':t.estadoPortal==='EN_CURSO'?'En curso':t.estadoPortal==='FINALIZADO'?'Finalizado':'Próximamente';
 const progreso=t.estadoPortal==='EN_CURSO'&&t.partidosTotales?Math.round(t.partidosFinalizados/t.partidosTotales*100):porcentajeOcupacion(t);destacado.innerHTML=`<div class="w2t-eyebrow"><span>Competencia destacada</span><span class="w2t-featured-status">${escapeHtml(estado)}</span></div><h2>${escapeHtml(t.nombre)}</h2><p>${escapeHtml(descripcionCatalogo(t.descripcion)||'Una competencia oficial para elegir, jugar y seguir en un solo lugar.')}</p><div class="w2t-featured-tags">${t.ramas.slice(0,2).map(x=>`<span>${escapeHtml(ramaPublica(x))}</span>`).join('')}${t.formatos.slice(0,1).map(x=>`<span>${escapeHtml(textoFormato(x))}</span>`).join('')}<span>${escapeHtml(rangoFechas(t.fechaInicio,t.fechaFin))}</span></div>${t.cupoTotal||t.partidosTotales?`<div class="w2t-featured-progress"><span><i style="width:${progreso}%"></i></span><small>${t.estadoPortal==='EN_CURSO'?t.partidosFinalizados+' de '+t.partidosTotales+' partidos finalizados':t.parejasConfirmadas+' de '+t.cupoTotal+' parejas confirmadas'}</small></div>`:''}<footer><span><b>${t.cuposDisponibles}</b><small>cupos disponibles</small></span><button class="catalog-open w2t-primary" type="button" data-torneo-id="${t.id}">${accionCatalogo(t)} →</button></footer>`;
}
async function cargar(){const contenedor=document.getElementById('listaTorneos');if(!contenedor)return;contenedor.innerHTML=Array.from({length:6},()=>'<div class="w2t-skeleton" aria-hidden="true"></div>').join('');try{catalogo.torneos=(await api<unknown[]>('/torneos')).map(adaptarResumen);renderPortadaTorneos();poblarSelect('filtroRama',catalogo.torneos.flatMap(t=>t.ramas),ramaPublica);poblarSelect('filtroFormato',catalogo.torneos.flatMap(t=>t.formatos),textoFormato);inicializarSelectsPremium();renderCatalogo();const parametros=new URLSearchParams(location.search),directo=Number(parametros.get('torneo'));if(Number.isInteger(directo)&&directo>0){await abrirTorneo(directo,false);const categoriaDirecta=Number(parametros.get('categoria'));if(categoriaDirecta>0&&detalleActual)await abrirCentroCompetitivo(detalleActual,categoriaDirecta,(parametros.get('vista')||'resumen') as VistaCentro,false);}}catch(error){contenedor.innerHTML=`<div class="w2t-error"><b>No pudimos cargar los torneos</b><p>${escapeHtml(error instanceof Error?error.message:'Revisá la conexión e intentá nuevamente.')}</p><button id="reintentarTorneos" class="btn btn-secondary" type="button">Reintentar</button></div>`;document.getElementById('reintentarTorneos')?.addEventListener('click',cargar,{once:true});}}


type EstadoScrollDetalle={mover:boolean;y:number};
function estadoScrollDetalle():EstadoScrollDetalle{const contenido=document.getElementById('premiumDetailContent'),tabs=document.querySelector<HTMLElement>('.premium-detail-tabs'),y=scrollY;if(!contenido||!tabs)return {mover:false,y};const rectTabs=tabs.getBoundingClientRect(),rectContenido=contenido.getBoundingClientRect(),topConfigurado=parseFloat(getComputedStyle(tabs).top)||0,tolerancia=3,tabsFijas=rectTabs.top<=topConfigurado+tolerancia,contenidoAvanzado=rectContenido.top<rectTabs.bottom-Math.max(32,rectTabs.height*.55);return {mover:tabsFijas&&contenidoAvanzado,y}}
function estabilizarScrollDetalle(estado:EstadoScrollDetalle){requestAnimationFrame(()=>requestAnimationFrame(()=>{if(!estado.mover){scrollTo({top:estado.y,behavior:'auto'});return}const contenido=document.getElementById('premiumDetailContent'),tabs=document.querySelector<HTMLElement>('.premium-detail-tabs');if(!contenido||!tabs)return;const margen=8,destino=Math.max(0,contenido.getBoundingClientRect().top+scrollY-tabs.offsetHeight-margen);scrollTo({top:destino,behavior:matchMedia('(prefers-reduced-motion: reduce)').matches?'auto':'smooth'})}))}
// F4.1C2: desplazamiento explicito de accesos rapidos.
function irAlContenidoDetalle(){requestAnimationFrame(()=>requestAnimationFrame(()=>{const contenido=document.getElementById('premiumDetailContent'),tabs=document.querySelector<HTMLElement>('.w2td-tabs,.premium-detail-tabs'),cabecera=document.querySelector<HTMLElement>('[data-w2-header],header[data-site-header]');if(!contenido)return;const altoCabecera=cabecera?.getBoundingClientRect().height||0,altoTabs=tabs?.getBoundingClientRect().height||0,margen=12,destino=Math.max(0,contenido.getBoundingClientRect().top+scrollY-altoCabecera-altoTabs-margen);scrollTo({top:destino,behavior:movimientoReducido()?'auto':'smooth'})}))}
document.addEventListener('click',e=>{const target=e.target as HTMLElement;const inscripcionPremium=target.closest<HTMLButtonElement>('.inscribir-pareja');if(inscripcionPremium&&!inscripcionPremium.disabled){if(!inscripcionPremium.dataset.resumen){const torneoNombre=inscripcionPremium.dataset.torneoNombre||detalleActual?.nombre||'Torneo';const categoriaNombre=inscripcionPremium.dataset.categoriaNombre||'Categoría';inscripcionPremium.dataset.resumen=torneoNombre+' · '+categoriaNombre;}abrirInscripcion(inscripcionPremium);return;}if(manejarCentroCompetitivoClick(target))return;const abrirCompetenciaPremium=target.closest<HTMLButtonElement>('[data-open-competition]');if(abrirCompetenciaPremium&&detalleActual){abrirCentroCompetitivo(detalleActual,Number(abrirCompetenciaPremium.dataset.openCompetition),'resumen');return;}if(target.closest('[data-back-catalog]')){volverCatalogo();return;}if(target.closest('[data-share-tournament]')){compartirTorneo();return;}if(target.closest('[data-go-categories]')){seleccionarPestanaDetalle('categorias');irAlContenidoDetalle();return;}if(target.closest('[data-go-rules]')){seleccionarPestanaDetalle('reglamento');irAlContenidoDetalle();return;}const tab=target.closest<HTMLButtonElement>('[data-detail-tab]');if(tab){const estado=estadoScrollDetalle();seleccionarPestanaDetalle(tab.dataset.detailTab as PestanaDetalle);estabilizarScrollDetalle(estado);return;}});
document.getElementById('listaTorneos')?.addEventListener('click', evento => {
  const abrir=(evento.target as HTMLElement).closest<HTMLButtonElement>('.catalog-open');if(abrir){sessionStorage.setItem('torneos-scroll',String(scrollY));abrirTorneo(Number(abrir.dataset.torneoId));return;}const retry=(evento.target as HTMLElement).closest<HTMLButtonElement>('[data-retry-torneo]');if(retry){abrirTorneo(Number(retry.dataset.retryTorneo),false);return;}
  const boton = (evento.target as HTMLElement).closest<HTMLButtonElement>('.inscribir-pareja');
  if (boton && !boton.disabled) { abrirInscripcion(boton); return; }
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


function cabeceraPublicaAltura(){const h=document.querySelector<HTMLElement>('header.site-header,.site-header,[data-site-header]');return h?.getBoundingClientRect().height||0}
function desplazarPrimeraCompetencia(){requestAnimationFrame(()=>requestAnimationFrame(()=>{const primera=document.querySelector<HTMLElement>('#listaTorneos .w2t-card,#listaTorneos .w2t-empty');if(!primera)return;const cabecera=cabeceraPublicaAltura(),margen=16,altoUtil=innerHeight-cabecera-margen*2,altoTarjeta=primera.getBoundingClientRect().height,topDocumento=primera.getBoundingClientRect().top+scrollY;const destino=altoTarjeta<=altoUtil?topDocumento-cabecera-margen-Math.max(0,(altoUtil-altoTarjeta)/2):topDocumento-cabecera-margen;scrollTo({top:Math.max(0,destino),behavior:movimientoReducido()?'auto':'smooth'})}))}
function cerrarSelectsPremium(excepto?:HTMLElement){document.querySelectorAll<HTMLElement>('.premium-select.open').forEach(x=>{if(x!==excepto){x.classList.remove('open');x.querySelector<HTMLButtonElement>('.premium-select-trigger')?.setAttribute('aria-expanded','false')}})}
// F4.1B3: selectores nativos de interfaz W2 y consistencia temporal.
function crearSelectPremium(select:HTMLSelectElement){
 if(select.dataset.premiumReady==='true')return;select.dataset.premiumReady='true';
 const prefijo='w2t-select';
 const raiz=document.createElement('div');raiz.className=prefijo;
 const boton=document.createElement('button');boton.type='button';boton.className=prefijo+'-trigger';boton.setAttribute('aria-haspopup','listbox');boton.setAttribute('aria-expanded','false');
 const lista=document.createElement('div');lista.className=prefijo+'-menu';lista.setAttribute('role','listbox');lista.tabIndex=-1;
 select.insertAdjacentElement('afterend',raiz);raiz.append(select,boton,lista);select.classList.add(prefijo+'-native');
 const rotulo=select.dataset.premiumSelect||'Seleccionar';
 const textoOpcion=(o:HTMLOptionElement)=>select.id==='filtroRama'&&o.value?ramaPublica(o.value):o.text;
 const dibujar=()=>{const opciones=[...select.options];const actual=opciones.find(o=>o.value===select.value)||opciones[0];boton.innerHTML=`<span><small>${escapeHtml(rotulo)}</small><b>${escapeHtml(textoOpcion(actual!))}</b></span><i aria-hidden="true"></i>`;lista.innerHTML=opciones.map(o=>`<button type="button" role="option" data-value="${escapeHtml(o.value)}" aria-selected="${o.value===select.value}" tabindex="-1"><span>${escapeHtml(textoOpcion(o))}</span><i aria-hidden="true">${o.value===select.value?'✓':''}</i></button>`).join('')};
 const cerrar=(devolverFoco=false)=>{raiz.classList.remove('open','open-up');boton.setAttribute('aria-expanded','false');if(devolverFoco)boton.focus({preventScroll:true})};
 const abrir=()=>{cerrarSelectsPremium();document.querySelectorAll<HTMLElement>('.w2t-select.open').forEach(x=>{if(x!==raiz){x.classList.remove('open','open-up');x.querySelector<HTMLButtonElement>('.w2t-select-trigger')?.setAttribute('aria-expanded','false')}});raiz.classList.remove('open-up');raiz.classList.add('open');boton.setAttribute('aria-expanded','true');const rect=raiz.getBoundingClientRect(),espacioAbajo=innerHeight-rect.bottom-12;lista.style.maxHeight=String(espacioAbajo>96?espacioAbajo:96)+'px';lista.style.overflowY='auto';requestAnimationFrame(()=>lista.querySelector<HTMLElement>('[aria-selected="true"]')?.focus())};
 boton.addEventListener('click',()=>raiz.classList.contains('open')?cerrar(true):abrir());
 lista.addEventListener('click',e=>{const opcion=(e.target as HTMLElement).closest<HTMLButtonElement>('[data-value]');if(!opcion)return;select.value=opcion.dataset.value||'';select.dispatchEvent(new Event('change',{bubbles:true}));dibujar();cerrar(true)});
 raiz.addEventListener('keydown',e=>{const items=[...lista.querySelectorAll<HTMLButtonElement>('[data-value]')];if(e.key==='Escape'){e.preventDefault();e.stopPropagation();cerrar(true);return}if(!raiz.classList.contains('open')&&['ArrowDown','ArrowUp','Enter',' '].includes(e.key)){e.preventDefault();abrir();return}const actual=Math.max(0,items.indexOf(document.activeElement as HTMLButtonElement));if(e.key==='ArrowDown'||e.key==='ArrowUp'){e.preventDefault();items[(actual+(e.key==='ArrowDown'?1:-1)+items.length)%items.length]?.focus()}if(e.key==='Home'){e.preventDefault();items[0]?.focus()}if(e.key==='End'){e.preventDefault();items.at(-1)?.focus()}if((e.key==='Enter'||e.key===' ')&&document.activeElement?.hasAttribute('data-value')){e.preventDefault();(document.activeElement as HTMLButtonElement).click()}});
 select.addEventListener('change',dibujar);new MutationObserver(dibujar).observe(select,{childList:true,subtree:true});dibujar();
}function inicializarSelectsPremium(){document.querySelectorAll<HTMLSelectElement>('select[data-premium-select]').forEach(crearSelectPremium)}
document.addEventListener('click',e=>{const objetivo=e.target as HTMLElement;if(!objetivo.closest('.premium-select,.w2t-select')){cerrarSelectsPremium();document.querySelectorAll<HTMLElement>('.w2t-select.open').forEach(x=>{x.classList.remove('open','open-up');x.querySelector<HTMLButtonElement>('.w2t-select-trigger')?.setAttribute('aria-expanded','false')})}});
document.getElementById('pestanasTorneos')?.addEventListener('click',e=>{const b=(e.target as HTMLElement).closest<HTMLButtonElement>('[data-catalog-state]');if(!b)return;catalogo.estado=b.dataset.catalogState as EstadoPortal;catalogo.visibles=9;renderCatalogo();});
document.getElementById('buscarTorneo')?.addEventListener('input',e=>{catalogo.busqueda=(e.target as HTMLInputElement).value;catalogo.visibles=9;renderCatalogo();});
document.getElementById('mostrarMasTorneos')?.addEventListener('click',()=>{catalogo.visibles+=9;renderCatalogo();});
document.getElementById('chipsFiltrosTorneos')?.addEventListener('click',e=>{const b=(e.target as HTMLElement).closest<HTMLButtonElement>('[data-remove-filter]');if(!b)return;const tipo=b.dataset.removeFilter;if(tipo==='inscripcion'){catalogo.soloInscripcion=false;(document.getElementById('filtroInscripcion') as HTMLInputElement).checked=false;}if(tipo==='rama'){catalogo.rama='';(document.getElementById('filtroRama') as HTMLSelectElement).value='';}if(tipo==='formato'){catalogo.formato='';(document.getElementById('filtroFormato') as HTMLSelectElement).value='';}catalogo.visibles=9;renderCatalogo();});
document.getElementById('abrirFiltrosTorneos')?.addEventListener('click',()=>panelFiltros(true));
document.getElementById('cerrarFiltrosTorneos')?.addEventListener('click',()=>panelFiltros(false));
document.getElementById('fondoFiltrosTorneos')?.addEventListener('click',()=>panelFiltros(false));
document.getElementById('limpiarFiltrosTorneos')?.addEventListener('click',limpiarFiltros);document.getElementById('limpiarFiltrosTorneosInline')?.addEventListener('click',limpiarFiltros);
const aplicarFiltros=()=>{catalogo.rama=(document.getElementById('filtroRama') as HTMLSelectElement).value;catalogo.formato=(document.getElementById('filtroFormato') as HTMLSelectElement).value;catalogo.soloInscripcion=(document.getElementById('filtroInscripcion') as HTMLInputElement).checked;catalogo.visibles=9;renderCatalogo();};document.getElementById('aplicarFiltrosTorneos')?.addEventListener('click',()=>{aplicarFiltros();panelFiltros(false)});['filtroRama','filtroFormato','filtroInscripcion'].forEach(id=>document.getElementById(id)?.addEventListener('change',()=>{if(matchMedia('(min-width: 900px)').matches)aplicarFiltros()}));document.getElementById('resumenTorneos')?.addEventListener('click',e=>{const b=(e.target as HTMLElement).closest<HTMLButtonElement>('button');if(!b)return;if(b.dataset.summaryState)catalogo.estado=b.dataset.summaryState as EstadoPortal;if(b.hasAttribute('data-summary-open')){catalogo.estado='PROXIMO';catalogo.soloInscripcion=true;(document.getElementById('filtroInscripcion') as HTMLInputElement).checked=true}catalogo.visibles=9;renderCatalogo();desplazarPrimeraCompetencia()});document.getElementById('torneoDestacado')?.addEventListener('click',e=>{const b=(e.target as HTMLElement).closest<HTMLButtonElement>('.catalog-open');if(b){sessionStorage.setItem('torneos-scroll',String(scrollY));abrirTorneo(Number(b.dataset.torneoId))}});
document.getElementById('volverCatalogoTorneos')?.addEventListener('click',()=>volverCatalogo());
addEventListener('popstate',async()=>{const p=new URLSearchParams(location.search),id=Number(p.get('torneo')),cat=Number(p.get('categoria'));if(cat>0&&centroActivo()&&restaurarCentroDesdeUrl())return;if(id>0){await abrirTorneo(id,false);if(cat>0&&detalleActual)await abrirCentroCompetitivo(detalleActual,cat,(p.get('vista')||'resumen') as VistaCentro,false)}else volverCatalogo(false);});
addEventListener('f4-back',(e:any)=>{detalleActual=e.detail.torneo;pestanaDetalle='categorias';document.getElementById('detalleTorneoHost')!.innerHTML=detallePremium(detalleActual!);seleccionarPestanaDetalle('categorias',false);});
cargar();

// accesibilidad-web-v85-20261011
function accesibilidadFiltrosTorneos(){const panel=document.getElementById('panelFiltrosTorneos');if(!panel)return;const abierto=panel.classList.contains('open');panel.toggleAttribute('inert',!abierto&&matchMedia('(max-width:899px)').matches)}
document.addEventListener('keydown',ev=>{const panel=document.getElementById('panelFiltrosTorneos');if(!panel?.classList.contains('open')||ev.key!=='Tab'||!matchMedia('(max-width:899px)').matches)return;const f=[...panel.querySelectorAll<HTMLElement>('button:not([disabled]),input:not([disabled]),select:not([disabled]),[tabindex="0"]')].filter(x=>x.offsetParent!==null);if(!f.length)return;if(ev.shiftKey&&document.activeElement===f[0]){ev.preventDefault();f.at(-1)?.focus()}else if(!ev.shiftKey&&document.activeElement===f.at(-1)){ev.preventDefault();f[0].focus()}});
new MutationObserver(accesibilidadFiltrosTorneos).observe(document.getElementById('panelFiltrosTorneos')!,{attributes:true,attributeFilter:['class']});accesibilidadFiltrosTorneos();


// F4.1: navegación accesible y precarga controlada del centro competitivo.
document.addEventListener('keydown',evento=>{manejarCentroCompetitivoTeclado(evento)});
document.addEventListener('pointerover',evento=>{const boton=(evento.target as HTMLElement).closest<HTMLElement>('[data-open-competition],[data-f4-category]');const id=Number(boton?.dataset.openCompetition||boton?.dataset.f4Category);if(id>0)precargarCentroCompetitivo(id)},{passive:true});
