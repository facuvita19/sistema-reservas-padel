import { api, type TorneoDetalle, type TorneoResumen } from './api';
import { abrirCentroCompetitivo, manejarCentroCompetitivoClick, manejarCentroCompetitivoTeclado, precargarCentroCompetitivo, centroActivo, restaurarCentroDesdeUrl, type VistaCentro } from './centro-competitivo-premium';
import { normalizarBusquedaTorneo, ramaPublica, textoFormato } from './torneos/dominio-torneos';
import { crearEstadoCatalogoTorneos, type EstadoCatalogoTorneos } from './torneos/estado-catalogo-torneos';
import { adaptarResumenTorneo } from './torneos/adaptador-catalogo-torneos';
import { crearDatosTorneos } from './torneos/datos-torneos';
import { capturarRetornoCatalogo, restaurarRetornoCatalogo } from './torneos/retorno-catalogo-torneos';
import { poblarSelectCatalogo, renderCatalogoTorneos, renderPortadaCatalogoTorneos } from './torneos/presentacion-catalogo-torneos';
import { crearInteraccionCatalogoTorneos } from './torneos/interaccion-catalogo-torneos';
import { crearInscripcionParejaTorneos } from './torneos/inscripcion-pareja-torneos';
import { crearCoordinacionDetalleTorneos } from './torneos/coordinacion-detalle-torneos';
import { crearArranquePortalTorneos } from './torneos/arranque-portal-torneos';

const escapeHtml=(valor:unknown)=>String(valor??'').replace(/[&<>"']/g,caracter=>({
 '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'
}[caracter]||caracter));
const movimientoReducido=()=>window.matchMedia('(prefers-reduced-motion: reduce)').matches;

const catalogo:EstadoCatalogoTorneos<TorneoResumen>={torneos:[],estado:'PROXIMO',busqueda:'',rama:'',formato:'',soloInscripcion:false,visibles:9};
// R2.1C2 estado y URL del catalogo extraidos del coordinador
const estadoCatalogo=crearEstadoCatalogoTorneos(catalogo);
const leerEstadoCatalogo=()=>estadoCatalogo.leerDesdeUrl();
const reflejarEstadoCatalogo=()=>estadoCatalogo.reflejarEnControles();
const escribirEstadoCatalogo=(modo:'push'|'replace'='replace')=>estadoCatalogo.escribirEnUrl(modo);

function resultadosCatalogo(){const q=normalizarBusquedaTorneo(catalogo.busqueda);return catalogo.torneos.filter(t=>t.estadoPortal===catalogo.estado&&(!q||normalizarBusquedaTorneo([t.nombre,t.descripcion,...t.categorias,...t.ramas,...t.formatos].join(' ')).includes(q))&&(!catalogo.rama||t.ramas.includes(catalogo.rama))&&(!catalogo.formato||t.formatos.includes(catalogo.formato))&&(!catalogo.soloInscripcion||t.inscripcionDisponible));}

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
// R2.1C4 carga, cache e invalidacion extraidas del coordinador
const datosTorneos=crearDatosTorneos<TorneoResumen,TorneoDetalle>({
 cargarCatalogo:signal=>api<unknown[]>('/torneos',{signal}).then(items=>items.map(adaptarResumenTorneo)),
 cargarDetalle:(id,signal)=>api<TorneoDetalle>(`/torneos/${id}`,{signal}),
 duracionCacheMs:60_000
});
const obtenerDetalleTorneo=(id:number,signal:AbortSignal)=>datosTorneos.obtenerDetalle(id,signal);
const detalleTorneo=crearCoordinacionDetalleTorneos({
 nuevaSolicitud:()=>datosTorneos.nuevaSolicitudDetalle(),
 obtener:obtenerDetalleTorneo,
 vigente:version=>datosTorneos.esSolicitudDetalleVigente(version),
 abortada:error=>datosTorneos.esAbortada(error),
 restaurarCatalogo:restaurarRetornoCatalogo,
 movimientoReducido,
 escapeHtml
});
function abrirTorneo(id:number,actualizarUrl=true){return detalleTorneo.abrir(id,actualizarUrl)}
function volverCatalogo(actualizarUrl=true){detalleTorneo.volver(actualizarUrl)}
const invalidarDatosTorneos=()=>datosTorneos.invalidar(detalleTorneo.torneoActual()?.id);
const inscripcionPareja=crearInscripcionParejaTorneos({
 api,
 alCompletar:async()=>{invalidarDatosTorneos();await cargar(true)}
});

