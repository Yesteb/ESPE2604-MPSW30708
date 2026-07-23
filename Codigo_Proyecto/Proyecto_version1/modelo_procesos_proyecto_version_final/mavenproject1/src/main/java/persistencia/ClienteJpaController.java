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


    /**
     * Una página de clientes resuelta con LIMIT/OFFSET real. Cliente no tiene
     * colecciones en carga ansiosa, así que aquí sí basta con
     * {@code setFirstResult/setMaxResults}.
     *
     * <p>El desempate por id hace la paginación determinista: sin él, dos
     * clientes homónimos podrían intercambiarse entre páginas.</p>
     */
    public List<Cliente> buscarPagina(String estado, int desde, int cantidad) {
        EntityManager em = JPAUtil.obtenerGestorEntidades();
        try {
            String filtro = (estado == null) ? "" : "WHERE c.estado = :estado ";
            var consulta = em.createQuery(
                    "SELECT c FROM Cliente c " + filtro + "ORDER BY c.nombre, c.id", Cliente.class)
                .setFirstResult(Math.max(0, desde))
                .setMaxResults(Math.max(1, cantidad));
            if (estado != null) consulta.setParameter("estado", estado);
            return consulta.getResultList();
        } finally {
            em.close();
        }
    }


    public int contarConFiltro(String estado) {
        EntityManager em = JPAUtil.obtenerGestorEntidades();
        try {
            if (estado == null) {
                return em.createQuery("SELECT COUNT(c) FROM Cliente c", Long.class)
                    .getSingleResult().intValue();
            }
            return em.createQuery("SELECT COUNT(c) FROM Cliente c WHERE c.estado = :e", Long.class)
                .setParameter("e", estado).getSingleResult().intValue();
        } finally {
            em.close();
        }
    }


    /**
     * Filtra en la base de datos en lugar de traer la lista completa y recorrerla
     * en memoria. {@code WITH ORDINALITY} numera las filas que devuelve la función
     * para conservar su orden de relevancia al unirlas con la tabla, de modo que
     * el resultado son entidades {@link Cliente} completas y ya ordenadas.
     */
    @SuppressWarnings("unchecked")
    public List<Cliente> buscarPorTexto(String texto, int limite) {
        if (texto == null || texto.isBlank()) return List.of();

        EntityManager em = JPAUtil.obtenerGestorEntidades();
        try {
            return em.createNativeQuery(
                    "SELECT c.* FROM clientes c "
                  + "JOIN fn_buscar_clientes(CAST(?1 AS TEXT), ?2) WITH ORDINALITY "
                  + "     AS f(id, nombre, telefono, estado, pedidos_pend, similitud, orden) "
                  + "  ON f.id = c.id "
                  + "ORDER BY f.orden", Cliente.class)
                .setParameter(1, texto.trim())
                .setParameter(2, limite)
                .getResultList();
        } finally {
            em.close();
        }
    }
}
