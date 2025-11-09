package com.syncup.controller;

import com.syncup.dto.ApiResponse;
import com.syncup.model.Cancion;
import com.syncup.model.Usuario;
import com.syncup.repository.CancionRepository;
import com.syncup.service.CancionService;
import com.syncup.service.UsuarioIndexService;
import com.syncup.service.SimilitudService;
import com.syncup.service.AudioStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller para funcionalidades de administración.
 * Requerido según RF-010, RF-011, RF-012, RF-013, RF-014.
 * 
 * @author SyncUp Team
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    private final CancionService cancionService;
    private final CancionRepository cancionRepository;
    private final UsuarioIndexService usuarioIndexService;
    private final SimilitudService similitudService;
    private final AudioStorageService audioStorageService;
    
    // ========== GESTIÓN DE CANCIONES (RF-010) ==========
    
    /**
     * Lista todas las canciones del catálogo.
     * 
     * @return lista de canciones
     */
    @GetMapping("/songs")
    public ResponseEntity<ApiResponse<List<Cancion>>> listarCanciones() {
        List<Cancion> canciones = cancionService.obtenerTodas();
        return ResponseEntity.ok(ApiResponse.success(canciones));
    }
    
    /**
     * Crea una nueva canción en el catálogo.
     * Requerido según RF-010.
     * 
     * @param cancion datos de la canción
     * @return canción creada
     */
    @PostMapping("/songs")
    public ResponseEntity<ApiResponse<Cancion>> crearCancion(@RequestBody Cancion cancion) {
        Cancion cancionCreada = cancionService.crear(cancion);
        
        // Agregar al grafo de similitud
        similitudService.agregarCancion(cancionCreada);
        
        return ResponseEntity.ok(ApiResponse.success("Canción creada", cancionCreada));
    }
    
    /**
     * Actualiza una canción existente.
     * Requerido según RF-010.
     * 
     * @param id identificador de la canción
     * @param cancion datos actualizados
     * @return canción actualizada
     */
    @PutMapping("/songs/{id}")
    public ResponseEntity<ApiResponse<Cancion>> actualizarCancion(
            @PathVariable Long id,
            @RequestBody Cancion cancion) {
        Cancion cancionActualizada = cancionService.actualizar(id, cancion);
        
        // Reconstruir grafo de similitud (simplificado: en producción se actualizaría incrementalmente)
        similitudService.reconstruirGrafo();
        
        return ResponseEntity.ok(ApiResponse.success("Canción actualizada", cancionActualizada));
    }
    
    /**
     * Elimina una canción del catálogo.
     * Requerido según RF-010.
     * 
     * @param id identificador de la canción
     * @return respuesta de éxito
     */
    @DeleteMapping("/songs/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarCancion(@PathVariable Long id) {
        cancionService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.success("Canción eliminada", null));
    }

    /**
     * Sube un archivo de audio y devuelve la URL pública.
     *
     * @param file archivo de audio
     * @return URL accesible para la canción
     */
    @PostMapping("/songs/upload-audio")
    public ResponseEntity<ApiResponse<Map<String, String>>> subirAudio(@RequestParam("file") MultipartFile file) {
        try {
            String url = audioStorageService.storeAudio(file);
            Map<String, String> data = Map.of("url", url);
            return ResponseEntity.ok(ApiResponse.success("Audio subido correctamente", data));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error al guardar el audio: " + e.getMessage()));
        }
    }
    
    /**
     * Carga canciones masivamente desde un archivo de texto plano.
     * Requerido según RF-012.
     * 
     * Formato esperado: una canción por línea
     * Formato: titulo|artista|genero|año|duracion
     * 
     * @param file archivo de texto con las canciones
     * @return número de canciones cargadas
     */
    @PostMapping("/songs/bulk-upload")
    public ResponseEntity<ApiResponse<Map<String, Object>>> cargarCancionesMasivamente(
            @RequestParam("file") MultipartFile file) {
        int cancionesCargadas = 0;
        int errores = 0;
        List<String> erroresDetalle = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            
            String linea;
            int numeroLinea = 0;
            
            while ((linea = reader.readLine()) != null) {
                numeroLinea++;
                linea = linea.trim();
                
                if (linea.isEmpty()) {
                    continue;
                }
                
                try {
                    String[] partes = linea.split("\\|");
                    if (partes.length != 5) {
                        errores++;
                        erroresDetalle.add("Línea " + numeroLinea + ": formato incorrecto (esperado: titulo|artista|genero|año|duracion)");
                        continue;
                    }
                    
                    Cancion cancion = new Cancion();
                    cancion.setTitulo(partes[0].trim());
                    cancion.setArtista(partes[1].trim());
                    cancion.setGenero(partes[2].trim());
                    cancion.setAño(Integer.parseInt(partes[3].trim()));
                    cancion.setDuracion(Integer.parseInt(partes[4].trim()));
                    
                    cancionService.crear(cancion);
                    cancionesCargadas++;
                    
                } catch (Exception e) {
                    errores++;
                    erroresDetalle.add("Línea " + numeroLinea + ": " + e.getMessage());
                }
            }
            
            // Reconstruir grafo de similitud después de cargar todas las canciones
            similitudService.reconstruirGrafo();
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error al leer el archivo: " + e.getMessage()));
        }
        
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("cancionesCargadas", cancionesCargadas);
        resultado.put("errores", errores);
        resultado.put("erroresDetalle", erroresDetalle);
        
        return ResponseEntity.ok(ApiResponse.success("Carga masiva completada", resultado));
    }
    
    // ========== GESTIÓN DE USUARIOS (RF-011) ==========
    
    /**
     * Lista todos los usuarios del sistema.
     * Requerido según RF-011.
     * 
     * @return lista de usuarios
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<Usuario>>> listarUsuarios() {
        List<Usuario> usuarios = usuarioIndexService.getUsuariosMap().values().stream()
                .map(u -> {
                    u.setPassword(null); // No retornar contraseñas
                    return u;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(usuarios));
    }
    
    /**
     * Elimina un usuario del sistema.
     * Requerido según RF-011.
     * 
     * @param username username del usuario a eliminar
     * @return respuesta de éxito
     */
    @DeleteMapping("/users/{username}")
    public ResponseEntity<ApiResponse<Void>> eliminarUsuario(@PathVariable String username) {
        Usuario usuario = usuarioIndexService.getUsuario(username);
        if (usuario == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Eliminar de la BD y del índice
        // En una implementación completa, se eliminaría de la BD también
        usuarioIndexService.eliminarUsuario(username);
        
        return ResponseEntity.ok(ApiResponse.success("Usuario eliminado", null));
    }
    
    // ========== MÉTRICAS (RF-013, RF-014) ==========
    
    /**
     * Obtiene métricas de géneros (para gráfico Pie Chart).
     * Requerido según RF-013 y RF-014.
     * 
     * @return mapa con género y cantidad de canciones
     */
    @GetMapping("/metrics/genres")
    public ResponseEntity<ApiResponse<Map<String, Long>>> obtenerMetricasGeneros() {
        Map<String, Long> metricas = cancionRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        Cancion::getGenero,
                        Collectors.counting()
                ));
        
        return ResponseEntity.ok(ApiResponse.success(metricas));
    }
    
    /**
     * Obtiene métricas de artistas más populares (para gráfico Bar Chart).
     * Requerido según RF-013 y RF-014.
     * 
     * @param top número de artistas top a retornar (opcional, por defecto 10)
     * @return mapa con artista y cantidad de canciones
     */
    @GetMapping("/metrics/artists")
    public ResponseEntity<ApiResponse<Map<String, Long>>> obtenerMetricasArtistas(
            @RequestParam(defaultValue = "10") int top) {
        Map<String, Long> metricas = cancionRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        Cancion::getArtista,
                        Collectors.counting()
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(top)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        java.util.LinkedHashMap::new
                ));
        
        return ResponseEntity.ok(ApiResponse.success(metricas));
    }
}

