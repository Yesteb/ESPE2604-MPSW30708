package utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Datos de conexión a PostgreSQL, guardados fuera del JAR.
 *
 * <p>Antes vivían dentro de {@code persistence.xml}, lo que obligaba a
 * recompilar para apuntar a otro servidor y hacía que la contraseña viajara
 * dentro del artefacto distribuido. Ahora se leen de un archivo de texto en el
 * perfil del usuario, que puede editarse o rellenarse desde el diálogo de
 * configuración la primera vez que se abre la aplicación.</p>
 *
 * <p>Ubicación del archivo, en orden de preferencia:</p>
 * <ol>
 *   <li>La ruta indicada en la propiedad de sistema {@code pinkypuff.config}.</li>
 *   <li>{@code ~/.pinkypuff/conexion.properties}.</li>
 * </ol>
 */
public final class ConfiguracionBD {

    private static final String ARCHIVO   = "conexion.properties";
    private static final String CARPETA   = ".pinkypuff";
    private static final String PROP_RUTA = "pinkypuff.config";

    private static final String HOST_POR_DEFECTO   = "localhost";
    private static final String PUERTO_POR_DEFECTO = "5433";
    private static final String BASE_POR_DEFECTO   = "postgres";

    private String host;
    private String puerto;
    private String base;
    private String usuario;
    private String password;

    public ConfiguracionBD(String host, String puerto, String base, String usuario, String password) {
        this.host     = host;
        this.puerto   = puerto;
        this.base     = base;
        this.usuario  = usuario;
        this.password = password;
    }

    public String getHost()     { return host; }
    public String getPuerto()   { return puerto; }
    public String getBase()     { return base; }
    public String getUsuario()  { return usuario; }
    public String getPassword() { return password; }

    /** URL JDBC construida a partir de servidor, puerto y base. */
    public String getUrl() {
        return "jdbc:postgresql://" + host + ":" + puerto + "/" + base;
    }

    /** Ruta del archivo de configuración, exista o no todavía. */
    public static Path rutaArchivo() {
        String personalizada = System.getProperty(PROP_RUTA);
        if (personalizada != null && !personalizada.isBlank()) {
            return Paths.get(personalizada);
        }
        return Paths.get(System.getProperty("user.home"), CARPETA, ARCHIVO);
    }

    public static boolean existeArchivo() {
        return Files.isReadable(rutaArchivo());
    }

    /**
     * Carga la configuración guardada. Si el archivo no existe o está
     * incompleto, los valores que falten toman los de por defecto.
     */
    public static ConfiguracionBD cargar() {
        Properties p = new Properties();
        Path ruta = rutaArchivo();
        if (Files.isReadable(ruta)) {
            try (InputStream in = Files.newInputStream(ruta)) {
                p.load(in);
            } catch (IOException e) {
                System.err.println("No se pudo leer " + ruta + ": " + e.getMessage());
            }
        }
        return new ConfiguracionBD(
            p.getProperty("db.host",    HOST_POR_DEFECTO),
            p.getProperty("db.puerto",  PUERTO_POR_DEFECTO),
            p.getProperty("db.base",    BASE_POR_DEFECTO),
            p.getProperty("db.usuario", ""),
            p.getProperty("db.password", ""));
    }

    /**
     * Escribe la configuración, creando la carpeta si hace falta. En sistemas
     * tipo Unix el archivo queda accesible solo para el usuario, porque contiene
     * la contraseña en claro.
     */
    public void guardar() throws IOException {
        Path ruta = rutaArchivo();
        Files.createDirectories(ruta.getParent());

        Properties p = new Properties();
        p.setProperty("db.host",     host);
        p.setProperty("db.puerto",   puerto);
        p.setProperty("db.base",     base);
        p.setProperty("db.usuario",  usuario);
        p.setProperty("db.password", password);

        try (OutputStream out = Files.newOutputStream(ruta)) {
            p.store(out, "Conexion a PostgreSQL de PinkyPuff. Contiene la contrasena en claro.");
        }
        restringirPermisos(ruta);
    }

    private static void restringirPermisos(Path ruta) {
        try {
            Files.setPosixFilePermissions(ruta,
                java.util.Set.of(java.nio.file.attribute.PosixFilePermission.OWNER_READ,
                                 java.nio.file.attribute.PosixFilePermission.OWNER_WRITE));
        } catch (UnsupportedOperationException | IOException e) {
            // Windows no admite permisos POSIX: no es motivo para fallar
        }
    }

    /**
     * Abre una conexión directa para comprobar los datos, sin arrancar Hibernate.
     *
     * @return {@code null} si la conexión funciona, o el mensaje de error
     */
    public String probarConexion() {
        try {
            Class.forName("org.postgresql.Driver");
            try (Connection c = DriverManager.getConnection(getUrl(), usuario, password)) {
                return c.isValid(5) ? null : "La conexión no respondió a tiempo.";
            }
        } catch (ClassNotFoundException e) {
            return "Falta el driver de PostgreSQL en el classpath.";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    /** Propiedades con las que sobrescribir las del {@code persistence.xml}. */
    public Map<String, Object> propiedadesJpa() {
        Map<String, Object> props = new HashMap<>();
        props.put("jakarta.persistence.jdbc.url",      getUrl());
        props.put("jakarta.persistence.jdbc.user",     usuario);
        props.put("jakarta.persistence.jdbc.password", password);
        props.put("jakarta.persistence.jdbc.driver",   "org.postgresql.Driver");
        return props;
    }
}
