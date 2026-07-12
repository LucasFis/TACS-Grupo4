import http from 'k6/http';
import { check, sleep } from 'k6';
import { login } from '../helpers/auth.js';
import { optionsDefault } from '../helpers/options.js';

export const options = optionsDefault;

const BASE = 'http://backend-test:8080';
const USUARIO = { nombre: 'lucas_fis', contrasenia: 'Gordo123!' };

export function testCerrarSesion(authHeaders) {
    const res = http.del(`${BASE}/sesion`, null, { headers: authHeaders });
    check(res, {
        '[logout] status 204': (r) => r.status === 204,
    });
}

export default function () {
    const token = login(BASE, USUARIO);
    if (!token) return;
    testCerrarSesion({ 'Cookie': `token=${token}` });
    sleep(1);
}
