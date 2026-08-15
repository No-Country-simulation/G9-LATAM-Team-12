package com.g9latam.team12.backend;

import com.g9latam.team12.backend.model.Rol;
import com.g9latam.team12.backend.model.Usuario;
import com.g9latam.team12.backend.repository.UsuarioRepository;
import com.g9latam.team12.backend.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityBeansConfigTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();

        UserDetailsServiceImpl userDetailsService = new UserDetailsServiceImpl(usuarioRepository);

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);

        authenticationManager = new ProviderManager(provider);
    }

    @Test
    void passwordEncoder_hasheaYVerificaCorrectamente() {
        String hash = passwordEncoder.encode("miPassword123");

        assertThat(hash).isNotEqualTo("miPassword123");
        assertThat(passwordEncoder.matches("miPassword123", hash)).isTrue();
        assertThat(passwordEncoder.matches("passwordIncorrecta", hash)).isFalse();
    }

    @Test
    void authenticationManager_autenticaConCredencialesCorrectas() {
        String hashReal = passwordEncoder.encode("miPassword123");
        Usuario usuario = new Usuario();
        usuario.setEmail("test@energiai.com");
        usuario.setPassword(hashReal);
        usuario.setRol(Rol.USER);

        when(usuarioRepository.findByEmail("test@energiai.com"))
                .thenReturn(Optional.of(usuario));

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("test@energiai.com", "miPassword123"));

        assertThat(auth.isAuthenticated()).isTrue();
        assertThat(auth.getName()).isEqualTo("test@energiai.com");
    }

    @Test
    void authenticationManager_rechazaCredencialesIncorrectas() {
        String hashReal = passwordEncoder.encode("miPassword123");
        Usuario usuario = new Usuario();
        usuario.setEmail("test@energiai.com");
        usuario.setPassword(hashReal);
        usuario.setRol(Rol.USER);

        when(usuarioRepository.findByEmail("test@energiai.com"))
                .thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("test@energiai.com", "passwordIncorrecta")))
                .isInstanceOf(BadCredentialsException.class);
    }
}