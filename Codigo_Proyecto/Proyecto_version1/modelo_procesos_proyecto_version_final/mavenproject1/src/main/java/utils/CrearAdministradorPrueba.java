package utils;

import model.Administrador;
import persistencia.AdministradorJpaController;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class CrearAdministradorPrueba {

    public static void main(String[] args) throws Exception {
        String correo  = "admin@correo.es";
        String nombre  = "admin";
        String contrasena = "contraxd";

        AdministradorJpaController controlador = new AdministradorJpaController();

        Administrador administrador = controlador.buscarPorEmail(correo);
        if (administrador == null) {
            administrador = new Administrador();
            administrador.setEmail(correo);
            administrador.setNombre(nombre);
            administrador.setContrasena(sha256(contrasena));
            controlador.crear(administrador);
            System.out.println("Administrador creado.");
        } else {
            administrador.setNombre(nombre);
            administrador.setContrasena(sha256(contrasena));
            controlador.editar(administrador);
            System.out.println("Administrador actualizado.");
        }

        System.out.println("  email:    " + correo);
        System.out.println("  nombre:   " + nombre);
        System.out.println("  password: " + contrasena);

        persistencia.JPAUtil.cerrar();
    }


    private static String sha256(String entrada) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(entrada.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
