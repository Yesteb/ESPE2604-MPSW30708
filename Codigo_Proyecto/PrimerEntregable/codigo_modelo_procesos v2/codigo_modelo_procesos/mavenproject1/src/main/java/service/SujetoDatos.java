package service;

public interface SujetoDatos {
    void agregarObservador(ObservadorDatos observador);
    void eliminarObservador(ObservadorDatos observador);
    void notificarObservadores();
}
