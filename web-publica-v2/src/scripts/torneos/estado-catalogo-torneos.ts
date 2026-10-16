export type EstadoPortalTorneo='PROXIMO'|'EN_CURSO'|'FINALIZADO';
export type ModoHistorialTorneo='push'|'replace';

export type EstadoCatalogoTorneos<T>={
 torneos:T[];
 estado:EstadoPortalTorneo;
 busqueda:string;
 rama:string;
 formato:string;
 soloInscripcion:boolean;
 visibles:number;
};

const ESTADOS_PORTAL:EstadoPortalTorneo[]=['PROXIMO','EN_CURSO','FINALIZADO'];

/** Contrato único para leer, reflejar y persistir el catálogo de Torneos W2. */
export function crearEstadoCatalogoTorneos<T>(estado:EstadoCatalogoTorneos<T>){
 let restaurandoHistorial=false;
 function leerDesdeUrl(){const parametros=new URLSearchParams(location.search),portal=parametros.get('estado') as EstadoPortalTorneo|null;estado.estado=portal&&ESTADOS_PORTAL.includes(portal)?portal:'PROXIMO';estado.busqueda=parametros.get('buscar')||'';estado.rama=parametros.get('rama')||'';estado.formato=parametros.get('formato')||'';estado.soloInscripcion=parametros.get('inscripcion')==='abierta';estado.visibles=9}
 function reflejarEnControles(){const buscar=document.getElementById('buscarTorneo') as HTMLInputElement|null,rama=document.getElementById('filtroRama') as HTMLSelectElement|null,formato=document.getElementById('filtroFormato') as HTMLSelectElement|null,inscripcion=document.getElementById('filtroInscripcion') as HTMLInputElement|null;if(buscar)buscar.value=estado.busqueda;if(rama)rama.value=estado.rama;if(formato)formato.value=estado.formato;if(inscripcion)inscripcion.checked=estado.soloInscripcion;[rama,formato].forEach(select=>select?.dispatchEvent(new Event('change')))}
 function escribirEnUrl(modo:ModoHistorialTorneo='replace'){if(restaurandoHistorial||new URLSearchParams(location.search).has('torneo'))return;const url=new URL(location.href);estado.estado==='PROXIMO'?url.searchParams.delete('estado'):url.searchParams.set('estado',estado.estado);estado.busqueda?url.searchParams.set('buscar',estado.busqueda):url.searchParams.delete('buscar');estado.rama?url.searchParams.set('rama',estado.rama):url.searchParams.delete('rama');estado.formato?url.searchParams.set('formato',estado.formato):url.searchParams.delete('formato');estado.soloInscripcion?url.searchParams.set('inscripcion','abierta'):url.searchParams.delete('inscripcion');history[modo==='push'?'pushState':'replaceState']({nivel:'catalogo',estado:estado.estado},'',url)}
 function restaurarHistorial(accion:()=>void){restaurandoHistorial=true;try{accion()}finally{restaurandoHistorial=false}}
 return {leerDesdeUrl,reflejarEnControles,escribirEnUrl,restaurarHistorial};
}
