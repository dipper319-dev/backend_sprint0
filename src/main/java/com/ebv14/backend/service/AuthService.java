package com.ebv14.backend.service;

import com.ebv14.backend.dto.AuthDTO.*;
import com.ebv14.backend.model.Usuario;
import com.ebv14.backend.repository.UsuarioRepository;
import com.ebv14.backend.security.JwtUtil;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Random;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager,
                       EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
    }

    public MessageResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El correo ya existe");
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .balanceInicial(request.getBalanceInicial())
                .build();

        usuarioRepository.save(usuario);
        return new MessageResponse("Registro exitoso");
    }

    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Datos ingresados son incorrectos");
        }

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String token = jwtUtil.generateToken(usuario.getEmail());
        return new LoginResponse(token, usuario.getNombre(), usuario.getEmail());
    }

    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Correo no registrado"));

        String codigo = String.format("%06d", new Random().nextInt(999999));
        usuario.setCodigoRecuperacion(codigo);
        usuario.setCodigoExpiracion(LocalDateTime.now().plusMinutes(15));
        usuarioRepository.save(usuario);

        emailService.sendRecoveryCode(usuario.getEmail(), codigo);
        return new MessageResponse("Código enviado a tu correo");
    }

    public MessageResponse resetPassword(ResetPasswordRequest request) {
        Usuario usuario = usuarioRepository.findByCodigoRecuperacion(request.getCodigo())
                .orElseThrow(() -> new RuntimeException("Código inválido"));

        if (usuario.getCodigoExpiracion().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("El código ha expirado");
        }

        usuario.setPassword(passwordEncoder.encode(request.getNuevaPassword()));
        usuario.setCodigoRecuperacion(null);
        usuario.setCodigoExpiracion(null);
        usuarioRepository.save(usuario);

        return new MessageResponse("Contraseña actualizada exitosamente");
    }
}
