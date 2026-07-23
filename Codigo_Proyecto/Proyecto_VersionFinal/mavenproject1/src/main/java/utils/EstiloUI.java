package utils;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import com.formdev.flatlaf.FlatLightLaf;
import model.Cliente;

/**
 * Utilidades de personalización visual para la interfaz del dashboard.
 *
 * <p>Centraliza colores, paletas, formatos de fecha y la construcción
 * de componentes visuales reutilizables (chips, badges, etiquetas, botones,
 * tarjetas de estadística y barras de gráfico).</p>
 *
 * <p>Clase no instanciable: todos sus miembros son estáticos.</p>
 */
public final class EstiloUI {

    // -------------------------------------------------------------------------
    // Paleta de marca (PinkyPuff)
    // -------------------------------------------------------------------------

    /** Rosa predominante: acento principal, botones primarios, foco, selección. */
    public static final Color COLOR_PRIMARIO    = color("FE7FBE");
    /** Rosa apagado: hover y estados secundarios sobre el primario. */
    public static final Color COLOR_SECUNDARIO  = color("CC8FAD");
    /** Malva neutro: bordes y texto deshabilitado. */
    public static final Color COLOR_NEUTRO      = color("998A91");
    /** Ciruela oscuro: texto secundario y acentos sobre fondos claros. */
    public static final Color COLOR_OSCURO      = color("664757");
    /** Ciruela casi negro: texto principal y encabezados. */
    public static final Color COLOR_MUY_OSCURO  = color("331A26");

    /** Tinte muy claro del rosa primario: fondos de tarjetas/badges/chips. */
    public static final Color COLOR_PRIMARIO_CLARO   = color("FFECF5");
    /** Tinte muy claro del rosa secundario: fondos alternos de badges. */
    public static final Color COLOR_SECUNDARIO_CLARO = color("F7EEF3");
    /** Gris cálido muy claro (derivado del neutro): fondos neutros. */
    public static final Color COLOR_NEUTRO_CLARO     = color("F2ECEE");
    /** Gris cálido claro (derivado del neutro): bordes neutros y divisores. */
    public static final Color COLOR_BORDE_NEUTRO     = color("E3D8DD");
    /** Rosa resaltado: acento/borde y relleno de chips seleccionados en "+ Pedidos"
     *  (mismo color que la pestaña activa en la barra de navegación). */
    public static final Color COLOR_RESALTADO        = COLOR_PRIMARIO;

    // Paletas de color [fondo, texto, borde/acento]

    // Paleta para etiquetas de tipo de producto
    public static final Color[][] PALETA_TIPO = {
        {COLOR_PRIMARIO_CLARO, COLOR_MUY_OSCURO, COLOR_RESALTADO},
        {COLOR_PRIMARIO_CLARO, COLOR_MUY_OSCURO, COLOR_RESALTADO},
        {COLOR_PRIMARIO_CLARO, COLOR_MUY_OSCURO, COLOR_RESALTADO},
        {COLOR_PRIMARIO_CLARO, COLOR_MUY_OSCURO, COLOR_RESALTADO},
        {COLOR_PRIMARIO_CLARO, COLOR_MUY_OSCURO, COLOR_RESALTADO},
    };

    //Paleta para etiquetas de estilo
    public static final Color[][] PALETA_ESTILO = {
        {COLOR_SECUNDARIO_CLARO, COLOR_MUY_OSCURO, COLOR_RESALTADO},
        {COLOR_SECUNDARIO_CLARO, COLOR_MUY_OSCURO, COLOR_RESALTADO},
        {COLOR_SECUNDARIO_CLARO, COLOR_MUY_OSCURO, COLOR_RESALTADO},
        {COLOR_SECUNDARIO_CLARO, COLOR_MUY_OSCURO, COLOR_RESALTADO},
        {COLOR_SECUNDARIO_CLARO, COLOR_MUY_OSCURO, COLOR_RESALTADO},
        {COLOR_SECUNDARIO_CLARO, COLOR_MUY_OSCURO, COLOR_RESALTADO},
        {COLOR_SECUNDARIO_CLARO, COLOR_MUY_OSCURO, COLOR_RESALTADO},
        {COLOR_SECUNDARIO_CLARO, COLOR_MUY_OSCURO, COLOR_RESALTADO},
    };

