const API=`http://${window.location.hostname}:8080/api/publica`;
const STORAGE="padel.solicitud.codigo";
const REQUEST_TIMEOUT_MS=15000;
let creandoSolicitud=false;
const state={complex:null,courts:[],court:null,date:null,time:null,availability:null,created:null,timer:null};
const $=id=>document.getElementById(id);
const money=(v,c="ARS")=>new Intl.NumberFormat("es-AR",{style:"currency",currency:c}).format(Number(v||0));
const dateAr=v=>new Intl.DateTimeFormat("es-AR",{dateStyle:"full"}).format(new Date(v+"T12:00:00"));
const safe=v=>String(v??"").replace(/[&<>'"]/g,c=>({"&":"&amp;","<":"&lt;",">":"&gt;","'":"&#39;",'"':"&quot;"}[c]));

async function api(path,options={}){
  const controller=new AbortController();
  const timeout=setTimeout(()=>controller.abort(),REQUEST_TIMEOUT_MS);
  try{
    const response=await fetch(API+path,{
      headers:{"Content-Type":"application/json",...(options.headers||{})},
      ...options,
      signal:controller.signal
    });
    let data;
    try{data=await response.json()}catch{data={mensaje:"El servidor devolviÃ³ una respuesta invÃ¡lida."}}
    if(!response.ok){
      const error=new Error(data.mensaje||"No se pudo completar la operaciÃ³n.");
      error.codigo=data.codigo;
      error.operacionId=data.operacionId||response.headers.get("X-Operacion-Id");
      error.estado=response.status;
      throw error;
    }
    return data;
  }catch(error){
    if(error.name==="AbortError"){
      throw new Error("La operaciÃ³n demorÃ³ demasiado. VerificÃ¡ la conexiÃ³n e intentÃ¡ nuevamente.");
    }
    throw error;
  }finally{
    clearTimeout(timeout);
  }
}
function message(text){$("message").textContent=text;$("message").classList.toggle("hidden",!text)}
function showStep(n){document.querySelectorAll(".panel").forEach((p,i)=>p.classList.toggle("hidden",i!==n-1));document.querySelectorAll(".steps span").forEach((s,i)=>s.classList.toggle("active",i<=n-1));message("");location.hash="reservar"}

async function init(){const now=new Date();now.setMinutes(now.getMinutes()-now.getTimezoneOffset());$("date").min=now.toISOString().slice(0,10);$("date").value=new Date(now.getTime()+86400000).toISOString().slice(0,10);try{const [complex,courts]=await Promise.all([api("/complejo"),api("/canchas")]);state.complex=complex;state.courts=courts;document.documentElement.style.setProperty("--p",complex.colorPrincipal||"#486b86");$("brandName").textContent=complex.nombreComercial;$("footerName").textContent=complex.nombreComercial;$("footerContact").textContent=[complex.direccion,complex.whatsapp].filter(Boolean).join(" · ");document.title=`Reservas | ${complex.nombreComercial}`;$("court").innerHTML='<option value="">Seleccionar cancha</option>'+courts.map(c=>`<option value="${c.id}">${safe(c.nombre)}</option>`).join("");$("apiStatus").textContent="Disponibilidad online";$("court").addEventListener("change",courtChanged);const saved=localStorage.getItem(STORAGE);if(saved)$("trackingCode").value=saved}catch(e){$("apiStatus").textContent="Servicio no disponible";$("apiStatus").classList.add("offline");message("No pudimos conectarnos con el sistema. Verificá que la aplicación administrativa esté abierta.")}}
function courtChanged(){state.court=state.courts.find(c=>String(c.id)===$("court").value)||null;if(!state.court){$("courtInfo").textContent="Seleccioná una cancha para ver precio, duración y seña.";return}const c=state.court;$("courtInfo").innerHTML=`<strong>${safe(c.nombre)}</strong> · ${c.duracionMinutos} minutos · ${money(c.precio,state.complex.moneda)} · Seña ${money(c.importeSenia,state.complex.moneda)}`}

$("availability").addEventListener("click",async()=>{state.date=$("date").value;if(!state.court||!state.date)return message("Seleccioná una cancha y una fecha.");const b=$("availability");b.disabled=true;b.textContent="Consultando...";try{state.availability=await api(`/disponibilidad?canchaId=${state.court.id}&fecha=${state.date}`);renderTimes();showStep(2)}catch(e){message(e.message)}finally{b.disabled=false;b.textContent="Consultar horarios"}});
function renderTimes(){const list=state.availability.horarios||[];$("timesTitle").textContent=`${state.court.nombre} · ${dateAr(state.date)}`;$("times").innerHTML=list.map(t=>`<button class="time" data-time="${t.horaInicio}">${t.horaInicio.slice(0,5)} a ${t.horaFin.slice(0,5)}</button>`).join("");$("emptyTimes").classList.toggle("hidden",list.length>0);document.querySelectorAll(".time").forEach(b=>b.addEventListener("click",()=>selectTime(b.dataset.time)))}
function selectTime(value){state.time=state.availability.horarios.find(t=>t.horaInicio===value);$("selected").innerHTML=`<strong>${safe(state.court.nombre)}</strong><br>${dateAr(state.date)} · ${state.time.horaInicio.slice(0,5)} a ${state.time.horaFin.slice(0,5)}<br>Precio ${money(state.time.precio,state.complex.moneda)} · Seña ${money(state.time.importeSenia,state.complex.moneda)}`;showStep(3)}
document.querySelectorAll("[data-back]").forEach(b=>b.addEventListener("click",()=>showStep(Number(b.dataset.back))));

$("form").addEventListener("submit",async e=>{if(creandoSolicitud)return;creandoSolicitud=true;e.preventDefault();if(!state.time)return message("La selección del turno está incompleta.");const payload={canchaId:state.court.id,fecha:state.date,horaInicio:state.time.horaInicio,cantidadJugadores:Number($("players").value),cliente:{nombre:$("name").value.trim(),apellido:$("lastname").value.trim(),documento:$("document").value.trim(),telefono:$("phone").value.trim(),email:$("email").value.trim()||null},comentarios:$("comments").value.trim()||null};const b=$("submit");b.disabled=true;b.textContent="Creando solicitud...";try{const result=await api("/solicitudes",{method:"POST",body:JSON.stringify(payload)});state.created=result;localStorage.setItem(STORAGE,result.codigoSeguimiento);renderConfirmation(result);showStep(4)}catch(err){message(err.message)}finally{creandoSolicitud=false;b.disabled=false;b.textContent="Solicitar reserva"}});
function renderConfirmation(r){$("confirmation").innerHTML=`<strong>Solicitud #${r.solicitudId}</strong><br>${safe(r.cancha)} · ${dateAr(r.fecha)} · ${r.horaInicio.slice(0,5)} a ${r.horaFin.slice(0,5)}<br>Precio ${money(r.precioTotal,r.moneda)} · <strong>Seña ${money(r.importeSenia,r.moneda)}</strong><br><small>Código de seguimiento</small><br><code>${safe(r.codigoSeguimiento)}</code>`;$("instructions").textContent=state.complex.whatsapp?`Para informar el pago, comunicate por WhatsApp al ${state.complex.whatsapp}. Guardamos el código en este navegador.`:"Guardá el código para consultar el estado más adelante.";startTimer(r.vencimiento)}
function startTimer(value){clearInterval(state.timer);const end=new Date(value);const tick=()=>{const diff=end-Date.now();if(diff<=0){$("timer").textContent="EXPIRADA";clearInterval(state.timer);return}const m=Math.floor(diff/60000),s=Math.floor(diff%60000/1000);$("timer").textContent=`${String(m).padStart(2,"0")}:${String(s).padStart(2,"0")}`};tick();state.timer=setInterval(tick,1000)}
$("newRequest").addEventListener("click",()=>{clearInterval(state.timer);state.time=null;state.availability=null;$("form").reset();$("court").value="";courtChanged();showStep(1)});
$("viewCreated").addEventListener("click",()=>openTracking(state.created?.codigoSeguimiento));

function openTracking(code){$("trackingModal").classList.remove("hidden");if(code)$("trackingCode").value=code;$("trackingResult").classList.add("hidden");$("trackingError").classList.add("hidden");if(code)searchTracking()}
function closeTracking(){$("trackingModal").classList.add("hidden")}
$("openTracking").addEventListener("click",()=>openTracking(localStorage.getItem(STORAGE)));
$("closeTracking").addEventListener("click",closeTracking);
$("trackingModal").addEventListener("click",e=>{if(e.target===$("trackingModal"))closeTracking()});
$("searchTracking").addEventListener("click",searchTracking);
async function searchTracking(){const code=$("trackingCode").value.trim();if(!code)return trackingError("Ingresá el código de seguimiento.");const b=$("searchTracking");b.disabled=true;b.textContent="Consultando...";try{const r=await api(`/solicitudes/${encodeURIComponent(code)}`);localStorage.setItem(STORAGE,r.codigoSeguimiento);renderTracking(r);$("trackingError").classList.add("hidden")}catch(e){trackingError(e.message);$("trackingResult").classList.add("hidden")}finally{b.disabled=false;b.textContent="Consultar estado"}}
function trackingError(text){$("trackingError").textContent=text;$("trackingError").classList.remove("hidden")}
function renderTracking(r){const result=$("trackingResult");result.innerHTML=`<span class="state-badge">${safe(r.estado)}</span><h3>Solicitud #${r.solicitudId}</h3><p class="muted">${safe(r.mensaje)}</p><div class="tracking-grid"><div class="tracking-item"><small>Cancha</small><strong>${safe(r.cancha)}</strong></div><div class="tracking-item"><small>Turno</small><strong>${dateAr(r.fecha)} · ${r.horaInicio.slice(0,5)}</strong></div><div class="tracking-item"><small>Precio</small><strong>${money(r.precioTotal,r.moneda)}</strong></div><div class="tracking-item"><small>Acreditado</small><strong>${money(r.totalAcreditado,r.moneda)}</strong></div><div class="tracking-item"><small>Seña requerida</small><strong>${money(r.importeSenia,r.moneda)}</strong></div><div class="tracking-item"><small>Saldo</small><strong>${money(r.saldoPendiente,r.moneda)}</strong></div></div>`;result.classList.remove("hidden")}
init();

function paymentHtml(amount, currency) {
  if (!state.complex || !state.complex.pagoTransferenciaDisponible) return "";
  return `<h3>Datos para acreditar la seña</h3>
    <div class="tracking-grid">
      <div class="tracking-item"><small>Importe</small><strong>${money(amount,currency)}</strong></div>
      <div class="tracking-item"><small>Alias</small><strong>${safe(state.complex.pagoAlias)}</strong></div>
      <div class="tracking-item"><small>Titular</small><strong>${safe(state.complex.pagoTitular||"-")}</strong></div>
      <div class="tracking-item"><small>Entidad</small><strong>${safe(state.complex.pagoEntidad||"-")}</strong></div>
    </div>${state.complex.pagoInstrucciones?`<p class="muted">${safe(state.complex.pagoInstrucciones)}</p>`:""}`;
}
function whatsappUrl(id, cancha, fecha, hora, amount, currency) {
  if (!state.complex?.whatsapp) return null;
  const phone=state.complex.whatsapp.replace(/\D/g,"");
  const text=`Hola, informo el pago de la seña. Solicitud #${id}. ${cancha}, ${fecha}, ${hora}. Importe: ${money(amount,currency)}.`;
  return `https://wa.me/${phone}?text=${encodeURIComponent(text)}`;
}
const originalRenderConfirmation=renderConfirmation;
renderConfirmation=function(r){
  originalRenderConfirmation(r);
  const box=$("paymentBox"); const html=paymentHtml(r.importeSenia,r.moneda);
  box.innerHTML=html; box.classList.toggle("hidden",!html);
  const link=$("whatsappPayment"); const url=whatsappUrl(r.solicitudId,r.cancha,r.fecha,r.horaInicio.slice(0,5),r.importeSenia,r.moneda);
  if(url){link.href=url;link.classList.remove("hidden")}else link.classList.add("hidden");
};
const originalRenderTracking=renderTracking;
renderTracking=function(r){
  originalRenderTracking(r);
  const result=$("trackingResult"); const html=paymentHtml(r.importeSenia,r.moneda);
  if(html && r.pendiente) result.insertAdjacentHTML("beforeend",`<div class="summary left">${html}</div>`);
  const link=$("trackingWhatsapp"); const url=r.pendiente?whatsappUrl(r.solicitudId,r.cancha,r.fecha,r.horaInicio.slice(0,5),r.importeSenia,r.moneda):null;
  if(url){link.href=url;link.classList.remove("hidden")}else link.classList.add("hidden");
};