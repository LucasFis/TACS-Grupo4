import http from 'k6/http';
import { check, sleep } from 'k6';
import { login } from '../helpers/auth.js';
import { optionsDefault } from '../helpers/options.js';

export const options = optionsDefault;

const BASE = 'http://backend-test:8080';
const USUARIO = { nombre: 'lucas_fis', contrasenia: 'Gordo123!' };

function fetchFiguritas(authHeaders) {
    const res = http.get(`${BASE}/figuritas`, { headers: authHeaders });
    const body = res.json();
    return Array.isArray(body) ? body : [];
}

export function testAgregarRepetida(authHeaders) {
    const figuritas = fetchFiguritas(authHeaders);
    if (figuritas.length === 0) return;
    const figId = figuritas[Math.floor(Math.random() * figuritas.length)].id;

    const res = http.post(`${BASE}/colecciones/repetidas`, JSON.stringify({
        fig_id: figId,
        cantidad_existente: 2,
        modos_intercambio: ['INTERCAMBIO'],
    }), { headers: { ...authHeaders, 'Content-Type': 'application/json' } });
    check(res, {
        '[repetida] status 201 o 4xx': (r) => r.status === 201 || r.status >= 400,
    });
}

export default function () {
    const token = login(BASE, USUARIO);
    if (!token) return;
    testAgregarRepetida({ 'Cookie': `token=${token}` });
    sleep(1);
}
