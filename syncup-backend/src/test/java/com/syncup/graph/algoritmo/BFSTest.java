package com.syncup.graph.algoritmo;

import com.syncup.graph.GrafoSocial;
import com.syncup.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para el algoritmo BFS.
 * Requerido según RF-031.
 * 
 * @author SyncUp Team
 */
class BFSTest {
    
    private GrafoSocial grafo;
    private Usuario u1, u2, u3, u4;
    
    @BeforeEach
    void setUp() {
        grafo = new GrafoSocial();
        
        u1 = crearUsuario("user1", "Usuario 1");
        u2 = crearUsuario("user2", "Usuario 2");
        u3 = crearUsuario("user3", "Usuario 3");
        u4 = crearUsuario("user4", "Usuario 4");
        
        // Construir grafo: u1 -> u2 -> u3, u2 -> u4
        grafo.seguir(u1, u2);
        grafo.seguir(u2, u3);
        grafo.seguir(u2, u4);
    }
    
    @Test
    void testEncontrarUsuariosSugeridos() {
        List<Usuario> sugeridos = BFS.encontrarUsuariosSugeridos(grafo, u1, 2, 10);
        
        assertFalse(sugeridos.isEmpty());
        // u1 sigue a u2, entonces debería sugerir u3 y u4 (amigos de u2)
        assertTrue(sugeridos.contains(u3) || sugeridos.contains(u4));
    }
    
    @Test
    void testProfundidadMaxima() {
        // Profundidad 1: solo amigos directos
        List<Usuario> profundidad1 = BFS.encontrarUsuariosSugeridos(grafo, u1, 1, 10);
        
        // Profundidad 2: amigos de amigos
        List<Usuario> profundidad2 = BFS.encontrarUsuariosSugeridos(grafo, u1, 2, 10);
        
        assertTrue(profundidad2.size() >= profundidad1.size());
    }
    
    @Test
    void testMaxUsuarios() {
        List<Usuario> sugeridos = BFS.encontrarUsuariosSugeridos(grafo, u1, 2, 1);
        assertTrue(sugeridos.size() <= 1);
    }
    
    private Usuario crearUsuario(String username, String nombre) {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername(username);
        usuario.setNombre(nombre);
        usuario.setPassword("password");
        usuario.setRol(Usuario.Rol.USER);
        return usuario;
    }
}

