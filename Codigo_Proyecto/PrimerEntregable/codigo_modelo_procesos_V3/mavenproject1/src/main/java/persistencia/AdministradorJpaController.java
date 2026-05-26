package persistencia;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.io.Serializable;
import java.util.List;
import model.Administrador;
import persistencia.exceptions.NonexistentEntityException;
import persistencia.exceptions.PreexistingEntityException;

public class AdministradorJpaController implements Serializable, RepositorioAdministrador {

    private static final long serialVersionUID = 1L;

    public void crear(Administrador administrador) throws PreexistingEntityException, Exception {
        EntityManager em = JPAUtil.obtenerGestorEntidades();
        try {
            if (em.find(Administrador.class, administrador.getEmail()) != null) {
                throw new PreexistingEntityException("El administrador con email " + administrador.getEmail() + " ya existe.");
            }
            em.getTransaction().begin();
            em.persist(administrador);
            em.getTransaction().commit();
        } catch (PreexistingEntityException e) {
            throw e;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }


    public void editar(Administrador administrador) throws NonexistentEntityException, Exception {
        EntityManager em = JPAUtil.obtenerGestorEntidades();
        try {
            if (em.find(Administrador.class, administrador.getEmail()) == null) {
                throw new NonexistentEntityException("El administrador con email " + administrador.getEmail() + " no existe.");
            }
            em.getTransaction().begin();
            em.merge(administrador);
            em.getTransaction().commit();
        } catch (NonexistentEntityException e) {
            throw e;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }


    public void eliminar(String email) throws NonexistentEntityException {
        EntityManager em = JPAUtil.obtenerGestorEntidades();
        try {
            Administrador administrador = em.find(Administrador.class, email);
            if (administrador == null) {
                throw new NonexistentEntityException("El administrador con email " + email + " no existe.");
            }
            em.getTransaction().begin();
            em.remove(administrador);
            em.getTransaction().commit();
        } catch (NonexistentEntityException e) {
            throw e;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }


    public List<Administrador> buscarTodos() {
        return buscarAdministradores(true, -1, -1);
    }


    public List<Administrador> buscarTodos(int maxResultados, int primerResultado) {
        return buscarAdministradores(false, maxResultados, primerResultado);
    }


    private List<Administrador> buscarAdministradores(boolean todos, int maxResultados, int primerResultado) {
        EntityManager em = JPAUtil.obtenerGestorEntidades();
        try {
            TypedQuery<Administrador> consulta = em.createQuery("SELECT u FROM Administrador u", Administrador.class);
            if (!todos) {
                consulta.setMaxResults(maxResultados);
                consulta.setFirstResult(primerResultado);
            }
            return consulta.getResultList();
        } finally {
            em.close();
        }
    }


    public Administrador buscarPorEmail(String email) {
        EntityManager em = JPAUtil.obtenerGestorEntidades();
        try {
            return em.find(Administrador.class, email);
        } finally {
            em.close();
        }
    }


    public Administrador buscarPorNombre(String nombre) {
        EntityManager em = JPAUtil.obtenerGestorEntidades();
        try {
            TypedQuery<Administrador> consulta = em.createQuery(
                "SELECT u FROM Administrador u WHERE u.nombre = :nombre", Administrador.class);
            consulta.setParameter("nombre", nombre);
            List<Administrador> resultado = consulta.getResultList();
            return resultado.isEmpty() ? null : resultado.get(0);
        } finally {
            em.close();
        }
    }


    public int contarTotal() {
        EntityManager em = JPAUtil.obtenerGestorEntidades();
        try {
            TypedQuery<Long> consulta = em.createQuery("SELECT COUNT(u) FROM Administrador u", Long.class);
            return consulta.getSingleResult().intValue();
        } finally {
            em.close();
        }
    }
}
