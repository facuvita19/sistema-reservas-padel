import { api, type TorneoDetalle, type TorneoResumen } from './api';
import { abrirCentroCompetitivo, manejarCentroCompetitivoClick, manejarCentroCompetitivoTeclado, precargarCentroCompetitivo, centroActivo, restaurarCentroDesdeUrl } from './centro-competitivo-premium';
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
import { crearIntegracionNavegacionTorneos } from './torneos/integracion-navegacion-torneos';

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

crearIntegracionNavegacionTorneos({
 abrirInscripcion:boton=>inscripcionPareja.abrir(boton),
 torneoActual:()=>detalleTorneo.torneoActual(),
 manejarCentroClick:manejarCentroCompetitivoClick,
 abrirCentro:abrirCentroCompetitivo,
 manejarDetalleClick:target=>detalleTorneo.manejarClick(target),
 capturarRetorno:capturarRetornoCatalogo,
 abrirTorneo,
 volverCatalogo,
 centroActivo,
 restaurarCentroDesdeUrl,
 restaurarCatalogoDesdeHistorial:restaurar=>estadoCatalogo.restaurarHistorial(restaurar),
 restaurarEstadoCatalogo:()=>{leerEstadoCatalogo();reflejarEstadoCatalogo();renderCatalogo()},
 restaurarDetalleDesdeCentro:torneo=>detalleTorneo.restaurarDesdeCentro(torneo),
 manejarCentroTeclado:manejarCentroCompetitivoTeclado,
 precargarCentro:precargarCentroCompetitivo
});

void cargar();
