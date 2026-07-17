import http from 'k6/http';
import { check, sleep } from 'k6';
import { login } from '../helpers/auth.js';
import {optionsEscritura} from '../helpers/options.js';
import { usuarioRandom } from '../helpers/usuarios.js';

export const options = optionsEscritura;

const BASE = 'http://backend-test:8080';

const PERFILES = [
    'lucas-id-001',
    'sofia-id-001',
    'matias-id-001',
    'juan-id-001',
    'valentina-id-001',
    'diego-id-001',
];

function fetchFaltantesIds(authHeaders) {
    const res = http.get(`${BASE}/colecciones/faltantes`, { headers: authHeaders });
    const body = res.json();
    const contenido = body?.contenido || [];
    return contenido.map(f => f.id);
}

function fetchRepetidasIds(authHeaders) {
    const res = http.get(`${BASE}/colecciones/repetidas`, { headers: authHeaders });
    const body = res.json();
    return Array.isArray(body) ? body.map(r => r.figurita.id) : [];
}

function pick(arr) {
    return arr[Math.floor(Math.random() * arr.length)];
}

export function testCrearPropuesta(authHeaders) {
    const faltantesIds = fetchFaltantesIds(authHeaders);
    const repetidasIds = fetchRepetidasIds(authHeaders);
    if (!faltantesIds.length || !repetidasIds.length) return;

    const res = http.post(`${BASE}/propuestas`, JSON.stringify({
        destinatario_id: pick(PERFILES),
        figurita_buscada_id: pick(faltantesIds),
        figuritas_ofrecidas_ids: [pick(repetidasIds)],
    }), { headers: { ...authHeaders, 'Content-Type': 'application/json' } });
    check(res, {
        '[crear-propuesta] status 201 o 4xx': (r) => r.status === 201 || r.status >= 400 && r.status < 500,
    });
}

export default function () {
    const token = login(BASE, usuarioRandom());
    if (!token) return;
    testCrearPropuesta({ 'Cookie': `token=${token}` });
    sleep(1);
}
