// archivo-subida.js — EXCLUSIVO de archivo/lista.html: zona de arrastre
// para subir ZIPs. Valida extensión antes de enviar (SweetAlert si falla)
// y hace auto-submit al soltar/seleccionar un archivo válido.

document.addEventListener('DOMContentLoaded', () => {
    const zona = document.getElementById('zona-arrastre');
    const input = document.getElementById('input-archivo');
    const form = document.getElementById('form-subida');
    const nombreMostrado = document.getElementById('archivo-seleccionado');

    if (!zona || !input || !form) return;

    // Evita que soltar un archivo FUERA de la zona haga que el navegador
    // lo abra (navegaría fuera de la página perdiendo el estado)
    ['dragover', 'drop'].forEach(evento => {
        window.addEventListener(evento, (e) => e.preventDefault());
    });

    const formatearTamano = (bytes) =>
        bytes >= 1048576 ? (bytes / 1048576).toFixed(2) + ' MB'
                         : (bytes / 1024).toFixed(2) + ' KB';

        function validarYEnviar(archivo) {
        if (!archivo) return;

        if (!archivo.name.toLowerCase().endsWith('.zip')) {
            Swal.fire({
                icon: 'error',
                title: 'Archivo no compatible',
                text: '"' + archivo.name + '" no es un archivo .zip',
                confirmButtonColor: '#1e3a8a',
            });
            return;
        }

        // Poner el archivo DENTRO del input antes de enviar.
        // Por clic/explorador ya está; por arrastre el archivo vive
        // en dataTransfer y nunca pasó por el input — hay que copiarlo.
        // DataTransfer es la API estándar para manipular input.files.
        const transferidor = new DataTransfer();
        transferidor.items.add(archivo);
        input.files = transferidor.files;

        nombreMostrado.textContent = archivo.name + ' (' + formatearTamano(archivo.size) + ')';
        zona.classList.add('zona-arrastre--cargando');
        form.submit();
    }

    // Clic (o Enter/Espacio con foco por teclado) abre el explorador de archivos
    zona.addEventListener('click', () => input.click());
    zona.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            input.click();
        }
    });

    // Selección manual por el explorador
    input.addEventListener('change', () => validarYEnviar(input.files[0]));

    // Estados de arrastre
    ['dragenter', 'dragover'].forEach(evento => {
        zona.addEventListener(evento, (e) => {
            e.preventDefault();
            zona.classList.add('zona-arrastre--sobre');
        });
    });
    zona.addEventListener('dragleave', () => zona.classList.remove('zona-arrastre--sobre'));
    zona.addEventListener('drop', (e) => {
        e.preventDefault();
        zona.classList.remove('zona-arrastre--sobre');
        validarYEnviar(e.dataTransfer.files[0]);
    });
});