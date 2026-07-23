package view;

import controller.ControladorDashboard;
import java.awt.Color;
import utils.EstiloUI;
import javax.swing.JFrame;
import model.Administrador;
import persistencia.AdministradorJpaController;
import persistencia.ClienteJpaController;
import persistencia.EstadisticasJpaController;
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
        EstadisticasJpaController repoEstadisticas = new EstadisticasJpaController();
        PedidoService servicioPedido = new PedidoService(repoCliente, repoPedido, repoProducto);

        controlador = new ControladorDashboard(
            administrador,
            servicioPedido, repoEtiqueta, repoAdmin, repoEstadisticas,
            tabPedidos, tabTodosPedidos,
            tabClientes, tabReporte, tabConfiguraciones,
            tabConfigurarPassword
        );
        controlador.inicializar();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabPrincipal = new javax.swing.JPanel();
        panelTarjetas = new javax.swing.JPanel();
        cardLayoutPrincipal = new java.awt.CardLayout();
        tabPedidos = new javax.swing.JPanel();
        tabTodosPedidos = new javax.swing.JPanel();
        tabClientes = new javax.swing.JPanel();
        tabReporte = new javax.swing.JPanel();
        tabConfiguraciones = new javax.swing.JPanel();
        tabConfigurarPassword = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));

        tabPrincipal.setLayout(new java.awt.BorderLayout());
        tabPrincipal.setBorder(new javax.swing.border.LineBorder(EstiloUI.COLOR_OSCURO, 1, true));

        panelTarjetas.setLayout(cardLayoutPrincipal);

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
            .addGap(0, 745, Short.MAX_VALUE)
        );

        panelTarjetas.add(tabPedidos, "pedidos");

        tabTodosPedidos.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout tabTodosPedidosLayout = new javax.swing.GroupLayout(tabTodosPedidos);
        tabTodosPedidos.setLayout(tabTodosPedidosLayout);
        tabTodosPedidosLayout.setHorizontalGroup(
            tabTodosPedidosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 997, Short.MAX_VALUE)
        );
        tabTodosPedidosLayout.setVerticalGroup(
            tabTodosPedidosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 745, Short.MAX_VALUE)
        );

        panelTarjetas.add(tabTodosPedidos, "todos");

        tabClientes.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout tabClientesLayout = new javax.swing.GroupLayout(tabClientes);
        tabClientes.setLayout(tabClientesLayout);
        tabClientesLayout.setHorizontalGroup(
            tabClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 997, Short.MAX_VALUE)
        );
        tabClientesLayout.setVerticalGroup(
            tabClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 745, Short.MAX_VALUE)
        );

        panelTarjetas.add(tabClientes, "clientes");

        tabReporte.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout tabReporteLayout = new javax.swing.GroupLayout(tabReporte);
        tabReporte.setLayout(tabReporteLayout);
        tabReporteLayout.setHorizontalGroup(
            tabReporteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 997, Short.MAX_VALUE)
        );
        tabReporteLayout.setVerticalGroup(
            tabReporteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 745, Short.MAX_VALUE)
        );

        panelTarjetas.add(tabReporte, "reporte");

        tabConfiguraciones.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout tabConfiguracionesLayout = new javax.swing.GroupLayout(tabConfiguraciones);
        tabConfiguraciones.setLayout(tabConfiguracionesLayout);
        tabConfiguracionesLayout.setHorizontalGroup(
            tabConfiguracionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 997, Short.MAX_VALUE)
        );
        tabConfiguracionesLayout.setVerticalGroup(
            tabConfiguracionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 745, Short.MAX_VALUE)
        );

        panelTarjetas.add(tabConfiguraciones, "config");

        javax.swing.GroupLayout tabConfigurarPasswordLayout = new javax.swing.GroupLayout(tabConfigurarPassword);
        tabConfigurarPassword.setLayout(tabConfigurarPasswordLayout);
        tabConfigurarPasswordLayout.setHorizontalGroup(
            tabConfigurarPasswordLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 997, Short.MAX_VALUE)
        );
        tabConfigurarPasswordLayout.setVerticalGroup(
            tabConfigurarPasswordLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 745, Short.MAX_VALUE)
        );

        panelTarjetas.add(tabConfigurarPassword, "credenciales");

        java.util.List<String> titulosNavegacion = java.util.List.of(
            "+ Pedidos", "Registro de pedidos", "Clientes", "Reporte", "Configuraciones", "Configurar credenciales");
        java.util.List<String> clavesNavegacion = java.util.List.of(
            "pedidos", "todos", "clientes", "reporte", "config", "credenciales");
        utils.BarraNavegacionFX barraNavegacion = new utils.BarraNavegacionFX(titulosNavegacion,
            indice -> cardLayoutPrincipal.show(panelTarjetas, clavesNavegacion.get(indice)));

        tabPrincipal.add(barraNavegacion.getPanel(), java.awt.BorderLayout.NORTH);
        tabPrincipal.add(panelTarjetas, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabPrincipal, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabPrincipal, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 782, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtDescripcionActionPerformed(java.awt.event.ActionEvent evt) {
    }

    public static void main(String args[]) {
        utils.EstiloUI.aplicarFlatLaf();
        java.awt.EventQueue.invokeLater(() -> new FrmDashboard().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel tabClientes;
    private javax.swing.JPanel tabConfiguraciones;
    private javax.swing.JPanel tabConfigurarPassword;
    private javax.swing.JPanel tabPedidos;
    private javax.swing.JPanel tabPrincipal;
    private javax.swing.JPanel tabReporte;
    private javax.swing.JPanel tabTodosPedidos;
    private javax.swing.JPanel panelTarjetas;
    private java.awt.CardLayout cardLayoutPrincipal;
    // End of variables declaration//GEN-END:variables
}
