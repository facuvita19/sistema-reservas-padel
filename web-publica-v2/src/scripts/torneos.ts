import { api, type TorneoDetalle, type TorneoResumen } from './api';
import { abrirCentroCompetitivo, manejarCentroCompetitivoClick, manejarCentroCompetitivoTeclado, precargarCentroCompetitivo, centroActivo, restaurarCentroDesdeUrl, type VistaCentro } from './centro-competitivo-premium';
import { normalizarBusquedaTorneo, ramaPublica, textoFormato } from './torneos/dominio-torneos';
import { crearEstadoCatalogoTorneos, type EstadoCatalogoTorneos } from './torneos/estado-catalogo-torneos';
import { adaptarResumenTorneo } from './torneos/adaptador-catalogo-torneos';
import { crearDatosTorneos } from './torneos/datos-torneos';
import { detallePremium, renderPanelDetalleTorneo, type PestanaDetalle } from './torneos/presentacion-detalle-torneo';
import { capturarRetornoCatalogo, restaurarRetornoCatalogo } from './torneos/retorno-catalogo-torneos';
import { poblarSelectCatalogo, renderCatalogoTorneos, renderPortadaCatalogoTorneos } from './torneos/presentacion-catalogo-torneos';
import { crearInteraccionCatalogoTorneos } from './torneos/interaccion-catalogo-torneos';

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

const catalogo:EstadoCatalogoTorneos<TorneoResumen>={torneos:[],estado:'PROXIMO',busqueda:'',rama:'',formato:'',soloInscripcion:false,visibles:9};
// R2.1C2 estado y URL del catalogo extraidos del coordinador
const estadoCatalogo=crearEstadoCatalogoTorneos(catalogo);
const leerEstadoCatalogo=()=>estadoCatalogo.leerDesdeUrl();
const reflejarEstadoCatalogo=()=>estadoCatalogo.reflejarEnControles();
const escribirEstadoCatalogo=(modo:'push'|'replace'='replace')=>estadoCatalogo.escribirEnUrl(modo);

function resultadosCatalogo(){const q=normalizarBusquedaTorneo(catalogo.busqueda);return catalogo.torneos.filter(t=>t.estadoPortal===catalogo.estado&&(!q||normalizarBusquedaTorneo([t.nombre,t.descripcion,...t.categorias,...t.ramas,...t.formatos].join(' ')).includes(q))&&(!catalogo.rama||t.ramas.includes(catalogo.rama))&&(!catalogo.formato||t.formatos.includes(catalogo.formato))&&(!catalogo.soloInscripcion||t.inscripcionDisponible));}
// portada-torneos-premium-v51-20261011






function renderCatalogo(){renderCatalogoTorneos(catalogo,resultadosCatalogo(),limpiarFiltros)}


function limpiarFiltros(){catalogo.busqueda='';catalogo.rama='';catalogo.formato='';catalogo.soloInscripcion=false;catalogo.visibles=9;(document.getElementById('buscarTorneo') as HTMLInputElement).value='';(document.getElementById('filtroRama') as HTMLSelectElement).value='';(document.getElementById('filtroFormato') as HTMLSelectElement).value='';(document.getElementById('filtroInscripcion') as HTMLInputElement).checked=false;renderCatalogo();escribirEstadoCatalogo('replace')}
const interaccionCatalogo=crearInteraccionCatalogoTorneos({
 catalogo,
 renderCatalogo,
 limpiarFiltros,
 escribirEstadoCatalogo,
 abrirTorneo,
 capturarRetornoCatalogo,
 movimientoReducido
});
const inicializarSelectsPremium=()=>interaccionCatalogo.inicializarSelectsPremium();
let detalleActual:TorneoDetalle|null=null;
let pestanaDetalle:PestanaDetalle='informacion';
// R2.1C4 carga, cache e invalidacion extraidas del coordinador
const datosTorneos=crearDatosTorneos<TorneoResumen,TorneoDetalle>({
 cargarCatalogo:signal=>api<unknown[]>('/torneos',{signal}).then(items=>items.map(adaptarResumenTorneo)),
 cargarDetalle:(id,signal)=>api<TorneoDetalle>(`/torneos/${id}`,{signal}),
 duracionCacheMs:60_000
});
const obtenerCatalogo=(signal:AbortSignal,forzar=false)=>datosTorneos.obtenerCatalogo(signal,forzar);
const obtenerDetalleTorneo=(id:number,signal:AbortSignal)=>datosTorneos.obtenerDetalle(id,signal);
const invalidarDatosTorneos=()=>datosTorneos.invalidar(detalleActual?.id);



