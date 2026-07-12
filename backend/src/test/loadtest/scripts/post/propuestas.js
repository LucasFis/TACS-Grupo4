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

export function testCrearPropuesta(authHeaders) {
    const figuritas = fetchFiguritas(authHeaders);
    if (figuritas.length < 2) return;

    const yoRes = http.get(`${BASE}/yo`, { headers: authHeaders });
    const yo = yoRes.json();
    const miId = yo?.id || 'self';

    const idx1 = Math.floor(Math.random() * figuritas.length);
    let idx2 = Math.floor(Math.random() * figuritas.length);
    while (idx2 === idx1 && figuritas.length > 1) idx2 = Math.floor(Math.random() * figuritas.length);

    const res = http.post(`${BASE}/propuestas`, JSON.stringify({
        destinatario_id: miId,
        figurita_buscada_id: figuritas[idx1].id,
        figuritas_ofrecidas_ids: [figuritas[idx2].id],
    }), { headers: { ...authHeaders, 'Content-Type': 'application/json' } });
    check(res, {
        '[crear-propuesta] status 201 o 4xx': (r) => r.status === 201 || r.status >= 400,
    });
}

export default function () {
    const token = login(BASE, usuarioRandom());
    if (!token) return;
    testCrearPropuesta({ 'Cookie': `token=${token}` });
    sleep(1);
}
