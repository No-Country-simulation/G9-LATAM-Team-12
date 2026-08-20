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

    try {
        // Construir los headers con el Token si existe ---
        const token = localStorage.getItem(TOKEN_KEY); // Usamos la constante definida abajo
        const headers = { 'Content-Type': 'application/json' };
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const response = await fetch(`${URL_BACKEND}/analisis-energetico`, {
            method: 'POST',
            headers: headers,
            body: JSON.stringify(datos)
        });

        //Validar si no tiene permisos (Token expirado o no enviado) ---
        if (response.status === 403 || response.status === 401) {
            mostrarErrores({ detalles: ["Debes iniciar sesión para realizar un análisis"] });
            abrirModal('modal-login'); // Le abrimos el modal automáticamente
            return;
        }


        let data;
        try {   // Intentamos leer el body de la respuesta como JSON.
            data = await response.json();
            console.log("Respuesta completa:", data);
            console.log("Costo:", data.costo_estimado_mensual);
        } catch {   // Si falla, dejamos "data" en null en vez de que el programa se rompa.
            data = null;
        }

        if (!response.ok || !data) { // evitamos que mostrarResultado() reciba algo inválido.
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
        <h3 class="title is-5">Categoría: ${data.categoria}</h3>
        <p>Probabilidad: ${(data.probabilidad * 100).toFixed(0)}%</p>
        <p>Costo estimado mensual: R$ ${data.costo_estimado_mensual.toFixed(2)}</p>
        <h4 class="title is-6 mt-3">Recomendaciones:</h4>
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
        .map(d => {
            if (d.campo && d.mensaje) {
                return `<li>${d.campo}: ${d.mensaje}</li>`;
            }
            return `<li>${d}</li>`;
        })
        .join('');

    contenedor.innerHTML = `<ul class="error-lista">${listaErrores}</ul>`;
}

//  Muestra un mensaje cuando falla la conexión misma
function mostrarErrorConexion() {
    document.getElementById('resultado').innerHTML = `
        <p class="error">No se pudo conectar con el servidor. Verificá que el backend esté corriendo.</p>
    `;
}


/* ===================== Navbar burger (menú mobile) ===================== */
document.addEventListener('DOMContentLoaded', () => {
    // Seleccionamos todos los botones hamburguesa
    const burgers = document.querySelectorAll('.navbar-burger');

    burgers.forEach(burger => {
        burger.addEventListener('click', () => {
            // Buscamos el menú al que hace referencia el data-target ("navbar-menu")
            const targetId = burger.dataset.target;
            const target = document.getElementById(targetId);

            // Alternamos la clase 'is-active' para mostrar/ocultar
            burger.classList.toggle('is-active');
            if (target) {
                target.classList.toggle('is-active');
            }
        });
    });
});


/* ===================== Modales (histórico / usuarios / csv) ===================== */
function abrirModal(id) {
    document.getElementById(id)?.classList.add('is-active');
}
function cerrarModal(modal) {
    modal.classList.remove('is-active');
}

document.querySelectorAll('[data-target]').forEach(disparador => {
    // el navbar-burger también usa data-target, pero ya tiene su propio listener arriba
    if (disparador.classList.contains('navbar-burger')) return;

    disparador.addEventListener('click', (e) => {
        e.preventDefault();
        abrirModal(disparador.dataset.target);
    });
});

document.querySelectorAll('.modal').forEach(modal => {
    const cerrar = () => cerrarModal(modal);
    modal.querySelector('.modal-background')?.addEventListener('click', cerrar);
    modal.querySelector('.delete')?.addEventListener('click', cerrar);
    modal.querySelector('.modal-card-foot .button:not(.is-primary)')?.addEventListener('click', cerrar);
});

document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
        document.querySelectorAll('.modal.is-active').forEach(cerrarModal);
    }
});


/* ===================== Selector de archivo CSV ===================== */
const inputCsv = document.getElementById('input-csv');
const nombreCsv = document.getElementById('nombre-csv');
const btnConfirmarCsv = document.getElementById('btn-confirmar-csv');

inputCsv?.addEventListener('change', () => {
    const archivo = inputCsv.files[0];
    nombreCsv.textContent = archivo ? archivo.name : 'Ningún archivo seleccionado';
    btnConfirmarCsv.disabled = !archivo;
});

btnConfirmarCsv?.addEventListener('click', () => {
    // TODO: conectar con el endpoint del backend cuando esté definido
    // (por ejemplo, POST /analisis-energetico/importar con FormData)
    console.log('Archivo listo para subir:', inputCsv.files[0]);
});


