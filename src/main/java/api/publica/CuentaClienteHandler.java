package api.publica;

import java.io.IOException;
import java.io.OutputStream;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

import api.publica.CuentaClienteDTO.ActualizarPerfil;
import api.publica.CuentaClienteDTO.CambiarPassword;
import api.publica.CuentaClienteDTO.ActualizarPreferenciasDeportivas;
import api.publica.PreferenciasComunidadDTO.Actualizar;
import servicio.PreferenciasComunidadService;
import servicio.PreferenciasComunidadService.ConflictoPreferenciasComunidadException;
import servicio.IdentidadDeportivaService;
import servicio.AutenticacionClienteService;
import servicio.AutenticacionClienteService.SesionAutenticada;
import servicio.CuentaClienteService;
import api.publica.PagoClienteDTO.OrdenPagoPublica;
import servicio.CuentaClienteService.ConflictoPerfilException;
import servicio.CuentaClienteService.RecursoClienteNoEncontradoException;

public final class CuentaClienteHandler {

    private static final String PREFIJO = "/api/publica/cliente";

    private final AutenticacionClienteService autenticacionService;
    private final CuentaClienteService cuentaService;
    private final ObjectMapper mapper;
    private final PagoClienteCuentaService pagoCuentaService;
    private final JugadorPublicoService jugadorPublicoService;
    private final IdentidadDeportivaService identidadDeportivaService;
    private final PreferenciasComunidadService preferenciasComunidadService;
    private final ConvocatoriaClienteService convocatoriaClienteService;
    private final servicio.GestionSolicitudesComunidadService gestionSolicitudesComunidadService;
    private final DetalleConvocatoriaComunidadService detalleConvocatoriaComunidadService;
    private final servicio.SolicitudComunidadService solicitudComunidadService;
    private final servicio.InvitacionComunidadService invitacionComunidadService;

    public CuentaClienteHandler(
            AutenticacionClienteService autenticacionService,
            CuentaClienteService cuentaService,
            ObjectMapper mapper) {
        if (autenticacionService == null
                || cuentaService == null
                || mapper == null) {
            throw new IllegalArgumentException(
                    "Las dependencias del handler no pueden ser nulas.");
        }
        this.autenticacionService = autenticacionService;
        this.cuentaService = cuentaService;
        this.mapper = mapper;
        this.pagoCuentaService = new PagoClienteCuentaService();
        this.jugadorPublicoService = new JugadorPublicoService();
        this.identidadDeportivaService = new IdentidadDeportivaService();
        this.preferenciasComunidadService = new PreferenciasComunidadService();
        this.convocatoriaClienteService = new ConvocatoriaClienteService();
        this.gestionSolicitudesComunidadService =
                new servicio.GestionSolicitudesComunidadService();
        this.detalleConvocatoriaComunidadService =
                new DetalleConvocatoriaComunidadService();
        this.solicitudComunidadService =
                new servicio.SolicitudComunidadService();
        this.invitacionComunidadService = new servicio.InvitacionComunidadService();
    }

    public boolean puedeProcesar(String ruta) {
        return ruta != null
                && (ruta.equals(PREFIJO)
                        || ruta.startsWith(PREFIJO + "/"));
    }