// F4.1C: detalle de torneo nativo de Plataforma Deportiva W2.








function renderPanelDetalle(t:TorneoDetalle){renderPanelDetalleTorneo(t,pestanaDetalle)}
function seleccionarPestanaDetalle(tab:PestanaDetalle,actualizarUrl=true){if(!detalleActual)return;pestanaDetalle=tab;renderPanelDetalle(detalleActual);if(actualizarUrl){const u=new URL(location.href);u.searchParams.set('seccion',tab);history.replaceState(history.state,'',u);}requestAnimationFrame(()=>document.getElementById('premiumDetailTabs')?.scrollIntoView({behavior:movimientoReducido()?'auto':'smooth',block:'start'}));}
async function compartirTorneo(){try{if(navigator.share)await navigator.share({title:detalleActual?.nombre||'Torneo',url:location.href});else{await navigator.clipboard.writeText(location.href);const b=document.querySelector<HTMLButtonElement>('[data-share-tournament]');if(b){const original=b.textContent;b.textContent='Enlace copiado';setTimeout(()=>b.textContent=original,1800);}}}catch{}}
async function abrirTorneo(id:number,actualizarUrl=true){const catalog=document.getElementById('vistaCatalogoTorneos')!,detail=document.getElementById('vistaDetalleTorneo')!,host=document.getElementById('detalleTorneoHost')!;catalog.classList.add('hidden');detail.classList.remove('hidden');host.setAttribute('aria-busy','true');host.innerHTML='<div class="competition-loading">Cargando torneo...</div>';if(actualizarUrl){const u=new URL(location.href);u.searchParams.set('torneo',String(id));['seccion','categoria','vista','grupo','instancia','ronda'].forEach(x=>u.searchParams.delete(x));history.pushState({nivel:'detalle',torneo:id},'',u)}const solicitud=datosTorneos.nuevaSolicitudDetalle();const controlador=solicitud.controlador;const version=solicitud.version;try{const t=await obtenerDetalleTorneo(id,controlador.signal);if(!datosTorneos.esSolicitudDetalleVigente(version))return;detalleActual=t;pestanaDetalle=(new URLSearchParams(location.search).get('seccion') as PestanaDetalle)||'informacion';if(!['informacion','categorias','reglamento'].includes(pestanaDetalle))pestanaDetalle='informacion';host.removeAttribute('aria-busy');host.innerHTML=detallePremium(t,pestanaDetalle);document.title=`${t.nombre} | Plataforma Deportiva W2`;scrollTo({top:0,behavior:movimientoReducido()?'auto':'smooth'})}catch(error){if(!datosTorneos.esSolicitudDetalleVigente(version)||datosTorneos.esAbortada(error))return;host.removeAttribute('aria-busy');host.innerHTML=`<div class="competition-error"><b>No pudimos abrir el torneo</b><p>${escapeHtml(error instanceof Error?error.message:'Intentá nuevamente.')}</p><button class="btn btn-secondary" type="button" data-retry-torneo="${id}">Reintentar</button></div>`}}
function volverCatalogo(actualizarUrl=true){detalleActual=null;document.getElementById('vistaDetalleTorneo')?.classList.add('hidden');document.getElementById('vistaCatalogoTorneos')?.classList.remove('hidden');document.title='Torneos | Plataforma Deportiva W2';if(actualizarUrl){const u=new URL(location.href);['torneo','seccion','categoria','vista','grupo','instancia','ronda'].forEach(x=>u.searchParams.delete(x));history.pushState({},'',u)}restaurarRetornoCatalogo();}
// perfeccion-portada-torneos-v52b-20261011
// cierre-portada-torneos-v53-20261011
// refinamiento-interaccion-torneos-v54-20261011
// cierre-estructural-torneos-v55-20261011
// pulido-final-portada-torneos-v56-20261011
// refinamiento-interaccion-torneos-v54-20261011

