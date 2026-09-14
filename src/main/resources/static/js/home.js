// home.js — EXCLUSIVO de home.html: inicializa el carrusel de módulos.
// Requiere que swiper-bundle.min.js esté cargado antes (se incluye con defer en orden).

document.addEventListener('DOMContentLoaded', () => {
    const swiper = new Swiper('.home-carrusel', {
        loop: true,
        autoplay: {
            delay: 3000,
            disableOnInteraction: false,
            pauseOnMouseEnter: true,
        },
        slidesPerView: 1,
        spaceBetween: 20,
        pagination: {
            el: '.swiper-pagination',
            clickable: true,
        },
        navigation: {
            nextEl: '.swiper-button-next',
            prevEl: '.swiper-button-prev',
        },
        breakpoints: {
            640: { slidesPerView: 2, spaceBetween: 20 },
            900: { slidesPerView: 3, spaceBetween: 24 },
        },
    });

    // Accesibilidad: pausa el autoplay cuando el foco entra por teclado (Tab)
    const carrusel = document.querySelector('.home-carrusel');
    carrusel.addEventListener('focusin',  () => swiper.autoplay.stop());
    carrusel.addEventListener('focusout', () => swiper.autoplay.start());
});