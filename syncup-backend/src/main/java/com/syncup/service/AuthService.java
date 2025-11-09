package com.syncup.service;

import com.syncup.dto.JwtResponse;
import com.syncup.dto.LoginRequest;
import com.syncup.dto.RegisterRequest;
import com.syncup.model.Usuario;
import com.syncup.repository.UsuarioRepository;
import com.syncup.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de autenticación y registro de usuarios.
 * Requerido según RF-001.
 * 
 * @author SyncUp Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UsuarioRepository usuarioRepository;
    private final com.syncup.service.UsuarioIndexService usuarioIndexService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    
    /**
     * Registra un nuevo usuario en el sistema.
     * Requerido según RF-001.
     * 
     * @param request datos de registro
     * @return usuario creado
     * @throws IllegalArgumentException si el username ya existe
     */
    @Transactional
    public Usuario registrar(RegisterRequest request) {
        // Verificar si el username ya existe
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("El username ya está en uso");
        }
        
        // Crear nuevo usuario
        Usuario usuario = new Usuario();
        usuario.setUsername(request.getUsername());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setNombre(request.getNombre());
        usuario.setRol(Usuario.Rol.USER);
        
        // Guardar en BD
        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        
        // Agregar al índice en memoria
        usuarioIndexService.agregarUsuario(usuarioGuardado);
        
        log.info("Usuario '{}' registrado exitosamente", request.getUsername());
        return usuarioGuardado;
    }
    
    /**
     * Autentica un usuario y genera un token JWT.
     * Requerido según RF-001.
     * 
     * @param request datos de login
     * @return respuesta con token JWT
     * @throws IllegalArgumentException si las credenciales son inválidas
     */
    public JwtResponse autenticar(LoginRequest request) {
        // Buscar usuario desde el índice en memoria (O(1))
        Usuario usuario = usuarioIndexService.getUsuario(request.getUsername());
        
        if (usuario == null) {
            log.warn("Intento de login con username no existente: {}", request.getUsername());
            throw new IllegalArgumentException("Credenciales inválidas");
        }
        
        // Verificar que el usuario tenga una contraseña válida
        if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
            log.error("Usuario '{}' no tiene contraseña en memoria. Intentando recargar desde BD...", request.getUsername());
            
            // Intentar recargar desde BD
            Usuario usuarioDesdeBD = usuarioRepository.findByUsername(request.getUsername()).orElse(null);
            if (usuarioDesdeBD != null && usuarioDesdeBD.getPassword() != null && !usuarioDesdeBD.getPassword().isEmpty()) {
                log.info("Usuario '{}' recargado desde BD con contraseña. Actualizando índice...", request.getUsername());
                usuarioIndexService.agregarUsuario(usuarioDesdeBD);
                usuario = usuarioDesdeBD;
            } else {
                log.error("Usuario '{}' no tiene contraseña configurada ni en memoria ni en BD", request.getUsername());
                throw new IllegalArgumentException("Error en la configuración del usuario. Por favor contacte al administrador.");
            }
        }
        
        // Verificar contraseña
        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            log.warn("Intento de login con contraseña incorrecta para usuario: {}", request.getUsername());
            throw new IllegalArgumentException("Credenciales inválidas");
        }
        
        // Generar token JWT
        String token = jwtTokenProvider.generateToken(usuario.getUsername());
        
        log.info("Usuario '{}' autenticado exitosamente", request.getUsername());
        
        return new JwtResponse(
            token,
            "Bearer",
            usuario.getUsername(),
            usuario.getNombre(),
            usuario.getRol().name()
        );
    }
}

