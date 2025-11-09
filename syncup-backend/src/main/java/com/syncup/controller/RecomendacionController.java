package com.syncup.controller;

import com.syncup.dto.ApiResponse;
import com.syncup.model.Cancion;
import com.syncup.service.CancionService;
import com.syncup.service.RecomendacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para recomendaciones musicales.
 * Requerido según RF-005 y RF-006.
 * 
 * @author SyncUp Team
 */
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecomendacionController {
    
    private final RecomendacionService recomendacionService;
    private final CancionService cancionService;
    
    /**
     * Genera una playlist de "Descubrimiento Semanal" basada en los gustos del usuario.
     * Requerido según RF-005.
     * 
     * @param authentication autenticación actual
     * @param maxCanciones número máximo de canciones (opcional, por defecto 20)
     * @return lista de canciones recomendadas
     */
    @GetMapping("/discovery-weekly")
    public ResponseEntity<ApiResponse<List<Cancion>>> generarDescubrimientoSemanal(
            Authentication authentication,
            @RequestParam(defaultValue = "20") int maxCanciones) {
        String username = authentication.getName();
        List<Cancion> recomendaciones = recomendacionService.generarDescubrimientoSemanal(username, maxCanciones);
        return ResponseEntity.ok(ApiResponse.success(recomendaciones));
    }
    
    /**
     * Inicia una "Radio" a partir de una canción semilla.
     * Requerido según RF-006.
     * 
     * @param songId identificador de la canción semilla
     * @param maxCanciones número máximo de canciones (opcional, por defecto 30)
     * @return lista de canciones para la radio
     */
    @PostMapping("/radio")
    public ResponseEntity<ApiResponse<List<Cancion>>> iniciarRadio(
            @RequestParam Long songId,
            @RequestParam(defaultValue = "30") int maxCanciones) {
        Cancion cancionSemilla = cancionService.obtenerPorId(songId)
                .orElseThrow(() -> new IllegalArgumentException("Canción no encontrada"));
        
        List<Cancion> radio = recomendacionService.iniciarRadio(cancionSemilla, maxCanciones);
        return ResponseEntity.ok(ApiResponse.success("Radio iniciada", radio));
    }
}

