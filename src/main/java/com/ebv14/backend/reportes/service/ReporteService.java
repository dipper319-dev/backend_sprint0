package com.ebv14.backend.reportes.service;

import com.ebv14.backend.model.Transaccion;
import com.ebv14.backend.model.Usuario;
import com.ebv14.backend.reportes.dto.ReporteDTO;
import com.ebv14.backend.reportes.dto.ReporteDTO.*;
import com.ebv14.backend.reportes.repository.ReporteRepository;
import com.ebv14.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de Reportes Financieros.
 *
 * Cumple las HU:
 *  - Selección de parámetros  → acepta rango fechas, tipo, categoría
 *  - Procesamiento de datos   → consulta BD filtrando correctamente
 *  - Agrupación por categoría → gastosPorCategoria() con porcentaje
 *  - Resumen financiero       → totalIngresos, totalGastos, balance neto
 *  - Consistencia de datos    → lectura directa desde BD (readOnly=true)
 */
@Service
@Transactional(readOnly = true)
public class ReporteService {

    private final ReporteRepository  reporteRepository;
    private final UsuarioRepository  usuarioRepository;

    public ReporteService(ReporteRepository reporteRepository,
                          UsuarioRepository usuarioRepository) {
        this.reporteRepository = reporteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Genera el reporte completo aplicando todos los filtros indicados.
     *
     * @param email  email del usuario autenticado
     * @param filtro parámetros enviados por el frontend
     * @return {@link ReporteResponse} con movimientos, resumen y distribución
     */
    public ReporteResponse generarReporte(String email, FiltroReporte filtro) {

        Long usuarioId = resolverUsuarioId(email);

        // ── 1. Convertir fechas (null = sin límite) ────────────────────────────
        LocalDateTime desde = parsearDesde(filtro.getDesde());
        LocalDateTime hasta = parsearHasta(filtro.getHasta());

        // ── 2. Resolver tipo (null = ambos) ───────────────────────────────────
        Transaccion.TipoTransaccion tipo = resolverTipo(filtro.getTipo());

        Long catId = filtro.getCategoriaId();

        // ── 3. Obtener movimientos filtrados ──────────────────────────────────
        List<Transaccion> transacciones = reporteRepository.findConFiltros(
            usuarioId, desde, hasta, tipo, catId
        );

        // ── 4. Calcular totales ───────────────────────────────────────────────
        BigDecimal totalIngresos;
        BigDecimal totalGastos;

        if (tipo == Transaccion.TipoTransaccion.INGRESO) {
            // Solo ingresos solicitados → gastos = 0
            totalIngresos = reporteRepository.sumIngresosPorRango(usuarioId, desde, hasta);
            totalGastos   = BigDecimal.ZERO;
        } else if (tipo == Transaccion.TipoTransaccion.GASTO) {
            // Solo gastos solicitados → ingresos = 0
            totalIngresos = BigDecimal.ZERO;
            totalGastos   = reporteRepository.sumGastosPorRango(usuarioId, desde, hasta, catId);
        } else {
            // Ambos
            totalIngresos = reporteRepository.sumIngresosPorRango(usuarioId, desde, hasta);
            totalGastos   = reporteRepository.sumGastosPorRango(usuarioId, desde, hasta, catId);
        }

        // ── 5. Distribución de gastos por categoría ───────────────────────────
        List<CategoriaSummary> porCategoria = buildCategoriaSummary(
            usuarioId, desde, hasta, totalGastos
        );

        // ── 6. Mapear movimientos a DTO ───────────────────────────────────────
        List<MovimientoReporte> movimientosDTO = transacciones.stream()
            .map(t -> new MovimientoReporte(
                t.getId(),
                t.getMonto(),
                t.getDescripcion(),
                t.getTipo().name(),
                t.getCategoria() != null ? t.getCategoria().getNombre() : null,
                t.getCategoria() != null ? t.getCategoria().getId()     : null,
                t.getFechaTransaccion() != null ? t.getFechaTransaccion().toString() : null
            ))
            .toList();

        return new ReporteResponse(
            totalIngresos, totalGastos,
            porCategoria, movimientosDTO,
            filtro.getDesde(), filtro.getHasta(),
            filtro.getTipo(), catId
        );
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Long resolverUsuarioId(String email) {
        return usuarioRepository.findByEmail(email)
            .map(Usuario::getId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));
    }

    /**
     * Construye la lista de categorías con totales y porcentajes.
     * El porcentaje se calcula sobre el totalGastos del rango/filtro actual.
     */
    private List<CategoriaSummary> buildCategoriaSummary(
            Long usuarioId, LocalDateTime desde, LocalDateTime hasta,
            BigDecimal totalGastos) {

        List<Object[]> rows = reporteRepository.gastosPorCategoria(usuarioId, desde, hasta);
        List<CategoriaSummary> result = new ArrayList<>();

        for (Object[] row : rows) {
            Long       catId    = (Long)       row[0];
            String     nombre   = (String)     row[1];
            long       count    = (long)        row[2];
            BigDecimal total    = (BigDecimal) row[3];

            int porcentaje = 0;
            if (totalGastos.compareTo(BigDecimal.ZERO) > 0) {
                porcentaje = total
                    .multiply(BigDecimal.valueOf(100))
                    .divide(totalGastos, 0, RoundingMode.HALF_UP)
                    .intValue();
            }

            result.add(new CategoriaSummary(catId, nombre, (int) count, total, porcentaje));
        }
        return result;
    }

    /** Parsea "yyyy-MM-dd" a inicio del día, o null si la cadena está vacía. */
    private LocalDateTime parsearDesde(String fecha) {
        if (fecha == null || fecha.isBlank()) return null;
        try {
            return LocalDate.parse(fecha).atStartOfDay();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de fecha 'desde' inválido: " + fecha);
        }
    }

    /** Parsea "yyyy-MM-dd" a fin del día (23:59:59), o null si la cadena está vacía. */
    private LocalDateTime parsearHasta(String fecha) {
        if (fecha == null || fecha.isBlank()) return null;
        try {
            return LocalDate.parse(fecha).atTime(LocalTime.MAX);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de fecha 'hasta' inválido: " + fecha);
        }
    }

    /** Convierte "INGRESO"/"GASTO" al enum. "AMBOS" o null → null (sin filtro de tipo). */
    private Transaccion.TipoTransaccion resolverTipo(String tipo) {
        if (tipo == null || tipo.isBlank() || "AMBOS".equalsIgnoreCase(tipo)) return null;
        try {
            return Transaccion.TipoTransaccion.valueOf(tipo.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Tipo inválido: '" + tipo + "'. Use INGRESO, GASTO o AMBOS.");
        }
    }
}
