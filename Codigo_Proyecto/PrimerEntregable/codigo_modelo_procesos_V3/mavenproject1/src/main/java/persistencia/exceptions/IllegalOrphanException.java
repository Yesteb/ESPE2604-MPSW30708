package persistencia.exceptions;

import java.util.ArrayList;
import java.util.List;

public class IllegalOrphanException extends Exception {
    private List<String> mensajes;
    public IllegalOrphanException(List<String> mensajes) {
        super((mensajes != null && mensajes.size() > 0 ? mensajes.get(0) : null));
        if (mensajes == null) {
            this.mensajes = new ArrayList<String>();
        } else {
            this.mensajes = mensajes;
        }
    }
    public List<String> getMensajes() {
        return mensajes;
    }
}
