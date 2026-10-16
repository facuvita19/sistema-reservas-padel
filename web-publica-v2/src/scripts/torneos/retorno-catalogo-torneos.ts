type RetornoCatalogo={estadoUrl:string;creado:number};

const CLAVE='w2-torneos-retorno';
const VIGENCIA_MS=30*60*1000;
let retornoActual:RetornoCatalogo|null=null;

function urlCatalogoActual(){const url=new URL(location.href);['torneo','seccion','categoria','vista','grupo','instancia','ronda'].forEach(parametro=>url.searchParams.delete(parametro));return url.pathname+url.search}
function leerRetorno(){if(retornoActual)return retornoActual;try{const guardado=JSON.parse(sessionStorage.getItem(CLAVE)||'null') as RetornoCatalogo|null;if(!guardado||Date.now()-guardado.creado>VIGENCIA_MS)return null;return guardado}catch{return null}}
function limpiarRetorno(){retornoActual=null;sessionStorage.removeItem(CLAVE)}
function altoCabecera(){return document.querySelector<HTMLElement>('[data-w2-header],header[data-site-header]')?.getBoundingClientRect().height||0}

export function capturarRetornoCatalogo(_boton:HTMLButtonElement){retornoActual={estadoUrl:urlCatalogoActual(),creado:Date.now()};try{sessionStorage.setItem(CLAVE,JSON.stringify(retornoActual))}catch{}}

export function restaurarRetornoCatalogo(){requestAnimationFrame(()=>requestAnimationFrame(()=>{leerRetorno();const ancla=document.getElementById('pestanasTorneos')||document.getElementById('estadoCatalogoTorneos')||document.getElementById('listaTorneos');if(!ancla){scrollTo({top:0,behavior:'auto'});limpiarRetorno();return}const margen=12,destino=Math.max(0,ancla.getBoundingClientRect().top+scrollY-altoCabecera()-margen);scrollTo({top:destino,behavior:'auto'});requestAnimationFrame(()=>{const activo=ancla.querySelector<HTMLButtonElement>('[aria-selected="true"],button.active');activo?.focus({preventScroll:true})});limpiarRetorno()}))}
