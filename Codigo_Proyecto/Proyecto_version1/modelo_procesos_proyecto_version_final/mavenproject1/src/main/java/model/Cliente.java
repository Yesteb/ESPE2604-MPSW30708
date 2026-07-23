package model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
@Entity @Table(name = "clientes")
public class Cliente implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id @Column(length = 36, nullable = false)
    private String id;

    @Column(nullable = false)
    private String nombre;

    private String telefono;

    @Column(length = 500)
    private String descripcion;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(columnDefinition = "VARCHAR(255) NOT NULL DEFAULT 'ACTIVO'")
    private String estado = "ACTIVO";

    public Cliente(String id, String nombre, String telefono, String descripcion, LocalDateTime fechaRegistro) {
        this.id = id;
        this.nombre = nombre;
        this.telefono = telefono;
        this.descripcion = descripcion;
        this.fechaRegistro = fechaRegistro;
        this.estado = "ACTIVO";
    }
}
