package com.syncup.service;

import com.syncup.model.Usuario;
import com.syncup.repository.UsuarioRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio que mantiene un índice en memoria (HashMap) de usuarios para acceso O(1).
 * Requerido según RF-016.
 * 
 * @author SyncUp Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioIndexService {
    
    private final UsuarioRepository usuarioRepository;
    
    /**
     * HashMap para almacenar usuarios indexados por username.
     * Proporciona acceso O(1) según RF-016.
     */
    private final Map<String, Usuario> usuariosMap = new HashMap<>();
    
    /**
     * Carga todos los usuarios desde la base de datos al HashMap en memoria.
     * Se ejecuta al arranque de la aplicación mediante @PostConstruct.
     * Complejidad: O(n) donde n es el número de usuarios
     */
    @PostConstruct
    public void cargarUsuariosEnMemoria() {
        log.info("Cargando usuarios en memoria...");
        List<Usuario> usuarios = usuarioRepository.findAll();
        usuariosMap.clear();
        usuarios.forEach(usuario -> {
            // Verificar que el usuario tenga contraseña antes de agregarlo
            if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
                log.warn("Usuario '{}' cargado SIN contraseña desde BD!", usuario.getUsername());
            }
            usuariosMap.put(usuario.getUsername(), usuario);
        });
        log.info("{} usuarios cargados en memoria", usuariosMap.size());
        
        // Log de verificación
        usuariosMap.forEach((username, usuario) -> {
            boolean tienePassword = usuario.getPassword() != null && !usuario.getPassword().isEmpty();
            log.debug("Usuario en memoria: {} - Contraseña: {}", username, tienePassword ? "OK" : "FALTANTE");
        });
    }
    
    /**
     * Obtiene un usuario por su username desde el HashMap.
     * Si el usuario no tiene contraseña, intenta recargarlo desde BD.
     * Complejidad: O(1) normalmente, O(log n) si necesita recargar desde BD
     * 
     * @param username nombre de usuario a buscar
     * @return Usuario encontrado o null si no existe
     */
    public Usuario getUsuario(String username) {
        Usuario usuario = usuariosMap.get(username);
        
        // Si el usuario existe pero no tiene contraseña, intentar recargarlo desde BD
        if (usuario != null && (usuario.getPassword() == null || usuario.getPassword().isEmpty())) {
            log.warn("Usuario '{}' en memoria sin contraseña. Recargando desde BD...", username);
            Usuario usuarioDesdeBD = usuarioRepository.findByUsername(username).orElse(null);
            if (usuarioDesdeBD != null && usuarioDesdeBD.getPassword() != null && !usuarioDesdeBD.getPassword().isEmpty()) {
                log.info("Usuario '{}' recargado desde BD con contraseña", username);
                usuariosMap.put(username, usuarioDesdeBD);
                return usuarioDesdeBD;
            } else {
                log.error("Usuario '{}' tampoco tiene contraseña en BD!", username);
            }
        }
        
        return usuario;
    }
    
    /**
     * Agrega un usuario al HashMap en memoria.
     * Complejidad: O(1)
     * 
     * @param usuario usuario a agregar
     */
    public void agregarUsuario(Usuario usuario) {
        if (usuario == null) {
            log.warn("Intento de agregar usuario nulo al índice");
            return;
        }
        
        // Verificar que el usuario tenga contraseña
        if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
            log.warn("Usuario '{}' agregado al índice SIN contraseña!", usuario.getUsername());
            
            // Intentar cargarlo desde BD para obtener la contraseña
            Usuario usuarioDesdeBD = usuarioRepository.findByUsername(usuario.getUsername()).orElse(null);
            if (usuarioDesdeBD != null && usuarioDesdeBD.getPassword() != null && !usuarioDesdeBD.getPassword().isEmpty()) {
                log.info("Usuario '{}' recargado desde BD con contraseña antes de agregar al índice", usuario.getUsername());
                usuario = usuarioDesdeBD;
            } else {
                log.error("Usuario '{}' NO tiene contraseña en BD!", usuario.getUsername());
            }
        }
        
        usuariosMap.put(usuario.getUsername(), usuario);
    }
    
    /**
     * Elimina un usuario del HashMap en memoria.
     * Complejidad: O(1)
     * 
     * @param username nombre de usuario a eliminar
     */
    public void eliminarUsuario(String username) {
        usuariosMap.remove(username);
    }
    
    /**
     * Verifica si un usuario existe en el índice.
     * Complejidad: O(1)
     * 
     * @param username nombre de usuario a verificar
     * @return true si existe, false en caso contrario
     */
    public boolean existeUsuario(String username) {
        return usuariosMap.containsKey(username);
    }
    
    /**
     * Actualiza el índice después de modificar un usuario.
     * Complejidad: O(1)
     * 
     * @param usuario usuario actualizado
     */
    public void actualizarUsuario(Usuario usuario) {
        usuariosMap.put(usuario.getUsername(), usuario);
    }
    
    /**
     * Obtiene el mapa completo de usuarios (para uso interno en admin).
     * 
     * @return mapa de usuarios
     */
    public Map<String, Usuario> getUsuariosMap() {
        return usuariosMap;
    }
}

