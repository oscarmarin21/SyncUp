package com.syncup.service;

import com.syncup.graph.GrafoSocial;
import com.syncup.graph.algoritmo.BFS;
import com.syncup.model.Usuario;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * Servicio que gestiona las relaciones sociales entre usuarios usando el Grafo Social.
 * Requerido según RF-007, RF-008, RF-023, RF-024.
 * 
 * @author SyncUp Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SocialService {
    
    private final com.syncup.service.UsuarioIndexService usuarioIndexService;
    
    /**
     * Instancia del grafo social.
     */
    private final GrafoSocial grafoSocial = new GrafoSocial();
    
    /**
     * Carga las relaciones sociales desde la base de datos al grafo.
     * En una implementación completa, se cargarían desde una tabla de relaciones.
     * Por ahora, se inicializa vacío y se construye dinámicamente.
     */
    @PostConstruct
    public void inicializar() {
        log.info("Grafo social inicializado");
        // En una implementación completa, aquí se cargarían las relaciones desde BD
    }
    
    /**
     * Establece una relación de seguimiento entre dos usuarios.
     * Requerido según RF-007.
     * 
     * @param seguidorUsername username del usuario que sigue
     * @param seguidoUsername username del usuario que es seguido
     * @return true si se estableció la relación, false en caso contrario
     */
    public boolean seguirUsuario(String seguidorUsername, String seguidoUsername) {
        Usuario seguidor = usuarioIndexService.getUsuario(seguidorUsername);
        Usuario seguido = usuarioIndexService.getUsuario(seguidoUsername);
        
        if (seguidor == null || seguido == null) {
            log.warn("Intento de seguir con usuarios inválidos: {} -> {}", 
                    seguidorUsername, seguidoUsername);
            return false;
        }
        
        if (seguidor.equals(seguido)) {
            log.warn("Un usuario no puede seguirse a sí mismo");
            return false;
        }
        
        grafoSocial.seguir(seguidor, seguido);
        log.info("Usuario '{}' ahora sigue a '{}'", seguidorUsername, seguidoUsername);
        return true;
    }
    
    /**
     * Elimina la relación de seguimiento entre dos usuarios.
     * Requerido según RF-007.
     * 
     * @param seguidorUsername username del usuario que deja de seguir
     * @param seguidoUsername username del usuario que ya no es seguido
     * @return true si se eliminó la relación, false en caso contrario
     */
    public boolean dejarDeSeguir(String seguidorUsername, String seguidoUsername) {
        Usuario seguidor = usuarioIndexService.getUsuario(seguidorUsername);
        Usuario seguido = usuarioIndexService.getUsuario(seguidoUsername);
        
        if (seguidor == null || seguido == null) {
            return false;
        }
        
        grafoSocial.dejarDeSeguir(seguidor, seguido);
        log.info("Usuario '{}' dejó de seguir a '{}'", seguidorUsername, seguidoUsername);
        return true;
    }
    
    /**
     * Obtiene los usuarios seguidos por un usuario.
     * 
     * @param username username del usuario
     * @return conjunto de usuarios seguidos
     */
    public Set<Usuario> obtenerSeguidos(String username) {
        Usuario usuario = usuarioIndexService.getUsuario(username);
        if (usuario == null) {
            return Set.of();
        }
        return grafoSocial.obtenerSeguidos(usuario);
    }
    
    /**
     * Obtiene los seguidores de un usuario.
     * 
     * @param username username del usuario
     * @return conjunto de usuarios seguidores
     */
    public Set<Usuario> obtenerSeguidores(String username) {
        Usuario usuario = usuarioIndexService.getUsuario(username);
        if (usuario == null) {
            return Set.of();
        }
        return grafoSocial.obtenerSeguidores(usuario);
    }
    
    /**
     * Verifica si un usuario sigue a otro.
     * 
     * @param seguidorUsername username del seguidor
     * @param seguidoUsername username del seguido
     * @return true si sigue, false en caso contrario
     */
    public boolean sigueA(String seguidorUsername, String seguidoUsername) {
        Usuario seguidor = usuarioIndexService.getUsuario(seguidorUsername);
        Usuario seguido = usuarioIndexService.getUsuario(seguidoUsername);
        
        if (seguidor == null || seguido == null) {
            return false;
        }
        
        return grafoSocial.sigueA(seguidor, seguido);
    }
    
    /**
     * Obtiene sugerencias de usuarios a quienes seguir.
     * Requerido según RF-008.
     * Utiliza BFS en el grafo social para encontrar "amigos de amigos".
     * 
     * @param username username del usuario
     * @param maxSugerencias número máximo de sugerencias
     * @return lista de usuarios sugeridos
     */
    public List<Usuario> obtenerSugerencias(String username, int maxSugerencias) {
        Usuario usuario = usuarioIndexService.getUsuario(username);
        if (usuario == null) {
            return List.of();
        }
        
        // Usar BFS para encontrar usuarios a una profundidad de 2 (amigos de amigos)
        int profundidadMax = 2;
        return BFS.encontrarUsuariosSugeridos(grafoSocial, usuario, profundidadMax, maxSugerencias);
    }
    
    /**
     * Obtiene el grafo social.
     * 
     * @return instancia del grafo social
     */
    public GrafoSocial obtenerGrafo() {
        return grafoSocial;
    }
}

