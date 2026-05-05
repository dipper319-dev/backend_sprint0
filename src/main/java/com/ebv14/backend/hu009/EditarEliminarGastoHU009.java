package com.ebv14.backend.hu009;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ebv14.backend.model.Categoria;
import com.ebv14.backend.model.Transaccion;
import com.ebv14.backend.model.Usuario;
import com.ebv14.backend.repository.CategoriaRepository;
import com.ebv14.backend.repository.TransaccionRepository;
import com.ebv14.backend.repository.UsuarioRepository;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

public final class EditarEliminarGastoHU009 {
    private EditarEliminarGastoHU009() {
    }
}

@Data
@NoArgsConstructor
class EditarGastoHU009Request {

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El monto debe ser mayor que 0")
    private BigDecimal monto;

    @NotBlank(message = "La descripci\u00f3n es obligatoria")
    @Size(min = 3, max = 100, message = "La descripci\u00f3n debe tener entre 3 y 100 caracteres")
    private String descripcion;

    @NotNull(message = "La categor\u00eda es obligatoria")
    private Long categoriaId;
}

@Data
class DetalleGastoHU009Response {
    private boolean exitoso;
    private String mensaje;
    private Long id;
    private BigDecimal monto;
    private String descripcion;
    private String tipo;
    private Long categoriaId;
    private String categoriaNombre;
    private LocalDateTime fechaTransaccion;
    private LocalDateTime fechaModificacion;

    public DetalleGastoHU009Response(Transaccion gasto) {
        this.exitoso = true;
        this.mensaje = "Detalle del gasto";
        this.id = gasto.getId();
        this.monto = gasto.getMonto();
        this.descripcion = gasto.getDescripcion();
        this.tipo = gasto.getTipo().toString();
        this.fechaTransaccion = gasto.getFechaTransaccion();
        this.fechaModificacion = gasto.getFechaModificacion();

        if (gasto.getCategoria() != null) {
            this.categoriaId = gasto.getCategoria().getId();
            this.categoriaNombre = gasto.getCategoria().getNombre();
        } else {
            this.categoriaId = null;
            this.categoriaNombre = "Sin categor\u00eda";
        }
    }
}

@Data
class EdicionGastoHU009Response {
    private boolean exitoso;
    private String mensaje;
    private DetalleGastoHU009Response gasto;
    private BalanceActualizadoHU009Response balanceActualizado;
    private String mesOperacion;
    private LocalDateTime fechaModificacion;

    public EdicionGastoHU009Response(Transaccion gasto, BalanceActualizadoHU009Response balanceActualizado) {
        this.exitoso = true;
        this.mensaje = "Actualizaci\u00f3n exitosa";
        this.gasto = new DetalleGastoHU009Response(gasto);
        this.balanceActualizado = balanceActualizado;
        this.mesOperacion = YearMonth.now().toString();
        this.fechaModificacion = gasto.getFechaModificacion();
    }
}

@Data
class EliminacionGastoHU009Response {
    private boolean exitoso;
    private String mensaje;
    private Long idEliminado;
    private BigDecimal montoEliminado;
    private Long categoriaIdEliminada;
    private String categoriaNombreEliminada;
    private BalanceActualizadoHU009Response balanceActualizado;
    private String mesOperacion;
    private LocalDateTime fechaEliminacion;

    public EliminacionGastoHU009Response(Long idEliminado,
                                         BigDecimal montoEliminado,
                                         Categoria categoriaEliminada,
                                         BalanceActualizadoHU009Response balanceActualizado,
                                         LocalDateTime fechaEliminacion) {
        this.exitoso = true;
        this.mensaje = "Eliminaci\u00f3n exitosa";
        this.idEliminado = idEliminado;
        this.montoEliminado = montoEliminado;
        this.balanceActualizado = balanceActualizado;
        this.mesOperacion = YearMonth.now().toString();
        this.fechaEliminacion = fechaEliminacion;

        if (categoriaEliminada != null) {
            this.categoriaIdEliminada = categoriaEliminada.getId();
            this.categoriaNombreEliminada = categoriaEliminada.getNombre();
        } else {
            this.categoriaIdEliminada = null;
            this.categoriaNombreEliminada = "Sin categor\u00eda";
        }
    }
}

