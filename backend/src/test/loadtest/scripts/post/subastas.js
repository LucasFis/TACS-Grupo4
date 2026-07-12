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

export function testCrearSubasta(authHeaders) {
    const figuritas = fetchFiguritas(authHeaders);
    if (figuritas.length < 2) return;

    const idx1 = Math.floor(Math.random() * figuritas.length);
    let idx2 = Math.floor(Math.random() * figuritas.length);
    while (idx2 === idx1 && figuritas.length > 1) idx2 = Math.floor(Math.random() * figuritas.length);

    const res = http.post(`${BASE}/subastas`, JSON.stringify({
        figurita_id: figuritas[idx1].id,
        duracion_en_horas: 24,
        figuritas_deseadas_ids: [figuritas[idx2].id],
        calificacion_minima: 0,
    }), { headers: { ...authHeaders, 'Content-Type': 'application/json' } });
    check(res, {
        '[crear-subasta] status 201 o 4xx': (r) => r.status === 201 || r.status >= 400,
    });
}

export default function () {
    const token = login(BASE, USUARIO);
    if (!token) return;
    testCrearSubasta({ 'Cookie': `token=${token}` });
    sleep(1);
}
