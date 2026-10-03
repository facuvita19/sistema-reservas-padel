package negocio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ConfiguracionComplejo {
    public static final long ID_UNICO = 1L;

    private long id = ID_UNICO;
    private String nombreComercial = "Padel Reservas";
    private String razonSocial;
    private String direccion;
    private String telefono;
    private String whatsapp;
    private String email;
    private String instagram;
    private String moneda = "ARS";
    private BigDecimal porcentajeSenia = new BigDecimal("25.00");
    private int anticipacionMinimaHoras = 2;
    private int cancelacionMinimaHoras = 12;
    private int minutosReservaPendiente = 15;
    private String pagoAlias;
    private String pagoTitular;
    private String pagoEntidad;
    private String pagoInstrucciones;
    private String colorPrincipal = "#2F8F83";
    private String rutaLogo;
    private LocalDateTime fechaActualizacion;

    public long getId() { return id; }
    public void setId(long valor) { id = valor; }
    public String getNombreComercial() { return nombreComercial; }
    public void setNombreComercial(String valor) { nombreComercial = valor; }
    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String valor) { razonSocial = valor; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String valor) { direccion = valor; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String valor) { telefono = valor; }
    public String getWhatsapp() { return whatsapp; }
    public void setWhatsapp(String valor) { whatsapp = valor; }
    public String getEmail() { return email; }
    public void setEmail(String valor) { email = valor; }
    public String getInstagram() { return instagram; }
    public void setInstagram(String valor) { instagram = valor; }
    public String getMoneda() { return moneda; }
    public void setMoneda(String valor) { moneda = valor; }
    public BigDecimal getPorcentajeSenia() { return porcentajeSenia; }
    public void setPorcentajeSenia(BigDecimal valor) { porcentajeSenia = valor; }
    public int getAnticipacionMinimaHoras() { return anticipacionMinimaHoras; }
    public void setAnticipacionMinimaHoras(int valor) { anticipacionMinimaHoras = valor; }
    public int getCancelacionMinimaHoras() { return cancelacionMinimaHoras; }
    public void setCancelacionMinimaHoras(int valor) { cancelacionMinimaHoras = valor; }
    public int getMinutosReservaPendiente() { return minutosReservaPendiente; }
    public void setMinutosReservaPendiente(int valor) { minutosReservaPendiente = valor; }
    public String getPagoAlias() { return pagoAlias; }
    public void setPagoAlias(String valor) { pagoAlias = valor; }
    public String getPagoTitular() { return pagoTitular; }
    public void setPagoTitular(String valor) { pagoTitular = valor; }
    public String getPagoEntidad() { return pagoEntidad; }
    public void setPagoEntidad(String valor) { pagoEntidad = valor; }
    public String getPagoInstrucciones() { return pagoInstrucciones; }
    public void setPagoInstrucciones(String valor) { pagoInstrucciones = valor; }
    public String getColorPrincipal() { return colorPrincipal; }
    public void setColorPrincipal(String valor) { colorPrincipal = valor; }
    public String getRutaLogo() { return rutaLogo; }
    public void setRutaLogo(String valor) { rutaLogo = valor; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime valor) { fechaActualizacion = valor; }

    public boolean tieneInstruccionesPago() {
        return pagoAlias != null && !pagoAlias.isBlank();
    }
}
