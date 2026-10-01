import { api, type TorneoDetalle, type TorneoResumen } from './api';
import { etiqueta, fecha, fechaHora, moneda } from './formatos';

type Perfil = { nombre?: string; apellido?: string; telefono?: string };
type SolicitudCreada = { inscripcionId: number; estado: string; mensaje: string };

const escapeHtml = (valor: unknown) => String(valor ?? '').replace(/[&<>"']/g, caracter => ({
  '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;',
}[caracter] || caracter));

const campo = (id: string) => document.getElementById(id) as HTMLInputElement | HTMLTextAreaElement;
const modal = document.getElementById('modalInscripcion') as HTMLElement;
const formulario = document.getElementById('formInscripcion') as HTMLFormElement;
const errorCaja = document.getElementById('errorInscripcion') as HTMLElement;
const exitoCaja = document.getElementById('exitoInscripcion') as HTMLElement;
const enviar = document.getElementById('enviarInscripcion') as HTMLButtonElement;
let ultimoFoco: HTMLElement | null = null;

function disponibilidad(torneo: TorneoDetalle) {
  if (torneo.inscripcionDisponible) return { texto: 'Inscripción abierta', clase: 'text-emerald-300 border-emerald-500/30 bg-emerald-500/10' };
  const ahora = new Date();
  const desde = new Date(torneo.inscripcionDesde);
  const hasta = new Date(torneo.inscripcionHasta);
  if (ahora < desde) return { texto: `Abre ${fechaHora(torneo.inscripcionDesde)}`, clase: 'text-amber-200 border-amber-500/30 bg-amber-500/10' };
  if (ahora > hasta) return { texto: 'Inscripción finalizada', clase: 'text-slate-300 border-slate-500/30 bg-slate-500/10' };
  return { texto: etiqueta(torneo.estado), clase: 'text-slate-300 border-slate-500/30 bg-slate-500/10' };
}

function tarjeta(torneo: TorneoDetalle) {
  const estado = disponibilidad(torneo);
  const categorias = torneo.categorias.map(categoria => {
    const habilitada = torneo.inscripcionDisponible && categoria.disponible && categoria.cuposDisponibles > 0;
    return `<div class="rounded-xl border border-ink-700 bg-ink-900/70 p-4">
      <div class="flex items-start justify-between gap-3"><div><strong class="text-lg">${escapeHtml(categoria.nombre)}</strong><small class="mt-1 block text-slate-400">${escapeHtml(etiqueta(categoria.rama))}</small></div><span class="font-black">${moneda(categoria.precioInscripcion)}</span></div>
      <div class="mt-4 h-2 overflow-hidden rounded-full bg-ink-700"><div class="h-full rounded-full bg-brand-500" style="width:${Math.min(100, categoria.cupoParejas ? categoria.parejasConfirmadas / categoria.cupoParejas * 100 : 0)}%"></div></div>
      <div class="mt-2 flex justify-between text-xs text-slate-400"><span>${categoria.parejasConfirmadas} confirmadas</span><span>${categoria.cuposDisponibles} disponibles</span></div>
      <button class="mt-4 w-full rounded-lg px-4 py-2.5 font-black transition active:scale-[.98] ${habilitada ? 'inscribir-pareja bg-brand-500 text-white hover:bg-brand-600' : 'cursor-not-allowed bg-ink-700 text-slate-500'}" ${habilitada ? '' : 'disabled'} type="button" data-categoria-id="${categoria.id}" data-resumen="${escapeHtml(`${torneo.nombre} · ${categoria.nombre} · ${etiqueta(categoria.rama)} · ${moneda(categoria.precioInscripcion)}`)}">${habilitada ? 'Inscribir pareja' : 'No disponible'}</button>
    </div>`;
  }).join('');
  return `<article class="surface-card p-7">
    <div class="flex flex-col gap-5 lg:flex-row lg:items-start lg:justify-between"><div><span class="inline-flex rounded-full border px-3 py-1.5 text-xs font-black ${estado.clase}">${escapeHtml(estado.texto)}</span><h2 class="mt-4 text-3xl font-black">${escapeHtml(torneo.nombre)}</h2><p class="mt-3 max-w-3xl text-slate-400">${escapeHtml(torneo.descripcion || 'Torneo de pádel')}</p></div><div class="rounded-xl border border-ink-700 bg-ink-900 px-5 py-4 text-right"><small class="text-slate-500">Fechas</small><strong class="block">${fecha(torneo.fechaInicio)}<br>al ${fecha(torneo.fechaFin)}</strong></div></div>
    <div class="mt-6 grid gap-4 md:grid-cols-2 xl:grid-cols-3">${categorias}</div>
    ${torneo.reglamento ? `<details class="mt-6 rounded-xl border border-ink-700 bg-ink-900/50 p-4"><summary class="cursor-pointer font-black">Ver reglamento</summary><p class="mt-3 text-slate-400">${escapeHtml(torneo.reglamento)}</p></details>` : ''}
  </article>`;
}

async function completarPerfil() {
  try {
    const perfil = await api<Perfil>('/cliente/perfil');
    campo('responsableNombre').value = perfil.nombre || '';
    campo('responsableApellido').value = perfil.apellido || '';
    campo('responsableTelefono').value = perfil.telefono || '';
  } catch { /* La inscripción también admite visitantes. */ }
}

