package model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "administrador")
public class Administrador implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "username", nullable = false)
    private String nombre;

    @Column(name = "password_hash", nullable = false)
    private String contrasena;

    @OneToMany(mappedBy = "administrador", fetch = FetchType.LAZY)
    private List<Producto> productos = new ArrayList<>();
}
