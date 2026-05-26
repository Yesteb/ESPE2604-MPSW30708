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
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
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


    // Paletas de color [fondo, texto, borde/acento]


    // Paleta para etiquetas de tipo de producto
    public static final Color[][] PALETA_TIPO = {
        {color("FCE8F2"), color("4A0A2C"), color("C93384")},
        {color("FCE8F2"), color("4A0A2C"), color("C93384")},
        {color("FCE8F2"), color("4A0A2C"), color("C93384")},
        {color("FCE8F2"), color("4A0A2C"), color("C93384")},
        {color("FCE8F2"), color("4A0A2C"), color("C93384")},
    };

    //Paleta para etiquetas de estilo
    public static final Color[][] PALETA_ESTILO = {
        {color("FCE8F2"), color("4A0A2C"), color("C93384")},
        {color("FCE8F2"), color("4A0A2C"), color("C93384")},
        {color("FCE8F2"), color("4A0A2C"), color("C93384")},
        {color("FCE8F2"), color("4A0A2C"), color("C93384")},
        {color("FCE8F2"), color("4A0A2C"), color("C93384")},
        {color("FCE8F2"), color("4A0A2C"), color("C93384")},
        {color("FCE8F2"), color("4A0A2C"), color("C93384")},
        {color("FCE8F2"), color("4A0A2C"), color("C93384")},
    };

    //Paleta para etiquetas de precio rápido
    public static final Color[][] PALETA_PRECIO = {
        {color("FCE8F2"), color("4A0A2C"), color("C93384")},
        {color("FCE8F2"), color("4A0A2C"), color("C93384")},
        {color("FCE8F2"), color("4A0A2C"), color("C93384")},
        {color("FCE8F2"), color("4A0A2C"), color("C93384")},
    };

    public static final Color[] COLORES_ESTADO_ACTIVO    = {color("EAF3DE"), color("27500A"), color("639922")};
    public static final Color[] COLORES_ESTADO_INACTIVO  = {color("F1EFE8"), color("444444"), color("888780")};
    public static final Color[] COLORES_ESTADO_BLOQUEADO = {color("FCEBEB"), color("791F1F"), color("F09595")};

    public static final Color[] COLORES_FILTRO_TODOS      = {color("F5F5F5"), color("333333"), color("CCCCCC")};

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
        etiqueta.setForeground(color("666666"));
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
        etiqueta.setForeground(color("555555"));
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
        etiqueta.setForeground(Color.GRAY);
        panel.add(etiqueta);
        return panel;
    }

    // -------------------------------------------------------------------------
    // Componentes de selección
    // -------------------------------------------------------------------------

    /**
     * Crea un chip de selección ({@link JToggleButton}) con esquinas redondeadas
     * y borde más grueso cuando está seleccionado.
     *
     * @param texto   Texto que muestra el chip.
     * @param colores Arreglo de tres colores: {@code [fondo, texto, borde/acento]}.
     * @return {@link JToggleButton} configurado como chip visual.
     */
    public static JToggleButton crearChip(String texto, Color[] colores) {
        JToggleButton boton = new JToggleButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.setColor(colores[2]);
                g2.setStroke(isSelected() ? new BasicStroke(2f) : new BasicStroke(0.8f));
                g2.drawRoundRect(
                    isSelected() ? 1 : 0,
                    isSelected() ? 1 : 0,
                    getWidth()  - (isSelected() ? 3 : 1),
                    getHeight() - (isSelected() ? 3 : 1),
                    20, 20
                );
                g2.setColor(colores[1]);
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
        caja.setBackground(color("F5F5F5"));
        caja.setOpaque(true);
        caja.setBorder(new CompoundBorder(
            new LineBorder(color("E0E0E0"), 1, true),
            new EmptyBorder(8, 4, 8, 4)
        ));
        JLabel lblValor = new JLabel(valor, SwingConstants.CENTER);
        lblValor.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblValor.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel lblEtiqueta = new JLabel(etiqueta, SwingConstants.CENTER);
        lblEtiqueta.setFont(new Font("SansSerif", Font.PLAIN, 10));
        lblEtiqueta.setForeground(Color.GRAY);
        lblEtiqueta.setAlignmentX(Component.CENTER_ALIGNMENT);
        caja.add(lblValor);
        caja.add(lblEtiqueta);
        return caja;
    }

    /**
     * Crea una fila de gráfico de barras horizontal con etiqueta, barra de
     * progreso y valor numérico a la derecha.
     *
     * @param etiqueta   Nombre de la categoría que representa la barra.
     * @param valor      Valor actual de la barra.
     * @param maximo     Valor máximo posible de la barra.
     * @param colorBarra Color de relleno de la barra de progreso.
     * @return {@link JPanel} con la fila de gráfico estilizada.
     */
    public static JPanel crearBarraGrafico(String etiqueta, int valor, int maximo, Color colorBarra) {
        JPanel fila = new JPanel(new java.awt.BorderLayout(6, 0)) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);
        fila.setOpaque(false);

        JLabel lblEtiqueta = new JLabel(etiqueta);
        lblEtiqueta.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblEtiqueta.setPreferredSize(new Dimension(100, 20));

        JProgressBar barra = new JProgressBar(0, maximo);
        barra.setValue(valor);
        barra.setStringPainted(false);
        barra.setBackground(color("E8E8E8"));
        barra.setForeground(colorBarra);
        barra.setBorderPainted(false);
        barra.setPreferredSize(new Dimension(0, 10));

        JLabel lblValor = new JLabel(String.valueOf(valor));
        lblValor.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblValor.setForeground(Color.DARK_GRAY);
        lblValor.setPreferredSize(new Dimension(30, 20));
        lblValor.setHorizontalAlignment(SwingConstants.RIGHT);

        fila.add(lblEtiqueta, java.awt.BorderLayout.WEST);
        fila.add(barra,       java.awt.BorderLayout.CENTER);
        fila.add(lblValor,    java.awt.BorderLayout.EAST);
        return fila;
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
        divisor.setBorder(new MatteBorder(alturaSuperior, 0, alturaInferior, 0, color("E8E8E8")));
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

    public static JPanel crearTarjetaCliente(Cliente cliente, Runnable alEditar, Runnable alCambiarEstado) {
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
            lblTelefono.setForeground(Color.GRAY);
            informacion.add(lblTelefono);
        }
        if (cliente.getDescripcion() != null && !cliente.getDescripcion().isEmpty()) {
            JLabel lblDescripcion = new JLabel(cliente.getDescripcion());
            lblDescripcion.setFont(new Font(FAMILIA_FUENTE, Font.ITALIC, 11));
            lblDescripcion.setForeground(Color.GRAY);
            informacion.add(lblDescripcion);
        }
        tarjeta.add(informacion, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        panelBotones.setOpaque(false);

        JButton btnEditar = crearBotonAccion("Editar", color("E6F1FB"), color("042C53"), color("378ADD"));
        btnEditar.addActionListener(e -> alEditar.run());

        JButton btnEstado = crearBotonAccion("Estado", coloresEstado[0], coloresEstado[1], coloresEstado[2]);
        btnEstado.addActionListener(e -> alCambiarEstado.run());

        panelBotones.add(btnEditar);
        panelBotones.add(btnEstado);
        tarjeta.add(panelBotones, BorderLayout.EAST);

        return tarjeta;
    }
}
