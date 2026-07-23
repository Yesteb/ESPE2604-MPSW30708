package persistencia.exceptions;

public class NonexistentEntityException extends Exception {
    public NonexistentEntityException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
    public NonexistentEntityException(String mensaje) {
        super(mensaje);
    }
}
