package com.syncup.repository;

import com.syncup.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad {@link Usuario}.
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

    /**
     * Busca usuarios cuyo nombre o username contengan el texto indicado,
     * sin distinguir entre mayúsculas y minúsculas.
     *
     * Se usa en el buscador de usuarios de la sección Social.
     *
     * @param nombreFragment    fragmento a buscar en el campo {@code nombre}
     * @param usernameFragment  fragmento a buscar en el campo {@code username}
     * @return lista de usuarios que coinciden parcial o totalmente con el criterio
     */
    List<Usuario> findByNombreContainingIgnoreCaseOrUsernameContainingIgnoreCase(
            String nombreFragment,
            String usernameFragment
    );
}
