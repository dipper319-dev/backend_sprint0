package com.ebv14.backend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AlertaPresupuestoDTO {

    @Data
    public static class Response {
        private Long id;
        private String tipoAlerta;
        private BigDecimal porcentajeConsumo;
        private BigDecimal montoGastado;
        private BigDecimal presupuesto;
        private LocalDateTime fechaCreacion;
        private String mesAno;

        public Response(com.ebv14.backend.model.AlertaPresupuesto alerta) {
            this.id = alerta.getId();
            this.tipoAlerta = alerta.getTipoAlerta().toString();
            this.porcentajeConsumo = alerta.getPorcentajeConsumo();
            this.montoGastado = alerta.getMontoGastado();
            this.presupuesto = alerta.getPresupuesto();
            this.fechaCreacion = alerta.getFechaCreacion();
            this.mesAno = alerta.getMesAno();
        }
    }

    @Data
    public static class PresupuestoInfo {
        private BigDecimal presupuestoMensual;
        private BigDecimal montoGastado;
        private BigDecimal porcentajeConsumo;
        private String tipoAlerta; // null, "PREVENTIVA" o "CRITICA"
        private boolean tieneAlerta;
    }
}