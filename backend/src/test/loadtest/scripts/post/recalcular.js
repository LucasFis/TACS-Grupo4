import http from 'k6/http';
import { check, sleep } from 'k6';
import { login } from '../helpers/auth.js';
import { optionsDefault } from '../helpers/options.js';

export const options = optionsDefault;

const BASE = 'http://backend-test:8080';
const USUARIO = { nombre: 'lucas_fis', contrasenia: 'Gordo123!' };

export function testRecalcularSugerencias(authHeaders) {
    const res = http.post(`${BASE}/sugerencias/recalcular`, null, { headers: authHeaders });
    check(res, {
        '[recalcular] status 204': (r) => r.status === 204,
    });
}

export default function () {
    const token = login(BASE, USUARIO);
    if (!token) return;
    testRecalcularSugerencias({ 'Cookie': `token=${token}` });
    sleep(1);
}
