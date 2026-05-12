
package model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "usuario")
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "username", nullable = false)
    private String usuario;

    @Column(name = "password_hash", nullable = false)
    private String password;
}
