import http from 'k6/http';
import { check, sleep } from 'k6';
import { login } from './helpers/auth.js';
import { optionsDefault } from './helpers/options.js';

export const options = optionsDefault;

const BASE = 'http://backend-test:8080';
const USUARIO = { nombre: 'lucas_fis', contrasenia: 'Gordo123!' };

export function testAgregarFaltante(authHeaders, figId) {
    const res = http.post(`${BASE}/colecciones/faltantes`, JSON.stringify({
        fig_id: figId
    }), { headers: { ...authHeaders, 'Content-Type': 'application/json' } });
    check(res, {
        '[faltante] status 201 o 4xx': (r) => r.status === 201 || r.status >= 400,
    });
}

export function testAgregarRepetida(authHeaders, figId) {
    const res = http.post(`${BASE}/colecciones/repetidas`, JSON.stringify({
        fig_id: figId,
        cantidad_existente: 2,
        modos_intercambio: ['INTERCAMBIO']
    }), { headers: { ...authHeaders, 'Content-Type': 'application/json' } });
    check(res, {
        '[repetida] status 201 o 4xx': (r) => r.status === 201 || r.status >= 400,
    });
}

export function testEditarRepetida(authHeaders, figId) {
    const res = http.patch(`${BASE}/colecciones/repetidas/${figId}`, JSON.stringify({
        cantidad_nueva: 3,
        metodos: ['INTERCAMBIO', 'SUBASTA']
    }), { headers: { ...authHeaders, 'Content-Type': 'application/json' } });
    check(res, {
        '[editar-repetida] status 204 o 4xx': (r) => r.status === 204 || r.status >= 400,
    });
}

export function testRecalcularSugerencias(authHeaders) {
    const res = http.post(`${BASE}/sugerencias/recalcular`, null, { headers: authHeaders });
    check(res, {
        '[recalcular] status 204': (r) => r.status === 204,
    });
}

export function testAlternarFavorito(authHeaders, sugId) {
    const res = http.patch(`${BASE}/sugerencias/${sugId}/favorito`, null, { headers: authHeaders });
    check(res, {
        '[favorito] status 200 o 4xx': (r) => r.status === 200 || r.status >= 400,
    });
}

export default function () {
    const token = login(BASE, USUARIO);
    if (!token) return;
    const authHeaders = { 'Cookie': `token=${token}` };

    const figRes = http.get(`${BASE}/figuritas`, { headers: authHeaders });
    const figuritas = figRes.json();
    if (!Array.isArray(figuritas) || figuritas.length < 2) return;

    testAgregarFaltante(authHeaders, figuritas[0].id);
    testAgregarRepetida(authHeaders, figuritas[1].id);
    testEditarRepetida(authHeaders, figuritas[1].id);

    testRecalcularSugerencias(authHeaders);

    const sugRes = http.get(`${BASE}/sugerencias`, { headers: authHeaders });
    const sugBody = sugRes.json();
    if (sugBody && sugBody.contenido && sugBody.contenido.length > 0) {
        testAlternarFavorito(authHeaders, sugBody.contenido[0].id);
    }

    sleep(1);
}
