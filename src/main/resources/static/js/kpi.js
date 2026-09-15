// kpi.js — EXCLUSIVO de kpi/index.html: dibuja las gráficas con Chart.js.
// Consume las constantes JSON inyectadas por Thymeleaf (const DATOS_KPI),
// definidas en el script inline de la página (se ejecuta antes por orden de carga).

document.addEventListener('DOMContentLoaded', () => {

    // Dona: documentos por estatus (colores = mismas de las etiquetas de estatus)
    new Chart(document.getElementById('grafica-documentos'), {
        type: 'doughnut',
        data: {
            labels: ['Vigentes', 'Compensados', 'Cancelados'],
            datasets: [{
                data: [DATOS_KPI.docsVigentes, DATOS_KPI.docsCompensados, DATOS_KPI.docsCancelados],
                backgroundColor: ['#15803d', '#1e3a8a', '#b91c1c'],
                borderWidth: 2,
                borderColor: '#ffffff',
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { position: 'bottom' } }
        }
    });

    // Barras: ZIPs recibidos por mes
    new Chart(document.getElementById('grafica-archivos'), {
        type: 'bar',
        data: {
            labels: DATOS_KPI.etiquetasMeses,
            datasets: [{
                label: 'ZIPs recibidos',
                data: DATOS_KPI.archivosPorMes,
                backgroundColor: '#1e3a8a',
                borderRadius: 6,
                maxBarThickness: 48,
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: { y: { beginAtZero: true, ticks: { precision: 0 } } }
        }
    });
});