package service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import model.Administrador;
import model.Cliente;
import model.Pedido;
import model.Producto;
import persistencia.RepositorioCliente;
import persistencia.RepositorioPedido;
import persistencia.RepositorioProducto;
import persistencia.exceptions.NonexistentEntityException;

public class PedidoService implements SujetoDatos {

    /** Sugerencias que devuelve como máximo el buscador de clientes. */
    private static final int MAX_SUGERENCIAS_CLIENTE = 20;

    private final RepositorioCliente  repoCliente;
    private final RepositorioPedido   repoPedido;
    private final RepositorioProducto repoProducto;
    private final List<ObservadorDatos> observadores = new ArrayList<>();

    public PedidoService(RepositorioCliente repoCliente, RepositorioPedido repoPedido, RepositorioProducto repoProducto) {
        this.repoCliente  = repoCliente;
        this.repoPedido   = repoPedido;
        this.repoProducto = repoProducto;
    }

    @Override
    public void agregarObservador(ObservadorDatos observador) {
        observadores.add(observador);
    }

    @Override
    public void eliminarObservador(ObservadorDatos observador) {
        observadores.remove(observador);
    }

    @Override
    public void notificarObservadores() {
        for (ObservadorDatos obs : observadores) obs.actualizar();
    }

    public Pedido agregarProducto(String nombreCliente, String tipo, String estilo, String talla,
                                   String descripcion, BigDecimal precio, int cantidad,
                                   Map<String, String> atributos, Administrador administrador) throws Exception {
        Cliente cliente = repoCliente.buscarPorNombre(nombreCliente);
        if (cliente == null) {
            cliente = new Cliente(UUID.randomUUID().toString(), nombreCliente, null, null, LocalDateTime.now());
            repoCliente.crear(cliente);
        }

        Pedido pedido = repoPedido.buscarPendientePorCliente(cliente);
        if (pedido == null) {
            pedido = new Pedido(UUID.randomUUID().toString(), cliente, "PENDIENTE", LocalDateTime.now());
            repoPedido.crear(pedido);
        }

        Producto producto = new Producto(UUID.randomUUID().toString(), tipo, estilo, talla,
            descripcion, precio, cantidad, LocalDateTime.now(), administrador);
        producto.setPedido(pedido);
        if (atributos != null) producto.getAtributos().putAll(atributos);
        repoProducto.crear(producto);

        Pedido resultado = repoPedido.buscarPorId(pedido.getId());
        notificarObservadores();
        return resultado;
    }

    public void marcarCobrado(String pedidoId) throws NonexistentEntityException {
        Pedido pedido = repoPedido.buscarPorId(pedidoId);
        if (pedido == null) throw new NonexistentEntityException("Pedido no encontrado.");
        pedido.setEstado("COBRADO");
        pedido.setFechaCobro(LocalDateTime.now());
        repoPedido.editar(pedido);
        notificarObservadores();
    }

    public void marcarCancelado(String pedidoId) throws NonexistentEntityException {
        Pedido pedido = repoPedido.buscarPorId(pedidoId);
        if (pedido == null) throw new NonexistentEntityException("Pedido no encontrado.");
        pedido.setEstado("CANCELADO");
        repoPedido.editar(pedido);
        notificarObservadores();
    }

    public void reactivarPedido(String pedidoId) throws NonexistentEntityException {
        Pedido pedido = repoPedido.buscarPorId(pedidoId);
        if (pedido == null) throw new NonexistentEntityException("Pedido no encontrado.");
        pedido.setEstado("PENDIENTE");
        pedido.setFechaCobro(null);
        repoPedido.editar(pedido);
        notificarObservadores();
    }

    public void eliminarProducto(String productoId) throws NonexistentEntityException {
        repoProducto.eliminar(productoId);
        notificarObservadores();
    }

    public void eliminarPedido(String pedidoId) throws NonexistentEntityException {
        repoPedido.eliminar(pedidoId);
        notificarObservadores();
    }

    public void actualizarCliente(Cliente cliente) throws NonexistentEntityException {
        repoCliente.editar(cliente);
        notificarObservadores();
    }

