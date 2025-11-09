package com.syncup.util;

import com.syncup.model.Cancion;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para CsvExporter.
 * Requerido según RF-031.
 * 
 * @author SyncUp Team
 */
class CsvExporterTest {
    
    private CsvExporter exporter;
    
    public CsvExporterTest() {
        exporter = new CsvExporter();
    }
    
    @Test
    void testExportarFavoritos() {
        List<Cancion> favoritos = new ArrayList<>();
        
        Cancion c1 = new Cancion();
        c1.setId(1L);
        c1.setTitulo("Bohemian Rhapsody");
        c1.setArtista("Queen");
        c1.setGenero("Rock");
        c1.setAño(1975);
        c1.setDuracion(355);
        
        Cancion c2 = new Cancion();
        c2.setId(2L);
        c2.setTitulo("Another One Bites the Dust");
        c2.setArtista("Queen");
        c2.setGenero("Rock");
        c2.setAño(1980);
        c2.setDuracion(216);
        
        favoritos.add(c1);
        favoritos.add(c2);
        
        byte[] csv = exporter.exportarFavoritos(favoritos);
        
        assertNotNull(csv);
        assertTrue(csv.length > 0);
        
        String csvString = new String(csv);
        
        // Verificar encabezados
        assertTrue(csvString.contains("ID"));
        assertTrue(csvString.contains("Título"));
        assertTrue(csvString.contains("Artista"));
        
        // Verificar datos
        assertTrue(csvString.contains("Bohemian Rhapsody"));
        assertTrue(csvString.contains("Queen"));
        assertTrue(csvString.contains("1975"));
    }
    
    @Test
    void testExportarFavoritosVacio() {
        List<Cancion> favoritosVacios = new ArrayList<>();
        
        byte[] csv = exporter.exportarFavoritos(favoritosVacios);
        
        assertNotNull(csv);
        String csvString = new String(csv);
        assertTrue(csvString.contains("ID,Título,Artista"));
    }
    
    @Test
    void testExportarFavoritosNull() {
        byte[] csv = exporter.exportarFavoritos(null);
        
        assertNotNull(csv);
        assertTrue(csv.length > 0);
    }
}

