package controller;

import java.awt.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import model.Administrador;
import model.Cliente;
import model.EtiquetaConfig;
import model.Pedido;
import model.Producto;
import persistencia.RepositorioAdministrador;
import persistencia.RepositorioEstadisticas;
import persistencia.RepositorioEstadisticas.SerieMensual;
import persistencia.RepositorioEtiqueta;
import persistencia.exceptions.NonexistentEntityException;
import service.EstadisticasService;
import service.ObservadorDatos;
import service.PedidoService;
import utils.EstiloUI;
import utils.GraficoFX;
import utils.HashContrasena;
import utils.PaginadorPanel;

public class ControladorDashboard implements ObservadorDatos {

    private static final Color COLOR_MARCADOR = EstiloUI.COLOR_MARCADOR;

    private final JPanel tabPedidos, tabTodosPedidos;
    private final JPanel tabClientes, tabReporte, tabConfiguraciones, tabConfigurarPassword;

    private final PedidoService servicioPedido;
    private final RepositorioEtiqueta repoEtiqueta;
    private final RepositorioAdministrador repoAdministrador;
    private final RepositorioEstadisticas repoEstadisticas;
    private final PlaceholderController controladorMarcador = new PlaceholderController();
    private Administrador administradorActual;

    private String filtroTodos     = "TODOS";
    private String filtroClientes  = "TODOS";

    private static final int TAM_PAGINA_PEDIDOS  = 8;
    private static final int TAM_PAGINA_CLIENTES = 8;
    private static final int TAM_PAGINA_CONFIG   = 6;
    /** Barras como máximo en cada gráfico de etiquetas. */
    private static final int MAX_ETIQUETAS_GRAFICO = 15;

    private JPanel contenidoTodos, contenidoClientes, contenidoReporte;
    private PaginadorPanel<Pedido> paginadorTodos;
    private PaginadorPanel<Cliente> paginadorClientes;

    private ControladorTabPedidos tabPedidosCtrl;

    public ControladorDashboard(
            Administrador administradorActual,
            PedidoService servicioPedido,
            RepositorioEtiqueta repoEtiqueta,
            RepositorioAdministrador repoAdministrador,
            RepositorioEstadisticas repoEstadisticas,
            JPanel tabPedidos, JPanel tabTodosPedidos,
            JPanel tabClientes, JPanel tabReporte, JPanel tabConfiguraciones,
            JPanel tabConfigurarPassword) {
        this.administradorActual   = administradorActual;
        this.servicioPedido        = servicioPedido;
        this.repoEtiqueta          = repoEtiqueta;
        this.repoAdministrador     = repoAdministrador;
        this.repoEstadisticas      = repoEstadisticas;
        this.tabPedidos            = tabPedidos;
        this.tabTodosPedidos       = tabTodosPedidos;
        this.tabClientes           = tabClientes;
        this.tabReporte            = tabReporte;
        this.tabConfiguraciones    = tabConfiguraciones;
        this.tabConfigurarPassword = tabConfigurarPassword;
        this.tabPedidosCtrl = new ControladorTabPedidos(
            tabPedidos, servicioPedido, repoEtiqueta,
            controladorMarcador, administradorActual);
        servicioPedido.agregarObservador(this);
    }

    @Override
    public void actualizar() {
        refrescarTodo();
    }

    public void inicializar() {
        repoEtiqueta.inicializarPorDefecto();
        tabPedidosCtrl.construir();
        setupTabTodos();
        setupTabClientes();
        setupTabReporte();
        setupTabConfiguraciones();
        setupTabConfigurarPassword();
    }

