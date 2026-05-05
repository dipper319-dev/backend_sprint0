package com.ebv14.backend.controller;

import com.ebv14.backend.model.Usuario;
import com.ebv14.backend.repository.UsuarioRepository;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/presupuesto")
public class PresupuestoController {

    private final UsuarioRepository usuarioRepository;

    public PresupuestoController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Data
    public static class PresupuestoRequest {
        private BigDecimal presupuestoMensual;
    }

    // Obtener presupuesto actual
    @GetMapping
    public ResponseEntity<?> obtenerPresupuesto(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            Map<String, Object> response = new HashMap<>();
            response.put("presupuestoMensual", usuario.getPresupuestoMensual());
            response.put("mensaje", "Presupuesto obtenido correctamente");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Error al obtener presupuesto: " + e.getMessage())
            );
        }
    }

    // Actualizar presupuesto
    @PutMapping
    public ResponseEntity<?> actualizarPresupuesto(
            @RequestBody PresupuestoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (request.getPresupuestoMensual() == null || request.getPresupuestoMensual().compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "El presupuesto debe ser un valor positivo mayor a 0")
                );
            }

            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            usuario.setPresupuestoMensual(request.getPresupuestoMensual());
            usuarioRepository.save(usuario);

            Map<String, Object> response = new HashMap<>();
            response.put("presupuestoMensual", usuario.getPresupuestoMensual());
            response.put("mensaje", "Presupuesto configurado con éxito");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Error al actualizar presupuesto: " + e.getMessage())
            );
        }
    }
}