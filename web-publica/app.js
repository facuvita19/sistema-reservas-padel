const API=`http://${location.hostname}:8080/api/publica`,K={code:'padel.solicitud.codigo',hist:'padel.historial.v4',profile:'padel.perfil.v2',draft:'padel.borrador.v2',theme:'padel.tema.v2'};const $=id=>document.getElementById(id),state={complex:null,courts:[],court:null,date:localDate(new Date()),time:null,availability:null,created:null,step:1,timer:null,lastFocus:null};let installPrompt,toastTimer,historyTimer,waitingWorker;
function localDate(d){const x=new Date(d);x.setMinutes(x.getMinutes()-x.getTimezoneOffset());return x.toISOString().slice(0,10)}function addDays(v,n){const d=new Date(v+'T12:00:00');d.setDate(d.getDate()+n);return localDate(d)}function dateAr(v){return new Intl.DateTimeFormat('es-AR',{dateStyle:'full'}).format(new Date(v+'T12:00:00'))}function shortDate(v){return new Intl.DateTimeFormat('es-AR',{day:'2-digit',month:'short'}).format(new Date(v+'T12:00:00'))}function money(v,c='ARS'){return new Intl.NumberFormat('es-AR',{style:'currency',currency:c}).format(Number(v||0))}function esc(v){return String(v??'').replace(/[&<>'"]/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;',"'":'&#39;','"':'&quot;'}[c]))}
async function api(path,opt={}){const c=new AbortController(),t=setTimeout(()=>c.abort(),15000);try{const r=await fetch(API+path,{credentials:'include',headers:{'Content-Type':'application/json',...(opt.headers||{})},...opt,signal:c.signal});let d;try{d=await r.json()}catch{d={mensaje:'Respuesta inválida del servidor.'}}if(!r.ok)throw new Error(d.mensaje||'No se pudo completar la operación.');return d}catch(e){if(e.name==='AbortError')throw new Error('La operación demoró demasiado.');throw e}finally{clearTimeout(t)}}
function toast(t){clearTimeout(toastTimer);$('toast').textContent=t;$('toast').classList.remove('hidden');toastTimer=setTimeout(()=>$('toast').classList.add('hidden'),2500)}function msg(t){$('message').textContent=t;$('message').classList.toggle('hidden',!t)}function showStep(n){state.step=n;document.querySelectorAll('.panel').forEach((p,i)=>p.classList.toggle('hidden',i!==n-1));document.querySelectorAll('[data-step]').forEach((s,i)=>s.classList.toggle('active',i<n));msg('');const p=$(`step${n}`);p.focus({preventScroll:true});p.scrollIntoView({behavior:'smooth'})}
function theme(mode){localStorage.setItem(K.theme,mode);const effective=mode==='auto'?(matchMedia('(prefers-color-scheme:dark)').matches?'dark':'light'):mode;document.documentElement.dataset.theme=mode;document.documentElement.dataset.effectiveTheme=effective;const labels={auto:'Auto',dark:'Oscuro',light:'Claro'};const icons={auto:'Ã¢â€”Â',dark:'Ã¢ËœÂ¾',light:'Ã¢Ëœâ‚¬'};$('themeLabel').textContent=labels[mode];$('themeIcon').textContent=icons[mode];$('themeToggle').setAttribute('aria-label',`Cambiar apariencia. Tema actual: ${labels[mode]}`);$('themeToggle').title=`Tema: ${labels[mode].toLowerCase()}`;document.querySelector('meta[name=theme-color]').content=effective==='dark'?'#071018':'#f4f7f9'}
function cycleTheme(){const current=localStorage.getItem(K.theme)||'auto';const modes=['auto','dark','light'];const next=modes[(modes.indexOf(current)+1)%modes.length];theme(next);toast(`Tema: ${next==='auto'?'automatico':next}`)}
function dateButtons(el,center,count){const start=new Date(center+'T12:00:00');start.setDate(start.getDate()-Math.floor(count/2));const today=localDate(new Date());el.innerHTML=Array.from({length:count},(_,i)=>{const d=new Date(start);d.setDate(start.getDate()+i);const v=localDate(d),label=v===today?'Hoy':v===addDays(today,1)?'Mañana':new Intl.DateTimeFormat('es-AR',{weekday:'short'}).format(d);return `<button class="date-chip ${v===state.date?'selected':''}" data-date="${v}" ${v<today?'disabled':''}><strong>${esc(label)}</strong><small>${shortDate(v)}</small></button>`}).join('');el.querySelectorAll('[data-date]').forEach(b=>b.onclick=()=>{selectDate(b.dataset.date);saveDraft();if(state.step===2)loadAvailability(false)})}function selectDate(v){state.date=v;$('date').value=v;dateButtons($('quickDates'),v,13);dateButtons($('timesDates'),v,9)}
function renderCourts(){$('courtCards').innerHTML=state.courts.map(c=>`<button class="court-card" data-court="${c.id}"><h4>${esc(c.nombre)}</h4><p>${esc(c.descripcion||[c.tipo,c.superficie].filter(Boolean).join(' · ')||'Cancha disponible')}</p><div class="court-meta"><span>${esc(c.tipo||'Pádel')}</span><span>${c.duracionMinutos} min</span></div><div class="court-price"><small>Turno</small><strong>${money(c.precio,state.complex.moneda)}</strong></div></button>`).join('');document.querySelectorAll('[data-court]').forEach(b=>b.onclick=()=>selectCourt(b.dataset.court))}function selectCourt(id){state.court=state.courts.find(c=>String(c.id)===String(id))||null;document.querySelectorAll('[data-court]').forEach(b=>b.classList.toggle('selected',String(b.dataset.court)===String(id)));saveDraft()}
async function loadAvailability(next=true){if(!state.court)return msg('Seleccioná una cancha.');$('timesLoading').classList.remove('hidden');$('times').classList.add('hidden');$('emptyTimes').classList.add('hidden');try{state.availability=await api(`/disponibilidad?canchaId=${state.court.id}&fecha=${state.date}`);renderTimes();if(next)showStep(2);saveDraft()}catch(e){msg(e.message)}finally{$('timesLoading').classList.add('hidden');$('times').classList.remove('hidden')}}function group(h){const x=+h.slice(0,2);return x<12?'Mañana':x<18?'Tarde':'Noche'}function renderTimes(){const list=state.availability?.horarios||[];$('timesTitle').textContent=`${state.court.nombre} · ${dateAr(state.date)}`;$('availableCount').textContent=`${list.length} turnos disponibles`;let last='';$('times').innerHTML=list.map(t=>{const g=group(t.horaInicio),h=g!==last?`<div class="time-group">${g}</div>`:'';last=g;return `${h}<button class="time" data-time="${t.horaInicio}"><small>Disponible</small>${t.horaInicio.slice(0,5)} a ${t.horaFin.slice(0,5)}<em>${money(t.precio,state.complex.moneda)}</em></button>`}).join('');$('emptyTimes').classList.toggle('hidden',!!list.length);document.querySelectorAll('[data-time]').forEach(b=>b.onclick=()=>selectTime(b.dataset.time))}function selectTime(v){state.time=state.availability.horarios.find(t=>t.horaInicio===v);$('selected').innerHTML=`<div class="summary-grid"><div><small>Cancha</small><strong>${esc(state.court.nombre)}</strong></div><div><small>Fecha</small><strong>${dateAr(state.date)}</strong></div><div><small>Horario</small><strong>${state.time.horaInicio.slice(0,5)} a ${state.time.horaFin.slice(0,5)}</strong></div><div><small>Seña</small><strong>${money(state.time.importeSenia,state.complex.moneda)}</strong></div></div>`;showStep(3);saveDraft()}
function profile(){try{return JSON.parse(localStorage.getItem(K.profile)||'null')}catch{return null}}function restoreProfile(){const p=profile();if(!p||Date.now()-p.saved>30*864e5)return;['name','lastname','phone','email','players'].forEach(k=>{if(p[k]!=null)$(k).value=p[k]});$('rememberData').checked=true}function storeProfile(){if(!$('rememberData').checked)return localStorage.removeItem(K.profile);const p={saved:Date.now()};['name','lastname','phone','email','players'].forEach(k=>p[k]=$(k).value);localStorage.setItem(K.profile,JSON.stringify(p))}
function draft(){try{return JSON.parse(localStorage.getItem(K.draft)||'null')}catch{return null}}function saveDraft(){if(state.step===4)return;const d={saved:Date.now(),step:state.step,courtId:state.court?.id,date:state.date,time:state.time?.horaInicio,form:{}};['name','lastname','phone','email','players','comments'].forEach(k=>d.form[k]=$(k).value);localStorage.setItem(K.draft,JSON.stringify(d))}function clearDraft(){localStorage.removeItem(K.draft);$('draftNotice').classList.add('hidden')}async function restoreDraft(){const d=draft();if(!d)return;if(d.courtId)selectCourt(d.courtId);if(d.date)selectDate(d.date);Object.entries(d.form||{}).forEach(([k,v])=>{if($(k))$(k).value=v});if(d.step>=2&&state.court){await loadAvailability(true);if(d.time&&d.step>=3){const found=state.availability.horarios.find(t=>t.horaInicio===d.time);if(found)selectTime(d.time)}}toast('Progreso recuperado')}
function fieldError(i,t){const f=i.closest('.field');f.classList.toggle('invalid',!!t);f.querySelector('small').textContent=t||''}function validate(){let ok=true;const checks=[[$('name'),v=>v?'':'Ingresá tu nombre.'],[$('lastname'),v=>v?'':'Ingresá tu apellido.'],[$('document'),v=>/^\d{7,10}$/.test(v)?'':'Ingresá entre 7 y 10 números.'],[$('phone'),v=>/^[0-9+()\-\s]{6,30}$/.test(v)?'':'Ingresá un teléfono válido.'],[$('email'),v=>!v||/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v)?'':'Ingresá un correo válido.']];checks.forEach(([i,fn])=>{const e=fn(i.value.trim());fieldError(i,e);if(e)ok=false});if(!$('terms').checked){msg('Aceptá la condición de reserva.');ok=false}return ok}
async function submit(e){e.preventDefault();if(!state.time||!validate())return;const b=$('submit');b.disabled=true;b.querySelector('span').textContent='Validando turno...';try{const fresh=await api(`/disponibilidad?canchaId=${state.court.id}&fecha=${state.date}`);if(!(fresh.horarios||[]).some(t=>t.horaInicio===state.time.horaInicio))throw new Error('El horario acaba de ser ocupado. Elegí otro.');storeProfile();b.querySelector('span').textContent='Creando solicitud...';const payload={canchaId:state.court.id,fecha:state.date,horaInicio:state.time.horaInicio,cantidadJugadores:+$('players').value,cliente:{nombre:$('name').value.trim(),apellido:$('lastname').value.trim(),documento:$('document').value.trim(),telefono:$('phone').value.trim(),email:$('email').value.trim()||null},comentarios:$('comments').value.trim()||null};const r=await api('/solicitudes',{method:'POST',body:JSON.stringify(payload)});state.created=r;localStorage.setItem(K.code,r.codigoSeguimiento);upsert(r);clearDraft();renderConfirmation(r);showStep(4)}catch(x){msg(x.message);if(x.message.includes('ocupado'))loadAvailability(true)}finally{b.disabled=false;b.querySelector('span').textContent='Solicitar reserva'}}
function renderConfirmation(r){$('confirmation').innerHTML=`<div class="receipt-grid"><div><small>Solicitud</small><strong>#${r.solicitudId}</strong></div><div><small>Cancha</small><strong>${esc(r.cancha)}</strong></div><div><small>Turno</small><strong>${dateAr(r.fecha)} · ${r.horaInicio.slice(0,5)}</strong></div><div><small>Total / seña</small><strong>${money(r.precioTotal,r.moneda)} / ${money(r.importeSenia,r.moneda)}</strong></div></div><div class="tracking-code"><div><small>Codigo de seguimiento</small><code>${esc(r.codigoSeguimiento)}</code></div><button class="compact-copy" type="button" data-copy="${esc(r.codigoSeguimiento)}">Copiar</button></div>`;bindCopy();startTimer(r.vencimiento);renderPayment(r,$('paymentBox'),$('whatsappPayment'))}function startTimer(v){clearInterval(state.timer);const end=new Date(v);const tick=()=>{const d=end-Date.now();if(d<=0){$('timer').textContent='EXPIRADA';return clearInterval(state.timer)}$('timer').textContent=`${String(Math.floor(d/6e4)).padStart(2,'0')}:${String(Math.floor(d%6e4/1e3)).padStart(2,'0')}`};tick();state.timer=setInterval(tick,1e3)}function payment(r){if(!state.complex?.pagoTransferenciaDisponible)return'';return `<div class="tracking-grid"><div class="tracking-item"><small>Importe</small><strong>${money(r.importeSenia,r.moneda)}</strong></div><div class="tracking-item"><small>Alias</small><div class="alias-copy"><strong>${esc(state.complex.pagoAlias)}</strong><button class="compact-copy" type="button" data-copy="${esc(state.complex.pagoAlias)}">Copiar</button></div></div><div class="tracking-item"><small>Titular</small><strong>${esc(state.complex.pagoTitular||'-')}</strong></div><div class="tracking-item"><small>Entidad</small><strong>${esc(state.complex.pagoEntidad||'-')}</strong></div></div>`}function wa(r){if(!state.complex?.whatsapp)return null;const p=state.complex.whatsapp.replace(/\D/g,''),t=`Hola, informo el pago de la seña. Solicitud #${r.solicitudId}. ${r.cancha}, ${r.fecha}, ${r.horaInicio.slice(0,5)}. Importe: ${money(r.importeSenia,r.moneda)}.`;return `https://wa.me/${p}?text=${encodeURIComponent(t)}`}function renderPayment(r,box,link){const h=payment(r);box.innerHTML=h;box.classList.toggle('hidden',!h);const u=wa(r);if(u){link.href=u;link.classList.remove('hidden')}else link.classList.add('hidden');bindCopy()}
function hist(){try{return JSON.parse(localStorage.getItem(K.hist)||'[]')}catch{return[]}}function item(r){return{codigoSeguimiento:r.codigoSeguimiento,solicitudId:r.solicitudId,estado:r.estado||'PENDIENTE',cancha:r.cancha,fecha:r.fecha,horaInicio:r.horaInicio,horaFin:r.horaFin,precioTotal:r.precioTotal,importeSenia:r.importeSenia,moneda:r.moneda,vencimiento:r.vencimiento,pendiente:r.pendiente}}function saveHist(a){localStorage.setItem(K.hist,JSON.stringify(a.slice(0,15)));renderRecent()}function upsert(r){saveHist([item(r),...hist().filter(x=>x.codigoSeguimiento!==r.codigoSeguimiento)])}function past(x){return new Date(`${x.fecha}T${x.horaFin||x.horaInicio}`)<new Date()||['EXPIRADA','CANCELADA','COMPLETADA','AUSENTE'].includes(x.estado)}function relative(x){if(x.estado==='PENDIENTE'&&x.vencimiento){const d=new Date(x.vencimiento)-Date.now();if(d>0)return`Vence en ${Math.ceil(d/6e4)} min`}const d=new Date(`${x.fecha}T${x.horaInicio}`)-Date.now();return d>0?(d<864e5?`Faltan ${Math.ceil(d/36e5)} h`:`Faltan ${Math.ceil(d/864e5)} días`):'Turno anterior'}function historyCard(x,full=false){return `<article class="${full?'history-item':'booking-mini'}" ${full?'':`data-open="${esc(x.codigoSeguimiento)}"`}><div class="booking-top"><span class="badge state-${String(x.estado).toLowerCase()}">${esc(x.estado)}</span><small>#${x.solicitudId||'-'}</small></div><h3>${esc(x.cancha)}</h3><p>${dateAr(x.fecha)} · ${x.horaInicio.slice(0,5)}</p><div class="history-note">${relative(x)}</div>${full?`<div class="history-actions"><button class="button secondary" data-open="${esc(x.codigoSeguimiento)}">Ver</button><button class="button secondary" data-copy="${esc(x.codigoSeguimiento)}">Copiar</button><button class="button secondary" data-forget="${esc(x.codigoSeguimiento)}">Quitar</button></div>`:''}</article>`}function bindHistory(root){root.querySelectorAll('[data-open]').forEach(b=>b.onclick=()=>{closeModal('historyModal');openTracking(b.dataset.open)});root.querySelectorAll('[data-forget]').forEach(b=>b.onclick=()=>{saveHist(hist().filter(x=>x.codigoSeguimiento!==b.dataset.forget));renderHistory()});bindCopy()}function renderRecent(){const a=hist().filter(x=>!past(x)).sort((a,b)=>(a.fecha+a.horaInicio).localeCompare(b.fecha+b.horaInicio)).slice(0,3);$('recentBookings').innerHTML=a.length?a.map(x=>historyCard(x)).join(''):'<div class="empty">No hay reservas próximas.</div>';bindHistory($('recentBookings'))}function renderHistory(){const a=hist(),next=a.filter(x=>!past(x)),old=a.filter(past);$('historyList').innerHTML=(next.length?`<h3 class="history-title">Próximas</h3>${next.map(x=>historyCard(x,true)).join('')}`:'')+(old.length?`<h3 class="history-title">Anteriores</h3>${old.map(x=>historyCard(x,true)).join('')}`:'')||'<div class="empty">No hay reservas guardadas.</div>';bindHistory($('historyList'))}async function refreshHistory(){const a=hist(),out=[];$('historyStatus').textContent='Actualizando...';for(const x of a){try{const r=await api(`/solicitudes/${encodeURIComponent(x.codigoSeguimiento)}`);if(r.estado!==x.estado)toast(`Solicitud #${r.solicitudId}: ${r.estado}`);out.push(item(r))}catch{out.push(x)}}saveHist(out);renderHistory();$('historyStatus').textContent='Actualizado'}
function openModal(id){state.lastFocus=document.activeElement;$(id).classList.remove('hidden');document.body.style.overflow='hidden';setTimeout(()=>$(id).querySelector('button,input')?.focus(),20)}function closeModal(id){$(id).classList.add('hidden');document.body.style.overflow='';state.lastFocus?.focus();if(id==='historyModal')clearInterval(historyTimer)}function trap(e,m){if(e.key!=='Tab')return;const f=[...m.querySelectorAll('button:not([disabled]),a[href],input:not([disabled]),select,textarea')];if(!f.length)return;if(e.shiftKey&&document.activeElement===f[0]){e.preventDefault();f.at(-1).focus()}else if(!e.shiftKey&&document.activeElement===f.at(-1)){e.preventDefault();f[0].focus()}}function openTracking(c){openModal('trackingModal');if(c){$('trackingCode').value=c;searchTracking()}}async function searchTracking(){const c=$('trackingCode').value.trim();if(!c)return;$('searchTracking').disabled=true;try{const r=await api(`/solicitudes/${encodeURIComponent(c)}`);localStorage.setItem(K.code,c);upsert(r);$('trackingResult').innerHTML=`<div class="booking-top"><span class="badge state-${String(r.estado).toLowerCase()}">${esc(r.estado)}</span><button class="link" data-copy="${esc(c)}">Copiar código</button></div><h3>Solicitud #${r.solicitudId}</h3><div class="tracking-grid"><div class="tracking-item"><small>Cancha</small><strong>${esc(r.cancha)}</strong></div><div class="tracking-item"><small>Turno</small><strong>${dateAr(r.fecha)} · ${r.horaInicio.slice(0,5)}</strong></div><div class="tracking-item"><small>Precio</small><strong>${money(r.precioTotal,r.moneda)}</strong></div><div class="tracking-item"><small>Saldo</small><strong>${money(r.saldoPendiente,r.moneda)}</strong></div></div>`;const pay=document.createElement('div');pay.className='summary';$('trackingResult').appendChild(pay);renderPayment(r,pay,$('trackingWhatsapp'));bindCopy()}catch(e){$('trackingError').textContent=e.message;$('trackingError').classList.remove('hidden')}finally{$('searchTracking').disabled=false}}function bindCopy(){document.querySelectorAll('[data-copy]').forEach(b=>{if(b.dataset.bound)return;b.dataset.bound='1';b.onclick=async()=>{try{await navigator.clipboard.writeText(b.dataset.copy);toast('Copiado')}catch{toast('No se pudo copiar')}}})}
function renderComplex(){const c=state.complex,f=[['Dirección',c.direccion||'Consultar'],['WhatsApp',c.whatsapp||'No informado'],['Seña',`${c.porcentajeSenia}%`],['Plazo',`${c.minutosReservaPendiente} minutos`],['Moneda',c.moneda],['Transferencia',c.pagoTransferenciaDisponible?'Disponible':'Consultar']];$('complexFacts').innerHTML=f.map(([a,b])=>`<div class="fact"><small>${a}</small><strong>${esc(b)}</strong></div>`).join('');const a=[];if(c.direccion)a.push(`<a class="button secondary" target="_blank" rel="noopener" href="https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(c.direccion)}">Cómo llegar</a>`);if(c.whatsapp)a.push(`<a class="button whatsapp" target="_blank" rel="noopener" href="https://wa.me/${c.whatsapp.replace(/\D/g,'')}">WhatsApp</a>`);$('contactActions').innerHTML=a.join('')}
async function share(r){const text=`Reserva #${r.solicitudId}\n${r.cancha}\n${dateAr(r.fecha)} · ${r.horaInicio.slice(0,5)}\nCódigo: ${r.codigoSeguimiento}`;if(navigator.share)await navigator.share({title:'Reserva de pádel',text});else{await navigator.clipboard.writeText(text);toast('Resumen copiado')}}function network(){const on=navigator.onLine;$('networkBanner').textContent=on?'Conexión recuperada':'Sin conexión. Algunas funciones no están disponibles.';$('networkBanner').classList.toggle('hidden',on);if(on)toast('Conexión recuperada')}
async function install(){if(!installPrompt)return;installPrompt.prompt();await installPrompt.userChoice;installPrompt=null;document.querySelectorAll('#installButton,#installButtonSecondary').forEach(b=>b.classList.add('hidden'))}function sw(){if(!('serviceWorker'in navigator))return;navigator.serviceWorker.register('service-worker.js').then(reg=>{if(reg.waiting){waitingWorker=reg.waiting;$('updateBar').classList.remove('hidden')}reg.addEventListener('updatefound',()=>{const w=reg.installing;w.addEventListener('statechange',()=>{if(w.state==='installed'&&navigator.serviceWorker.controller){waitingWorker=w;$('updateBar').classList.remove('hidden')}})})});navigator.serviceWorker.addEventListener('controllerchange',()=>location.reload())}
async function init(){theme(localStorage.getItem(K.theme)||'auto');selectDate(state.date);$('date').min=state.date;restoreProfile();const d=draft();if(d&&Date.now()-d.saved<864e5)$('draftNotice').classList.remove('hidden');renderRecent();try{const [c,cs]=await Promise.all([api('/complejo'),api('/canchas')]);state.complex=c;state.courts=cs;document.documentElement.style.setProperty('--p',c.colorPrincipal||'#486b86');$('brandName').textContent=$('footerName').textContent=c.nombreComercial;$('footerContact').textContent=[c.direccion,c.whatsapp].filter(Boolean).join(' · ');document.title=`Reservas | ${c.nombreComercial}`;renderCourts();renderComplex();$('apiStatus').textContent='Disponibilidad online'}catch(e){$('apiStatus').textContent='Servicio no disponible';msg(e.message)}sw();network()}
$('themeToggle').onclick=cycleTheme;matchMedia('(prefers-color-scheme:dark)').addEventListener('change',()=>{if((localStorage.getItem(K.theme)||'auto')==='auto')theme('auto')});window.addEventListener('beforeinstallprompt',e=>{e.preventDefault();installPrompt=e;document.querySelectorAll('#installButton,#installButtonSecondary').forEach(b=>b.classList.remove('hidden'))});const installTop=$('installButton');const installSecondary=$('installButtonSecondary');if(installTop)installTop.onclick=install;if(installSecondary)installSecondary.onclick=install;$('applyUpdate').onclick=()=>waitingWorker?.postMessage('SKIP_WAITING');window.addEventListener('online',network);window.addEventListener('offline',network);$('availability').onclick=()=>loadAvailability(true);$('refreshTimes').onclick=()=>loadAvailability(false);$('prevDate').onclick=()=>{if(state.date>localDate(new Date())){selectDate(addDays(state.date,-1));loadAvailability(false)}};$('nextDate').onclick=()=>{selectDate(addDays(state.date,1));loadAvailability(false)};$('date').onchange=()=>{selectDate($('date').value);saveDraft()};document.querySelectorAll('[data-back]').forEach(b=>b.onclick=()=>showStep(+b.dataset.back));['name','lastname','document','phone','email','players','comments'].forEach(id=>$(id).addEventListener('input',saveDraft));$('form').onsubmit=submit;$('restoreDraft').onclick=restoreDraft;$('discardDraft').onclick=clearDraft;$('newRequest').onclick=()=>{clearDraft();state.time=null;state.availability=null;selectCourt(null);showStep(1)};$('shareCreated').onclick=()=>share(state.created);$('viewCreated').onclick=()=>openTracking(state.created?.codigoSeguimiento);$('openTracking').onclick=()=>openTracking(localStorage.getItem(K.code));$('openHistory').onclick=()=>{openModal('historyModal');renderHistory();refreshHistory();historyTimer=setInterval(refreshHistory,6e4)};document.querySelectorAll('[data-open-history]').forEach(b=>b.onclick=$('openHistory').onclick);$('closeTracking').onclick=()=>closeModal('trackingModal');$('closeHistory').onclick=()=>closeModal('historyModal');$('searchTracking').onclick=searchTracking;$('pasteTracking').onclick=async()=>{$('trackingCode').value=await navigator.clipboard.readText().catch(()=>'')};$('refreshHistory').onclick=refreshHistory;$('clearHistory').onclick=()=>{if(confirm('¿Borrar el historial?')){saveHist([]);renderHistory()}};document.querySelectorAll('.modal').forEach(m=>{m.onclick=e=>{if(e.target===m)closeModal(m.id)};m.onkeydown=e=>trap(e,m)});document.onkeydown=e=>{if(e.key==='Escape')document.querySelectorAll('.modal:not(.hidden)').forEach(m=>closeModal(m.id))};document.addEventListener('visibilitychange',()=>{if(document.visibilityState==='visible'&&state.step===2)loadAvailability(false)});init();