    private void setupTabTodos() {
        tabTodosPedidos.removeAll();
        tabTodosPedidos.setLayout(new BorderLayout());

        JPanel barraFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 6));
        barraFiltros.setBorder(new EmptyBorder(4, 6, 0, 6));
        String[] filtros   = {"TODOS", "PENDIENTE", "COBRADO", "CANCELADO"};
        String[] etiquetas = {"Todos", "Pendientes", "Cobrados", "Cancelados"};
        Color[][] coloresFiltros = {
            EstiloUI.COLORES_FILTRO_TODOS,
            EstiloUI.COLORES_ESTADO_PENDIENTE,
            EstiloUI.COLORES_ESTADO_ACTIVO,
            EstiloUI.COLORES_ESTADO_BLOQUEADO
        };
        ButtonGroup grupoFiltro = new ButtonGroup();
        for (int i = 0; i < filtros.length; i++) {
            final String filtro = filtros[i];
            JToggleButton chip = EstiloUI.crearChip(etiquetas[i], coloresFiltros[i]);
            if (filtro.equals(filtroTodos)) chip.setSelected(true);
            chip.addActionListener(e -> { filtroTodos = filtro; paginadorTodos.reiniciar(); });
            grupoFiltro.add(chip);
            barraFiltros.add(chip);
        }

        // Fuente paginada: la base de datos entrega solo los pedidos de la página
        // visible, en vez de traerlos todos con sus productos para mostrar ocho.
        paginadorTodos = new PaginadorPanel<>(TAM_PAGINA_PEDIDOS,
            new PaginadorPanel.FuenteDatos<Pedido>() {
                @Override public List<Pedido> pagina(int desde, int cantidad) {
                    return servicioPedido.obtenerPaginaPedidos(filtroTodos, desde, cantidad);
                }
                @Override public int total() {
                    return servicioPedido.contarPedidos(filtroTodos);
                }
            },
            this::crearTarjetaPedido,
            () -> crearMensajeVacio("Sin pedidos"));
        contenidoTodos = paginadorTodos.getPanelContenido();

        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(new Color(255, 255, 255));
        contenedor.add(contenidoTodos, BorderLayout.NORTH);
        JScrollPane desplazable = new JScrollPane(contenedor);
        desplazable.setBorder(null);
        desplazable.getVerticalScrollBar().setUnitIncrement(16);
        paginadorTodos.vincularScroll(desplazable);

        tabTodosPedidos.add(barraConRefrescar(barraFiltros, this::refrescarTodos), BorderLayout.NORTH);
        tabTodosPedidos.add(desplazable, BorderLayout.CENTER);
        tabTodosPedidos.add(paginadorTodos.getPanelControles(), BorderLayout.SOUTH);
    }

    private void setupTabClientes() {
        tabClientes.removeAll();
        tabClientes.setLayout(new BorderLayout());

        JPanel barraFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 6));
        barraFiltros.setBorder(new EmptyBorder(4, 6, 0, 6));

        String[] filtros   = {"TODOS",    "ACTIVO",    "INACTIVO",    "BLOQUEADO"};
        String[] etiquetas = {"Todos",    "Activos",   "Inactivos",   "Bloqueados"};
        Color[][] coloresFiltros = {
            EstiloUI.COLORES_FILTRO_TODOS,
            EstiloUI.COLORES_ESTADO_ACTIVO,
            EstiloUI.COLORES_ESTADO_INACTIVO,
            EstiloUI.COLORES_ESTADO_BLOQUEADO
        };

        ButtonGroup grupoFiltroClientes = new ButtonGroup();
        for (int i = 0; i < filtros.length; i++) {
            final String filtro = filtros[i];
            JToggleButton chip = EstiloUI.crearChip(etiquetas[i], coloresFiltros[i]);
            if (filtro.equals(filtroClientes)) chip.setSelected(true);
            chip.addActionListener(e -> { filtroClientes = filtro; paginadorClientes.reiniciar(); });
            grupoFiltroClientes.add(chip);
            barraFiltros.add(chip);
        }

        paginadorClientes = new PaginadorPanel<>(TAM_PAGINA_CLIENTES,
            new PaginadorPanel.FuenteDatos<Cliente>() {
                @Override public List<Cliente> pagina(int desde, int cantidad) {
                    return servicioPedido.obtenerPaginaClientes(filtroClientes, desde, cantidad);
                }
                @Override public int total() {
                    return servicioPedido.contarClientes(filtroClientes);
                }
            },
            this::crearTarjetaCliente,
            () -> crearMensajeVacio("Sin clientes registrados"));
        contenidoClientes = paginadorClientes.getPanelContenido();

        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(new Color(255, 255, 255));
        contenedor.add(contenidoClientes, BorderLayout.NORTH);
        JScrollPane desplazable = new JScrollPane(contenedor);
        desplazable.setBorder(null);
        desplazable.getVerticalScrollBar().setUnitIncrement(16);
        paginadorClientes.vincularScroll(desplazable);

        tabClientes.add(barraConRefrescar(barraFiltros, this::refrescarClientes), BorderLayout.NORTH);
        tabClientes.add(desplazable, BorderLayout.CENTER);
        tabClientes.add(paginadorClientes.getPanelControles(), BorderLayout.SOUTH);
    }

    private void setupTabReporte() {
        tabReporte.removeAll();
        tabReporte.setLayout(new BorderLayout());

        JPanel barraExportar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        barraExportar.setBorder(new EmptyBorder(4, 8, 0, 8));

        JButton btnRefrescar = crearBotonAccion("Refrescar", EstiloUI.COLOR_NEUTRO_CLARO, EstiloUI.COLOR_MUY_OSCURO, EstiloUI.COLOR_NEUTRO);
        btnRefrescar.addActionListener(e -> refrescarReporte());

        JButton btnPdfTodos = crearBotonAccion("Exportar todo (PDF)", EstiloUI.COLOR_PRIMARIO_CLARO, EstiloUI.COLOR_MUY_OSCURO, EstiloUI.COLOR_PRIMARIO);
        btnPdfTodos.addActionListener(e ->
            ReportePDF.generarReportePedidos(servicioPedido.obtenerTodos(), "Reporte General", tabReporte));

        JButton btnPdfPendientes = crearBotonAccion("Exportar pendientes (PDF)", c("FAEEDA"), c("633806"), c("EF9F27"));
        btnPdfPendientes.addActionListener(e ->
            ReportePDF.generarReportePedidos(servicioPedido.obtenerTodosConFiltro("PENDIENTE"), "Pedidos Pendientes", tabReporte));

        JButton btnPdfCobrados = crearBotonAccion("Exportar cobrados (PDF)", c("EAF3DE"), c("173404"), c("639922"));
        btnPdfCobrados.addActionListener(e ->
            ReportePDF.generarReportePedidos(servicioPedido.obtenerTodosConFiltro("COBRADO"), "Pedidos Cobrados", tabReporte));

        barraExportar.add(btnRefrescar);
        barraExportar.add(btnPdfPendientes);
        barraExportar.add(btnPdfCobrados);
        barraExportar.add(btnPdfTodos);

        contenidoReporte = new JPanel();
        contenidoReporte.setLayout(new BoxLayout(contenidoReporte, BoxLayout.Y_AXIS));
        contenidoReporte.setBorder(new EmptyBorder(8, 8, 8, 8));
        contenidoReporte.setBackground(new Color(255, 255, 255));

        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(new Color(255, 255, 255));
        contenedor.add(contenidoReporte, BorderLayout.NORTH);
        JScrollPane desplazable = new JScrollPane(contenedor);
        desplazable.setBorder(null);
        desplazable.getVerticalScrollBar().setUnitIncrement(16);

        tabReporte.add(barraExportar, BorderLayout.NORTH);
        tabReporte.add(desplazable, BorderLayout.CENTER);
        refrescarReporte();
    }

    private void setupTabConfiguraciones() {
        setupTabConfiguraciones(null);
    }
    private void setupTabConfiguraciones(String categoriaAEnfocar){
        tabConfiguraciones.removeAll();
        tabConfiguraciones.setLayout(new BorderLayout());
        
        JPanel principal = new JPanel();
        principal.setLayout(new BoxLayout(principal, BoxLayout.Y_AXIS));
        principal.setBorder(new EmptyBorder(10, 14, 10, 14));
        principal.setBackground(new Color(255, 255, 255));
        
        principal.add(crearWidgetNuevaCategoria());
        principal.add(Box.createVerticalStrut(12));
        principal.add(crearSeccionConfig("Tipos de producto", "TIPO", EstiloUI.PALETA_TIPO));
        principal.add(Box.createVerticalStrut(12));
        
        JPanel seccionEnfocada = null;
        for(EtiquetaConfig cat : repoEtiqueta.buscarPorCategoria("CATEGORIA_CUSTOM")){
            final String nombre = cat.getValor();
            principal.add(Box.createVerticalStrut(12));
            JPanel seccionCustom = crearSeccionConfig(nombre, nombre, EstiloUI.PALETA_ESTILO, () -> eliminarCategoriaCustom(nombre));
            principal.add(seccionCustom);
            if(nombre.equalsIgnoreCase(categoriaAEnfocar)){
                seccionEnfocada = seccionCustom;
            }
            
        }
        principal.add(Box.createVerticalStrut(12));
        principal.add(crearSeccionPreciosConfig());
        
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(new Color(255, 255, 255));
        contenedor.add(principal, BorderLayout.NORTH);
        JScrollPane desplazable = new JScrollPane(contenedor);
        desplazable.setBorder(null);
        desplazable.getVerticalScrollBar().setUnitIncrement(16);

        // Refrescar aquí reconstruye la pestaña entera: las secciones se generan
        // a partir de las categorías guardadas, así que hay que volver a leerlas.
        JPanel barraSuperior = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 6));
        barraSuperior.setBorder(new EmptyBorder(4, 8, 0, 8));
        barraSuperior.add(crearBotonRefrescar(() -> {
            setupTabConfiguraciones();
            tabPedidosCtrl.cargarCategoriasCustom();
            tabPedidosCtrl.recargarChips();
        }));

        tabConfiguraciones.add(barraSuperior, BorderLayout.NORTH);
        tabConfiguraciones.add(desplazable, BorderLayout.CENTER);
        tabConfiguraciones.revalidate();
        tabConfiguraciones.repaint();
        
        if(seccionEnfocada != null){
            final JPanel objetivo = seccionEnfocada;
            SwingUtilities.invokeLater(() -> objetivo.scrollRectToVisible(new Rectangle(0,0,objetivo.getWidth(),objetivo.getHeight())));
        }
        
    }

    private void setupTabConfigurarPassword() {
        tabConfigurarPassword.removeAll();
        tabConfigurarPassword.setLayout(new BorderLayout());

        JPanel principal = new JPanel();
        principal.setLayout(new BoxLayout(principal, BoxLayout.Y_AXIS));
        principal.setBorder(new EmptyBorder(10, 14, 10, 14));
        principal.setBackground(new Color(255, 255, 255));

        principal.add(crearSeccionGestionAdministrador());

        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(new Color(255, 255, 255));
        contenedor.add(principal, BorderLayout.NORTH);
        JScrollPane desplazable = new JScrollPane(contenedor);
        desplazable.setBorder(null);
        desplazable.getVerticalScrollBar().setUnitIncrement(16);
        tabConfigurarPassword.add(desplazable, BorderLayout.CENTER);
        tabConfigurarPassword.revalidate();
    }

    private JPanel crearSeccionConfig(String titulo, String categoria, Color[][] paleta) {
        return crearSeccionConfig(titulo, categoria, paleta, null);
    }

    private JPanel crearSeccionConfig(String titulo, String categoria, Color[][] paleta, Runnable alEliminarCategoria) {
        JPanel seccion = new JPanel();
        seccion.setLayout(new BoxLayout(seccion, BoxLayout.Y_AXIS));
        seccion.setAlignmentX(Component.LEFT_ALIGNMENT);
        seccion.setBorder(new CompoundBorder(
            new LineBorder(EstiloUI.COLOR_BORDE_NEUTRO, 1, true),
            new EmptyBorder(8, 10, 8, 10)));

        if (alEliminarCategoria != null) {
            JPanel filaEncabezado = new JPanel(new BorderLayout(8, 0));
            filaEncabezado.setAlignmentX(Component.LEFT_ALIGNMENT);
            filaEncabezado.setOpaque(false);
            filaEncabezado.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
            filaEncabezado.add(crearSeccionLabel(titulo), BorderLayout.WEST);
            JButton btnElimCat = crearBotonAccion("Eliminar categoría", c("FCEBEB"), c("791F1F"), c("F09595"));
            btnElimCat.addActionListener(e -> alEliminarCategoria.run());
            filaEncabezado.add(btnElimCat, BorderLayout.EAST);
            seccion.add(filaEncabezado);
        } else {
            seccion.add(crearSeccionLabel(titulo));
        }
        seccion.add(Box.createVerticalStrut(6));

        PaginadorPanel<EtiquetaConfig> paginador = new PaginadorPanel<>(
            TAM_PAGINA_CONFIG, () -> repoEtiqueta.buscarPorCategoria(categoria),
            () -> crearMensajeVacio("Sin etiquetas"));
        paginador.setRenderizador(ec -> crearFilaConfig(ec.getValor(), () -> {
            try {
                repoEtiqueta.eliminar(ec.getId());
                paginador.recargar();
                tabPedidosCtrl.recargarChips();
            } catch (NonexistentEntityException ex) {
                JOptionPane.showMessageDialog(null, "Error al eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }));
        paginador.recargar();

        seccion.add(paginador.getPanelContenido());
        seccion.add(paginador.getPanelControles());
        seccion.add(Box.createVerticalStrut(8));

        JPanel filaAgregar = new JPanel(new BorderLayout(6, 0));
        filaAgregar.setAlignmentX(Component.LEFT_ALIGNMENT);
        filaAgregar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        JTextField txtNuevaEtiqueta = new JTextField();
        controladorMarcador.aplicarACampo(txtNuevaEtiqueta, "Nueva etiqueta...");
        JButton btnAdd = new JButton("Agregar");
        btnAdd.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btnAdd.addActionListener(e -> {
            String valor = txtNuevaEtiqueta.getForeground().equals(COLOR_MARCADOR) ? "" : txtNuevaEtiqueta.getText().trim();
            if (valor.isEmpty()) return;
            List<EtiquetaConfig> actuales = repoEtiqueta.buscarPorCategoria(categoria);
            boolean existe = actuales.stream().anyMatch(ec -> ec.getValor().equalsIgnoreCase(valor));
            if (existe) {
                JOptionPane.showMessageDialog(null, "Ya existe esa etiqueta.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            repoEtiqueta.crear(new EtiquetaConfig(categoria, valor, null, actuales.size()));
            txtNuevaEtiqueta.setText("");
            paginador.recargar();
            tabPedidosCtrl.recargarChips();
        });
        filaAgregar.add(txtNuevaEtiqueta, BorderLayout.CENTER);
        filaAgregar.add(btnAdd, BorderLayout.EAST);
        seccion.add(filaAgregar);

        return seccion;
    }

    private JPanel crearSeccionPreciosConfig() {
        JPanel seccion = new JPanel();
        seccion.setLayout(new BoxLayout(seccion, BoxLayout.Y_AXIS));
        seccion.setAlignmentX(Component.LEFT_ALIGNMENT);
        seccion.setBorder(new CompoundBorder(
            new LineBorder(EstiloUI.COLOR_BORDE_NEUTRO, 1, true),
            new EmptyBorder(8, 10, 8, 10)));

        seccion.add(crearSeccionLabel("Precios rápidos"));
        seccion.add(Box.createVerticalStrut(6));

        PaginadorPanel<EtiquetaConfig> paginadorPrecios = new PaginadorPanel<>(
            TAM_PAGINA_CONFIG, () -> repoEtiqueta.buscarPorCategoria("PRECIO_RAPIDO"),
            () -> crearMensajeVacio("Sin precios"));
        paginadorPrecios.setRenderizador(config -> {
            String etiqueta = config.getValor() + " — $" +
                (config.getValorNumerico() != null ? config.getValorNumerico().toPlainString() : "?");
            return crearFilaConfig(etiqueta, () -> {
                try {
                    repoEtiqueta.eliminar(config.getId());
                    paginadorPrecios.recargar();
                    tabPedidosCtrl.recargarChips();
                } catch (NonexistentEntityException ex) {
                    JOptionPane.showMessageDialog(null, "Error al eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        });
        paginadorPrecios.recargar();

        seccion.add(paginadorPrecios.getPanelContenido());
        seccion.add(paginadorPrecios.getPanelControles());
        seccion.add(Box.createVerticalStrut(8));

        JPanel filaAgregar = new JPanel(new BorderLayout(4, 0));
        filaAgregar.setAlignmentX(Component.LEFT_ALIGNMENT);
        filaAgregar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        JTextField txtEtiqueta = new JTextField();
        controladorMarcador.aplicarACampo(txtEtiqueta, "Etiqueta...");
        JTextField txtValor = new JTextField();
        controladorMarcador.aplicarACampo(txtValor, "Precio...");
        txtValor.setPreferredSize(new Dimension(70, 28));
        JButton btnAdd = new JButton("Agregar");
        btnAdd.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btnAdd.addActionListener(e -> {
            String etiqueta = txtEtiqueta.getForeground().equals(COLOR_MARCADOR) ? "" : txtEtiqueta.getText().trim();
            String valorStr = txtValor.getForeground().equals(COLOR_MARCADOR)   ? "" : txtValor.getText().trim();
            if (etiqueta.isEmpty() || valorStr.isEmpty()) return;
            try {
                BigDecimal valor = new BigDecimal(valorStr);
                List<EtiquetaConfig> actuales = repoEtiqueta.buscarPorCategoria("PRECIO_RAPIDO");
                repoEtiqueta.crear(new EtiquetaConfig("PRECIO_RAPIDO", etiqueta, valor, actuales.size()));
                txtEtiqueta.setText("");
                txtValor.setText("");
                paginadorPrecios.recargar();
                tabPedidosCtrl.recargarChips();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Precio inválido.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        JPanel campos = new JPanel(new BorderLayout(4, 0));
        campos.add(txtEtiqueta, BorderLayout.CENTER);
        campos.add(txtValor, BorderLayout.EAST);
        filaAgregar.add(campos, BorderLayout.CENTER);
        filaAgregar.add(btnAdd, BorderLayout.EAST);
        seccion.add(filaAgregar);

        return seccion;
    }

    private void refrescarTodos() {
        paginadorTodos.recargar();
    }

    private void refrescarClientes() {
        paginadorClientes.recargar();
    }

    private void refrescarReporte() {
        contenidoReporte.removeAll();

        // Los cuatro indicadores salen de una consulta de una fila. Antes cada uno
        // costaba cargar una tabla entera con sus productos para contar o sumar.
        repoEstadisticas.refrescarSiHaceFalta();
        RepositorioEstadisticas.Kpis kpis = repoEstadisticas.kpis();

        JPanel filasEstadisticas = new JPanel(new GridLayout(1, 4, 8, 0));
        filasEstadisticas.setAlignmentX(Component.LEFT_ALIGNMENT);
        filasEstadisticas.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        filasEstadisticas.add(crearStatBox(String.valueOf(kpis.pendientes()), "Pendientes"));
        filasEstadisticas.add(crearStatBox(String.format("$%.2f", kpis.porCobrar()), "Por cobrar"));
        filasEstadisticas.add(crearStatBox(String.valueOf(kpis.cobradosHoy()), "Cobrados hoy"));
        filasEstadisticas.add(crearStatBox(String.format("$%.2f", kpis.totalCobrado()), "Total cobrado"));
        contenidoReporte.add(filasEstadisticas);
        contenidoReporte.add(Box.createVerticalStrut(12));

        // --- Resumen mensual (estilo contable): Pedidas / Compradas / Canceladas ---
        // Las series las agrega la base de datos con funciones de ventana: una
        // consulta por estado en lugar de seis recorridos de la lista completa.
        final int MESES_RESUMEN = 6;
        SerieMensual serieTodas      = repoEstadisticas.evolucionMensual(null,        MESES_RESUMEN);
        SerieMensual serieCobradas   = repoEstadisticas.evolucionMensual("COBRADO",   MESES_RESUMEN);
        SerieMensual serieCanceladas = repoEstadisticas.evolucionMensual("CANCELADO", MESES_RESUMEN);
        List<String> meses = serieTodas.etiquetas();

        contenidoReporte.add(crearSeccionLabel("Pedidos por mes (cantidad)"));
        contenidoReporte.add(Box.createVerticalStrut(6));
        contenidoReporte.add(GraficoFX.crearBarrasMensuales(
            meses,
            serieTodas.conteo(),
            serieCobradas.conteo(),
            serieCanceladas.conteo(),
            false));
        contenidoReporte.add(Box.createVerticalStrut(12));

        contenidoReporte.add(crearSeccionLabel("Pedidos por mes (monto $)"));
        contenidoReporte.add(Box.createVerticalStrut(6));
        contenidoReporte.add(GraficoFX.crearBarrasMensuales(
            meses,
            serieTodas.monto(),
            serieCobradas.monto(),
            serieCanceladas.monto(),
            true));
        contenidoReporte.add(Box.createVerticalStrut(16));

        // --- Etiquetas más pedidas / compradas / canceladas, por cada categoría ---
        for (String categoria : EstadisticasService.categorias(repoEtiqueta)) {
            String nombre = EstadisticasService.nombreLegible(categoria);
            contenidoReporte.add(EstiloUI.crearDivisor(4, 8));
            contenidoReporte.add(crearSeccionLabel(nombre));
            contenidoReporte.add(Box.createVerticalStrut(6));

            agregarSubGraficoEtiqueta(nombre + " más pedidas",    categoria, null,        EstiloUI.COLOR_PRIMARIO);
            agregarSubGraficoEtiqueta(nombre + " más compradas",  categoria, "COBRADO",   c("639922"));
            agregarSubGraficoEtiqueta(nombre + " más canceladas", categoria, "CANCELADO", c("791F1F"));
        }

        contenidoReporte.revalidate();
        contenidoReporte.repaint();
    }

    private void agregarSubGraficoEtiqueta(String titulo, String categoria, String estado, Color color) {
        Map<String, Integer> conteo =
            repoEstadisticas.conteoPorEtiqueta(categoria, estado, MAX_ETIQUETAS_GRAFICO);
        contenidoReporte.add(crearLabel(titulo));
        contenidoReporte.add(Box.createVerticalStrut(3));
        if (conteo.isEmpty()) {
            contenidoReporte.add(crearMensajeVacio("Sin datos aún"));
        } else {
            contenidoReporte.add(GraficoFX.crearBarrasHorizontales(conteo, color));
        }
        contenidoReporte.add(Box.createVerticalStrut(10));
    }

    private void refrescarTodo() {
        refrescarTodos();
        refrescarClientes();
        refrescarReporte();
    }

    private void onMarcarCobrado(String pedidoId) {
        int respuesta = JOptionPane.showConfirmDialog(null, "¿Marcar pedido como cobrado?",
            "Confirmar", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) return;
        try {
            servicioPedido.marcarCobrado(pedidoId);
        } catch (NonexistentEntityException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    private void onMarcarCancelado(String pedidoId) {
        int respuesta = JOptionPane.showConfirmDialog(null, "¿Cancelar este pedido?",
            "Confirmar", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) return;
        try {
            servicioPedido.marcarCancelado(pedidoId);
        } catch (NonexistentEntityException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    private void onReactivarPedido(String pedidoId) {
        try {
            servicioPedido.reactivarPedido(pedidoId);
        } catch (NonexistentEntityException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    private void onEliminarPedido(String pedidoId) {
        int respuesta = JOptionPane.showConfirmDialog(null,
            "¿Eliminar este pedido y todos sus productos?",
            "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (respuesta != JOptionPane.YES_OPTION) return;
        try {
            servicioPedido.eliminarPedido(pedidoId);
        } catch (NonexistentEntityException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    private void onEliminarProducto(String productoId) {
        try {
            servicioPedido.eliminarProducto(productoId);
        } catch (NonexistentEntityException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    private void onCambiarEstadoCliente(Cliente cliente) {
        String[] valores   = {"ACTIVO", "INACTIVO", "BLOQUEADO"};
        String[] etiquetas = {"Activo", "Inactivo", "Bloqueado"};

        JComboBox<String> comboEstado = new JComboBox<>(etiquetas);
        String estadoActual = cliente.getEstado() != null ? cliente.getEstado() : "ACTIVO";
        for (int i = 0; i < valores.length; i++) {
            if (valores[i].equals(estadoActual)) { comboEstado.setSelectedIndex(i); break; }
        }

        JPanel formulario = new JPanel(new GridLayout(0, 1, 4, 4));
        formulario.add(crearLabel("Cliente: " + cliente.getNombre()));
        formulario.add(crearLabel("Nuevo estado:"));
        formulario.add(comboEstado);

        int resultado = JOptionPane.showConfirmDialog(null, formulario,
            "Cambiar estado del cliente", JOptionPane.OK_CANCEL_OPTION);
        if (resultado != JOptionPane.OK_OPTION) return;

        String nuevoEstado = valores[comboEstado.getSelectedIndex()];
        try {
            servicioPedido.cambiarEstadoCliente(cliente.getId(), nuevoEstado);
        } catch (NonexistentEntityException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onEliminarCliente(Cliente cliente) {
        int respuesta = JOptionPane.showConfirmDialog(null,
            "¿Eliminar a \"" + cliente.getNombre() + "\" y todos sus pedidos?",
            "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (respuesta != JOptionPane.YES_OPTION) return;
        try {
            servicioPedido.eliminarCliente(cliente.getId());
        } catch (NonexistentEntityException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onEditarCliente(Cliente cliente) {
        JTextField campoNombre = new JTextField(cliente.getNombre());
        JTextField campoTelefono = new JTextField(cliente.getTelefono() != null ? cliente.getTelefono() : "");
        JTextArea campoDesc = new JTextArea(cliente.getDescripcion() != null ? cliente.getDescripcion() : "", 3, 20);
        campoDesc.setLineWrap(true);

        JPanel formulario = new JPanel(new GridLayout(0, 1, 4, 4));
        formulario.add(crearLabel("Nombre")); formulario.add(campoNombre);
        formulario.add(crearLabel("Teléfono")); formulario.add(campoTelefono);
        formulario.add(crearLabel("Descripción")); formulario.add(new JScrollPane(campoDesc));

        int resultado = JOptionPane.showConfirmDialog(null, formulario, "Editar cliente", JOptionPane.OK_CANCEL_OPTION);
        if (resultado != JOptionPane.OK_OPTION) return;

        String nombre = campoNombre.getText().trim();
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(null, "El nombre no puede estar vacío.");
            return;
        }
        cliente.setNombre(nombre);
        cliente.setTelefono(campoTelefono.getText().trim().isEmpty() ? null : campoTelefono.getText().trim());
        cliente.setDescripcion(campoDesc.getText().trim().isEmpty() ? null : campoDesc.getText().trim());
        try {
            servicioPedido.actualizarCliente(cliente);
        } catch (NonexistentEntityException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    private JPanel crearTarjetaPedido(Pedido pedido) {
        String estado = pedido.getEstado();
        Color colorFondo = "PENDIENTE".equals(estado) ? c("FAEEDA")
            : "COBRADO".equals(estado) ? c("EAF3DE") : EstiloUI.COLOR_NEUTRO_CLARO;
        Color colorBorde = "PENDIENTE".equals(estado) ? c("EF9F27")
            : "COBRADO".equals(estado) ? c("639922") : EstiloUI.COLOR_NEUTRO;

        JPanel tarjeta = new JPanel(new BorderLayout(4, 4)) {
            @Override public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        tarjeta.setBackground(colorFondo);
        tarjeta.setBorder(new CompoundBorder(
            new MatteBorder(0, 4, 0, 0, colorBorde),
            new EmptyBorder(8, 10, 8, 10)));
        tarjeta.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);
        JLabel lblNombre = new JLabel(pedido.getCliente().getNombre());
        lblNombre.setFont(new Font("SansSerif", Font.BOLD, 13));
        JLabel lblTotal = new JLabel(String.format("$%.2f", pedido.getTotal()));
        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 13));
        encabezado.add(lblNombre, BorderLayout.WEST);
        encabezado.add(lblTotal, BorderLayout.EAST);
        tarjeta.add(encabezado, BorderLayout.NORTH);

        JPanel listaProductos = new JPanel();
        listaProductos.setLayout(new BoxLayout(listaProductos, BoxLayout.Y_AXIS));
        listaProductos.setOpaque(false);
        if (pedido.getProductos().isEmpty()) {
            JLabel lblVacio = new JLabel("Sin productos");
            lblVacio.setFont(new Font("SansSerif", Font.ITALIC, 11));
            lblVacio.setForeground(EstiloUI.COLOR_NEUTRO);
            listaProductos.add(lblVacio);
        } else {
            for (Producto prod : pedido.getProductos()) {
                listaProductos.add(crearFilaProducto(prod));
            }
        }
        tarjeta.add(listaProductos, BorderLayout.CENTER);

        JPanel pie = new JPanel(new BorderLayout(4, 0));
        pie.setOpaque(false);

        JPanel panelBadges = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        panelBadges.setOpaque(false);
        Color[] coloresEstado = "PENDIENTE".equals(estado)
            ? new Color[]{c("FAEEDA"), c("633806")}
            : "COBRADO".equals(estado)
                ? new Color[]{c("EAF3DE"), c("173404")}
                : new Color[]{EstiloUI.COLOR_NEUTRO_CLARO, EstiloUI.COLOR_OSCURO};
        panelBadges.add(crearBadge(estado, coloresEstado[0], coloresEstado[1]));

        String textoFecha = pedido.getFechaRegistro() != null
            ? pedido.getFechaRegistro().format(EstiloUI.FORMATO_HORA) + " · " + pedido.getFechaRegistro().format(EstiloUI.FORMATO_FECHA)
            : "";
        JLabel lblFecha = new JLabel(textoFecha);
        lblFecha.setFont(new Font("SansSerif", Font.PLAIN, 10));
        lblFecha.setForeground(EstiloUI.COLOR_NEUTRO);
        panelBadges.add(lblFecha);
        pie.add(panelBadges, BorderLayout.WEST);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 0));
        panelBotones.setOpaque(false);

        if ("PENDIENTE".equals(estado)) {
            JButton btnCobrado = crearBotonAccion("Cobrado", c("EAF3DE"), c("27500A"), c("639922"));
            btnCobrado.addActionListener(e -> onMarcarCobrado(pedido.getId()));
            JButton btnCancelar = crearBotonAccion("Cancelar", c("FCEBEB"), c("791F1F"), c("F09595"));
            btnCancelar.addActionListener(e -> onMarcarCancelado(pedido.getId()));
            panelBotones.add(btnCobrado);
            panelBotones.add(btnCancelar);
        } else if ("CANCELADO".equals(estado)) {
            JButton btnReactivar = crearBotonAccion("Reactivar", c("FAEEDA"), c("633806"), c("EF9F27"));
            btnReactivar.addActionListener(e -> onReactivarPedido(pedido.getId()));
            panelBotones.add(btnReactivar);
        }

        JButton btnTicket = crearBotonAccion("Ticket PDF", EstiloUI.COLOR_SECUNDARIO_CLARO, EstiloUI.COLOR_MUY_OSCURO, EstiloUI.COLOR_SECUNDARIO);
        btnTicket.addActionListener(e -> ReportePDF.generarTicketPedido(pedido, contenidoTodos));
        panelBotones.add(btnTicket);

        JButton btnEliminar = crearBotonAccion("Eliminar", c("FCEBEB"), c("791F1F"), c("F09595"));
        btnEliminar.addActionListener(e -> onEliminarPedido(pedido.getId()));
        panelBotones.add(btnEliminar);

        pie.add(panelBotones, BorderLayout.EAST);
        tarjeta.add(pie, BorderLayout.SOUTH);

        return tarjeta;
    }

    private JPanel crearFilaProducto(Producto prod) {
        JPanel fila = new JPanel(new BorderLayout(4, 0)) {
            @Override public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        fila.setOpaque(false);
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);
        fila.setBorder(new EmptyBorder(3, 0, 3, 0));

        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        izq.setOpaque(false);

        if (prod.getTipo() != null && !prod.getTipo().isEmpty()) {
            List<EtiquetaConfig> tipos = repoEtiqueta.buscarPorCategoria("TIPO");
            int idx = 0;
            for (int i = 0; i < tipos.size(); i++) if (tipos.get(i).getValor().equals(prod.getTipo())) { idx = i; break; }
            Color[] colores = EstiloUI.PALETA_TIPO[idx % EstiloUI.PALETA_TIPO.length];
            izq.add(crearBadge(prod.getTipo(), colores[0], colores[1]));
        }
        if (prod.getEstilo() != null && !prod.getEstilo().isEmpty()) {
            List<EtiquetaConfig> estilos = repoEtiqueta.buscarPorCategoria("ESTILO");
            int idx = 0;
            for (int i = 0; i < estilos.size(); i++) if (estilos.get(i).getValor().equals(prod.getEstilo())) { idx = i; break; }
            Color[] colores = EstiloUI.PALETA_ESTILO[idx % EstiloUI.PALETA_ESTILO.length];
            izq.add(crearBadge(prod.getEstilo(), colores[0], colores[1]));
        }
        if (prod.getTalla() != null && !prod.getTalla().isEmpty())
            izq.add(crearBadge(prod.getTalla(), EstiloUI.COLOR_NEUTRO_CLARO, EstiloUI.COLOR_OSCURO));
        if (prod.getAtributos() != null) {
            for (Map.Entry<String, String> attr : prod.getAtributos().entrySet()) {
                if (attr.getValue() != null && !attr.getValue().isEmpty())
                    izq.add(crearBadge(attr.getValue(), EstiloUI.COLOR_SECUNDARIO_CLARO, EstiloUI.COLOR_OSCURO));
            }
        }

        String textoDesc = (prod.getDescripcion() != null && !prod.getDescripcion().isEmpty())
            ? prod.getDescripcion() : "";
        JLabel lblDesc = new JLabel(textoDesc);
        lblDesc.setFont(new Font("SansSerif", Font.PLAIN, 12));
        izq.add(lblDesc);

        fila.add(izq, BorderLayout.CENTER);

        JPanel der = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        der.setOpaque(false);
        if (prod.getCantidad() > 1) {
            JLabel lblCant = new JLabel("x" + prod.getCantidad());
            lblCant.setFont(new Font("SansSerif", Font.PLAIN, 10));
            lblCant.setForeground(EstiloUI.COLOR_NEUTRO);
            der.add(lblCant);
        }
        JLabel lblPrecio = new JLabel(String.format("$%.2f",
            prod.getPrecio().multiply(BigDecimal.valueOf(prod.getCantidad()))));
        lblPrecio.setFont(new Font("SansSerif", Font.BOLD, 12));
        der.add(lblPrecio);

        JButton btnEliminar = new JButton("×");
        btnEliminar.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnEliminar.setForeground(c("791F1F"));
        btnEliminar.setBackground(c("FCEBEB"));
        btnEliminar.setFocusPainted(false);
        btnEliminar.setBorder(new CompoundBorder(
            new LineBorder(c("F09595"), 1, true),
            new EmptyBorder(0, 5, 0, 5)));
        btnEliminar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnEliminar.addActionListener(e -> onEliminarProducto(prod.getId()));
        der.add(btnEliminar);

        fila.add(der, BorderLayout.EAST);
        return fila;
    }

    private JPanel crearTarjetaCliente(Cliente cliente) {
        return EstiloUI.crearTarjetaCliente(
            cliente,
            () -> onEditarCliente(cliente),
            () -> onCambiarEstadoCliente(cliente),
            () -> onEliminarCliente(cliente)
        );
    }

    private JPanel crearWidgetNuevaCategoria() {
        JPanel seccion = new JPanel();
        seccion.setLayout(new BoxLayout(seccion, BoxLayout.Y_AXIS));
        seccion.setAlignmentX(Component.LEFT_ALIGNMENT);
        seccion.setBorder(new CompoundBorder(
            new LineBorder(EstiloUI.COLOR_PRIMARIO, 1, true),
            new EmptyBorder(8, 10, 8, 10)));

        seccion.add(crearSeccionLabel("Agregar categoría personalizada"));
        seccion.add(Box.createVerticalStrut(6));

        JPanel fila = new JPanel(new BorderLayout(6, 0));
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        JTextField txtNombreCategoria = new JTextField();
        controladorMarcador.aplicarACampo(txtNombreCategoria, "ej. Color, Material...");
        JButton btnAgregar = new JButton("Agregar categoría");
        btnAgregar.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btnAgregar.addActionListener(e -> {
            String nombre = txtNombreCategoria.getForeground().equals(COLOR_MARCADOR) ? "" : txtNombreCategoria.getText().trim();
            if (nombre.isEmpty()) return;
            String nombreMayus = nombre.toUpperCase();
            if ("TIPO".equals(nombreMayus)
                    || "PRECIO_RAPIDO".equals(nombreMayus) || "CATEGORIA_CUSTOM".equals(nombreMayus)) {
                JOptionPane.showMessageDialog(null, "Nombre reservado del sistema.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            List<EtiquetaConfig> existentes = repoEtiqueta.buscarPorCategoria("CATEGORIA_CUSTOM");
            if (existentes.stream().anyMatch(ec -> ec.getValor().equalsIgnoreCase(nombre))) {
                JOptionPane.showMessageDialog(null, "Ya existe esa categoría.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            try{
                repoEtiqueta.crear(new EtiquetaConfig("CATEGORIA_CUSTOM", nombre, null, existentes.size()));
                
                
            } catch(Exception ex){
                JOptionPane.showMessageDialog(null, "No se pudo crear la categoria" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            txtNombreCategoria.setText("");
            JOptionPane.showMessageDialog(null, "Categoria \"" + nombre + "\" creadacorrectamente.", "Categoria creada", JOptionPane.INFORMATION_MESSAGE);
            setupTabConfiguraciones(nombre);
            tabPedidosCtrl.cargarCategoriasCustom();
            });
        fila.add(txtNombreCategoria, BorderLayout.CENTER);
        fila.add(btnAgregar, BorderLayout.EAST);
        seccion.add(fila);
        return seccion;
    }

    private void eliminarCategoriaCustom(String nombre) {
        int respuesta = JOptionPane.showConfirmDialog(null,
            "¿Eliminar la categoría \"" + nombre + "\" y todos sus valores?",
            "Confirmar", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) return;
        for (EtiquetaConfig ec : repoEtiqueta.buscarPorCategoria(nombre)) {
            try { repoEtiqueta.eliminar(ec.getId()); } catch (Exception ex) { }
        }
        repoEtiqueta.buscarPorCategoria("CATEGORIA_CUSTOM").stream()
            .filter(ec -> ec.getValor().equals(nombre))
            .findFirst()
            .ifPresent(ec -> {
                try { repoEtiqueta.eliminar(ec.getId()); } catch (Exception ex) { }
            });
        tabPedidosCtrl.eliminarAtributo(nombre);
        setupTabConfiguraciones();
        tabPedidosCtrl.cargarCategoriasCustom();
    }

    private JPanel crearSeccionGestionAdministrador() {
        JPanel seccion = new JPanel();
        seccion.setLayout(new BoxLayout(seccion, BoxLayout.Y_AXIS));
        seccion.setAlignmentX(Component.LEFT_ALIGNMENT);
        seccion.setBorder(new CompoundBorder(
            new LineBorder(EstiloUI.COLOR_BORDE_NEUTRO, 1, true),
            new EmptyBorder(8, 10, 8, 10)));

        seccion.add(crearSeccionLabel("Gestión de administrador"));
        seccion.add(Box.createVerticalStrut(8));

        JPanel filaBuscar = new JPanel(new BorderLayout(6, 0));
        filaBuscar.setAlignmentX(Component.LEFT_ALIGNMENT);
        filaBuscar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        JTextField txtBuscar = new JTextField();
        controladorMarcador.aplicarACampo(txtBuscar, "Nombre de usuario o email...");
        JButton btnBuscar = new JButton("Buscar");
        btnBuscar.setFont(new Font("SansSerif", Font.PLAIN, 12));
        filaBuscar.add(txtBuscar, BorderLayout.CENTER);
        filaBuscar.add(btnBuscar, BorderLayout.EAST);
        seccion.add(filaBuscar);
        seccion.add(Box.createVerticalStrut(8));

        JPanel panelResultado = new JPanel();
        panelResultado.setLayout(new BoxLayout(panelResultado, BoxLayout.Y_AXIS));
        panelResultado.setAlignmentX(Component.LEFT_ALIGNMENT);
        seccion.add(panelResultado);

        Runnable buscar = () -> {
            String busqueda = txtBuscar.getForeground().equals(COLOR_MARCADOR) ? "" : txtBuscar.getText().trim();
            if (busqueda.isEmpty()) return;
            Administrador encontrado = repoAdministrador.buscarPorNombre(busqueda);
            if (encontrado == null) encontrado = repoAdministrador.buscarPorEmail(busqueda);
            panelResultado.removeAll();
            if (encontrado == null) {
                JLabel lbl = new JLabel("No se encontró ningún administrador.");
                lbl.setFont(new Font("SansSerif", Font.ITALIC, 12));
                lbl.setForeground(c("791F1F"));
                lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
                panelResultado.add(lbl);
            } else {
                mostrarFormEdicionAdministrador(panelResultado, encontrado);
            }
            panelResultado.revalidate();
            panelResultado.repaint();
        };

        txtBuscar.addActionListener(e -> buscar.run());
        btnBuscar.addActionListener(e -> buscar.run());

        return seccion;
    }

    private void mostrarFormEdicionAdministrador(JPanel contenedor, Administrador administrador) {
        final String correoOriginal = administrador.getEmail();

        JPanel filaBadge = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        filaBadge.setAlignmentX(Component.LEFT_ALIGNMENT);
        filaBadge.add(crearBadge("Encontrado: " + administrador.getNombre(), c("EAF3DE"), c("173404")));
        contenedor.add(filaBadge);
        contenedor.add(Box.createVerticalStrut(8));

        JPanel panelNombre = new JPanel(new BorderLayout(0, 2));
        panelNombre.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelNombre.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        panelNombre.add(crearLabel("Nombre"), BorderLayout.NORTH);
        JTextField txtNombre = new JTextField(administrador.getNombre());
        panelNombre.add(txtNombre, BorderLayout.CENTER);
        contenedor.add(panelNombre);
        contenedor.add(Box.createVerticalStrut(6));

        JPanel panelEmail = new JPanel(new BorderLayout(0, 2));
        panelEmail.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelEmail.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        panelEmail.add(crearLabel("Email"), BorderLayout.NORTH);
        JTextField txtEmail = new JTextField(administrador.getEmail());
        panelEmail.add(txtEmail, BorderLayout.CENTER);
        contenedor.add(panelEmail);
        contenedor.add(Box.createVerticalStrut(6));

        JPanel panelContrasena = new JPanel(new BorderLayout(0, 2));
        panelContrasena.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelContrasena.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        panelContrasena.add(crearLabel("Nueva contraseña (vacío = sin cambios)"), BorderLayout.NORTH);
        JPasswordField txtContrasena = new JPasswordField();
        panelContrasena.add(txtContrasena, BorderLayout.CENTER);
        contenedor.add(panelContrasena);
        contenedor.add(Box.createVerticalStrut(6));

        JPanel panelConfirmacion = new JPanel(new BorderLayout(0, 2));
        panelConfirmacion.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelConfirmacion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        panelConfirmacion.add(crearLabel("Confirmar contraseña"), BorderLayout.NORTH);
        JPasswordField txtConfirmacion = new JPasswordField();
        panelConfirmacion.add(txtConfirmacion, BorderLayout.CENTER);
        contenedor.add(panelConfirmacion);
        contenedor.add(Box.createVerticalStrut(10));

        JButton btnGuardar = new JButton("Guardar cambios");
        btnGuardar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnGuardar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnGuardar.setBackground(EstiloUI.COLOR_PRIMARIO);
        btnGuardar.setForeground(EstiloUI.COLOR_MUY_OSCURO);
        btnGuardar.setFont(btnGuardar.getFont().deriveFont(Font.BOLD, 13f));
        btnGuardar.setFocusPainted(false);
        btnGuardar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnGuardar.addActionListener(e -> {
            String nuevoNombre  = txtNombre.getText().trim();
            String nuevoEmail   = txtEmail.getText().trim();
            String contrasena   = new String(txtContrasena.getPassword());
            String confirmacion = new String(txtConfirmacion.getPassword());

            if (nuevoNombre.isEmpty()) {
                JOptionPane.showMessageDialog(null, "El nombre no puede estar vacío.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (nuevoEmail.isEmpty() || !nuevoEmail.contains("@")) {
                JOptionPane.showMessageDialog(null, "El email no es válido.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!contrasena.isEmpty() && !contrasena.equals(confirmacion)) {
                JOptionPane.showMessageDialog(null, "Las contraseñas no coinciden.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                String hash = contrasena.isEmpty() ? administrador.getContrasena() : HashContrasena.calcular(contrasena);
                if (!nuevoEmail.equals(correoOriginal)) {
                    if (repoAdministrador.buscarPorEmail(nuevoEmail) != null) {
                        JOptionPane.showMessageDialog(null, "Ese email ya está registrado.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    Administrador nuevo = new Administrador();
                    nuevo.setEmail(nuevoEmail);
                    nuevo.setNombre(nuevoNombre);
                    nuevo.setContrasena(hash);
                    repoAdministrador.crear(nuevo);
                    repoAdministrador.eliminar(correoOriginal);
                } else {
                    administrador.setNombre(nuevoNombre);
                    administrador.setContrasena(hash);
                    repoAdministrador.editar(administrador);
                }
                JOptionPane.showMessageDialog(null, "Administrador actualizado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        contenedor.add(btnGuardar);
    }

    private JLabel crearBadge(String texto, Color fondo, Color frente) {
        return EstiloUI.crearBadge(texto, fondo, frente);
    }

    private JLabel crearSeccionLabel(String texto) {
        return EstiloUI.crearSeccionLabel(texto);
    }

    private JLabel crearLabel(String texto) {
        return EstiloUI.crearLabel(texto);
    }

    private JPanel crearMensajeVacio(String texto) {
        return EstiloUI.crearMensajeVacio(texto);
    }

    private JPanel crearStatBox(String valor, String etiqueta) {
        return EstiloUI.crearStatBox(valor, etiqueta);
    }

    private JPanel crearFilaConfig(String texto, Runnable alEliminar) {
        return EstiloUI.crearFilaConfig(texto, alEliminar);
    }

    private JButton crearBotonAccion(String texto, Color fondo, Color frente, Color borde) {
        return EstiloUI.crearBotonAccion(texto, fondo, frente, borde);
    }

    /**
     * Botón "Refrescar" con el aspecto neutro que ya usaba la pestaña de reporte,
     * para que sea el mismo en todas.
     */
    private JButton crearBotonRefrescar(Runnable accion) {
        JButton boton = crearBotonAccion("Refrescar",
            EstiloUI.COLOR_NEUTRO_CLARO, EstiloUI.COLOR_MUY_OSCURO, EstiloUI.COLOR_NEUTRO);
        boton.setToolTipText("Vuelve a leer los datos de la base de datos");
        boton.addActionListener(e -> accion.run());
        return boton;
    }

    /**
     * Coloca el botón de refrescar a la derecha de una barra existente (la de
     * filtros), sin alterar el contenido que ya tenía a la izquierda.
     */
    private JPanel barraConRefrescar(JPanel contenidoIzquierda, Runnable accion) {
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 6));
        derecha.setBorder(new EmptyBorder(4, 0, 0, 6));
        derecha.add(crearBotonRefrescar(accion));

        JPanel barra = new JPanel(new BorderLayout());
        barra.add(contenidoIzquierda, BorderLayout.CENTER);
        barra.add(derecha, BorderLayout.EAST);
        return barra;
    }

    private static Color c(String hex) {
        return EstiloUI.color(hex);
    }
}
