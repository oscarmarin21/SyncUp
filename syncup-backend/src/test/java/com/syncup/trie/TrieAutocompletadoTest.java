package com.syncup.trie;

import com.syncup.model.Cancion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para TrieAutocompletado.
 * Requerido según RF-031.
 * 
 * @author SyncUp Team
 */
class TrieAutocompletadoTest {
    
    private TrieAutocompletado trie;
    
    @BeforeEach
    void setUp() {
        trie = new TrieAutocompletado();
    }
    
    @Test
    void testBuscarPorPrefijo() {
        // Insertar canciones
        Cancion c1 = crearCancion(1L, "Bohemian Rhapsody");
        Cancion c2 = crearCancion(2L, "Bohemian");
        Cancion c3 = crearCancion(3L, "Another One Bites the Dust");
        Cancion c4 = crearCancion(4L, "Baby");
        
        trie.insertar(c1.getTitulo(), c1);
        trie.insertar(c2.getTitulo(), c2);
        trie.insertar(c3.getTitulo(), c3);
        trie.insertar(c4.getTitulo(), c4);
        
        // Buscar por prefijo "Bo"
        List<Cancion> resultados = trie.buscarPorPrefijo("Bo");
        
        assertEquals(2, resultados.size());
        assertTrue(resultados.contains(c1));
        assertTrue(resultados.contains(c2));
        
        // Buscar por prefijo "Baby"
        List<Cancion> resultadosBaby = trie.buscarPorPrefijo("Baby");
        assertEquals(1, resultadosBaby.size());
        assertTrue(resultadosBaby.contains(c4));
        
        // Buscar prefijo que no existe
        List<Cancion> resultadosVacios = trie.buscarPorPrefijo("XYZ");
        assertTrue(resultadosVacios.isEmpty());
    }
    
    @Test
    void testBuscarPorPrefijoCaseInsensitive() {
        Cancion c1 = crearCancion(1L, "Bohemian Rhapsody");
        trie.insertar(c1.getTitulo(), c1);
        
        List<Cancion> resultados = trie.buscarPorPrefijo("bohemian");
        assertEquals(1, resultados.size());
        
        resultados = trie.buscarPorPrefijo("BOHEMIAN");
        assertEquals(1, resultados.size());
    }
    
    @Test
    void testEliminar() {
        Cancion c1 = crearCancion(1L, "Test Song");
        trie.insertar(c1.getTitulo(), c1);
        
        assertFalse(trie.buscarPorPrefijo("Test").isEmpty());
        
        boolean eliminado = trie.eliminar(c1.getTitulo(), c1);
        assertTrue(eliminado);
        
        assertTrue(trie.buscarPorPrefijo("Test").isEmpty());
    }
    
    private Cancion crearCancion(Long id, String titulo) {
        Cancion cancion = new Cancion();
        cancion.setId(id);
        cancion.setTitulo(titulo);
        cancion.setArtista("Test Artist");
        cancion.setGenero("Rock");
        cancion.setAño(2020);
        cancion.setDuracion(240);
        return cancion;
    }
}

