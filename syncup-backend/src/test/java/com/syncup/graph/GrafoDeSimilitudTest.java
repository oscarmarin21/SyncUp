package com.syncup.graph;

import com.syncup.model.Cancion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para GrafoDeSimilitud.
 * Requerido según RF-031.
 * 
 * @author SyncUp Team
 */
class GrafoDeSimilitudTest {
    
    private GrafoDeSimilitud grafo;
    private Cancion c1, c2, c3;
    
    @BeforeEach
    void setUp() {
        grafo = new GrafoDeSimilitud();
        
        c1 = crearCancion(1L, "Song 1", "Artist A", "Rock", 2020);
        c2 = crearCancion(2L, "Song 2", "Artist A", "Rock", 2021);
        c3 = crearCancion(3L, "Song 3", "Artist B", "Pop", 2020);
    }
    
    @Test
    void testCalcularSimilitud() {
        // Mismo artista y género -> alta similitud
        double similitud1 = grafo.calcularSimilitud(c1, c2);
        assertTrue(similitud1 > 0.8);
        
        // Diferente género y artista -> baja similitud
        double similitud2 = grafo.calcularSimilitud(c1, c3);
        assertTrue(similitud2 < similitud1);
        
        // Misma canción -> similitud 1.0
        double similitud3 = grafo.calcularSimilitud(c1, c1);
        assertEquals(1.0, similitud3);
    }
    
    @Test
    void testAgregarArista() {
        grafo.agregarArista(c1, c2, 0.9);
        
        assertTrue(grafo.existeArista(c1, c2));
        assertTrue(grafo.existeArista(c2, c1)); // No dirigido
        
        Double peso = grafo.obtenerPeso(c1, c2);
        assertNotNull(peso);
        assertEquals(0.9, peso);
    }
    
    @Test
    void testObtenerVecinos() {
        grafo.agregarArista(c1, c2, 0.9);
        grafo.agregarArista(c1, c3, 0.5);
        
        var vecinos = grafo.obtenerVecinos(c1);
        assertEquals(2, vecinos.size());
        assertTrue(vecinos.contains(c2));
        assertTrue(vecinos.contains(c3));
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

