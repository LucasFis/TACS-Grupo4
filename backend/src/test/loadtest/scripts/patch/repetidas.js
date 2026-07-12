import http from 'k6/http';
import { check, sleep } from 'k6';
import { login } from '../helpers/auth.js';
import { optionsEscrituraConEfectos } from '../helpers/options.js';
import { usuarioRandom } from '../helpers/usuarios.js';

export const options = optionsEscrituraConEfectos;

const BASE = 'http://backend-test:8080';

function fetchRepetidas(authHeaders) {
    const res = http.get(`${BASE}/colecciones/repetidas`, { headers: authHeaders });
    const body = res.json();
    return Array.isArray(body) ? body : [];
}

function pick(arr) {
    return arr[Math.floor(Math.random() * arr.length)];
}

export function testEditarRepetida(authHeaders) {
    const repetidas = fetchRepetidas(authHeaders);
    if (!repetidas.length) return;

    const rep = pick(repetidas);
    const res = http.patch(`${BASE}/colecciones/repetidas/${rep.figurita.id}`, JSON.stringify({
        cantidad_nueva: rep.cantidad_existente + 1,
        metodos: ['INTERCAMBIO', 'SUBASTA'],
    }), { headers: { ...authHeaders, 'Content-Type': 'application/json' } });
    check(res, {
        '[editar-repetida] status 204 o 4xx': (r) => r.status === 204 || r.status >= 400 && r.status < 500,
    });
}

export default function () {
    const token = login(BASE, usuarioRandom());
    if (!token) return;
    testEditarRepetida({ 'Cookie': `token=${token}` });
    sleep(1);
}
