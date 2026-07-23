package model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
@Entity @Table(name = "etiquetas_config")
public class EtiquetaConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String categoria;

    @Column(nullable = false)
    private String valor;

    @Column(name = "valor_numerico", precision = 10, scale = 2)
    private BigDecimal valorNumerico;

    private int orden;

    @ManyToMany(mappedBy = "etiquetas", fetch = FetchType.LAZY)
    private List<Producto> productos = new ArrayList<>();

    public EtiquetaConfig(String categoria, String valor, BigDecimal valorNumerico, int orden) {
        this.categoria = categoria;
        this.valor = valor;
        this.valorNumerico = valorNumerico;
        this.orden = orden;
    }
}
