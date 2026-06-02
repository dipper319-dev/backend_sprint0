package com.ebv14.backend.historial.controller;

import com.ebv14.backend.historial.dto.HistorialDTO.HistorialResponse;
import com.ebv14.backend.historial.service.HistorialService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Controller – Historial de Movimientos
 *
 * Endpoints:
 *
 *  GET /api/historial
 *      → Lista todos los movimientos del usuario autenticado,
 *        ordenados por fecha descendente.
 *
 *  GET /api/historial?tipo=INGRESO
 *  GET /api/historial?tipo=GASTO
 *      → Filtra únicamente por tipo.
 *
 *  GET /api/historial?categoriaId=3
 *      → Filtra únicamente por categoría.
 *
 *  GET /api/historial?tipo=GASTO&categoriaId=3
 *      → Filtra por tipo Y categoría simultáneamente.
 *
 * Todos los parámetros son opcionales; la combinación adecuada
 * se resuelve en el servicio. La actualización dinámica del
 * frontend se consigue porque cada cambio de filtro dispara
 * una nueva llamada a este endpoint sin recargar la página.
 *
 * Requiere JWT válido en el header Authorization: Bearer <token>
 */
@RestController
@RequestMapping("/api/historial")
public class HistorialController {

    private final HistorialService historialService;

    public HistorialController(HistorialService historialService) {
        this.historialService = historialService;
    }

    /**
     * Devuelve el historial de movimientos aplicando los filtros
     * opcionales recibidos como query params.
     *
     * @param tipo        "INGRESO" o "GASTO" (opcional)
     * @param categoriaId ID numérico de la categoría (opcional)
     * @param userDetails usuario autenticado inyectado por Spring Security
     */
    @GetMapping
    public ResponseEntity<HistorialResponse> listar(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Long categoriaId,
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();

        HistorialResponse response;

        if (tipo != null && categoriaId != null) {
            // Filtro combinado: tipo + categoría
            response = historialService.filtrarPorTipoYCategoria(email, tipo, categoriaId);

        } else if (tipo != null) {
            // Solo por tipo
            response = historialService.filtrarPorTipo(email, tipo);

        } else if (categoriaId != null) {
            // Solo por categoría
            response = historialService.filtrarPorCategoria(email, categoriaId);

        } else {
            // Sin filtros: todos los movimientos
            response = historialService.listarTodos(email);
        }

        return ResponseEntity.ok(response);
    }
}
