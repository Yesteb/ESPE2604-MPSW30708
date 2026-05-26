package persistencia;

import jakarta.persistence.EntityManager;
import java.io.Serializable;
import java.util.List;
import model.Cliente;
import persistencia.exceptions.NonexistentEntityException;
import persistencia.exceptions.PreexistingEntityException;

public class ClienteJpaController implements Serializable, RepositorioCliente {
    private static final long serialVersionUID = 1L;

    public void crear(Cliente cliente) throws PreexistingEntityException {
        EntityManager em = JPAUtil.obtenerGestorEntidades();
        try {
            if (em.find(Cliente.class, cliente.getId()) != null)
                throw new PreexistingEntityException("Cliente ya existe: " + cliente.getId());
            em.getTransaction().begin();
            em.persist(cliente);
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


    public void editar(Cliente cliente) throws NonexistentEntityException {
        EntityManager em = JPAUtil.obtenerGestorEntidades();
        try {
            if (em.find(Cliente.class, cliente.getId()) == null)
                throw new NonexistentEntityException("Cliente no encontrado: " + cliente.getId());
            em.getTransaction().begin();
            em.merge(cliente);
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


    public void eliminar(String id) throws NonexistentEntityException {
        EntityManager em = JPAUtil.obtenerGestorEntidades();
        try {
            Cliente clienteEncontrado = em.find(Cliente.class, id);
            if (clienteEncontrado == null) throw new NonexistentEntityException("Cliente no encontrado: " + id);
            em.getTransaction().begin();
            em.remove(clienteEncontrado);
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


    public List<Cliente> buscarTodos() {
        EntityManager em = JPAUtil.obtenerGestorEntidades();
        try {
            return em.createQuery("SELECT c FROM Cliente c ORDER BY c.nombre", Cliente.class).getResultList();
        } finally {
            em.close();
        }
    }


    public List<Cliente> buscarPorEstado(String estado) {
        EntityManager em = JPAUtil.obtenerGestorEntidades();
        try {
            return em.createQuery(
                "SELECT c FROM Cliente c WHERE c.estado = :estado ORDER BY c.nombre", Cliente.class)
                .setParameter("estado", estado)
                .getResultList();
        } finally {
            em.close();
        }
    }


    public Cliente buscarPorId(String id) {
        EntityManager em = JPAUtil.obtenerGestorEntidades();
        try {
            return em.find(Cliente.class, id);
        } finally {
            em.close();
        }
    }


    public Cliente buscarPorNombre(String nombre) {
        EntityManager em = JPAUtil.obtenerGestorEntidades();
        try {
            List<Cliente> resultados = em.createQuery(
                "SELECT c FROM Cliente c WHERE LOWER(c.nombre) = LOWER(:n)", Cliente.class)
                .setParameter("n", nombre).getResultList();
            return resultados.isEmpty() ? null : resultados.get(0);
        } finally {
            em.close();
        }
    }


    public int contarTotal() {
        EntityManager em = JPAUtil.obtenerGestorEntidades();
        try {
            return em.createQuery("SELECT COUNT(c) FROM Cliente c", Long.class)
                .getSingleResult().intValue();
        } finally {
            em.close();
        }
    }
}
