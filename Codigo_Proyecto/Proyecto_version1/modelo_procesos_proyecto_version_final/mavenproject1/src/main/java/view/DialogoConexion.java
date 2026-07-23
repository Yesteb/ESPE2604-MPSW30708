package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import utils.ConfiguracionBD;
import utils.EstiloUI;

/**
 * Diálogo para introducir los datos de conexión a PostgreSQL.
 *
 * <p>Se muestra cuando la aplicación no consigue conectarse al arrancar: en vez
 * de morir con una traza de Hibernate, el usuario puede corregir el servidor o
 * las credenciales y guardarlas.</p>
 */
public class DialogoConexion extends JDialog {

    private final JTextField     txtHost    = new JTextField(18);
    private final JTextField     txtPuerto  = new JTextField(6);
    private final JTextField     txtBase    = new JTextField(18);
    private final JTextField     txtUsuario = new JTextField(18);
    private final JPasswordField txtClave   = new JPasswordField(18);
    private final JLabel         lblEstado  = new JLabel(" ");

    private final String titulo;
    private boolean guardado = false;

    public DialogoConexion(Window padre, ConfiguracionBD inicial, String motivo) {
        this(padre, inicial, "No se pudo conectar a la base de datos", motivo);
    }

    public DialogoConexion(Window padre, ConfiguracionBD inicial, String titulo, String motivo) {
        super(padre, "Configurar conexión a la base de datos", ModalityType.APPLICATION_MODAL);
        this.titulo = titulo;

        txtHost.setText(inicial.getHost());
        txtPuerto.setText(inicial.getPuerto());
        txtBase.setText(inicial.getBase());
        txtUsuario.setText(inicial.getUsuario());
        txtClave.setText(inicial.getPassword());

        setLayout(new BorderLayout());
        add(crearCabecera(motivo), BorderLayout.NORTH);
        add(crearFormulario(),     BorderLayout.CENTER);
        add(crearBotones(),        BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(460, getHeight()));
        setLocationRelativeTo(padre);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private JPanel crearCabecera(String motivo) {
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBorder(new EmptyBorder(14, 16, 6, 16));

        JLabel etiquetaTitulo = new JLabel(titulo);
        etiquetaTitulo.setFont(etiquetaTitulo.getFont().deriveFont(Font.BOLD, 14f));
        cabecera.add(etiquetaTitulo, BorderLayout.NORTH);

        if (motivo != null && !motivo.isBlank()) {
            JLabel detalle = new JLabel("<html><body style='width:380px'>" + escapar(motivo) + "</body></html>");
            detalle.setForeground(new Color(90, 90, 90));
            detalle.setBorder(new EmptyBorder(6, 0, 0, 0));
            cabecera.add(detalle, BorderLayout.CENTER);
        }
        return cabecera;
    }

    private JPanel crearFormulario() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(4, 16, 4, 16),
            BorderFactory.createEmptyBorder()));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 0, 4, 8);
        g.anchor = GridBagConstraints.WEST;

        int fila = 0;
        agregarFila(form, g, fila++, "Servidor",   txtHost);
        agregarFila(form, g, fila++, "Puerto",     txtPuerto);
        agregarFila(form, g, fila++, "Base",       txtBase);
        agregarFila(form, g, fila++, "Usuario",    txtUsuario);
        agregarFila(form, g, fila++, "Contraseña", txtClave);

        g.gridx = 0; g.gridy = fila; g.gridwidth = 2; g.fill = GridBagConstraints.HORIZONTAL;
        lblEstado.setBorder(new EmptyBorder(6, 0, 0, 0));
        form.add(lblEstado, g);

        return form;
    }

    private void agregarFila(JPanel form, GridBagConstraints g, int fila, String etiqueta, JTextField campo) {
        g.gridx = 0; g.gridy = fila; g.gridwidth = 1; g.fill = GridBagConstraints.NONE; g.weightx = 0;
        form.add(new JLabel(etiqueta), g);
        g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
        form.add(campo, g);
    }

    private JPanel crearBotones() {
        JButton btnProbar = boton("Probar conexión");
        btnProbar.addActionListener(e -> probar(btnProbar));

        JButton btnGuardar = boton("Guardar y continuar");
        btnGuardar.addActionListener(e -> guardar());

        JButton btnSalir = boton("Salir");
        btnSalir.addActionListener(e -> { guardado = false; dispose(); });

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 10));
        barra.setBorder(new EmptyBorder(0, 16, 6, 12));
        barra.add(btnSalir);
        barra.add(Box.createHorizontalStrut(10));
        barra.add(btnProbar);
        barra.add(btnGuardar);
        return barra;
    }

    private JButton boton(String texto) {
        JButton b = new JButton(texto);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** Prueba los datos del formulario en segundo plano, sin congelar el diálogo. */
    private void probar(JButton origen) {
        origen.setEnabled(false);
        lblEstado.setForeground(Color.DARK_GRAY);
        lblEstado.setText("Probando…");

        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() {
                return leerFormulario().probarConexion();
            }
            @Override protected void done() {
                origen.setEnabled(true);
                String error;
                try {
                    error = get();
                } catch (Exception ex) {
                    error = ex.getMessage();
                }
                if (error == null) {
                    lblEstado.setForeground(new Color(30, 110, 30));
                    lblEstado.setText("Conexión correcta.");
                } else {
                    lblEstado.setForeground(new Color(150, 30, 30));
                    lblEstado.setText("<html><body style='width:380px'>" + escapar(error) + "</body></html>");
                }
                pack();
            }
        }.execute();
    }

    private void guardar() {
        try {
            leerFormulario().guardar();
            guardado = true;
            dispose();
        } catch (Exception ex) {
            lblEstado.setForeground(new Color(150, 30, 30));
            lblEstado.setText("No se pudo guardar: " + escapar(ex.getMessage()));
        }
    }

    private ConfiguracionBD leerFormulario() {
        return new ConfiguracionBD(
            txtHost.getText().trim(),
            txtPuerto.getText().trim(),
            txtBase.getText().trim(),
            txtUsuario.getText().trim(),
            new String(txtClave.getPassword()));
    }

    /** {@code true} si el usuario guardó datos nuevos (hay que reintentar). */
    public boolean seGuardo() {
        return guardado;
    }

    private static String escapar(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /**
     * Comprueba la conexión y, mientras falle, ofrece el diálogo para corregirla.
     *
     * @return {@code true} si al final hay una conexión utilizable
     */
    public static boolean asegurarConexion() {
        while (true) {
            ConfiguracionBD actual = ConfiguracionBD.cargar();
            boolean primerArranque = !ConfiguracionBD.existeArchivo();

            // Sin archivo de configuración es el primer arranque: pedir los datos
            // directamente en vez de intentar y fallar con un error técnico.
            String error = primerArranque
                ? "Indique dónde está la base de datos. Puede comprobarlo antes de guardar."
                : actual.probarConexion();

            if (error == null) return true;

            EstiloUI.aplicarFlatLaf();
            DialogoConexion dialogo = new DialogoConexion(null, actual,
                primerArranque ? "Configuración inicial" : "No se pudo conectar a la base de datos",
                error);
            dialogo.setVisible(true);

            if (!dialogo.seGuardo()) return false;   // el usuario eligió salir
            persistencia.JPAUtil.reiniciar();        // releer con los datos nuevos
        }
    }
}
