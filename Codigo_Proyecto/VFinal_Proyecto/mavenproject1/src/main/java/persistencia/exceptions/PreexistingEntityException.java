package persistencia.exceptions;

public class PreexistingEntityException extends Exception {
    public PreexistingEntityException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
    public PreexistingEntityException(String mensaje) {
        super(mensaje);
    }
}
