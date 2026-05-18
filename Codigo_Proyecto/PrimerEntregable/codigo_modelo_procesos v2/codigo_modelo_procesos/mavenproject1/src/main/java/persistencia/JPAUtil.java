package persistencia;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JPAUtil {

    private static final String UNIDAD_PERSISTENCIA = "pinkyPuffPersistance";
    private static EntityManagerFactory fabricaGestores;

    private JPAUtil() {}

    public static synchronized EntityManagerFactory obtenerFabricaGestores() {
        if (fabricaGestores == null || !fabricaGestores.isOpen()) {
            fabricaGestores = Persistence.createEntityManagerFactory(UNIDAD_PERSISTENCIA);
        }
        return fabricaGestores;
    }

    public static EntityManager obtenerGestorEntidades() {
        return obtenerFabricaGestores().createEntityManager();
    }

    public static void cerrar() {
        if (fabricaGestores != null && fabricaGestores.isOpen()) {
            fabricaGestores.close();
        }
    }
}
