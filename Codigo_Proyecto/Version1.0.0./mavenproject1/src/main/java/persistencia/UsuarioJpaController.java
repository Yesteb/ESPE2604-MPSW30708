package persistencia;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.io.Serializable;
import java.util.List;
import model.Usuario;
import persistencia.exceptions.NonexistentEntityException;
import persistencia.exceptions.PreexistingEntityException;

public class UsuarioJpaController implements Serializable {

    private static final long serialVersionUID = 1L;

    public void create(Usuario usuario) throws PreexistingEntityException, Exception {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            if (em.find(Usuario.class, usuario.getEmail()) != null) {
                throw new PreexistingEntityException("El usuario con email " + usuario.getEmail() + " ya existe.");
            }
            em.getTransaction().begin();
            em.persist(usuario);
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

    public void edit(Usuario usuario) throws NonexistentEntityException, Exception {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            if (em.find(Usuario.class, usuario.getEmail()) == null) {
                throw new NonexistentEntityException("El usuario con email " + usuario.getEmail() + " no existe.");
            }
            em.getTransaction().begin();
            em.merge(usuario);
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

    public void destroy(String email) throws NonexistentEntityException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            Usuario usuario = em.find(Usuario.class, email);
            if (usuario == null) {
                throw new NonexistentEntityException("El usuario con email " + email + " no existe.");
            }
            em.getTransaction().begin();
            em.remove(usuario);
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

    public List<Usuario> findUsuarioEntities() {
        return findUsuarioEntities(true, -1, -1);
    }

    public List<Usuario> findUsuarioEntities(int maxResults, int firstResult) {
        return findUsuarioEntities(false, maxResults, firstResult);
    }

    private List<Usuario> findUsuarioEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Usuario> q = em.createQuery("SELECT u FROM Usuario u", Usuario.class);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Usuario findUsuario(String email) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Usuario.class, email);
        } finally {
            em.close();
        }
    }

    public Usuario findByUsuario(String username) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Usuario> q = em.createQuery(
                "SELECT u FROM Usuario u WHERE u.usuario = :username", Usuario.class);
            q.setParameter("username", username);
            List<Usuario> result = q.getResultList();
            return result.isEmpty() ? null : result.get(0);
        } finally {
            em.close();
        }
    }

    public int getUsuarioCount() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Long> q = em.createQuery("SELECT COUNT(u) FROM Usuario u", Long.class);
            return q.getSingleResult().intValue();
        } finally {
            em.close();
        }
    }
}
