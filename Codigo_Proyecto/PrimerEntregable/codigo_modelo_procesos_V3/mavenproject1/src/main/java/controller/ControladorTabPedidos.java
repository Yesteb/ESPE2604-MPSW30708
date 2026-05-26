package controller;

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
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;
import model.Administrador;
import model.EtiquetaConfig;
import persistencia.RepositorioEtiqueta;
import service.PedidoService;
import utils.EstiloUI;

class ControladorTabPedidos {

    private final JPanel panel;
    private final PedidoService servicioPedido;
    private final RepositorioEtiqueta repoEtiqueta;
    private final PlaceholderController placeholders;
    private final Administrador administrador;

    private String tipoSel;
    private String estiloSel;
    private String tallaSel;
    private final Map<String, String>  atributosSel        = new LinkedHashMap<>();
    private final Map<String, JPanel>  chipsPersonalizados = new LinkedHashMap<>();
    private final Map<JTextField, String> mapaMarcadores   = new java.util.IdentityHashMap<>();

    private JPanel panelChipsTipo;
    private JPanel panelChipsEstilo;
    private JPanel panelChipsTalla;
    private JPanel panelPreciosRapidos;
    private JPanel contenedorCategoriasCustom;

    private ButtonGroup grupoTipo;
    private ButtonGroup grupoTalla;

    private JTextField txtCliente;
    private JTextField txtPrecio;
    private JTextField txtDescripcion;
    private JTextField txtCantidad;

    ControladorTabPedidos(JPanel panel, PedidoService servicioPedido, RepositorioEtiqueta repoEtiqueta,
                          PlaceholderController placeholders, Administrador administrador) {
        this.panel          = panel;
        this.servicioPedido = servicioPedido;
        this.repoEtiqueta   = repoEtiqueta;
        this.placeholders   = placeholders;
        this.administrador  = administrador;
    }

    void construir() {
        panel.removeAll();
        panel.setLayout(new BorderLayout());

        JPanel principal = new JPanel();
        principal.setLayout(new BoxLayout(principal, BoxLayout.Y_AXIS));
        principal.setBorder(new EmptyBorder(12, 16, 12, 16));

        List<EtiquetaConfig> tipos   = repoEtiqueta.buscarPorCategoria("TIPO");
        List<EtiquetaConfig> estilos = repoEtiqueta.buscarPorCategoria("ESTILO");
        List<EtiquetaConfig> tallas  = repoEtiqueta.buscarPorCategoria("TALLA");
        List<EtiquetaConfig> precios = repoEtiqueta.buscarPorCategoria("PRECIO_RAPIDO");

        if (!tipos.isEmpty()) tipoSel = tipos.get(0).getValor();

        principal.add(EstiloUI.crearSeccionLabel("Tipo de producto"));
        panelChipsTipo = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        panelChipsTipo.setAlignmentX(Component.LEFT_ALIGNMENT);
        construirChipsTipo(tipos);
        principal.add(panelChipsTipo);
        principal.add(Box.createVerticalStrut(6));

        principal.add(EstiloUI.crearSeccionLabel("Estilo"));
        panelChipsEstilo = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        panelChipsEstilo.setAlignmentX(Component.LEFT_ALIGNMENT);
        construirChipsEstilo(estilos);
        principal.add(panelChipsEstilo);
        principal.add(Box.createVerticalStrut(6));

        principal.add(EstiloUI.crearSeccionLabel("Talla (opcional)"));
        panelChipsTalla = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        panelChipsTalla.setAlignmentX(Component.LEFT_ALIGNMENT);
        construirChipsTalla(tallas);
        principal.add(panelChipsTalla);
        principal.add(Box.createVerticalStrut(6));

        contenedorCategoriasCustom = new JPanel();
        contenedorCategoriasCustom.setLayout(new BoxLayout(contenedorCategoriasCustom, BoxLayout.Y_AXIS));
        contenedorCategoriasCustom.setAlignmentX(Component.LEFT_ALIGNMENT);
        cargarCategoriasCustom();
        principal.add(contenedorCategoriasCustom);

        principal.add(EstiloUI.crearSeccionLabel("Precio rápido"));
        panelPreciosRapidos = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        panelPreciosRapidos.setAlignmentX(Component.LEFT_ALIGNMENT);
        construirPreciosRapidos(precios);
        principal.add(panelPreciosRapidos);
        principal.add(Box.createVerticalStrut(12));

        principal.add(construirFormulario());
        principal.add(Box.createVerticalStrut(10));

        JButton btnAgregar = new JButton("Agregar producto");
        btnAgregar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnAgregar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnAgregar.setBackground(new Color(253, 155, 170));
        btnAgregar.setForeground(Color.WHITE);
        btnAgregar.setFont(btnAgregar.getFont().deriveFont(Font.BOLD, 13f));
        btnAgregar.setFocusPainted(false);
        btnAgregar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAgregar.addActionListener(e -> onAgregarProducto());
        principal.add(btnAgregar);

        JScrollPane desplazable = new JScrollPane(principal);
        desplazable.setBorder(null);
        desplazable.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(desplazable, BorderLayout.CENTER);
        panel.revalidate();
    }

