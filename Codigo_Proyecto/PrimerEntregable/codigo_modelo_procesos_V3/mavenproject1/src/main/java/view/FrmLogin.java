package view;

import controller.PlaceholderController;
import java.awt.Color;
import javax.swing.JOptionPane;

public class FrmLogin extends javax.swing.JFrame {

    int xRaton, yRaton;

    private controller.ControladorFrmLogin controlador;
    private FrmDashboard tablero;

    private static final java.util.logging.Logger registrador = java.util.logging.Logger.getLogger(FrmLogin.class.getName());

    public FrmLogin() {
        initComponents();
        setLocationRelativeTo(null);
        this.tablero = tablero;
        PlaceholderController controladorMarcador = new PlaceholderController();
        controladorMarcador.aplicarACampo(txtUsername, "Ingrese su nombre de usuario");
        controladorMarcador.aplicarACampoPassword(txtPassword, "***************************************");
        controlador = new controller.ControladorFrmLogin(this, tablero);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    @SuppressWarnings("unchecked")
    private void initComponents() {

        jPanel4 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        LblUsuario = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtPassword = new javax.swing.JPasswordField();
        txtUsername = new javax.swing.JTextField();
        SptUsuario = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        btnLogin = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        PnlExit = new javax.swing.JPanel();
        LblExit = new javax.swing.JLabel();
        LblUsuario1 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        lblResetPassword = new javax.swing.JLabel();

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        jLabel4.setBackground(new java.awt.Color(18, 18, 18));
        jLabel4.setFont(new java.awt.Font("Roboto Light", 1, 14));
        jLabel4.setText("Contraseña");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setLocationByPlatform(true);
        setUndecorated(true);
        setResizable(false);
        addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                formFocusGained(evt);
            }
        });

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        LblUsuario.setBackground(new java.awt.Color(18, 18, 18));
        LblUsuario.setFont(new java.awt.Font("Tw Cen MT", 1, 18));
        LblUsuario.setText("USUARIO");

        jLabel5.setBackground(new java.awt.Color(18, 18, 18));
        jLabel5.setFont(new java.awt.Font("Tw Cen MT", 1, 18));
        jLabel5.setText("CONTRASEÑA");

        txtPassword.setFont(new java.awt.Font("Tw Cen MT", 0, 12));
        txtPassword.setForeground(new java.awt.Color(153, 153, 153));
        txtPassword.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        txtPassword.setText("***************************************");
        txtPassword.setBorder(null);
        txtPassword.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtPasswordFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtPasswordFocusLost(evt);
            }
        });
        txtPassword.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                txtPasswordMousePressed(evt);
            }
        });
        txtPassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPasswordActionPerformed(evt);
            }
        });

        txtUsername.setFont(new java.awt.Font("Tw Cen MT", 0, 12));
        txtUsername.setForeground(new java.awt.Color(153, 153, 153));
        txtUsername.setText("Ingrese su nombre de usuario");
        txtUsername.setToolTipText("");
        txtUsername.setBorder(null);
        txtUsername.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        txtUsername.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtUsernameFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtUsernameFocusLost(evt);
            }
        });
        txtUsername.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                txtUsernameMousePressed(evt);
            }
        });
        txtUsername.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtUsernameActionPerformed(evt);
            }
        });

        SptUsuario.setBackground(new java.awt.Color(255, 255, 255));
        SptUsuario.setForeground(new java.awt.Color(0, 0, 0));

        jSeparator2.setBackground(new java.awt.Color(255, 255, 255));
        jSeparator2.setForeground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(LblUsuario)
                            .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 274, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(17, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(SptUsuario, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                                .addGap(2, 2, 2)
                                .addComponent(txtPassword, javax.swing.GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE)))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(LblUsuario)
                .addGap(18, 18, 18)
                .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(SptUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(42, 42, 42)
                .addComponent(jLabel5)
                .addGap(18, 18, 18)
                .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(26, Short.MAX_VALUE))
        );

        btnLogin.setBackground(new java.awt.Color(253, 155, 170));
        btnLogin.setFont(new java.awt.Font("Tw Cen MT", 1, 18));
        btnLogin.setForeground(new java.awt.Color(51, 51, 51));
        btnLogin.setText("Iniciar Sesion");
        btnLogin.setBorder(null);
        btnLogin.setBorderPainted(false);
        btnLogin.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnLogin.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnLogin.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnLoginMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnLoginMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnLoginMouseExited(evt);
            }
        });
        btnLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoginActionPerformed(evt);
            }
        });

        jPanel7.setBackground(new java.awt.Color(255, 255, 255));
        jPanel7.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                jPanel7MouseDragged(evt);
            }
        });
        jPanel7.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jPanel7MousePressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 452, Short.MAX_VALUE)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 29, Short.MAX_VALUE)
        );

        PnlExit.setBackground(new java.awt.Color(251, 211, 212));
        PnlExit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                PnlExitMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                PnlExitMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                PnlExitMousePressed(evt);
            }
        });

        LblExit.setBackground(new java.awt.Color(251, 211, 212));
        LblExit.setFont(new java.awt.Font("Tw Cen MT Condensed Extra Bold", 0, 18));
        LblExit.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LblExit.setText("X");
        LblExit.setAlignmentY(0.0F);
        LblExit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                LblExitMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                LblExitMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                LblExitMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                LblExitMousePressed(evt);
            }
        });

        javax.swing.GroupLayout PnlExitLayout = new javax.swing.GroupLayout(PnlExit);
        PnlExit.setLayout(PnlExitLayout);
        PnlExitLayout.setHorizontalGroup(
            PnlExitLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PnlExitLayout.createSequentialGroup()
                .addComponent(LblExit, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        PnlExitLayout.setVerticalGroup(
            PnlExitLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PnlExitLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(LblExit, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        LblUsuario1.setBackground(new java.awt.Color(18, 18, 18));
        LblUsuario1.setFont(new java.awt.Font("Tw Cen MT", 1, 36));
        LblUsuario1.setText("Pinky");

        jLabel1.setFont(new java.awt.Font("Tw Cen MT", 1, 36));
        jLabel1.setForeground(new java.awt.Color(253, 137, 155));
        jLabel1.setText("Puff");

        lblResetPassword.setBackground(new java.awt.Color(18, 18, 18));
        lblResetPassword.setFont(new java.awt.Font("Tw Cen MT", 0, 14));
        lblResetPassword.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblResetPassword.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                        .addComponent(LblUsuario1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel1)
                        .addGap(33, 33, 33))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(134, 134, 134))))
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(PnlExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 82, Short.MAX_VALUE)
                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addGap(177, 177, 177)
                                .addComponent(btnLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addGap(193, 193, 193)
                                .addComponent(lblResetPassword)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(PnlExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(25, 25, 25)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(LblUsuario1, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addGap(70, 70, 70)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(46, 46, 46)
                .addComponent(btnLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblResetPassword)
                .addContainerGap(142, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnLoginActionPerformed(java.awt.event.ActionEvent evt) {
        controlador.manejarLogin();
    }

    private void txtUsernameActionPerformed(java.awt.event.ActionEvent evt) {
        txtPassword.requestFocus();
    }

    private void txtUsernameFocusLost(java.awt.event.FocusEvent evt) {
    }

    private void txtUsernameFocusGained(java.awt.event.FocusEvent evt) {
    }

    private void txtPasswordActionPerformed(java.awt.event.ActionEvent evt) {
        controlador.manejarLogin();
    }

    private void txtPasswordFocusLost(java.awt.event.FocusEvent evt) {
    }

    private void txtPasswordFocusGained(java.awt.event.FocusEvent evt) {
    }

    private void LblExitMousePressed(java.awt.event.MouseEvent evt) {
    }

    private void LblExitMouseEntered(java.awt.event.MouseEvent evt) {
        PnlExit.setBackground(Color.PINK);
    }

    private void PnlExitMousePressed(java.awt.event.MouseEvent evt) {
    }

    private void PnlExitMouseExited(java.awt.event.MouseEvent evt) {
    }

    private void PnlExitMouseEntered(java.awt.event.MouseEvent evt) {
    }

    private void LblExitMouseClicked(java.awt.event.MouseEvent evt) {
        controlador.manejarSalida();
    }

    private void LblExitMouseExited(java.awt.event.MouseEvent evt) {
        PnlExit.setBackground(new Color(251, 211, 212));
    }

    private void btnLoginMouseEntered(java.awt.event.MouseEvent evt) {
        btnLogin.setBackground(Color.PINK);
    }

    private void btnLoginMouseExited(java.awt.event.MouseEvent evt) {
        btnLogin.setBackground(new Color(253, 155, 170));
    }

    private void txtUsernameMousePressed(java.awt.event.MouseEvent evt) {
    }

    private void txtPasswordMousePressed(java.awt.event.MouseEvent evt) {
    }

    private void formFocusGained(java.awt.event.FocusEvent evt) {
    }

    private void btnLoginMouseClicked(java.awt.event.MouseEvent evt) {
        deshabilitarBotonLogin();
    }

    private void jPanel7MousePressed(java.awt.event.MouseEvent evt) {
        xRaton = evt.getX();
        yRaton = evt.getY();
    }

    private void jPanel7MouseDragged(java.awt.event.MouseEvent evt) {
        int posX = evt.getXOnScreen();
        int posY = evt.getYOnScreen();
        this.setLocation(posX - xRaton, posY - yRaton);
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new FrmLogin().setVisible(true));
    }

    public String getUsuarioIngresado() {
        if (txtUsername.getForeground().equals(new java.awt.Color(153, 153, 153))) {
            return "";
        }
        return txtUsername.getText().trim();
    }

    public String getContrasenaIngresada() {
        if (txtPassword.getForeground().equals(new java.awt.Color(153, 153, 153))) {
            return "";
        }
        return new String(txtPassword.getPassword()).trim();
    }

    public void habilitarBotonLogin() {
        btnLogin.setEnabled(true);
    }

    public void deshabilitarBotonLogin() {
        btnLogin.setEnabled(false);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel LblExit;
    private javax.swing.JLabel LblUsuario;
    private javax.swing.JLabel LblUsuario1;
    private javax.swing.JPanel PnlExit;
    private javax.swing.JSeparator SptUsuario;
    private javax.swing.JButton btnLogin;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel lblResetPassword;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtUsername;
    // End of variables declaration//GEN-END:variables
}
