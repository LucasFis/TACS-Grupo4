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

function crearSubasta(authHeaders, figuritas) {
    const idx1 = Math.floor(Math.random() * figuritas.length);
    let idx2 = Math.floor(Math.random() * figuritas.length);
    while (idx2 === idx1 && figuritas.length > 1) idx2 = Math.floor(Math.random() * figuritas.length);

    const res = http.post(`${BASE}/subastas`, JSON.stringify({
        figurita_id: figuritas[idx1].id,
        duracion_en_horas: 24,
        figuritas_deseadas_ids: [figuritas[idx2].id],
        calificacion_minima: 0,
    }), { headers: { ...authHeaders, 'Content-Type': 'application/json' } });
    return res.status === 201 ? res.json() : null;
}

export function testCancelarSubasta(authHeaders) {
    const figuritas = fetchFiguritas(authHeaders);
    if (figuritas.length === 0) return;

    crearSubasta(authHeaders, figuritas);

    const subRes = http.get(`${BASE}/subastas`, { headers: authHeaders });
    const subBody = subRes.json();
    if (!subBody?.contenido?.length) return;

    const subId = pick(subBody.contenido).id;
    const res = http.patch(`${BASE}/subastas/${subId}/cancelar`, null, { headers: authHeaders });
    check(res, {
        '[cancelar-subasta] status 204 o 4xx': (r) => r.status === 204 || r.status >= 400,
    });
}

export function testCerrarSubasta(authHeaders) {
    const figuritas = fetchFiguritas(authHeaders);
    if (figuritas.length === 0) return;

    crearSubasta(authHeaders, figuritas);

    const subRes = http.get(`${BASE}/subastas`, { headers: authHeaders });
    const subBody = subRes.json();
    if (!subBody?.contenido?.length) return;

    const subId = pick(subBody.contenido).id;
    const res = http.patch(`${BASE}/subastas/${subId}/cerrar`, null, { headers: authHeaders });
    check(res, {
        '[cerrar-subasta] status 204 o 4xx': (r) => r.status === 204 || r.status >= 400,
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
