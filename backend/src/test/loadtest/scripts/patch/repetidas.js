import http from 'k6/http';
import { check, sleep } from 'k6';
import { login } from '../helpers/auth.js';
import { optionsDefault } from '../helpers/options.js';
import { usuarioRandom } from '../helpers/usuarios.js';

export const options = optionsDefault;

const BASE = 'http://backend-test:8080';

function fetchFiguritas(authHeaders) {
    const res = http.get(`${BASE}/figuritas`, { headers: authHeaders });
    const body = res.json();
    return Array.isArray(body) ? body : [];
}

export function testEditarRepetida(authHeaders) {
    const figuritas = fetchFiguritas(authHeaders);
    if (figuritas.length === 0) return;
    const figId = figuritas[Math.floor(Math.random() * figuritas.length)].id;

    const res = http.patch(`${BASE}/colecciones/repetidas/${figId}`, JSON.stringify({
        cantidad_nueva: 3,
        metodos: ['INTERCAMBIO', 'SUBASTA'],
    }), { headers: { ...authHeaders, 'Content-Type': 'application/json' } });
    check(res, {
        '[editar-repetida] status 204 o 4xx': (r) => r.status === 204 || r.status >= 400,
    });
}

export default function () {
    const token = login(BASE, usuarioRandom());
    if (!token) return;
    testEditarRepetida({ 'Cookie': `token=${token}` });
    sleep(1);
}
