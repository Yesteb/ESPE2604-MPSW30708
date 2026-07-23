package utils;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.function.Function;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import model.Cliente;

/**
 * Campo de búsqueda de clientes con autocompletado tipo barra de navegador.
 *
 * <p>Muestra un popup flotante debajo del campo con sugerencias filtradas
 * por coincidencia parcial de nombre mientras el usuario escribe.
 * Soporta navegación con teclado (↑↓ Enter Escape) y selección con clic.</p>
 *
 * <p>El filtrado lo resuelve la función de búsqueda que se recibe en el
 * constructor, no este componente: así puede apoyarse en el índice de la base
 * de datos en lugar de recorrer en memoria la lista completa de clientes.</p>
 */
public class BuscadorCliente extends JPanel {

    private static final int MAX_VISIBLES = 6;
    private static final int ALTO_FILA    = 34;
    /** Pausa de inactividad antes de consultar, en milisegundos. */
    private static final int RETARDO_BUSQUEDA = 180;

    private static final Color FONDO_POPUP   = Color.WHITE;
    private static final Color BORDE_POPUP   = EstiloUI.COLOR_SECUNDARIO;
    private static final Color COLOR_SEL     = EstiloUI.COLOR_PRIMARIO_CLARO;
    private static final Color COLOR_HOVER   = EstiloUI.COLOR_NEUTRO_CLARO;
    private static final Color COLOR_MARC    = EstiloUI.COLOR_MARCADOR;
    private static final String TEXTO_GUIA   = "Nombre del cliente";

    private final JTextField              campo;
    private final DefaultListModel<Cliente> modelo;
    private final JList<Cliente>          lista;
    private final JWindow                 popup;
    private final Function<String, List<Cliente>> busqueda;
    private final Timer                   temporizador;

    private boolean suprimirFiltro = false;
    private int     indiceHover    = -1;

    /**
     * @param busqueda función que recibe el texto tecleado y devuelve las
     *                 sugerencias ya filtradas y ordenadas por relevancia
     */
    public BuscadorCliente(Function<String, List<Cliente>> busqueda) {
        super(new BorderLayout());
        this.busqueda = busqueda;

        temporizador = new Timer(RETARDO_BUSQUEDA, e -> lanzarBusqueda());
        temporizador.setRepeats(false);

        campo = new JTextField();
        campo.setText(TEXTO_GUIA);
        campo.setForeground(COLOR_MARC);

        modelo = new DefaultListModel<>();
        lista  = new JList<>(modelo);
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lista.setCellRenderer(new RendererCliente());
        lista.setBackground(FONDO_POPUP);
        lista.setFixedCellHeight(ALTO_FILA);
        lista.setFocusable(false);

        popup = new JWindow();
        popup.setFocusableWindowState(false);
        JScrollPane scroll = new JScrollPane(lista);
        scroll.setBorder(new CompoundBorder(
            new LineBorder(BORDE_POPUP, 1, true),
            new EmptyBorder(2, 0, 2, 0)
        ));
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(ALTO_FILA);
        popup.setContentPane(scroll);

        add(campo, BorderLayout.CENTER);
        conectarEventos();
    }

    // -------------------------------------------------------------------------
    // API pública
    // -------------------------------------------------------------------------

    /** Texto introducido por el usuario; cadena vacía si sólo está el marcador. */
    public String getTexto() {
        if (COLOR_MARC.equals(campo.getForeground())) return "";
        return campo.getText().trim();
    }

    /** Restablece el campo al estado de marcador de posición. */
    public void limpiar() {
        suprimirFiltro = true;
        restaurarMarcador();
        cerrarPopup();
        suprimirFiltro = false;
    }

    /** Devuelve el JTextField interno para gestión de foco externa. */
    public JTextField getTextField() {
        return campo;
    }

    // -------------------------------------------------------------------------
    // Eventos
    // -------------------------------------------------------------------------

    private void conectarEventos() {
        // Placeholder
        campo.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (TEXTO_GUIA.equals(campo.getText())) {
                    campo.setText("");
                    campo.setForeground(Color.BLACK);
                }
                if (!campo.getText().trim().isEmpty()) filtrar();
            }

