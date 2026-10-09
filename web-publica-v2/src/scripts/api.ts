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
    if (error instanceof DOMException && error.name === 'AbortError') throw new Error('La consulta demoró demasiado. Intentá nuevamente.');
    throw error;
  } finally { window.clearTimeout(temporizador); }
}

export interface Complejo {
  nombreComercial: string; direccion?: string; telefono?: string; whatsapp?: string; email?: string; instagram?: string;
  moneda: string; porcentajeSenia: number; anticipacionMinimaHoras: number; minutosReservaPendiente: number;
  colorPrincipal?: string; pagoAlias?: string; pagoTitular?: string; pagoEntidad?: string; pagoInstrucciones?: string;
  pagoTransferenciaDisponible: boolean;
}
export interface Cancha { id:number; nombre:string; descripcion?:string; tipo?:string; superficie?:string; tieneIluminacion:boolean; horaApertura:string; horaCierre:string; duracionMinutos:number; precio:number; importeSenia:number; diasDisponibles:string[]; }
export interface CategoriaTorneo { id:number; nombre:string; rama:string; cupoParejas:number; parejasConfirmadas:number; cuposDisponibles:number; precioInscripcion:number; premioCampeon?:number|null; premioSubcampeon?:number|null; premioDescripcion?:string|null; disponible:boolean; }
export interface TorneoResumen { id:number; nombre:string; descripcion?:string; fechaInicio:string; fechaFin:string; inscripcionDesde:string; inscripcionHasta:string; estado:string; inscripcionDisponible:boolean; cantidadCategorias:number; }
export interface TorneoDetalle extends Omit<TorneoResumen,'cantidadCategorias'> { reglamento?:string; categorias:CategoriaTorneo[]; }
export interface ParejaCuadroPublica { inscripcionId:number; jugadores:string[]; }
export interface SetCuadroPublico { numero:number; tipo:string; puntosPareja1:number; puntosPareja2:number; }
export interface PartidoCuadroPublico { id:number; fase:string; orden:number; estado:string; bye:boolean; pareja1?:ParejaCuadroPublica|null; pareja2?:ParejaCuadroPublica|null; ganadoraInscripcionId?:number|null; resultado?:string|null; sets:SetCuadroPublico[]; fecha?:string|null; horaInicio?:string|null; horaFin?:string|null; canchaId?:number|null; cancha?:string|null; fechaFinalizacion?:string|null; }
export interface FaseCuadroPublica { nombre:string; partidos:PartidoCuadroPublico[]; }
export interface CampeonaCuadroPublica extends ParejaCuadroPublica { resultadoFinal?:string|null; }
export interface CuadroCategoriaPublico { torneo:{id:number;nombre:string;estado:string;fechaInicio:string;fechaFin:string}; categoria:{id:number;nombre:string;rama:string}; campeona?:CampeonaCuadroPublica|null; fases:FaseCuadroPublica[]; }
