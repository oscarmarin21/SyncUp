package com.syncup.controller;

import com.syncup.dto.ApiResponse;
import com.syncup.dto.SearchRequest;
import com.syncup.model.Cancion;
import com.syncup.service.BusquedaAvanzadaService;
import com.syncup.service.CancionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para búsqueda y consulta de canciones.
 * Requerido según RF-003 y RF-004.
 * 
 * @author SyncUp Team
 */
@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
public class CancionController {
    
    private final CancionService cancionService;
    private final BusquedaAvanzadaService busquedaAvanzadaService;
    
    /**
     * Busca canciones por autocompletado de título.
     * Requerido según RF-003.
     * 
     * @param prefix prefijo de búsqueda
     * @return lista de canciones que coinciden con el prefijo
     */
    @GetMapping("/autocomplete")
    public ResponseEntity<ApiResponse<List<Cancion>>> autocomplete(@RequestParam String prefix) {
        List<Cancion> canciones = cancionService.buscarPorAutocompletado(prefix);
        return ResponseEntity.ok(ApiResponse.success(canciones));
    }
    
    /**
     * Realiza una búsqueda avanzada de canciones por múltiples atributos.
     * Requerido según RF-004 (con concurrencia según RF-030).
     * 
     * @param request criterios de búsqueda (artista, género, año, operador)
     * @return lista de canciones que coinciden con los criterios
     */
    @PostMapping("/search/advanced")
    public ResponseEntity<ApiResponse<List<Cancion>>> buscarAvanzada(@RequestBody SearchRequest request) {
        List<Cancion> resultados = busquedaAvanzadaService.buscar(request);
        return ResponseEntity.ok(ApiResponse.success(resultados));
    }
    
    /**
     * Obtiene una canción por su ID.
     * 
     * @param id identificador de la canción
     * @return canción encontrada
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Cancion>> obtenerPorId(@PathVariable Long id) {
        return cancionService.obtenerPorId(id)
                .map(cancion -> ResponseEntity.ok(ApiResponse.success(cancion)))
                .orElse(ResponseEntity.notFound().build());
    }
}

