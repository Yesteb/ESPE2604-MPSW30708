package controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import model.Usuario;
import persistencia.controladoraPersistencia;

public class SistemaAutenticacion {

    private final controladoraPersistencia persistencia;
    private final EmailService emailService;
    private CambiarContra tokenRecuperacion;

    public SistemaAutenticacion() {
        this.persistencia  = new controladoraPersistencia();
        this.emailService  = new EmailService();
    }

    public boolean login(String username, String password) {
        Usuario usuario = persistencia.buscarPorUsuario(username);
        if (usuario == null) return false;
        return usuario.getPassword().equals(hashearPassword(password));
    }

    public void enviarCodigoRecuperacion(String email) {
        String codigo = generarCodigo();
        tokenRecuperacion = new CambiarContra();
        tokenRecuperacion.setEmail(email);
        tokenRecuperacion.setCodigo(codigo);
        tokenRecuperacion.setTiempoExpiracion(LocalDateTime.now().plusMinutes(15));
        emailService.enviarCodigo(email, codigo);
    }

    public boolean verificarCodigo(String codigo) {
        if (tokenRecuperacion == null || tokenRecuperacion.estaExpirado()) return false;
        return tokenRecuperacion.getCodigo().equals(codigo);
    }

    public void restablecerPassword(String email, String nuevaPassword) {
        try {
            Usuario usuario = persistencia.buscarUsuario(email);
            if (usuario != null) {
                usuario.setPassword(hashearPassword(nuevaPassword));
                persistencia.actualizarUsuario(usuario);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String generarCodigo() {
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }

    private String hashearPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
