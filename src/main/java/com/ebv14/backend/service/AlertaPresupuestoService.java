package com.ebv14.backend.service;

import com.ebv14.backend.model.AlertaPresupuesto;
import com.ebv14.backend.model.Usuario;
import com.ebv14.backend.repository.AlertaPresupuestoRepository;
import com.ebv14.backend.repository.TransaccionRepository;
import com.ebv14.backend.repository.UsuarioRepository;
import com.ebv14.backend.model.Transaccion;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Service
public class AlertaPresupuestoService {

    private final AlertaPresupuestoRepository alertaRepository;
    private final UsuarioRepository usuarioRepository;
    private final TransaccionRepository transaccionRepository;

    public AlertaPresupuestoService(AlertaPresupuestoRepository alertaRepository,
                                    UsuarioRepository usuarioRepository,
                                    TransaccionRepository transaccionRepository) {
        this.alertaRepository = alertaRepository;
        this.usuarioRepository = usuarioRepository;
        this.transaccionRepository = transaccionRepository;
    }

    // Calcular porcentaje de presupuesto consumido
    public BigDecimal calcularPorcentajeConsumo(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.getPresupuestoMensual().compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalGastos = transaccionRepository
                .sumByUsuarioIdAndTipo(usuarioId, Transaccion.TipoTransaccion.GASTO);

        if (totalGastos == null || totalGastos.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        // Calcular porcentaje: (gastos / presupuesto) * 100
        return totalGastos.divide(usuario.getPresupuestoMensual(), 4, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    // Evaluar y generar alertas
    public AlertaPresupuesto evaluarYGenerarAlerta(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.getPresupuestoMensual().compareTo(BigDecimal.ZERO) <= 0) {
            return null; // No hay presupuesto configurado
        }

        BigDecimal porcentaje = calcularPorcentajeConsumo(usuarioId);
        BigDecimal totalGastos = transaccionRepository
                .sumByUsuarioIdAndTipo(usuarioId, Transaccion.TipoTransaccion.GASTO);

        String mesAno = YearMonth.now().toString();
        AlertaPresupuesto.TipoAlerta tipo = null;

        // Determinar tipo de alerta
        if (porcentaje.compareTo(new BigDecimal("100")) >= 0) {
            tipo = AlertaPresupuesto.TipoAlerta.CRITICA;
        } else if (porcentaje.compareTo(new BigDecimal("80")) >= 0) {
            tipo = AlertaPresupuesto.TipoAlerta.PREVENTIVA;
        } else {
            return null; // No hay alerta
        }

        // Crear y guardar la alerta
        AlertaPresupuesto alerta = AlertaPresupuesto.builder()
                .usuario(usuario)
                .tipoAlerta(tipo)
                .porcentajeConsumo(porcentaje)
                .montoGastado(totalGastos)
                .presupuesto(usuario.getPresupuestoMensual())
                .mesAno(mesAno)
                .build();

        return alertaRepository.save(alerta);
    }

    // Obtener alertas del mes actual
    public java.util.List<AlertaPresupuesto> obtenerAlertasDelMes(Long usuarioId) {
        String mesAno = YearMonth.now().toString();
        return alertaRepository.findByUsuarioAndMes(usuarioId, mesAno);
    }

    // Obtener última alerta
    public AlertaPresupuesto obtenerUltimaAlerta(Long usuarioId) {
        return alertaRepository.findLastAlert(usuarioId).orElse(null);
    }
}