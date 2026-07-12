import http from 'k6/http';
import { check, sleep } from 'k6';
import { login } from '../helpers/auth.js';
import { optionsDefault } from '../helpers/options.js';

export const options = optionsDefault;

const BASE = 'http://backend-test:8080';
const USUARIO = { nombre: 'lucas_fis', contrasenia: 'Gordo123!' };

export function testEditarContrasenia(authHeaders) {
    const res = http.put(`${BASE}/usuarios/contrasenia`, JSON.stringify({
        contrasenia_actual: USUARIO.contrasenia,
        contrasenia_nueva: 'NewLT1234!',
    }), { headers: { ...authHeaders, 'Content-Type': 'application/json' } });
    check(res, {
        '[contrasenia] status 204 o 400': (r) => r.status === 204 || r.status === 400,
    });
    if (res.status === 204) {
        http.put(`${BASE}/usuarios/contrasenia`, JSON.stringify({
            contrasenia_actual: 'NewLT1234!',
            contrasenia_nueva: USUARIO.contrasenia,
        }), { headers: { ...authHeaders, 'Content-Type': 'application/json' } });
    }
}

export default function () {
    const token = login(BASE, USUARIO);
    if (!token) return;
    testEditarContrasenia({ 'Cookie': `token=${token}` });
    sleep(1);
}
