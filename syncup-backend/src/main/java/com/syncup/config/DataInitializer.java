package com.syncup.config;

import com.syncup.model.Cancion;
import com.syncup.model.Usuario;
import com.syncup.repository.CancionRepository;
import com.syncup.repository.UsuarioRepository;
import com.syncup.service.FavoritosService;
import com.syncup.service.SocialService;
import com.syncup.service.UsuarioIndexService;
import com.syncup.service.AutocompletadoService;
import com.syncup.service.AudioStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Componente que inicializa datos en el sistema al arranque.
 * Carga usuarios y canciones en las estructuras de datos en memoria.
 * Crea datos de prueba para demostración.
 * 
 * @author SyncUp Team
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final UsuarioRepository usuarioRepository;
    private final CancionRepository cancionRepository;
    private final UsuarioIndexService usuarioIndexService;
    private final PasswordEncoder passwordEncoder;
    private final FavoritosService favoritosService;
    private final SocialService socialService;
    private final AutocompletadoService autocompletadoService;
    private final AudioStorageService audioStorageService;
    
    @Override
    public void run(String... args) throws Exception {
        log.info("Inicializando datos del sistema...");
        
        // ========== CREAR USUARIOS ==========
        Usuario admin = crearObtenerUsuario("admin", "admin123", "Administrador", Usuario.Rol.ADMIN);
        log.debug("Usuario administrador inicializado: {}", admin.getUsername());
        Usuario user1 = crearObtenerUsuario("juan", "password123", "Juan Pérez", Usuario.Rol.USER);
        Usuario user2 = crearObtenerUsuario("maria", "password123", "María García", Usuario.Rol.USER);
        Usuario user3 = crearObtenerUsuario("carlos", "password123", "Carlos López", Usuario.Rol.USER);
        Usuario user4 = crearObtenerUsuario("ana", "password123", "Ana Martínez", Usuario.Rol.USER);
        
        // Verificar y corregir usuarios que puedan tener problemas
        verificarYCorregirUsuarios();
        
        // Recargar usuarios en el índice para asegurar que todos estén sincronizados desde BD
        usuarioIndexService.cargarUsuariosEnMemoria();
        
        // Verificar que todos los usuarios tengan contraseñas después de cargar
        verificarContraseñasUsuarios();
        
        // ========== CREAR CANCIONES ==========
        List<Cancion> canciones = crearCancionesDePrueba();
        log.info("Creadas {} canciones de prueba", canciones.size());
        
        // ========== CONFIGURAR FAVORITOS ==========
        configurarFavoritos(user1, canciones);
        configurarFavoritos(user2, canciones);
        configurarFavoritos(user3, canciones);
        log.info("Favoritos configurados para usuarios de prueba");
        
        // ========== CONFIGURAR RELACIONES SOCIALES ==========
        configurarRelacionesSociales(user1, user2, user3, user4);
        log.info("Relaciones sociales configuradas");

        // Asegurar que todas las canciones tengan audio disponible
        asignarAudioPorDefectoASiEsNecesario();
        eliminarCancionesSinAudioValido();
        autocompletadoService.reconstruirDesdeBD();
        
        log.info("Inicialización de datos completada");
        
        // Log de estadísticas
        List<Usuario> usuarios = usuarioRepository.findAll();
        List<Cancion> todasLasCanciones = cancionRepository.findAll();
        
        log.info("Estadísticas del sistema:");
        log.info("  - Usuarios: {}", usuarios.size());
        log.info("  - Canciones: {}", todasLasCanciones.size());
        log.info("\n=== CREDENCIALES DE PRUEBA ===");
        log.info("Admin: admin / admin123");
        log.info("Usuario 1: juan / password123");
        log.info("Usuario 2: maria / password123");
        log.info("Usuario 3: carlos / password123");
        log.info("Usuario 4: ana / password123");
    }
    
    private Usuario crearObtenerUsuario(String username, String password, String nombre, Usuario.Rol rol) {
        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);
        if (usuario == null) {
            usuario = new Usuario();
            usuario.setUsername(username);
            usuario.setPassword(passwordEncoder.encode(password));
            usuario.setNombre(nombre);
            usuario.setRol(rol);
            usuario = usuarioRepository.save(usuario);
            log.info("Usuario creado: {} / {}", username, password);
        } else {
            // Si el usuario ya existe, SIEMPRE actualizar la contraseña y otros datos
            // para asegurar que están sincronizados
            boolean necesitaActualizar = false;
            
            // Verificar si la contraseña necesita actualizarse (si no está hasheada con BCrypt)
            String currentPassword = usuario.getPassword();
            if (currentPassword == null || currentPassword.isEmpty() || 
                (!currentPassword.startsWith("$2a$") && !currentPassword.startsWith("$2b$"))) {
                usuario.setPassword(passwordEncoder.encode(password));
                necesitaActualizar = true;
                log.info("Contraseña del usuario '{}' actualizada (no estaba hasheada correctamente)", username);
            }
            
            // Actualizar nombre y rol si han cambiado
            if (usuario.getNombre() == null || !usuario.getNombre().equals(nombre)) {
                usuario.setNombre(nombre);
                necesitaActualizar = true;
            }
            if (usuario.getRol() != rol) {
                usuario.setRol(rol);
                necesitaActualizar = true;
            }
            
            // Si necesita actualizar, guardar y refrescar desde BD
            if (necesitaActualizar) {
                usuario = usuarioRepository.save(usuario);
                // Refrescar desde BD para asegurar que tenemos la versión más reciente
                usuario = usuarioRepository.findByUsername(username).orElse(usuario);
                log.info("Usuario '{}' actualizado en BD", username);
            }
        }
        
        // Agregar al índice en memoria (después de asegurar que está actualizado en BD)
        usuarioIndexService.agregarUsuario(usuario);
        return usuario;
    }
    
    private List<Cancion> crearCancionesDePrueba() {
        // Canciones variadas para tener datos de prueba interesantes
        String anotherOneBite = "/audio/Queen – Another One Bites The Dust (Official Lyric Video).wav";
        String bohemian = "/audio/queen--bohemian-rhapsody-official-video-remastered.wav";
        String wewillrockyou = "/audio/Queen - We Will Rock You.wav";
        String smellslike = "/audio/Nirvana - Smells Like Teen Spirit (Lyrics).wav";
        String thriller = "/audio/Michael Jackson - Thriller (Official Video - Shortened Version).wav";
        String billiejean = "/audio/Michael Jackson - Billie Jean (Official Video).wav";
        String beatIt = "/audio/Michael Jackson - Beat It (Official 4K Video).wav";
        String sweet = "/audio/Guns N' Roses - Sweet Child O' Mine (Lyrics).wav";
        String takeiteasy = "/audio/Eagles - Take it Easy (Official Audio).wav";
        String hotelcalifornia = "/audio/Eagles-Hotel California.wav";
        String stairway = "/audio/Led Zeppelin - Stairway To Heaven (Official Audio).wav";
        String likearolling = "/audio/Bob Dylan - Like a Rolling Stone (Official Audio).wav";
        String imagine = "/audio/Imagine - John Lennon & The Plastic Ono Band (w The Flux Fiddlers) (Ultimate Mix 2018) - 4K REMASTER.wav";
        String purpleRain = "/audio/Prince - Purple Rain (Lyrics).wav";
        String superstition = "/audio/Superstition.wav";
        String gimmeshelter = "/audio/The Rolling Stones - Gimme Shelter (Official Lyric Video).wav";
        String adayinthelife = "/audio/The Beatles - A Day In The Life.wav";
        String comfortablynumb = "/audio/Pink Floyd - Comfortably numb.wav";
        String paintItBlack = "/audio/The Rolling Stones - Paint It, Black (Official Lyric Video).wav";
        String freeBird = "/audio/Lynyrd Skynyrd - Free Bird (Official Audio).wav";






        Cancion[] cancionesArray = {
            crearObtenerCancion("Bohemian Rhapsody", "Queen", "Rock", 1975, 355, bohemian),
            crearObtenerCancion("Another One Bites the Dust", "Queen", "Rock", 1980, 216, anotherOneBite),
            crearObtenerCancion("We Will Rock You", "Queen", "Rock", 1977, 122, wewillrockyou),
            crearObtenerCancion("Billie Jean", "Michael Jackson", "Pop", 1983, 294, billiejean),
            crearObtenerCancion("Thriller", "Michael Jackson", "Pop", 1982, 357, thriller),
            crearObtenerCancion("Beat It", "Michael Jackson", "Pop", 1983, 258, beatIt),
            crearObtenerCancion("Hotel California", "Eagles", "Rock", 1977, 391, hotelcalifornia),
            crearObtenerCancion("Take It Easy", "Eagles", "Rock", 1972, 211, takeiteasy),
            crearObtenerCancion("Stairway to Heaven", "Led Zeppelin", "Rock", 1971, 482, stairway),
            crearObtenerCancion("Smells Like Teen Spirit", "Nirvana", "Grunge", 1991, 301, smellslike),
            crearObtenerCancion("Sweet Child O' Mine", "Guns N' Roses", "Rock", 1988, 356, sweet),
            crearObtenerCancion("Like a Rolling Stone", "Bob Dylan", "Folk Rock", 1965, 366, likearolling),
            crearObtenerCancion("Imagine", "John Lennon", "Rock", 1971, 183, imagine),
            crearObtenerCancion("Purple Rain", "Prince", "Pop Rock", 1984, 537, purpleRain),
            crearObtenerCancion("Superstition", "Stevie Wonder", "Funk", 1972, 267, superstition),
            crearObtenerCancion("Gimme Shelter", "The Rolling Stones", "Rock", 1969, 271, gimmeshelter),
            crearObtenerCancion("A Day in the Life", "The Beatles", "Rock", 1967, 335, adayinthelife),
            crearObtenerCancion("Comfortably Numb", "Pink Floyd", "Progressive Rock", 1979, 384, comfortablynumb),
            crearObtenerCancion("Paint It Black", "The Rolling Stones", "Rock", 1966, 203, paintItBlack),
            crearObtenerCancion("Free Bird", "Lynyrd Skynyrd", "Southern Rock", 1974, 554, freeBird)
        };
        
        return List.of(cancionesArray);
    }
    
    private Cancion crearObtenerCancion(String titulo, String artista, String genero, int año, int duracion, String audioUrl) {
        java.util.Optional<Cancion> existente = cancionRepository.findByTituloAndArtista(titulo, artista);
        if (existente.isPresent()) {
            Cancion cancionExistente = existente.get();
            boolean requiereActualizacion = false;
            if (cancionExistente.getDuracion() == null || !cancionExistente.getDuracion().equals(duracion)) {
                cancionExistente.setDuracion(duracion);
                requiereActualizacion = true;
            }
            if (audioUrl != null && (cancionExistente.getAudioUrl() == null || !cancionExistente.getAudioUrl().equals(audioUrl))) {
                cancionExistente.setAudioUrl(audioUrl);
                requiereActualizacion = true;
            }
            if (requiereActualizacion) {
                cancionExistente = cancionRepository.save(cancionExistente);
            }
            return cancionExistente;
        }
        
        Cancion cancion = new Cancion();
        cancion.setTitulo(titulo);
        cancion.setArtista(artista);
        cancion.setGenero(genero);
        cancion.setAño(año);
        cancion.setDuracion(duracion);
        cancion.setAudioUrl(audioUrl);
        return cancionRepository.save(cancion);
    }

    private void asignarAudioPorDefectoASiEsNecesario() {
        List<Cancion> canciones = cancionRepository.findAll();
        String[] tracks = {"/audio/syncup_intro.wav", "/audio/syncup_groove.wav", "/audio/syncup_chill.wav"};
        int asignadas = 0;

        for (int i = 0; i < canciones.size(); i++) {
            Cancion cancion = canciones.get(i);
            if (cancion.getAudioUrl() == null || cancion.getAudioUrl().isBlank() || !audioStorageService.exists(cancion.getAudioUrl())) {
                String track = tracks[i % tracks.length];
                if (audioStorageService.exists(track)) {
                    cancion.setAudioUrl(track);
                    cancionRepository.save(cancion);
                    asignadas++;
                }
            }
        }

        if (asignadas > 0) {
            log.info("Asignados audios de ejemplo a {} canciones que no tenían URL de audio", asignadas);
        }
    }

    private void eliminarCancionesSinAudioValido() {
        List<Cancion> canciones = cancionRepository.findAll();
        int eliminadas = 0;

        for (Cancion cancion : canciones) {
            if (cancion.getAudioUrl() == null || cancion.getAudioUrl().isBlank() || !audioStorageService.exists(cancion.getAudioUrl())) {
                log.warn("Eliminando canción '{}' (ID: {}) por no tener audio disponible", cancion.getTitulo(), cancion.getId());
                cancionRepository.delete(cancion);
                eliminadas++;
            }
        }

        if (eliminadas > 0) {
            log.info("Se eliminaron {} canciones sin audio válido", eliminadas);
        }
    }

    private void configurarFavoritos(Usuario usuario, List<Cancion> canciones) {
        // Cada usuario tiene algunos favoritos aleatorios
        if (usuario.getUsername().equals("juan")) {
            // Juan le gusta el rock clásico
            favoritosService.agregarFavorito(usuario.getUsername(), canciones.get(0)); // Bohemian Rhapsody
            favoritosService.agregarFavorito(usuario.getUsername(), canciones.get(6)); // Hotel California
            favoritosService.agregarFavorito(usuario.getUsername(), canciones.get(8)); // Stairway to Heaven
            favoritosService.agregarFavorito(usuario.getUsername(), canciones.get(11)); // Sweet Child O' Mine
        } else if (usuario.getUsername().equals("maria")) {
            // María le gusta el pop
            favoritosService.agregarFavorito(usuario.getUsername(), canciones.get(3)); // Billie Jean
            favoritosService.agregarFavorito(usuario.getUsername(), canciones.get(4)); // Thriller
            favoritosService.agregarFavorito(usuario.getUsername(), canciones.get(5)); // Beat It
            favoritosService.agregarFavorito(usuario.getUsername(), canciones.get(13)); // Purple Rain
        } else if (usuario.getUsername().equals("carlos")) {
            // Carlos tiene gustos variados
            favoritosService.agregarFavorito(usuario.getUsername(), canciones.get(9)); // Smells Like Teen Spirit
            favoritosService.agregarFavorito(usuario.getUsername(), canciones.get(17)); // Comfortably Numb
            favoritosService.agregarFavorito(usuario.getUsername(), canciones.get(19)); // Free Bird
        }
    }
    
    private void configurarRelacionesSociales(Usuario user1, Usuario user2, Usuario user3, Usuario user4) {
        // Crear una red social:
        // user1 sigue a user2 y user3
        // user2 sigue a user3 y user4
        // user3 sigue a user4
        
        socialService.seguirUsuario(user1.getUsername(), user2.getUsername());
        socialService.seguirUsuario(user1.getUsername(), user3.getUsername());
        socialService.seguirUsuario(user2.getUsername(), user3.getUsername());
        socialService.seguirUsuario(user2.getUsername(), user4.getUsername());
        socialService.seguirUsuario(user3.getUsername(), user4.getUsername());
        
        log.info("Red social creada:");
        log.info("  {} sigue a {} y {}", user1.getUsername(), user2.getUsername(), user3.getUsername());
        log.info("  {} sigue a {} y {}", user2.getUsername(), user3.getUsername(), user4.getUsername());
        log.info("  {} sigue a {}", user3.getUsername(), user4.getUsername());
    }
    
    /**
     * Verifica y corrige usuarios que puedan tener problemas con contraseñas.
     * Esto asegura que todos los usuarios existentes tengan contraseñas válidas.
     */
    private void verificarYCorregirUsuarios() {
        log.info("Verificando y corrigiendo usuarios existentes...");
        List<Usuario> todosLosUsuarios = usuarioRepository.findAll();
        int usuariosCorregidos = 0;
        
        for (Usuario usuario : todosLosUsuarios) {
            boolean necesitaCorreccion = false;
            String username = usuario.getUsername();
            
            // Verificar contraseña - SIEMPRE corregir si está mal
            String password = usuario.getPassword();
            if (password == null || password.isEmpty() || 
                (!password.startsWith("$2a$") && !password.startsWith("$2b$"))) {
                // Contraseña inválida o no hasheada - usar la contraseña por defecto según username
                String defaultPassword = username.equals("admin") ? "admin123" : "password123";
                usuario.setPassword(passwordEncoder.encode(defaultPassword));
                necesitaCorreccion = true;
                log.warn("⚠️  Contraseña del usuario '{}' corregida a: {}", username, defaultPassword);
            }
            
            // Verificar nombre
            if (usuario.getNombre() == null || usuario.getNombre().isEmpty()) {
                usuario.setNombre(username);
                necesitaCorreccion = true;
                log.warn("Nombre del usuario '{}' corregido", username);
            }
            
            // Verificar rol
            if (usuario.getRol() == null) {
                usuario.setRol(username.equals("admin") ? Usuario.Rol.ADMIN : Usuario.Rol.USER);
                necesitaCorreccion = true;
                log.warn("Rol del usuario '{}' corregido", username);
            }
            
            if (necesitaCorreccion) {
                usuario = usuarioRepository.save(usuario);
                // Refrescar desde BD para asegurar que se guardó correctamente
                usuario = usuarioRepository.findByUsername(username).orElse(usuario);
                usuariosCorregidos++;
                log.info("✅ Usuario '{}' guardado y refrescado desde BD", username);
            } else {
                // Verificar que la contraseña se puede leer correctamente
                Usuario usuarioVerificado = usuarioRepository.findByUsername(username).orElse(null);
                if (usuarioVerificado != null && 
                    (usuarioVerificado.getPassword() == null || usuarioVerificado.getPassword().isEmpty())) {
                    log.error("❌ Usuario '{}' en BD SIN contraseña después de verificación!", username);
                    // Forzar corrección
                    String defaultPassword = username.equals("admin") ? "admin123" : "password123";
                    usuarioVerificado.setPassword(passwordEncoder.encode(defaultPassword));
                    usuarioRepository.save(usuarioVerificado);
                    usuariosCorregidos++;
                    log.warn("✅ Usuario '{}' forzado a corregir contraseña", username);
                }
            }
        }
        
        if (usuariosCorregidos > 0) {
            log.info("✅ {} usuarios corregidos en total", usuariosCorregidos);
        } else {
            log.info("✅ Todos los usuarios están correctos");
        }
    }
    
    /**
     * Verifica que todos los usuarios en memoria tengan contraseñas válidas.
     */
    private void verificarContraseñasUsuarios() {
        log.info("Verificando contraseñas de usuarios en memoria...");
        int usuariosSinPassword = 0;
        
        for (Usuario usuario : usuarioIndexService.getUsuariosMap().values()) {
            if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
                usuariosSinPassword++;
                log.error("Usuario '{}' NO tiene contraseña en memoria después de cargar!", usuario.getUsername());
                
                // Intentar cargarlo directamente de BD
                Usuario usuarioDesdeBD = usuarioRepository.findByUsername(usuario.getUsername()).orElse(null);
                if (usuarioDesdeBD != null && usuarioDesdeBD.getPassword() != null && !usuarioDesdeBD.getPassword().isEmpty()) {
                    usuarioIndexService.agregarUsuario(usuarioDesdeBD);
                    log.info("Usuario '{}' recargado desde BD con contraseña", usuario.getUsername());
                }
            }
        }
        
        if (usuariosSinPassword == 0) {
            log.info("Todos los usuarios tienen contraseñas válidas");
        } else {
            log.warn("{} usuarios sin contraseña en memoria", usuariosSinPassword);
        }
    }
}

