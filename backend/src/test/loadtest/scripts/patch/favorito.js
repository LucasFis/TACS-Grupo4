import http from 'k6/http';
import { check, sleep } from 'k6';
import { login } from '../helpers/auth.js';
import { optionsEscrituraConEfectos } from '../helpers/options.js';
import { usuarioRandom } from '../helpers/usuarios.js';

export const options = optionsEscrituraConEfectos;

const BASE = 'http://backend-test:8080';

function pick(arr) {
    return arr[Math.floor(Math.random() * arr.length)];
}

export function testAlternarFavorito(authHeaders) {
    const sugRes = http.get(`${BASE}/sugerencias`, { headers: authHeaders });
    check(sugRes, { 'sugerencias: status 200': (r) => r.status === 200 });
    const body = sugRes.json();
    if (!body?.contenido?.length) return;

    const sugId = pick(body.contenido).id;
    const res = http.patch(`${BASE}/sugerencias/${sugId}/favorito`, null, { headers: authHeaders });
    check(res, {
        '[favorito] status 204 o 4xx': (r) => r.status === 204 || r.status >= 400 && r.status < 500,
    });
}

export default function () {
    const token = login(BASE, usuarioRandom());
    if (!token) return;
    testAlternarFavorito({ 'Cookie': `token=${token}` });
    sleep(1);
}
