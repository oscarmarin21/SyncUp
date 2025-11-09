package com.syncup.graph.algoritmo;

import com.syncup.graph.GrafoDeSimilitud;
import com.syncup.model.Cancion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para el algoritmo de Dijkstra.
 * Requerido según RF-031.
 * 
 * @author SyncUp Team
 */
class DijkstraTest {
    
    private GrafoDeSimilitud grafo;
    private Cancion c1, c2, c3;
    
    @BeforeEach
    void setUp() {
        grafo = new GrafoDeSimilitud();
        
        c1 = crearCancion(1L, "Song 1", "Artist A", "Rock", 2020);
        c2 = crearCancion(2L, "Song 2", "Artist A", "Rock", 2021);
        c3 = crearCancion(3L, "Song 3", "Artist B", "Pop", 2020);
        
        // Construir un grafo simple
        grafo.agregarArista(c1, c2, 0.9); // Alta similitud
        grafo.agregarArista(c2, c3, 0.5); // Baja similitud
    }
    
    @Test
    void testEncontrarCamino() {
        List<Cancion> camino = Dijkstra.encontrarCamino(grafo, c1, c3);
        
        assertFalse(camino.isEmpty());
        assertEquals(c1, camino.get(0)); // Origen
        assertTrue(camino.contains(c3)); // Debe llegar al destino
    }
    
    @Test
    void testEncontrarSimilares() {
        List<Cancion> similares = Dijkstra.encontrarSimilares(grafo, c1, 5);
        
        assertFalse(similares.isEmpty());
        assertTrue(similares.contains(c2)); // Vecino directo con alta similitud
    }
    
    @Test
    void testMismaCancion() {
        List<Cancion> camino = Dijkstra.encontrarCamino(grafo, c1, c1);
        assertEquals(1, camino.size());
        assertEquals(c1, camino.get(0));
    }
    
    private Cancion crearCancion(Long id, String titulo, String artista, String genero, int año) {
        Cancion cancion = new Cancion();
        cancion.setId(id);
        cancion.setTitulo(titulo);
        cancion.setArtista(artista);
        cancion.setGenero(genero);
        cancion.setAño(año);
        cancion.setDuracion(240);
        return cancion;
    }
}

