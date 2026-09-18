// seguridad-login.js — EXCLUSIVO de seguridad/login.html:
// mostrar/ocultar la contraseña con el icono del ojo.

const passwordLogin = (loginPass, loginEye) => {
    const input = document.getElementById(loginPass),
          iconEye = document.getElementById(loginEye)

    iconEye.addEventListener('click', () => {
        input.type === 'password' ? input.type = 'text'
                                  : input.type = 'password'
        iconEye.classList.toggle('ri-eye-fill')
        iconEye.classList.toggle('ri-eye-off-fill')
    })
}
passwordLogin('password', 'loginPassword')