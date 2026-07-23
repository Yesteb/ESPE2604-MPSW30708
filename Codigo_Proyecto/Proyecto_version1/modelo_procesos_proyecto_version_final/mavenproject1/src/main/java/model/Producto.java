package model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
@Entity @Table(name = "productos")
public class Producto implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id @Column(length = 36, nullable = false)
    private String id;

    private String tipo;
    private String estilo;
    private String talla;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false)
    private int cantidad;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "producto_atributos", joinColumns = @JoinColumn(name = "producto_id"))
    @MapKeyColumn(name = "categoria")
    @Column(name = "valor")
    private Map<String, String> atributos = new HashMap<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "administrador_email")
    private Administrador administrador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = true)
    private Pedido pedido;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "producto_etiquetas",
        joinColumns = @JoinColumn(name = "producto_id"),
        inverseJoinColumns = @JoinColumn(name = "etiqueta_id")
    )
    private List<EtiquetaConfig> etiquetas = new ArrayList<>();

    public Producto(String id, String tipo, String estilo, String talla,
                    String descripcion, BigDecimal precio, int cantidad,
                    LocalDateTime fechaRegistro, Administrador administrador) {
        this.id = id;
        this.tipo = tipo;
        this.estilo = estilo;
        this.talla = talla;
        this.descripcion = descripcion;
        this.precio = precio;
        this.cantidad = cantidad;
        this.fechaRegistro = fechaRegistro;
        this.administrador = administrador;
    }
}
