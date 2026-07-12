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

function pick(arr) {
    return arr[Math.floor(Math.random() * arr.length)];
}

export function testOfertarEnSubasta(authHeaders) {
    const figRes = http.get(`${BASE}/figuritas`, { headers: authHeaders });
    const figuritas = figRes.json();
    if (!Array.isArray(figuritas) || figuritas.length === 0) return;

    const subRes = http.get(`${BASE}/subastas`, { headers: authHeaders });
    const subBody = subRes.json();
    if (!subBody?.contenido?.length) return;

    const subId = pick(subBody.contenido).id;
    const figuritaOfrecida = pick(figuritas).id;

    const res = http.post(`${BASE}/subastas/${subId}/ofertas`, JSON.stringify({
        figuritas_ofrecidas_id: [figuritaOfrecida],
    }), { headers: { ...authHeaders, 'Content-Type': 'application/json' } });
    check(res, {
        '[ofertar] status 201 o 4xx': (r) => r.status === 201 || r.status >= 400,
    });
}

export default function () {
    const token = login(BASE, usuarioRandom());
    if (!token) return;
    testOfertarEnSubasta({ 'Cookie': `token=${token}` });
    sleep(1);
}
