package controller;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CambiarContra {
    private String email;
    private String codigo;
    private LocalDateTime tiempoExpiracion;

    public boolean estaExpirado() {
        return LocalDateTime.now().isAfter(tiempoExpiracion);
    }
}
