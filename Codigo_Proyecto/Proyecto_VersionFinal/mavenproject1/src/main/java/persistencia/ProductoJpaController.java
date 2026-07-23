package persistencia;

import jakarta.persistence.EntityManager;
import java.io.Serializable;
import model.Producto;
import persistencia.exceptions.NonexistentEntityException;
import persistencia.exceptions.PreexistingEntityException;

public class ProductoJpaController implements Serializable, RepositorioProducto {
    private static final long serialVersionUID = 1L;

    public void crear(Producto producto) throws PreexistingEntityException {
        EntityManager em = JPAUtil.obtenerGestorEntidades();
        try {
            if (em.find(Producto.class, producto.getId()) != null)
                throw new PreexistingEntityException("Producto ya existe: " + producto.getId());
            em.getTransaction().begin();
            if (producto.getAdministrador() != null)
                producto.setAdministrador(em.merge(producto.getAdministrador()));
            if (producto.getPedido() != null)
                producto.setPedido(em.merge(producto.getPedido()));
            em.persist(producto);
            em.getTransaction().commit();
        } catch (PreexistingEntityException e) {
            throw e;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }


    public void eliminar(String id) throws NonexistentEntityException {
        EntityManager em = JPAUtil.obtenerGestorEntidades();
        try {
            Producto producto = em.find(Producto.class, id);
            if (producto == null) throw new NonexistentEntityException("Producto no encontrado: " + id);
            em.getTransaction().begin();
            em.remove(producto);
            em.getTransaction().commit();
        } catch (NonexistentEntityException e) {
            throw e;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }
}
