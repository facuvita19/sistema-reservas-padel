package servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dao.ConfiguracionComplejoDAO;
import negocio.ConfiguracionComplejo;

class ConfiguracionComplejoServiceTest {

	private ConfiguracionDAODoble dao;
	private ConfiguracionComplejoService service;

	@BeforeEach
	void preparar() {
		dao = new ConfiguracionDAODoble();
		service = new ConfiguracionComplejoService(dao);
	}

	@Test
	void obtieneValoresPredeterminadosSiNoExisteRegistro() {
		ConfiguracionComplejo configuracion = service.obtener();
		assertEquals("Padel Reservas", configuracion.getNombreComercial());
		assertEquals("ARS", configuracion.getMoneda());
		assertEquals(new BigDecimal("25.00"), configuracion.getPorcentajeSenia());
	}

	@Test
	void guardaYNormalizaConfiguracionValida() {
		ConfiguracionComplejo configuracion = crearConfiguracion();
		configuracion.setNombreComercial("  Arena   Padel  ");
		configuracion.setEmail("  CLUB@EMAIL.COM  ");
		configuracion.setMoneda("ars");
		configuracion.setColorPrincipal("#486b86");
		configuracion.setPorcentajeSenia(new BigDecimal("25.00"));

		service.guardar(configuracion);

		assertSame(configuracion, dao.guardada);
		assertEquals(1L, configuracion.getId());
		assertEquals("Arena Padel", configuracion.getNombreComercial());
		assertEquals("club@email.com", configuracion.getEmail());
		assertEquals("ARS", configuracion.getMoneda());
		assertEquals("#486B86", configuracion.getColorPrincipal());
		assertEquals(new BigDecimal("25.00"), configuracion.getPorcentajeSenia());
	}

	@Test
	void rechazaNombreVacioEmailColorYMonedaInvalidos() {
		ConfiguracionComplejo nombreVacio = crearConfiguracion();
		nombreVacio.setNombreComercial(" ");

		assertThrows(IllegalArgumentException.class, () -> service.guardar(nombreVacio));

		ConfiguracionComplejo emailInvalido = crearConfiguracion();
		emailInvalido.setEmail("correo-invalido");

		assertThrows(IllegalArgumentException.class, () -> service.guardar(emailInvalido));

		ConfiguracionComplejo colorInvalido = crearConfiguracion();
		colorInvalido.setColorPrincipal("azul");

		assertThrows(IllegalArgumentException.class, () -> service.guardar(colorInvalido));

		ConfiguracionComplejo monedaInvalida = crearConfiguracion();
		monedaInvalida.setMoneda("PESOS");

		assertThrows(IllegalArgumentException.class, () -> service.guardar(monedaInvalida));
	}

	@Test
	void rechazaPorcentajeYPlazosInvalidos() {
		ConfiguracionComplejo porcentajeInvalido = crearConfiguracion();

		porcentajeInvalido.setPorcentajeSenia(new BigDecimal("101"));

		assertThrows(IllegalArgumentException.class, () -> service.guardar(porcentajeInvalido));

		ConfiguracionComplejo plazoInvalido = crearConfiguracion();

		plazoInvalido.setAnticipacionMinimaHoras(-1);

		assertThrows(IllegalArgumentException.class, () -> service.guardar(plazoInvalido));
	}

	@Test
	void calculaSeniaSegunConfiguracion() {
		ConfiguracionComplejo configuracion = crearConfiguracion();
		configuracion.setPorcentajeSenia(new BigDecimal("25.00"));
		dao.actual = configuracion;

		assertEquals(new BigDecimal("6250.00"), service.calcularSenia(new BigDecimal("25000.00")));
	}

	private ConfiguracionComplejo crearConfiguracion() {
		ConfiguracionComplejo configuracion = new ConfiguracionComplejo();
		configuracion.setNombreComercial("Arena Padel");
		configuracion.setEmail("club@email.com");
		configuracion.setMoneda("ARS");
		configuracion.setPorcentajeSenia(new BigDecimal("25.00"));
		configuracion.setAnticipacionMinimaHoras(2);
		configuracion.setCancelacionMinimaHoras(12);
		configuracion.setColorPrincipal("#486B86");
		return configuracion;
	}

	private static final class ConfiguracionDAODoble implements ConfiguracionComplejoDAO {
		private ConfiguracionComplejo actual;
		private ConfiguracionComplejo guardada;

		@Override
		public ConfiguracionComplejo obtener() {
			return actual;
		}

		@Override
		public void guardar(ConfiguracionComplejo configuracion) {
			guardada = configuracion;
			actual = configuracion;
		}
	}
}
