package com.g9latam.team12.backend;

import com.g9latam.team12.backend.model.Rol;
import com.g9latam.team12.backend.model.Usuario;
import com.g9latam.team12.backend.repository.UsuarioRepository;
import com.g9latam.team12.backend.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsername_devuelveUserDetailsSiExiste() {
        Usuario usuario = new Usuario();
        usuario.setEmail("test@energiai.com");
        usuario.setPassword("hashDePrueba");
        usuario.setRol(Rol.USER);

        when(usuarioRepository.findByEmail("test@energiai.com"))
                .thenReturn(Optional.of(usuario));

        UserDetails result = userDetailsService.loadUserByUsername("test@energiai.com");

        assertThat(result.getUsername()).isEqualTo("test@energiai.com");
        assertThat(result.getPassword()).isEqualTo("hashDePrueba");
        assertThat(result.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    void loadUserByUsername_lanzaExcepcionSiNoExiste() {
        when(usuarioRepository.findByEmail("noexiste@energiai.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userDetailsService.loadUserByUsername("noexiste@energiai.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}