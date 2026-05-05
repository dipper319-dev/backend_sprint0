package com.ebv14.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "balance_inicial", nullable = false)
    @Builder.Default
    private BigDecimal balanceInicial = BigDecimal.ZERO;

    @Column(name = "presupuesto_mensual", nullable = false)
    @Builder.Default
    private BigDecimal presupuestoMensual = BigDecimal.ZERO;

    @Column(name = "codigo_recuperacion")
    private String codigoRecuperacion;

    @Column(name = "codigo_expiracion")
    private LocalDateTime codigoExpiracion;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Categoria> categorias = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.fechaRegistro = LocalDateTime.now();
        if (this.balanceInicial == null) {
            this.balanceInicial = BigDecimal.ZERO;
        }
        if (this.presupuestoMensual == null) {
            this.presupuestoMensual = BigDecimal.ZERO;
        }
    }
}