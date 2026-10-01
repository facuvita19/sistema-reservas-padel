const API_BASE = '/api/publica';

export async function api<T>(ruta: string, opciones: RequestInit = {}): Promise<T> {
  const controlador = new AbortController();
  const temporizador = window.setTimeout(() => controlador.abort(), 15000);
  try {
    const respuesta = await fetch(`${API_BASE}${ruta}`, {
      credentials: 'include',
      headers: { 'Content-Type': 'application/json', ...(opciones.headers || {}) },
      ...opciones,
      signal: controlador.signal,
    });
    let datos: unknown;
    try { datos = await respuesta.json(); }
    catch { datos = { mensaje: 'La respuesta del servidor no es válida.' }; }
    if (!respuesta.ok) {
      const mensaje = typeof datos === 'object' && datos && 'mensaje' in datos
        ? String((datos as { mensaje: unknown }).mensaje)
        : 'No se pudo completar la operación.';
      throw new Error(mensaje);
    }
    return datos as T;
  } catch (error) {
    if (error instanceof DOMException && error.name === 'AbortError') {
      throw new Error('La consulta demoró demasiado. Intentá nuevamente.');
    }
    throw error;
  } finally {
    window.clearTimeout(temporizador);
  }
}

export interface Complejo {
  nombreComercial: string;
  direccion?: string;
  telefono?: string;
  whatsapp?: string;
  email?: string;
  instagram?: string;
  moneda: string;
  porcentajeSenia: number;
  anticipacionMinimaHoras: number;
  minutosReservaPendiente: number;
  colorPrincipal?: string;
  pagoTransferenciaDisponible: boolean;
}

export interface Cancha {
  id: number;
  nombre: string;
  descripcion?: string;
  tipo?: string;
  superficie?: string;
  tieneIluminacion: boolean;
  horaApertura: string;
  horaCierre: string;
  duracionMinutos: number;
  precio: number;
  importeSenia: number;
  diasDisponibles: string[];
}

export interface CategoriaTorneo {
  id: number;
  nombre: string;
  rama: string;
  cupoParejas: number;
  parejasConfirmadas: number;
  cuposDisponibles: number;
  precioInscripcion: number;
  disponible: boolean;
}

export interface TorneoResumen {
  id: number;
  nombre: string;
  descripcion?: string;
  fechaInicio: string;
  fechaFin: string;
  inscripcionDesde: string;
  inscripcionHasta: string;
  estado: string;
  inscripcionDisponible: boolean;
  cantidadCategorias: number;
}

export interface TorneoDetalle extends Omit<TorneoResumen, 'cantidadCategorias'> {
  reglamento?: string;
  categorias: CategoriaTorneo[];
}