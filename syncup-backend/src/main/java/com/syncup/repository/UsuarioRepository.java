package com.syncup.repository;

import com.syncup.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad Usuario.
 * Proporciona acceso a los datos de usuarios almacenados en la base de datos.
 * 
 * @author SyncUp Team
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    /**
     * Busca un usuario por su nombre de usuario.
     * 
     * @param username nombre de usuario a buscar
     * @return Optional con el usuario encontrado o vacío si no existe
     * Complejidad: O(1) con índice en la base de datos
     */
    Optional<Usuario> findByUsername(String username);
    
    /**
     * Verifica si existe un usuario con el nombre de usuario dado.
     * 
     * @param username nombre de usuario a verificar
     * @return true si existe, false en caso contrario
     * Complejidad: O(1) con índice en la base de datos
     */
    boolean existsByUsername(String username);
}

