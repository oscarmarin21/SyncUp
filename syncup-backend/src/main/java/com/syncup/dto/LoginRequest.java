package com.syncup.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO para solicitudes de login.
 * 
 * @author SyncUp Team
 */
@Data
public class LoginRequest {
    
    @NotBlank(message = "El username es requerido")
    private String username;
    
    @NotBlank(message = "La contraseña es requerida")
    private String password;
}