async function cargar(forzar=false){const contenedor=document.getElementById('listaTorneos');if(!contenedor)return;const solicitud=datosTorneos.nuevaSolicitudCatalogo();const controlador=solicitud.controlador;const version=solicitud.version;contenedor.setAttribute('aria-busy','true');contenedor.innerHTML=Array.from({length:6},()=>'<div class="w2t-skeleton" aria-hidden="true"></div>').join('');try{const dato=await obtenerCatalogo(controlador.signal,forzar);if(!datosTorneos.esSolicitudCatalogoVigente(version))return;catalogo.torneos=dato;contenedor.removeAttribute('aria-busy');leerEstadoCatalogo();renderPortadaCatalogoTorneos(catalogo);poblarSelectCatalogo('filtroRama',catalogo.torneos.flatMap(t=>t.ramas),ramaPublica);poblarSelectCatalogo('filtroFormato',catalogo.torneos.flatMap(t=>t.formatos),textoFormato);inicializarSelectsPremium();reflejarEstadoCatalogo();renderCatalogo();escribirEstadoCatalogo('replace');const parametros=new URLSearchParams(location.search),directo=Number(parametros.get('torneo'));if(Number.isInteger(directo)&&directo>0){await abrirTorneo(directo,false);const categoriaDirecta=Number(parametros.get('categoria'));if(categoriaDirecta>0&&detalleActual)await abrirCentroCompetitivo(detalleActual,categoriaDirecta,(parametros.get('vista')||'resumen') as VistaCentro,false)}}catch(error){if(!datosTorneos.esSolicitudCatalogoVigente(version)||datosTorneos.esAbortada(error))return;contenedor.removeAttribute('aria-busy');contenedor.innerHTML=`<div class="w2t-error"><b>No pudimos cargar los torneos</b><p>${escapeHtml(error instanceof Error?error.message:'Revisá la conexión e intentá nuevamente.')}</p><button id="reintentarTorneos" class="btn btn-secondary" type="button">Reintentar</button></div>`;document.getElementById('reintentarTorneos')?.addEventListener('click',()=>cargar(true),{once:true})}}


