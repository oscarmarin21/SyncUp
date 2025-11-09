package com.syncup.security;

import com.syncup.model.Usuario;
import com.syncup.service.UsuarioIndexService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Filtro que intercepta las peticiones HTTP y valida los tokens JWT.
 * 
 * @author SyncUp Team
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtTokenProvider tokenProvider;
    private final UsuarioIndexService usuarioIndexService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                String username = tokenProvider.getUsernameFromToken(jwt);
                
                // Obtener el usuario para conocer su rol
                Usuario usuario = usuarioIndexService.getUsuario(username);
                
                if (usuario != null) {
                    // Asignar el rol correcto según el usuario
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    if (usuario.getRol() == Usuario.Rol.ADMIN) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                    }
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                    
                    // Crear autenticación con el rol correcto
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    username,
                                    null,
                                    authorities
                            );
                    
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception ex) {
            log.error("No se pudo establecer la autenticación del usuario: {}", ex.getMessage());
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * Extrae el token JWT del header Authorization.
     * 
     * @param request petición HTTP
     * @return token JWT o null si no existe
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

