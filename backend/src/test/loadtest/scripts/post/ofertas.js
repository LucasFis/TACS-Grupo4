import http from 'k6/http';
import { check, sleep } from 'k6';
import { login } from '../helpers/auth.js';
import { optionsEscritura } from '../helpers/options.js';
import { usuarioRandom } from '../helpers/usuarios.js';

export const options = optionsEscritura;

const BASE = 'http://backend-test:8080';

function fetchFaltantesIds(authHeaders) {
    const res = http.get(`${BASE}/colecciones/faltantes`, { headers: authHeaders });
    const body = res.json();
    const contenido = body?.contenido || [];
    return new Set(contenido.map(f => f.id));
}

function fetchRepetidasIds(authHeaders) {
    const res = http.get(`${BASE}/colecciones/repetidas`, { headers: authHeaders });
    const body = res.json();
    return Array.isArray(body) ? body.map(r => r.figurita.id) : [];
}

function fetchSubastas(authHeaders) {
    const res = http.get(`${BASE}/subastas`, { headers: authHeaders });
    const body = res.json();
    return body?.contenido || [];
}

function pick(arr) {
    return arr[Math.floor(Math.random() * arr.length)];
}

export function testOfertarEnSubasta(authHeaders) {
    const faltantesIds = fetchFaltantesIds(authHeaders);
    const repetidasIds = fetchRepetidasIds(authHeaders);
    if (!repetidasIds.length) return;

    const subs = fetchSubastas(authHeaders);
    const abiertas = subs.filter(s => !s.fecha_cierre && faltantesIds.has(s.figurita_subastada?.id));
    if (!abiertas.length) return;

    const subId = pick(abiertas).id;
    const figuritaOfrecida = pick(repetidasIds);

    const res = http.post(`${BASE}/subastas/${subId}/ofertas`, JSON.stringify({
        figuritas_ofrecidas_id: [figuritaOfrecida],
    }), { headers: { ...authHeaders, 'Content-Type': 'application/json' } });
    check(res, {
        '[ofertar] status 201 o 4xx': (r) => r.status === 201 || r.status >= 400 && r.status < 500,
    });
}

export default function () {
    const token = login(BASE, usuarioRandom());
    if (!token) return;
    testOfertarEnSubasta({ 'Cookie': `token=${token}` });
    sleep(1);
}