/* ===================== Modo oscuro ===================== */
const CLAVE_TEMA = 'energiai-tema';
const botonTema = document.getElementById('toggle-tema');
const iconoTema = document.getElementById('icono-tema');

function aplicarTema(tema) {
    document.documentElement.setAttribute('data-theme', tema);
    iconoTema.textContent = tema === 'dark' ? '☀️' : '🌙';
    localStorage.setItem(CLAVE_TEMA, tema);
}

// Preferencia guardada > preferencia del sistema > claro por defecto
const temaGuardado = localStorage.getItem(CLAVE_TEMA);
const prefiereOscuro = window.matchMedia('(prefers-color-scheme: dark)').matches;
aplicarTema(temaGuardado || (prefiereOscuro ? 'dark' : 'light'));

botonTema?.addEventListener('click', () => {
    const temaActual = document.documentElement.getAttribute('data-theme');
    aplicarTema(temaActual === 'dark' ? 'light' : 'dark');
});

/* ===================== Lógica de Autenticación (JWT) ===================== */
const URL_BACKEND = `http://${window.location.hostname}:8080`;const TOKEN_KEY = 'energiaI_token';
const EMAIL_KEY = 'energiaI_email';

function verificarAuthUI() {
    const token = localStorage.getItem(TOKEN_KEY);
    const email = localStorage.getItem(EMAIL_KEY);

    const menuInvitado = document.getElementById('menu-invitado');
    const itemsUsuario = document.querySelectorAll('.item-usuario'); // Selecciona todos los enlaces del usuario

    if (token) {
        // Ocultamos el menú de Login/Registro
        if (menuInvitado) menuInvitado.classList.add('is-hidden');

        // Mostramos todas las opciones del usuario
        itemsUsuario.forEach(item => item.classList.remove('is-hidden'));

        // Colocamos el email en la barra
        const displayEmail = document.getElementById('user-email-display');
        if (displayEmail) displayEmail.textContent = email;
    } else {
        // Mostramos el menú de Login/Registro
        if (menuInvitado) menuInvitado.classList.remove('is-hidden');

        // Ocultamos todas las opciones del usuario
        itemsUsuario.forEach(item => item.classList.add('is-hidden'));
    }
}
// Ejecutar al cargar la página
document.addEventListener('DOMContentLoaded', verificarAuthUI);

// Manejar Registro
document.getElementById('form-registro')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = document.getElementById('reg-email').value;
    const password = document.getElementById('reg-password').value;
    const errorMsj = document.getElementById('error-registro');
    errorMsj.textContent = '';

    try {
        const response = await fetch(`${URL_BACKEND}/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });

        if (response.ok) {
            alert('Cuenta creada con éxito. Ahora puedes iniciar sesión.');
            cerrarModal(document.getElementById('modal-registro'));
            abrirModal('modal-login');
        } else {
            const data = await response.json();
            // Verificamos si el backend envió una lista de errores de validación (detalles)
            if (data.detalles && data.detalles.length > 0) {
                // Extraemos los mensajes (ej: "La contraseña debe tener mínimo 8 caracteres")
                errorMsj.innerHTML = data.detalles.map(d => d.mensaje || d).join('<br>');
            } else {
                errorMsj.textContent = data.mensaje || 'Error al registrar el usuario';
            }
        }
    } catch (err) {
        errorMsj.textContent = 'Error de conexión con el servidor';
    }
});

// Manejar Login
document.getElementById('form-login')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = document.getElementById('login-email').value;
    const password = document.getElementById('login-password').value;
    const errorMsj = document.getElementById('error-login');
    errorMsj.textContent = '';

    try {
        const response = await fetch(`${URL_BACKEND}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });

        if (response.ok) {
            const data = await response.json();
            // Guardamos el token y el email en localStorage
            localStorage.setItem(TOKEN_KEY, data.token);
            localStorage.setItem(EMAIL_KEY, data.email);

            cerrarModal(document.getElementById('modal-login'));
            verificarAuthUI();
        } else {
            errorMsj.textContent = 'Credenciales inválidas';
        }
    } catch (err) {
        errorMsj.textContent = 'Error de conexión con el servidor';
    }
});

// Manejar Cierre de sesión (Logout)
document.getElementById('btn-logout')?.addEventListener('click', () => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(EMAIL_KEY);
    verificarAuthUI();
    // Limpiamos los resultados de análisis si cerramos sesión
    document.getElementById('resultado').innerHTML = '';
    if(graficoActual) graficoActual.destroy();
});