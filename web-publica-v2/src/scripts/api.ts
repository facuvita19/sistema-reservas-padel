const API_BASE = '/api/publica';

export async function api<T>(ruta: string, opciones: RequestInit = {}): Promise<T> {
  const controlador = new AbortController();
  const señalExterna = opciones.signal;
  let vencio = false;
  const cancelarExternamente = () => controlador.abort(señalExterna?.reason);
  if (señalExterna?.aborted) cancelarExternamente();
  else señalExterna?.addEventListener('abort', cancelarExternamente, { once: true });
  const temporizador = window.setTimeout(() => { vencio = true; controlador.abort(); }, 15000);
  try {
    const respuesta = await fetch(`${API_BASE}${ruta}`, {
      ...opciones,
      credentials: 'include',
      headers: { 'Content-Type': 'application/json', ...(opciones.headers || {}) },
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
      if (vencio) throw new Error('La consulta demoró demasiado. Intentá nuevamente.');
      throw error;
    }
    throw error;
  } finally {
    window.clearTimeout(temporizador);
    señalExterna?.removeEventListener('abort', cancelarExternamente);
  }
}

export interface Complejo {
  nombreComercial: string; direccion?: string; telefono?: string; whatsapp?: string; email?: string; instagram?: string;
  moneda: string; porcentajeSenia: number; anticipacionMinimaHoras: number; minutosReservaPendiente: number;
  colorPrincipal?: string; pagoAlias?: string; pagoTitular?: string; pagoEntidad?: string; pagoInstrucciones?: string;
  pagoTransferenciaDisponible: boolean; pagoOnlineDisponible?: boolean;
}
export interface Cancha { id:number; nombre:string; descripcion?:string; tipo?:string; superficie?:string; tieneIluminacion:boolean; horaApertura:string; horaCierre:string; duracionMinutos:number; precio:number; importeSenia:number; diasDisponibles:string[]; }
export interface CategoriaTorneo { id:number; nombre:string; rama:string; cupoParejas:number; parejasConfirmadas:number; cuposDisponibles:number; precioInscripcion:number; premioCampeon?:number|null; premioSubcampeon?:number|null; premioDescripcion?:string|null; disponible:boolean; formatoCompetencia:string; cantidadGruposTres:number; cantidadGruposCuatro:number; clasificadosProyectados:number; }
export interface TorneoResumen { id:number; nombre:string; descripcion?:string; fechaInicio:string; fechaFin:string; inscripcionDesde:string; inscripcionHasta:string; estado:string; estadoPortal:'PROXIMO'|'EN_CURSO'|'FINALIZADO'; inscripcionDisponible:boolean; cantidadCategorias:number; categoriasDisponibles:number; cupoTotal:number; parejasConfirmadas:number; cuposDisponibles:number; precioMinimo:number; precioMaximo:number; categorias:string[]; ramas:string[]; formatos:string[]; etapaCompetitiva:string; partidosTotales:number; partidosFinalizados:number; partidosCancelados:number; resultadosDisponibles:boolean; campeonesDisponibles:boolean; }
export interface TorneoDetalle extends Omit<TorneoResumen,'cantidadCategorias'|'categorias'> { reglamento?:string; categorias:CategoriaTorneo[]; }
export interface ParejaCuadroPublica { inscripcionId:number; jugadores:string[]; }
export interface SetCuadroPublico { numero:number; tipo:string; puntosPareja1:number; puntosPareja2:number; }
export interface PartidoCuadroPublico { id:number; fase:string; orden:number; estado:string; bye:boolean; pareja1?:ParejaCuadroPublica|null; pareja2?:ParejaCuadroPublica|null; ganadoraInscripcionId?:number|null; resultado?:string|null; sets:SetCuadroPublico[]; fecha?:string|null; horaInicio?:string|null; horaFin?:string|null; canchaId?:number|null; cancha?:string|null; fechaFinalizacion?:string|null; partidoSiguienteId?:number|null; posicionSiguiente?:string|null; }
export interface FaseCuadroPublica { nombre:string; partidos:PartidoCuadroPublico[]; }
export interface CampeonaCuadroPublica extends ParejaCuadroPublica { resultadoFinal?:string|null; }
export interface CuadroCategoriaPublico { torneo:{id:number;nombre:string;estado:string;fechaInicio:string;fechaFin:string}; categoria:{id:number;nombre:string;rama:string}; campeona?:CampeonaCuadroPublica|null; fases:FaseCuadroPublica[]; }

// competencia-torneo-publica-v1
export interface IntegranteGrupoPublico { inscripcionId:number; pareja:string; cabezaSerie:boolean; ordenSorteo:number; }
export interface PosicionGrupoPublica { posicion:number; inscripcionId:number; pareja:string; partidosJugados:number; partidosGanados:number; partidosPerdidos:number; setsGanados:number; setsPerdidos:number; diferenciaSets:number; gamesGanados:number; gamesPerdidos:number; diferenciaGames:number; estado:string; }
export interface PartidoGrupoPublico { id:number; tipo?:string|null; orden:number; estado:string; pareja1?:ParejaCuadroPublica|null; pareja2?:ParejaCuadroPublica|null; ganadoraInscripcionId?:number|null; resultado?:string|null; sets:SetCuadroPublico[]; fecha?:string|null; horaInicio?:string|null; horaFin?:string|null; canchaId?:number|null; cancha?:string|null; fechaFinalizacion?:string|null; }
export interface GrupoTorneoPublico { id:number; nombre:string; orden:number; capacidad:number; modoAsignacion:string; confirmado:boolean; clasifican:number; posicionesDefinitivas:boolean; desempatePendiente:boolean; integrantes:IntegranteGrupoPublico[]; posiciones:PosicionGrupoPublica[]; partidos:PartidoGrupoPublico[]; }
export interface CompetenciaCategoriaPublica { torneo:{id:number;nombre:string;estado:string;fechaInicio:string;fechaFin:string}; categoria:{id:number;nombre:string;rama:string;formatoCompetencia:string;cupoParejas:number;cantidadGruposTres:number;cantidadGruposCuatro:number;clasificadosProyectados:number}; resumen:{etapa:string;gruposConfigurados:boolean;gruposConfirmados:boolean;posicionesDefinitivas:boolean;desempatePendiente:boolean;eliminatoriasGeneradas:boolean;partidosGrupos:number;partidosGruposFinalizados:number;partidosEliminatorios:number;partidosEliminatoriosFinalizados:number}; grupos:GrupoTorneoPublico[]; eliminatorias?:CuadroCategoriaPublico|null; }


// W2.4B.4 contratos compartidos de reserva y pago
export interface SolicitudReservaCreada { solicitudId:number; codigoSeguimiento:string; estado:string; cancha:string; fecha:string; horaInicio:string; horaFin:string; precioTotal:number; importeSenia:number; vencimiento:string; minutosParaPagar:number; moneda:string; mensaje:string; }
export interface OrdenPagoPublica { ordenId?:number; reservaId?:number; proveedor?:'MANUAL'|'MERCADO_PAGO'|string; metodo?:'TRANSFERENCIA_MANUAL'|'PAGO_ONLINE'|string; estado?:string; importe?:number; moneda?:string; fechaExpiracion?:string|null; segundosRestantes?:number; puedePagarOnline?:boolean; puedeInformarTransferencia?:boolean; puedeReintentar?:boolean; requiereActualizacion?:boolean; checkoutUrl?:string|null; mensaje?:string; }

export type MetodoOrdenPago='TRANSFERENCIA_MANUAL'|'PAGO_ONLINE';
export const obtenerOrdenPago=(reservaId:number)=>api<OrdenPagoPublica>(`/cliente/reservas/${reservaId}/pago`);
export const crearOrdenPago=(reservaId:number,metodo:MetodoOrdenPago='TRANSFERENCIA_MANUAL')=>api<OrdenPagoPublica>(`/cliente/reservas/${reservaId}/pago`,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({metodo})});