    public void procesar(
            HttpExchange intercambio,
            String ruta,
            String metodo,
            String operacionId) throws IOException {
        try {
            SesionAutenticada autenticada = autenticar(intercambio);

            if (ruta.equals(PREFIJO + "/comunidad/invitaciones/recibidas") && "GET".equals(metodo)) { responder(intercambio,200,invitacionComunidadService.recibidas(autenticada.cliente().getId())); return; }
            if (ruta.equals(PREFIJO + "/comunidad/invitaciones/enviadas") && "GET".equals(metodo)) { responder(intercambio,200,invitacionComunidadService.enviadas(autenticada.cliente().getId())); return; }
            if (ruta.equals(PREFIJO + "/comunidad/invitaciones") && "POST".equals(metodo)) { var e=mapper.readValue(intercambio.getRequestBody(),api.publica.InvitacionComunidadDTO.Enviar.class); responder(intercambio,201,invitacionComunidadService.enviar(autenticada.cliente().getId(),e.perfilDestinatarioId(),e.objetivo(),e.mensaje())); return; }
            String invitacionBase=PREFIJO+"/comunidad/invitaciones/";
            if (ruta.startsWith(invitacionBase) && "POST".equals(metodo)) { String resto=ruta.substring(invitacionBase.length()); int barra=resto.indexOf('/'); if(barra>0){long id=parsearId(resto.substring(0,barra));String accion=resto.substring(barra+1);Object resultado=switch(accion){case "aceptar"->invitacionComunidadService.aceptar(autenticada.cliente().getId(),id);case "rechazar"->invitacionComunidadService.rechazar(autenticada.cliente().getId(),id);case "cancelar"->invitacionComunidadService.cancelar(autenticada.cliente().getId(),id);default->null;};if(resultado!=null){responder(intercambio,200,resultado);return;}} }

            if (ruta.equals(PREFIJO + "/perfil")
                    && "GET".equals(metodo)) {
                responder(intercambio, 200,
                        cuentaService.obtenerPerfil(
                                autenticada.cliente()));
                return;
            }

            if (ruta.equals(PREFIJO + "/perfil-deportivo")
                    && "GET".equals(metodo)) {
                responder(intercambio, 200,
                        jugadorPublicoService.buscarPorCliente(
                                autenticada.cliente().getId()));
                return;
            }

            if (ruta.equals(PREFIJO + "/perfil-deportivo/preferencias")
                    && "PUT".equals(metodo)) {
                actualizarPreferenciasDeportivas(intercambio, autenticada);
                return;
            }

            if (ruta.equals(PREFIJO + "/comunidad/preferencias")
                    && "GET".equals(metodo)) {
                responder(intercambio, 200, preferenciasComunidadService.obtener(
                        autenticada.cliente().getId()));
                return;
            }

            if (ruta.equals(PREFIJO + "/comunidad/preferencias")
                    && "PUT".equals(metodo)) {
                Actualizar entrada = mapper.readValue(
                        intercambio.getRequestBody(), Actualizar.class);
                responder(intercambio, 200, preferenciasComunidadService.actualizar(
                        autenticada.cliente().getId(), entrada));
                return;
            }

            String gestionConvocatoriaBase =
                    PREFIJO + "/comunidad/convocatorias/";
            if (ruta.startsWith(gestionConvocatoriaBase)
                    && ruta.endsWith("/solicitudes")
                    && "GET".equals(metodo)) {
                String valor = ruta.substring(
                        gestionConvocatoriaBase.length(),
                        ruta.length() - "/solicitudes".length());
                responder(intercambio, 200,
                        gestionSolicitudesComunidadService.listar(
                                autenticada.cliente().getId(),
                                parsearId(valor)));
                return;
            }

            String gestionSolicitudBase =
                    PREFIJO + "/comunidad/solicitudes/";
            if (ruta.startsWith(gestionSolicitudBase)
                    && ruta.endsWith("/aceptar")
                    && "POST".equals(metodo)) {
                String valor = ruta.substring(
                        gestionSolicitudBase.length(),
                        ruta.length() - "/aceptar".length());
                responder(intercambio, 200,
                        gestionSolicitudesComunidadService.aceptar(
                                autenticada.cliente().getId(),
                                parsearId(valor)));
                return;
            }

            if (ruta.startsWith(gestionSolicitudBase)
                    && ruta.endsWith("/rechazar")
                    && "POST".equals(metodo)) {
                String valor = ruta.substring(
                        gestionSolicitudBase.length(),
                        ruta.length() - "/rechazar".length());
                responder(intercambio, 200,
                        gestionSolicitudesComunidadService.rechazar(
                                autenticada.cliente().getId(),
                                parsearId(valor)));
                return;
            }

            String solicitudConvocatoriaBase =
                    PREFIJO + "/comunidad/convocatorias/";
            if (ruta.startsWith(solicitudConvocatoriaBase)
                    && ruta.endsWith("/mi-estado")
                    && "GET".equals(metodo)) {
                String valor = ruta.substring(
                        solicitudConvocatoriaBase.length(),
                        ruta.length() - "/mi-estado".length());
                responder(intercambio, 200,
                        detalleConvocatoriaComunidadService.contexto(
                                autenticada.cliente().getId(),
                                parsearId(valor)));
                return;
            }

            if (ruta.startsWith(solicitudConvocatoriaBase)
                    && ruta.endsWith("/solicitudes")
                    && "POST".equals(metodo)) {
                String valor = ruta.substring(
                        solicitudConvocatoriaBase.length(),
                        ruta.length() - "/solicitudes".length());
                ConvocatoriaPublicaDTO.CrearSolicitud entrada =
                        mapper.readValue(intercambio.getRequestBody(),
                                ConvocatoriaPublicaDTO.CrearSolicitud.class);
                responder(intercambio, 201,
                        detalleConvocatoriaComunidadService.solicitar(
                                autenticada.cliente().getId(),
                                parsearId(valor), entrada.mensaje()));
                return;
            }

            String retiroSolicitudBase =
                    PREFIJO + "/comunidad/solicitudes/";
            if (ruta.startsWith(retiroSolicitudBase)
                    && ruta.endsWith("/retirar")
                    && "POST".equals(metodo)) {
                String valor = ruta.substring(
                        retiroSolicitudBase.length(),
                        ruta.length() - "/retirar".length());
                responder(intercambio, 200,
                        detalleConvocatoriaComunidadService.retirar(
                                autenticada.cliente().getId(),
                                parsearId(valor)));
                return;
            }

            if (ruta.equals(PREFIJO + "/comunidad/convocatorias") && "GET".equals(metodo)) {
                responder(intercambio,200,convocatoriaClienteService.listar(autenticada.cliente().getId()));
                return;
            }

            String convocatoriaReservaBase = PREFIJO + "/reservas/";
            if (ruta.startsWith(convocatoriaReservaBase) && ruta.endsWith("/convocatoria")) {
                String valor=ruta.substring(convocatoriaReservaBase.length(),ruta.length()-"/convocatoria".length());
                long reservaId=parsearId(valor);
                if ("GET".equals(metodo)) {
                    var convocatoria=convocatoriaClienteService.buscarPorReserva(autenticada.cliente().getId(),reservaId);
                    responder(intercambio,200,convocatoria==null?new ConvocatoriaClienteDTO.SinConvocatoria(reservaId,"La reserva no tiene una convocatoria activa."):convocatoria);
                    return;
                }
                if ("POST".equals(metodo)) {
                    var entrada=mapper.readValue(intercambio.getRequestBody(),ConvocatoriaClienteDTO.Publicar.class);
                    responder(intercambio,201,convocatoriaClienteService.publicar(autenticada.cliente().getId(),reservaId,entrada));
                    return;
                }
            }

            String cerrarBase=PREFIJO+"/comunidad/convocatorias/";
            if (ruta.startsWith(cerrarBase) && ruta.endsWith("/cerrar") && "POST".equals(metodo)) {
                String valor=ruta.substring(cerrarBase.length(),ruta.length()-"/cerrar".length());
                long convocatoriaId=parsearId(valor);
                byte[] cuerpo=intercambio.getRequestBody().readAllBytes();
                var entrada=cuerpo.length==0?new ConvocatoriaClienteDTO.Cerrar(null):mapper.readValue(cuerpo,ConvocatoriaClienteDTO.Cerrar.class);
                responder(intercambio,200,convocatoriaClienteService.cerrar(autenticada.cliente().getId(),convocatoriaId,entrada));
                return;
            }

            if (ruta.equals(PREFIJO + "/perfil")
                    && "PUT".equals(metodo)) {
                actualizarPerfil(intercambio, autenticada);
                return;
            }

            // cambiarPasswordClienteEndpointV1
            if (ruta.equals(PREFIJO + "/password")
                    && "PUT".equals(metodo)) {
                cambiarPassword(intercambio, autenticada);
                return;
            }

            if (ruta.equals(PREFIJO + "/pagos/capacidades") && "GET".equals(metodo)) {
                responder(intercambio, 200, pagoCuentaService.capacidades());
                return;
            }

            String pagoBase = PREFIJO + "/reservas/";
            if (ruta.startsWith(pagoBase) && ruta.endsWith("/pago")) {
                String valor = ruta.substring(pagoBase.length(), ruta.length() - "/pago".length());
                long reservaId = parsearId(valor);
                if ("GET".equals(metodo)) {
                    OrdenPagoPublica actual = pagoCuentaService.actual(autenticada.cliente().getId(), reservaId);
                    responder(intercambio, 200, actual == null ? new SinOrdenPago("La reserva todavía no tiene una orden de pago.") : actual);
                    return;
                }
                if ("POST".equals(metodo)) {
                    byte[] cuerpo = intercambio.getRequestBody().readAllBytes();
                    PagoClienteDTO.CrearOrdenPago entrada = cuerpo.length == 0 ? new PagoClienteDTO.CrearOrdenPago("TRANSFERENCIA_MANUAL") : mapper.readValue(cuerpo, PagoClienteDTO.CrearOrdenPago.class);
                    responder(intercambio, 201, pagoCuentaService.crear(autenticada.cliente().getId(), reservaId, entrada.metodo()));
                    return;
                }
            }

            if (ruta.startsWith(pagoBase) && ruta.endsWith("/pagos") && "GET".equals(metodo)) {
                String valor = ruta.substring(pagoBase.length(), ruta.length() - "/pagos".length());
                responder(intercambio, 200, pagoCuentaService.historial(autenticada.cliente().getId(), parsearId(valor)));
                return;
            }

            if (!"GET".equals(metodo)) {
                error(intercambio, 405, "METODO_NO_PERMITIDO",
                        "El metodo solicitado no esta permitido.",
                        operacionId);
                return;
            }

            if (ruta.equals(PREFIJO + "/reservas")) {
                responder(intercambio, 200,
                        cuentaService.listarReservas(
                                autenticada.cliente().getId()));
                return;
            }

            String prefijoDetalle = PREFIJO + "/reservas/";
            if (ruta.startsWith(prefijoDetalle)) {
                long reservaId = parsearId(
                        ruta.substring(prefijoDetalle.length()));
                responder(intercambio, 200,
                        cuentaService.buscarReserva(
                                autenticada.cliente().getId(),
                                reservaId));
                return;
            }

            error(intercambio, 404, "RECURSO_NO_ENCONTRADO",
                    "El recurso de la cuenta no existe.",
                    operacionId);
        } catch (NoAutenticadoException exception) {
            CookieSesionCliente.eliminar(
                    intercambio,
                    CookieSesionCliente.debeSerSegura());
            error(intercambio, 401, "NO_AUTENTICADO",
                    exception.getMessage(), operacionId);
        } catch (JugadorPublicoService.PerfilPublicoNoEncontradoException exception) {
            error(intercambio, 404, "PERFIL_DEPORTIVO_NO_VINCULADO",
                    "Tu cuenta todavía no tiene un perfil deportivo vinculado.",
                    operacionId);
        } catch (RecursoClienteNoEncontradoException exception) {
            error(intercambio, 404, "RECURSO_NO_ENCONTRADO",
                    exception.getMessage(), operacionId);
        } catch (ConflictoPerfilException exception) {
            error(intercambio, 409, "CONFLICTO_PERFIL",
                    exception.getMessage(), operacionId);
        } catch (ConflictoPreferenciasComunidadException exception) {
            error(intercambio, 409, "CONFLICTO_PREFERENCIAS_COMUNIDAD",
                    exception.getMessage(), operacionId);
        } catch (servicio.ConvocatoriaComunidadService.ConvocatoriaAjenaException exception) {
            error(intercambio,403,"CONVOCATORIA_AJENA",exception.getMessage(),operacionId);
        } catch (servicio.ConvocatoriaComunidadService.ConvocatoriaNoEncontradaException exception) {
            error(intercambio,404,"CONVOCATORIA_NO_ENCONTRADA",exception.getMessage(),operacionId);
        } catch (servicio.ConvocatoriaComunidadService.ConflictoConvocatoriaActivaException | servicio.ConvocatoriaComunidadService.ConvocatoriaNoAdmiteCambiosException | servicio.ConvocatoriaComunidadService.ReservaNoPublicableException exception) {
            error(intercambio,409,"CONFLICTO_CONVOCATORIA",exception.getMessage(),operacionId);
        } catch (servicio.GestionSolicitudesComunidadService.ConvocatoriaAjenaException exception) {
            error(intercambio, 403, "CONVOCATORIA_AJENA",
                    exception.getMessage(), operacionId);
        } catch (servicio.GestionSolicitudesComunidadService.ConvocatoriaNoEncontradaException
                | servicio.GestionSolicitudesComunidadService.SolicitudNoEncontradaException exception) {
            error(intercambio, 404, "SOLICITUD_COMUNIDAD_NO_ENCONTRADA",
                    exception.getMessage(), operacionId);
        } catch (servicio.GestionSolicitudesComunidadService.ConvocatoriaNoAdmiteGestionException
                | servicio.GestionSolicitudesComunidadService.SolicitudYaResueltaException
                | servicio.GestionSolicitudesComunidadService.SinCupoParaAceptarException
                | servicio.GestionSolicitudesComunidadService.PerfilYaParticipaException exception) {
            error(intercambio, 409, "CONFLICTO_GESTION_SOLICITUD",
                    exception.getMessage(), operacionId);
        } catch (servicio.SolicitudComunidadService.ConvocatoriaNoEncontradaException exception) {
            error(intercambio, 404, "CONVOCATORIA_NO_ENCONTRADA",
                    exception.getMessage(), operacionId);
        } catch (servicio.SolicitudComunidadService.SolicitudPropiaNoEncontradaException exception) {
            error(intercambio, 404, "SOLICITUD_COMUNIDAD_NO_ENCONTRADA",
                    exception.getMessage(), operacionId);
        } catch (servicio.SolicitudComunidadService.AutoSolicitudException
                | servicio.SolicitudComunidadService.PerfilNoHabilitadoException exception) {
            error(intercambio, 403, "SOLICITUD_COMUNIDAD_NO_AUTORIZADA",
                    exception.getMessage(), operacionId);
        } catch (servicio.SolicitudComunidadService.SolicitudNoRetirableException exception) {
            error(intercambio, 409, "SOLICITUD_COMUNIDAD_NO_RETIRABLE",
                    exception.getMessage(), operacionId);
        } catch (servicio.SolicitudComunidadService.PerfilNoDisponibleParaTurnosException
                | servicio.SolicitudComunidadService.PerfilYaParticipaException
                | servicio.SolicitudComunidadService.ConvocatoriaNoDisponibleException
                | servicio.SolicitudComunidadService.ConvocatoriaVencidaException
                | servicio.SolicitudComunidadService.ReservaNoDisponibleException
                | servicio.SolicitudComunidadService.ConvocatoriaSinCupoException
                | servicio.SolicitudComunidadService.SolicitudNoReabribleException exception) {
            error(intercambio, 409, "CONFLICTO_SOLICITUD_COMUNIDAD",
                    exception.getMessage(), operacionId);
        } catch (servicio.InvitacionComunidadService.AutoInvitacionException | servicio.InvitacionComunidadService.PerfilNoHabilitadoException | servicio.InvitacionComunidadService.InvitacionAjenaException exception) {
            error(intercambio,403,"INVITACION_COMUNIDAD_NO_AUTORIZADA",exception.getMessage(),operacionId);
        } catch (servicio.InvitacionComunidadService.InvitacionNoEncontradaException exception) {
            error(intercambio,404,"INVITACION_COMUNIDAD_NO_ENCONTRADA",exception.getMessage(),operacionId);
        } catch (servicio.InvitacionComunidadService.DestinatarioNoDisponibleException | servicio.InvitacionComunidadService.ObjetivoNoHabilitadoException | servicio.InvitacionComunidadService.InvitacionDuplicadaException | servicio.InvitacionComunidadService.InvitacionNoPendienteException | servicio.InvitacionComunidadService.InvitacionVencidaException exception) {
            error(intercambio,409,"CONFLICTO_INVITACION_COMUNIDAD",exception.getMessage(),operacionId);
        } catch (JacksonException exception) {
            error(intercambio, 400, "JSON_INVALIDO",
                    "El cuerpo JSON no tiene el formato esperado.",
                    operacionId);
        } catch (IllegalArgumentException exception) {
            error(intercambio, 400, "SOLICITUD_INVALIDA",
                    exception.getMessage(), operacionId);
        }
    }

