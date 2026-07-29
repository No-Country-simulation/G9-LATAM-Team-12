document.getElementById('form-consumo').addEventListener('submit', async (e) => {
    e.preventDefault(); // evita que la página se recargue (comportamiento por defecto del form)

    const datos = {
        consumo_kwh: Number(document.getElementById('consumoKwh').value),
        uso_horario_pico: document.getElementById('usoHorarioPico').checked,
        cantidad_equipos: Number(document.getElementById('cantidadEquipos').value),
        tipo_inmueble: document.getElementById('tipoInmueble').value,
        horas_alto_consumo: Number(document.getElementById('horasAltoConsumo').value)
    };

    try {   // Hacemos la petición POST al backend.
        const response = await fetch('http://localhost:8080/analisis-energetico', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(datos)
        });

        let data;
        try {   // Intentamos leer el body de la respuesta como JSON.
            data = await response.json();
        } catch {   // Si falla, dejamos "data" en null en vez de que el programa se rompa.
            data = null;
        }

        if (!response.ok|| !data) { // evitamos que mostrarResultado() reciba algo inválido.
            console.error('Error de validación o respuesta inválida:', data);
            return;
        }

        mostrarResultado(data);

    } catch (error) {   // Este catch atrapa errores de red
        console.error('Error de conexión:', error);
    }
});

function mostrarResultado(data) {
    document.getElementById('resultado').innerHTML = `
        <h3>Categoría: ${data.categoria}</h3>
        <p>Probabilidad: ${(data.probabilidad * 100).toFixed(0)}%</p>
    `;
}