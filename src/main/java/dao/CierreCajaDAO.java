package dao;

import java.time.LocalDate;
import java.util.List;

import negocio.CierreCaja;
import negocio.ResumenCajaDiaria;

public interface CierreCajaDAO {
    ResumenCajaDiaria calcularResumen(LocalDate fecha);
    CierreCaja buscarPorFecha(LocalDate fecha);
    List<CierreCaja> listar();
    void guardar(CierreCaja cierre);
}
