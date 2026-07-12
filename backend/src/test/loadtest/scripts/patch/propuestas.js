import http from 'k6/http';
import { check, sleep } from 'k6';
import { login } from '../helpers/auth.js';
import { optionsEscrituraConEfectos } from '../helpers/options.js';
import { usuarioRandom } from '../helpers/usuarios.js';

export const options = optionsEscrituraConEfectos;

const BASE = 'http://backend-test:8080';

function fetchFiguritas(authHeaders) {
    const res = http.get(`${BASE}/figuritas`, { headers: authHeaders });
    const body = res.json();
    return Array.isArray(body) ? body : [];
}

function pick(arr) {
    return arr[Math.floor(Math.random() * arr.length)];
}

function fetchYo(authHeaders) {
    const res = http.get(`${BASE}/yo`, { headers: authHeaders });
    return res.json();
}

function fetchPropuestas(authHeaders) {
    const res = http.get(`${BASE}/propuestas`, { headers: authHeaders });
    const body = res.json();
    return body?.contenido || [];
}

export function testCancelarPropuesta(authHeaders) {
    const yo = fetchYo(authHeaders);
    const props = fetchPropuestas(authHeaders);
    const mia = props.filter(p => p.autor?.id === yo.id);
    if (!mia.length) return;

    const propId = pick(mia).id;
    const res = http.patch(`${BASE}/propuestas/${propId}/cancelar`, null, { headers: authHeaders });
    check(res, {
        '[cancelar-propuesta] status 204 o 4xx': (r) => r.status === 204 || r.status >= 400 && r.status < 500,
    });
}

export function testAceptarPropuesta(authHeaders) {
    const yo = fetchYo(authHeaders);
    const props = fetchPropuestas(authHeaders);
    const paraMi = props.filter(p => p.destinatario?.id === yo.id);
    if (!paraMi.length) return;

    const propId = pick(paraMi).id;
    const res = http.patch(`${BASE}/propuestas/${propId}/aceptar`, null, { headers: authHeaders });
    check(res, {
        '[aceptar-propuesta] status 204 o 4xx': (r) => r.status === 204 || r.status >= 400 && r.status < 500,
    });
}

export function testRechazarPropuesta(authHeaders) {
    const yo = fetchYo(authHeaders);
    const props = fetchPropuestas(authHeaders);
    const paraMi = props.filter(p => p.destinatario?.id === yo.id);
    if (!paraMi.length) return;

    const propId = pick(paraMi).id;
    const res = http.patch(`${BASE}/propuestas/${propId}/rechazar`, null, { headers: authHeaders });
    check(res, {
        '[rechazar-propuesta] status 204 o 4xx': (r) => r.status === 204 || r.status >= 400 && r.status < 500,
    });
}

export default function () {
    const token = login(BASE, usuarioRandom());
    if (!token) return;
    const authHeaders = { 'Cookie': `token=${token}` };

    const fns = [testCancelarPropuesta, testAceptarPropuesta, testRechazarPropuesta];
    fns[Math.floor(Math.random() * fns.length)](authHeaders);

    sleep(1);
}
