package persistencia;

import java.util.List;
import model.Cliente;
import model.Pedido;
import persistencia.exceptions.NonexistentEntityException;
import persistencia.exceptions.PreexistingEntityException;

public interface RepositorioPedido {
    void crear(Pedido pedido) throws PreexistingEntityException;
    void editar(Pedido pedido);
    void eliminar(String id) throws NonexistentEntityException;
    List<Pedido> buscarTodos();
    List<Pedido> buscarPorEstado(String estado);
    Pedido buscarPendientePorCliente(Cliente cliente);
    int contarPorEstado(String estado);
    List<Pedido> buscarCobradosHoy();
    Pedido buscarPorId(String id);

    /**
     * Una página de pedidos, ordenados del más reciente al más antiguo.
     *
     * @param estado   estado a filtrar, o {@code null} para todos
     * @param desde    índice del primer elemento (0 = primera página)
     * @param cantidad tamaño de la página
     */
    List<Pedido> buscarPagina(String estado, int desde, int cantidad);

    /** Total de pedidos que cumplen el filtro; {@code null} cuenta todos. */
    int contarConFiltro(String estado);
}