    //Paleta para etiquetas de precio rápido
    public static final Color[][] PALETA_PRECIO = {
        {COLOR_PRIMARIO_CLARO, COLOR_MUY_OSCURO, COLOR_RESALTADO},
        {COLOR_PRIMARIO_CLARO, COLOR_MUY_OSCURO, COLOR_RESALTADO},
        {COLOR_PRIMARIO_CLARO, COLOR_MUY_OSCURO, COLOR_RESALTADO},
        {COLOR_PRIMARIO_CLARO, COLOR_MUY_OSCURO, COLOR_RESALTADO},
    };

    /** Paleta neutra para chips "sin valor" (ej. "— Sin estilo"). */
    public static final Color[] COLORES_SIN_VALOR = {COLOR_NEUTRO_CLARO, COLOR_OSCURO, COLOR_NEUTRO};

    public static final Color[] COLORES_ESTADO_ACTIVO    = {color("EAF3DE"), color("27500A"), color("639922")};
    public static final Color[] COLORES_ESTADO_INACTIVO  = {COLOR_NEUTRO_CLARO, COLOR_OSCURO, COLOR_NEUTRO};
    public static final Color[] COLORES_ESTADO_BLOQUEADO = {color("FCEBEB"), color("791F1F"), color("F09595")};
    public static final Color[] COLORES_ESTADO_PENDIENTE = {color("FAEEDA"), color("633806"), color("EF9F27")};

    public static final Color[] COLORES_FILTRO_TODOS      = {COLOR_NEUTRO_CLARO, COLOR_MUY_OSCURO, COLOR_NEUTRO};

    public static final String FAMILIA_FUENTE = "SansSerif";

    // -------------------------------------------------------------------------
    // Formatos de fecha y hora
    // -------------------------------------------------------------------------

    /** Formato de hora: HH:mm */
    public static final DateTimeFormatter FORMATO_HORA  = DateTimeFormatter.ofPattern("HH:mm");

    /** Formato de fecha corta: dd/MM/yy */
    public static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yy");

    // -------------------------------------------------------------------------
    // Colores de uso general
    // -------------------------------------------------------------------------

    /** Color gris usado como texto de marcador de posición en campos de texto. */
    public static final Color COLOR_MARCADOR = new Color(153, 153, 153);
    
    private EstiloUI() {}

    // -------------------------------------------------------------------------
    // Look and Feel
    // -------------------------------------------------------------------------

