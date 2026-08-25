const URL_BACKEND = `${window.location.origin}/api`;
const TOKEN_KEY = 'energiaI_token';
const EMAIL_KEY = 'energiaI_email';

let histChart = null;
let ultimoAnalisis = null;
let tarifa = 0.75;
let mode = 'hogar';

// --- AUTH LOGIC ---
function verificarAuthUI() {
    const token = localStorage.getItem(TOKEN_KEY);
    const email = localStorage.getItem(EMAIL_KEY);

    const menuInvitado = document.getElementById('menu-invitado');
    const itemsUsuario = document.querySelectorAll('.item-usuario');

    if (token) {
        if (menuInvitado) menuInvitado.classList.add('is-hidden');
        itemsUsuario.forEach(item => item.classList.remove('is-hidden'));
        const displayEmail = document.getElementById('user-email-display');
        if (displayEmail) displayEmail.textContent = email;
    } else {
        if (menuInvitado) menuInvitado.classList.remove('is-hidden');
        itemsUsuario.forEach(item => item.classList.add('is-hidden'));
    }
}
document.addEventListener('DOMContentLoaded', verificarAuthUI);

document.getElementById('form-registro')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = document.getElementById('reg-email').value;
    const password = document.getElementById('reg-password').value;
    const errorMsj = document.getElementById('error-registro');
    errorMsj.innerHTML = '';

    const btnSubmit = e.target.querySelector('button[type="submit"]');
    const textoOriginal = btnSubmit.textContent;
    btnSubmit.classList.add('is-loading');
    btnSubmit.disabled = true;

    // Validación proactiva y verborrágica de la contraseña
    const erroresPassword = [];
    if (password.length < 8) erroresPassword.push("Tener al menos 8 caracteres de longitud.");
    if (!/[A-Z]/.test(password)) erroresPassword.push("Incluir al menos una letra mayúscula (A-Z).");
    if (!/[0-9]/.test(password)) erroresPassword.push("Contener al menos un número (0-9).");
    if (!/[!@#$%^&*(),.?":{}|<>]/.test(password)) erroresPassword.push("Incluir al menos un carácter especial (ej. @, #, $, !).");

    if (erroresPassword.length > 0) {
        errorMsj.innerHTML = `<strong>Tu contraseña no es segura. Le falta:</strong><br>• ${erroresPassword.join('<br>• ')}`;
        btnSubmit.classList.remove('is-loading');
        btnSubmit.disabled = false;
        btnSubmit.textContent = textoOriginal;
        return;
    }

    try {
        const response = await fetch(`${URL_BACKEND}/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });

        if (response.ok) {
            alert('¡Excelente! Tu cuenta ha sido creada con éxito. Ahora puedes iniciar sesión con tus nuevas credenciales.');
            cerrarModal(document.getElementById('modal-registro'));
            abrirModal('modal-login');
        } else {
            let data = {};
            try { data = await response.json(); } catch(e) {}

            if (response.status === 409 || response.status === 400) {
                errorMsj.innerHTML = `<strong>No se pudo crear la cuenta:</strong><br>Parece que este correo (${email}) ya está registrado en nuestro sistema o los datos no son válidos. Intenta iniciar sesión o usa otro correo electrónico.`;
            } else if (data.detalles && data.detalles.length > 0) {
                errorMsj.innerHTML = `<strong>El sistema rechazó los datos por estos motivos:</strong><br>• ` + data.detalles.map(d => d.mensaje || d).join('<br>• ');
            } else {
                errorMsj.innerHTML = `<strong>Ocurrió un problema inesperado:</strong><br>${data.mensaje || 'Hubo un error del lado del servidor al intentar registrarte (Código ' + response.status + '). Inténtalo nuevamente en unos minutos.'}`;
            }
        }
    } catch (err) {
        errorMsj.innerHTML = `<strong>Problema de conexión:</strong><br>No pudimos establecer comunicación con los servidores de EnergiAI. Por favor, verifica que estés conectado a internet o inténtalo más tarde por si el sistema está en mantenimiento.`;
    } finally {
        btnSubmit.classList.remove('is-loading');
        btnSubmit.disabled = false;
        btnSubmit.textContent = textoOriginal;
    }
});

document.getElementById('form-login')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = document.getElementById('login-email').value;
    const password = document.getElementById('login-password').value;
    const errorMsj = document.getElementById('error-login');
    errorMsj.innerHTML = '';

    const btnSubmit = e.target.querySelector('button[type="submit"]');
    const textoOriginal = btnSubmit.textContent;
    btnSubmit.classList.add('is-loading');
    btnSubmit.disabled = true;

    try {
        const response = await fetch(`${URL_BACKEND}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });

        if (response.ok) {
            const data = await response.json();
            localStorage.setItem(TOKEN_KEY, data.token);
            localStorage.setItem(EMAIL_KEY, data.email);
            cerrarModal(document.getElementById('modal-login'));
            verificarAuthUI();
        } else {
            if (response.status === 401 || response.status === 403) {
                errorMsj.innerHTML = `<strong>Acceso denegado:</strong><br>El correo o la contraseña que ingresaste son incorrectos. Verifica que no tengas activadas las mayúsculas (Bloq Mayús) y vuelve a intentarlo.`;
            } else {
                errorMsj.innerHTML = `<strong>Error inesperado del servidor (Código ${response.status}):</strong><br>Ocurrió un fallo temporal validando tus credenciales. Por favor, intenta acceder más tarde.`;
            }
        }
    } catch (err) {
        errorMsj.innerHTML = `<strong>Sin conexión con el servidor de autenticación:</strong><br>Parece que estás desconectado de internet o el sistema central de EnergiAI está temporalmente fuera de línea. Revisa tu conexión de red.`;
    } finally {
        btnSubmit.classList.remove('is-loading');
        btnSubmit.disabled = false;
        btnSubmit.textContent = textoOriginal;
    }
});

