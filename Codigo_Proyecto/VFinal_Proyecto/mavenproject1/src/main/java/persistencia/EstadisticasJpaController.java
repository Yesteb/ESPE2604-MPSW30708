package persistencia;

import jakarta.persistence.EntityManager;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Agregaciones estadísticas resueltas por la base de datos mediante funciones de
 * ventana ({@code fn_evolucion_mensual}, {@code fn_top_etiquetas}).
 *
 * <p>Sustituye a los cálculos que {@code EstadisticasService} hacía en memoria
 * recorriendo la lista completa de pedidos con sus productos. Devuelve las
 * mismas estructuras ({@code Map} ordenados) para que los gráficos no cambien.</p>
 */
public class EstadisticasJpaController implements Serializable, RepositorioEstadisticas {

    private static final long serialVersionUID = 1L;

    /**
     * Indicadores de cabecera en una sola consulta de una fila.
     *
     * <p>Antes esta cabecera costaba cuatro cargas completas de tablas (todos
     * los pedidos, todos los cobrados, todos los pendientes y los cobrados de
     * hoy) para mostrar cuatro números.</p>
     *
     * <p>Los valores provienen de {@code mv_dashboard_inicio}, así que reflejan
     * el último {@code sp_refrescar_dashboard()}. Para la cabecera de un reporte
     * es el compromiso correcto; los importes exactos de un pedido concreto se
     * leen de {@code pedido_resumen}, que sí está siempre al día.</p>
     */
    @Override
    public Kpis kpis() {
        EntityManager em = JPAUtil.obtenerGestorEntidades();
        try {
            Object[] f = (Object[]) em.createNativeQuery(
                    "SELECT pedidos_pendientes, monto_por_cobrar, cobrados_hoy, monto_cobrado_total "
                  + "FROM mv_dashboard_inicio WHERE id = 1")
                .getSingleResult();

            return new Kpis(
                ((Number) f[0]).intValue(),
                f[1] == null ? BigDecimal.ZERO : (BigDecimal) f[1],
                ((Number) f[2]).intValue(),
                f[3] == null ? BigDecimal.ZERO : (BigDecimal) f[3]);
        } finally {
            em.close();
        }
    }

    /**
     * Serie mensual de cantidad de pedidos y monto facturado.
     *
     * <p>Una sola consulta devuelve ambas magnitudes y ya incluye los meses sin
     * actividad, que la función rellena con {@code generate_series}.</p>
     *
     * @param estado estado a filtrar, o {@code null} para todos
     * @param meses  número de meses hacia atrás, incluido el actual
     */
    @Override
    public SerieMensual evolucionMensual(String estado, int meses) {
        EntityManager em = JPAUtil.obtenerGestorEntidades();
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> filas = em.createNativeQuery(
                    "SELECT etiqueta, pedidos, monto "
                  + "FROM fn_evolucion_mensual(?1, CAST(?2 AS VARCHAR))")
                .setParameter(1, meses)
                .setParameter(2, estado)
                .getResultList();

            List<String>        etiquetas = new ArrayList<>(filas.size());
            Map<String, Number> conteo    = new LinkedHashMap<>();
            Map<String, Number> monto     = new LinkedHashMap<>();

            for (Object[] f : filas) {
                String etiqueta = (String) f[0];
                etiquetas.add(etiqueta);
                conteo.put(etiqueta, ((Number) f[1]).intValue());
                monto.put(etiqueta, f[2] == null ? BigDecimal.ZERO : (BigDecimal) f[2]);
            }
            return new SerieMensual(etiquetas, conteo, monto);
        } finally {
            em.close();
        }
    }

    /**
     * Unidades vendidas por valor de etiqueta, de mayor a menor.
     *
     * @param categoria categoría fija (TIPO, ESTILO, TALLA) o personalizada
     * @param estado    estado de pedido a filtrar, o {@code null} para todos
     */
    @Override
    public Map<String, Integer> conteoPorEtiqueta(String categoria, String estado, int limite) {
        EntityManager em = JPAUtil.obtenerGestorEntidades();
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> filas = em.createNativeQuery(
                    "SELECT valor, unidades "
                  + "FROM fn_top_etiquetas(CAST(?1 AS TEXT), CAST(?2 AS TEXT), ?3)")
                .setParameter(1, categoria)
                .setParameter(2, estado)
                .setParameter(3, limite)
                .getResultList();

            Map<String, Integer> conteo = new LinkedHashMap<>();
            for (Object[] f : filas) {
                conteo.put((String) f[0], ((Number) f[1]).intValue());
            }
            return conteo;
        } finally {
            em.close();
        }
    }

    /** Refresca las vistas materializadas del panel de inicio, siempre. */
    @Override
    public void refrescarDashboard() {
        refrescar(false);
    }

    /**
     * Refresca solo si algún trigger marcó los datos como modificados. Abrir el
     * reporte dos veces seguidas sin tocar nada no vuelve a recalcular.
     */
    @Override
    public void refrescarSiHaceFalta() {
        refrescar(true);
    }

    private void refrescar(boolean soloSiSucio) {
        EntityManager em = JPAUtil.obtenerGestorEntidades();
        try {
            em.getTransaction().begin();
            em.createNativeQuery("CALL sp_refrescar_dashboard(true, ?1)")
                .setParameter(1, soloSiSucio)
                .executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            // El panel seguirá mostrando la instantánea anterior: no es fatal
            System.err.println("No se pudo refrescar el dashboard: " + e.getMessage());
        } finally {
            em.close();
        }
    }
}
