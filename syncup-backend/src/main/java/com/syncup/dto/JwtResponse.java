package com.syncup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO para respuestas de autenticación con JWT.
 * 
 * @author SyncUp Team
 */
@Data
@AllArgsConstructor
public class JwtResponse {
    
    private String token;
    private String type = "Bearer";
    private String username;
    private String nombre;
    private String rol;
}

