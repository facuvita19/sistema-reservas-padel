import { api, type CuadroCategoriaPublico, type PartidoCuadroPublico, type ParejaCuadroPublica } from './api';
import { etiqueta, fecha } from './formatos';

const modal = document.getElementById('modalCuadroTorneo') as HTMLElement | null;
const contenido = document.getElementById('contenidoCuadroTorneo') as HTMLElement | null;
const titulo = document.getElementById('tituloCuadroTorneo') as HTMLElement | null;
const subtitulo = document.getElementById('subtituloCuadroTorneo') as HTMLElement | null;
let ultimoFoco: HTMLElement | null = null;

const escapeHtml = (valor: unknown) => String(valor ?? '').replace(/[&<>"']/g, caracter => ({
  '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;',
}[caracter] || caracter));

function nombres(pareja?: ParejaCuadroPublica | null) {
  return pareja?.jugadores?.length ? pareja.jugadores.map(escapeHtml).join(' / ') : 'Por definir';
}

function programacion(partido: PartidoCuadroPublico) {
  if (partido.bye) return 'Clasificación directa por BYE';
  if (!partido.fecha || !partido.horaInicio) return 'Sin programación';
  const horario = partido.horaFin ? `${partido.horaInicio.slice(0, 5)}–${partido.horaFin.slice(0, 5)}` : partido.horaInicio.slice(0, 5);
  return `${fecha(partido.fecha)} · ${horario}${partido.cancha ? ` · ${escapeHtml(partido.cancha)}` : ''}`;
}

function pareja(partido: PartidoCuadroPublico, lado: 1 | 2) {
  const valor = lado === 1 ? partido.pareja1 : partido.pareja2;
  const ganadora = Boolean(valor && partido.ganadoraInscripcionId === valor.inscripcionId);
  return `<div class="flex items-center justify-between gap-3 rounded-xl border px-3 py-3 ${ganadora ? 'border-amber-400/50 bg-amber-400/10 text-amber-100' : 'border-ink-700 bg-ink-900/70 text-slate-200'}">
    <span class="min-w-0 text-sm font-bold leading-snug">${ganadora ? '<span class="mr-1 text-amber-300">★</span>' : ''}${nombres(valor)}</span>
    ${ganadora ? '<span class="shrink-0 rounded-full bg-amber-300/15 px-2 py-1 text-[10px] font-black uppercase tracking-wide text-amber-200">Ganadora</span>' : ''}
  </div>`;
}

function partido(partido: PartidoCuadroPublico) {
  const finalizado = partido.estado === 'FINALIZADO';
  return `<article class="w-[290px] shrink-0 rounded-2xl border ${finalizado ? 'border-brand-500/45' : 'border-ink-700'} bg-ink-850 p-4 shadow-xl shadow-black/10">
    <div class="flex items-center justify-between gap-3">
      <span class="text-[10px] font-black uppercase tracking-[.16em] text-brand-300">${escapeHtml(etiqueta(partido.fase))} #${partido.orden}</span>
      <span class="rounded-full border border-ink-700 bg-ink-900 px-2 py-1 text-[10px] font-black uppercase text-slate-400">${escapeHtml(etiqueta(partido.estado))}</span>
    </div>
    <div class="mt-4 space-y-2">${pareja(partido, 1)}${pareja(partido, 2)}</div>
    <div class="mt-4 border-t border-ink-700 pt-3">
      <strong class="block text-base font-black ${partido.resultado ? 'text-white' : 'text-slate-500'}">${escapeHtml(partido.resultado || 'Sin resultado')}</strong>
      <small class="mt-2 block leading-relaxed text-slate-500">${programacion(partido)}</small>
    </div>
  </article>`;
}

function render(cuadro: CuadroCategoriaPublico) {
  if (titulo) titulo.textContent = `${cuadro.categoria.nombre} · ${etiqueta(cuadro.categoria.rama)}`;
  if (subtitulo) subtitulo.textContent = `${cuadro.torneo.nombre} · ${fecha(cuadro.torneo.fechaInicio)} al ${fecha(cuadro.torneo.fechaFin)}`;
  const campeona = cuadro.campeona
    ? `<section class="mb-7 flex flex-col gap-4 rounded-2xl border border-amber-400/35 bg-gradient-to-r from-amber-400/10 to-transparent p-5 sm:flex-row sm:items-center sm:justify-between">
        <div><p class="text-xs font-black uppercase tracking-[.18em] text-amber-300">Campeona de la categoría</p><h3 class="mt-2 text-xl font-black text-amber-50">${nombres(cuadro.campeona)}</h3><p class="mt-1 text-sm text-amber-100/65">Final: ${escapeHtml(cuadro.campeona.resultadoFinal || 'Sin resultado')}</p></div>
        <div class="text-4xl text-amber-300">★</div>
      </section>`
    : `<section class="mb-7 rounded-2xl border border-ink-700 bg-ink-850 p-5"><p class="text-xs font-black uppercase tracking-[.18em] text-brand-300">Campeona por definir</p><p class="mt-2 text-sm text-slate-400">La categoría todavía no tiene una final terminada.</p></section>`;
  const fases = cuadro.fases.length
    ? `<div class="flex min-w-max items-stretch gap-6">${cuadro.fases.map(fase => `<section class="flex min-w-[290px] flex-col"><div class="mb-3 flex items-center justify-between"><h3 class="text-sm font-black uppercase tracking-[.16em] text-slate-300">${escapeHtml(etiqueta(fase.nombre))}</h3><span class="text-xs text-slate-500">${fase.partidos.length} partido(s)</span></div><div class="flex flex-1 flex-col justify-around gap-5">${fase.partidos.map(partido).join('')}</div></section>`).join('')}</div>`
    : `<div class="rounded-2xl border border-dashed border-ink-700 p-8 text-center text-slate-400">El cuadro todavía no fue generado para esta categoría.</div>`;
  if (contenido) contenido.innerHTML = `${campeona}<div class="overflow-x-auto pb-3">${fases}</div>`;
}

export async function abrirCuadroTorneo(categoriaId: number, disparador?: HTMLElement) {
  if (!modal || !contenido) return;
  ultimoFoco = disparador || null;
  contenido.innerHTML = '<div class="rounded-2xl border border-ink-700 bg-ink-850 p-8 text-slate-400">Cargando cuadro...</div>';
  modal.classList.remove('hidden');
  modal.classList.add('flex');
  document.body.style.overflow = 'hidden';
  try {
    render(await api<CuadroCategoriaPublico>(`/torneos/categorias/${categoriaId}/cuadro`));
  } catch (error) {
    contenido.innerHTML = `<div class="rounded-2xl border border-rose-500/30 bg-rose-500/10 p-6 text-rose-200">${escapeHtml(error instanceof Error ? error.message : 'No se pudo cargar el cuadro.')}</div>`;
  }
}

function cerrar() {
  if (!modal) return;
  modal.classList.add('hidden');
  modal.classList.remove('flex');
  document.body.style.overflow = '';
  ultimoFoco?.focus();
}

document.getElementById('cerrarCuadroTorneo')?.addEventListener('click', cerrar);
modal?.addEventListener('click', evento => { if (evento.target === modal) cerrar(); });
document.addEventListener('keydown', evento => { if (evento.key === 'Escape' && modal && !modal.classList.contains('hidden')) cerrar(); });