    public void eliminarCliente(String clienteId) throws NonexistentEntityException {
        Cliente cliente = repoCliente.buscarPorId(clienteId);
        if (cliente == null) throw new NonexistentEntityException("Cliente no encontrado.");
        repoPedido.buscarTodos().stream()
            .filter(p -> p.getCliente().getId().equals(clienteId))
            .forEach(p -> {
                try { repoPedido.eliminar(p.getId()); }
                catch (NonexistentEntityException ex) { throw new RuntimeException(ex); }
            });
        repoCliente.eliminar(clienteId);
        notificarObservadores();
    }

    public List<Pedido> obtenerTodos() {
        return repoPedido.buscarTodos();
    }

    public List<Pedido> obtenerTodosConFiltro(String filtro) {
        if ("PENDIENTE".equals(filtro) || "COBRADO".equals(filtro) || "CANCELADO".equals(filtro))
            return repoPedido.buscarPorEstado(filtro);
        return repoPedido.buscarTodos();
    }

    /**
     * Traduce el filtro de la interfaz al estado que entiende el repositorio:
     * "TODOS" (o cualquier valor no reconocido) equivale a no filtrar.
     */
    private static String estadoPedido(String filtro) {
        return ("PENDIENTE".equals(filtro) || "COBRADO".equals(filtro) || "CANCELADO".equals(filtro))
            ? filtro : null;
    }

    /** Una página de pedidos, traída de la base de datos ya recortada. */
    public List<Pedido> obtenerPaginaPedidos(String filtro, int desde, int cantidad) {
        return repoPedido.buscarPagina(estadoPedido(filtro), desde, cantidad);
    }

    /** Total de pedidos que cumplen el filtro, para calcular las páginas. */
    public int contarPedidos(String filtro) {
        return repoPedido.contarConFiltro(estadoPedido(filtro));
    }

    public List<Cliente> obtenerClientes() {
        return repoCliente.buscarTodos();
    }

    /** Búsqueda incremental de clientes, resuelta por índice en la base de datos. */
    public List<Cliente> buscarClientes(String texto) {
        return repoCliente.buscarPorTexto(texto, MAX_SUGERENCIAS_CLIENTE);
    }

    public List<Cliente> obtenerClientesConFiltro(String filtro) {
        if ("ACTIVO".equals(filtro) || "INACTIVO".equals(filtro) || "BLOQUEADO".equals(filtro))
            return repoCliente.buscarPorEstado(filtro);
        return repoCliente.buscarTodos();
    }

    /** "TODOS" (o cualquier valor no reconocido) equivale a no filtrar. */
    private static String estadoCliente(String filtro) {
        return ("ACTIVO".equals(filtro) || "INACTIVO".equals(filtro) || "BLOQUEADO".equals(filtro))
            ? filtro : null;
    }

    /** Una página de clientes, traída de la base de datos ya recortada. */
    public List<Cliente> obtenerPaginaClientes(String filtro, int desde, int cantidad) {
        return repoCliente.buscarPagina(estadoCliente(filtro), desde, cantidad);
    }

    /** Total de clientes que cumplen el filtro, para calcular las páginas. */
    public int contarClientes(String filtro) {
        return repoCliente.contarConFiltro(estadoCliente(filtro));
    }

    public void cambiarEstadoCliente(String clienteId, String nuevoEstado) throws NonexistentEntityException {
        Cliente cliente = repoCliente.buscarPorId(clienteId);
        if (cliente == null) throw new NonexistentEntityException("Cliente no encontrado.");
        cliente.setEstado(nuevoEstado);
        repoCliente.editar(cliente);
        notificarObservadores();
    }

    public int contarPendientes() {
        return repoPedido.contarPorEstado("PENDIENTE");
    }

    public int contarCobradosHoy() {
        return repoPedido.buscarCobradosHoy().size();
    }

    public BigDecimal totalPorCobrar() {
        return repoPedido.buscarPorEstado("PENDIENTE").stream()
            .map(Pedido::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<Pedido> obtenerCobradosHoy() {
        return repoPedido.buscarCobradosHoy();
    }
}
