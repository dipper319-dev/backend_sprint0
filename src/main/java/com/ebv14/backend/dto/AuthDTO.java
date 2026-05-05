package com.ebv14.backend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

public class AuthDTO {

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "El nombre es obligatorio")
        private String nombre;

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "Correo inválido")
        private String email;

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "Mínimo 8 caracteres")
        @Pattern(regexp = ".*\\d.*", message = "Debe incluir números")
        private String password;

        @NotNull(message = "El balance inicial es obligatorio")
        @DecimalMin(value = "0.0", message = "El balance inicial no puede ser negativo")
        private BigDecimal balanceInicial;
    }

    @Data
    public static class LoginRequest {
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "Correo inválido")
        private String email;

        @NotBlank(message = "La contraseña es obligatoria")
        private String password;
    }

    @Data
    public static class LoginResponse {
        private String token;
        private String nombre;
        private String email;

        public LoginResponse(String token, String nombre, String email) {
            this.token = token;
            this.nombre = nombre;
            this.email = email;
        }
    }

    @Data
    public static class ForgotPasswordRequest {
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "Correo inválido")
        private String email;
    }

    @Data
    public static class ResetPasswordRequest {
        @NotBlank(message = "El código es obligatorio")
        private String codigo;

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "Mínimo 8 caracteres")
        private String nuevaPassword;
    }

    @Data
    public static class MessageResponse {
        private String mensaje;

        public MessageResponse(String mensaje) {
            this.mensaje = mensaje;
        }
    }
}