    /**
     * Aplica FlatLaf (Look and Feel moderno) con la paleta de marca PinkyPuff,
     * usando el rosa {@link #COLOR_PRIMARIO} como color de acento predominante.
     */
    public static void aplicarFlatLaf() {
        try {
            FlatLightLaf.setup();

            // Acento predominante: botones primarios, foco, selección, checks/radios
            UIManager.put("Component.accentColor", COLOR_PRIMARIO);

            // Bordes y estados de componentes (campos, combos, spinners...)
            UIManager.put("Component.borderColor",         COLOR_SECUNDARIO);
            UIManager.put("Component.disabledBorderColor", COLOR_NEUTRO);
            UIManager.put("Component.focusedBorderColor",  COLOR_PRIMARIO);

            // Texto
            UIManager.put("Label.foreground",         COLOR_MUY_OSCURO);
            UIManager.put("text",                     COLOR_MUY_OSCURO);
            UIManager.put("Component.disabledForeground", COLOR_NEUTRO);

            // Fondo general de la app (paneles, scroll, contenido de pestañas):
            // antes quedaba el gris genérico de Swing, que no combinaba con nada.
            UIManager.put("Panel.background",      Color.WHITE);
            UIManager.put("ScrollPane.background", Color.WHITE);
            UIManager.put("Viewport.background",   Color.WHITE);
            UIManager.put("control",               Color.WHITE);

            // Pestañas: base oscura (ciruela) con la pestaña activa en rosa sólido
            UIManager.put("TabbedPane.background",           COLOR_MUY_OSCURO);
            UIManager.put("TabbedPane.foreground",           COLOR_PRIMARIO_CLARO);
            UIManager.put("TabbedPane.selectedBackground",   COLOR_PRIMARIO);
            UIManager.put("TabbedPane.selectedForeground",   COLOR_MUY_OSCURO);
            UIManager.put("TabbedPane.hoverColor",           COLOR_OSCURO);
            UIManager.put("TabbedPane.focusColor",           COLOR_PRIMARIO);
            UIManager.put("TabbedPane.underlineColor",       COLOR_PRIMARIO);
            UIManager.put("TabbedPane.contentAreaColor",     COLOR_NEUTRO_CLARO);
            UIManager.put("TabbedPane.disabledForeground",   COLOR_NEUTRO);

            // Selección de lista/tabla
            UIManager.put("List.selectionBackground",         COLOR_PRIMARIO);
            UIManager.put("List.selectionForeground",         COLOR_MUY_OSCURO);
            UIManager.put("Table.selectionBackground",        COLOR_PRIMARIO);
            UIManager.put("Table.selectionForeground",        COLOR_MUY_OSCURO);

            // Botones y chips: hover/presionado derivados del propio acento (no gris/azul genérico)
            UIManager.put("Button.hoverBackground",           COLOR_PRIMARIO_CLARO);
            UIManager.put("Button.pressedBackground",         COLOR_SECUNDARIO_CLARO);
            UIManager.put("Button.default.background",        COLOR_PRIMARIO);
            UIManager.put("Button.default.foreground",        COLOR_MUY_OSCURO);
            UIManager.put("Button.default.hoverBackground",   COLOR_SECUNDARIO);
            UIManager.put("Button.default.pressedBackground", mezclar(COLOR_SECUNDARIO, Color.BLACK, 0.12));
            UIManager.put("ToggleButton.hoverBackground",     COLOR_PRIMARIO_CLARO);
            UIManager.put("ToggleButton.pressedBackground",   COLOR_SECUNDARIO_CLARO);
            UIManager.put("ToggleButton.selectedBackground",  COLOR_PRIMARIO);

            // Esquinas redondeadas para una apariencia más moderna
            UIManager.put("Button.arc",       14);
            UIManager.put("Component.arc",    10);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ProgressBar.arc",  10);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.trackArc", 999);

            FlatLightLaf.updateUI();
        } catch (Exception ex) {
            Logger.getLogger(EstiloUI.class.getName()).log(Level.WARNING, "No se pudo aplicar FlatLaf", ex);
        }
    }

    // -------------------------------------------------------------------------
    // Conversión de color
    // -------------------------------------------------------------------------

    /**
     * Convierte un código hexadecimal de color a un objeto {@link Color}.
     *
     * @param hex Código hexadecimal de 6 caracteres sin el símbolo {@code #}
     *            (ej. {@code "FCE8F2"}).
     * @return Color correspondiente al código hexadecimal.
     */
    public static Color color(String hex) {
        return Color.decode("#" + hex);
    }

    // -------------------------------------------------------------------------
    // Etiquetas y textos
    // -------------------------------------------------------------------------

    /**
     * Crea una etiqueta de encabezado de sección con texto en mayúsculas,
     * fuente pequeña en negrita y color gris oscuro.
     *
     * @param texto Texto que se mostrará en la etiqueta.
     * @return {@link JLabel} configurado como encabezado de sección.
     */
    public static JLabel crearSeccionLabel(String texto) {
        JLabel etiqueta = new JLabel(texto.toUpperCase());
        etiqueta.setFont(new Font("SansSerif", Font.BOLD, 10));
        etiqueta.setForeground(COLOR_OSCURO);
        etiqueta.setAlignmentX(Component.LEFT_ALIGNMENT);
        return etiqueta;
    }

    /**
     * Crea una etiqueta de formulario con fuente pequeña y color gris medio.
     *
     * @param texto Texto descriptivo del campo de formulario.
     * @return {@link JLabel} estilizado para formularios.
     */
    public static JLabel crearLabel(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(new Font("SansSerif", Font.PLAIN, 11));
        etiqueta.setForeground(COLOR_NEUTRO);
        return etiqueta;
    }

    /**
     * Crea un panel con mensaje de estado vacío, centrado y en cursiva.
     *
     * @param texto Mensaje que se mostrará cuando no haya contenido.
     * @return {@link JPanel} con la etiqueta de mensaje vacío.
     */
    public static JPanel crearMensajeVacio(String texto) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(new Font("SansSerif", Font.ITALIC, 13));
        etiqueta.setForeground(COLOR_NEUTRO);
        panel.add(etiqueta);
        return panel;
    }

    // -------------------------------------------------------------------------
    // Componentes de selección
    // -------------------------------------------------------------------------

    /**
     * Crea un chip de selección ({@link JToggleButton}) con esquinas redondeadas:
     * contorneado cuando no está seleccionado, relleno sólido cuando sí lo está,
     * y con retroalimentación visual de hover/presionado derivada de sus propios
     * colores (para que combine, en vez de usar el resaltado gris por defecto).
     *
     * @param texto   Texto que muestra el chip.
     * @param colores Arreglo de tres colores: {@code [fondo, texto, borde/acento]}.
     * @return {@link JToggleButton} configurado como chip visual.
     */
    public static JToggleButton crearChip(String texto, Color[] colores) {
        JToggleButton boton = new JToggleButton(texto) {
            {
                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override public void mouseEntered(java.awt.event.MouseEvent e) { repaint(); }
                    @Override public void mouseExited(java.awt.event.MouseEvent e)  { repaint(); }
                });
                getModel().addChangeListener(e -> repaint());
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean presionado = getModel().isArmed() && getModel().isPressed();
                boolean sobre       = getModel().isRollover();

                Color fondo;
                Color texto2;
                if (isSelected()) {
                    fondo  = presionado ? mezclar(colores[2], Color.BLACK, 0.18)
                           : sobre      ? mezclar(colores[2], colores[1], 0.35)
                                        : colores[2];
                    texto2 = colores[1];
                } else {
                    fondo  = presionado ? mezclar(colores[0], colores[2], 0.45)
                           : sobre      ? mezclar(colores[0], colores[2], 0.25)
                                        : colores[0];
                    texto2 = colores[1];
                }

                g2.setColor(fondo);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.setColor(colores[2]);
                g2.setStroke(isSelected() ? new BasicStroke(1.4f) : new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.setColor(texto2);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(
                    getText(),
                    (getWidth()  - fm.stringWidth(getText())) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2
                );
                g2.dispose();
            }
        };
        boton.setBackground(colores[0]);
        boton.setForeground(colores[1]);
        boton.setOpaque(false);
        boton.setContentAreaFilled(false);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setFont(new Font("SansSerif", Font.PLAIN, 12));
        boton.setBorder(new EmptyBorder(5, 12, 5, 12));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return boton;
    }

    /**
     * Mezcla dos colores según una proporción (0 = {@code base}, 1 = {@code acento}).
     */
    private static Color mezclar(Color base, Color acento, double proporcion) {
        double p = Math.max(0, Math.min(1, proporcion));
        int r = (int) Math.round(base.getRed()   * (1 - p) + acento.getRed()   * p);
        int g = (int) Math.round(base.getGreen() * (1 - p) + acento.getGreen() * p);
        int b = (int) Math.round(base.getBlue()  * (1 - p) + acento.getBlue()  * p);
        return new Color(r, g, b);
    }

    // -------------------------------------------------------------------------
    // Indicadores visuales
    // -------------------------------------------------------------------------

    /**
     * Crea un badge (etiqueta pill con fondo redondeado) para mostrar
     * estados, categorías u otra información compacta.
     *
     * @param texto  Texto que muestra el badge.
     * @param fondo  Color de fondo del badge.
     * @param frente Color del texto del badge.
     * @return {@link JLabel} renderizado como badge con esquinas redondeadas.
     */
    public static JLabel crearBadge(String texto, Color fondo, Color frente) {
        JLabel badge = new JLabel(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(new Font("SansSerif", Font.BOLD, 10));
        badge.setForeground(frente);
        badge.setBackground(fondo);
        badge.setOpaque(false);
        badge.setBorder(new EmptyBorder(2, 6, 2, 6));
        return badge;
    }

    /**
     * Crea una caja de estadística con un valor destacado y una etiqueta
     * descriptiva debajo, con fondo gris claro y borde suave.
     *
     * @param valor    Valor principal a mostrar (ej. {@code "12"} o {@code "$45.00"}).
     * @param etiqueta Descripción del valor (ej. {@code "Pendientes"}).
     * @return {@link JPanel} con la caja de estadística estilizada.
     */
    public static JPanel crearStatBox(String valor, String etiqueta) {
        JPanel caja = new JPanel();
        caja.setLayout(new BoxLayout(caja, BoxLayout.Y_AXIS));
        caja.setBackground(COLOR_NEUTRO_CLARO);
        caja.setOpaque(true);
        caja.setBorder(new CompoundBorder(
            new LineBorder(COLOR_BORDE_NEUTRO, 1, true),
            new EmptyBorder(8, 4, 8, 4)
        ));
        JLabel lblValor = new JLabel(valor, SwingConstants.CENTER);
        lblValor.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblValor.setForeground(COLOR_MUY_OSCURO);
        lblValor.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel lblEtiqueta = new JLabel(etiqueta, SwingConstants.CENTER);
        lblEtiqueta.setFont(new Font("SansSerif", Font.PLAIN, 10));
        lblEtiqueta.setForeground(COLOR_NEUTRO);
        lblEtiqueta.setAlignmentX(Component.CENTER_ALIGNMENT);
        caja.add(lblValor);
        caja.add(lblEtiqueta);
        return caja;
    }

    // -------------------------------------------------------------------------
    // Botones y filas de acción
    // -------------------------------------------------------------------------

    /**
     * Crea un botón de acción con colores y borde personalizados, sin efecto
     * de foco y con cursor de mano.
     *
     * @param texto  Texto visible del botón.
     * @param fondo  Color de fondo del botón.
     * @param frente Color del texto del botón.
     * @param borde  Color del borde del botón.
     * @return {@link JButton} estilizado como botón de acción.
     */
    public static JButton crearBotonAccion(String texto, Color fondo, Color frente, Color borde) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("SansSerif", Font.PLAIN, 11));
        boton.setForeground(frente);
        boton.setBackground(fondo);
        boton.setOpaque(true);
        boton.setFocusPainted(false);
        boton.setBorder(new CompoundBorder(
            new LineBorder(borde, 1, true),
            new EmptyBorder(2, 7, 2, 7)
        ));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return boton;
    }

    /**
     * Crea una fila de configuración con un texto descriptivo a la izquierda
     * y un botón "Eliminar" a la derecha.
     *
     * @param texto      Texto que describe el elemento de configuración.
     * @param alEliminar Acción que se ejecuta al pulsar el botón eliminar.
     * @return {@link JPanel} con la fila de configuración estilizada.
     */
    public static JPanel crearFilaConfig(String texto, Runnable alEliminar) {
        JPanel fila = new JPanel(new java.awt.BorderLayout(8, 0)) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);
        fila.setBorder(new EmptyBorder(3, 0, 3, 0));
        fila.setOpaque(false);

        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(new Font("SansSerif", Font.PLAIN, 12));
        fila.add(etiqueta, java.awt.BorderLayout.CENTER);

        JButton btnEliminar = new JButton("Eliminar");
        btnEliminar.setFont(new Font("SansSerif", Font.PLAIN, 11));
        btnEliminar.setForeground(color("791F1F"));
        btnEliminar.setBackground(color("FCEBEB"));
        btnEliminar.setOpaque(true);
        btnEliminar.setFocusPainted(false);
        btnEliminar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnEliminar.addActionListener(e -> alEliminar.run());
        fila.add(btnEliminar, java.awt.BorderLayout.EAST);
        return fila;
    }

    /**
     * Crea un separador visual horizontal con relleno arriba y abajo.
     *
     * @param alturaSuperior Espacio en píxeles sobre el separador.
     * @param alturaInferior Espacio en píxeles bajo el separador.
     * @return {@link JPanel} que actúa como divisor de secciones con borde inferior.
     */
    public static JPanel crearDivisor(int alturaSuperior, int alturaInferior) {
        JPanel divisor = new JPanel();
        divisor.setOpaque(false);
        divisor.setAlignmentX(Component.LEFT_ALIGNMENT);
        divisor.setMaximumSize(new Dimension(Integer.MAX_VALUE, alturaSuperior + 1 + alturaInferior));
        divisor.setBorder(new MatteBorder(alturaSuperior, 0, alturaInferior, 0, COLOR_BORDE_NEUTRO));
        return divisor;
    }

    public static Color[] coloresEstadoCliente(String estado) {
        if ("INACTIVO".equals(estado)) return COLORES_ESTADO_INACTIVO;
        if ("BLOQUEADO".equals(estado)) return COLORES_ESTADO_BLOQUEADO;
        return COLORES_ESTADO_ACTIVO;
    }

    public static String etiquetaEstadoCliente(String estado) {
        if ("INACTIVO".equals(estado)) return "Inactivo";
        if ("BLOQUEADO".equals(estado)) return "Bloqueado";
        return "Activo";
    }

    public static JPanel crearTarjetaCliente(Cliente cliente, Runnable alEditar, Runnable alCambiarEstado, Runnable alEliminar) {
        String estado = cliente.getEstado() != null ? cliente.getEstado() : "ACTIVO";
        Color[] coloresEstado = coloresEstadoCliente(estado);
        String etiquetaEstado = etiquetaEstadoCliente(estado);

        JPanel tarjeta = new JPanel(new BorderLayout(8, 0)) {
            @Override public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        tarjeta.setAlignmentX(Component.LEFT_ALIGNMENT);
        tarjeta.setBorder(new CompoundBorder(
            new LineBorder(coloresEstado[2], 1, true),
            new EmptyBorder(8, 12, 8, 12)));

        JPanel informacion = new JPanel();
        informacion.setLayout(new BoxLayout(informacion, BoxLayout.Y_AXIS));
        informacion.setOpaque(false);

        JPanel filaNombre = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        filaNombre.setOpaque(false);
        filaNombre.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblNombre = new JLabel(cliente.getNombre());
        lblNombre.setFont(new Font(FAMILIA_FUENTE, Font.BOLD, 13));
        filaNombre.add(lblNombre);
        filaNombre.add(crearBadge(etiquetaEstado, coloresEstado[0], coloresEstado[1]));
        informacion.add(filaNombre);

        if (cliente.getTelefono() != null && !cliente.getTelefono().isEmpty()) {
            JLabel lblTelefono = new JLabel("Tel: " + cliente.getTelefono());
            lblTelefono.setFont(new Font(FAMILIA_FUENTE, Font.PLAIN, 11));
            lblTelefono.setForeground(COLOR_NEUTRO);
            informacion.add(lblTelefono);
        }
        if (cliente.getDescripcion() != null && !cliente.getDescripcion().isEmpty()) {
            JLabel lblDescripcion = new JLabel(cliente.getDescripcion());
            lblDescripcion.setFont(new Font(FAMILIA_FUENTE, Font.ITALIC, 11));
            lblDescripcion.setForeground(COLOR_NEUTRO);
            informacion.add(lblDescripcion);
        }
        tarjeta.add(informacion, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        panelBotones.setOpaque(false);

        JButton btnEditar = crearBotonAccion("Editar", COLOR_PRIMARIO_CLARO, COLOR_MUY_OSCURO, COLOR_PRIMARIO);
        btnEditar.addActionListener(e -> alEditar.run());

        JButton btnEstado = crearBotonAccion("Estado", coloresEstado[0], coloresEstado[1], coloresEstado[2]);
        btnEstado.addActionListener(e -> alCambiarEstado.run());

        JButton btnEliminar = crearBotonAccion("Eliminar", color("FCEBEB"), color("791F1F"), color("F09595"));
        btnEliminar.addActionListener(e -> alEliminar.run());

        panelBotones.add(btnEditar);
        panelBotones.add(btnEstado);
        panelBotones.add(btnEliminar);
        tarjeta.add(panelBotones, BorderLayout.EAST);

        return tarjeta;
    }
}
