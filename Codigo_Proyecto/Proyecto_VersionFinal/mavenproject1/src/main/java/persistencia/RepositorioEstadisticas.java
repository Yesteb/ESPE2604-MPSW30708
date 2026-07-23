package persistencia;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Agregaciones para la pestaña de reporte, calculadas en la base de datos.
 */
public interface RepositorioEstadisticas {

    /**
     * Serie mensual completa: etiquetas de mes en orden cronológico y, para cada
     * una, la cantidad de pedidos y el monto facturado.
     *
     * @param etiquetas meses en orden ("ene 26", "feb 26"…), sin huecos
     * @param conteo    cantidad de pedidos por mes
     * @param monto     monto facturado por mes
     */
    record SerieMensual(
            List<String>        etiquetas,
            Map<String, Number> conteo,
            Map<String, Number> monto) {
    }

    /**
     * Indicadores de la cabecera del reporte, leídos de la vista materializada
     * {@code mv_dashboard_inicio} en una sola consulta de una fila.
     *
     * @param pendientes    pedidos en estado PENDIENTE
     * @param porCobrar     importe pendiente de cobro
     * @param cobradosHoy   pedidos cobrados en el día en curso
     * @param totalCobrado  facturación histórica
     */
    record Kpis(
            int        pendientes,
            BigDecimal porCobrar,
            int        cobradosHoy,
            BigDecimal totalCobrado) {
    }

    Kpis kpis();

    SerieMensual evolucionMensual(String estado, int meses);

    Map<String, Integer> conteoPorEtiqueta(String categoria, String estado, int limite);

    /** Recalcula las vistas materializadas incondicionalmente. */
    void refrescarDashboard();

    /** Recalcula solo si los triggers marcaron los datos como modificados. */
    void refrescarSiHaceFalta();
}