    void recargarChips() {
        if (panelChipsTipo == null) return;
        tipoSel   = null;
        estiloSel = null;
        tallaSel  = null;

        List<EtiquetaConfig> tipos   = repoEtiqueta.buscarPorCategoria("TIPO");
        List<EtiquetaConfig> estilos = repoEtiqueta.buscarPorCategoria("ESTILO");
        List<EtiquetaConfig> tallas  = repoEtiqueta.buscarPorCategoria("TALLA");
        List<EtiquetaConfig> precios = repoEtiqueta.buscarPorCategoria("PRECIO_RAPIDO");

        if (!tipos.isEmpty()) tipoSel = tipos.get(0).getValor();
        construirChipsTipo(tipos);
        construirChipsEstilo(estilos);
        construirChipsTalla(tallas);
        construirPreciosRapidos(precios);
        for (Map.Entry<String, JPanel> entrada : chipsPersonalizados.entrySet())
            construirChipsCustom(entrada.getKey(), entrada.getValue());
    }

    void cargarCategoriasCustom() {
        if (contenedorCategoriasCustom == null) return;
        contenedorCategoriasCustom.removeAll();
        chipsPersonalizados.clear();
        for (EtiquetaConfig cat : repoEtiqueta.buscarPorCategoria("CATEGORIA_CUSTOM")) {
            String nombre = cat.getValor();
            contenedorCategoriasCustom.add(EstiloUI.crearSeccionLabel(nombre));
            JPanel panelChips = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
            panelChips.setAlignmentX(Component.LEFT_ALIGNMENT);
            chipsPersonalizados.put(nombre, panelChips);
            construirChipsCustom(nombre, panelChips);
            contenedorCategoriasCustom.add(panelChips);
            contenedorCategoriasCustom.add(Box.createVerticalStrut(6));
        }
        contenedorCategoriasCustom.revalidate();
        contenedorCategoriasCustom.repaint();
    }

    void eliminarAtributo(String categoria) {
        atributosSel.remove(categoria);
    }

    private void construirChipsTipo(List<EtiquetaConfig> tipos) {
        panelChipsTipo.removeAll();
        grupoTipo = new ButtonGroup();
        for (int i = 0; i < tipos.size(); i++) {
            final String valor = tipos.get(i).getValor();
            Color[] colores = EstiloUI.PALETA_TIPO[i % EstiloUI.PALETA_TIPO.length];
            JToggleButton chip = EstiloUI.crearChip(valor, colores);
            if (valor.equals(tipoSel)) chip.setSelected(true);
            chip.addActionListener(e -> tipoSel = valor);
            grupoTipo.add(chip);
            panelChipsTipo.add(chip);
        }
        panelChipsTipo.revalidate();
        panelChipsTipo.repaint();
    }

    private void construirChipsEstilo(List<EtiquetaConfig> estilos) {
        panelChipsEstilo.removeAll();
        ButtonGroup grupo = new ButtonGroup();
        JToggleButton sinEstilo = EstiloUI.crearChip("— Sin estilo",
            new Color[]{EstiloUI.color("F0F0F0"), EstiloUI.color("555555"), EstiloUI.color("AAAAAA")});
        if (estiloSel == null) sinEstilo.setSelected(true);
        sinEstilo.addActionListener(e -> estiloSel = null);
        grupo.add(sinEstilo);
        panelChipsEstilo.add(sinEstilo);
        for (int i = 0; i < estilos.size(); i++) {
            final String valor = estilos.get(i).getValor();
            Color[] colores = EstiloUI.PALETA_ESTILO[i % EstiloUI.PALETA_ESTILO.length];
            JToggleButton chip = EstiloUI.crearChip(valor, colores);
            if (valor.equals(estiloSel)) chip.setSelected(true);
            chip.addActionListener(e -> estiloSel = valor);
            grupo.add(chip);
            panelChipsEstilo.add(chip);
        }
        panelChipsEstilo.revalidate();
        panelChipsEstilo.repaint();
    }

