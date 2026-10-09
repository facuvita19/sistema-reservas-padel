import { api, type Cancha, type Complejo, type TorneoResumen } from './api';
import { fecha, hora, moneda } from './formatos';

const byId = <T extends HTMLElement>(id:string) => document.getElementById(id) as T | null;
const text = (id:string, value:string) => { const node=byId(id); if(node) node.textContent=value; };
const escapeHtml = (value:unknown) => String(value ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c] || c));
const digits = (value?:string) => (value || '').replace(/\D/g,'');
const today = () => { const d=new Date(); d.setMinutes(d.getMinutes()-d.getTimezoneOffset()); return d.toISOString().slice(0,10); };

function courtCard(cancha:Cancha, currency:string) {
  const href=`/reservar?cancha=${cancha.id}`;
  return `<article class="home-court-card">
    <div class="home-court-visual"><span class="home-court-number">${escapeHtml(cancha.nombre)}</span><span class="home-court-status">Disponible online</span><div class="mini-court"><i></i><b></b></div></div>
    <div class="home-court-content"><div class="home-court-title"><div><small>${escapeHtml(cancha.tipo || 'Pádel')}</small><h3>${escapeHtml(cancha.nombre)}</h3></div><span>${cancha.tieneIluminacion ? 'Con iluminación' : 'Luz natural'}</span></div>
    <p>${escapeHtml(cancha.descripcion || cancha.superficie || 'Cancha preparada para tu próximo partido.')}</p>
    <div class="home-court-tags"><span>${escapeHtml(cancha.superficie || 'Superficie informada')}</span><span>${cancha.duracionMinutos} min</span><span>${hora(cancha.horaApertura)}–${hora(cancha.horaCierre)}</span></div>
    <div class="home-court-footer"><div><small>Turno</small><strong>${moneda(cancha.precio,currency)}</strong></div><div><small>Seña</small><strong>${moneda(cancha.importeSenia,currency)}</strong></div><a class="btn btn-primary" href="${href}">Reservar</a></div></div>
  </article>`;
}

function tournamentCard(tournament?:TorneoResumen) {
  const node=byId('torneoDestacado'); if(!node) return;
  if(!tournament){node.innerHTML='<div class="home-empty-state"><span>PRÓXIMAMENTE</span><h3>Nuevas competencias en preparación</h3><p>Volvé pronto para descubrir categorías, fechas y cupos.</p><a class="btn btn-secondary" href="/torneos">Visitar torneos</a></div>';return;}
  const state=tournament.inscripcionDisponible?'Inscripción abierta':tournament.estado.toLowerCase().replaceAll('_',' ');
  node.innerHTML=`<div class="home-tournament-top"><span>${escapeHtml(state)}</span><small>${tournament.cantidadCategorias} categoría${tournament.cantidadCategorias===1?'':'s'}</small></div><h3>${escapeHtml(tournament.nombre)}</h3><p>${escapeHtml(tournament.descripcion || 'Una nueva competencia para vivir dentro y fuera de la cancha.')}</p><div class="home-tournament-date"><small>Se juega</small><strong>${fecha(tournament.fechaInicio)} al ${fecha(tournament.fechaFin)}</strong></div><a class="btn btn-primary" href="/torneos">Ver torneo y categorías</a>`;
}

function contact(complex:Complejo){
  const node=byId('contactoComplejo'); if(!node) return;
  const links:string[]=[];
  if(complex.direccion) links.push(`<a href="https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(complex.direccion)}" target="_blank" rel="noopener"><small>Ubicación</small><strong>${escapeHtml(complex.direccion)}</strong></a>`);
  if(complex.whatsapp) links.push(`<a href="https://wa.me/${digits(complex.whatsapp)}" target="_blank" rel="noopener"><small>WhatsApp</small><strong>${escapeHtml(complex.whatsapp)}</strong></a>`);
  else if(complex.telefono) links.push(`<a href="tel:${digits(complex.telefono)}"><small>Teléfono</small><strong>${escapeHtml(complex.telefono)}</strong></a>`);
  if(complex.email) links.push(`<a href="mailto:${escapeHtml(complex.email)}"><small>Correo</small><strong>${escapeHtml(complex.email)}</strong></a>`);
  if(complex.instagram) { const handle=complex.instagram.replace(/^@/,''); links.push(`<a href="https://instagram.com/${encodeURIComponent(handle)}" target="_blank" rel="noopener"><small>Instagram</small><strong>@${escapeHtml(handle)}</strong></a>`); }
  node.innerHTML=links.length?links.join(''):'<span class="text-slate-400">Los datos de contacto estarán disponibles próximamente.</span>';
}

function setupQuick(courts:Cancha[]){
  const date=byId<HTMLInputElement>('fechaRapida'); const select=byId<HTMLSelectElement>('canchaRapida'); const form=byId<HTMLFormElement>('formReservaRapida');
  if(date){date.min=today();date.value=today();}
  if(select) select.innerHTML='<option value="">Cualquier cancha</option>'+courts.map(c=>`<option value="${c.id}">${escapeHtml(c.nombre)}</option>`).join('');
  form?.addEventListener('submit',event=>{event.preventDefault();const params=new URLSearchParams();if(date?.value)params.set('fecha',date.value);if(select?.value)params.set('cancha',select.value);location.href=`/reservar?${params.toString()}`;});
}

async function load(){
  const state=byId('estadoPortada');
  try{
    const [complex,courts,tournaments]=await Promise.all([api<Complejo>('/complejo'),api<Cancha[]>('/canchas'),api<TorneoResumen[]>('/torneos').catch(()=>[])]);
    document.documentElement.style.setProperty('--color-principal',complex.colorPrincipal || '#3d9b70');
    text('nombreComplejo',complex.nombreComercial);text('marcaNombre',complex.nombreComercial);text('direccionComplejo',complex.direccion || 'Ubicación a confirmar');
    const prices=courts.map(c=>Number(c.precio)).filter(Number.isFinite);const durations=courts.map(c=>c.duracionMinutos).filter(Boolean);
    text('datoCanchas',String(courts.length));text('metricaCanchas',String(courts.length));text('datoTurno',prices.length?moneda(Math.min(...prices),complex.moneda):'Consultar');
    text('datoSenia',`${complex.porcentajeSenia}%`);text('metricaDuracion',durations.length?`${Math.round(durations.reduce((a,b)=>a+b,0)/durations.length)} min`:'Consultar');text('metricaPlazo',`${complex.minutosReservaPendiente} min`);
    text('heroDisponibilidad',courts.length?`${courts.length} cancha${courts.length===1?'':'s'} para elegir`:'Próximamente');
    const container=byId('canchasDestacadas'); if(container) container.innerHTML=courts.length?courts.slice(0,3).map(c=>courtCard(c,complex.moneda)).join(''):'<div class="home-empty-state col-span-full"><span>SIN CANCHAS PUBLICADAS</span><h3>Estamos preparando la disponibilidad</h3><p>Consultá nuevamente más tarde.</p></div>';
    setupQuick(courts);tournamentCard(tournaments.find(t=>t.inscripcionDisponible) || tournaments[0]);contact(complex);state?.remove();
  }catch(error){
    if(state) state.innerHTML=`<div><strong>No pudimos cargar la portada</strong><p>${escapeHtml(error instanceof Error?error.message:'Intentá nuevamente en unos instantes.')}</p><button id="reintentarPortada" class="btn btn-secondary mt-4" type="button">Reintentar</button></div>`;
    byId('reintentarPortada')?.addEventListener('click',load,{once:true});tournamentCard();
  }
}
load();
