package com.ebv14.backend.historial.service;

import com.ebv14.backend.historial.dto.HistorialDTO;
import com.ebv14.backend.historial.dto.HistorialDTO.MovimientoResponse;
import com.ebv14.backend.historial.dto.HistorialDTO.HistorialResponse;
import com.ebv14.backend.historial.repository.HistorialRepository;
import com.ebv14.backend.model.Transaccion;
import com.ebv14.backend.model.Usuario;
import com.ebv14.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de Historial de Movimientos.
 *
 * Cumple las HU:
 *  - Visualización del historial  → listarTodos()
 *  - Ordenamiento por fecha DESC  → queries en el repositorio
 *  - Filtrado por tipo            → filtrarPorTipo()
 *  - Filtrado por categoría       → filtrarPorCategoria()
 *  - Filtrado por tipo+categoría  → filtrarPorTipoYCategoria()
 *  - Consistencia de datos        → lectura directa desde BD sin caché
 */
@Service
@Transactional(readOnly = true)
public class HistorialService {

    private final HistorialRepository historialRepository;
    private final UsuarioRepository   usuarioRepository;

    public HistorialService(HistorialRepository historialRepository,
                            UsuarioRepository usuarioRepository) {
        this.historialRepository = historialRepository;
        this.usuarioRepository   = usuarioRepository;
    }

    /**
     * Retorna todos los movimientos del usuario autenticado,
     * ordenados del más reciente al más antiguo.
     */
    public HistorialResponse listarTodos(String email) {
        Long usuarioId = resolverUsuarioId(email);
        List<MovimientoResponse> movimientos = historialRepository
                .findAllByUsuarioOrdenados(usuarioId)
                .stream()
                .map(MovimientoResponse::new)
                .toList();
        return new HistorialResponse(movimientos);
    }

    /**
     * Filtra por tipo de movimiento: INGRESO o GASTO.
     */
    public HistorialResponse filtrarPorTipo(String email, String tipoStr) {
        Long usuarioId = resolverUsuarioId(email);
        Transaccion.TipoTransaccion tipo = parseTipo(tipoStr);

        List<MovimientoResponse> movimientos = historialRepository
                .findByUsuarioAndTipo(usuarioId, tipo)
                .stream()
                .map(MovimientoResponse::new)
                .toList();
        return new HistorialResponse(movimientos);
    }

    /**
     * Filtra por categoría (ID numérico de la categoría).
     */
    public HistorialResponse filtrarPorCategoria(String email, Long categoriaId) {
        Long usuarioId = resolverUsuarioId(email);
        List<MovimientoResponse> movimientos = historialRepository
                .findByUsuarioAndCategoria(usuarioId, categoriaId)
                .stream()
                .map(MovimientoResponse::new)
                .toList();
        return new HistorialResponse(movimientos);
    }

    /**
     * Filtra simultáneamente por tipo Y categoría.
     */
    public HistorialResponse filtrarPorTipoYCategoria(String email, String tipoStr, Long categoriaId) {
        Long usuarioId = resolverUsuarioId(email);
        Transaccion.TipoTransaccion tipo = parseTipo(tipoStr);

        List<MovimientoResponse> movimientos = historialRepository
                .findByUsuarioAndTipoAndCategoria(usuarioId, tipo, categoriaId)
                .stream()
                .map(MovimientoResponse::new)
                .toList();
        return new HistorialResponse(movimientos);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Long resolverUsuarioId(String email) {
        return usuarioRepository.findByEmail(email)
                .map(Usuario::getId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));
    }

    private Transaccion.TipoTransaccion parseTipo(String tipoStr) {
        try {
            return Transaccion.TipoTransaccion.valueOf(tipoStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Tipo inválido: '" + tipoStr + "'. Use INGRESO o GASTO.");
        }
    }
}
