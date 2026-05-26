package persistencia;

import jakarta.persistence.EntityManager;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import model.Cliente;
import model.Pedido;
import persistencia.exceptions.NonexistentEntityException;
import persistencia.exceptions.PreexistingEntityException;

public class PedidoJpaController implements Serializable, RepositorioPedido {
    private static final long serialVersionUID = 1L;

    public void crear(Pedido pedido) throws PreexistingEntityException {
        EntityManager em = JPAUtil.obtenerGestorEntidades();
        try {
            if (em.find(Pedido.class, pedido.getId()) != null)
                throw new PreexistingEntityException("Pedido ya existe: " + pedido.getId());
            em.getTransaction().begin();
            em.persist(pedido);
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


    public void editar(Pedido pedido) {
        EntityManager em = JPAUtil.obtenerGestorEntidades();
        try {
            em.getTransaction().begin();
            em.merge(pedido);
            em.getTransaction().commit();
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
            Pedido pedido = em.find(Pedido.class, id);
            if (pedido == null) throw new NonexistentEntityException("Pedido no encontrado: " + id);
            em.getTransaction().begin();
            em.remove(pedido);
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


    public List<Pedido> buscarTodos() {
        EntityManager em = JPAUtil.obtenerGestorEntidades();
        try {
            return em.createQuery(
                "SELECT DISTINCT p FROM Pedido p LEFT JOIN FETCH p.productos ORDER BY p.fechaRegistro DESC",
                Pedido.class).getResultList();
        } finally {
            em.close();
        }
    }


    public List<Pedido> buscarPorEstado(String estado) {
        EntityManager em = JPAUtil.obtenerGestorEntidades();
        try {
            return em.createQuery(
                "SELECT DISTINCT p FROM Pedido p LEFT JOIN FETCH p.productos WHERE p.estado = :e ORDER BY p.fechaRegistro DESC",
                Pedido.class).setParameter("e", estado).getResultList();
        } finally {
            em.close();
        }
    }


    public Pedido buscarPendientePorCliente(Cliente cliente) {
        EntityManager em = JPAUtil.obtenerGestorEntidades();
        try {
            List<Pedido> resultados = em.createQuery(
                "SELECT DISTINCT p FROM Pedido p LEFT JOIN FETCH p.productos WHERE p.cliente = :c AND p.estado = 'PENDIENTE' ORDER BY p.fechaRegistro DESC",
                Pedido.class).setParameter("c", cliente).setMaxResults(1).getResultList();
            return resultados.isEmpty() ? null : resultados.get(0);
        } finally {
            em.close();
        }
    }


    public int contarPorEstado(String estado) {
        EntityManager em = JPAUtil.obtenerGestorEntidades();
        try {
            return em.createQuery("SELECT COUNT(p) FROM Pedido p WHERE p.estado = :e", Long.class)
                .setParameter("e", estado).getSingleResult().intValue();
        } finally {
            em.close();
        }
    }


    public List<Pedido> buscarCobradosHoy() {
        EntityManager em = JPAUtil.obtenerGestorEntidades();
        try {
            LocalDateTime inicio = LocalDate.now().atStartOfDay();
            LocalDateTime fin = inicio.plusDays(1);
            return em.createQuery(
                "SELECT p FROM Pedido p WHERE p.estado = 'COBRADO' AND p.fechaCobro >= :s AND p.fechaCobro < :e",
                Pedido.class).setParameter("s", inicio).setParameter("e", fin).getResultList();
        } finally {
            em.close();
        }
    }


    public Pedido buscarPorId(String id) {
        EntityManager em = JPAUtil.obtenerGestorEntidades();
        try {
            return em.createQuery(
                "SELECT DISTINCT p FROM Pedido p LEFT JOIN FETCH p.productos WHERE p.id = :id",
                Pedido.class).setParameter("id", id).setMaxResults(1).getResultList()
                .stream().findFirst().orElse(null);
        } finally {
            em.close();
        }
    }
}
