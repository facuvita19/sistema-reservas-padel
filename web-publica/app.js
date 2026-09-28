const API=`http://${window.location.hostname}:8080/api/publica`;
const STORAGE="padel.solicitud.codigo";
const REQUEST_TIMEOUT_MS=15000;
let creandoSolicitud=false;
let toastTimer=null;
const state={complex:null,courts:[],court:null,date:null,time:null,availability:null,created:null,timer:null};
const $=id=>document.getElementById(id);
const money=(v,c="ARS")=>new Intl.NumberFormat("es-AR",{style:"currency",currency:c}).format(Number(v||0));
const dateAr=v=>new Intl.DateTimeFormat("es-AR",{dateStyle:"full"}).format(new Date(v+"T12:00:00"));
const safe=v=>String(v??"").replace(/[&<>'"]/g,c=>({"&":"&amp;","<":"&lt;",">":"&gt;","'":"&#39;",'"':"&quot;"}[c]));

async function api(path,options={}){
  const controller=new AbortController();
  const timeout=setTimeout(()=>controller.abort(),REQUEST_TIMEOUT_MS);
  try{
    const response=await fetch(API+path,{headers:{"Content-Type":"application/json",...(options.headers||{})},...options,signal:controller.signal});
    let data;
    try{data=await response.json()}catch{data={mensaje:"El servidor devolvió una respuesta inválida."}}
    if(!response.ok){
      const error=new Error(data.mensaje||"No se pudo completar la operación.");
      error.codigo=data.codigo;error.operacionId=data.operacionId||response.headers.get("X-Operacion-Id");error.estado=response.status;throw error;
    }
    return data;
  }catch(error){
    if(error.name==="AbortError")throw new Error("La operación demoró demasiado. Verificá la conexión e intentá nuevamente.");
    throw error;
  }finally{clearTimeout(timeout)}
}
function message(text){$("message").textContent=text;$("message").classList.toggle("hidden",!text);if(text)$("message").scrollIntoView({behavior:"smooth",block:"center"})}
function toast(text){clearTimeout(toastTimer);$("toast").textContent=text;$("toast").classList.remove("hidden");toastTimer=setTimeout(()=>$("toast").classList.add("hidden"),2200)}
function showStep(n){document.querySelectorAll(".panel").forEach((p,i)=>p.classList.toggle("hidden",i!==n-1));document.querySelectorAll("[data-step-indicator]").forEach((s,i)=>s.classList.toggle("active",i<=n-1));message("");if(location.hash!=="#reservar")location.hash="reservar";else $("reservar").scrollIntoView({behavior:"smooth",block:"start"})}
function hexToRgb(hex){const clean=String(hex||"").replace("#","");if(!/^[0-9a-f]{6}$/i.test(clean))return "72,107,134";return `${parseInt(clean.slice(0,2),16)},${parseInt(clean.slice(2,4),16)},${parseInt(clean.slice(4,6),16)}`}

async function init(){
  const now=new Date();now.setMinutes(now.getMinutes()-now.getTimezoneOffset());
  $("date").min=now.toISOString().slice(0,10);$("date").value=new Date(now.getTime()+86400000).toISOString().slice(0,10);
  try{
    const [complex,courts]=await Promise.all([api("/complejo"),api("/canchas")]);
    state.complex=complex;state.courts=courts;
    const color=complex.colorPrincipal||"#486b86";document.documentElement.style.setProperty("--p",color);document.documentElement.style.setProperty("--p-rgb",hexToRgb(color));
    $("brandName").textContent=complex.nombreComercial;$("footerName").textContent=complex.nombreComercial;$("footerContact").textContent=[complex.direccion,complex.whatsapp].filter(Boolean).join(" · ");document.title=`Reservas | ${complex.nombreComercial}`;
    $("court").innerHTML='<option value="">Seleccionar cancha</option>'+courts.map(c=>`<option value="${c.id}">${safe(c.nombre)}</option>`).join("");
    $("apiStatus").innerHTML='<i></i> Disponibilidad online';$("apiStatus").classList.remove("offline");$("court").addEventListener("change",courtChanged);
    const saved=localStorage.getItem(STORAGE);if(saved)$("trackingCode").value=saved;
  }catch(e){$("apiStatus").innerHTML='<i></i> Servicio no disponible';$("apiStatus").classList.add("offline");$("court").innerHTML='<option value="">No se pudieron cargar las canchas</option>';message("No pudimos conectarnos con el sistema. Verificá que la API esté en funcionamiento.")}
}
function courtChanged(){
  state.court=state.courts.find(c=>String(c.id)===$("court").value)||null;
  if(!state.court){$("courtInfo").innerHTML='<span class="detail-icon">⌁</span><p>Seleccioná una cancha para ver precio, duración y seña.</p>';return}
  const c=state.court;
  $("courtInfo").innerHTML=`<div class="detail-grid"><div><small>Cancha</small><strong>${safe(c.nombre)}</strong></div><div><small>Duración</small><strong>${c.duracionMinutos} minutos</strong></div><div><small>Precio</small><strong>${money(c.precio,state.complex.moneda)}</strong></div><div><small>Seña</small><strong>${money(c.importeSenia,state.complex.moneda)}</strong></div></div>`;
}

$("availability").addEventListener("click",async()=>{
  state.date=$("date").value;if(!state.court||!state.date)return message("Seleccioná una cancha y una fecha para continuar.");
  const b=$("availability");b.disabled=true;b.querySelector("span").textContent="Consultando...";
  try{state.availability=await api(`/disponibilidad?canchaId=${state.court.id}&fecha=${state.date}`);renderTimes();showStep(2)}catch(e){message(e.message)}finally{b.disabled=false;b.querySelector("span").textContent="Consultar horarios"}
});
function timeGroup(hour){const h=Number(hour.slice(0,2));return h<12?"Mañana":h<18?"Tarde":"Noche"}
function renderTimes(){
  const list=state.availability.horarios||[];$("timesTitle").textContent=`${state.court.nombre} · ${dateAr(state.date)}`;
  let last="";$("times").innerHTML=list.map(t=>{const group=timeGroup(t.horaInicio);const header=group!==last?`<div class="time-group">${group}</div>`:"";last=group;return `${header}<button class="time" type="button" data-time="${t.horaInicio}"><small>Disponible</small>${t.horaInicio.slice(0,5)} a ${t.horaFin.slice(0,5)}</button>`}).join("");
  $("emptyTimes").classList.toggle("hidden",list.length>0);document.querySelectorAll(".time").forEach(b=>b.addEventListener("click",()=>selectTime(b.dataset.time)));
}
function selectTime(value){
  state.time=state.availability.horarios.find(t=>t.horaInicio===value);document.querySelectorAll(".time").forEach(b=>b.classList.toggle("selected",b.dataset.time===value));
  $("selected").innerHTML=`<div><small>Cancha</small><strong>${safe(state.court.nombre)}</strong></div><div><small>Fecha</small><strong>${dateAr(state.date)}</strong></div><div><small>Horario</small><strong>${state.time.horaInicio.slice(0,5)} a ${state.time.horaFin.slice(0,5)}</strong></div><div><small>Seña</small><strong>${money(state.time.importeSenia,state.complex.moneda)}</strong></div>`;showStep(3);
}
document.querySelectorAll("[data-back]").forEach(b=>b.addEventListener("click",()=>showStep(Number(b.dataset.back))));

function fieldError(input,text){const field=input.closest(".field");field?.classList.toggle("invalid",Boolean(text));const error=field?.querySelector(".field-error");if(error)error.textContent=text||""}
function validateForm(){
  let valid=true;const checks=[
    [$("name"),v=>v.length?"":"Ingresá tu nombre."],[$("lastname"),v=>v.length?"":"Ingresá tu apellido."],
    [$("document"),v=>/^\d{7,10}$/.test(v)?"":"Ingresá entre 7 y 10 números."],[$("phone"),v=>/^[0-9+()\-\s]{6,30}$/.test(v)?"":"Ingresá un teléfono válido."],
    [$("email"),v=>!v||/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v)?"":"Ingresá un correo válido."]];
  checks.forEach(([input,fn])=>{const error=fn(input.value.trim());fieldError(input,error);if(error)valid=false});
  const terms=$("form").querySelector('.terms input');if(!terms.checked){message("Aceptá la condición de reserva para continuar.");valid=false}
  return valid;
}
["name","lastname","document","phone","email"].forEach(id=>$(id).addEventListener("input",()=>fieldError($(id),"")));
$("form").addEventListener("submit",async e=>{
  e.preventDefault();if(creandoSolicitud)return;if(!state.time)return message("La selección del turno está incompleta.");if(!validateForm())return;
  creandoSolicitud=true;const payload={canchaId:state.court.id,fecha:state.date,horaInicio:state.time.horaInicio,cantidadJugadores:Number($("players").value),cliente:{nombre:$("name").value.trim(),apellido:$("lastname").value.trim(),documento:$("document").value.trim(),telefono:$("phone").value.trim(),email:$("email").value.trim()||null},comentarios:$("comments").value.trim()||null};
  const b=$("submit");b.disabled=true;b.querySelector("span").textContent="Creando solicitud...";
  try{const result=await api("/solicitudes",{method:"POST",body:JSON.stringify(payload)});state.created=result;localStorage.setItem(STORAGE,result.codigoSeguimiento);renderConfirmation(result);showStep(4)}catch(err){message(err.message)}finally{creandoSolicitud=false;b.disabled=false;b.querySelector("span").textContent="Solicitar reserva"}
});
function renderConfirmation(r){
  $("confirmation").innerHTML=`<div class="tracking-grid"><div class="tracking-item"><small>Solicitud</small><strong>#${r.solicitudId}</strong></div><div class="tracking-item"><small>Cancha</small><strong>${safe(r.cancha)}</strong></div><div class="tracking-item"><small>Fecha y horario</small><strong>${dateAr(r.fecha)} · ${r.horaInicio.slice(0,5)} a ${r.horaFin.slice(0,5)}</strong></div><div class="tracking-item"><small>Precio y seña</small><strong>${money(r.precioTotal,r.moneda)} · ${money(r.importeSenia,r.moneda)}</strong></div></div><div class="copy-row"><div><small>Código de seguimiento</small><code>${safe(r.codigoSeguimiento)}</code></div><button class="copy-button" type="button" data-copy="${safe(r.codigoSeguimiento)}">Copiar código</button></div>`;
  bindCopyButtons();$("instructions").textContent=state.complex.whatsapp?`Podés informar el pago por WhatsApp al ${state.complex.whatsapp}. Guardamos el código en este navegador.`:"Guardá el código para consultar el estado más adelante.";startTimer(r.vencimiento);renderPayment(r.importeSenia,r.moneda,$("paymentBox"),$("whatsappPayment"),r);
}
function startTimer(value){clearInterval(state.timer);const end=new Date(value);const tick=()=>{const diff=end-Date.now();if(diff<=0){$("timer").textContent="EXPIRADA";clearInterval(state.timer);return}const m=Math.floor(diff/60000),s=Math.floor(diff%60000/1000);$("timer").textContent=`${String(m).padStart(2,"0")}:${String(s).padStart(2,"0")}`};tick();state.timer=setInterval(tick,1000)}
$("newRequest").addEventListener("click",()=>{clearInterval(state.timer);state.time=null;state.availability=null;$("form").reset();$("court").value="";courtChanged();showStep(1)});$("viewCreated").addEventListener("click",()=>openTracking(state.created?.codigoSeguimiento));

function openTracking(code){$("trackingModal").classList.remove("hidden");document.body.classList.add("modal-open");if(code)$("trackingCode").value=code;$("trackingResult").classList.add("hidden");$("trackingError").classList.add("hidden");setTimeout(()=>$("trackingCode").focus(),50);if(code)searchTracking()}
function closeTracking(){$("trackingModal").classList.add("hidden");document.body.classList.remove("modal-open")}
$("openTracking").addEventListener("click",()=>openTracking(localStorage.getItem(STORAGE)));document.querySelectorAll("[data-open-tracking]").forEach(b=>b.addEventListener("click",()=>openTracking(localStorage.getItem(STORAGE))));$("closeTracking").addEventListener("click",closeTracking);$("trackingModal").addEventListener("click",e=>{if(e.target===$("trackingModal"))closeTracking()});document.addEventListener("keydown",e=>{if(e.key==="Escape")closeTracking()});$("trackingCode").addEventListener("keydown",e=>{if(e.key==="Enter")searchTracking()});
$("pasteTracking").addEventListener("click",async()=>{try{$("trackingCode").value=await navigator.clipboard.readText();toast("Código pegado") }catch{toast("Pegá el código manualmente")}});$("searchTracking").addEventListener("click",searchTracking);
async function searchTracking(){
  const code=$("trackingCode").value.trim();if(!code)return trackingError("Ingresá el código de seguimiento.");const b=$("searchTracking");b.disabled=true;b.textContent="Consultando...";
  try{const r=await api(`/solicitudes/${encodeURIComponent(code)}`);localStorage.setItem(STORAGE,r.codigoSeguimiento);renderTracking(r);$("trackingError").classList.add("hidden")}catch(e){trackingError(e.message);$("trackingResult").classList.add("hidden");$("trackingWhatsapp").classList.add("hidden")}finally{b.disabled=false;b.textContent="Consultar estado"}
}
function trackingError(text){$("trackingError").textContent=text;$("trackingError").classList.remove("hidden")}
function timelineHtml(r){const confirmed=r.confirmada||["COMPLETADA","AUSENTE"].includes(r.estado);const ended=["EXPIRADA","CANCELADA"].includes(r.estado);return `<div class="timeline"><div class="timeline-step done"><i></i>Solicitud</div><div class="timeline-step ${confirmed||ended?"done":""}"><i></i>${confirmed?"Confirmada":ended?r.estado:"Pendiente"}</div><div class="timeline-step ${["COMPLETADA","AUSENTE"].includes(r.estado)?"done":""}"><i></i>Turno</div></div>`}
function renderTracking(r){
  const stateClass=`state-${String(r.estado).toLowerCase()}`;const result=$("trackingResult");
  result.innerHTML=`<div class="tracking-top"><div><span class="state-badge ${stateClass}">${safe(r.estado)}</span><h3>Solicitud #${r.solicitudId}</h3></div><button class="copy-button" type="button" data-copy="${safe(r.codigoSeguimiento)}">Copiar código</button></div><p class="muted">${safe(r.mensaje)}</p>${timelineHtml(r)}<div class="tracking-grid"><div class="tracking-item"><small>Cancha</small><strong>${safe(r.cancha)}</strong></div><div class="tracking-item"><small>Turno</small><strong>${dateAr(r.fecha)} · ${r.horaInicio.slice(0,5)}</strong></div><div class="tracking-item"><small>Precio</small><strong>${money(r.precioTotal,r.moneda)}</strong></div><div class="tracking-item"><small>Acreditado</small><strong>${money(r.totalAcreditado,r.moneda)}</strong></div><div class="tracking-item"><small>Seña requerida</small><strong>${money(r.importeSenia,r.moneda)}</strong></div><div class="tracking-item"><small>Saldo</small><strong>${money(r.saldoPendiente,r.moneda)}</strong></div></div>`;
  if(r.pendiente){const box=document.createElement("div");box.className="summary left";result.appendChild(box);renderPayment(r.importeSenia,r.moneda,box,$("trackingWhatsapp"),r)}else $("trackingWhatsapp").classList.add("hidden");
  result.classList.remove("hidden");bindCopyButtons();
}
function paymentHtml(amount,currency){if(!state.complex?.pagoTransferenciaDisponible)return "";return `<h3 class="payment-title">Datos para acreditar la seña</h3><div class="tracking-grid"><div class="tracking-item"><small>Importe</small><strong>${money(amount,currency)}</strong></div><div class="tracking-item"><small>Alias</small><div class="copy-row"><strong>${safe(state.complex.pagoAlias)}</strong><button class="copy-button" type="button" data-copy="${safe(state.complex.pagoAlias)}">Copiar</button></div></div><div class="tracking-item"><small>Titular</small><strong>${safe(state.complex.pagoTitular||"-")}</strong></div><div class="tracking-item"><small>Entidad</small><strong>${safe(state.complex.pagoEntidad||"-")}</strong></div></div>${state.complex.pagoInstrucciones?`<p class="muted">${safe(state.complex.pagoInstrucciones)}</p>`:""}`}
function whatsappUrl(id,cancha,fecha,hora,amount,currency){if(!state.complex?.whatsapp)return null;const phone=state.complex.whatsapp.replace(/\D/g,"");const text=`Hola, informo el pago de la seña. Solicitud #${id}. ${cancha}, ${fecha}, ${hora}. Importe: ${money(amount,currency)}.`;return `https://wa.me/${phone}?text=${encodeURIComponent(text)}`}
function renderPayment(amount,currency,box,link,r){const html=paymentHtml(amount,currency);box.innerHTML=html;box.classList.toggle("hidden",!html);const url=whatsappUrl(r.solicitudId,r.cancha,r.fecha,r.horaInicio.slice(0,5),amount,currency);if(url){link.href=url;link.classList.remove("hidden")}else link.classList.add("hidden");bindCopyButtons()}
function bindCopyButtons(){document.querySelectorAll("[data-copy]").forEach(b=>{if(b.dataset.bound)return;b.dataset.bound="1";b.addEventListener("click",async()=>{try{await navigator.clipboard.writeText(b.dataset.copy);toast("Copiado al portapapeles")}catch{toast("No se pudo copiar")}})})}
init();
