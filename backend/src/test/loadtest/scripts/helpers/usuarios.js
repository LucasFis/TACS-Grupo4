export const USUARIOS = [
    { nombre: 'admin',       contrasenia: 'Admin12!',        rol: 'ADMINISTRADOR' },
    { nombre: 'lucas_fis',   contrasenia: 'Gordo123!',       rol: 'USUARIO' },
    { nombre: 'sofia_ape',   contrasenia: 'Password1@',      rol: 'USUARIO' },
    { nombre: 'mati_crim',   contrasenia: 'Wordpass1$',      rol: 'USUARIO' },
    { nombre: 'juan_jose',   contrasenia: 'Una_contrasenia1', rol: 'USUARIO' },
    { nombre: 'vale_gom',    contrasenia: 'Passval2&',       rol: 'USUARIO' },
    { nombre: 'diego_ram',   contrasenia: 'Diegopass2#',     rol: 'USUARIO' },
];

export const USUARIOS_REGULARES = USUARIOS.filter(u => u.rol === 'USUARIO');

export function usuarioRandom() {
    return USUARIOS_REGULARES[Math.floor(Math.random() * USUARIOS_REGULARES.length)];
}

export function usuarioAdmin() {
    return USUARIOS.find(u => u.rol === 'ADMINISTRADOR');
}
