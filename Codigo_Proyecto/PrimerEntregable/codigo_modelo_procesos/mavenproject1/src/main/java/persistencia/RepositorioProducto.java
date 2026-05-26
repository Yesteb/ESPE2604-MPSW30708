package persistencia;

import model.Producto;
import persistencia.exceptions.NonexistentEntityException;
import persistencia.exceptions.PreexistingEntityException;

public interface RepositorioProducto {
    void crear(Producto producto) throws PreexistingEntityException;
    void eliminar(String id) throws NonexistentEntityException;
}