    private void construirChipsTalla(List<EtiquetaConfig> tallas) {
        panelChipsTalla.removeAll();
        grupoTalla = new ButtonGroup();
        JToggleButton sinTalla = EstiloUI.crearChip("— Sin talla",
            new Color[]{EstiloUI.color("F0F0F0"), EstiloUI.color("555555"), EstiloUI.color("AAAAAA")});
        if (tallaSel == null) sinTalla.setSelected(true);
        sinTalla.addActionListener(e -> tallaSel = null);
        grupoTalla.add(sinTalla);
        panelChipsTalla.add(sinTalla);
        for (int i = 0; i < tallas.size(); i++) {
            final String valor = tallas.get(i).getValor();
            Color[] colores = EstiloUI.PALETA_TIPO[i % EstiloUI.PALETA_TIPO.length];
            JToggleButton chip = EstiloUI.crearChip(valor, colores);
            if (valor.equals(tallaSel)) chip.setSelected(true);
            chip.addActionListener(e -> tallaSel = valor);
            grupoTalla.add(chip);
            panelChipsTalla.add(chip);
        }
        panelChipsTalla.revalidate();
        panelChipsTalla.repaint();
    }

    private void construirPreciosRapidos(List<EtiquetaConfig> precios) {
        panelPreciosRapidos.removeAll();
        for (int i = 0; i < precios.size(); i++) {
            EtiquetaConfig config = precios.get(i);
            Color[] colores = EstiloUI.PALETA_PRECIO[i % EstiloUI.PALETA_PRECIO.length];
            String etiqueta = config.getValor() + (config.getValorNumerico() != null
                ? " $" + config.getValorNumerico().toPlainString() : "");
            JButton btn = new JButton(etiqueta) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                    g2.setColor(colores[2]);
                    g2.setStroke(new BasicStroke(0.8f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                    g2.setColor(colores[1]);
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                    g2.dispose();
                }
            };
            btn.setBackground(colores[0]);
            btn.setForeground(colores[1]);
            btn.setOpaque(false);
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setFont(new Font("SansSerif", Font.BOLD, 12));
            btn.setBorder(new EmptyBorder(5, 12, 5, 12));
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            if (config.getValorNumerico() != null) {
                final String valorStr = config.getValorNumerico().toPlainString();
                btn.addActionListener(e -> {
                    txtPrecio.setText(valorStr);
                    txtPrecio.setForeground(Color.BLACK);
                });
            }
            panelPreciosRapidos.add(btn);
        }
        panelPreciosRapidos.revalidate();
        panelPreciosRapidos.repaint();
    }

    private void construirChipsCustom(String categoria, JPanel panelChips) {
        panelChips.removeAll();
        ButtonGroup grupo = new ButtonGroup();
        JToggleButton sinValor = EstiloUI.crearChip("— Ninguno",
            new Color[]{EstiloUI.color("F0F0F0"), EstiloUI.color("555555"), EstiloUI.color("AAAAAA")});
        if (!atributosSel.containsKey(categoria)) sinValor.setSelected(true);
        sinValor.addActionListener(e -> atributosSel.remove(categoria));
        grupo.add(sinValor);
        panelChips.add(sinValor);
        List<EtiquetaConfig> elementos = repoEtiqueta.buscarPorCategoria(categoria);
        for (int i = 0; i < elementos.size(); i++) {
            final String valor = elementos.get(i).getValor();
            Color[] colores = EstiloUI.PALETA_ESTILO[i % EstiloUI.PALETA_ESTILO.length];
            JToggleButton chip = EstiloUI.crearChip(valor, colores);
            if (valor.equals(atributosSel.get(categoria))) chip.setSelected(true);
            chip.addActionListener(ev -> atributosSel.put(categoria, valor));
            grupo.add(chip);
            panelChips.add(chip);
        }
        panelChips.revalidate();
        panelChips.repaint();
    }

