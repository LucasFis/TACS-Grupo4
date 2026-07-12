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

export function testEditarOferta(authHeaders) {
    const figuritas = fetchFiguritas(authHeaders);
    if (figuritas.length === 0) return;

    const subRes = http.get(`${BASE}/subastas`, { headers: authHeaders });
    const subBody = subRes.json();
    if (!subBody?.contenido?.length) return;

    const sub = pick(subBody.contenido);
    if (!sub.ofertas?.length) return;

    const ofertaId = pick(sub.ofertas).id;
    const figuritaOfrecida = pick(figuritas).id;

    const res = http.patch(`${BASE}/subastas/${sub.id}/ofertas/${ofertaId}`, JSON.stringify({
        figuritas_ofrecidas_id: [figuritaOfrecida],
    }), { headers: { ...authHeaders, 'Content-Type': 'application/json' } });
    check(res, {
        '[editar-oferta] status 204 o 4xx': (r) => r.status === 204 || r.status >= 400,
    });
}

export default function () {
    const token = login(BASE, USUARIO);
    if (!token) return;
    testEditarOferta({ 'Cookie': `token=${token}` });
    sleep(1);
}
