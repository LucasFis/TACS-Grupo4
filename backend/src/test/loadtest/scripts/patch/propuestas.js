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

function pick(arr) {
    return arr[Math.floor(Math.random() * arr.length)];
}

function crearPropuesta(authHeaders, figuritas) {
    const yoRes = http.get(`${BASE}/yo`, { headers: authHeaders });
    const yo = yoRes.json();
    const miId = yo?.id || 'self';

    http.post(`${BASE}/propuestas`, JSON.stringify({
        destinatario_id: miId,
        figurita_buscada_id: pick(figuritas).id,
        figuritas_ofrecidas_ids: [pick(figuritas).id],
    }), { headers: { ...authHeaders, 'Content-Type': 'application/json' } });
}

export function testCancelarPropuesta(authHeaders) {
    const figuritas = fetchFiguritas(authHeaders);
    if (figuritas.length === 0) return;

    crearPropuesta(authHeaders, figuritas);

    const propRes = http.get(`${BASE}/propuestas`, { headers: authHeaders });
    const propBody = propRes.json();
    if (!propBody?.contenido?.length) return;

    const propId = pick(propBody.contenido).id;
    const res = http.patch(`${BASE}/propuestas/${propId}/cancelar`, null, { headers: authHeaders });
    check(res, {
        '[cancelar-propuesta] status 204 o 4xx': (r) => r.status === 204 || r.status >= 400,
    });
}

export function testAceptarPropuesta(authHeaders) {
    const figuritas = fetchFiguritas(authHeaders);
    if (figuritas.length === 0) return;

    crearPropuesta(authHeaders, figuritas);

    const propRes = http.get(`${BASE}/propuestas`, { headers: authHeaders });
    const propBody = propRes.json();
    if (!propBody?.contenido?.length) return;

    const propId = pick(propBody.contenido).id;
    const res = http.patch(`${BASE}/propuestas/${propId}/aceptar`, null, { headers: authHeaders });
    check(res, {
        '[aceptar-propuesta] status 204 o 4xx': (r) => r.status === 204 || r.status >= 400,
    });
}

export function testRechazarPropuesta(authHeaders) {
    const figuritas = fetchFiguritas(authHeaders);
    if (figuritas.length === 0) return;

    crearPropuesta(authHeaders, figuritas);

    const propRes = http.get(`${BASE}/propuestas`, { headers: authHeaders });
    const propBody = propRes.json();
    if (!propBody?.contenido?.length) return;

    const propId = pick(propBody.contenido).id;
    const res = http.patch(`${BASE}/propuestas/${propId}/rechazar`, null, { headers: authHeaders });
    check(res, {
        '[rechazar-propuesta] status 204 o 4xx': (r) => r.status === 204 || r.status >= 400,
    });
}

export default function () {
    const token = login(BASE, USUARIO);
    if (!token) return;
    const authHeaders = { 'Cookie': `token=${token}` };

    const fns = [testCancelarPropuesta, testAceptarPropuesta, testRechazarPropuesta];
    fns[Math.floor(Math.random() * fns.length)](authHeaders);

    sleep(1);
}
