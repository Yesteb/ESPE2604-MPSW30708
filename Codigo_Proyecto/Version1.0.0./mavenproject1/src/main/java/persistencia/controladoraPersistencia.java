package persistencia;

import java.util.List;
import model.Usuario;
import persistencia.exceptions.NonexistentEntityException;
import persistencia.exceptions.PreexistingEntityException;

public class controladoraPersistencia {

    private final UsuarioJpaController usuarioController = new UsuarioJpaController();

    public void crearUsuario(Usuario usuario) throws PreexistingEntityException, Exception {
        usuarioController.create(usuario);
    }

    public void actualizarUsuario(Usuario usuario) throws NonexistentEntityException, Exception {
        usuarioController.edit(usuario);
    }

    public void eliminarUsuario(String email) throws NonexistentEntityException {
        usuarioController.destroy(email);
    }

    public Usuario buscarUsuario(String email) {
        return usuarioController.findUsuario(email);
    }

    public Usuario buscarPorUsuario(String username) {
        return usuarioController.findByUsuario(username);
    }

    public List<Usuario> listarUsuarios() {
        return usuarioController.findUsuarioEntities();
    }

    public int contarUsuarios() {
        return usuarioController.getUsuarioCount();
    }
}
