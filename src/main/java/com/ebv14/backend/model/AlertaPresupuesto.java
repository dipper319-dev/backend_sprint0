package com.ebv14.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "alertas_presupuesto")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertaPresupuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAlerta tipoAlerta;

    @Column(nullable = false)
    private BigDecimal porcentajeConsumo;

    @Column(nullable = false)
    private BigDecimal montoGastado;

    @Column(nullable = false)
    private BigDecimal presupuesto;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "mes_ano")
    private String mesAno; // Formato: "2026-05"

    @PrePersist
    public void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
    }

    public enum TipoAlerta {
        PREVENTIVA,  // 80%
        CRITICA      // 100%+
    }
}