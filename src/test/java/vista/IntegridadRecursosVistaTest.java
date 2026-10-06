package vista;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javafx.fxml.FXML;

class IntegridadRecursosVistaTest {

    private static final Path RECURSOS = Path.of(
            "src", "main", "resources");
    private static final Path FXML = RECURSOS.resolve("fxml");
    private static final Path CSS = RECURSOS.resolve("css");
    private static final String ESPACIO_FXML =
            "http://javafx.com/fxml/1";

    @Test
    void ningunRecursoVisualEstaVacio() throws Exception {
        assertTrue(Files.isDirectory(FXML),
                "No existe el directorio de vistas FXML.");
        assertTrue(Files.isDirectory(CSS),
                "No existe el directorio de estilos CSS.");

        try (Stream<Path> archivos = Files.walk(RECURSOS)) {
            List<Path> vacios = archivos
                    .filter(Files::isRegularFile)
                    .filter(this::esRecursoVisual)
                    .filter(archivo -> tamano(archivo) == 0)
                    .toList();
            assertTrue(vacios.isEmpty(),
                    "Hay recursos visuales vacios: " + vacios);
        }
    }

    @Test
    void todosLosFxmlSonXmlValidosYDeclaranControlador() throws Exception {
        List<Path> vistas = listarFxml();
        assertFalse(vistas.isEmpty(), "No se encontraron archivos FXML.");

        for (Path vista : vistas) {
            Document documento = assertDoesNotThrow(
                    () -> analizar(vista),
                    () -> "FXML invalido: " + vista);
            Element raiz = documento.getDocumentElement();
            assertNotNull(raiz, "El FXML no tiene elemento raiz: " + vista);

            String controlador = atributoFx(raiz, "controller");
            assertFalse(controlador.isBlank(),
                    "El FXML no declara fx:controller: " + vista);
            assertDoesNotThrow(() -> Class.forName(controlador),
                    () -> "No existe el controlador " + controlador
                            + " declarado por " + vista);
        }
    }

    @Test
    void camposFxmlDelControladorExistenEnLaVista() throws Exception {
        for (Path vista : listarFxml()) {
            Document documento = analizar(vista);
            String nombreControlador = atributoFx(
                    documento.getDocumentElement(), "controller");
            Class<?> controlador = Class.forName(nombreControlador);
            Set<String> identificadores = identificadoresFx(documento);

            for (Field campo : campos(controlador)) {
                if (!campo.isAnnotationPresent(FXML.class)) continue;
                assertTrue(identificadores.contains(campo.getName()),
                        () -> "Falta fx:id=\"" + campo.getName()
                                + "\" en " + vista
                                + " para " + nombreControlador);
            }
        }
    }

    @Test
    void accionesFxmlExistenEnElControlador() throws Exception {
        for (Path vista : listarFxml()) {
            Document documento = analizar(vista);
            String nombreControlador = atributoFx(
                    documento.getDocumentElement(), "controller");
            Class<?> controlador = Class.forName(nombreControlador);
            Set<String> metodos = nombresMetodos(controlador);

            for (String accion : acciones(documento)) {
                assertTrue(metodos.contains(accion),
                        () -> "No existe el metodo #" + accion
                                + " usado por " + vista
                                + " en " + nombreControlador);
            }
        }
    }

    private List<Path> listarFxml() throws Exception {
        try (Stream<Path> archivos = Files.list(FXML)) {
            return archivos
                    .filter(Files::isRegularFile)
                    .filter(archivo -> archivo.getFileName().toString()
                            .endsWith(".fxml"))
                    .sorted()
                    .toList();
        }
    }

    private Document analizar(Path archivo) throws Exception {
        DocumentBuilderFactory fabrica =
                DocumentBuilderFactory.newInstance();
        fabrica.setNamespaceAware(true);
        fabrica.setFeature(
                "http://apache.org/xml/features/disallow-doctype-decl",
                true);
        fabrica.setFeature(
                "http://xml.org/sax/features/external-general-entities",
                false);
        fabrica.setFeature(
                "http://xml.org/sax/features/external-parameter-entities",
                false);
        fabrica.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        fabrica.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

        try (InputStream entrada = Files.newInputStream(archivo)) {
            return fabrica.newDocumentBuilder().parse(entrada);
        }
    }

    private Set<String> identificadoresFx(Document documento) {
        Set<String> identificadores = new HashSet<>();
        NodeList elementos = documento.getElementsByTagName("*");
        for (int i = 0; i < elementos.getLength(); i++) {
            Element elemento = (Element) elementos.item(i);
            String id = atributoFx(elemento, "id");
            if (!id.isBlank()) identificadores.add(id);
        }
        return identificadores;
    }

    private Set<String> acciones(Document documento) {
        Set<String> acciones = new HashSet<>();
        NodeList elementos = documento.getElementsByTagName("*");
        for (int i = 0; i < elementos.getLength(); i++) {
            NamedNodeMap atributos = elementos.item(i).getAttributes();
            for (int j = 0; j < atributos.getLength(); j++) {
                Node atributo = atributos.item(j);
                String nombre = atributo.getNodeName();
                String valor = atributo.getNodeValue();
                boolean esEvento = nombre != null
                        && nombre.startsWith("on")
                        && nombre.length() > 2;
                if (esEvento && valor != null && valor.startsWith("#")
                        && valor.length() > 1) {
                    acciones.add(valor.substring(1));
                }
            }
        }
        return acciones;
    }

    private String atributoFx(Element elemento, String nombre) {
        String valor = elemento.getAttributeNS(ESPACIO_FXML, nombre);
        if (valor == null || valor.isBlank()) {
            valor = elemento.getAttribute("fx:" + nombre);
        }
        return valor == null ? "" : valor.trim();
    }

    private List<Field> campos(Class<?> tipo) {
        java.util.ArrayList<Field> resultado = new java.util.ArrayList<>();
        for (Class<?> actual = tipo;
                actual != null && actual != Object.class;
                actual = actual.getSuperclass()) {
            resultado.addAll(List.of(actual.getDeclaredFields()));
        }
        return resultado;
    }

    private Set<String> nombresMetodos(Class<?> tipo) {
        Set<String> resultado = new HashSet<>();
        for (Class<?> actual = tipo;
                actual != null && actual != Object.class;
                actual = actual.getSuperclass()) {
            for (Method metodo : actual.getDeclaredMethods()) {
                resultado.add(metodo.getName());
            }
        }
        return resultado;
    }

    private boolean esRecursoVisual(Path archivo) {
        String nombre = archivo.getFileName().toString().toLowerCase();
        return nombre.endsWith(".fxml") || nombre.endsWith(".css");
    }

    private long tamano(Path archivo) {
        try {
            return Files.size(archivo);
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "No se pudo leer el tamano de " + archivo, exception);
        }
    }
}
