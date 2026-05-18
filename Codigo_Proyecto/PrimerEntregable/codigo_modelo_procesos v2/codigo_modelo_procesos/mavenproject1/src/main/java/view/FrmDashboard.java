package view;

import controller.ControladorDashboard;
import java.awt.Color;
import javax.swing.JFrame;
import model.Administrador;
import persistencia.AdministradorJpaController;
import persistencia.ClienteJpaController;
import persistencia.EtiquetaConfigJpaController;
import persistencia.PedidoJpaController;
import persistencia.ProductoJpaController;
import service.PedidoService;

public class FrmDashboard extends javax.swing.JFrame {

    private static final java.util.logging.Logger registrador = java.util.logging.Logger.getLogger(FrmDashboard.class.getName());
    private ControladorDashboard controlador;

    public FrmDashboard() {
        this(null);
    }

    public FrmDashboard(Administrador administrador) {
        initComponents();
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        ClienteJpaController repoCliente     = new ClienteJpaController();
        PedidoJpaController repoPedido       = new PedidoJpaController();
        ProductoJpaController repoProducto   = new ProductoJpaController();
        EtiquetaConfigJpaController repoEtiqueta = new EtiquetaConfigJpaController();
        AdministradorJpaController repoAdmin = new AdministradorJpaController();
        PedidoService servicioPedido = new PedidoService(repoCliente, repoPedido, repoProducto);

        controlador = new ControladorDashboard(
            administrador,
            servicioPedido, repoEtiqueta, repoAdmin,
            tabPedidos, tabTodosPedidos,
            tabClientes, tabReporte, tabConfiguraciones,
            tabConfigurarPassword,
            lblContadorPendientes, lblContadorPorCobrar, lblContadorCobradosHoy
        );
        controlador.inicializar();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabPrincipal = new javax.swing.JTabbedPane();
        tabPedidos = new javax.swing.JPanel();
        tabTodosPedidos = new javax.swing.JPanel();
        tabClientes = new javax.swing.JPanel();
        tabReporte = new javax.swing.JPanel();
        tabConfiguraciones = new javax.swing.JPanel();
        tabConfigurarPassword = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        lblPendientes = new javax.swing.JLabel();
        lblContadorPendientes = new javax.swing.JLabel();
        lblCobradosHoy = new javax.swing.JLabel();
        lblPorCobrar = new javax.swing.JLabel();
        lblContadorPorCobrar = new javax.swing.JLabel();
        lblContadorCobradosHoy = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));

        tabPrincipal.setBackground(new java.awt.Color(255, 102, 153));
        tabPrincipal.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(102, 102, 102), 1, true));
        tabPrincipal.setForeground(new java.awt.Color(255, 255, 255));
        tabPrincipal.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        tabPrincipal.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabPrincipalMouseClicked(evt);
            }
        });

        tabPedidos.setBackground(new java.awt.Color(255, 255, 255));
        tabPedidos.setForeground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout tabPedidosLayout = new javax.swing.GroupLayout(tabPedidos);
        tabPedidos.setLayout(tabPedidosLayout);
        tabPedidosLayout.setHorizontalGroup(
            tabPedidosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 997, Short.MAX_VALUE)
        );
        tabPedidosLayout.setVerticalGroup(
            tabPedidosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 640, Short.MAX_VALUE)
        );

        tabPrincipal.addTab("+ Pedidos", tabPedidos);

        tabTodosPedidos.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout tabTodosPedidosLayout = new javax.swing.GroupLayout(tabTodosPedidos);
        tabTodosPedidos.setLayout(tabTodosPedidosLayout);
        tabTodosPedidosLayout.setHorizontalGroup(
            tabTodosPedidosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 997, Short.MAX_VALUE)
        );
        tabTodosPedidosLayout.setVerticalGroup(
            tabTodosPedidosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 640, Short.MAX_VALUE)
        );

        tabPrincipal.addTab("Todos", tabTodosPedidos);

        tabClientes.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout tabClientesLayout = new javax.swing.GroupLayout(tabClientes);
        tabClientes.setLayout(tabClientesLayout);
        tabClientesLayout.setHorizontalGroup(
            tabClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 997, Short.MAX_VALUE)
        );
        tabClientesLayout.setVerticalGroup(
            tabClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 640, Short.MAX_VALUE)
        );

        tabPrincipal.addTab("Clientes", tabClientes);

        tabReporte.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout tabReporteLayout = new javax.swing.GroupLayout(tabReporte);
        tabReporte.setLayout(tabReporteLayout);
        tabReporteLayout.setHorizontalGroup(
            tabReporteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 997, Short.MAX_VALUE)
        );
        tabReporteLayout.setVerticalGroup(
            tabReporteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 640, Short.MAX_VALUE)
        );

        tabPrincipal.addTab("Reporte", tabReporte);

        tabConfiguraciones.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout tabConfiguracionesLayout = new javax.swing.GroupLayout(tabConfiguraciones);
        tabConfiguraciones.setLayout(tabConfiguracionesLayout);
        tabConfiguracionesLayout.setHorizontalGroup(
            tabConfiguracionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 997, Short.MAX_VALUE)
        );
        tabConfiguracionesLayout.setVerticalGroup(
            tabConfiguracionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 640, Short.MAX_VALUE)
        );

        tabPrincipal.addTab("Configuraciones", tabConfiguraciones);

        javax.swing.GroupLayout tabConfigurarPasswordLayout = new javax.swing.GroupLayout(tabConfigurarPassword);
        tabConfigurarPassword.setLayout(tabConfigurarPasswordLayout);
        tabConfigurarPasswordLayout.setHorizontalGroup(
            tabConfigurarPasswordLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 997, Short.MAX_VALUE)
        );
        tabConfigurarPasswordLayout.setVerticalGroup(
            tabConfigurarPasswordLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 640, Short.MAX_VALUE)
        );

        tabPrincipal.addTab("Configurar credenciales", tabConfigurarPassword);

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(102, 102, 102), 1, true));
        jPanel3.setForeground(new java.awt.Color(255, 255, 255));

        lblPendientes.setText("Pendientes");

        lblContadorPendientes.setFont(new java.awt.Font("Adwaita Sans", 1, 24)); // NOI18N
        lblContadorPendientes.setForeground(new java.awt.Color(255, 102, 153));
        lblContadorPendientes.setText("0");

        lblCobradosHoy.setText("Cobrados hoy");

        lblPorCobrar.setText("Por cobrar");

        lblContadorPorCobrar.setFont(new java.awt.Font("Adwaita Sans", 1, 24)); // NOI18N
        lblContadorPorCobrar.setForeground(new java.awt.Color(255, 102, 153));
        lblContadorPorCobrar.setText("0");

        lblContadorCobradosHoy.setFont(new java.awt.Font("Adwaita Sans", 1, 24)); // NOI18N
        lblContadorCobradosHoy.setForeground(new java.awt.Color(255, 102, 153));
        lblContadorCobradosHoy.setText("0");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(102, 102, 102)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(lblContadorPendientes)
                        .addGap(164, 164, 164)
                        .addComponent(lblContadorPorCobrar))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(lblPendientes)
                        .addGap(120, 120, 120)
                        .addComponent(lblPorCobrar)))
                .addGap(210, 210, 210)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblCobradosHoy)
                    .addComponent(lblContadorCobradosHoy))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(19, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblContadorPendientes)
                    .addComponent(lblContadorPorCobrar)
                    .addComponent(lblContadorCobradosHoy))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPendientes)
                    .addComponent(lblCobradosHoy)
                    .addComponent(lblPorCobrar))
                .addGap(17, 17, 17))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabPrincipal)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tabPrincipal))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void tabPrincipalMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabPrincipalMouseClicked
  
    }//GEN-LAST:event_tabPrincipalMouseClicked

    private void txtDescripcionActionPerformed(java.awt.event.ActionEvent evt) {
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new FrmDashboard().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel3;
    private javax.swing.JLabel lblCobradosHoy;
    private javax.swing.JLabel lblContadorCobradosHoy;
    private javax.swing.JLabel lblContadorPendientes;
    private javax.swing.JLabel lblContadorPorCobrar;
    private javax.swing.JLabel lblPendientes;
    private javax.swing.JLabel lblPorCobrar;
    private javax.swing.JPanel tabClientes;
    private javax.swing.JPanel tabConfiguraciones;
    private javax.swing.JPanel tabConfigurarPassword;
    private javax.swing.JPanel tabPedidos;
    private javax.swing.JTabbedPane tabPrincipal;
    private javax.swing.JPanel tabReporte;
    private javax.swing.JPanel tabTodosPedidos;
    // End of variables declaration//GEN-END:variables
}
