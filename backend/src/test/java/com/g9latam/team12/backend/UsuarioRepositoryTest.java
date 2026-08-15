package com.g9latam.team12.backend;
import com.g9latam.team12.backend.model.Rol;
import com.g9latam.team12.backend.model.Usuario;
import com.g9latam.team12.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void guardarUsuario_persisteConIdGenerado() {
        Usuario usuario = new Usuario();
        usuario.setEmail("test@energiai.com");
        usuario.setPassword("hashDePrueba");
        usuario.setRol(Rol.USER);

        Usuario guardado = usuarioRepository.save(usuario);

        assertThat(guardado.getId()).isNotNull();
        assertThat(guardado.getEmail()).isEqualTo("test@energiai.com");
    }

    @Test
    void buscarPorEmail_devuelveUsuarioExistente() {
        Usuario usuario = new Usuario();
        usuario.setEmail("busqueda@energiai.com");
        usuario.setPassword("hashDePrueba");
        usuario.setRol(Rol.USER);
        usuarioRepository.save(usuario);

        Optional<Usuario> encontrado = usuarioRepository.findByEmail("busqueda@energiai.com");

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getEmail()).isEqualTo("busqueda@energiai.com");
    }

    @Test
    void buscarPorEmail_devuelveVacioSiNoExiste() {
        Optional<Usuario> encontrado = usuarioRepository.findByEmail("noexiste@energiai.com");

        assertThat(encontrado).isEmpty();
    }

    @Test
    void emailDuplicado_lanzaExcepcion() {
        Usuario primero = new Usuario();
        primero.setEmail("duplicado@energiai.com");
        primero.setPassword("hash1");
        primero.setRol(Rol.USER);
        usuarioRepository.saveAndFlush(primero);

        Usuario segundo = new Usuario();
        segundo.setEmail("duplicado@energiai.com");
        segundo.setPassword("hash2");
        segundo.setRol(Rol.USER);

        assertThatThrownBy(() -> usuarioRepository.saveAndFlush(segundo))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}