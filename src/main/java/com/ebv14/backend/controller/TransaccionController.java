package com.ebv14.backend.controller;

import com.ebv14.backend.dto.TransaccionDTO.*;
import com.ebv14.backend.service.TransaccionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransaccionController {

    private final TransaccionService transaccionService;

    public TransaccionController(TransaccionService transaccionService) {
        this.transaccionService = transaccionService;
    }

    // HU-005: Registrar ingreso o gasto
    @PostMapping
    public ResponseEntity<Response> registrar(
            @Valid @RequestBody Request request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(transaccionService.registrar(request, userDetails.getUsername()));
    }

    // Listar todas las transacciones del usuario
    @GetMapping
    public ResponseEntity<List<Response>> listar(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(transaccionService.listar(userDetails.getUsername()));
    }

    // Obtener balance del usuario
    @GetMapping("/balance")
    public ResponseEntity<BalanceResponse> getBalance(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(transaccionService.getBalance(userDetails.getUsername()));
    }
}
