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
            if (data.detalles && data.detalles.length > 0) {
                errorMsj.innerHTML = data.detalles.map(d => d.mensaje || d).join('<br>');
            } else {
                errorMsj.textContent = data.mensaje || 'Error al registrar el usuario';
            }
        }
    } catch (err) {
        errorMsj.textContent = 'Error de conexión con el servidor';
    }
});

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
        </div>
      </div>
      <div class="cost-card"><span class="cl">costo_estimado_mensual</span><span class="cv">R$ ${data.costo_estimado_mensual.toFixed(2).replace('.', ',')}</span></div>
      <button class="btn btn-primary" style="width:100%; margin-top:16px;" id="btn-guardar-historial">Guardar este análisis</button>
    `;

    document.getElementById('btn-guardar-historial').addEventListener('click', guardarEnHistorial);
}

function mostrarErrores(data) {
    const contenedor = document.getElementById('resultado-card');
    if (!data || !data.detalles) {
        contenedor.innerHTML = `<div class="empty-state" style="color:var(--red);">Ocurrió un error inesperado. Revisá los datos ingresados.</div>`;
        return;
    }
    const listaErrores = data.detalles
        .map(d => {
            if (d.campo && d.mensaje) return `<li>${d.campo}: ${d.mensaje}</li>`;
            return `<li>${d}</li>`;
        })
        .join('');
    contenedor.innerHTML = `<ul style="color:var(--red); padding:20px;">${listaErrores}</ul>`;
    goTo('resultado');
}

function mostrarErrorConexion() {
    document.getElementById('resultado-card').innerHTML = `
        <div class="empty-state" style="color:var(--red);">No se pudo conectar con el servidor. Verificá que el backend esté corriendo.</div>
    `;
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

function updateTariff(){
    tarifa = Number(document.getElementById('in-tarifa').value) || 0.75;
    const display = document.getElementById('tariff-display');
    if (display) display.textContent = 'R$ ' + tarifa.toFixed(2).replace('.', ',');
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
    if(costEl) costEl.textContent = 'R$ ' + r.cost.toFixed(2).replace('.', ',');
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
            tbody.innerHTML = '<tr><td colspan="4" style="color:var(--red);">No se pudo cargar el histórico.</td></tr>';
            return;
        }

        const data = await response.json();

        // Handle Resumen Cards
        if (data.resumen) {
            const prom = data.resumen.promedioCostoMensual || data.resumen.promedio_costo_mensual || 0;
            const freq = data.resumen.categoriaMasFrecuente || data.resumen.categoria_mas_frecuente || '--';
            const tend = data.resumen.tendencia || '--';

            const promEl = document.getElementById('res-promedio');
            if(promEl) promEl.textContent = `R$ ${prom.toFixed(2).replace('.', ',')}`;

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
            tbody.innerHTML = '<tr><td colspan="4" class="has-text-grey">Todavía no guardaste ningún análisis.</td></tr>';
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
                <td>R$ ${a.costo.toFixed(2)}</td>
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
        tbody.innerHTML = '<tr><td colspan="4" style="color:var(--red);">Error al cargar.</td></tr>';
    }
}

setMode('hogar');
renderAlertLog();
