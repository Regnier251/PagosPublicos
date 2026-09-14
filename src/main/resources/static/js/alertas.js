// Requiere sweetalert2.all.min.js cargado antes (mismo orden con defer).
//
// Convenciones que atiende este archivo:
//   <body th:data-exito="...">  → toast verde al cargar la página
//   <body th:data-error="...">  → toast rojo al cargar la página
//   <form data-confirmar="¿texto?">  → pide confirmación (botón azul)
//   <form data-confirmar="..." data-confirmar-peligro> → confirmación con botón rojo

document.addEventListener('DOMContentLoaded', () => {

    // ── Toasts para mensajes flash (exito / error) ──
    const Toast = Swal.mixin({
        toast: true,
        position: 'top-end',
        showConfirmButton: false,
        timerProgressBar: true,
    });

    const exito = document.body.dataset.exito;
    const error = document.body.dataset.error;

    if (exito) Toast.fire({ icon: 'success', title: exito, timer: 3500 });
    if (error) Toast.fire({ icon: 'error', title: error, timer: 6000 });

    // ── Confirmaciones para formularios destructivos ──
    document.querySelectorAll('form[data-confirmar]').forEach(form => {
        form.addEventListener('submit', (evento) => {
            evento.preventDefault();

            const esPeligroso = form.hasAttribute('data-confirmar-peligro');

            Swal.fire({
                title: form.dataset.confirmar,
                icon: 'warning',
                showCancelButton: true,
                confirmButtonText: 'Sí, continuar',
                cancelButtonText: 'Cancelar',
                confirmButtonColor: esPeligroso ? '#b91c1c' : '#1e3a8a',
                cancelButtonColor: '#64748b',
                reverseButtons: true,
            }).then((resultado) => {
                if (resultado.isConfirmed) form.submit();
            });
        });
    });
});