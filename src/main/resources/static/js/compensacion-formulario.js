// compensacion-formulario.js — EXCLUSIVO de compensacion/formulario.html.
// Suma en vivo los montos capturados por tipo (CxC / CxP), muestra si cuadran
// y habilita/deshabilita el botón de aplicar. SOLO es anticipación visual:
// las reglas de negocio se revalidan SIEMPRE en el servidor.

document.addEventListener('DOMContentLoaded', () => {
    const filas = document.querySelectorAll('tr[data-tipo]');
    const panel = document.getElementById('cruce-panel');
    const totalCxc = document.getElementById('cruce-total-cxc');
    const totalCxp = document.getElementById('cruce-total-cxp');
    const mensaje = document.getElementById('cruce-mensaje');
    const botonAplicar = document.getElementById('btn-aplicar');

    // Sin documentos (o elementos ausentes) no hay nada que hacer:
    // el botón ya llega deshabilitado desde el servidor.
    if (filas.length === 0 || !panel || !botonAplicar) return;

    const formatear = (centavos) =>
        (centavos / 100).toLocaleString('es-MX',
            { minimumFractionDigits: 2, maximumFractionDigits: 2 });

    function recalcular() {
        let centavosCxc = 0;
        let centavosCxp = 0;
        let hayExcedidos = false;

        filas.forEach(fila => {
            const input = fila.querySelector('.input-celda');
            if (!input) return;

            const valor = parseFloat(input.value);
            const esValido = !isNaN(valor) && valor > 0;
            const centavos = esValido ? Math.round(valor * 100) : 0;

            // Aviso visual si el monto excede el saldo del documento
            const excede = esValido && input.dataset.saldo !== undefined
                    && valor > parseFloat(input.dataset.saldo);
            input.classList.toggle('input-celda--excedido', excede);
            if (excede) hayExcedidos = true;

            if (esValido) {
                if (fila.dataset.tipo === 'CXC') centavosCxc += centavos;
                else centavosCxp += centavos;
            }
        });

        totalCxc.textContent = formatear(centavosCxc);
        totalCxp.textContent = formatear(centavosCxp);

        panel.classList.remove('cruce-panel--ok', 'cruce-panel--error', 'cruce-panel--neutro');

        if (hayExcedidos) {
            panel.classList.add('cruce-panel--error');
            mensaje.textContent = 'Hay montos que exceden el saldo disponible';
            botonAplicar.disabled = true;
        } else if (centavosCxc === 0 && centavosCxp === 0) {
            panel.classList.add('cruce-panel--neutro');
            mensaje.textContent = 'Captura los montos a aplicar';
            botonAplicar.disabled = true;
        } else if (centavosCxc !== centavosCxp) {
            panel.classList.add('cruce-panel--error');
            const diferencia = Math.abs(centavosCxc - centavosCxp);
            const sobrante = centavosCxc > centavosCxp ? 'CxC' : 'CxP';
            mensaje.textContent = 'No cuadran — excede ' + sobrante
                    + ' por $' + formatear(diferencia);
            botonAplicar.disabled = true;
        } else {
            panel.classList.add('cruce-panel--ok');
            mensaje.textContent = '✓ Cuadran — monto a compensar: $' + formatear(centavosCxc);
            botonAplicar.disabled = false;
        }
    }

    filas.forEach(fila => {
        const input = fila.querySelector('.input-celda');
        if (input) input.addEventListener('input', recalcular);
    });

    recalcular(); // estado inicial coherente
});