    private void cambiarPassword(
            HttpExchange intercambio,
            SesionAutenticada autenticada) throws IOException {
        CambiarPassword entrada = mapper.readValue(
                intercambio.getRequestBody(),
                CambiarPassword.class);
        cuentaService.cambiarPassword(
                autenticada.usuario().getId(),
                entrada);
        responder(intercambio, 200,
                new MensajeCuenta(
                        "La contrasena fue actualizada correctamente."));
    }

    private void actualizarPreferenciasDeportivas(
            HttpExchange intercambio,
            SesionAutenticada autenticada) throws IOException {
        ActualizarPreferenciasDeportivas entrada = mapper.readValue(
                intercambio.getRequestBody(),
                ActualizarPreferenciasDeportivas.class);
        if (entrada.disponibleParaJugar() == null
                || entrada.aceptaInvitaciones() == null) {
            throw new IllegalArgumentException(
                    "Las dos preferencias deportivas son obligatorias.");
        }
        identidadDeportivaService.actualizarPreferenciasPorCliente(
                autenticada.cliente().getId(),
                entrada.disponibleParaJugar(),
                entrada.aceptaInvitaciones());
        responder(intercambio, 200,
                jugadorPublicoService.buscarPorCliente(
                        autenticada.cliente().getId()));
    }

