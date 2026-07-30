document.getElementById('form-consumo').addEventListener('submit', async (e) => {
    e.preventDefault(); // evita que la página se recargue (comportamiento por defecto del form)

    const boton = e.target.querySelector('button[type="submit"]');
    const textoOriginal = boton.textContent;

    // Deshabilitamos el botón y cambiamos el texto mientras esperamos la respuesta
    boton.disabled = true;
    boton.textContent = 'Analizando...';

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
            console.log("Respuesta completa:", data);
        console.log("Costo:", data.costo_estimado_mensual);
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
    } finally { // Restauramos el botón a su estado original
        boton.disabled = false;
        boton.textContent = textoOriginal;
    }
});


let graficoActual = null;   // guardamos referencia para poder destruirlo y evitar duplicados

//  caso feliz, HTTP 200
function mostrarResultado(data) {
    // Convertimos el array de recomendaciones en una lista HTML
    const listaRecomendaciones = data.recomendaciones
        .map(r => `<li>${r}</li>`)
        .join('');

    document.getElementById('resultado').innerHTML = `
        <h3>Categoría: ${data.categoria}</h3>
        <p>Probabilidad: ${(data.probabilidad * 100).toFixed(0)}%</p>
        <p>Costo estimado mensual: R$ ${data.costo_estimado_mensual.toFixed(2)}</p>
        <h4>Recomendaciones:</h4>
        <ul>${listaRecomendaciones}</ul>
    `;

    dibujarGrafico(data);
}

function dibujarGrafico(data) {
    const ctx = document.getElementById('grafico-probabilidad');

    if (graficoActual) {
        graficoActual.destroy();
    }

    const color = obtenerColorPorCategoria(data.categoria);

    graficoActual = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ['Probabilidad', 'Resto'],
            datasets: [{
                data: [data.probabilidad * 100, 100 - data.probabilidad * 100],
                backgroundColor: [color, '#e0e0e0']
            }]
        },
        options: {
            plugins: {
                legend: { display: false }
            }
        }
    });
}

//  Indicador visual tipo semáforo según la categoría
function obtenerColorPorCategoria(categoria) {
    switch (categoria) {
        case 'Eficiente':
            return '#008000';
        case 'Moderado':
            return '#FF7800';
        case 'Ineficiente':
            return '#FF0000';
        default:
            return '#9E9E9E';   // por si llega algo inesperado
    }
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
