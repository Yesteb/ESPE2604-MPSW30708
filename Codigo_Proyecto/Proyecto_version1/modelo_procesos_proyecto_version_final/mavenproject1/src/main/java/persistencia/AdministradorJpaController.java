package persistencia;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.io.Serializable;
import java.util.List;
import model.Administrador;
import model.ResultadoLogin;
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


    /**
     * Delega la autenticación en {@code fn_autenticar_admin}: un solo viaje a la
     * base de datos que comprueba la contraseña, deja constancia del intento en
     * {@code sesiones_admin} y aplica el bloqueo por intentos fallidos.
     *
     * <p>La llamada es una escritura (inserta en la bitácora), de ahí la
     * transacción explícita.</p>
     */
    public ResultadoLogin autenticar(String usuario, String hash, String origen) {
        EntityManager em = JPAUtil.obtenerGestorEntidades();
        try {
            em.getTransaction().begin();
            Object[] fila = (Object[]) em.createNativeQuery(
                    "SELECT r_email, r_username, r_autenticado, r_intentos, r_bloqueado, r_mensaje "
                  + "FROM fn_autenticar_admin(CAST(?1 AS VARCHAR), CAST(?2 AS VARCHAR), CAST(?3 AS VARCHAR))")
                .setParameter(1, usuario)
                .setParameter(2, hash)
                .setParameter(3, origen)
                .getSingleResult();
            em.getTransaction().commit();

            return new ResultadoLogin(
                (String)  fila[0],
                (String)  fila[1],
                (Boolean) fila[2],
                ((Number) fila[3]).intValue(),
                (Boolean) fila[4],
                (String)  fila[5]);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Error al autenticar: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }


    public void cerrarSesion(String email) {
        if (email == null) return;
        EntityManager em = JPAUtil.obtenerGestorEntidades();
        try {
            em.getTransaction().begin();
            em.createNativeQuery("CALL sp_cerrar_sesion(CAST(?1 AS VARCHAR))")
                .setParameter(1, email)
                .executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            // Cerrar sesión es informativo: no debe impedir que el usuario salga
            System.err.println("No se pudo registrar el cierre de sesión: " + e.getMessage());
        } finally {
            em.close();
        }
    }
}
