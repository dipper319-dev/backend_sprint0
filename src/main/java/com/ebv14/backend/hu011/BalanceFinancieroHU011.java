package com.ebv14.backend.hu011;

import com.ebv14.backend.model.Transaccion;
import com.ebv14.backend.model.Usuario;
import com.ebv14.backend.repository.TransaccionRepository;
import com.ebv14.backend.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class BalanceFinancieroHU011 {
    private BalanceFinancieroHU011() {}
}

// ─── Response: un movimiento individual ───────────────────────────────────────
@Data
class MovimientoHU011Response {
    private Long id;
    private String tipo;
    private BigDecimal monto;
    private String descripcion;
    private LocalDateTime fechaTransaccion;

    public MovimientoHU011Response(Transaccion t) {
        this.id = t.getId();
        this.tipo = t.getTipo().toString();
        this.monto = t.getMonto();
        this.descripcion = t.getDescripcion();
        this.fechaTransaccion = t.getFechaTransaccion();
    }
}

// ─── Response: balance completo ───────────────────────────────────────────────
@Data
class BalanceHU011Response {
    private BigDecimal balanceInicial;
    private BigDecimal totalIngresos;
    private BigDecimal totalGastos;
    private BigDecimal balanceNeto;
    private List<MovimientoHU011Response> movimientosRecientes;
    private LocalDateTime consultadoEn;

    public BalanceHU011Response(BigDecimal balanceInicial,
                                BigDecimal totalIngresos,
                                BigDecimal totalGastos,
                                List<MovimientoHU011Response> movimientosRecientes) {
        this.balanceInicial = safe(balanceInicial);
        this.totalIngresos = safe(totalIngresos);
        this.totalGastos = safe(totalGastos);
        this.balanceNeto = this.totalIngresos.subtract(this.totalGastos);
        this.movimientosRecientes = movimientosRecientes;
        this.consultadoEn = LocalDateTime.now();
    }

    private BigDecimal safe(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}

// ─── Response: error ──────────────────────────────────────────────────────────
@Data
class ErrorHU011Response {
    private boolean exitoso = false;
    private String codigo;
    private String mensaje;
    private LocalDateTime fecha = LocalDateTime.now();

    public ErrorHU011Response(String codigo, String mensaje) {
        this.codigo = codigo;
        this.mensaje = mensaje;
    }
}

// ─── Service ──────────────────────────────────────────────────────────────────
@Service
class BalanceFinancieroHU011Service {

    private final TransaccionRepository transaccionRepository;
    private final UsuarioRepository usuarioRepository;

    public BalanceFinancieroHU011Service(TransaccionRepository transaccionRepository,
                                         UsuarioRepository usuarioRepository) {
        this.transaccionRepository = transaccionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public BalanceHU011Response obtenerBalance(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        BigDecimal totalIngresos = transaccionRepository.sumIngresosByUsuarioId(usuario.getId());
        BigDecimal totalGastos = transaccionRepository.sumGastosByUsuarioId(usuario.getId());

        List<MovimientoHU011Response> movimientosRecientes =
                transaccionRepository
                        .findByUsuarioIdOrderByFechaTransaccionDesc(usuario.getId())
                        .stream()
                        .limit(10)
                        .map(MovimientoHU011Response::new)
                        .toList();

        return new BalanceHU011Response(
                usuario.getBalanceInicial(),
                totalIngresos,
                totalGastos,
                movimientosRecientes
        );
    }
}

// ─── Controller ───────────────────────────────────────────────────────────────
@RestController
@RequestMapping("/api/balance")
class BalanceFinancieroHU011Controller {

    private final BalanceFinancieroHU011Service service;

    public BalanceFinancieroHU011Controller(BalanceFinancieroHU011Service service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> obtenerBalance(
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorHU011Response("NO_AUTENTICADO", "Debes iniciar sesion para ver tu balance"));
        }

        try {
            return ResponseEntity.ok(service.obtenerBalance(userDetails.getUsername()));
        } catch (EntityNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ErrorHU011Response("USUARIO_NO_ENCONTRADO", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorHU011Response("ERROR_INTERNO", "No se pudo obtener el balance"));
        }
    }
}