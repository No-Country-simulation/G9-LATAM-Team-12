package com.g9latam.team12.backend;

import com.g9latam.team12.backend.model.AnalisisHistorial;
import com.g9latam.team12.backend.model.Rol;
import com.g9latam.team12.backend.model.Usuario;
import com.g9latam.team12.backend.repository.AnalisisHistorialRepository;
import com.g9latam.team12.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AnalisisHistorialRepositoryTest {

    @Autowired
    private AnalisisHistorialRepository historialRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario crearUsuario(String email) {
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setPassword("hashDePrueba");
        usuario.setRol(Rol.USER);
        return usuarioRepository.save(usuario);
    }

    private AnalisisHistorial crearHistorial(Usuario usuario, String categoria, BigDecimal costo) {
        AnalisisHistorial historial = new AnalisisHistorial();
        historial.setUsuario(usuario);
        historial.setConsumoKwh(300);
        historial.setCategoria(categoria);
        historial.setProbabilidad(0.75);
        historial.setCostoEstimadoMensual(costo);
        historial.setFecha(LocalDateTime.now());
        return historial;
    }

    @Test
    void guardarHistorial_persisteConIdGenerado() {
        Usuario usuario = crearUsuario("historial1@energiai.com");
        AnalisisHistorial historial = crearHistorial(usuario, "Moderado", new BigDecimal("225.00"));

        AnalisisHistorial guardado = historialRepository.save(historial);

        assertThat(guardado.getId()).isNotNull();
        assertThat(guardado.getUsuario().getEmail()).isEqualTo("historial1@energiai.com");
    }

    @Test
    void buscarPorUsuario_devuelveSoloLosDeEseUsuario() {
        Usuario usuario1 = crearUsuario("historial2@energiai.com");
        Usuario usuario2 = crearUsuario("historial3@energiai.com");

        historialRepository.save(crearHistorial(usuario1, "Moderado", new BigDecimal("225.00")));
        historialRepository.save(crearHistorial(usuario1, "Ineficiente", new BigDecimal("315.00")));
        historialRepository.save(crearHistorial(usuario2, "Eficiente", new BigDecimal("75.00")));

        List<AnalisisHistorial> resultado =
                historialRepository.findByUsuarioIdOrderByFechaDesc(usuario1.getId());

        assertThat(resultado).hasSize(2);
        assertThat(resultado).allMatch(h -> h.getUsuario().getId().equals(usuario1.getId()));
    }

    @Test
    void buscarPorUsuario_ordenaDelMasRecienteAlMasViejo() {
        Usuario usuario = crearUsuario("historial4@energiai.com");

        AnalisisHistorial viejo = crearHistorial(usuario, "Moderado", new BigDecimal("225.00"));
        viejo.setFecha(LocalDateTime.now().minusDays(2));
        historialRepository.save(viejo);

        AnalisisHistorial reciente = crearHistorial(usuario, "Eficiente", new BigDecimal("75.00"));
        reciente.setFecha(LocalDateTime.now());
        historialRepository.save(reciente);

        List<AnalisisHistorial> resultado =
                historialRepository.findByUsuarioIdOrderByFechaDesc(usuario.getId());

        assertThat(resultado.get(0).getCategoria()).isEqualTo("Eficiente");
        assertThat(resultado.get(1).getCategoria()).isEqualTo("Moderado");
    }
}