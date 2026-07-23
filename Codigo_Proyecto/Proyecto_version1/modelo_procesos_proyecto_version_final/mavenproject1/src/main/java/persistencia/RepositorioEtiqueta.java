package persistencia;

import java.util.List;
import model.EtiquetaConfig;
import persistencia.exceptions.NonexistentEntityException;

public interface RepositorioEtiqueta {
    void crear(EtiquetaConfig etiqueta);
    void eliminar(Long id) throws NonexistentEntityException;
    List<EtiquetaConfig> buscarPorCategoria(String categoria);
    void inicializarPorDefecto();
}
