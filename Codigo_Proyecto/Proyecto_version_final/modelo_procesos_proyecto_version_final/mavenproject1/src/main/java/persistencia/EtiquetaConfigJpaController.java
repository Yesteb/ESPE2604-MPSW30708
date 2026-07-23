package persistencia;

import jakarta.persistence.EntityManager;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import model.EtiquetaConfig;
import persistencia.exceptions.NonexistentEntityException;

public class EtiquetaConfigJpaController implements Serializable, RepositorioEtiqueta {
    private static final long serialVersionUID = 1L;

    public void crear(EtiquetaConfig etiqueta) {
        EntityManager em = JPAUtil.obtenerGestorEntidades();
        try {
            em.getTransaction().begin();
            em.persist(etiqueta);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException(ex);
        } finally {
            em.close();
        }
    }


    public void eliminar(Long id) throws NonexistentEntityException {
        EntityManager em = JPAUtil.obtenerGestorEntidades();
        try {
            EtiquetaConfig etiqueta = em.find(EtiquetaConfig.class, id);
            if (etiqueta == null) throw new NonexistentEntityException("Etiqueta no encontrada: " + id);
            em.getTransaction().begin();
            em.remove(etiqueta);
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


    public List<EtiquetaConfig> buscarPorCategoria(String categoria) {
        EntityManager em = JPAUtil.obtenerGestorEntidades();
        try {
            return em.createQuery(
                "SELECT e FROM EtiquetaConfig e WHERE e.categoria = :cat ORDER BY e.orden, e.id",
                EtiquetaConfig.class).setParameter("cat", categoria).getResultList();
        } finally {
            em.close();
        }
    }


    public void inicializarPorDefecto() {
        EntityManager em = JPAUtil.obtenerGestorEntidades();
        try {
            long total = em.createQuery("SELECT COUNT(e) FROM EtiquetaConfig e", Long.class).getSingleResult();
            if (total > 0) return;
        } finally {
            em.close();
        }

        String[] tipos   = {"Ropa", "Bolso", "Accesorio", "Zapatos"};
        Object[][] precios = {{"Liquidación", 1.0}, {"Base", 2.5}, {"Buen estado", 5.0}, {"Premium", 10.0}};

        for (int i = 0; i < tipos.length; i++)
            crear(new EtiquetaConfig("TIPO", tipos[i], null, i));
        for (int i = 0; i < precios.length; i++)
            crear(new EtiquetaConfig("PRECIO_RAPIDO", (String) precios[i][0],
                BigDecimal.valueOf((Double) precios[i][1]), i));
    }
}
