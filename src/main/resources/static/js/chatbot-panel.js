// chatbot-panel.js — Widget de chat flotante. El script crea el DOM
// (botón + panel), por eso basta incluirlo en la cabecera de cada página.
// Consumo: GET /chatbot/consultar?pregunta=... (GET = sin CSRF; sesión ya requerida).

document.addEventListener('DOMContentLoaded', () => {

    // ── Construcción del DOM ──
    const lanzador = document.createElement('button');
    lanzador.className = 'chat-lanzador';
    lanzador.title = 'Asistente';
    lanzador.textContent = '💬';
    document.body.appendChild(lanzador);

    const panel = document.createElement('div');
    panel.className = 'chat-panel';
    panel.innerHTML = `
        <div class="chat-panel__cabecera">
            <span class="chat-panel__titulo">Asistente<small>consultas de trámites y pólizas</small></span>
            <button type="button" class="chat-panel__cerrar" title="Cerrar">✕</button>
        </div>
        <div class="chat-panel__mensajes"></div>
        <form class="chat-panel__entrada">
            <input type="text" placeholder="Escribe tu pregunta..." autocomplete="off">
            <button type="submit">Enviar</button>
        </form>`;
    document.body.appendChild(panel);

    const mensajes = panel.querySelector('.chat-panel__mensajes');
    const form = panel.querySelector('.chat-panel__entrada');
    const input = form.querySelector('input');
    const botonEnviar = form.querySelector('button');
    const CLAVE_HISTORIAL = 'chat_historial_visual';

    // ── Utilidades ──
    function agregarMensaje(texto, tipo) {
        const div = document.createElement('div');
        div.className = 'chat-mensaje chat-mensaje--' + tipo;
        div.textContent = texto;           // textContent: sin inyección HTML
        mensajes.appendChild(div);
        mensajes.scrollTop = mensajes.scrollHeight;
        return div;
    }

    function marcarOcupado(ocupado) {
        botonEnviar.disabled = ocupado;
        input.disabled = ocupado;
    }

    function cargarHistorialVisual() {
        try {
            return JSON.parse(localStorage.getItem(CLAVE_HISTORIAL)) || [];
        } catch {
            return [];
        }
    }

    function registrarEnHistorial(texto, tipo) {
        const historial = cargarHistorialVisual();
        historial.push({ tipo: tipo, texto: texto });
        // Conserva solo los últimos 30 para no engordar el localStorage
        localStorage.setItem(CLAVE_HISTORIAL, JSON.stringify(historial.slice(-30)));
    }

    // ── Abrir / cerrar ──
        lanzador.addEventListener('click', () => {
        panel.classList.toggle('chat-panel--abierto');
        if (panel.classList.contains('chat-panel--abierto')) {
            input.focus();
            if (mensajes.children.length === 0) {
                const historial = cargarHistorialVisual();
                if (historial.length > 0) {
                    historial.forEach(m => agregarMensaje(m.texto, m.tipo));
                } else {
                    agregarMensaje('Hola. Puedo consultar: trámites por periodo, '
                        + 'trámites sin póliza, datos de una póliza y proveedores no registrados.',
                        'bot');
                }
            }
        }
    });
    panel.querySelector('.chat-panel__cerrar').addEventListener('click', () => {
        panel.classList.remove('chat-panel--abierto');
    });

    // ── Envío ──
    form.addEventListener('submit', async (evento) => {
        evento.preventDefault();
        const pregunta = input.value.trim();
        if (!pregunta || botonEnviar.disabled) return;

        agregarMensaje(pregunta, 'usuario');
        registrarEnHistorial(pregunta, 'usuario');
        input.value = '';
        marcarOcupado(true);
        const indicador = agregarMensaje('Consultando...', 'bot');

        try {
            const respuesta = await fetch(
                '/chatbot/consultar?pregunta=' + encodeURIComponent(pregunta));
            if (!respuesta.ok) {
                throw new Error('El servidor respondió ' + respuesta.status);
            }
            const texto = await respuesta.text();
            indicador.textContent = texto;
            registrarEnHistorial(texto, 'bot');
        } catch (error) {
            indicador.className = 'chat-mensaje chat-mensaje--error';
            indicador.textContent = 'No se pudo completar la consulta. Intenta de nuevo.';
        } finally {
            marcarOcupado(false);
            mensajes.scrollTop = mensajes.scrollHeight;
            input.focus();
        }
    });
});