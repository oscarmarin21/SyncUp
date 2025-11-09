package com.syncup.controller;

import com.syncup.dto.ApiResponse;
import com.syncup.dto.JwtResponse;
import com.syncup.dto.LoginRequest;
import com.syncup.dto.RegisterRequest;
import com.syncup.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller para autenticación y registro de usuarios.
 * Requerido según RF-001.
 * 
 * @author SyncUp Team
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * Registra un nuevo usuario en el sistema.
     * 
     * @param request datos de registro
     * @return respuesta con el usuario creado
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<JwtResponse>> register(@Valid @RequestBody RegisterRequest request) {
        authService.registrar(request);
        
        // Autenticar automáticamente después del registro
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(request.getUsername());
        loginRequest.setPassword(request.getPassword());
        JwtResponse jwtResponse = authService.autenticar(loginRequest);
        
        return ResponseEntity.ok(ApiResponse.success("Usuario registrado exitosamente", jwtResponse));
    }
    
    /**
     * Autentica un usuario y genera un token JWT.
     * 
     * @param request datos de login
     * @return respuesta con el token JWT
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@Valid @RequestBody LoginRequest request) {
        JwtResponse jwtResponse = authService.autenticar(request);
        return ResponseEntity.ok(ApiResponse.success("Autenticación exitosa", jwtResponse));
    }
}

