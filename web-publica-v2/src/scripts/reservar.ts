import { api, type Cancha, type Complejo } from './api';
import { fecha, hora, moneda } from './formatos';

type Horario = { horaInicio: string; horaFin: string; precio: number; importeSenia: number };
type Disponibilidad = { fecha: string; cancha: Cancha; disponibleEseDia: boolean; horarios: Horario[] };
type Perfil = { nombre?: string; apellido?: string; documento?: string; telefono?: string; email?: string };
type Creada = { solicitudId: number; codigoSeguimiento: string; estado: string; cancha: string; fecha: string; horaInicio: string; horaFin: string; precioTotal: number; importeSenia: number; vencimiento: string; minutosParaPagar: number; moneda: string; mensaje: string };

const estado: { complejo?: Complejo; canchas: Cancha[]; cancha?: Cancha; horario?: Horario; fecha: string } = { canchas: [], fecha: '' };
const el = <T extends HTMLElement>(id: string) => document.getElementById(id) as T;
const valor = (id: string) => el<HTMLInputElement | HTMLTextAreaElement>(id).value.trim();

function fechaLocal(d = new Date()) { const x = new Date(d); x.setMinutes(x.getMinutes() - x.getTimezoneOffset()); return x.toISOString().slice(0, 10); }
function mensaje(texto = '') { const caja = el('mensajeReserva'); caja.textContent = texto; caja.classList.toggle('hidden', !texto); if (texto) caja.scrollIntoView({ behavior: 'smooth', block: 'center' }); }
function paso(numero: number) { document.querySelectorAll<HTMLElement>('.panel-reserva').forEach(p => p.classList.toggle('hidden', Number(p.dataset.panel) !== numero)); document.querySelectorAll<HTMLElement>('[data-paso-indicador]').forEach(p => p.classList.toggle('activo', Number(p.dataset.pasoIndicador) <= numero)); mensaje(); window.scrollTo({ top: 0, behavior: 'smooth' }); }

function renderCanchas() {
  el('listaCanchasReserva').innerHTML = estado.canchas.map(c => `<button type="button" class="cancha-reserva rounded-2xl border p-5 text-left transition hover:-translate-y-0.5 ${estado.cancha?.id === c.id ? 'seleccionada border-brand-300 bg-brand-500/15' : 'border-ink-700 bg-ink-900 hover:border-brand-500'}" data-id="${c.id}"><small class="font-black uppercase tracking-widest text-brand-300">${c.tipo || 'Pádel'}</small><strong class="mt-2 block text-xl">${c.nombre}</strong><span class="mt-2 block text-sm text-slate-400">${c.descripcion || c.superficie || ''}</span><span class="mt-4 block font-black">${moneda(c.precio, estado.complejo?.moneda || 'ARS')}</span><span class="text-xs text-slate-500">${c.duracionMinutos} minutos · seña ${moneda(c.importeSenia, estado.complejo?.moneda || 'ARS')}</span></button>`).join('');
}

async function cargarInicial() {
  try {
    [estado.complejo, estado.canchas] = await Promise.all([api<Complejo>('/complejo'), api<Cancha[]>('/canchas')]);
    estado.fecha = fechaLocal();
    el<HTMLInputElement>('fechaReserva').min = estado.fecha;
    el<HTMLInputElement>('fechaReserva').value = estado.fecha;
    const id = Number(new URLSearchParams(location.search).get('cancha'));
    estado.cancha = estado.canchas.find(c => c.id === id);
    renderCanchas();
  } catch (error) { mensaje(error instanceof Error ? error.message : 'No se pudieron cargar las canchas.'); }
}

el('listaCanchasReserva').addEventListener('click', evento => { const boton = (evento.target as HTMLElement).closest<HTMLButtonElement>('.cancha-reserva'); if (!boton) return; estado.cancha = estado.canchas.find(c => c.id === Number(boton.dataset.id)); renderCanchas(); });

el('buscarHorarios').addEventListener('click', async () => {
  if (!estado.cancha) return mensaje('Seleccioná una cancha.');
  estado.fecha = el<HTMLInputElement>('fechaReserva').value;
  if (!estado.fecha) return mensaje('Seleccioná una fecha.');
  const lista = el('listaHorarios'); lista.innerHTML = '<p class="text-slate-400">Consultando disponibilidad...</p>';
  paso(2);
  try {
    const disponibilidad = await api<Disponibilidad>(`/disponibilidad?canchaId=${estado.cancha.id}&fecha=${estado.fecha}`);
    el('tituloHorarios').textContent = `${estado.cancha.nombre} · ${fecha(estado.fecha)}`;
    if (!disponibilidad.disponibleEseDia || !disponibilidad.horarios.length) { lista.innerHTML = '<div class="col-span-full rounded-xl border border-amber-500/30 bg-amber-500/10 p-5 text-amber-100">No hay horarios disponibles para esta fecha.</div>'; return; }
    lista.innerHTML = disponibilidad.horarios.map(h => `<button type="button" class="horario-reserva rounded-xl border border-ink-700 bg-ink-900 p-4 text-left transition hover:border-brand-300 hover:bg-ink-800" data-inicio="${h.horaInicio}"><small class="text-emerald-300">Disponible</small><strong class="mt-1 block text-xl">${hora(h.horaInicio)} a ${hora(h.horaFin)}</strong><span class="mt-2 block text-sm text-slate-400">${moneda(h.precio, estado.complejo?.moneda || 'ARS')}</span></button>`).join('');
    lista.querySelectorAll<HTMLButtonElement>('.horario-reserva').forEach(b => b.addEventListener('click', () => { estado.horario = disponibilidad.horarios.find(h => h.horaInicio === b.dataset.inicio); mostrarDatos(); }));
  } catch (error) { mensaje(error instanceof Error ? error.message : 'No se pudo consultar la disponibilidad.'); paso(1); }
});

