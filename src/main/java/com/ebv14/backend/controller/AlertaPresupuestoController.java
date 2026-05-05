package com.ebv14.backend.controller;

import com.ebv14.backend.dto.AlertaPresupuestoDTO;
import com.ebv14.backend.model.AlertaPresupuesto;
import com.ebv14.backend.service.AlertaPresupuestoService;
import com.ebv14.backend.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/alertas")
public class AlertaPresupuestoController {

    private final AlertaPresupuestoService alertaService;
    private final UsuarioRepository usuarioRepository;

    public AlertaPresupuestoController(AlertaPresupuestoService alertaService,
                                       UsuarioRepository usuarioRepository) {
        this.alertaService = alertaService;
        this.usuarioRepository = usuarioRepository;
    }

    // Obtener información del presupuesto y alertas
    @GetMapping("/presupuesto")
    public ResponseEntity<?> obtenerPresupuesto(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            var usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            AlertaPresupuestoDTO.PresupuestoInfo info = new AlertaPresupuestoDTO.PresupuestoInfo();
            info.setPresupuestoMensual(usuario.getPresupuestoMensual());

            // Calcular porcentaje y generar alerta si es necesario
            java.math.BigDecimal porcentaje = alertaService.calcularPorcentajeConsumo(usuario.getId());
            info.setPorcentajeConsumo(porcentaje);

            // Obtener gastos totales
            java.math.BigDecimal gastos = new java.math.BigDecimal("0");
            AlertaPresupuesto ultimaAlerta = alertaService.obtenerUltimaAlerta(usuario.getId());

            if (ultimaAlerta != null) {
                info.setMontoGastado(ultimaAlerta.getMontoGastado());
                info.setTipoAlerta(ultimaAlerta.getTipoAlerta().toString());
                info.setTieneAlerta(true);
            } else {
                info.setTieneAlerta(false);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Información de presupuesto obtenida");
            response.put("data", info);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Error al obtener presupuesto: " + e.getMessage())
            );
        }
    }

    // Obtener alertas del mes
    @GetMapping("/mes")
    public ResponseEntity<?> obtenerAlertasDelMes(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            var usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            List<AlertaPresupuestoDTO.Response> alertas = alertaService
                    .obtenerAlertasDelMes(usuario.getId())
                    .stream()
                    .map(AlertaPresupuestoDTO.Response::new)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Alertas del mes obtenidas");
            response.put("cantidad", alertas.size());
            response.put("data", alertas);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Error al obtener alertas: " + e.getMessage())
            );
        }
    }

    // Evaluar presupuesto y generar alerta si es necesario
    @PostMapping("/evaluar")
    public ResponseEntity<?> evaluarPresupuesto(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            var usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            AlertaPresupuesto alerta = alertaService.evaluarYGenerarAlerta(usuario.getId());

            Map<String, Object> response = new HashMap<>();
            if (alerta != null) {
                response.put("mensaje", "Alerta generada");
                response.put("data", new AlertaPresupuestoDTO.Response(alerta));
                response.put("tieneAlerta", true);
            } else {
                response.put("mensaje", "Sin alertas");
                response.put("tieneAlerta", false);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Error al evaluar presupuesto: " + e.getMessage())
            );
        }
    }
}