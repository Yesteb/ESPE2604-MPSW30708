package utils;

import model.Usuario;
import persistencia.UsuarioJpaController;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class CrearUsuarioPrueba {

    public static void main(String[] args) throws Exception {
        String email    = "prueba@test.com";
        String username = "admin";
        String password = "contra";

        UsuarioJpaController controller = new UsuarioJpaController();

        Usuario u = controller.findUsuario(email);
        if (u == null) {
            u = new Usuario();
            u.setEmail(email);
            u.setUsuario(username);
            u.setPassword(sha256(password));
            controller.create(u);
            System.out.println("Usuario creado.");
        } else {
            u.setUsuario(username);
            u.setPassword(sha256(password));
            controller.edit(u);
            System.out.println("Usuario actualizado.");
        }

        System.out.println("  email:    " + email);
        System.out.println("  usuario:  " + username);
        System.out.println("  password: " + password);

        persistencia.JPAUtil.close();
    }

    private static String sha256(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