async function mostrarDatos() {
  if (!estado.cancha || !estado.horario) return;
  el('resumenTurno').innerHTML = `<div class="grid gap-4 sm:grid-cols-4"><div><small class="text-slate-400">Cancha</small><strong class="block">${estado.cancha.nombre}</strong></div><div><small class="text-slate-400">Fecha</small><strong class="block">${fecha(estado.fecha)}</strong></div><div><small class="text-slate-400">Horario</small><strong class="block">${hora(estado.horario.horaInicio)} a ${hora(estado.horario.horaFin)}</strong></div><div><small class="text-slate-400">Seña</small><strong class="block">${moneda(estado.horario.importeSenia, estado.complejo?.moneda || 'ARS')}</strong></div></div>`;
  try { const p = await api<Perfil>('/cliente/perfil'); ['Nombre','Apellido','Documento','Telefono','Email'].forEach(k => { const dato = p[k.toLowerCase() as keyof Perfil]; if (dato) el<HTMLInputElement>(`cliente${k}`).value = String(dato); }); } catch { }
  paso(3);
}

document.querySelectorAll<HTMLButtonElement>('[data-volver]').forEach(b => b.addEventListener('click', () => paso(Number(b.dataset.volver))));

function validar() {
  if (!valor('clienteNombre')) throw new Error('Ingresá tu nombre.');
  if (!valor('clienteApellido')) throw new Error('Ingresá tu apellido.');
  if (!/^\d{7,10}$/.test(valor('clienteDocumento').replace(/\D/g, ''))) throw new Error('El documento debe contener entre 7 y 10 números.');
  if (!/^[0-9+()\-\s]{6,30}$/.test(valor('clienteTelefono'))) throw new Error('Ingresá un teléfono válido.');
  if (valor('clienteEmail') && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(valor('clienteEmail'))) throw new Error('Ingresá un correo electrónico válido.');
  const jugadores = Number(valor('cantidadJugadores')); if (jugadores < 1 || jugadores > 8) throw new Error('La cantidad de jugadores debe estar entre 1 y 8.');
  if (!el<HTMLInputElement>('aceptarCondiciones').checked) throw new Error('Aceptá la condición de reserva.');
}

el<HTMLFormElement>('formReserva').addEventListener('submit', async evento => {
  evento.preventDefault(); const boton = el<HTMLButtonElement>('enviarReserva');
  try {
    validar(); if (!estado.cancha || !estado.horario) throw new Error('Seleccioná nuevamente el turno.');
    boton.disabled = true; boton.textContent = 'Validando turno...';
    const disponibilidad = await api<Disponibilidad>(`/disponibilidad?canchaId=${estado.cancha.id}&fecha=${estado.fecha}`);
    if (!disponibilidad.horarios.some(h => h.horaInicio === estado.horario!.horaInicio)) throw new Error('El horario acaba de ser ocupado. Elegí otro.');
    boton.textContent = 'Creando solicitud...';
    const creada = await api<Creada>('/solicitudes', { method: 'POST', body: JSON.stringify({ canchaId: estado.cancha.id, fecha: estado.fecha, horaInicio: estado.horario.horaInicio, cantidadJugadores: Number(valor('cantidadJugadores')), cliente: { nombre: valor('clienteNombre'), apellido: valor('clienteApellido'), documento: valor('clienteDocumento').replace(/\D/g, ''), telefono: valor('clienteTelefono'), email: valor('clienteEmail') || null }, comentarios: valor('comentariosReserva') || null }) });
    localStorage.setItem('padel.solicitud.codigo', creada.codigoSeguimiento);
    el('confirmacionReserva').innerHTML = `<div class="grid gap-4 rounded-2xl border border-emerald-500/30 bg-emerald-500/10 p-6 sm:grid-cols-2"><div><small class="text-emerald-200/70">Código de seguimiento</small><strong class="block text-2xl">${creada.codigoSeguimiento}</strong></div><div><small class="text-emerald-200/70">Solicitud</small><strong class="block text-2xl">#${creada.solicitudId}</strong></div><div><small class="text-emerald-200/70">Turno</small><strong class="block">${creada.cancha} · ${fecha(creada.fecha)} · ${hora(creada.horaInicio)}</strong></div><div><small class="text-emerald-200/70">Total / seña</small><strong class="block">${moneda(creada.precioTotal, creada.moneda)} / ${moneda(creada.importeSenia, creada.moneda)}</strong></div></div><p class="mt-5 text-slate-300">${creada.mensaje}</p><p class="mt-2 text-sm text-amber-200">Tenés ${creada.minutosParaPagar} minutos para acreditar la seña.</p>`;
    paso(4);
  } catch (error) { mensaje(error instanceof Error ? error.message : 'No se pudo crear la solicitud.'); }
  finally { boton.disabled = false; boton.textContent = 'Solicitar reserva'; }
});

el('nuevaReserva').addEventListener('click', () => { el<HTMLFormElement>('formReserva').reset(); estado.horario = undefined; paso(1); });
cargarInicial();