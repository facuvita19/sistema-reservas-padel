import { api, type Complejo } from './api';
import { etiqueta, fecha, fechaHora, hora, moneda } from './formatos';

type Reserva = {
  reservaId?: number; solicitudId?: number; codigoSeguimiento: string; estado: string; cancha: string;
  fecha: string; horaInicio: string; horaFin: string; precioTotal: number; totalAcreditado: number;
  importeSenia: number; saldoPendiente: number; vencimiento?: string | null; fechaExpiracion?: string | null;
  fechaCancelacion?: string | null; pendiente: boolean; confirmada: boolean; expirada: boolean; moneda: string; mensaje: string;
};
type Historial = { proximas: Reserva[]; anteriores: Reserva[]; total: number };
type Perfil = { clienteId: number; nombre: string; apellido: string; documento: string; telefono: string; email?: string };

const el = <T extends HTMLElement>(id: string) => document.getElementById(id) as T;
let complejo: Complejo | null = null;

const escapeHtml = (valor: unknown) => String(valor ?? '').replace(/[&<>"']/g, c => ({ '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;' }[c] || c));

function estadoVisual(reserva: Reserva) {
  if (reserva.confirmada) return { texto: 'Confirmada', clase: 'border-emerald-500/30 bg-emerald-500/10 text-emerald-300' };
  if (reserva.expirada || reserva.estado === 'EXPIRADA') return { texto: 'Expirada', clase: 'border-slate-500/30 bg-slate-500/10 text-slate-300' };
  if (reserva.estado === 'CANCELADA') return { texto: 'Cancelada', clase: 'border-rose-500/30 bg-rose-500/10 text-rose-300' };
  if (reserva.pendiente) return { texto: 'Pendiente de seña', clase: 'border-amber-500/30 bg-amber-500/10 text-amber-200' };
  return { texto: etiqueta(reserva.estado), clase: 'border-brand-500/30 bg-brand-500/10 text-brand-300' };
}

function pagoHtml(reserva: Reserva) {
  if (!reserva.pendiente || !complejo?.pagoTransferenciaDisponible) return '';
  const whatsapp = (complejo.whatsapp || '').replace(/\D/g, '');
  const texto = encodeURIComponent(`Hola, informo el pago de la seña. Solicitud #${reserva.solicitudId || reserva.reservaId}. ${reserva.cancha}, ${reserva.fecha}, ${hora(reserva.horaInicio)}. Importe: ${moneda(reserva.importeSenia, reserva.moneda)}.`);
  return `<div class="mt-6 rounded-2xl border border-brand-500/30 bg-brand-500/10 p-5">
    <h3 class="text-lg font-black">Datos para acreditar la seña</h3>
    <div class="mt-4 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
      <div><small class="text-slate-400">Importe</small><strong class="block">${moneda(reserva.importeSenia, reserva.moneda)}</strong></div>
      <div><small class="text-slate-400">Alias</small><strong class="block">${escapeHtml(complejo.pagoAlias || '-')}</strong></div>
      <div><small class="text-slate-400">Titular</small><strong class="block">${escapeHtml(complejo.pagoTitular || '-')}</strong></div>
      <div><small class="text-slate-400">Entidad</small><strong class="block">${escapeHtml(complejo.pagoEntidad || '-')}</strong></div>
    </div>
    <div class="mt-5 flex flex-wrap gap-3"><button class="copiar-codigo rounded-lg border border-brand-300 px-4 py-2 font-black" data-copiar="${escapeHtml(complejo.pagoAlias || '')}" type="button">Copiar alias</button>${whatsapp ? `<a class="rounded-lg bg-emerald-600 px-4 py-2 font-black text-white hover:bg-emerald-500" target="_blank" rel="noopener" href="https://wa.me/${whatsapp}?text=${texto}">Informar por WhatsApp</a>` : ''}</div>
  </div>`;
}

function tarjeta(reserva: Reserva, compacta = false) {
  const estado = estadoVisual(reserva);
  return `<article class="surface-card p-6 sm:p-7">
    <div class="flex flex-col gap-5 sm:flex-row sm:items-start sm:justify-between"><div><span class="inline-flex rounded-full border px-3 py-1.5 text-xs font-black ${estado.clase}">${estado.texto}</span><h3 class="mt-4 text-2xl font-black">${escapeHtml(reserva.cancha)}</h3><p class="mt-2 text-slate-400">${fecha(reserva.fecha)} · ${hora(reserva.horaInicio)} a ${hora(reserva.horaFin)}</p></div><div class="text-left sm:text-right"><small class="text-slate-500">Código</small><strong class="block font-mono text-sm">${escapeHtml(reserva.codigoSeguimiento)}</strong><button class="copiar-codigo mt-2 text-xs font-black text-brand-300" data-copiar="${escapeHtml(reserva.codigoSeguimiento)}" type="button">Copiar código</button></div></div>
    <div class="mt-6 grid gap-4 border-t border-ink-700 pt-5 sm:grid-cols-2 lg:grid-cols-4"><div><small class="text-slate-500">Total</small><strong class="block">${moneda(reserva.precioTotal, reserva.moneda)}</strong></div><div><small class="text-slate-500">Acreditado</small><strong class="block">${moneda(reserva.totalAcreditado, reserva.moneda)}</strong></div><div><small class="text-slate-500">Seña</small><strong class="block">${moneda(reserva.importeSenia, reserva.moneda)}</strong></div><div><small class="text-slate-500">Saldo</small><strong class="block">${moneda(reserva.saldoPendiente, reserva.moneda)}</strong></div></div>
    ${reserva.vencimiento && reserva.pendiente ? `<p class="mt-4 text-sm text-amber-200">Vencimiento de la reserva temporal: ${fechaHora(reserva.vencimiento)}</p>` : ''}
    <p class="mt-4 text-sm text-slate-400">${escapeHtml(reserva.mensaje || '')}</p>
    ${compacta ? '' : pagoHtml(reserva)}
  </article>`;
}

function enlazarCopias(contenedor: HTMLElement) {
  contenedor.querySelectorAll<HTMLButtonElement>('.copiar-codigo').forEach(b => b.addEventListener('click', async () => {
    try {
      await navigator.clipboard.writeText(b.dataset.copiar || '');
      const original = b.textContent;
      b.textContent = 'Copiado';
      b.classList.remove('error-copia');
      b.classList.add('copiado');
      window.setTimeout(() => {
        b.textContent = original;
        b.classList.remove('copiado');
      }, 1400);
    } catch {
      const original = b.textContent;
      b.textContent = 'No se pudo copiar';
      b.classList.remove('copiado');
      b.classList.add('error-copia');
      window.setTimeout(() => {
        b.textContent = original;
        b.classList.remove('error-copia');
      }, 1600);
    }
  }));
}

async function consultarCodigo(codigo: string) {
  const resultado = el('resultadoSeguimiento');
  resultado.classList.remove('hidden');
  resultado.innerHTML = '<div class="surface-card p-7 text-slate-400">Consultando reserva...</div>';
  try {
    const reserva = await api<Reserva>(`/solicitudes/${encodeURIComponent(codigo)}`);
    localStorage.setItem('padel.solicitud.codigo', codigo);
    resultado.innerHTML = tarjeta(reserva);
    enlazarCopias(resultado);
  } catch (error) {
    resultado.innerHTML = `<div class="surface-card border-rose-500/30 p-7 text-rose-200">${escapeHtml(error instanceof Error ? error.message : 'No se pudo consultar la reserva.')}</div>`;
  }
}

async function cargarCuenta() {
  const estado = el('estadoCuenta');
  try {
    const [perfil, historial] = await Promise.all([api<Perfil>('/cliente/perfil'), api<Historial>('/cliente/reservas')]);
    estado.classList.add('hidden');
    el('modoVisitante').classList.add('hidden');
    el('modoAutenticado').classList.remove('hidden');
    el('consultaCodigoSiempre').classList.remove('hidden');
    el('saludoCliente').textContent = `Reservas de ${perfil.nombre}`;
    const contenedor = el('reservasCuenta');
    contenedor.innerHTML = `${historial.proximas.length ? `<div><h3 class="mb-4 text-xl font-black">Próximas</h3><div class="grid gap-5">${historial.proximas.map(r => tarjeta(r)).join('')}</div></div>` : '<div class="surface-card p-7 text-slate-400">No tenés reservas próximas.</div>'}${historial.anteriores.length ? `<div class="mt-8"><h3 class="mb-4 text-xl font-black">Anteriores</h3><div class="grid gap-5">${historial.anteriores.map(r => tarjeta(r, true)).join('')}</div></div>` : ''}`;
    enlazarCopias(contenedor);
  } catch {
    estado.classList.add('hidden');
    el('modoAutenticado').classList.add('hidden');
    el('consultaCodigoSiempre').classList.add('hidden');
    el('modoVisitante').classList.remove('hidden');
    const guardado = localStorage.getItem('padel.solicitud.codigo');
    if (guardado) { el<HTMLInputElement>('codigoSeguimiento').value = guardado; consultarCodigo(guardado); }
  }
}

el<HTMLFormElement>('formSeguimiento').addEventListener('submit', evento => {
  evento.preventDefault();
  const codigo = el<HTMLInputElement>('codigoSeguimiento').value.trim();
  const error = el('errorSeguimiento');
  if (!codigo) { error.textContent = 'Ingresá el código de seguimiento.'; error.classList.remove('hidden'); return; }
  error.classList.add('hidden');
  consultarCodigo(codigo);
});

el<HTMLFormElement>('formSeguimientoCuenta').addEventListener('submit', evento => {
  evento.preventDefault();
  const codigo = el<HTMLInputElement>('codigoSeguimientoCuenta').value.trim();
  const error = el('errorSeguimientoCuenta');
  if (!codigo) {
    error.textContent = 'Ingresá el código de seguimiento.';
    error.classList.remove('hidden');
    return;
  }
  error.classList.add('hidden');
  consultarCodigo(codigo);
});

el('actualizarReservas').addEventListener('click', cargarCuenta);

(async () => {
  try { complejo = await api<Complejo>('/complejo'); } catch { complejo = null; }
  await cargarCuenta();
})();