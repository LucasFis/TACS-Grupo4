import http from 'k6/http';
import { check, sleep } from 'k6';
import { login } from '../helpers/auth.js';
import { optionsEscritura } from '../helpers/options.js';
import { usuarioRandom } from '../helpers/usuarios.js';

export const options = optionsEscritura;

const BASE = 'http://backend-test:8080';

function fetchRepetidasSubasta(authHeaders) {
    const res = http.get(`${BASE}/colecciones/repetidas`, { headers: authHeaders });
    const body = res.json();
    if (!Array.isArray(body)) return [];
    return body
        .filter(r => r.metodos?.includes('SUBASTA') && (r.cantidad_existente - r.cantidad_reservada) > 0)
        .map(r => r.figurita.id);
}

function fetchFaltantesIds(authHeaders) {
    const res = http.get(`${BASE}/colecciones/faltantes`, { headers: authHeaders });
    const body = res.json();
    return body?.contenido?.map(f => f.id) || [];
}

function pick(arr) {
    return arr[Math.floor(Math.random() * arr.length)];
}

export function testCrearSubasta(authHeaders) {
    const subastables = fetchRepetidasSubasta(authHeaders);
    const faltantesIds = fetchFaltantesIds(authHeaders);
    if (!subastables.length || !faltantesIds.length) return;

    const res = http.post(`${BASE}/subastas`, JSON.stringify({
        figurita_id: pick(subastables),
        duracion_en_horas: 24,
        figuritas_deseadas_ids: [pick(faltantesIds)],
        calificacion_minima: 0,
    }), { headers: { ...authHeaders, 'Content-Type': 'application/json' } });
    check(res, {
        '[crear-subasta] status 201 o 4xx': (r) => r.status === 201 || r.status >= 400 && r.status < 500,
    });
}

export default function () {
    const token = login(BASE, usuarioRandom());
    if (!token) return;
    testCrearSubasta({ 'Cookie': `token=${token}` });
    sleep(1);
}
