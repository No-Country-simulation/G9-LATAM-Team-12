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
            mostrarErrores(data);
            return;
        }

        mostrarResultado(data);

    } catch (error) {   // Este catch atrapa errores de red
        console.error('Error de conexión:', error);
        mostrarErrorConexion();
    }
});

//  caso feliz, HTTP 200
function mostrarResultado(data) {
    document.getElementById('resultado').innerHTML = `
        <h3>Categoría: ${data.categoria}</h3>
        <p>Probabilidad: ${(data.probabilidad * 100).toFixed(0)}%</p>
    `;
}

//  HTTP 400 con el formato que arma TratadorDeErrores en el backend
function mostrarErrores(data) {
    const contenedor = document.getElementById('resultado');

    if (!data || !data.detalles) {
        contenedor.innerHTML = `<p class="error">Ocurrió un error inesperado. Revisá los datos ingresados.</p>`;
        return;
    }

    const listaErrores = data.detalles
        .map(d => `<li>${d.campo}: ${d.mensaje}</li>`)
        .join('');

    contenedor.innerHTML = `
        <p class="error">${data.error}</p>
        <ul class="error-lista">${listaErrores}</ul>
    `;
}

//  Muestra un mensaje cuando falla la conexión misma
function mostrarErrorConexion() {
    document.getElementById('resultado').innerHTML = `
        <p class="error">No se pudo conectar con el servidor. Verificá que el backend esté corriendo.</p>
    `;
}
