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


    /**
     * Paginación en dos fases, y no con un simple {@code setMaxResults} sobre la
     * consulta con {@code JOIN FETCH}.
     *
     * <p>Motivo: al mezclar {@code JOIN FETCH} de una colección con
     * {@code setFirstResult/setMaxResults}, Hibernate no puede traducirlo a
     * LIMIT/OFFSET (el join multiplica las filas) y avisa
     * <em>"firstResult/maxResults specified with collection fetch; applying in
     * memory"</em>: descarga la tabla entera y pagina en Java, que es justo lo
     * que se quiere evitar.</p>
     *
     * <p>Así que primero se pide solo la página de identificadores —eso sí baja
     * a un LIMIT/OFFSET real— y después se recuperan esos pedidos con sus
     * productos en una segunda consulta.</p>
     */
    public List<Pedido> buscarPagina(String estado, int desde, int cantidad) {
        EntityManager em = JPAUtil.obtenerGestorEntidades();
        try {
            // Fase 1: solo los IDs de la página. El desempate por id evita que un
            // pedido salte de página cuando varios comparten fecha_registro.
            String filtro = (estado == null) ? "" : "WHERE p.estado = :estado ";
            var consultaIds = em.createQuery(
                    "SELECT p.id FROM Pedido p " + filtro
                  + "ORDER BY p.fechaRegistro DESC, p.id", String.class)
                .setFirstResult(Math.max(0, desde))
                .setMaxResults(Math.max(1, cantidad));
            if (estado != null) consultaIds.setParameter("estado", estado);

            List<String> ids = consultaIds.getResultList();
            if (ids.isEmpty()) return List.of();

            // Fase 2: esos pedidos con sus productos, sin límites que estorben
            return em.createQuery(
                    "SELECT DISTINCT p FROM Pedido p LEFT JOIN FETCH p.productos "
                  + "WHERE p.id IN :ids ORDER BY p.fechaRegistro DESC, p.id", Pedido.class)
                .setParameter("ids", ids)
                .getResultList();
        } finally {
            em.close();
        }
    }


    public int contarConFiltro(String estado) {
        EntityManager em = JPAUtil.obtenerGestorEntidades();
        try {
            if (estado == null) {
                return em.createQuery("SELECT COUNT(p) FROM Pedido p", Long.class)
                    .getSingleResult().intValue();
            }
            return em.createQuery("SELECT COUNT(p) FROM Pedido p WHERE p.estado = :e", Long.class)
                .setParameter("e", estado).getSingleResult().intValue();
        } finally {
            em.close();
        }
    }
}
