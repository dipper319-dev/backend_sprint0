package com.ebv14.backend.dto;

import com.ebv14.backend.model.Transaccion;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransaccionDTO {

    @Data
    public static class Request {
        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser superior a cero")
        private BigDecimal monto;

        private String descripcion;

        @NotNull(message = "El tipo es obligatorio")
        private Transaccion.TipoTransaccion tipo;
    }

    @Data
    public static class Response {
        private Long id;
        private BigDecimal monto;
        private String descripcion;
        private Transaccion.TipoTransaccion tipo;
        private LocalDateTime fechaTransaccion;

        public Response(Transaccion t) {
            this.id = t.getId();
            this.monto = t.getMonto();
            this.descripcion = t.getDescripcion();
            this.tipo = t.getTipo();
            this.fechaTransaccion = t.getFechaTransaccion();
        }
    }

    @Data
    public static class BalanceResponse {
        private BigDecimal balanceInicial;
        private BigDecimal totalIngresos;
        private BigDecimal totalGastos;
        private BigDecimal balance;

        public BalanceResponse(BigDecimal balanceInicial, BigDecimal ingresos, BigDecimal gastos) {
            this.balanceInicial = balanceInicial;
            this.totalIngresos = ingresos;
            this.totalGastos = gastos;
            // balance = dinero inicial + ingresos - gastos
            this.balance = balanceInicial.add(ingresos).subtract(gastos);
        }
    }
}
