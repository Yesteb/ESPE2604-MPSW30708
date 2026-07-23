package persistencia;

import java.util.List;
import model.Administrador;
import model.ResultadoLogin;
import persistencia.exceptions.NonexistentEntityException;
import persistencia.exceptions.PreexistingEntityException;

public interface RepositorioAdministrador {
    void crear(Administrador administrador) throws PreexistingEntityException, Exception;
    void editar(Administrador administrador) throws NonexistentEntityException, Exception;
    void eliminar(String email) throws NonexistentEntityException;
    List<Administrador> buscarTodos();
    List<Administrador> buscarTodos(int maxResultados, int primerResultado);
    Administrador buscarPorEmail(String email);
    Administrador buscarPorNombre(String nombre);
    int contarTotal();

    /**
     * Autentica contra la base de datos en una sola llamada: comprueba la
     * contraseña, registra el intento y decide el bloqueo por intentos fallidos.
     *
     * @param usuario nombre de usuario o email
     * @param hash    contraseña ya cifrada con {@link utils.HashContrasena}
     * @param origen  equipo desde el que se intenta el acceso (puede ser null)
     */
    ResultadoLogin autenticar(String usuario, String hash, String origen);

    /** Marca como cerradas las sesiones abiertas del administrador. */
    void cerrarSesion(String email);
}
