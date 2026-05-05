package com.ebv14.backend.service;

import com.ebv14.backend.dto.TransaccionDTO.*;
import com.ebv14.backend.model.*;
import com.ebv14.backend.repository.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TransaccionService {

    private final TransaccionRepository transaccionRepository;
    private final UsuarioRepository usuarioRepository;

    public TransaccionService(TransaccionRepository transaccionRepository,
                              UsuarioRepository usuarioRepository) {
        this.transaccionRepository = transaccionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Response registrar(Request request, String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Transaccion transaccion = Transaccion.builder()
                .monto(request.getMonto())
                .descripcion(request.getDescripcion())
                .tipo(request.getTipo())
                .usuario(usuario)
                .build();

        return new Response(transaccionRepository.save(transaccion));
    }

    public List<Response> listar(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return transaccionRepository
                .findByUsuarioIdOrderByFechaTransaccionDesc(usuario.getId())
                .stream()
                .map(Response::new)
                .toList();
    }

    public BalanceResponse getBalance(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        var ingresos = transaccionRepository.sumIngresosByUsuarioId(usuario.getId());
        var gastos = transaccionRepository.sumGastosByUsuarioId(usuario.getId());

        // ✅ Se suma el balance inicial al cálculo
        return new BalanceResponse(
            usuario.getBalanceInicial(),
            ingresos,
            gastos
        );
    }
}