type EstadoScrollDetalle={mover:boolean;y:number};
function estadoScrollDetalle():EstadoScrollDetalle{const contenido=document.getElementById('premiumDetailContent'),tabs=document.querySelector<HTMLElement>('.premium-detail-tabs'),y=scrollY;if(!contenido||!tabs)return {mover:false,y};const rectTabs=tabs.getBoundingClientRect(),rectContenido=contenido.getBoundingClientRect(),topConfigurado=parseFloat(getComputedStyle(tabs).top)||0,tolerancia=3,tabsFijas=rectTabs.top<=topConfigurado+tolerancia,contenidoAvanzado=rectContenido.top<rectTabs.bottom-Math.max(32,rectTabs.height*.55);return {mover:tabsFijas&&contenidoAvanzado,y}}
function estabilizarScrollDetalle(estado:EstadoScrollDetalle){requestAnimationFrame(()=>requestAnimationFrame(()=>{if(!estado.mover){scrollTo({top:estado.y,behavior:'auto'});return}const contenido=document.getElementById('premiumDetailContent'),tabs=document.querySelector<HTMLElement>('.premium-detail-tabs');if(!contenido||!tabs)return;const margen=8,destino=Math.max(0,contenido.getBoundingClientRect().top+scrollY-tabs.offsetHeight-margen);scrollTo({top:destino,behavior:matchMedia('(prefers-reduced-motion: reduce)').matches?'auto':'smooth'})}))}
// F4.1C2: desplazamiento explicito de accesos rapidos.
function irAlContenidoDetalle(){requestAnimationFrame(()=>requestAnimationFrame(()=>{const contenido=document.getElementById('premiumDetailContent'),tabs=document.querySelector<HTMLElement>('.w2td-tabs,.premium-detail-tabs'),cabecera=document.querySelector<HTMLElement>('[data-w2-header],header[data-site-header]');if(!contenido)return;const altoCabecera=cabecera?.getBoundingClientRect().height||0,altoTabs=tabs?.getBoundingClientRect().height||0,margen=12,destino=Math.max(0,contenido.getBoundingClientRect().top+scrollY-altoCabecera-altoTabs-margen);scrollTo({top:destino,behavior:movimientoReducido()?'auto':'smooth'})}))}
document.addEventListener('click',e=>{const target=e.target as HTMLElement;const inscripcionPremium=target.closest<HTMLButtonElement>('.inscribir-pareja');if(inscripcionPremium&&!inscripcionPremium.disabled){if(!inscripcionPremium.dataset.resumen){const torneoNombre=inscripcionPremium.dataset.torneoNombre||detalleActual?.nombre||'Torneo';const categoriaNombre=inscripcionPremium.dataset.categoriaNombre||'Categoría';inscripcionPremium.dataset.resumen=torneoNombre+' · '+categoriaNombre;}abrirInscripcion(inscripcionPremium);return;}if(manejarCentroCompetitivoClick(target))return;const abrirCompetenciaPremium=target.closest<HTMLButtonElement>('[data-open-competition]');if(abrirCompetenciaPremium&&detalleActual){abrirCentroCompetitivo(detalleActual,Number(abrirCompetenciaPremium.dataset.openCompetition),'resumen');return;}if(target.closest('[data-back-catalog]')){volverCatalogo();return;}if(target.closest('[data-share-tournament]')){compartirTorneo();return;}if(target.closest('[data-go-categories]')){seleccionarPestanaDetalle('categorias');irAlContenidoDetalle();return;}if(target.closest('[data-go-rules]')){seleccionarPestanaDetalle('reglamento');irAlContenidoDetalle();return;}const tab=target.closest<HTMLButtonElement>('[data-detail-tab]');if(tab){const estado=estadoScrollDetalle();seleccionarPestanaDetalle(tab.dataset.detailTab as PestanaDetalle);estabilizarScrollDetalle(estado);return;}});
document.getElementById('listaTorneos')?.addEventListener('click', evento => {
  const abrir=(evento.target as HTMLElement).closest<HTMLButtonElement>('.catalog-open');if(abrir){capturarRetornoCatalogo(abrir);abrirTorneo(Number(abrir.dataset.torneoId));return;}const retry=(evento.target as HTMLElement).closest<HTMLButtonElement>('[data-retry-torneo]');if(retry){abrirTorneo(Number(retry.dataset.retryTorneo),false);return;}
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
    establecerEnviando(false);mostrarExito(creada);invalidarDatosTorneos();await cargar(true);
  }catch(error){
    establecerEnviando(false);errorCaja.textContent=error instanceof Error?error.message:'No se pudo enviar la inscripción. Revisá los datos e intentá nuevamente.';
    errorCaja.classList.remove('hidden');errorCaja.focus();errorCaja.scrollIntoView({behavior:'smooth',block:'center'});
  }
});


document.getElementById('volverCatalogoTorneos')?.addEventListener('click',()=>volverCatalogo());
addEventListener('popstate',async()=>{const p=new URLSearchParams(location.search),id=Number(p.get('torneo')),cat=Number(p.get('categoria'));if(cat>0&&centroActivo()&&restaurarCentroDesdeUrl())return;if(id>0){await abrirTorneo(id,false);if(cat>0&&detalleActual)await abrirCentroCompetitivo(detalleActual,cat,(p.get('vista')||'resumen') as VistaCentro,false)}else{estadoCatalogo.restaurarHistorial(()=>{leerEstadoCatalogo();reflejarEstadoCatalogo();renderCatalogo();volverCatalogo(false)})}});
addEventListener('f4-back',(e:any)=>{detalleActual=e.detail.torneo;pestanaDetalle='categorias';document.getElementById('detalleTorneoHost')!.innerHTML=detallePremium(detalleActual!,pestanaDetalle);seleccionarPestanaDetalle('categorias',false);});
cargar();

// F4.1: navegación accesible y precarga controlada del centro competitivo.
document.addEventListener('keydown',evento=>{manejarCentroCompetitivoTeclado(evento)});
document.addEventListener('pointerover',evento=>{const boton=(evento.target as HTMLElement).closest<HTMLElement>('[data-open-competition],[data-f4-category]');const id=Number(boton?.dataset.openCompetition||boton?.dataset.f4Category);if(id>0)precargarCentroCompetitivo(id)},{passive:true});
