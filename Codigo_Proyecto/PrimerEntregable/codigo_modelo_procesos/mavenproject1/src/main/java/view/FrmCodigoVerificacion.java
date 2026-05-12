package view;

import controller.ControladorFrmCodigoVerificacion;
import controller.SistemaAutenticacion;
import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class FrmCodigoVerificacion extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FrmCodigoVerificacion.class.getName());

    private JTextField txtCodigo;
    private JPasswordField txtNuevaPassword;
    private JPasswordField txtConfirmarPassword;
    private JButton btnVerificar;
    private JButton btnCancelar;
    private ControladorFrmCodigoVerificacion controlador;

    public FrmCodigoVerificacion(SistemaAutenticacion sistemaAuth, String email) {
        initComponents();
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        configurarVista(email);
        setLocationRelativeTo(null);
        controlador = new ControladorFrmCodigoVerificacion(this, sistemaAuth, email);
    }

    private void configurarVista(String email) {
        setTitle("Verificación de código");
        setResizable(false);

        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setBackground(Color.WHITE);
        panelPrincipal.setLayout(new java.awt.GridBagLayout());

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;



        JLabel lblSubtitulo = new JLabel("Código enviado a: " + email);
        lblSubtitulo.setFont(new Font("Tw Cen MT", Font.PLAIN, 13));
        lblSubtitulo.setForeground(new Color(100, 100, 100));
        gbc.gridy = 1; gbc.insets = new java.awt.Insets(0, 20, 10, 20);
        panelPrincipal.add(lblSubtitulo, gbc);

        JLabel lblCodigo = new JLabel("Código de verificación");
        lblCodigo.setFont(new Font("Tw Cen MT", Font.BOLD, 15));
        gbc.gridy = 2; gbc.insets = new java.awt.Insets(10, 20, 2, 20);
        panelPrincipal.add(lblCodigo, gbc);

        txtCodigo = new JTextField();
        txtCodigo.setFont(new Font("Tw Cen MT", Font.PLAIN, 14));
        txtCodigo.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 200, 200)));
        txtCodigo.setPreferredSize(new java.awt.Dimension(340, 30));
        gbc.gridy = 3; gbc.insets = new java.awt.Insets(0, 20, 10, 20);
        panelPrincipal.add(txtCodigo, gbc);

        JLabel lblNuevaPassword = new JLabel("Nueva contraseña");
        lblNuevaPassword.setFont(new Font("Tw Cen MT", Font.BOLD, 15));
        gbc.gridy = 4; gbc.insets = new java.awt.Insets(10, 20, 2, 20);
        panelPrincipal.add(lblNuevaPassword, gbc);

        txtNuevaPassword = new JPasswordField();
        txtNuevaPassword.setFont(new Font("Tw Cen MT", Font.PLAIN, 14));
        txtNuevaPassword.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 200, 200)));
        txtNuevaPassword.setPreferredSize(new java.awt.Dimension(340, 30));
        gbc.gridy = 5; gbc.insets = new java.awt.Insets(0, 20, 10, 20);
        panelPrincipal.add(txtNuevaPassword, gbc);

        JLabel lblConfirmar = new JLabel("Confirmar contraseña");
        lblConfirmar.setFont(new Font("Tw Cen MT", Font.BOLD, 15));
        gbc.gridy = 6; gbc.insets = new java.awt.Insets(10, 20, 2, 20);
        panelPrincipal.add(lblConfirmar, gbc);

        txtConfirmarPassword = new JPasswordField();
        txtConfirmarPassword.setFont(new Font("Tw Cen MT", Font.PLAIN, 14));
        txtConfirmarPassword.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 200, 200)));
        txtConfirmarPassword.setPreferredSize(new java.awt.Dimension(340, 30));
        txtConfirmarPassword.addActionListener(e -> controlador.manejarVerificacion());
        gbc.gridy = 7; gbc.insets = new java.awt.Insets(0, 20, 15, 20);
        panelPrincipal.add(txtConfirmarPassword, gbc);

        JPanel panelBotones = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 0));
        panelBotones.setBackground(Color.WHITE);

        btnVerificar = new JButton("Verificar");
        btnVerificar.setFont(new Font("Tw Cen MT", Font.BOLD, 14));
        btnVerificar.setBackground(new Color(253, 155, 170));
        btnVerificar.setForeground(new Color(51, 51, 51));
        btnVerificar.setBorderPainted(false);
        btnVerificar.setPreferredSize(new java.awt.Dimension(130, 38));
        btnVerificar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnVerificar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btnVerificar.setBackground(Color.PINK); }
            public void mouseExited(java.awt.event.MouseEvent evt) { btnVerificar.setBackground(new Color(253, 155, 170)); }
        });
        btnVerificar.addActionListener(e -> controlador.manejarVerificacion());

        btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Tw Cen MT", Font.PLAIN, 14));
        btnCancelar.setBackground(new Color(230, 230, 230));
        btnCancelar.setForeground(new Color(51, 51, 51));
        btnCancelar.setBorderPainted(false);
        btnCancelar.setPreferredSize(new java.awt.Dimension(100, 38));
        btnCancelar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCancelar.addActionListener(e -> controlador.manejarCancelar());

        panelBotones.add(btnVerificar);
        panelBotones.add(btnCancelar);

        gbc.gridy = 8; gbc.insets = new java.awt.Insets(5, 20, 20, 20);
        panelPrincipal.add(panelBotones, gbc);

        getContentPane().removeAll();
        getContentPane().add(panelPrincipal);
        pack();
    }

    public String getCodigoIngresado() {
        return txtCodigo.getText().trim();
    }

    public String getNuevaPasswordIngresada() {
        return new String(txtNuevaPassword.getPassword()).trim();
    }

    public String getConfirmarPasswordIngresada() {
        return new String(txtConfirmarPassword.getPassword()).trim();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jPasswordField1 = new javax.swing.JPasswordField();
        jPasswordField2 = new javax.swing.JPasswordField();
        jSeparator1 = new javax.swing.JSeparator();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setText("Verificación de código");

        jLabel2.setText("Codigo enviado a: usuario@correo.com");

        jButton1.setText("jButton1");

        jButton2.setText("jButton2");

        jTextField1.setText("jTextField1");

        jPasswordField1.setText("jPasswordField1");

        jPasswordField2.setText("jPasswordField2");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(166, 166, 166)
                        .addComponent(jPasswordField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(163, 163, 163)
                        .addComponent(jLabel1))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(106, 106, 106)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jButton1)
                            .addComponent(jLabel2)
                            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField1, javax.swing.GroupLayout.Alignment.LEADING))
                        .addGap(47, 47, 47)
                        .addComponent(jButton2))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(157, 157, 157)
                        .addComponent(jPasswordField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(221, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addComponent(jLabel1)
                .addGap(29, 29, 29)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(62, 62, 62)
                .addComponent(jPasswordField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPasswordField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 106, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addGap(86, 86, 86))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new FrmCodigoVerificacion(new controller.SistemaAutenticacion(), "test@correo.com").setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPasswordField jPasswordField1;
    private javax.swing.JPasswordField jPasswordField2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
