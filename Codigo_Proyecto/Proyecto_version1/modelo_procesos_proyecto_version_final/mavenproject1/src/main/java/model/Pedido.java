package model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
@Entity @Table(name = "pedidos")
public class Pedido implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id @Column(length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(nullable = false)
    private String estado;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_cobro")
    private LocalDateTime fechaCobro;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<Producto> productos = new ArrayList<>();

    public Pedido(String id, Cliente cliente, String estado, LocalDateTime fechaRegistro) {
        this.id = id;
        this.cliente = cliente;
        this.estado = estado;
        this.fechaRegistro = fechaRegistro;
    }


    public BigDecimal getTotal() {
        return productos.stream()
            .map(p -> p.getPrecio().multiply(BigDecimal.valueOf(p.getCantidad())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
