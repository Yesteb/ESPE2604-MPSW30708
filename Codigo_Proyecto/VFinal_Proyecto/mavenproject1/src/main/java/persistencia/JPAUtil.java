package persistencia;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import utils.ConfiguracionBD;

public class JPAUtil {

    private static final String UNIDAD_PERSISTENCIA = "pinkyPuffPersistance";
    private static EntityManagerFactory fabricaGestores;

    private JPAUtil() {}

    public static synchronized EntityManagerFactory obtenerFabricaGestores() {
        if (fabricaGestores == null || !fabricaGestores.isOpen()) {
            // Los datos de conexión llegan desde el archivo externo, no desde
            // persistence.xml: así se puede apuntar a otro servidor sin recompilar
            // y la contraseña no viaja dentro del JAR distribuido.
            fabricaGestores = Persistence.createEntityManagerFactory(
                UNIDAD_PERSISTENCIA, ConfiguracionBD.cargar().propiedadesJpa());
        }
        return fabricaGestores;
    }

    public static EntityManager obtenerGestorEntidades() {
        return obtenerFabricaGestores().createEntityManager();
    }

    /**
     * Descarta la fábrica actual para que la siguiente operación vuelva a leer
     * la configuración. Necesario tras cambiar los datos de conexión.
     */
    public static synchronized void reiniciar() {
        cerrar();
        fabricaGestores = null;
    }

    public static void cerrar() {
        if (fabricaGestores != null && fabricaGestores.isOpen()) {
            fabricaGestores.close();
        }
    }
}