    private JPanel construirFormulario() {
        JPanel panelForm = new JPanel();
        panelForm.setLayout(new BoxLayout(panelForm, BoxLayout.Y_AXIS));
        panelForm.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel fila1 = new JPanel(new BorderLayout(8, 0));
        fila1.setAlignmentX(Component.LEFT_ALIGNMENT);
        fila1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel panelCliente = new JPanel(new BorderLayout(0, 2));
        panelCliente.add(EstiloUI.crearLabel("Cliente"), BorderLayout.NORTH);
        txtCliente = new JTextField();
        aplicarMarcador(txtCliente, "Nombre del cliente");
        panelCliente.add(txtCliente, BorderLayout.CENTER);

        JPanel panelPrecio = new JPanel(new BorderLayout(0, 2));
        panelPrecio.setPreferredSize(new Dimension(100, 52));
        panelPrecio.setMinimumSize(new Dimension(80, 52));
        panelPrecio.add(EstiloUI.crearLabel("Precio $"), BorderLayout.NORTH);
        txtPrecio = new JTextField();
        aplicarMarcador(txtPrecio, "0.00");
        panelPrecio.add(txtPrecio, BorderLayout.CENTER);

        fila1.add(panelCliente, BorderLayout.CENTER);
        fila1.add(panelPrecio, BorderLayout.EAST);
        panelForm.add(fila1);
        panelForm.add(Box.createVerticalStrut(6));

        JPanel fila2 = new JPanel(new BorderLayout(8, 0));
        fila2.setAlignmentX(Component.LEFT_ALIGNMENT);
        fila2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel panelDescripcion = new JPanel(new BorderLayout(0, 2));
        panelDescripcion.add(EstiloUI.crearLabel("Descripción (opcional)"), BorderLayout.NORTH);
        txtDescripcion = new JTextField();
        aplicarMarcador(txtDescripcion, "Descripción opcional");
        panelDescripcion.add(txtDescripcion, BorderLayout.CENTER);

        JPanel panelCantidad = new JPanel(new BorderLayout(0, 2));
        panelCantidad.setPreferredSize(new Dimension(80, 52));
        panelCantidad.setMinimumSize(new Dimension(60, 52));
        panelCantidad.add(EstiloUI.crearLabel("Cantidad"), BorderLayout.NORTH);
        txtCantidad = new JTextField();
        aplicarMarcador(txtCantidad, "1");
        panelCantidad.add(txtCantidad, BorderLayout.CENTER);

        fila2.add(panelDescripcion, BorderLayout.CENTER);
        fila2.add(panelCantidad, BorderLayout.EAST);
        panelForm.add(fila2);

        return panelForm;
    }

    private void onAgregarProducto() {
        String cliente   = leerCampo(txtCliente);
        String precioStr = leerCampo(txtPrecio);
        String desc      = leerCampo(txtDescripcion);
        String cantStr   = leerCampo(txtCantidad);

        if (cliente.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Ingresa el nombre del cliente.", "Aviso", JOptionPane.WARNING_MESSAGE);
            txtCliente.requestFocus();
            return;
        }
        if (tipoSel == null) {
            JOptionPane.showMessageDialog(null, "Selecciona el tipo de producto.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (precioStr.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Ingresa el precio.", "Aviso", JOptionPane.WARNING_MESSAGE);
            txtPrecio.requestFocus();
            return;
        }
        BigDecimal precio;
        int cantidad;
        try {
            precio = new BigDecimal(precioStr.replace(",", "."));
            if (precio.compareTo(BigDecimal.ZERO) <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Precio inválido.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            cantidad = Integer.parseInt(cantStr.isEmpty() ? "1" : cantStr);
            if (cantidad <= 0) cantidad = 1;
        } catch (NumberFormatException e) {
            cantidad = 1;
        }
        try {
            servicioPedido.agregarProducto(cliente, tipoSel, estiloSel, tallaSel,
                desc.isEmpty() ? null : desc, precio, cantidad, new LinkedHashMap<>(atributosSel), administrador);
            limpiarCampo(txtCliente);
            limpiarCampo(txtPrecio);
            limpiarCampo(txtDescripcion);
            limpiarCampo(txtCantidad);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al agregar: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void aplicarMarcador(JTextField campo, String textoGuia) {
        mapaMarcadores.put(campo, textoGuia);
        placeholders.aplicarACampo(campo, textoGuia);
    }

    private String leerCampo(JTextField campo) {
        if (campo.getForeground().equals(EstiloUI.COLOR_MARCADOR)) return "";
        return campo.getText().trim();
    }

    private void limpiarCampo(JTextField campo) {
        String textoGuia = mapaMarcadores.get(campo);
        if (textoGuia != null) {
            campo.setText(textoGuia);
            campo.setForeground(EstiloUI.COLOR_MARCADOR);
        } else {
            campo.setText("");
        }
    }
}