async function abrirInscripcion(boton: HTMLButtonElement) {
  ultimoFoco = boton;
  formulario.reset();
  campo('torneoCategoriaId').value = boton.dataset.categoriaId || '';
  const resumen = document.getElementById('resumenInscripcion');
  if (resumen) resumen.textContent = boton.dataset.resumen || '';
  errorCaja.classList.add('hidden');
  exitoCaja.classList.add('hidden');
  enviar.classList.remove('hidden');
  enviar.disabled = false;
  enviar.textContent = 'Enviar inscripción';
  document.getElementById('contadorComentarios')!.textContent = '0 / 500';
  modal.classList.remove('hidden');
  modal.classList.add('flex');
  document.body.style.overflow = 'hidden';
  await completarPerfil();
  campo('responsableNombre').focus();
}

function cerrarInscripcion() {
  modal.classList.add('hidden');
  modal.classList.remove('flex');
  document.body.style.overflow = '';
  ultimoFoco?.focus();
}

function telefonoNormalizado(valor: string) { return valor.replace(/\D/g, ''); }

function validar() {
  const requeridos: Array<[string, string]> = [
    ['responsableNombre', 'Ingresá el nombre del responsable.'],
    ['responsableApellido', 'Ingresá el apellido del responsable.'],
    ['responsableTelefono', 'Ingresá el teléfono del responsable.'],
    ['parejaNombre', 'Ingresá el nombre del segundo integrante.'],
    ['parejaApellido', 'Ingresá el apellido del segundo integrante.'],
    ['parejaTelefono', 'Ingresá el teléfono del segundo integrante.'],
  ];
  for (const [id, mensaje] of requeridos) {
    if (!campo(id).value.trim()) { campo(id).focus(); throw new Error(mensaje); }
  }
  if (telefonoNormalizado(campo('responsableTelefono').value) === telefonoNormalizado(campo('parejaTelefono').value)) {
    campo('parejaTelefono').focus();
    throw new Error('Los integrantes deben tener teléfonos diferentes.');
  }
  if (campo('comentariosInscripcion').value.trim().length > 500) throw new Error('Los comentarios no pueden superar 500 caracteres.');
}

async function cargar() {
  const contenedor = document.getElementById('listaTorneos');
  if (!contenedor) return;
  contenedor.innerHTML = '<div class="surface-card p-8 text-slate-400">Cargando torneos...</div>';
  try {
    const resumenes = await api<TorneoResumen[]>('/torneos');
    if (!resumenes.length) { contenedor.innerHTML = '<div class="surface-card p-8 text-slate-400">No hay torneos publicados.</div>'; return; }
    const detalles = await Promise.all(resumenes.map(torneo => api<TorneoDetalle>(`/torneos/${torneo.id}`)));
    contenedor.innerHTML = detalles.map(tarjeta).join('');
  } catch (error) {
    contenedor.innerHTML = `<div class="surface-card p-8"><p class="text-rose-300">${escapeHtml(error instanceof Error ? error.message : 'No se pudieron cargar los torneos.')}</p><button id="reintentarTorneos" class="mt-4 rounded-lg border border-brand-300 px-4 py-2 font-bold">Reintentar</button></div>`;
    document.getElementById('reintentarTorneos')?.addEventListener('click', cargar, { once: true });
  }
}

document.getElementById('listaTorneos')?.addEventListener('click', evento => {
  const boton = (evento.target as HTMLElement).closest<HTMLButtonElement>('.inscribir-pareja');
  if (boton && !boton.disabled) abrirInscripcion(boton);
});

document.getElementById('cerrarInscripcion')?.addEventListener('click', cerrarInscripcion);
document.getElementById('cancelarInscripcion')?.addEventListener('click', cerrarInscripcion);
document.addEventListener('keydown', evento => { if (evento.key === 'Escape' && !modal.classList.contains('hidden')) cerrarInscripcion(); });

campo('comentariosInscripcion').addEventListener('input', () => {
  document.getElementById('contadorComentarios')!.textContent = `${campo('comentariosInscripcion').value.length} / 500`;
});

formulario.addEventListener('submit', async evento => {
  evento.preventDefault();
  errorCaja.classList.add('hidden');
  exitoCaja.classList.add('hidden');
  try {
    validar();
    enviar.disabled = true;
    enviar.textContent = 'Enviando...';
    const creada = await api<SolicitudCreada>('/torneos/inscripciones', {
      method: 'POST',
      body: JSON.stringify({
        torneoCategoriaId: Number(campo('torneoCategoriaId').value),
        responsable: { nombre: campo('responsableNombre').value.trim(), apellido: campo('responsableApellido').value.trim(), telefono: campo('responsableTelefono').value.trim() },
        pareja: { nombre: campo('parejaNombre').value.trim(), apellido: campo('parejaApellido').value.trim(), telefono: campo('parejaTelefono').value.trim() },
        comentarios: campo('comentariosInscripcion').value.trim() || null,
      }),
    });
    exitoCaja.innerHTML = `<strong class="block text-xl">Solicitud enviada</strong><span class="mt-3 block">Número de solicitud: <b>#${creada.inscripcionId}</b></span><span class="block">Estado: <b>${escapeHtml(etiqueta(creada.estado))}</b></span><p class="mt-3 text-emerald-200/80">${escapeHtml(creada.mensaje)}</p>`;
    exitoCaja.classList.remove('hidden');
    enviar.classList.add('hidden');
    await cargar();
  } catch (error) {
    errorCaja.textContent = error instanceof Error ? error.message : 'No se pudo enviar la inscripción.';
    errorCaja.classList.remove('hidden');
    enviar.disabled = false;
    enviar.textContent = 'Enviar inscripción';
  }
});

cargar();