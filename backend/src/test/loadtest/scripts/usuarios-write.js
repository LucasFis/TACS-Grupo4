import http from 'k6/http';
import { check, sleep } from 'k6';
import { login } from './helpers/auth.js';
import { optionsDefault } from './helpers/options.js';

export const options = optionsDefault;

const BASE = 'http://backend-test:8080';
const USUARIO = { nombre: 'lucas_fis', contrasenia: 'Gordo123!' };

export function testRegistrarUsuario() {
    const nombre = `lt_${__VU}_${__ITER}`;
    const res = http.post(`${BASE}/usuarios`, JSON.stringify({
        nombre: nombre,
        contrasenia: 'Loadtest1!',
        rol: 'USUARIO'
    }), { headers: { 'Content-Type': 'application/json' } });
    check(res, {
        '[registrar] status 204': (r) => r.status === 204,
    });
}

export function testRegistrarAdministrador(authHeaders) {
    const nombre = `lt_adm_${__VU}_${__ITER}`;
    const res = http.post(`${BASE}/administradores`, JSON.stringify({
        nombre: nombre,
        contrasenia: 'AdminLT1!',
        rol: 'ADMINISTRADOR'
    }), { headers: { ...authHeaders, 'Content-Type': 'application/json' } });
    check(res, {
        '[admin] status 204 o 403': (r) => r.status === 204 || r.status === 403,
    });
}

export function testVerificarNombre() {
    const res = http.head(`${BASE}/usuarios/${USUARIO.nombre}`);
    check(res, {
        '[head] status 200 o 404': (r) => r.status === 200 || r.status === 404,
    });
}

export function testEditarContrasenia(authHeaders) {
    const res = http.put(`${BASE}/usuarios/contrasenia`, JSON.stringify({
        contrasenia_actual: USUARIO.contrasenia,
        contrasenia_nueva: 'NewLT1234!'
    }), { headers: { ...authHeaders, 'Content-Type': 'application/json' } });
    check(res, {
        '[contrasenia] status 204 o 400': (r) => r.status === 204 || r.status === 400,
    });
    if (res.status === 204) {
        http.put(`${BASE}/usuarios/contrasenia`, JSON.stringify({
            contrasenia_actual: 'NewLT1234!',
            contrasenia_nueva: USUARIO.contrasenia
        }), { headers: { ...authHeaders, 'Content-Type': 'application/json' } });
    }
}

export function testEditarPerfil(authHeaders) {
    const res = http.put(`${BASE}/perfil`, JSON.stringify({
        nombre: 'Lucas',
        nombre_usuario: USUARIO.nombre,
        medios_de_contacto: []
    }), { headers: { ...authHeaders, 'Content-Type': 'application/json' } });
    check(res, {
        '[editar-perfil] status 200 o 204': (r) => r.status === 200 || r.status === 204,
    });
}

export function testCalificarPerfil(authHeaders) {
    const res = http.post(`${BASE}/perfil/calificaciones`, JSON.stringify({
        valor: 5,
        destinatario_id: 'nonexistent',
        transaction_id: 'fake_tx',
        tipo_transaccion: 'INTERCAMBIO'
    }), { headers: { ...authHeaders, 'Content-Type': 'application/json' } });
    check(res, {
        '[calificar] responde': (r) => r.status >= 200,
    });
}

export function testMarcarLeidas(authHeaders) {
    const res = http.patch(`${BASE}/perfil/notificaciones/leidas`, null, { headers: authHeaders });
    check(res, {
        '[leidas] status 204': (r) => r.status === 204,
    });
}

export function testCerrarSesion(authHeaders) {
    const res = http.del(`${BASE}/sesion`, null, { headers: authHeaders });
    check(res, {
        '[logout] status 204': (r) => r.status === 204,
    });
}

export default function () {
    const token = login(BASE, USUARIO);
    if (!token) return;
    const authHeaders = { 'Cookie': `token=${token}` };

    testRegistrarUsuario();
    testVerificarNombre();
    testRegistrarAdministrador(authHeaders);
    testEditarPerfil(authHeaders);
    testMarcarLeidas(authHeaders);
    testCalificarPerfil(authHeaders);
    testEditarContrasenia(authHeaders);
    testCerrarSesion(authHeaders);

    sleep(1);
}