document.getElementById('btn-logout')?.addEventListener('click', () => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(EMAIL_KEY);
    verificarAuthUI();
    document.getElementById('resultado-card').innerHTML = `<div class="empty-state">Todavía no cargaste una lectura. Andá a "Nueva lectura" para generar un resultado.</div>`;
});

// --- SUBMIT LECTURA ---
document.getElementById('form-consumo')?.addEventListener('submit', async (e) => {
    e.preventDefault();

    const boton = e.target.querySelector('button[type="submit"]');
    const textoOriginal = boton.textContent;
    boton.classList.add('is-loading');
    boton.disabled = true;

    const consumoKwh = parseFloat(document.getElementById('consumoKwh').value);
    const tipoInmueble = document.getElementById('tipoInmueble').value;
    const cantidadEquipos = parseInt(document.getElementById('cantidadEquipos').value, 10);
    const horasAltoConsumo = parseFloat(document.getElementById('horasAltoConsumo').value);
    const usoHorarioPico = document.getElementById('usoHorarioPico').checked;
    const errorContenedor = document.getElementById('resultado-card');

    // Validación verborrágica en JS para coincidir exactamente con el modelo
    const erroresFormulario = [];
    if (isNaN(consumoKwh) || consumoKwh <= 0) {
        erroresFormulario.push("<strong>Consumo mensual (kWh):</strong> Debes ingresar un número mayor a 0.");
    }
    if (tipoInmueble !== 'Casa' && tipoInmueble !== 'Departamento') {
        erroresFormulario.push("<strong>Tipo de inmueble:</strong> Por ahora, el sistema de Machine Learning solo está entrenado para evaluar 'Casa' o 'Departamento'. Otras opciones estarán disponibles próximamente.");
    }
    if (isNaN(cantidadEquipos) || cantidadEquipos < 1) {
        erroresFormulario.push("<strong>Cantidad de equipos:</strong> Debes ingresar al menos 1 equipo de alto consumo.");
    }
    if (isNaN(horasAltoConsumo) || horasAltoConsumo < 0 || horasAltoConsumo > 24) {
        erroresFormulario.push("<strong>Horas de alto consumo:</strong> Debe ser un valor realista entre 0 y 24 horas al día.");
    }

    if (erroresFormulario.length > 0) {
        errorContenedor.innerHTML = `
            <div class="notification is-warning is-light m-4">
                <strong>No podemos procesar tu consulta todavía:</strong><br>
                <ul style="margin-top: 10px; margin-left: 20px; list-style-type: disc;">
                    <li>${erroresFormulario.join('</li><li>')}</li>
                </ul>
            </div>`;
        goTo('resultado');
        boton.classList.remove('is-loading');
        boton.disabled = false;
        boton.textContent = textoOriginal;
        return;
    }

    const datos = {
        consumo_kwh: Number(document.getElementById('consumoKwh').value),
        uso_horario_pico: document.getElementById('usoHorarioPico').checked,
        cantidad_equipos: Number(document.getElementById('cantidadEquipos').value),
        tipo_inmueble: document.getElementById('tipoInmueble').value,
        horas_alto_consumo: Number(document.getElementById('horasAltoConsumo').value)
    };

    try {
        const token = localStorage.getItem(TOKEN_KEY);
        const headers = { 'Content-Type': 'application/json' };
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const response = await fetch(`${URL_BACKEND}/analisis-energetico`, {
            method: 'POST',
            headers: headers,
            body: JSON.stringify(datos)
        });

        if (response.status === 403 || response.status === 401) {
            mostrarErrores({ detalles: ["Debes iniciar sesión para realizar un análisis"] });
            abrirModal('modal-login');
            return;
        }

        let data;
        try {
            data = await response.json();
        } catch {
            data = null;
        }

        if (!response.ok || !data) {
            mostrarErrores(data);
            return;
        }

        mostrarResultado(data);
        ultimoAnalisis = {
            consumo_kwh: datos.consumo_kwh,
            categoria: data.categoria,
            probabilidad: data.probabilidad,
            costo_estimado_mensual: data.costo_estimado_mensual
        };

        goTo('resultado');

    } catch (error) {
        console.error('Error de conexión:', error);
        mostrarErrorConexion();
    } finally {
        boton.disabled = false;
        boton.textContent = textoOriginal;
    }
});

