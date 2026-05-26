package controller;

import model.Administrador;
import persistencia.RepositorioAdministrador;
import utils.HashContrasena;

public class SistemaAutenticacion {

    private final RepositorioAdministrador repositorio;

    public SistemaAutenticacion(RepositorioAdministrador repositorio) {
        this.repositorio = repositorio;
    }

    public boolean iniciarSesion(String nombre, String contrasena) {
        Administrador administrador = repositorio.buscarPorNombre(nombre);
        if (administrador == null) return false;
        return administrador.getContrasena().equals(HashContrasena.calcular(contrasena));
    }

    public Administrador obtenerAdministradorAutenticado(String nombre, String contrasena) {
        Administrador administrador = repositorio.buscarPorNombre(nombre);
        if (administrador == null) return null;
        return administrador.getContrasena().equals(HashContrasena.calcular(contrasena)) ? administrador : null;
    }
}
