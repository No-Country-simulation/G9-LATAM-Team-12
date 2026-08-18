package com.g9latam.team12.backend;

import com.g9latam.team12.backend.dto.AnalisisResponseDTO;
import com.g9latam.team12.backend.dto.ConsumoRequestDTO;
import com.g9latam.team12.backend.model.AnalisisHistorial;
import com.g9latam.team12.backend.model.Rol;
import com.g9latam.team12.backend.model.Usuario;
import com.g9latam.team12.backend.repository.AnalisisHistorialRepository;
import com.g9latam.team12.backend.service.AnalisisHistorialService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AnalisisHistorialServiceTest {

    @Mock
    private AnalisisHistorialRepository historialRepository;

    private final AnalisisHistorialService service = new AnalisisHistorialService(null);

    @Test
    void guardar_persisteElHistorialConLosDatosDelAnalisis() {
        AnalisisHistorialService servicioReal = new AnalisisHistorialService(historialRepository);

        Usuario usuario = new Usuario();
        usuario.setEmail("historial-service@energiai.com");
        usuario.setRol(Rol.USER);

        ConsumoRequestDTO request = new ConsumoRequestDTO(
                450.0, true, 10, "Casa", 8.0, true);

        AnalisisResponseDTO response = new AnalisisResponseDTO(
                "Ineficiente", 0.86, List.of("Recomendación 1"), 315.00);

        servicioReal.guardar(usuario, request, response);

        ArgumentCaptor<AnalisisHistorial> captor = ArgumentCaptor.forClass(AnalisisHistorial.class);
        verify(historialRepository).save(captor.capture());

        AnalisisHistorial guardado = captor.getValue();
        assertThat(guardado.getUsuario()).isEqualTo(usuario);
        assertThat(guardado.getConsumoKwh()).isEqualTo(450);
        assertThat(guardado.getCategoria()).isEqualTo("Ineficiente");
        assertThat(guardado.getProbabilidad()).isEqualTo(0.86);
        assertThat(guardado.getCostoEstimadoMensual()).isEqualByComparingTo("315.00");
        assertThat(guardado.getFecha()).isNotNull();
    }
}