// --- RENDER RESULTADO (DEMO STYLE) ---
function catClass(cat){ return cat==='Eficiente'?'Eficiente':cat==='Moderado'?'Moderado':'Ineficiente'; }
function percentFor(cat){ return cat==='Eficiente'?0.15:cat==='Moderado'?0.5:0.85; }
function angleFor(p){ return -90 + p*180; }

function gaugeSvg(percent, cat){
    return `<svg class="gauge" viewBox="0 0 300 190">
      <path d="M 30 150 A 120 120 0 0 1 90 46.08" fill="none" stroke="#7FA06B" stroke-width="22"/>
      <path d="M 90 46.08 A 120 120 0 0 1 210 46.08" fill="none" stroke="#D9A441" stroke-width="22"/>
      <path d="M 210 46.08 A 120 120 0 0 1 270 150" fill="none" stroke="#BE5B4C" stroke-width="22"/>
      <g class="needle" style="transform:rotate(${angleFor(percent)}deg);">
        <line x1="150" y1="150" x2="150" y2="38" stroke="#EDE7D9" stroke-width="3"/>
        <circle cx="150" cy="150" r="8" fill="#EDE7D9"/><circle cx="150" cy="150" r="3" fill="#12181A"/>
      </g></svg>`;
}

function mostrarResultado(data) {
    const percent = percentFor(data.categoria);
    const inmueble = document.getElementById('tipoInmueble').value || 'Inmueble';
    const consumo = document.getElementById('consumoKwh').value || '0';

    const listaRecomendaciones = (data.recomendaciones || [])
        .map(r => `<li>${r}</li>`)
        .join('');

    document.getElementById('resultado-card').innerHTML = `
      <div class="gauge-row">
        ${gaugeSvg(percent, data.categoria)}
        <div class="gauge-meta">
          <div class="cat cat-${catClass(data.categoria)}">${data.categoria}</div>
          <div class="prob">probabilidad ${(data.probabilidad*100).toFixed(0)}% · ${inmueble} · ${consumo} kWh</div>
          <ul class="rec-list">${listaRecomendaciones}</ul>
        <div class="cost-card"><span class="cl">costo_estimado_mensual</span><span class="cv">${monedaSymbol} ${data.costo_estimado_mensual.toFixed(2).replace('.', ',')}</span></div>
      </div>
      <button class="button is-primary is-fullwidth mt-4" id="btn-guardar-historial">Guardar este análisis</button>
    `;

    document.getElementById('btn-guardar-historial').addEventListener('click', guardarEnHistorial);
}

