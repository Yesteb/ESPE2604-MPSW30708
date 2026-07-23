package service;

import java.util.ArrayList;
import java.util.List;
import persistencia.RepositorioEtiqueta;

/**
 * Utilidades de presentación para la pestaña de reporte: qué categorías de
 * etiqueta existen y cómo se llaman en pantalla.
 *
 * <p>Las agregaciones (series mensuales y conteos por etiqueta) ya no se
 * calculan aquí: las resuelve la base de datos con funciones de ventana, a
 * través de {@link persistencia.RepositorioEstadisticas}.</p>
 */
public final class EstadisticasService {

    private EstadisticasService() {}

    /** Categorías de etiquetas reconocidas por el sistema: fijas más las personalizadas. */
    public static List<String> categorias(RepositorioEtiqueta repoEtiqueta) {
        List<String> categorias = new ArrayList<>(List.of("TIPO", "ESTILO", "TALLA", "PRECIO_RAPIDO"));
        repoEtiqueta.buscarPorCategoria("CATEGORIA_CUSTOM")
            .forEach(ec -> categorias.add(ec.getValor()));
        return categorias;
    }

    /** Nombre legible de una categoría para mostrar como encabezado de sección. */
    public static String nombreLegible(String categoria) {
        return switch (categoria) {
            case "TIPO" -> "Tipo";
            case "ESTILO" -> "Estilo";
            case "TALLA" -> "Talla";
            case "PRECIO_RAPIDO" -> "Precio";
            default -> categoria;
        };
    }
}
