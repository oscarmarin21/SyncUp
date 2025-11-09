package com.syncup.service;

import com.syncup.graph.GrafoDeSimilitud;
import com.syncup.model.Cancion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para RecomendacionService.
 * Requerido según RF-031.
 * 
 * @author SyncUp Team
 */
@ExtendWith(MockitoExtension.class)
class RecomendacionServiceTest {
    
    @Mock
    private FavoritosService favoritosService;
    
    @Mock
    private SimilitudService similitudService;
    
    @InjectMocks
    private RecomendacionService recomendacionService;
    
    private Cancion c1, c2, c3;
    private GrafoDeSimilitud grafo;
    
    @BeforeEach
    void setUp() {
        c1 = crearCancion(1L, "Song 1", "Artist A", "Rock", 2020);
        c2 = crearCancion(2L, "Song 2", "Artist A", "Rock", 2021);
        c3 = crearCancion(3L, "Song 3", "Artist B", "Pop", 2020);
        
        grafo = new GrafoDeSimilitud();
        grafo.agregarArista(c1, c2, 0.9);
    }
    
    @Test
    void testGenerarDescubrimientoSemanal() {
        // Configurar favoritos del usuario
        LinkedList<Cancion> favoritos = new LinkedList<>();
        favoritos.add(c1);
        
        when(favoritosService.obtenerFavoritos("testuser")).thenReturn(favoritos);
        when(similitudService.obtenerGrafo()).thenReturn(grafo);
        
        List<Cancion> recomendaciones = recomendacionService.generarDescubrimientoSemanal("testuser", 10);
        
        assertNotNull(recomendaciones);
        // No debe incluir canciones ya en favoritos
        assertFalse(recomendaciones.contains(c1));
    }
    
    @Test
    void testGenerarDescubrimientoSemanalSinFavoritos() {
        when(favoritosService.obtenerFavoritos("testuser")).thenReturn(new LinkedList<>());
        
        List<Cancion> recomendaciones = recomendacionService.generarDescubrimientoSemanal("testuser", 10);
        
        assertTrue(recomendaciones.isEmpty());
    }
    
    @Test
    void testIniciarRadio() {
        when(similitudService.obtenerGrafo()).thenReturn(grafo);
        
        List<Cancion> radio = recomendacionService.iniciarRadio(c1, 10);
        
        assertNotNull(radio);
        assertFalse(radio.isEmpty());
        assertEquals(c1, radio.get(0)); // Primera canción debe ser la semilla
    }
    
    @Test
    void testIniciarRadioConCancionNull() {
        List<Cancion> radio = recomendacionService.iniciarRadio(null, 10);
        
        assertTrue(radio.isEmpty());
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