function mostrarErrores(data) {
    const contenedor = document.getElementById('resultado-card');
    if (!data || !data.detalles) {
        contenedor.innerHTML = `
        <div class="notification is-warning is-light m-4">
            <strong>Ocurrió un inconveniente con los datos ingresados</strong><br>
            El servidor no pudo procesar tu solicitud, pero no especificó el motivo exacto. Por favor, asegúrate de haber completado todos los campos del formulario con valores numéricos lógicos y vuelve a intentarlo.
        </div>`;
        return;
    }
    const listaErrores = data.detalles
        .map(d => {
            if (d.campo && d.mensaje) return `<li><strong>${d.campo}</strong>: ${d.mensaje}</li>`;
            return `<li>${d}</li>`;
        })
        .join('');
    contenedor.innerHTML = `
        <div class="notification is-danger is-light m-4">
            <strong>Hemos detectado algunos errores en tu lectura:</strong><br>
            <ul style="margin-top: 10px; margin-left: 20px; list-style-type: disc;">${listaErrores}</ul>
            <p style="margin-top: 10px;">Por favor, corrige estos campos en la pestaña "Nueva lectura" para que podamos generar tu diagnóstico.</p>
        </div>`;
    goTo('resultado');
}

function mostrarErrorConexion() {
    document.getElementById('resultado-card').innerHTML = `
        <div class="notification is-danger is-light m-4">
            <strong>¡Vaya! No pudimos comunicarnos con el servidor.</strong><br>
            <p style="margin-top: 5px;">Tu intento de enviar la lectura falló porque no hay conexión con la base de datos de EnergiAI. Esto generalmente ocurre por dos razones:</p>
            <ul style="margin-top: 5px; margin-left: 20px; list-style-type: disc;">
                <li>Tu conexión a internet es inestable o está caída.</li>
                <li>Nuestros servidores de cálculo (OCI) están en mantenimiento temporal.</li>
            </ul>
            <p style="margin-top: 10px;">Por favor, espera unos minutos e inténtalo de nuevo.</p>
        </div>`;
    goTo('resultado');
}

async function guardarEnHistorial() {
    const boton = document.getElementById('btn-guardar-historial');
    boton.disabled = true;
    boton.textContent = 'Guardando...';

    try {
        const token = localStorage.getItem(TOKEN_KEY);
        const response = await fetch(`${URL_BACKEND}/historial`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
            body: JSON.stringify(ultimoAnalisis)
        });

        if (response.ok) {
            boton.textContent = '✓ Guardado';
        } else {
            boton.disabled = false;
            boton.textContent = 'Error al guardar — reintentar';
        }
    } catch {
        boton.disabled = false;
        boton.textContent = 'Error de conexión — reintentar';
    }
}

// --- MODALS ---
function abrirModal(id) {
    document.getElementById(id)?.classList.add('is-active');
}
function cerrarModal(modal) {
    modal.classList.remove('is-active');
}

document.querySelectorAll('[data-target]').forEach(disparador => {
    disparador.addEventListener('click', (e) => {
        e.preventDefault();
        abrirModal(disparador.dataset.target);
    });
});
document.querySelectorAll('.modal').forEach(modal => {
    const cerrar = () => cerrarModal(modal);
    modal.querySelector('.modal-background')?.addEventListener('click', cerrar);
    modal.querySelector('.delete')?.addEventListener('click', cerrar);
    modal.querySelector('.btn-cancel')?.addEventListener('click', (e) => { e.preventDefault(); cerrar(); });
});
document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
        document.querySelectorAll('.modal.is-active').forEach(cerrarModal);
    }
});

