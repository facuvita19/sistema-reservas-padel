import { api, type Cancha, type Complejo } from './api';
import { hora, moneda } from './formatos';

const texto = (id: string, valor: string) => { const nodo = document.getElementById(id); if (nodo) nodo.textContent = valor; };

function tarjeta(cancha: Cancha, monedaComplejo: string) {
  return `<article class="surface-card flex flex-col p-6 transition hover:-translate-y-1 hover:border-brand-300">
    <div class="flex items-start justify-between gap-4">
      <div><p class="text-xs font-black uppercase tracking-widest text-brand-300">${cancha.tipo || 'Pádel'}</p><h3 class="mt-2 text-2xl font-black">${cancha.nombre}</h3></div>
      <span class="rounded-full border border-emerald-500/30 bg-emerald-500/10 px-3 py-1 text-xs font-bold text-emerald-300">Activa</span>
    </div>
    <p class="mt-3 text-slate-400">${cancha.descripcion || cancha.superficie || 'Cancha disponible'}</p>
    <div class="mt-5 flex flex-wrap gap-2 text-xs text-slate-300">
      <span class="rounded-full bg-ink-800 px-3 py-1.5">${cancha.superficie || 'Superficie informada'}</span>
      <span class="rounded-full bg-ink-800 px-3 py-1.5">${cancha.duracionMinutos} minutos</span>
      <span class="rounded-full bg-ink-800 px-3 py-1.5">${hora(cancha.horaApertura)} a ${hora(cancha.horaCierre)}</span>
    </div>
    <div class="mt-6 grid grid-cols-2 gap-3 border-t border-ink-700 pt-5">
      <div><small class="text-slate-500">Turno</small><strong class="block text-lg">${moneda(cancha.precio, monedaComplejo)}</strong></div>
      <div><small class="text-slate-500">Seña</small><strong class="block text-lg">${moneda(cancha.importeSenia, monedaComplejo)}</strong></div>
    </div>
    <a class="focus-ring mt-6 rounded-xl bg-brand-500 px-5 py-3 text-center font-black text-white transition hover:bg-brand-600" href="/reservar?cancha=${cancha.id}">Reservar</a>
  </article>`;
}

async function cargar() {
  const contenedor = document.getElementById('canchasDestacadas');
  const estado = document.getElementById('estadoPortada');
  try {
    const [complejo, canchas] = await Promise.all([api<Complejo>('/complejo'), api<Cancha[]>('/canchas')]);
    document.documentElement.style.setProperty('--color-principal', complejo.colorPrincipal || '#527f9c');
    texto('nombreComplejo', complejo.nombreComercial);
    texto('marcaNombre', complejo.nombreComercial);
    texto('direccionComplejo', complejo.direccion || 'Dirección a confirmar');
    texto('datoCanchas', String(canchas.length));
    texto('datoSenia', `${complejo.porcentajeSenia}%`);
    texto('datoTurno', canchas.length ? moneda(Math.min(...canchas.map(c => c.precio)), complejo.moneda) : '-');
    if (contenedor) contenedor.innerHTML = canchas.slice(0, 3).map(c => tarjeta(c, complejo.moneda)).join('');
    if (estado) estado.remove();
  } catch (error) {
    if (estado) estado.innerHTML = `<p class="text-rose-300">${error instanceof Error ? error.message : 'No se pudieron cargar los datos.'}</p><button id="reintentarPortada" class="mt-4 rounded-lg border border-brand-300 px-4 py-2 font-bold">Reintentar</button>`;
    document.getElementById('reintentarPortada')?.addEventListener('click', cargar, { once: true });
  }
}

cargar();