// fechas-y-pasos-v3
function decorateBookingStepsV3() {
    document.querySelectorAll('.steps [data-step]').forEach(function (step) {
        if (step.querySelector('.step-number')) return;

        var number = step.getAttribute('data-step') || '';
        var label = step.textContent.replace(/^\\s*[0-9]+\\s*/, '').trim();

        step.textContent = '';

        var numberElement = document.createElement('b');
        numberElement.className = 'step-number';
        numberElement.textContent = number;

        var labelElement = document.createElement('em');
        labelElement.className = 'step-label';
        labelElement.textContent = label;

        step.appendChild(numberElement);
        step.appendChild(labelElement);
    });
}

decorateBookingStepsV3();

// ampliar-dias-disponibles-v1

// modales-cuentas-v1
(function () {
    const openLoginButton = document.getElementById('openLogin');
    const openRegisterButton = document.getElementById('openRegister');
    const closeLoginButton = document.getElementById('closeLogin');
    const closeRegisterButton = document.getElementById('closeRegister');
    const goLoginButton = document.getElementById('goLogin');
    const goRegisterButton = document.getElementById('goRegister');
    const loginModal = document.getElementById('loginModal');
    const registerModal = document.getElementById('registerModal');

    if (!openLoginButton || !openRegisterButton
            || !closeLoginButton || !closeRegisterButton
            || !goLoginButton || !goRegisterButton
            || !loginModal || !registerModal) {
        console.warn('No se pudo inicializar la interfaz de cuentas.');
        return;
    }

    function openAccountModal(id) {
        openModal(id);
    }

    function switchAccountModal(fromId, toId) {
        closeModal(fromId);
        window.setTimeout(function () {
            openAccountModal(toId);
        }, 20);
    }

    openLoginButton.addEventListener('click', function () {
        openAccountModal('loginModal');
    });

    openRegisterButton.addEventListener('click', function () {
        openAccountModal('registerModal');
    });

    closeLoginButton.addEventListener('click', function () {
        closeModal('loginModal');
    });

    closeRegisterButton.addEventListener('click', function () {
        closeModal('registerModal');
    });

    goRegisterButton.addEventListener('click', function () {
        switchAccountModal('loginModal', 'registerModal');
    });

    goLoginButton.addEventListener('click', function () {
        switchAccountModal('registerModal', 'loginModal');
    });
})();
// sesion-y-login-clientes-v1
(function () {
    const openLoginButton = document.getElementById('openLogin');
    const openRegisterButton = document.getElementById('openRegister');
    const loginForm = document.getElementById('loginForm');
    const loginEmail = document.getElementById('loginEmail');
    const loginPassword = document.getElementById('loginPassword');
    const loginError = document.getElementById('loginError');
    const submitLogin = document.getElementById('submitLogin');
    const navActions = document.querySelector('.nav-actions');

    if (!openLoginButton || !openRegisterButton || !loginForm
            || !loginEmail || !loginPassword || !loginError
            || !submitLogin || !navActions) {
        console.warn('No se pudo inicializar el acceso de clientes.');
        return;
    }

    const accountBadge = document.createElement('button');
    accountBadge.id = 'accountBadge';
    accountBadge.type = 'button';
    accountBadge.className = 'nav-btn hidden';
    accountBadge.disabled = true;
    navActions.insertBefore(accountBadge, openLoginButton);

    function showLoginError(text) {
        loginError.textContent = text || '';
        loginError.classList.toggle('hidden', !text);
    }

    function applySession(session) {
        const authenticated = Boolean(session && session.autenticado && session.perfil);
        openLoginButton.classList.toggle('hidden', authenticated);
        openRegisterButton.classList.toggle('hidden', authenticated);
        accountBadge.classList.toggle('hidden', !authenticated);

        if (authenticated) {
            const profile = session.perfil;
            const name = String(profile.nombre || '').trim();
            accountBadge.textContent = name ? 'Hola, ' + name : 'Mi cuenta';
        } else {
            accountBadge.textContent = '';
        }
    }

    async function request(path, options) {
        const response = await fetch(API + path, {
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json',
                ...((options && options.headers) || {})
            },
            ...(options || {})
        });

        let data;
        try {
            data = await response.json();
        } catch {
            data = { mensaje: 'El servidor devolvio una respuesta invalida.' };
        }

        if (!response.ok) {
            throw new Error(data.mensaje || 'No se pudo completar la operacion.');
        }

        return data;
    }

    async function loadCurrentSession() {
        try {
            const session = await request('/auth/sesion', { method: 'GET' });
            applySession(session);
        } catch (error) {
            applySession(null);
            console.warn('No se pudo consultar la sesion del cliente.', error);
        }
    }

    loginForm.addEventListener('submit', async function (event) {
        event.preventDefault();
        showLoginError('');

        const email = loginEmail.value.trim();
        const password = loginPassword.value;

        if (!email || !password) {
            showLoginError('Completa el correo y la contrasena.');
            return;
        }

        submitLogin.disabled = true;
        submitLogin.textContent = 'Ingresando...';

        try {
            const session = await request('/auth/login', {
                method: 'POST',
                body: JSON.stringify({ email, password })
            });
            applySession(session);
            loginForm.reset();
            closeModal('loginModal');
            toast('Sesion iniciada correctamente');
        } catch (error) {
            showLoginError(error.message);
        } finally {
            submitLogin.disabled = false;
            submitLogin.textContent = 'Ingresar';
        }
    });

    loadCurrentSession();
})();
// cerrar-sesion-clientes-v1
(function () {
    const accountBadge = document.getElementById('accountBadge');
    const openLoginButton = document.getElementById('openLogin');
    const openRegisterButton = document.getElementById('openRegister');
    const navActions = document.querySelector('.nav-actions');

    if (!accountBadge || !openLoginButton || !openRegisterButton || !navActions) {
        console.warn('No se pudo inicializar el cierre de sesion.');
        return;
    }

    accountBadge.disabled = false;
    accountBadge.setAttribute('aria-haspopup', 'true');
    accountBadge.setAttribute('aria-expanded', 'false');

    const menu = document.createElement('div');
    menu.id = 'accountMenu';
    menu.className = 'account-menu hidden';
    menu.innerHTML = '<button id="openProfile" type="button">Mi perfil</button><button id="logoutAccount" type="button">Cerrar sesi\u00f3n</button>';
    navActions.appendChild(menu);

    const logoutButton = document.getElementById('logoutAccount');

    function closeAccountMenu() {
        menu.classList.add('hidden');
        accountBadge.setAttribute('aria-expanded', 'false');
    }

    function showGuestHeader() {
        accountBadge.classList.add('hidden');
        accountBadge.textContent = '';
        openLoginButton.classList.remove('hidden');
        openRegisterButton.classList.remove('hidden');
        closeAccountMenu();
    }

    accountBadge.addEventListener('click', function (event) {
        event.stopPropagation();
        const willOpen = menu.classList.contains('hidden');
        menu.classList.toggle('hidden', !willOpen);
        accountBadge.setAttribute('aria-expanded', String(willOpen));
    });

    logoutButton.addEventListener('click', async function () {
        logoutButton.disabled = true;
        logoutButton.textContent = 'Cerrando...';

        try {
            const response = await fetch(API + '/auth/logout', {
                method: 'POST',
                credentials: 'include',
                headers: { 'Content-Type': 'application/json' }
            });

            if (!response.ok) {
                let data = null;
                try { data = await response.json(); } catch { }
                throw new Error(data && data.mensaje
                    ? data.mensaje
                    : 'No se pudo cerrar la sesion.');
            }

            showGuestHeader();
            toast('Sesi\u00f3n cerrada correctamente');
        } catch (error) {
            toast(error.message || 'No se pudo cerrar la sesion');
        } finally {
            logoutButton.disabled = false;
            logoutButton.textContent = 'Cerrar sesi\u00f3n';
        }
    });

    document.addEventListener('click', function (event) {
        if (!menu.contains(event.target) && event.target !== accountBadge) {
            closeAccountMenu();
        }
    });

    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape' && !menu.classList.contains('hidden')) {
            closeAccountMenu();
            accountBadge.focus();
        }
    });
})();
// registro-clientes-web-v1
(function () {
    const form = document.getElementById('registerForm');
    const nameInput = document.getElementById('registerName');
    const lastnameInput = document.getElementById('registerLastname');
    const documentInput = document.getElementById('registerDocument');
    const phoneInput = document.getElementById('registerPhone');
    const emailInput = document.getElementById('registerEmail');
    const passwordInput = document.getElementById('registerPassword');
    const passwordRepeatInput = document.getElementById('registerPasswordRepeat');
    const errorBox = document.getElementById('registerError');
    const submitButton = document.getElementById('submitRegister');

    if (!form || !nameInput || !lastnameInput || !documentInput
            || !phoneInput || !emailInput || !passwordInput
            || !passwordRepeatInput || !errorBox || !submitButton) {
        console.warn('No se pudo inicializar el registro de clientes.');
        return;
    }

    function showError(text) {
        errorBox.textContent = text || '';
        errorBox.classList.toggle('hidden', !text);
    }

    function validate() {
        const name = nameInput.value.trim();
        const lastname = lastnameInput.value.trim();
        const documentValue = documentInput.value.replace(/\D/g, '');
        const phone = phoneInput.value.trim();
        const email = emailInput.value.trim();
        const password = passwordInput.value;
        const passwordRepeat = passwordRepeatInput.value;

        if (!name || !lastname || !documentValue || !phone || !email
                || !password || !passwordRepeat) {
            throw new Error('Completa todos los campos.');
        }
        if (!/^\d{7,10}$/.test(documentValue)) {
            throw new Error('El documento debe tener entre 7 y 10 numeros.');
        }
        if (!/^[0-9+()\-\s]{6,30}$/.test(phone)) {
            throw new Error('El telefono no tiene un formato valido.');
        }
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
            throw new Error('El correo electronico no tiene un formato valido.');
        }
        if (password.length < 8) {
            throw new Error('La contrasena debe tener al menos 8 caracteres.');
        }
        if (password !== passwordRepeat) {
            throw new Error('Las contrasenas no coinciden.');
        }

        return {
            nombre: name,
            apellido: lastname,
            documento: documentValue,
            telefono: phone,
            email: email,
            password: password
        };
    }

    function applyRegisteredSession(session) {
        const openLogin = document.getElementById('openLogin');
        const openRegister = document.getElementById('openRegister');
        const accountBadge = document.getElementById('accountBadge');
        const profile = session && session.perfil;

        if (!profile || !accountBadge || !openLogin || !openRegister) {
            window.location.reload();
            return;
        }

        openLogin.classList.add('hidden');
        openRegister.classList.add('hidden');
        accountBadge.classList.remove('hidden');
        accountBadge.textContent = profile.nombre
            ? 'Hola, ' + String(profile.nombre).trim()
            : 'Mi cuenta';
    }

    submitButton.addEventListener('click', async function (event) {
        event.preventDefault();
        showError('');

        let payload;
        try {
            payload = validate();
        } catch (error) {
            showError(error.message);
            return;
        }

        submitButton.disabled = true;
        submitButton.textContent = 'Creando cuenta...';

        try {
            const response = await fetch(API + '/auth/registro', {
                method: 'POST',
                credentials: 'include',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            let data;
            try {
                data = await response.json();
            } catch {
                data = { mensaje: 'El servidor devolvio una respuesta invalida.' };
            }

            if (!response.ok) {
                throw new Error(data.mensaje || 'No se pudo crear la cuenta.');
            }

            applyRegisteredSession(data);
            form.reset();
            closeModal('registerModal');
            toast('Cuenta creada correctamente');
        } catch (error) {
            showError(error.message || 'No se pudo crear la cuenta.');
        } finally {
            submitButton.disabled = false;
            submitButton.textContent = 'Crear cuenta';
        }
    });
})();
// historial-sincronizado-clientes-v1
(function () {
    const openHistoryButton = document.getElementById('openHistory');
    const refreshButton = document.getElementById('refreshHistory');
    const clearButton = document.getElementById('clearHistory');
    const historyList = document.getElementById('historyList');
    const historyStatus = document.getElementById('historyStatus');

    if (!openHistoryButton || !refreshButton || !clearButton
            || !historyList || !historyStatus) {
        console.warn('No se pudo inicializar el historial sincronizado.');
        return;
    }

    const localOpenHistory = openHistoryButton.onclick;
    const localRefreshHistory = refreshButton.onclick;
    const localClearHistory = clearButton.onclick;
    let synchronizedMode = false;

    async function request(path) {
        const response = await fetch(API + path, {
            method: 'GET',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' }
        });

        let data;
        try {
            data = await response.json();
        } catch {
            data = { mensaje: 'El servidor devolvio una respuesta invalida.' };
        }

        if (!response.ok) {
            const error = new Error(data.mensaje || 'No se pudo cargar el historial.');
            error.status = response.status;
            throw error;
        }

        return data;
    }

    function moneyAccount(value, currency) {
        return new Intl.NumberFormat('es-AR', {
            style: 'currency',
            currency: currency || 'ARS'
        }).format(Number(value || 0));
    }

    function stateClass(value) {
        return 'state-' + String(value || '').toLowerCase();
    }

    function reservationCard(reservation) {
        const code = reservation.codigoSeguimiento;
        const actions = [];

        if (code) {
            actions.push('<button class="button primary" data-account-open="'
                + esc(code) + '">Ver estado</button>');
            actions.push('<button class="button secondary" data-copy="'
                + esc(code) + '">Copiar codigo</button>');
        }

        return '<article class="history-item account-history-item">'
            + '<div class="booking-top"><span class="badge '
            + stateClass(reservation.estado) + '">'
            + esc(reservation.estado || '') + '</span><small>#'
            + esc(reservation.reservaId || '-') + '</small></div>'
            + '<h3>' + esc(reservation.cancha || '') + '</h3>'
            + '<p>' + dateAr(reservation.fecha) + ' - '
            + String(reservation.horaInicio || '').slice(0, 5) + ' a '
            + String(reservation.horaFin || '').slice(0, 5) + '</p>'
            + '<div class="account-history-money"><span><small>Total</small><strong>'
            + moneyAccount(reservation.precioTotal, reservation.moneda)
            + '</strong></span><span><small>Acreditado</small><strong>'
            + moneyAccount(reservation.totalAcreditado, reservation.moneda)
            + '</strong></span><span><small>Saldo</small><strong>'
            + moneyAccount(reservation.saldoPendiente, reservation.moneda)
            + '</strong></span></div>'
            + (reservation.mensaje
                ? '<p class="history-note">' + esc(reservation.mensaje) + '</p>'
                : '')
            + (actions.length
                ? '<div class="history-actions">' + actions.join('') + '</div>'
                : '<div class="history-note">Reserva registrada por el complejo.</div>')
            + '</article>';
    }

    function renderSection(title, reservations) {
        if (!Array.isArray(reservations) || reservations.length === 0) {
            return '';
        }
        return '<h3 class="history-title">' + title + '</h3>'
            + reservations.map(reservationCard).join('');
    }

    function bindSynchronizedActions() {
        historyList.querySelectorAll('[data-account-open]').forEach(function (button) {
            button.addEventListener('click', function () {
                closeModal('historyModal');
                openTracking(button.dataset.accountOpen);
            });
        });
        bindCopy();
    }

    function renderSynchronizedHistory(history) {
        const next = Array.isArray(history.proximas) ? history.proximas : [];
        const previous = Array.isArray(history.anteriores) ? history.anteriores : [];
        const html = renderSection('Proximas', next)
            + renderSection('Anteriores', previous);

        historyList.innerHTML = html || '<div class="empty">Todavia no tenes reservas asociadas a tu cuenta.</div>';
        historyStatus.textContent = (history.total || 0) + ' reservas sincronizadas';
        clearButton.classList.add('hidden');
        bindSynchronizedActions();
    }

    async function loadSynchronizedHistory() {
        historyStatus.textContent = 'Actualizando...';
        try {
            const history = await request('/cliente/reservas');
            synchronizedMode = true;
            renderSynchronizedHistory(history);
        } catch (error) {
            if (error.status === 401) {
                synchronizedMode = false;
                clearButton.classList.remove('hidden');
                renderHistory();
                historyStatus.textContent = 'Guardado en este dispositivo';
                return;
            }
            historyStatus.textContent = 'No se pudo actualizar';
            historyList.innerHTML = '<div class="alert">'
                + esc(error.message) + '</div>';
        }
    }

    openHistoryButton.onclick = function () {
        openModal('historyModal');
        loadSynchronizedHistory();
    };

    document.querySelectorAll('[data-open-history]').forEach(function (button) {
        button.onclick = openHistoryButton.onclick;
    });

    refreshButton.onclick = function () {
        if (synchronizedMode) {
            loadSynchronizedHistory();
        } else if (typeof localRefreshHistory === 'function') {
            localRefreshHistory.call(refreshButton);
        } else {
            refreshHistory();
        }
    };

    clearButton.onclick = function () {
        if (synchronizedMode) return;
        if (typeof localClearHistory === 'function') {
            localClearHistory.call(clearButton);
        }
    };
})();
// recientes-sincronizadas-clientes-v1
(function () {
    const recentContainer = document.getElementById('recentBookings');
    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');

    if (!recentContainer) {
        console.warn('No se pudo inicializar las reservas recientes sincronizadas.');
        return;
    }

    let requestNumber = 0;

    async function getJson(path) {
        const response = await fetch(API + path, {
            method: 'GET',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' }
        });

        let data;
        try {
            data = await response.json();
        } catch {
            data = { mensaje: 'El servidor devolvio una respuesta invalida.' };
        }

        if (!response.ok) {
            const error = new Error(data.mensaje || 'No se pudieron cargar las reservas.');
            error.status = response.status;
            throw error;
        }

        return data;
    }

    function synchronizedCard(reservation) {
        const code = reservation.codigoSeguimiento;
        const openAttribute = code
            ? ' data-recent-account="' + esc(code) + '" tabindex="0" role="button"'
            : '';
        const payment = money(reservation.saldoPendiente, reservation.moneda || 'ARS');

        return '<article class="booking-mini synchronized-recent"'
            + openAttribute + '>'
            + '<div class="booking-top"><span class="badge state-'
            + String(reservation.estado || '').toLowerCase() + '">'
            + esc(reservation.estado || '') + '</span><small>#'
            + esc(reservation.reservaId || '-') + '</small></div>'
            + '<h3>' + esc(reservation.cancha || '') + '</h3>'
            + '<p>' + dateAr(reservation.fecha) + ' - '
            + String(reservation.horaInicio || '').slice(0, 5) + ' a '
            + String(reservation.horaFin || '').slice(0, 5) + '</p>'
            + '<div class="synchronized-recent-footer"><span>Saldo</span><strong>'
            + payment + '</strong></div>'
            + (code
                ? '<small class="synchronized-recent-help">Abrir estado</small>'
                : '<small class="synchronized-recent-help">Reserva registrada por el complejo</small>')
            + '</article>';
    }

    function bindCards() {
        recentContainer.querySelectorAll('[data-recent-account]').forEach(function (card) {
            function open() {
                openTracking(card.dataset.recentAccount);
            }
            card.addEventListener('click', open);
            card.addEventListener('keydown', function (event) {
                if (event.key === 'Enter' || event.key === ' ') {
                    event.preventDefault();
                    open();
                }
            });
        });
    }

    function renderSynchronized(history) {
        const next = Array.isArray(history.proximas)
            ? history.proximas.slice(0, 3)
            : [];

        recentContainer.innerHTML = next.length
            ? next.map(synchronizedCard).join('')
            : '<div class="empty">No hay reservas proximas en tu cuenta.</div>';
        bindCards();
    }

    async function refreshRecentAccount() {
        const currentRequest = ++requestNumber;
        try {
            const history = await getJson('/cliente/reservas');
            if (currentRequest !== requestNumber) return;
            renderSynchronized(history);
        } catch (error) {
            if (currentRequest !== requestNumber) return;
            if (error.status === 401) {
                renderRecent();
                return;
            }
            console.warn('No se pudieron actualizar las reservas recientes.', error);
        }
    }

    if (loginForm) {
        loginForm.addEventListener('submit', function () {
            window.setTimeout(refreshRecentAccount, 700);
        });
    }

    if (registerForm) {
        registerForm.addEventListener('submit', function () {
            window.setTimeout(refreshRecentAccount, 900);
        });
    }

    document.addEventListener('click', function (event) {
        if (event.target && event.target.id === 'logoutAccount') {
            window.setTimeout(function () {
                requestNumber++;
                renderRecent();
            }, 500);
        }
    });

    document.addEventListener('visibilitychange', function () {
        if (document.visibilityState === 'visible') {
            refreshRecentAccount();
        }
    });

    refreshRecentAccount();
})();
// precargar-formulario-cuenta-v1
(function () {
    const nameInput = document.getElementById('name');
    const lastnameInput = document.getElementById('lastname');
    const documentInput = document.getElementById('document');
    const phoneInput = document.getElementById('phone');
    const emailInput = document.getElementById('email');
    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');

    if (!nameInput || !lastnameInput || !documentInput
            || !phoneInput || !emailInput) {
        console.warn('No se pudo inicializar la precarga del formulario.');
        return;
    }

    let lastProfileId = null;

    async function getProfile() {
        const response = await fetch(API + '/cliente/perfil', {
            method: 'GET',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' }
        });

        if (response.status === 401) return null;

        let data;
        try {
            data = await response.json();
        } catch {
            throw new Error('El servidor devolvio una respuesta invalida.');
        }

        if (!response.ok) {
            throw new Error(data.mensaje || 'No se pudo cargar el perfil.');
        }

        return data;
    }

    function applyProfile(profile) {
        if (!profile) {
            lastProfileId = null;
            return;
        }

        const profileId = String(profile.clienteId || '');
        if (profileId && profileId === lastProfileId) return;

        nameInput.value = profile.nombre || '';
        lastnameInput.value = profile.apellido || '';
        documentInput.value = profile.documento || '';
        phoneInput.value = profile.telefono || '';
        emailInput.value = profile.email || '';

        nameInput.dispatchEvent(new Event('input', { bubbles: true }));
        lastnameInput.dispatchEvent(new Event('input', { bubbles: true }));
        phoneInput.dispatchEvent(new Event('input', { bubbles: true }));
        emailInput.dispatchEvent(new Event('input', { bubbles: true }));

        lastProfileId = profileId;
    }

    async function refreshProfile() {
        try {
            applyProfile(await getProfile());
        } catch (error) {
            console.warn('No se pudo precargar el perfil del cliente.', error);
        }
    }

    if (loginForm) {
        loginForm.addEventListener('submit', function () {
            window.setTimeout(refreshProfile, 700);
        });
    }

    if (registerForm) {
        registerForm.addEventListener('submit', function () {
            window.setTimeout(refreshProfile, 900);
        });
    }

    document.addEventListener('click', function (event) {
        if (event.target && event.target.id === 'logoutAccount') {
            lastProfileId = null;
        }
    });

    document.addEventListener('visibilitychange', function () {
        if (document.visibilityState === 'visible') {
            refreshProfile();
        }
    });

    refreshProfile();
})();
// reserva-perfil-autenticado-v1
(function () {
    const card = document.getElementById('authenticatedBookingProfile');
    const fullName = document.getElementById('authenticatedBookingName');
    const documentValue = document.getElementById('authenticatedBookingDocument');
    const phoneValue = document.getElementById('authenticatedBookingPhone');
    const emailValue = document.getElementById('authenticatedBookingEmail');

    if (!card || !fullName || !documentValue || !phoneValue || !emailValue) {
        console.warn('No se pudo inicializar el resumen del cliente autenticado.');
        return;
    }

    function formatDocument(value) {
        const digits = String(value || '').replace(/\D/g, '');
        return digits ? new Intl.NumberFormat('es-AR').format(Number(digits)) : '-';
    }

    function showGuestBookingForm() {
        document.body.classList.remove('customer-authenticated');
        card.classList.add('hidden');
        fullName.textContent = '';
        documentValue.textContent = '';
        phoneValue.textContent = '';
        emailValue.textContent = '';
    }

    function showAuthenticatedBookingForm(profile) {
        if (!profile) {
            showGuestBookingForm();
            return;
        }

        fullName.textContent = [profile.nombre, profile.apellido]
                .filter(Boolean).join(' ');
        documentValue.textContent = formatDocument(profile.documento);
        phoneValue.textContent = profile.telefono || '-';
        emailValue.textContent = profile.email || '-';
        card.classList.remove('hidden');
        document.body.classList.add('customer-authenticated');
    }

    async function refreshAuthenticatedBookingForm() {
        try {
            const response = await fetch(API + '/cliente/perfil', {
                method: 'GET',
                credentials: 'include',
                headers: { 'Content-Type': 'application/json' }
            });

            if (response.status === 401) {
                showGuestBookingForm();
                return;
            }

            if (!response.ok) {
                throw new Error('No se pudo consultar el perfil.');
            }

            showAuthenticatedBookingForm(await response.json());
        } catch (error) {
            showGuestBookingForm();
            console.warn('No se pudo actualizar el formulario de reserva.', error);
        }
    }

    document.addEventListener('click', function (event) {
        if (event.target && event.target.id === 'logoutAccount') {
            window.setTimeout(refreshAuthenticatedBookingForm, 500);
        }
    });

    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');

    if (loginForm) {
        loginForm.addEventListener('submit', function () {
            window.setTimeout(refreshAuthenticatedBookingForm, 700);
        });
    }

    if (registerForm) {
        registerForm.addEventListener('submit', function () {
            window.setTimeout(refreshAuthenticatedBookingForm, 900);
        });
    }

    document.addEventListener('visibilitychange', function () {
        if (document.visibilityState === 'visible') {
            refreshAuthenticatedBookingForm();
        }
    });

    refreshAuthenticatedBookingForm();
})();
// mi-perfil-estructura-v1
(function () {
    const modal = document.getElementById('profileModal');
    const openButton = document.getElementById('openProfile');
    const closeButton = document.getElementById('closeProfile');
    const accountMenu = document.getElementById('accountMenu');
    const accountBadge = document.getElementById('accountBadge');

    if (!modal || !openButton || !closeButton) {
        console.warn('No se pudo inicializar el modal Mi perfil.');
        return;
    }

    let previousFocus = null;

    function openProfileModal() {
        previousFocus = document.activeElement;
        accountMenu?.classList.add('hidden');
        accountBadge?.setAttribute('aria-expanded', 'false');
        modal.classList.remove('hidden');
        document.body.style.overflow = 'hidden';
        window.setTimeout(function () {
            document.getElementById('profileName')?.focus();
        }, 20);
    }

    function closeProfileModal() {
        modal.classList.add('hidden');
        document.body.style.overflow = '';
        previousFocus?.focus();
    }

    openButton.addEventListener('click', openProfileModal);
    closeButton.addEventListener('click', closeProfileModal);

    modal.addEventListener('click', function (event) {
        if (event.target === modal) closeProfileModal();
    });

    modal.addEventListener('keydown', function (event) {
        if (event.key === 'Escape') {
            event.preventDefault();
            closeProfileModal();
            return;
        }

        if (event.key !== 'Tab') return;
        const focusable = Array.from(modal.querySelectorAll(
            'button:not([disabled]), input:not([disabled]), a[href], textarea, select'
        ));
        if (!focusable.length) return;
        const first = focusable[0];
        const last = focusable[focusable.length - 1];
        if (event.shiftKey && document.activeElement === first) {
            event.preventDefault();
            last.focus();
        } else if (!event.shiftKey && document.activeElement === last) {
            event.preventDefault();
            first.focus();
        }
    });

    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape' && !modal.classList.contains('hidden')) {
            closeProfileModal();
        }
    });
})();
// mi-perfil-conectado-v1
(function () {
    const modal = document.getElementById('profileModal');
    const openButton = document.getElementById('openProfile');
    const form = document.getElementById('profileForm');
    const nameInput = document.getElementById('profileName');
    const lastnameInput = document.getElementById('profileLastname');
    const documentInput = document.getElementById('profileDocument');
    const phoneInput = document.getElementById('profilePhone');
    const emailInput = document.getElementById('profileEmail');
    const errorBox = document.getElementById('profileError');
    const successBox = document.getElementById('profileSuccess');
    const submitButton = document.getElementById('submitProfile');

    if (!modal || !openButton || !form || !nameInput || !lastnameInput
            || !documentInput || !phoneInput || !emailInput
            || !errorBox || !successBox || !submitButton) {
        console.warn('No se pudo conectar el formulario Mi perfil.');
        return;
    }

    function showMessage(element, message) {
        element.textContent = message || '';
        element.classList.toggle('hidden', !message);
    }

    function clearMessages() {
        showMessage(errorBox, '');
        showMessage(successBox, '');
    }

    function fillProfile(profile) {
        nameInput.value = profile.nombre || '';
        lastnameInput.value = profile.apellido || '';
        documentInput.value = profile.documento || '';
        phoneInput.value = profile.telefono || '';
        emailInput.value = profile.email || '';
    }

    async function requestProfile(method, body) {
        const options = {
            method: method,
            credentials: 'include',
            headers: { 'Content-Type': 'application/json; charset=utf-8' }
        };
        if (body) options.body = JSON.stringify(body);

        const response = await fetch(API + '/cliente/perfil', options);
        let data;
        try {
            data = await response.json();
        } catch {
            data = { mensaje: 'El servidor devolvio una respuesta invalida.' };
        }

        if (!response.ok) {
            const error = new Error(data.mensaje || 'No se pudo procesar el perfil.');
            error.status = response.status;
            throw error;
        }
        return data;
    }

    async function loadProfile() {
        clearMessages();
        submitButton.disabled = true;
        try {
            fillProfile(await requestProfile('GET'));
        } catch (error) {
            showMessage(errorBox, error.message);
            if (error.status === 401) {
                modal.classList.add('hidden');
                document.body.style.overflow = '';
            }
        } finally {
            submitButton.disabled = false;
        }
    }

    function validate() {
        const name = nameInput.value.trim();
        const lastname = lastnameInput.value.trim();
        const phone = phoneInput.value.trim();
        const email = emailInput.value.trim();

        if (!name) return 'El nombre es obligatorio.';
        if (!lastname) return 'El apellido es obligatorio.';
        if (!/^[0-9+()\-\s]{6,30}$/.test(phone)) {
            return 'El telefono no tiene un formato valido.';
        }
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
            return 'El correo electronico no tiene un formato valido.';
        }
        return '';
    }

    function updateVisibleProfile(profile) {
        const accountBadge = document.getElementById('accountBadge');
        if (accountBadge) {
            accountBadge.textContent = 'Hola, ' + (profile.nombre || 'Cliente');
        }

        const bookingName = document.getElementById('authenticatedBookingName');
        const bookingDocument = document.getElementById('authenticatedBookingDocument');
        const bookingPhone = document.getElementById('authenticatedBookingPhone');
        const bookingEmail = document.getElementById('authenticatedBookingEmail');

        if (bookingName) {
            bookingName.textContent = [profile.nombre, profile.apellido]
                    .filter(Boolean).join(' ');
        }
        if (bookingDocument) {
            const digits = String(profile.documento || '').replace(/\D/g, '');
            bookingDocument.textContent = digits
                    ? new Intl.NumberFormat('es-AR').format(Number(digits))
                    : '-';
        }
        if (bookingPhone) bookingPhone.textContent = profile.telefono || '-';
        if (bookingEmail) bookingEmail.textContent = profile.email || '-';

        const reservationFields = {
            name: profile.nombre,
            lastname: profile.apellido,
            document: profile.documento,
            phone: profile.telefono,
            email: profile.email
        };
        Object.entries(reservationFields).forEach(function (entry) {
            const input = document.getElementById(entry[0]);
            if (input) input.value = entry[1] || '';
        });

        document.dispatchEvent(new CustomEvent('customer-profile-updated', {
            detail: profile
        }));
    }

    openButton.addEventListener('click', function () {
        window.setTimeout(loadProfile, 0);
    });

    submitButton.addEventListener('click', async function (event) {
        event.preventDefault();
        clearMessages();

        const validationError = validate();
        if (validationError) {
            showMessage(errorBox, validationError);
            return;
        }

        submitButton.disabled = true;
        submitButton.textContent = 'Guardando...';

        try {
            const profile = await requestProfile('PUT', {
                nombre: nameInput.value.trim(),
                apellido: lastnameInput.value.trim(),
                telefono: phoneInput.value.trim(),
                email: emailInput.value.trim()
            });
            fillProfile(profile);
            updateVisibleProfile(profile);
            showMessage(successBox, 'Tus datos se guardaron correctamente.');
        } catch (error) {
            showMessage(errorBox, error.message);
        } finally {
            submitButton.disabled = false;
            submitButton.textContent = 'Guardar cambios';
        }
    });
})();
// cambiar-password-web-v1
(function () {
    const profileModal = document.getElementById('profileModal');
    const form = document.getElementById('passwordForm');
    const currentInput = document.getElementById('currentPassword');
    const newInput = document.getElementById('newPassword');
    const repeatInput = document.getElementById('repeatNewPassword');
    const errorBox = document.getElementById('passwordError');
    const successBox = document.getElementById('passwordSuccess');
    const submitButton = document.getElementById('submitPassword');
    const section = document.querySelector('.password-section');

    if (!profileModal || !form || !currentInput || !newInput
            || !repeatInput || !errorBox || !successBox
            || !submitButton || !section) {
        console.warn('No se pudo inicializar Cambiar contrasena.');
        return;
    }

    function showMessage(element, message) {
        element.textContent = message || '';
        element.classList.toggle('hidden', !message);
    }

    function clearMessages() {
        showMessage(errorBox, '');
        showMessage(successBox, '');
    }

    function clearForm() {
        currentInput.value = '';
        newInput.value = '';
        repeatInput.value = '';
        clearMessages();
        section.open = false;
    }

    submitButton.addEventListener('click', async function (event) {
        event.preventDefault();
        clearMessages();

        const currentPassword = currentInput.value;
        const newPassword = newInput.value;
        const repeatedPassword = repeatInput.value;

        if (!currentPassword) {
            showMessage(errorBox, 'Ingresa tu contrasena actual.');
            return;
        }
        if (newPassword.length < 8) {
            showMessage(errorBox, 'La contrasena nueva debe tener al menos 8 caracteres.');
            return;
        }
        if (newPassword !== repeatedPassword) {
            showMessage(errorBox, 'Las contrasenas nuevas no coinciden.');
            return;
        }
        if (newPassword === currentPassword) {
            showMessage(errorBox, 'La contrasena nueva debe ser diferente.');
            return;
        }

        submitButton.disabled = true;
        submitButton.textContent = 'Actualizando...';

        try {
            const response = await fetch(API + '/cliente/password', {
                method: 'PUT',
                credentials: 'include',
                headers: { 'Content-Type': 'application/json; charset=utf-8' },
                body: JSON.stringify({
                    passwordActual: currentPassword,
                    passwordNuevo: newPassword,
                    passwordRepetido: repeatedPassword
                })
            });

            let data;
            try {
                data = await response.json();
            } catch {
                data = { mensaje: 'El servidor devolvio una respuesta invalida.' };
            }

            if (!response.ok) {
                throw new Error(data.mensaje || 'No se pudo cambiar la contrasena.');
            }

            currentInput.value = '';
            newInput.value = '';
            repeatInput.value = '';
            const successMessage = data.mensaje
                    || 'La contrasena fue actualizada correctamente.';
            showMessage(successBox, successMessage);
            if (typeof toast === 'function') toast('Contrasena actualizada');
            successBox.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        } catch (error) {
            showMessage(errorBox, error.message);
        } finally {
            submitButton.disabled = false;
            submitButton.textContent = 'Actualizar contrasena';
        }
    });

    const closeButton = document.getElementById('closeProfile');
    closeButton?.addEventListener('click', clearForm);

    profileModal.addEventListener('click', function (event) {
        if (event.target === profileModal) clearForm();
    });

    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape'
                && !profileModal.classList.contains('hidden')) {
            clearForm();
        }
    });

    section.addEventListener('toggle', function () {
        if (!section.open) clearForm();
    });
})();
// correccion-password-perfil-v1
(function () {
    const modal = document.getElementById('profileModal');
    const passwordBox = document.getElementById('passwordForm');
    const submitButton = document.getElementById('submitPassword');

    if (!modal || !passwordBox || !submitButton) return;

    modal.addEventListener('click', function (event) {
        if (event.target === modal) {
            event.stopImmediatePropagation();
        }
    }, true);

    passwordBox.addEventListener('keydown', function (event) {
        if (event.key === 'Enter') {
            event.preventDefault();
            submitButton.click();
        }
    });
})();
// limpiar-password-al-abrir-v1
(function () {
    const openProfile = document.getElementById('openProfile');
    const closeProfile = document.getElementById('closeProfile');
    const profileModal = document.getElementById('profileModal');
    const section = document.querySelector('.password-section');
    const fields = [
        document.getElementById('currentPassword'),
        document.getElementById('newPassword'),
        document.getElementById('repeatNewPassword')
    ].filter(Boolean);
    const errorBox = document.getElementById('passwordError');
    const successBox = document.getElementById('passwordSuccess');

    if (!openProfile || !profileModal || !fields.length) return;

    function clearPasswordState() {
        fields.forEach(function (field) {
            field.value = '';
            field.setAttribute('value', '');
        });
        if (errorBox) {
            errorBox.textContent = '';
            errorBox.classList.add('hidden');
        }
        if (successBox) {
            successBox.textContent = '';
            successBox.classList.add('hidden');
        }
        if (section) section.open = false;
    }

    function clearAfterAutofill() {
        clearPasswordState();
        window.setTimeout(clearPasswordState, 60);
        window.setTimeout(clearPasswordState, 250);
    }

    openProfile.addEventListener('click', clearAfterAutofill);
    closeProfile?.addEventListener('click', clearPasswordState);

    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape' && !profileModal.classList.contains('hidden')) {
            clearPasswordState();
        }
    });
})();
// recuperacion-password-web-v1
(function () {
    const loginModal = document.getElementById('loginModal');
    const recoveryModal = document.getElementById('recoveryModal');
    const resetModal = document.getElementById('resetPasswordModal');
    const openRecovery = document.getElementById('openRecovery');
    const closeRecovery = document.getElementById('closeRecovery');
    const closeReset = document.getElementById('closeResetPassword');
    const backToLogin = document.getElementById('backToLogin');
    const resetGoLogin = document.getElementById('resetGoLogin');
    const recoveryForm = document.getElementById('recoveryForm');
    const recoveryEmail = document.getElementById('recoveryEmail');
    const recoveryError = document.getElementById('recoveryError');
    const recoverySuccess = document.getElementById('recoverySuccess');
    const recoveryDevLink = document.getElementById('recoveryDevLink');
    const recoveryTestLink = document.getElementById('recoveryTestLink');
    const recoveryExpiration = document.getElementById('recoveryExpiration');
    const submitRecovery = document.getElementById('submitRecovery');
    const resetForm = document.getElementById('resetPasswordForm');
    const newPassword = document.getElementById('resetPassword');
    const repeatPassword = document.getElementById('resetPasswordRepeat');
    const resetError = document.getElementById('resetPasswordError');
    const resetSuccess = document.getElementById('resetPasswordSuccess');
    const submitReset = document.getElementById('submitResetPassword');
    const resetHelp = document.getElementById('resetPasswordHelp');

    if (!openRecovery || !recoveryModal || !resetModal) return;
    let recoveryToken = null;

    function message(element, value) {
        element.textContent = value || '';
        element.classList.toggle('hidden', !value);
    }
    function show(modal) { modal.classList.remove('hidden'); document.body.style.overflow = 'hidden'; }
    function hide(modal) { modal.classList.add('hidden'); document.body.style.overflow = ''; }
    function openLogin() { hide(recoveryModal); hide(resetModal); loginModal?.classList.remove('hidden'); document.body.style.overflow = 'hidden'; }
    function clearRecovery() {
        recoveryForm.reset(); message(recoveryError, ''); message(recoverySuccess, '');
        recoveryDevLink.classList.add('hidden'); recoveryTestLink.removeAttribute('href'); recoveryExpiration.textContent = '';
    }
    function clearReset() {
        resetForm.reset(); message(resetError, ''); message(resetSuccess, ''); resetGoLogin.classList.add('hidden');
    }

    openRecovery.addEventListener('click', function () {
        loginModal?.classList.add('hidden'); clearRecovery(); show(recoveryModal); recoveryEmail.focus();
    });
    closeRecovery.addEventListener('click', function () { clearRecovery(); hide(recoveryModal); });
    closeReset.addEventListener('click', function () { clearReset(); hide(resetModal); });
    backToLogin.addEventListener('click', openLogin);
    resetGoLogin.addEventListener('click', function () {
        const url = new URL(location.href); url.searchParams.delete('recuperar'); history.replaceState({}, '', url);
        openLogin();
    });

    recoveryForm.addEventListener('submit', async function (event) {
        event.preventDefault(); message(recoveryError, ''); message(recoverySuccess, ''); recoveryDevLink.classList.add('hidden');
        const email = recoveryEmail.value.trim();
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) { message(recoveryError, 'Ingresa un correo electronico valido.'); return; }
        submitRecovery.disabled = true; submitRecovery.textContent = 'Enviando...';
        try {
            const response = await api('/auth/recuperar', { method: 'POST', body: JSON.stringify({ email }) });
            message(recoverySuccess, response.mensaje);
            if (response.enlacePrueba) {
                recoveryTestLink.href = response.enlacePrueba;
                recoveryExpiration.textContent = response.vencimiento ? 'Vence: ' + new Date(response.vencimiento).toLocaleString('es-AR') : '';
                recoveryDevLink.classList.remove('hidden');
            }
        } catch (error) { message(recoveryError, error.message); }
        finally { submitRecovery.disabled = false; submitRecovery.textContent = 'Enviar instrucciones'; }
    });

    async function openResetFromUrl() {
        const params = new URLSearchParams(location.search);
        const token = params.get('recuperar');
        if (!token) return;
        recoveryToken = token;
        loginModal?.classList.add('hidden'); clearReset(); show(resetModal);
        submitReset.disabled = true; resetHelp.textContent = 'Validando el enlace...';
        try {
            const state = await api('/auth/recuperacion?token=' + encodeURIComponent(token));
            if (!state.vigente) throw new Error('El enlace de recuperacion no es valido o ha vencido.');
            resetHelp.textContent = 'Elegi una contrasena nueva para tu cuenta.';
            submitReset.disabled = false; newPassword.focus();
        } catch (error) {
            resetHelp.textContent = 'No se puede utilizar este enlace.'; message(resetError, error.message); submitReset.disabled = true;
        }
    }

    resetForm.addEventListener('submit', async function (event) {
        event.preventDefault(); message(resetError, ''); message(resetSuccess, '');
        if (newPassword.value.length < 8) { message(resetError, 'La contrasena debe tener al menos 8 caracteres.'); return; }
        if (newPassword.value !== repeatPassword.value) { message(resetError, 'Las contrasenas no coinciden.'); return; }
        submitReset.disabled = true; submitReset.textContent = 'Guardando...';
        try {
            const response = await api('/auth/restablecer', { method: 'POST', body: JSON.stringify({ token: recoveryToken, passwordNuevo: newPassword.value, passwordRepetido: repeatPassword.value }) });
            resetForm.reset(); message(resetSuccess, response.mensaje); resetGoLogin.classList.remove('hidden');
            const url = new URL(location.href); url.searchParams.delete('recuperar'); history.replaceState({}, '', url);
        } catch (error) { message(resetError, error.message); submitReset.disabled = false; }
        finally { submitReset.textContent = 'Guardar nueva contrasena'; }
    });

    document.addEventListener('keydown', function (event) {
        if (event.key !== 'Escape') return;
        if (!recoveryModal.classList.contains('hidden')) { clearRecovery(); hide(recoveryModal); }
        if (!resetModal.classList.contains('hidden')) { clearReset(); hide(resetModal); }
    });
    openResetFromUrl();
})();
// torneos-web-publica-v1
(function () {
    const list = document.getElementById('tournamentsList');
    const status = document.getElementById('tournamentsStatus');
    const refresh = document.getElementById('refreshTournaments');
    const modal = document.getElementById('tournamentModal');
    const closeButton = document.getElementById('closeTournamentModal');
    const form = document.getElementById('tournamentForm');
    if (!list || !status || !refresh || !modal || !closeButton || !form) return;

    const field = id => document.getElementById(id);
    const escapeHtml = value => String(value ?? '').replace(/[&<>"']/g, char => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    })[char]);
    const dateText = value => value
        ? new Intl.DateTimeFormat('es-AR', { dateStyle: 'medium' }).format(new Date(value + (value.length === 10 ? 'T12:00:00' : '')))
        : '-';
    const moneyText = value => new Intl.NumberFormat('es-AR', {
        style: 'currency', currency: 'ARS', maximumFractionDigits: 0
    }).format(Number(value || 0));

    function showTournamentError(text) {
        field('tournamentError').textContent = text || '';
        field('tournamentError').classList.toggle('hidden', !text);
    }

    function showTournamentSuccess(text) {
        field('tournamentSuccess').textContent = text || '';
        field('tournamentSuccess').classList.toggle('hidden', !text);
    }

    async function getProfile() {
        const response = await fetch(API + '/cliente/perfil', {
            method: 'GET', credentials: 'include', headers: { 'Content-Type': 'application/json' }
        });
        if (response.status === 401) return null;
        if (!response.ok) return null;
        return response.json();
    }

    async function openTournament(tournamentId, categoryId, tournamentName, categoryName) {
        form.reset();
        showTournamentError('');
        showTournamentSuccess('');
        field('tournamentCategoryId').value = categoryId;
        field('tournamentModalTitle').textContent = 'Inscribir pareja';
        field('tournamentSelection').textContent = tournamentName + ' · ' + categoryName;
        field('tournamentProfileHint').textContent = 'Podés completar los datos manualmente.';
        try {
            const profile = await getProfile();
            if (profile) {
                field('tournamentResponsibleName').value = profile.nombre || '';
                field('tournamentResponsibleLastname').value = profile.apellido || '';
                field('tournamentResponsiblePhone').value = profile.telefono || '';
                field('tournamentProfileHint').textContent = 'Usamos los datos de tu perfil. Podés revisarlos antes de enviar.';
            }
        } catch (error) {
            console.warn('No se pudo precargar el perfil para el torneo.', error);
        }
        modal.classList.remove('hidden');
        document.body.classList.add('modal-open');
        field('tournamentPartnerName').focus();
    }

    function closeTournament() {
        modal.classList.add('hidden');
        document.body.classList.remove('modal-open');
    }

    function categoryHtml(category, tournament) {
        const enabled = Boolean(tournament.inscripcionDisponible && category.disponible && category.cuposDisponibles > 0);
        return `<article class="tournament-category">
            <div><strong>${escapeHtml(category.nombre)} · ${escapeHtml(category.rama)}</strong>
            <small>${category.cuposDisponibles} de ${category.cupoParejas} cupos disponibles</small></div>
            <div class="tournament-category-actions"><span>${moneyText(category.precioInscripcion)}</span>
            <button class="button primary tournament-register" type="button"
                data-tournament-id="${tournament.id}" data-category-id="${category.id}"
                data-tournament-name="${escapeHtml(tournament.nombre)}"
                data-category-name="${escapeHtml(category.nombre)} · ${escapeHtml(category.rama)}"
                ${enabled ? '' : 'disabled'}>${enabled ? 'Inscribir pareja' : 'No disponible'}</button></div>
        </article>`;
    }

    async function loadTournamentDetail(summary) {
        const detail = await api('/torneos/' + summary.id);
        return `<article class="card tournament-card">
            <div class="tournament-card-head"><div><span class="badge">${escapeHtml(detail.estado)}</span>
            <h3>${escapeHtml(detail.nombre)}</h3></div><strong>${detail.categorias.length} categoría(s)</strong></div>
            <p class="muted">${escapeHtml(detail.descripcion || '')}</p>
            <div class="tournament-facts"><span><b>Fechas</b>${dateText(detail.fechaInicio)} al ${dateText(detail.fechaFin)}</span>
            <span><b>Inscripción</b>${dateText(detail.inscripcionDesde)} al ${dateText(detail.inscripcionHasta)}</span></div>
            <div class="tournament-categories">${detail.categorias.map(category => categoryHtml(category, detail)).join('')}</div>
        </article>`;
    }

    async function loadTournaments() {
        status.textContent = 'Cargando torneos...';
        list.innerHTML = '';
        refresh.disabled = true;
        try {
            const summaries = await api('/torneos');
            if (!summaries.length) {
                status.textContent = 'No hay torneos publicados por el momento.';
                return;
            }
            const cards = await Promise.all(summaries.map(loadTournamentDetail));
            list.innerHTML = cards.join('');
            status.textContent = summaries.length + ' torneo(s) disponible(s).';
        } catch (error) {
            status.textContent = error.message || 'No se pudieron cargar los torneos.';
        } finally {
            refresh.disabled = false;
        }
    }

    list.addEventListener('click', event => {
        const button = event.target.closest('.tournament-register');
        if (!button || button.disabled) return;
        openTournament(button.dataset.tournamentId, button.dataset.categoryId,
            button.dataset.tournamentName, button.dataset.categoryName);
    });

    form.addEventListener('submit', async event => {
        event.preventDefault();
        showTournamentError('');
        showTournamentSuccess('');
        const submit = field('submitTournament');
        const payload = {
            torneoCategoriaId: Number(field('tournamentCategoryId').value),
            responsable: {
                nombre: field('tournamentResponsibleName').value.trim(),
                apellido: field('tournamentResponsibleLastname').value.trim(),
                telefono: field('tournamentResponsiblePhone').value.trim()
            },
            pareja: {
                nombre: field('tournamentPartnerName').value.trim(),
                apellido: field('tournamentPartnerLastname').value.trim(),
                telefono: field('tournamentPartnerPhone').value.trim()
            },
            comentarios: field('tournamentComments').value.trim() || null
        };
        if (!payload.responsable.nombre || !payload.responsable.apellido || !payload.responsable.telefono
                || !payload.pareja.nombre || !payload.pareja.apellido || !payload.pareja.telefono) {
            showTournamentError('Completá los datos obligatorios de ambos integrantes.');
            return;
        }
        submit.disabled = true;
        submit.textContent = 'Enviando...';
        try {
            const created = await api('/torneos/inscripciones', {
                method: 'POST', body: JSON.stringify(payload)
            });
            showTournamentSuccess((created.mensaje || 'Inscripción enviada correctamente.')
                + ' Número de solicitud: ' + created.inscripcionId + '.');
            submit.classList.add('hidden');
            await loadTournaments();
        } catch (error) {
            showTournamentError(error.message || 'No se pudo enviar la inscripción.');
        } finally {
            submit.disabled = false;
            submit.textContent = 'Enviar inscripción';
        }
    });

    refresh.addEventListener('click', loadTournaments);
    closeButton.addEventListener('click', closeTournament);
    modal.addEventListener('click', event => { if (event.target === modal) closeTournament(); });
    document.addEventListener('keydown', event => {
        if (event.key === 'Escape' && !modal.classList.contains('hidden')) closeTournament();
    });
    document.addEventListener('visibilitychange', () => {
        if (document.visibilityState === 'visible') loadTournaments();
    });
    loadTournaments();
})();
