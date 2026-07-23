package utils;

import java.awt.FlowLayout;
import java.awt.Font;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

/**
 * Lista paginada genérica: divide una colección en páginas de tamaño fijo y
 * ofrece controles para avanzar/retroceder o saltar directamente a un número
 * de página.
 *
 * <p>Admite dos orígenes de datos:</p>
 * <ul>
 *   <li>Una {@link Supplier} que devuelve la lista completa, que se corta en
 *       memoria. Vale para colecciones pequeñas (las etiquetas de configuración).</li>
 *   <li>Una {@link FuenteDatos}, que pide a la base de datos solo la página que
 *       se va a mostrar. Es lo que deben usar los listados que crecen sin
 *       límite, como pedidos y clientes.</li>
 * </ul>
 *
 * @param <T> tipo de elemento mostrado en cada página.
 */
public class PaginadorPanel<T> {

    /**
     * Origen de datos paginado: entrega una página concreta y sabe cuántos
     * elementos hay en total, sin necesidad de materializarlos todos.
     *
     * @param <T> tipo de elemento
     */
    public interface FuenteDatos<T> {
        /**
         * @param desde    índice del primer elemento de la página (0 = primera)
         * @param cantidad número de elementos a devolver
         */
        List<T> pagina(int desde, int cantidad);

        /** Total de elementos disponibles, para calcular el número de páginas. */
        int total();
    }

    private final int tamanoPagina;
    private final FuenteDatos<T> fuente;
    private final Supplier<JComponent> mensajeVacio;
    private Function<T, ? extends JComponent> renderizador;

    private final JPanel panelContenido;
    private final JPanel panelControles;
    private final JLabel lblInfo;
    private final JTextField txtPagina;
    private final JButton btnAnterior;
    private final JButton btnSiguiente;

    /** Elementos de la página que se está mostrando (no la colección entera). */
    private List<T> pagina = Collections.emptyList();
    private int totalElementos = 0;
    private int paginaActual = 1;
    private JScrollPane scrollAsociado;

    /** Constructor completo: renderiza y muestra la primera página de inmediato. */
    public PaginadorPanel(int tamanoPagina, Supplier<List<T>> proveedorDatos,
            Function<T, ? extends JComponent> renderizador, Supplier<JComponent> mensajeVacio) {
        this(tamanoPagina, proveedorDatos, mensajeVacio);
        this.renderizador = renderizador;
        recargar();
    }

    /**
     * Constructor sin renderizador: úsalo cuando la fila de cada elemento
     * necesita poder refrescar el propio paginador (ej. un botón "eliminar").
     * Llama a {@link #setRenderizador} y luego a {@link #recargar()} antes de mostrarlo.
     */
    public PaginadorPanel(int tamanoPagina, Supplier<List<T>> proveedorDatos, Supplier<JComponent> mensajeVacio) {
        this(tamanoPagina, enMemoria(proveedorDatos), mensajeVacio);
    }

    /** Constructor con datos paginados en la base de datos, con renderizador. */
    public PaginadorPanel(int tamanoPagina, FuenteDatos<T> fuente,
            Function<T, ? extends JComponent> renderizador, Supplier<JComponent> mensajeVacio) {
        this(tamanoPagina, fuente, mensajeVacio);
        this.renderizador = renderizador;
        recargar();
    }

    /** Constructor con datos paginados en la base de datos, sin renderizador. */
    public PaginadorPanel(int tamanoPagina, FuenteDatos<T> fuente, Supplier<JComponent> mensajeVacio) {
        this.tamanoPagina   = Math.max(1, tamanoPagina);
        this.fuente         = fuente;
        this.mensajeVacio   = mensajeVacio;

        panelContenido = new JPanel();
        panelContenido.setLayout(new BoxLayout(panelContenido, BoxLayout.Y_AXIS));
        panelContenido.setBorder(new EmptyBorder(8, 8, 8, 8));
        panelContenido.setBackground(new java.awt.Color(255, 255, 255));

        btnAnterior = new JButton("‹ Anterior");
        btnAnterior.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btnAnterior.addActionListener(e -> irAPagina(paginaActual - 1));

        btnSiguiente = new JButton("Siguiente ›");
        btnSiguiente.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btnSiguiente.addActionListener(e -> irAPagina(paginaActual + 1));

        lblInfo = new JLabel();
        lblInfo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblInfo.setHorizontalAlignment(SwingConstants.CENTER);

        txtPagina = new JTextField(3);
        txtPagina.setHorizontalAlignment(SwingConstants.CENTER);
        txtPagina.addActionListener(e -> irAPaginaDesdeCampo());

        JButton btnIr = new JButton("Ir");
        btnIr.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btnIr.addActionListener(e -> irAPaginaDesdeCampo());

        panelControles = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        panelControles.setBackground(new java.awt.Color(255, 255, 255));
        panelControles.add(btnAnterior);
        panelControles.add(lblInfo);
        panelControles.add(btnSiguiente);
        panelControles.add(Box.createHorizontalStrut(10));
        panelControles.add(new JLabel("Ir a página:"));
        panelControles.add(txtPagina);
        panelControles.add(btnIr);
        panelControles.setVisible(false);
    }

