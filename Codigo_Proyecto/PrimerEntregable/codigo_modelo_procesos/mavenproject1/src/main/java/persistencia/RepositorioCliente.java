package persistencia;

import java.util.List;
import model.Cliente;
import persistencia.exceptions.NonexistentEntityException;
import persistencia.exceptions.PreexistingEntityException;

public interface RepositorioCliente {
    void crear(Cliente cliente) throws PreexistingEntityException;
    void editar(Cliente cliente) throws NonexistentEntityException;
    void eliminar(String id) throws NonexistentEntityException;
    List<Cliente> buscarTodos();
    List<Cliente> buscarPorEstado(String estado);
    Cliente buscarPorId(String id);
    Cliente buscarPorNombre(String nombre);
    int contarTotal();
}
