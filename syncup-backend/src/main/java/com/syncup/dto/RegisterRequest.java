package com.syncup.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO para solicitudes de registro.
 * 
 * @author SyncUp Team
 */
@Data
public class RegisterRequest {
    
    @NotBlank(message = "El username es requerido")
    private String username;
    
    @NotBlank(message = "La contraseña es requerida")
    private String password;
    
    @NotBlank(message = "El nombre es requerido")
    private String nombre;
}

