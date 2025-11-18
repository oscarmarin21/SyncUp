package com.syncup.controller;

import com.syncup.dto.ApiResponse;
import com.syncup.model.Cancion;
import com.syncup.model.Usuario;
import com.syncup.repository.UsuarioRepository;
import com.syncup.service.FavoritosService;
import com.syncup.service.UsuarioIndexService;
import com.syncup.util.CsvExporter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;

/**
 * Controller para gestión de perfil de usuario y favoritos.
 * Requerido según RF-002 y RF-009.
 *
 * @author SyncUp Team
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioIndexService usuarioIndexService;
    private final UsuarioRepository usuarioRepository;
    private final FavoritosService favoritosService;
    private final CsvExporter csvExporter;
    private final com.syncup.service.CancionService cancionService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Busca usuarios por nombre o username.
     * Endpoint usado por el buscador de usuarios en la sección Social.
     *
     * @param query texto a buscar (parcial) en los campos {@code nombre} o {@code username}
     * @return respuesta con la lista de usuarios que coinciden con la búsqueda
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Usuario>>> buscarUsuarios(
            @RequestParam String query
    ) {
        String term = query == null ? "" : query.trim();
        if (term.isEmpty()) {
            // Si no hay término, devolvemos lista vacía sin error
            return ResponseEntity.ok(ApiResponse.success(List.of()));
        }

        List<Usuario> resultados =
                usuarioRepository.findByNombreContainingIgnoreCaseOrUsernameContainingIgnoreCase(
                        term, term
                );

        // No retornar contraseñas en la respuesta
        resultados.forEach(u -> u.setPassword(null));

        return ResponseEntity.ok(ApiResponse.success(resultados));
    }

    /**
     * Obtiene el perfil del usuario actual.
     *
     * @param authentication autenticación actual
     * @return perfil del usuario
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Usuario>> obtenerPerfil(Authentication authentication) {
        String username = authentication.getName();
        Usuario usuario = usuarioIndexService.getUsuario(username);

        if (usuario == null) {
            return ResponseEntity.notFound().build();
        }

        // No retornar la contraseña
        usuario.setPassword(null);
        return ResponseEntity.ok(ApiResponse.success(usuario));
    }

    /**
     * Actualiza el perfil del usuario actual.
     * Requerido según RF-002.
     *
     * @param authentication autenticación actual
     * @param usuario datos actualizados
     * @return usuario actualizado
     */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<Usuario>> actualizarPerfil(
            Authentication authentication,
            @RequestBody Usuario usuario) {
        String username = authentication.getName();
        Usuario usuarioActual = usuarioIndexService.getUsuario(username);

        if (usuarioActual == null) {
            return ResponseEntity.notFound().build();
        }

        // Actualizar campos permitidos
        if (usuario.getNombre() != null) {
            usuarioActual.setNombre(usuario.getNombre());
        }
        if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
            usuarioActual.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }

        Usuario usuarioGuardado = usuarioRepository.save(usuarioActual);
        usuarioIndexService.actualizarUsuario(usuarioGuardado);
        usuarioGuardado.setPassword(null);

        return ResponseEntity.ok(ApiResponse.success("Perfil actualizado", usuarioGuardado));
    }

    /**
     * Obtiene la lista de canciones favoritas del usuario.
     * Requerido según RF-002.
     *
     * @param authentication autenticación actual
     * @return lista de canciones favoritas
     */
    @GetMapping("/me/favorites")
    public ResponseEntity<ApiResponse<LinkedList<Cancion>>> obtenerFavoritos(Authentication authentication) {
        String username = authentication.getName();
        LinkedList<Cancion> favoritos = favoritosService.obtenerFavoritos(username);
        return ResponseEntity.ok(ApiResponse.success(favoritos));
    }

    /**
     * Agrega una canción a los favoritos del usuario.
     * Requerido según RF-002.
     *
     * @param authentication autenticación actual
     * @param songId identificador de la canción
     * @return respuesta de éxito
     */
    @PostMapping("/me/favorites/{songId}")
    public ResponseEntity<ApiResponse<Void>> agregarFavorito(
            Authentication authentication,
            @PathVariable Long songId) {
        String username = authentication.getName();

        Cancion cancion = cancionService.obtenerPorId(songId)
                .orElseThrow(() -> new IllegalArgumentException("Canción no encontrada"));

        favoritosService.agregarFavorito(username, cancion);
        return ResponseEntity.ok(ApiResponse.success("Canción agregada a favoritos", null));
    }

    /**
     * Elimina una canción de los favoritos del usuario.
     * Requerido según RF-002.
     *
     * @param authentication autenticación actual
     * @param songId identificador de la canción
     * @return respuesta de éxito
     */
    @DeleteMapping("/me/favorites/{songId}")
    public ResponseEntity<ApiResponse<Void>> eliminarFavorito(
            Authentication authentication,
            @PathVariable Long songId) {
        String username = authentication.getName();

        Cancion cancion = cancionService.obtenerPorId(songId)
                .orElseThrow(() -> new IllegalArgumentException("Canción no encontrada"));

        favoritosService.eliminarFavorito(username, cancion);
        return ResponseEntity.ok(ApiResponse.success("Canción eliminada de favoritos", null));
    }

    /**
     * Descarga un reporte CSV de las canciones favoritas del usuario.
     * Requerido según RF-009.
     *
     * @param authentication autenticación actual
     * @param response respuesta HTTP
     */
    @GetMapping("/me/favorites/export")
    public void exportarFavoritos(Authentication authentication, HttpServletResponse response) {
        String username = authentication.getName();
        LinkedList<Cancion> favoritos = favoritosService.obtenerFavoritos(username);

        byte[] csvBytes = csvExporter.exportarFavoritos(favoritos);

        response.setContentType(MediaType.TEXT_PLAIN_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=favoritos_" + username + ".csv");
        response.setContentLength(csvBytes.length);

        try {
            response.getOutputStream().write(csvBytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            throw new RuntimeException("Error al exportar favoritos", e);
        }
    }
}
