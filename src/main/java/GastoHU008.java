package com.ebv14.backend.hu008;

import com.ebv14.backend.model.Categoria;
import com.ebv14.backend.model.Transaccion;
import com.ebv14.backend.model.Usuario;
import com.ebv14.backend.repository.CategoriaRepository;
import com.ebv14.backend.repository.TransaccionRepository;
import com.ebv14.backend.repository.UsuarioRepository;
import com.ebv14.backend.service.AlertaPresupuestoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Data
class GastoRequest {

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal monto;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 3, max = 100, message = "La descripción debe tener entre 3 y 100 caracteres")
    private String descripcion;

    @NotNull(message = "La categoría es obligatoria")
    private Long categoriaId;
}


@Data
class GastoResponse {

    private Long id;
    private BigDecimal monto;
    private String descripcion;
    private Long categoriaId;
    private String categoriaNombre;
    private String tipo;
    private LocalDateTime fechaTransaccion;
    private String mensaje;

    // Constructor: convertir de Transaccion a Response
    public GastoResponse(Transaccion t) {
        this.id = t.getId();
        this.monto = t.getMonto();
        this.descripcion = t.getDescripcion();
        if (t.getCategoria() != null) {
            this.categoriaId = t.getCategoria().getId();
            this.categoriaNombre = t.getCategoria().getNombre();
        } else {
            this.categoriaId = null;
            this.categoriaNombre = "Sin categoría";
        }
        this.tipo = t.getTipo().toString();
        this.fechaTransaccion = t.getFechaTransaccion();
        this.mensaje = "Gasto registrado correctamente";
    }
}


@Data
class BalanceResponse {

    private BigDecimal balanceInicial;
    private BigDecimal totalIngresos;
    private BigDecimal totalGastos;
    private BigDecimal balanceActual;

    public BalanceResponse(BigDecimal balanceInicial, BigDecimal ingresos, BigDecimal gastos) {
        this.balanceInicial = balanceInicial;
        this.totalIngresos = ingresos != null ? ingresos : BigDecimal.ZERO;
        this.totalGastos = gastos != null ? gastos : BigDecimal.ZERO;
        // Balance = inicial + ingresos - gastos
        this.balanceActual = balanceInicial
                .add(this.totalIngresos)
                .subtract(this.totalGastos);
    }
}


@Data
class ErrorResponse {

    private String error;
    private String codigo;
    private LocalDateTime timestamp;

    public ErrorResponse(String error, String codigo) {
        this.error = error;
        this.codigo = codigo;
        this.timestamp = LocalDateTime.now();
    }
}



@Service
class GastoService {

    private final TransaccionRepository transaccionRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final AlertaPresupuestoService alertaPresupuestoService;

    public GastoService(TransaccionRepository transaccionRepository,
                        UsuarioRepository usuarioRepository,
                        CategoriaRepository categoriaRepository,
                        AlertaPresupuestoService alertaPresupuestoService) {
        this.transaccionRepository = transaccionRepository;
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.alertaPresupuestoService = alertaPresupuestoService;
    }


    public GastoResponse registrarGasto(GastoRequest request, String email) {
        try {
            // PASO 1: Obtener usuario por email (del token JWT)
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado en la base de datos"));

            // PASO 2: Obtener categoría y validar que pertenece al usuario
            Categoria categoria = categoriaRepository.findByIdAndUsuarioId(request.getCategoriaId(), usuario.getId())
                    .orElseThrow(() -> new RuntimeException("La categoría seleccionada no es válida o no pertenece al usuario"));

            // PASO 3: Construir el objeto Transaccion (tipo SIEMPRE es GASTO)
            Transaccion gasto = Transaccion.builder()
                    .monto(request.getMonto())
                    .descripcion(request.getDescripcion())
                    .tipo(Transaccion.TipoTransaccion.GASTO)
                    .categoria(categoria)
                    .usuario(usuario)
                    .build();

            // PASO 4: Guardar en BD
            // La fecha se asigna automáticamente en @PrePersist
            Transaccion gastoGuardado = transaccionRepository.save(gasto);

            // PASO 5: ✨ Evaluar presupuesto y generar alerta si es necesario
            alertaPresupuestoService.evaluarYGenerarAlerta(usuario.getId());

            // PASO 6: Retornar respuesta
            return new GastoResponse(gastoGuardado);

        } catch (RuntimeException e) {
            throw new RuntimeException("Error al registrar gasto: " + e.getMessage());
        }
    }


    public List<GastoResponse> listarGastos(String email) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            return transaccionRepository
                    .findByUsuarioIdAndTipoOrderByFechaTransaccionDesc(
                            usuario.getId(),
                            Transaccion.TipoTransaccion.GASTO
                    )
                    .stream()
                    .map(GastoResponse::new)
                    .toList();

        } catch (RuntimeException e) {
            throw new RuntimeException("Error al listar gastos: " + e.getMessage());
        }
    }


    public BalanceResponse obtenerBalance(String email) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            BigDecimal totalIngresos = transaccionRepository
                    .sumByUsuarioIdAndTipo(usuario.getId(), Transaccion.TipoTransaccion.INGRESO);

            BigDecimal totalGastos = transaccionRepository
                    .sumByUsuarioIdAndTipo(usuario.getId(), Transaccion.TipoTransaccion.GASTO);

            return new BalanceResponse(
                    usuario.getBalanceInicial(),
                    totalIngresos,
                    totalGastos
            );

        } catch (RuntimeException e) {
            throw new RuntimeException("Error al obtener balance: " + e.getMessage());
        }
    }
}



@RestController
@RequestMapping("/api/gastos")
class GastoController {

    private final GastoService gastoService;

    public GastoController(GastoService gastoService) {
        this.gastoService = gastoService;
    }


    @PostMapping
    public ResponseEntity<?> registrarGasto(
            @Valid @RequestBody GastoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            GastoResponse respuesta = gastoService.registrarGasto(
                    request,
                    userDetails.getUsername()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage(), "GASTO_ERROR"));
        }
    }


    @GetMapping
    public ResponseEntity<?> listarGastos(
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            List<GastoResponse> gastos = gastoService.listarGastos(userDetails.getUsername());
            return ResponseEntity.ok(gastos);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage(), "LISTAR_ERROR"));
        }
    }


    @GetMapping("/balance")
    public ResponseEntity<?> obtenerBalance(
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            BalanceResponse balance = gastoService.obtenerBalance(userDetails.getUsername());
            return ResponseEntity.ok(balance);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(e.getMessage(), "BALANCE_ERROR"));
        }
    }
}