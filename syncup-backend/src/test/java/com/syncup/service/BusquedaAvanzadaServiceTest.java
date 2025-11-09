package com.syncup.service;

import com.syncup.dto.SearchRequest;
import com.syncup.model.Cancion;
import com.syncup.repository.CancionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios para BusquedaAvanzadaService.
 * Requerido según RF-031.
 * 
 * @author SyncUp Team
 */
@ExtendWith(MockitoExtension.class)
class BusquedaAvanzadaServiceTest {
    
    @Mock
    private CancionRepository cancionRepository;
    
    @InjectMocks
    private BusquedaAvanzadaService busquedaAvanzadaService;
    
    private Cancion c1, c2, c3;
    
    @BeforeEach
    void setUp() {
        c1 = crearCancion(1L, "Song 1", "Artist A", "Rock", 2020);
        c2 = crearCancion(2L, "Song 2", "Artist A", "Pop", 2021);
        c3 = crearCancion(3L, "Song 3", "Artist B", "Rock", 2020);
    }
    
    @Test
    void testBuscarConOperadorAND() {
        // Configurar mocks
        when(cancionRepository.findByArtista("Artist A")).thenReturn(Arrays.asList(c1, c2));
        when(cancionRepository.findByGenero("Rock")).thenReturn(Arrays.asList(c1, c3));
        when(cancionRepository.findByAño(2020)).thenReturn(Arrays.asList(c1, c3));
        
        SearchRequest request = new SearchRequest();
        request.setArtista("Artist A");
        request.setGenero("Rock");
        request.setAño(2020);
        request.setOperador("AND");
        
        List<Cancion> resultados = busquedaAvanzadaService.buscar(request);
        
        // Con AND, solo c1 cumple todos los criterios
        assertEquals(1, resultados.size());
        assertTrue(resultados.contains(c1));
    }
    
    @Test
    void testBuscarConOperadorOR() {
        // Configurar mocks
        when(cancionRepository.findByArtista("Artist A")).thenReturn(Arrays.asList(c1, c2));
        when(cancionRepository.findByGenero("Pop")).thenReturn(Arrays.asList(c2));
        
        SearchRequest request = new SearchRequest();
        request.setArtista("Artist A");
        request.setGenero("Pop");
        request.setOperador("OR");
        
        List<Cancion> resultados = busquedaAvanzadaService.buscar(request);
        
        // Con OR, debe incluir todas las canciones que cumplan al menos un criterio
        assertTrue(resultados.size() >= 1);
        assertTrue(resultados.contains(c1) || resultados.contains(c2));
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