// --- CSV UPLOAD ---
const inputCsv = document.getElementById('input-csv');
const btnConfirmarCsv = document.getElementById('btn-confirmar-csv');
inputCsv?.addEventListener('change', () => {
    const archivo = inputCsv.files[0];
    if (archivo) {
        btnConfirmarCsv.disabled = false;
        document.getElementById('csv-hint').textContent = `Seleccionado: ${archivo.name}`;
    } else {
        btnConfirmarCsv.disabled = true;
    }
});
btnConfirmarCsv?.addEventListener('click', async () => {
    const archivo = inputCsv.files[0];
    if (!archivo) return;
    const textoOriginal = btnConfirmarCsv.textContent;
    btnConfirmarCsv.disabled = true;
    btnConfirmarCsv.textContent = 'Importando...';

    let resultadoDiv = document.getElementById('resultado-csv');
    if (!resultadoDiv) {
        resultadoDiv = document.createElement('div');
        resultadoDiv.id = 'resultado-csv';
        resultadoDiv.className = 'hint mt-4';
        btnConfirmarCsv.insertAdjacentElement('afterend', resultadoDiv);
    }

    try {
        const token = localStorage.getItem(TOKEN_KEY);
        const formData = new FormData();
        formData.append('file', archivo);

        const response = await fetch(`${URL_BACKEND}/historial/importar`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${token}` },
            body: formData
        });

        const data = await response.json();
        if (!response.ok) {
            resultadoDiv.innerHTML = `<span style="color:var(--red);">Error al importar el archivo.</span>`;
            return;
        }

        const listaErrores = data.errores ? data.errores.map(e => `<li>Fila ${e.fila}: ${e.motivo}</li>`).join('') : '';
        resultadoDiv.innerHTML = `
            ${data.filas_exitosas} de ${data.filas_procesadas} filas importadas correctamente.
            ${data.filas_con_error > 0 ? `<div class="has-text-danger">Filas con error:<ul>${listaErrores}</ul></div>` : ''}
        `;
    } catch (error) {
        resultadoDiv.innerHTML = `<span style="color:var(--red);">No se pudo conectar con el servidor.</span>`;
    } finally {
        btnConfirmarCsv.disabled = false;
        btnConfirmarCsv.textContent = textoOriginal;
    }
});

// --- DEMO VIEWS & INTERACTIONS ---
const alertLog = [
    {t:'Sucursal Sur superó 400 kWh en el mes de Julio', d:'hace 3 días'},
    {t:'Casa Matriz clasificada como Ineficiente dos meses seguidos', d:'hace 5 días'},
    {t:'Sucursal Norte volvió a rango Eficiente', d:'hace 1 semana'}
];


let monedaSymbol = 'R$';

function updateTariff(){
    tarifa = Number(document.getElementById('in-tarifa').value) || 0.75;
    const monedaSelect = document.getElementById('in-moneda');
    if (monedaSelect) {
        monedaSymbol = monedaSelect.value.split(' ')[0];
    }
    const display = document.getElementById('tariff-display');
    if (display) display.textContent = monedaSymbol + ' ' + tarifa.toFixed(2).replace('.', ',');

    // Si la función renderSimulador existe, la llamamos para actualizar el simulador con la nueva moneda
    if (typeof renderSimulador === 'function') renderSimulador();
    // Si estamos en la pestaña historial, la recargamos para actualizar el símbolo
    const vistaActiva = document.querySelector('.view.is-active');
    if (vistaActiva && vistaActiva.id === 'view-historial') {
        loadHistorialDemo();
    }
}

function goTo(view){
    document.querySelectorAll('.view').forEach(v => v.classList.remove('is-active'));
    document.querySelectorAll('.navitem').forEach(n => n.classList.remove('is-active'));
    const targetView = document.getElementById('view-' + view);
    if(targetView) targetView.classList.add('is-active');
    const targetNav = document.querySelector(`.navitem[data-view="${view}"]`);
    if(targetNav) targetNav.classList.add('is-active');

    // Si estamos en mobile, cerrar el sidebar al hacer click
    const sidebar = document.getElementById('sidebar-menu');
    if (sidebar && sidebar.classList.contains('is-active')) {
        sidebar.classList.remove('is-active');
        document.querySelector('.navbar-burger').classList.remove('is-active');
    }

    if(view === 'historial') loadHistorialDemo();
}

function setMode(m){
    mode = m;

    // Quitar primary style al botón inactivo y ponérselo al activo
    const btnHogar = document.getElementById('mode-hogar');
    const btnEmpresa = document.getElementById('mode-empresa');

    if(m === 'hogar'){
        if(btnHogar) btnHogar.classList.add('is-primary');
        if(btnEmpresa) btnEmpresa.classList.remove('is-primary');
    } else {
        if(btnHogar) btnHogar.classList.remove('is-primary');
        if(btnEmpresa) btnEmpresa.classList.add('is-primary');
    }

    document.querySelectorAll('.empresa-only').forEach(el => el.classList.toggle('is-hidden', m !== 'empresa'));
    const rankingView = document.getElementById('view-ranking');
    if(m === 'hogar' && rankingView && rankingView.classList.contains('is-active')) goTo('lectura');
}

document.querySelectorAll('.navitem').forEach(n => n.addEventListener('click', () => goTo(n.dataset.view)));

// Navbar Burger Logic for Mobile
document.addEventListener('DOMContentLoaded', () => {
    const burger = document.querySelector('.navbar-burger');
    if (burger) {
        burger.addEventListener('click', () => {
            const target = document.getElementById(burger.dataset.target);
            burger.classList.toggle('is-active');
            if (target) {
                target.classList.toggle('is-active'); // Sidebar
                target.classList.toggle('is-hidden-touch');
            }
            // Also toggle top nav menu on mobile
            const topMenu = document.getElementById('nav-menu-top');
            if(topMenu) topMenu.classList.toggle('is-active');
        });
    }
});

function renderAlertLog(){
    const logEl = document.getElementById('alert-log');
    if (logEl) logEl.innerHTML = alertLog.map(a => `<div class="log-item"><div class="dot"></div><div><div class="lt">${a.t}</div><div class="ld">${a.d}</div></div></div>`).join('');
}

function categorizeSim(consumo, pico, horas, equipos){
    const base = consumo + (pico?50:0) + horas*10 + equipos*5;
    let cat = base < 300 ? 'Eficiente' : base < 550 ? 'Moderado' : 'Ineficiente';
    let prob = cat==='Eficiente' ? 0.86 : cat==='Moderado' ? 0.73 : 0.81;
    return {cat, prob, cost: consumo * tarifa, base};
}

function renderSimulador(){
    const equipos = Number(document.getElementById('sim-equipos').value);
    const horas = Number(document.getElementById('sim-horas').value);
    const pico = document.getElementById('sim-pico').classList.contains('on');
    document.getElementById('sim-equipos-val').textContent = equipos;
    document.getElementById('sim-horas-val').textContent = horas;

    const consumoBase = 420;
    const r = categorizeSim(consumoBase, pico, horas, equipos);
    const percent = percentFor(r.cat);

    const needle = document.getElementById('sim-needle');
    if(needle) needle.style.transform = `rotate(${angleFor(percent)}deg)`;

    const catEl = document.getElementById('sim-cat');
    if (catEl) {
        catEl.textContent = r.cat;
        catEl.className = 'cat cat-' + catClass(r.cat);
    }
    const costEl = document.getElementById('sim-costo');
    if(costEl) costEl.textContent = monedaSymbol + ' ' + r.cost.toFixed(2).replace('.', ',');
}

async function loadHistorialDemo() {
    const tbody = document.querySelector('#hist-table tbody');
    if (!tbody) return;

    tbody.innerHTML = '<tr><td colspan="4">Cargando...</td></tr>';

    try {
        const token = localStorage.getItem(TOKEN_KEY);
        const response = await fetch(`${URL_BACKEND}/historial`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) {
            tbody.innerHTML = `<tr><td colspan="4">
                <div class="notification is-danger is-light">
                    <strong>Error al cargar el historial</strong><br>
                    El servidor respondió con un error (Código ${response.status}). Es posible que tu sesión haya expirado. Intenta cerrar sesión y volver a ingresar.
                </div>
            </td></tr>`;
            return;
        }

        const data = await response.json();

        // Handle Resumen Cards
        if (data.resumen) {
            const prom = data.resumen.promedioCostoMensual || data.resumen.promedio_costo_mensual || 0;
            const freq = data.resumen.categoriaMasFrecuente || data.resumen.categoria_mas_frecuente || '--';
            const tend = data.resumen.tendencia || '--';

            const promEl = document.getElementById('res-promedio');
            if(promEl) promEl.textContent = `${monedaSymbol} ${prom.toFixed(2).replace('.', ',')}`;

            const freqEl = document.getElementById('res-frecuente');
            if(freqEl) {
                freqEl.textContent = freq;
                freqEl.className = 'title is-4 ' + (freq === 'Eficiente' ? 'has-text-success' : freq === 'Moderado' ? 'has-text-warning' : 'has-text-danger');
            }

            const tendEl = document.getElementById('res-tendencia');
            if(tendEl) {
                let tendColor = 'has-text-white';
                const tendLower = tend.toLowerCase();
                if (tendLower.includes('sube') || tendLower.includes('aumento') || tendLower.includes('alza')) tendColor = 'has-text-danger';
                if (tendLower.includes('baja') || tendLower.includes('reducción') || tendLower.includes('baja')) tendColor = 'has-text-success';
                tendEl.textContent = tend;
                tendEl.className = 'title is-4 ' + tendColor;
            }
        }

        if (!data.analisis || data.analisis.length === 0) {
            tbody.innerHTML = `<tr><td colspan="4">
                <div class="notification is-info is-light">
                    <strong>Aún no tienes un historial</strong><br>
                    Todavía no has guardado ningún análisis de consumo. Dirígete a la pestaña "Nueva lectura", ingresa tus datos y haz clic en "Guardar este análisis" para empezar a generar tu historial.
                </div>
            </td></tr>`;
            return;
        }

        // Parse records safely
        const records = data.analisis.map(a => ({
            fecha: new Date(a.fecha),
            consumo: a.consumoKwh || a.consumo_kwh || 0,
            categoria: a.categoria || '--',
            costo: a.costoEstimadoMensual || a.costo_estimado_mensual || 0
        }));

        // Table (descending, newest first)
        const sortedDesc = [...records].sort((a,b) => b.fecha - a.fecha);
        tbody.innerHTML = sortedDesc.map(a => `
            <tr>
                <td>${a.fecha.toLocaleDateString('es-AR')}</td>
                <td>${a.consumo}</td>
                <td><span class="tag is-${catClass(a.categoria)}">${a.categoria}</span></td>
                <td>${monedaSymbol} ${a.costo.toFixed(2)}</td>
            </tr>
        `).join('');

        if (window.Chart) {
            Chart.defaults.color = '#9BA3A0';
            Chart.defaults.font.family = "'IBM Plex Mono', monospace";

            // Line Chart (ascending, oldest first, max 15 points)
            const sortedAsc = [...records].sort((a,b) => a.fecha - b.fecha).slice(-15);

            if(histChart) histChart.destroy();
            const ctxHist = document.getElementById('histChart');
            if (ctxHist) {
                histChart = new Chart(ctxHist.getContext('2d'), {
                    type: 'line',
                    data: {
                        labels: sortedAsc.map(a => a.fecha.toLocaleDateString('es-AR', {day:'2-digit', month:'2-digit'})),
                        datasets: [{
                            label: 'Consumo (kWh)',
                            data: sortedAsc.map(a => a.consumo),
                            borderColor: '#66BB6A',
                            backgroundColor: 'rgba(102,187,106,0.1)',
                            fill: true,
                            tension: 0.35,
                            pointRadius: 4,
                            pointBackgroundColor: '#66BB6A'
                        }]
                    },
                    options: {
                        responsive: true,
                        maintainAspectRatio: false,
                        plugins: { legend: { display: false } },
                        scales: {
                            x: { grid: { color: '#313D40' } },
                            y: { grid: { color: '#313D40' }, beginAtZero: true }
                        }
                    }
                });
            }

            // Doughnut Chart
            if(window.catChartInstance) window.catChartInstance.destroy();
            const ctxCat = document.getElementById('catChart');
            if (ctxCat) {
                const counts = { 'Eficiente': 0, 'Moderado': 0, 'Ineficiente': 0 };
                records.forEach(a => { if(counts[a.categoria] !== undefined) counts[a.categoria]++; });

                window.catChartInstance = new Chart(ctxCat.getContext('2d'), {
                    type: 'doughnut',
                    data: {
                        labels: ['Eficiente', 'Moderado', 'Ineficiente'],
                        datasets: [{
                            data: [counts['Eficiente'], counts['Moderado'], counts['Ineficiente']],
                            backgroundColor: ['#7FA06B', '#D9A441', '#BE5B4C'],
                            borderWidth: 0,
                            hoverOffset: 4
                        }]
                    },
                    options: {
                        responsive: true,
                        maintainAspectRatio: false,
                        cutout: '75%',
                        plugins: {
                            legend: { position: 'bottom', labels: { usePointStyle: true, padding: 15 } }
                        }
                    }
                });
            }
        }
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="4">
            <div class="notification is-danger is-light">
                <strong>Sin conexión al servidor</strong><br>
                Ocurrió un problema de red al intentar descargar tus datos históricos. Verifica tu conexión a internet e inténtalo de nuevo.
            </div>
        </td></tr>`;
    }
}

setMode('hogar');
renderAlertLog();
updateTariff();

/* ===== EnergiAI Lab (Showcase R&D) - Demos ===== */

// MODULE 2: Simulador Fotovoltaico
function updateSolarDemo() {
    const slider = document.getElementById('solar-kwp');
    if (!slider) return;
    const kwp = parseFloat(slider.value);

    const kwpValEl = document.getElementById('solar-kwp-val');
    const ahorroEl = document.getElementById('solar-ahorro');
    const panelesEl = document.getElementById('solar-paneles');

    // Estimaciones simples para la demo (no son cálculos reales de ROI)
    const ahorroMensual = Math.round(kwp * 1500);
    const paneles = Math.max(1, Math.round(kwp / 0.5));

    if (kwpValEl) kwpValEl.textContent = kwp.toFixed(1);
    if (ahorroEl) ahorroEl.textContent = `$ ${ahorroMensual.toLocaleString('es-AR')} /mes`;
    if (panelesEl) panelesEl.textContent = paneles;
}

// MODULE 3: Telemetría IoT (Smart Plugs)
let iotDemoActive = true;
let iotDemoInterval = null;

function toggleIotDemo() {
    const toggleEl = document.getElementById('iot-toggle');
    const cableEl = document.getElementById('iot-cable');
    const consumoEl = document.getElementById('iot-consumo');
    if (!toggleEl) return;

    iotDemoActive = !iotDemoActive;
    toggleEl.classList.toggle('on', iotDemoActive);

    if (iotDemoActive) {
        if (cableEl) cableEl.style.opacity = '1';
        if (iotDemoInterval) clearInterval(iotDemoInterval);
        iotDemoInterval = setInterval(() => {
            const w = Math.round(10 + Math.random() * 15);
            if (consumoEl) consumoEl.textContent = `${w} W`;
        }, 1500);
    } else {
        if (cableEl) cableEl.style.opacity = '0.2';
        if (consumoEl) consumoEl.textContent = '0 W';
        if (iotDemoInterval) {
            clearInterval(iotDemoInterval);
            iotDemoInterval = null;
        }
    }
}

// MODULE 1: Smart OCR Scanner
function runOcrDemo() {
    const laser = document.getElementById('ocr-laser');
    const result1 = document.getElementById('ocr-result-1');
    const result2 = document.getElementById('ocr-result-2');
    const btn = document.getElementById('btn-demo-ocr');
    if (!laser || !result1 || !result2 || !btn) return;

    btn.disabled = true;
    laser.classList.add('scanning');
    result1.textContent = 'kWh: ---';
    result2.textContent = 'Monto: ---';

    setTimeout(() => {
        const kwh = (150 + Math.random() * 200).toFixed(1);
        const monto = Math.round(kwh * 85);
        result1.textContent = `kWh: ${kwh}`;
        result2.textContent = `Monto: $ ${monto.toLocaleString('es-AR')}`;
        laser.classList.remove('scanning');
        btn.disabled = false;
    }, 1800);
}

document.addEventListener('DOMContentLoaded', () => {
    const btnOcr = document.getElementById('btn-demo-ocr');
    if (btnOcr) btnOcr.addEventListener('click', runOcrDemo);

    // Inicializa el simulador solar con el valor por defecto del slider
    updateSolarDemo();
});