const arranquePortal=crearArranquePortalTorneos({
 catalogo,
 nuevaSolicitud:()=>datosTorneos.nuevaSolicitudCatalogo(),
 obtener:(signal,forzar)=>datosTorneos.obtenerCatalogo(signal,forzar),
 vigente:version=>datosTorneos.esSolicitudCatalogoVigente(version),
 abortada:error=>datosTorneos.esAbortada(error),
 leerEstado:leerEstadoCatalogo,
 renderPortada:()=>renderPortadaCatalogoTorneos(catalogo),
 poblarSelects:()=>{
  poblarSelectCatalogo('filtroRama',catalogo.torneos.flatMap(t=>t.ramas),ramaPublica);
  poblarSelectCatalogo('filtroFormato',catalogo.torneos.flatMap(t=>t.formatos),textoFormato);
 },
 inicializarSelects:inicializarSelectsPremium,
 reflejarEstado:reflejarEstadoCatalogo,
 renderCatalogo,
 escribirEstado:()=>escribirEstadoCatalogo('replace'),
 abrirTorneo,
 torneoActual:()=>detalleTorneo.torneoActual(),
 abrirCentro:abrirCentroCompetitivo,
 escapeHtml
});
const cargar=(forzar=false)=>arranquePortal.cargar(forzar);

document.addEventListener('click',e=>{const target=e.target as HTMLElement;const inscripcion=target.closest<HTMLButtonElement>('.inscribir-pareja');if(inscripcion&&!inscripcion.disabled){if(!inscripcion.dataset.resumen){const torneo=inscripcion.dataset.torneoNombre||detalleTorneo.torneoActual()?.nombre||'Torneo',categoria=inscripcion.dataset.categoriaNombre||'Categoría';inscripcion.dataset.resumen=torneo+' · '+categoria}void inscripcionPareja.abrir(inscripcion);return}if(manejarCentroCompetitivoClick(target))return;const competencia=target.closest<HTMLButtonElement>('[data-open-competition]'),actual=detalleTorneo.torneoActual();if(competencia&&actual){void abrirCentroCompetitivo(actual,Number(competencia.dataset.openCompetition),'resumen');return}detalleTorneo.manejarClick(target)});
document.getElementById('listaTorneos')?.addEventListener('click', evento => {
  const abrir=(evento.target as HTMLElement).closest<HTMLButtonElement>('.catalog-open');if(abrir){capturarRetornoCatalogo(abrir);abrirTorneo(Number(abrir.dataset.torneoId));return;}const retry=(evento.target as HTMLElement).closest<HTMLButtonElement>('[data-retry-torneo]');if(retry){abrirTorneo(Number(retry.dataset.retryTorneo),false);return;}
  const boton = (evento.target as HTMLElement).closest<HTMLButtonElement>('.inscribir-pareja');
  if (boton && !boton.disabled) { void inscripcionPareja.abrir(boton); return; }
});

document.getElementById('volverCatalogoTorneos')?.addEventListener('click',()=>volverCatalogo());
addEventListener('popstate',async()=>{const p=new URLSearchParams(location.search),id=Number(p.get('torneo')),cat=Number(p.get('categoria'));if(cat>0&&centroActivo()&&restaurarCentroDesdeUrl())return;if(id>0){await abrirTorneo(id,false);const actual=detalleTorneo.torneoActual();if(cat>0&&actual)await abrirCentroCompetitivo(actual,cat,(p.get('vista')||'resumen') as VistaCentro,false)}else{estadoCatalogo.restaurarHistorial(()=>{leerEstadoCatalogo();reflejarEstadoCatalogo();renderCatalogo();volverCatalogo(false)})}});
addEventListener('f4-back',(e:Event)=>detalleTorneo.restaurarDesdeCentro((e as CustomEvent<{torneo:TorneoDetalle}>).detail.torneo));
cargar();

// F4.1: navegación accesible y precarga controlada del centro competitivo.
document.addEventListener('keydown',evento=>{manejarCentroCompetitivoTeclado(evento)});
document.addEventListener('pointerover',evento=>{const boton=(evento.target as HTMLElement).closest<HTMLElement>('[data-open-competition],[data-f4-category]');const id=Number(boton?.dataset.openCompetition||boton?.dataset.f4Category);if(id>0)precargarCentroCompetitivo(id)},{passive:true});
