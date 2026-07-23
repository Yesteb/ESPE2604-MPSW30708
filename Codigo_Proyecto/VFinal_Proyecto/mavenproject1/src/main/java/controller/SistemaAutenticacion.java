package controller;

import java.net.InetAddress;
import java.net.UnknownHostException;
import model.Administrador;
import model.ResultadoLogin;
import persistencia.RepositorioAdministrador;
import utils.HashContrasena;

public class SistemaAutenticacion {

    private final RepositorioAdministrador repositorio;

    public SistemaAutenticacion(RepositorioAdministrador repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * Autentica al administrador contra la base de datos. El conteo de intentos
     * fallidos y el bloqueo los resuelve {@code fn_autenticar_admin}, de modo que
     * persisten aunque se cierre la aplicación.
     *
     * @return resultado con el veredicto y el mensaje ya redactado
     */
    public ResultadoLogin iniciarSesion(String nombre, String contrasena) {
        return repositorio.autenticar(nombre, HashContrasena.calcular(contrasena), equipoActual());
    }

    /** Recupera la entidad completa tras un inicio de sesión válido. */
    public Administrador obtenerAdministrador(ResultadoLogin resultado) {
        if (resultado == null || !resultado.autenticado()) return null;
        return repositorio.buscarPorEmail(resultado.email());
    }

    public void cerrarSesion(Administrador administrador) {
        if (administrador != null) repositorio.cerrarSesion(administrador.getEmail());
    }

    /** Nombre del equipo, para dejarlo registrado en la bitácora de accesos. */
    private static String equipoActual() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return null;
        }
    }
}