@Data
class BalanceActualizadoHU009Response {
    private BigDecimal balanceInicial;
    private BigDecimal totalIngresos;
    private BigDecimal totalGastos;
    private BigDecimal balanceActual;

    public BalanceActualizadoHU009Response(BigDecimal balanceInicial,
                                           BigDecimal totalIngresos,
                                           BigDecimal totalGastos) {
        this.balanceInicial = valorSeguro(balanceInicial);
        this.totalIngresos = valorSeguro(totalIngresos);
        this.totalGastos = valorSeguro(totalGastos);
        this.balanceActual = this.balanceInicial.add(this.totalIngresos).subtract(this.totalGastos);
    }

    private BigDecimal valorSeguro(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }
}

@Data
class ErrorHU009Response {
    private boolean exitoso;
    private String codigo;
    private String mensaje;
    private Map<String, String> errores;
    private LocalDateTime fecha;

    public ErrorHU009Response(String codigo, String mensaje) {
        this.exitoso = false;
        this.codigo = codigo;
        this.mensaje = mensaje;
        this.errores = Map.of();
        this.fecha = LocalDateTime.now();
    }

    public ErrorHU009Response(String codigo, String mensaje, Map<String, String> errores) {
        this.exitoso = false;
        this.codigo = codigo;
        this.mensaje = mensaje;
        this.errores = errores;
        this.fecha = LocalDateTime.now();
    }
}

@Getter
class GastoHU009Exception extends RuntimeException {
    private final HttpStatus status;
    private final String codigo;

    public GastoHU009Exception(HttpStatus status, String codigo, String mensaje) {
        super(mensaje);
        this.status = status;
        this.codigo = codigo;
    }
}

@Service
class EditarEliminarGastoHU009Service {

    private final TransaccionRepository transaccionRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;

    public EditarEliminarGastoHU009Service(TransaccionRepository transaccionRepository,
                                           UsuarioRepository usuarioRepository,
                                           CategoriaRepository categoriaRepository) {
        this.transaccionRepository = transaccionRepository;
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public DetalleGastoHU009Response obtenerDetalleGasto(Long id, String email) {
        Usuario usuario = obtenerUsuarioAutenticado(email);
        Transaccion gasto = obtenerGastoDelUsuario(id, usuario);
        return new DetalleGastoHU009Response(gasto);
    }

    @Transactional
    public EdicionGastoHU009Response editarGasto(Long id, EditarGastoHU009Request request, String email) {
        Usuario usuario = obtenerUsuarioAutenticado(email);
        Transaccion gasto = obtenerGastoDelUsuario(id, usuario);
        Categoria categoria = obtenerCategoriaDelUsuario(request.getCategoriaId(), usuario);

        gasto.setMonto(request.getMonto());
        gasto.setDescripcion(request.getDescripcion().trim());
        gasto.setCategoria(categoria);
        gasto.setFechaModificacion(LocalDateTime.now());

        Transaccion gastoActualizado = transaccionRepository.save(gasto);
        BalanceActualizadoHU009Response balanceActualizado = obtenerBalanceActualizado(usuario);

        return new EdicionGastoHU009Response(gastoActualizado, balanceActualizado);
    }

    @Transactional
    public EliminacionGastoHU009Response eliminarGasto(Long id, String email) {
        Usuario usuario = obtenerUsuarioAutenticado(email);
        Transaccion gasto = obtenerGastoDelUsuario(id, usuario);

        Long idEliminado = gasto.getId();
        BigDecimal montoEliminado = gasto.getMonto();
        Categoria categoriaEliminada = gasto.getCategoria();
        LocalDateTime fechaEliminacion = LocalDateTime.now();

        transaccionRepository.delete(gasto);
        transaccionRepository.flush();

        BalanceActualizadoHU009Response balanceActualizado = obtenerBalanceActualizado(usuario);

        return new EliminacionGastoHU009Response(
                idEliminado,
                montoEliminado,
                categoriaEliminada,
                balanceActualizado,
                fechaEliminacion
        );
    }

    public BalanceActualizadoHU009Response obtenerBalanceActualizado(Usuario usuario) {
        BigDecimal totalIngresos = transaccionRepository.sumIngresosByUsuarioId(usuario.getId());
        BigDecimal totalGastos = transaccionRepository.sumGastosByUsuarioId(usuario.getId());
        return new BalanceActualizadoHU009Response(usuario.getBalanceInicial(), totalIngresos, totalGastos);
    }

    private Usuario obtenerUsuarioAutenticado(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new GastoHU009Exception(
                        HttpStatus.NOT_FOUND,
                        "USUARIO_NO_ENCONTRADO",
                        "Usuario no encontrado"
                ));
    }

