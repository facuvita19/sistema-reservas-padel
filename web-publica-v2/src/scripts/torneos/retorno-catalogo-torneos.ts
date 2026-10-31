// T2B1B2 - retorno compatible con el contrato visible del catalogo
export type RetornoCatalogo={estadoUrl:string,scrollY:number,torneoId:number,creado:number};
const CLAVE='w2-torneos-retorno';const VIGENCIA_MS=30*60*1000;let retornoActual:RetornoCatalogo|null=null;
function urlCatalogoActual(){const url=new URL(location.href);['torneo','seccion','categoria','vista','grupo','instancia','ronda'].forEach(p=>url.searchParams.delete(p));return url.pathname+url.search}
function valido(r:RetornoCatalogo|null){return Boolean(r&&Date.now()-r.creado<=VIGENCIA_MS)}
function leerRetorno(){if(valido(retornoActual))return retornoActual;try{const r=JSON.parse(sessionStorage.getItem(CLAVE)||'null') as RetornoCatalogo|null;if(valido(r)){retornoActual=r;return r}}catch{}return null}
function limpiarRetorno(){retornoActual=null;try{sessionStorage.removeItem(CLAVE)}catch{}}
function altoCabecera(){return document.querySelector<HTMLElement>('[data-w2-header],header[data-site-header]')?.getBoundingClientRect().height||0}
function tarjeta(id:number){return document.querySelector<HTMLElement>('[data-torneo-id="'+id+'"],.catalog-open[data-torneo-id="'+id+'"]')?.closest<HTMLElement>('.w2t-card')||null}
export function capturarRetornoCatalogo(boton:HTMLButtonElement){retornoActual={estadoUrl:urlCatalogoActual(),scrollY,torneoId:Number(boton.dataset.torneoId)||0,creado:Date.now()};try{sessionStorage.setItem(CLAVE,JSON.stringify(retornoActual))}catch{}}
export function restaurarFocoCatalogo(){const r=leerRetorno(),control=r?tarjeta(r.torneoId)?.querySelector<HTMLButtonElement>('.catalog-open'):null;requestAnimationFrame(()=>control?.focus({preventScroll:true}))}
export function restaurarRetornoCatalogo(){requestAnimationFrame(()=>requestAnimationFrame(()=>{const r=leerRetorno(),ancla=document.getElementById('pestanasTorneos')||document.getElementById('estadoCatalogoTorneos')||document.getElementById('listaTorneos');if(ancla){const destino=Math.max(0,ancla.getBoundingClientRect().top+scrollY-altoCabecera()-12);scrollTo({top:destino,behavior:'auto'});if(r)restaurarFocoCatalogo()}else scrollTo({top:0,behavior:'auto'});limpiarRetorno()}))}