    private void actualizarPerfil(
            HttpExchange intercambio,
            SesionAutenticada autenticada) throws IOException {
        ActualizarPerfil entrada = mapper.readValue(
                intercambio.getRequestBody(),
                ActualizarPerfil.class);
        responder(intercambio, 200,
                cuentaService.actualizarPerfil(
                        autenticada.cliente().getId(),
                        entrada));
    }

    private SesionAutenticada autenticar(
            HttpExchange intercambio) {
        String token = CookieSesionCliente.leer(intercambio);
        SesionAutenticada autenticada =
                autenticacionService.obtenerSesion(token);
        if (autenticada == null) {
            throw new NoAutenticadoException();
        }
        return autenticada;
    }

    private long parsearId(String valor) {
        try {
            long id = Long.parseLong(valor);
            if (id <= 0) throw new NumberFormatException();
            return id;
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException(
                    "El ID de la reserva debe ser positivo.");
        }
    }

    private void error(
            HttpExchange intercambio,
            int estado,
            String codigo,
            String mensaje,
            String operacionId) throws IOException {
        responder(intercambio, estado, new ErrorCuenta(
                estado,
                codigo,
                mensaje,
                intercambio.getRequestURI().getPath(),
                operacionId));
    }

    private void responder(
            HttpExchange intercambio,
            int estado,
            Object cuerpo) throws IOException {
        byte[] datos = mapper.writeValueAsBytes(cuerpo);
        intercambio.getResponseHeaders().set(
                "Content-Type", "application/json; charset=UTF-8");
        intercambio.sendResponseHeaders(estado, datos.length);
        try (OutputStream salida = intercambio.getResponseBody()) {
            salida.write(datos);
        }
    }

    private record MensajeCuenta(String mensaje) {
    }

    private record SinOrdenPago(String mensaje) {
    }

    private record ErrorCuenta(
            int estado,
            String codigo,
            String mensaje,
            String ruta,
            String operacionId) {
    }

    public static final class NoAutenticadoException
            extends RuntimeException {

        public NoAutenticadoException() {
            super("Debes iniciar sesion para acceder a este recurso.");
        }
    }
}