    /**
     * Envuelve un proveedor de lista completa como {@link FuenteDatos}, cortando
     * la página en memoria. La lista se pide una sola vez por recarga, no una
     * vez por página.
     */
    private static <T> FuenteDatos<T> enMemoria(Supplier<List<T>> proveedor) {
        return new FuenteDatos<>() {
            private List<T> cache = Collections.emptyList();

            /** Se consulta antes que {@code pagina()}: aquí se refresca la caché. */
            @Override
            public int total() {
                cache = proveedor.get();
                return cache.size();
            }

            @Override
            public List<T> pagina(int desde, int cantidad) {
                if (desde >= cache.size()) return Collections.emptyList();
                return cache.subList(desde, Math.min(desde + cantidad, cache.size()));
            }
        };
    }

    public void setRenderizador(Function<T, ? extends JComponent> renderizador) {
        this.renderizador = renderizador;
    }

    /**
     * Asocia el {@link JScrollPane} que envuelve el contenido paginado, para
     * que el scroll vuelva arriba en cada cambio de página. Las tarjetas
     * renderizadas tienen alto variable (no son filas uniformes), así que sin
     * esto el usuario queda con el scroll en una posición arbitraria de la
     * página nueva en vez de empezar a verla desde el inicio.
     */
    public void vincularScroll(JScrollPane scroll) {
        this.scrollAsociado = scroll;
    }

    /** Panel donde se dibujan los elementos de la página actual (agrégalo a tu layout). */
    public JPanel getPanelContenido() { return panelContenido; }

    /** Barra con los botones de navegación y el campo de salto de página. */
    public JPanel getPanelControles() { return panelControles; }

    /** Vuelve a consultar los datos (ej. tras agregar/eliminar un elemento) manteniendo la página actual. */
    public void recargar() {
        totalElementos = fuente.total();
        paginaActual = Math.max(1, Math.min(paginaActual, totalPaginas()));
        cargarPagina();
        renderizarPagina();
    }

    /** Vuelve a consultar los datos y regresa a la primera página (ej. al cambiar un filtro). */
    public void reiniciar() {
        paginaActual = 1;
        recargar();
    }

    private void irAPaginaDesdeCampo() {
        try {
            irAPagina(Integer.parseInt(txtPagina.getText().trim()));
        } catch (NumberFormatException ex) {
            txtPagina.setText(String.valueOf(paginaActual));
        }
    }

    private void irAPagina(int numero) {
        paginaActual = Math.max(1, Math.min(numero, totalPaginas()));
        cargarPagina();
        renderizarPagina();
    }

    /** Pide a la fuente únicamente los elementos de la página actual. */
    private void cargarPagina() {
        pagina = fuente.pagina((paginaActual - 1) * tamanoPagina, tamanoPagina);
    }

    private int totalPaginas() {
        return Math.max(1, (int) Math.ceil(totalElementos / (double) tamanoPagina));
    }

    private void renderizarPagina() {
        panelContenido.removeAll();
        if (pagina.isEmpty()) {
            panelContenido.add(mensajeVacio.get());
        } else {
            for (T elemento : pagina) {
                panelContenido.add(renderizador.apply(elemento));
                panelContenido.add(Box.createVerticalStrut(6));
            }
        }

        int totalPaginas = totalPaginas();
        lblInfo.setText(String.format("Página %d de %d (%d en total)", paginaActual, totalPaginas, totalElementos));
        txtPagina.setText(String.valueOf(paginaActual));
        btnAnterior.setEnabled(paginaActual > 1);
        btnSiguiente.setEnabled(paginaActual < totalPaginas);
        panelControles.setVisible(totalPaginas > 1);

        panelContenido.revalidate();
        panelContenido.repaint();
        panelControles.revalidate();
        panelControles.repaint();

        if (scrollAsociado != null) {
            SwingUtilities.invokeLater(() ->
                scrollAsociado.getViewport().setViewPosition(new java.awt.Point(0, 0)));
        }
    }
}
