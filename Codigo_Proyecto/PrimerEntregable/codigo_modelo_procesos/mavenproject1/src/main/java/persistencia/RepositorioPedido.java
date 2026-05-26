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
}
