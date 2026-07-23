package persistencia;

import java.util.List;
import model.Cliente;
import persistencia.exceptions.NonexistentEntityException;
import persistencia.exceptions.PreexistingEntityException;

public interface RepositorioCliente {
    void crear(Cliente cliente) throws PreexistingEntityException;
    void editar(Cliente cliente) throws NonexistentEntityException;
    void eliminar(String id) throws NonexistentEntityException;
    List<Cliente> buscarTodos();
    List<Cliente> buscarPorEstado(String estado);
    Cliente buscarPorId(String id);
    Cliente buscarPorNombre(String nombre);
    int contarTotal();

    /**
     * Búsqueda por texto resuelta en la base de datos con {@code fn_buscar_clientes},
     * que se apoya en el índice GIN de trigramas y ordena por relevancia
     * (primero las coincidencias por prefijo, luego por similitud).
     *
     * @param texto  fragmento a buscar; si está vacío devuelve lista vacía
     * @param limite número máximo de resultados
     */
    List<Cliente> buscarPorTexto(String texto, int limite);

    /**
     * Una página de clientes ordenados por nombre.
     *
     * @param estado   estado a filtrar, o {@code null} para todos
     * @param desde    índice del primer elemento (0 = primera página)
     * @param cantidad tamaño de la página
     */
    List<Cliente> buscarPagina(String estado, int desde, int cantidad);

    /** Total de clientes que cumplen el filtro; {@code null} cuenta todos. */
    int contarConFiltro(String estado);
}