    private Transaccion obtenerGastoDelUsuario(Long id, Usuario usuario) {
        if (id == null) {
            throw new GastoHU009Exception(
                    HttpStatus.BAD_REQUEST,
                    "ID_INVALIDO",
                    "El id del gasto es obligatorio"
            );
        }

        Transaccion transaccion = transaccionRepository.findById(id)
                .orElseThrow(() -> new GastoHU009Exception(
                        HttpStatus.NOT_FOUND,
                        "GASTO_NO_ENCONTRADO",
                        "El gasto no existe"
                ));

        if (transaccion.getUsuario() == null || !usuario.getId().equals(transaccion.getUsuario().getId())) {
            throw new GastoHU009Exception(
                    HttpStatus.FORBIDDEN,
                    "GASTO_DE_OTRO_USUARIO",
                        "No puedes ver, editar ni eliminar gastos de otro usuario"
            );
        }

        if (transaccion.getTipo() != Transaccion.TipoTransaccion.GASTO) {
            throw new GastoHU009Exception(
                    HttpStatus.BAD_REQUEST,
                    "NO_ES_GASTO",
                    "Este endpoint solo permite consultar, editar o eliminar gastos"
            );
        }

        return transaccion;
    }

    private Categoria obtenerCategoriaDelUsuario(Long categoriaId, Usuario usuario) {
        if (categoriaId == null) {
            throw new GastoHU009Exception(
                    HttpStatus.BAD_REQUEST,
                    "CATEGORIA_OBLIGATORIA",
                    "La categor\u00eda es obligatoria"
            );
        }

        return categoriaRepository.findByIdAndUsuarioId(categoriaId, usuario.getId())
                .orElseThrow(() -> new GastoHU009Exception(
                        HttpStatus.BAD_REQUEST,
                        "CATEGORIA_INVALIDA",
                        "La categor\u00eda seleccionada no existe o no pertenece al usuario"
                ));
    }
}

@RestController
@RequestMapping("/api/gastos")
class EditarEliminarGastoHU009Controller {

    private final EditarEliminarGastoHU009Service service;

    public EditarEliminarGastoHU009Controller(EditarEliminarGastoHU009Service service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerDetalleGasto(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(service.obtenerDetalleGasto(id, obtenerEmail(userDetails)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editarGasto(
            @PathVariable Long id,
            @Valid @RequestBody EditarGastoHU009Request request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(service.editarGasto(id, request, obtenerEmail(userDetails)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarGasto(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(service.eliminarGasto(id, obtenerEmail(userDetails)));
    }

    private String obtenerEmail(UserDetails userDetails) {
        if (userDetails == null) {
            throw new GastoHU009Exception(
                    HttpStatus.UNAUTHORIZED,
                    "USUARIO_NO_AUTENTICADO",
                    "Debes iniciar sesi\u00f3n para realizar esta acci\u00f3n"
            );
        }
        return userDetails.getUsername();
    }

    @ExceptionHandler(GastoHU009Exception.class)
    public ResponseEntity<ErrorHU009Response> manejarErrorHU009(GastoHU009Exception e) {
        return ResponseEntity
                .status(e.getStatus())
                .body(new ErrorHU009Response(e.getCodigo(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorHU009Response> manejarErroresValidacion(MethodArgumentNotValidException e) {
        Map<String, String> errores = new HashMap<>();

        e.getBindingResult().getAllErrors().forEach(error -> {
            if (error instanceof FieldError fieldError) {
                errores.put(fieldError.getField(), fieldError.getDefaultMessage());
            }
        });

        return ResponseEntity
                .badRequest()
                .body(new ErrorHU009Response(
                        "DATOS_INVALIDOS",
                        "Hay datos inv\u00e1lidos en la solicitud",
                        errores
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorHU009Response> manejarErrorGeneral(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorHU009Response(
                        "ERROR_INTERNO",
                        "No se pudo procesar la solicitud. Intenta nuevamente"
                ));
    }
}
