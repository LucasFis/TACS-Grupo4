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

function fetchYo(authHeaders) {
    const res = http.get(`${BASE}/yo`, { headers: authHeaders });
    return res.json();
}

function fetchSubastas(authHeaders) {
    const res = http.get(`${BASE}/subastas`, { headers: authHeaders });
    const body = res.json();
    return body?.contenido || [];
}

function pick(arr) {
    return arr[Math.floor(Math.random() * arr.length)];
}

export function testCancelarSubasta(authHeaders) {
    const yo = fetchYo(authHeaders);
    const subs = fetchSubastas(authHeaders);
    const mias = subs.filter(s => s.autor?.id === yo.id && !s.fecha_cierre);
    if (!mias.length) return;

    const subId = pick(mias).id;
    const res = http.patch(`${BASE}/subastas/${subId}/cancelar`, null, { headers: authHeaders });
    check(res, {
        '[cancelar-subasta] status 204 o 4xx': (r) => r.status === 204 || r.status >= 400 && r.status < 500,
    });
}

export function testCerrarSubasta(authHeaders) {
    const yo = fetchYo(authHeaders);
    const subs = fetchSubastas(authHeaders);
    const mias = subs.filter(s => s.autor?.id === yo.id && !s.fecha_cierre);
    if (!mias.length) return;

    const subId = pick(mias).id;
    const res = http.patch(`${BASE}/subastas/${subId}/cerrar`, null, { headers: authHeaders });
    check(res, {
        '[cerrar-subasta] status 204 o 4xx': (r) => r.status === 204 || r.status >= 400 && r.status < 500,
    });
}

export default function () {
    const token = login(BASE, usuarioRandom());
    if (!token) return;
    const authHeaders = { 'Cookie': `token=${token}` };

    const fns = [testCancelarSubasta, testCerrarSubasta];
    fns[Math.floor(Math.random() * fns.length)](authHeaders);

    sleep(1);
}