            @Override
            public void focusLost(FocusEvent e) {
                Timer t = new Timer(150, ev -> {
                    cerrarPopup();
                    if (campo.getText().trim().isEmpty()) restaurarMarcador();
                });
                t.setRepeats(false);
                t.start();
            }
        });

        // Filtrar al escribir
        campo.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { filtrar(); }
            @Override public void removeUpdate(DocumentEvent e)  { filtrar(); }
            @Override public void changedUpdate(DocumentEvent e) { filtrar(); }
        });

        // Navegación con teclado desde el campo
        campo.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!popup.isVisible()) return;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_DOWN -> {
                        moverSeleccion(+1);
                        e.consume();
                    }
                    case KeyEvent.VK_UP -> {
                        moverSeleccion(-1);
                        e.consume();
                    }
                    case KeyEvent.VK_ENTER -> {
                        seleccionar(lista.getSelectedValue());
                        e.consume();
                    }
                    case KeyEvent.VK_ESCAPE -> {
                        cerrarPopup();
                        e.consume();
                    }
                }
            }
        });

        // Clic en elemento de la lista
        lista.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int idx = lista.locationToIndex(e.getPoint());
                if (idx >= 0) seleccionar(modelo.getElementAt(idx));
            }
        });

        // Hover para resaltar fila
        lista.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int idx = lista.locationToIndex(e.getPoint());
                if (idx != indiceHover) {
                    indiceHover = idx;
                    lista.repaint();
                }
            }
        });

        // Reposicionar popup al mover/redimensionar la ventana padre
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                Window ventana = SwingUtilities.getWindowAncestor(BuscadorCliente.this);
                if (ventana != null) {
                    ventana.addComponentListener(new ComponentAdapter() {
                        @Override public void componentMoved(ComponentEvent ce)   { reposicionarPopup(); }
                        @Override public void componentResized(ComponentEvent ce) { reposicionarPopup(); }
                    });
                }
            }
        });
    }

    // -------------------------------------------------------------------------
    // Lógica interna
    // -------------------------------------------------------------------------

    /**
     * Programa una búsqueda tras una breve pausa de inactividad. El antirrebote
     * evita lanzar una consulta por cada tecla: escribiendo "Beatriz" se ejecuta
     * una sola vez en lugar de siete.
     */
    private void filtrar() {
        if (suprimirFiltro) return;
        if (COLOR_MARC.equals(campo.getForeground())) return;

        if (campo.getText().trim().isEmpty()) {
            temporizador.stop();
            cerrarPopup();
            return;
        }
        temporizador.restart();
    }

    /**
     * Ejecuta la búsqueda fuera del hilo de la interfaz: ahora consulta a la base
     * de datos y bloquear el EDT congelaría la ventana mientras se escribe.
     */
    private void lanzarBusqueda() {
        final String texto = campo.getText().trim();
        if (texto.isEmpty()) { cerrarPopup(); return; }

        new SwingWorker<List<Cliente>, Void>() {
            @Override
            protected List<Cliente> doInBackground() {
                return busqueda.apply(texto);
            }

            @Override
            protected void done() {
                // Si el usuario siguió escribiendo, este resultado ya no corresponde
                // a lo que hay en el campo: descartarlo en vez de pintarlo.
                if (!texto.equals(campo.getText().trim())) return;

                List<Cliente> encontrados;
                try {
                    encontrados = get();
                } catch (Exception e) {
                    cerrarPopup();
                    return;
                }

                modelo.clear();
                encontrados.forEach(modelo::addElement);
                if (modelo.isEmpty()) { cerrarPopup(); return; }

                lista.clearSelection();
                indiceHover = -1;
                mostrarPopup();
            }
        }.execute();
    }

    private void mostrarPopup() {
        if (!isShowing()) return;
        reposicionarPopup();
        if (!popup.isVisible()) popup.setVisible(true);
    }

    private void reposicionarPopup() {
        if (!isShowing()) return;
        Point origen = campo.getLocationOnScreen();
        int ancho    = campo.getWidth();
        int visible  = Math.min(modelo.size(), MAX_VISIBLES);
        int alto     = visible * ALTO_FILA + 6; // margen del borde/padding
        popup.setBounds(origen.x, origen.y + campo.getHeight(), ancho, alto);
    }

    private void moverSeleccion(int delta) {
        if (modelo.isEmpty()) return;
        int idx = lista.getSelectedIndex();
        int nuevo = (idx + delta + modelo.size()) % modelo.size();
        lista.setSelectedIndex(nuevo);
        lista.ensureIndexIsVisible(nuevo);
    }

    private void seleccionar(Cliente cliente) {
        if (cliente == null) return;
        suprimirFiltro = true;
        campo.setText(cliente.getNombre());
        campo.setForeground(Color.BLACK);
        suprimirFiltro = false;
        cerrarPopup();
        campo.requestFocus();
    }

    private void cerrarPopup() {
        popup.setVisible(false);
    }

    private void restaurarMarcador() {
        campo.setText(TEXTO_GUIA);
        campo.setForeground(COLOR_MARC);
    }

    // -------------------------------------------------------------------------
    // Renderer de celda
    // -------------------------------------------------------------------------

    private class RendererCliente implements ListCellRenderer<Cliente> {
        @Override
        public Component getListCellRendererComponent(JList<? extends Cliente> list,
                Cliente value, int index, boolean isSelected, boolean cellHasFocus) {

            JPanel fila = new JPanel(new BorderLayout(8, 0));
            fila.setBorder(new EmptyBorder(5, 12, 5, 12));
            fila.setOpaque(true);

            if (isSelected) {
                fila.setBackground(COLOR_SEL);
            } else if (index == indiceHover) {
                fila.setBackground(COLOR_HOVER);
            } else {
                fila.setBackground(FONDO_POPUP);
            }

            if (value == null) return fila;

            JLabel nombre = new JLabel(value.getNombre());
            nombre.setFont(new Font("SansSerif", Font.BOLD, 13));
            nombre.setForeground(isSelected ? EstiloUI.COLOR_MUY_OSCURO : EstiloUI.COLOR_OSCURO);
            fila.add(nombre, BorderLayout.CENTER);

            String estado = value.getEstado() != null ? value.getEstado() : "ACTIVO";
            Color[] cols  = EstiloUI.coloresEstadoCliente(estado);
            String  etq   = EstiloUI.etiquetaEstadoCliente(estado);
            JLabel badge  = new JLabel(etq);
            badge.setFont(new Font("SansSerif", Font.PLAIN, 10));
            badge.setForeground(cols[1]);
            badge.setBackground(cols[0]);
            badge.setOpaque(true);
            badge.setBorder(new EmptyBorder(2, 6, 2, 6));
            fila.add(badge, BorderLayout.EAST);

            return fila;
        }
    }
}
