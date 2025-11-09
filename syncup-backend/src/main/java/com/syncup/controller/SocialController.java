package com.syncup.controller;

import com.syncup.dto.ApiResponse;
import com.syncup.model.Usuario;
import com.syncup.service.SocialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller para funcionalidades sociales (seguir usuarios, sugerencias).
 * Requerido según RF-007 y RF-008.
 * 
 * @author SyncUp Team
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class SocialController {
    
    private final SocialService socialService;
    
    /**
     * Sigue a un usuario.
     * Requerido según RF-007.
     * 
     * @param authentication autenticación actual
     * @param username username del usuario a seguir
     * @return respuesta de éxito
     */
    @PostMapping("/{username}/follow")
    public ResponseEntity<ApiResponse<Void>> seguirUsuario(
            Authentication authentication,
            @PathVariable String username) {
        String seguidorUsername = authentication.getName();
        boolean exito = socialService.seguirUsuario(seguidorUsername, username);
        
        if (exito) {
            return ResponseEntity.ok(ApiResponse.success("Ahora sigues a " + username, null));
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("No se pudo seguir al usuario"));
        }
    }
    
    /**
     * Deja de seguir a un usuario.
     * Requerido según RF-007.
     * 
     * @param authentication autenticación actual
     * @param username username del usuario a dejar de seguir
     * @return respuesta de éxito
     */
    @DeleteMapping("/{username}/follow")
    public ResponseEntity<ApiResponse<Void>> dejarDeSeguir(
            Authentication authentication,
            @PathVariable String username) {
        String seguidorUsername = authentication.getName();
        boolean exito = socialService.dejarDeSeguir(seguidorUsername, username);
        
        if (exito) {
            return ResponseEntity.ok(ApiResponse.success("Dejaste de seguir a " + username, null));
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("No se pudo dejar de seguir al usuario"));
        }
    }
    
    /**
     * Obtiene sugerencias de usuarios a quienes seguir.
     * Requerido según RF-008.
     * 
     * @param authentication autenticación actual
     * @param maxSugerencias número máximo de sugerencias (opcional, por defecto 10)
     * @return lista de usuarios sugeridos
     */
    @GetMapping("/suggestions")
    public ResponseEntity<ApiResponse<List<Usuario>>> obtenerSugerencias(
            Authentication authentication,
            @RequestParam(defaultValue = "10") int maxSugerencias) {
        String username = authentication.getName();
        List<Usuario> sugerencias = socialService.obtenerSugerencias(username, maxSugerencias);
        
        // No retornar contraseñas
        sugerencias.forEach(u -> u.setPassword(null));
        
        return ResponseEntity.ok(ApiResponse.success(sugerencias));
    }

    /**
     * Obtiene la lista de usuarios a los que sigue el usuario autenticado.
     *
     * @param authentication autenticación actual
     * @return lista de usuarios seguidos
     */
    @GetMapping("/me/following")
    public ResponseEntity<ApiResponse<List<Usuario>>> obtenerSeguidos(Authentication authentication) {
        String username = authentication.getName();
        Set<Usuario> seguidos = socialService.obtenerSeguidos(username);
        List<Usuario> respuesta = seguidos.stream()
                .peek(u -> u.setPassword(null))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(respuesta));
    }

    /**
     * Obtiene la lista de usuarios que siguen al usuario autenticado.
     *
     * @param authentication autenticación actual
     * @return lista de seguidores
     */
    @GetMapping("/me/followers")
    public ResponseEntity<ApiResponse<List<Usuario>>> obtenerSeguidores(Authentication authentication) {
        String username = authentication.getName();
        Set<Usuario> seguidores = socialService.obtenerSeguidores(username);
        List<Usuario> respuesta = seguidores.stream()
                .peek(u -> u.setPassword(null))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(respuesta));
    }
}

