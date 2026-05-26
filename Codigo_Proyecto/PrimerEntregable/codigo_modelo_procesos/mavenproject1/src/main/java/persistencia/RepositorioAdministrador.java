package persistencia;

import java.util.List;
import model.Administrador;
import persistencia.exceptions.NonexistentEntityException;
import persistencia.exceptions.PreexistingEntityException;

public interface RepositorioAdministrador {
    void crear(Administrador administrador) throws PreexistingEntityException, Exception;
    void editar(Administrador administrador) throws NonexistentEntityException, Exception;
    void eliminar(String email) throws NonexistentEntityException;
    List<Administrador> buscarTodos();
    List<Administrador> buscarTodos(int maxResultados, int primerResultado);
    Administrador buscarPorEmail(String email);
    Administrador buscarPorNombre(String nombre);
    int contarTotal();
}
