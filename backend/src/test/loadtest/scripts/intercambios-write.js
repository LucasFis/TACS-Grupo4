import http from 'k6/http';
import { check, sleep } from 'k6';
import { login } from './helpers/auth.js';
import { optionsDefault } from './helpers/options.js';

export const options = optionsDefault;

const BASE = 'http://backend-test:8080';
const USUARIO = { nombre: 'lucas_fis', contrasenia: 'Gordo123!' };

export function testCrearPropuesta(authHeaders, destinatarioId, figuritaBuscadaId, figuritasOfrecidasIds) {
    const res = http.post(`${BASE}/propuestas`, JSON.stringify({
        destinatario_id: destinatarioId,
        figurita_buscada_id: figuritaBuscadaId,
        figuritas_ofrecidas_ids: figuritasOfrecidasIds
    }), { headers: { ...authHeaders, 'Content-Type': 'application/json' } });
    check(res, {
        '[crear-propuesta] status 201 o 4xx': (r) => r.status === 201 || r.status >= 400,
    });
}

export function testCancelarPropuesta(authHeaders, propId) {
    const res = http.patch(`${BASE}/propuestas/${propId}/cancelar`, null, { headers: authHeaders });
    check(res, {
        '[cancelar-propuesta] status 204 o 4xx': (r) => r.status === 204 || r.status >= 400,
    });
}

export function testAceptarPropuesta(authHeaders, propId) {
    const res = http.patch(`${BASE}/propuestas/${propId}/aceptar`, null, { headers: authHeaders });
    check(res, {
        '[aceptar-propuesta] status 204 o 4xx': (r) => r.status === 204 || r.status >= 400,
    });
}

export function testRechazarPropuesta(authHeaders, propId) {
    const res = http.patch(`${BASE}/propuestas/${propId}/rechazar`, null, { headers: authHeaders });
    check(res, {
        '[rechazar-propuesta] status 204 o 4xx': (r) => r.status === 204 || r.status >= 400,
    });
}

export function testCrearSubasta(authHeaders, figuritaId, figuritasDeseadasIds) {
    const res = http.post(`${BASE}/subastas`, JSON.stringify({
        figurita_id: figuritaId,
        duracion_en_horas: 24,
        figuritas_deseadas_ids: figuritasDeseadasIds,
        calificacion_minima: 0
    }), { headers: { ...authHeaders, 'Content-Type': 'application/json' } });
    check(res, {
        '[crear-subasta] status 201 o 4xx': (r) => r.status === 201 || r.status >= 400,
    });
    return res.status === 201 ? res.json() : null;
}

export function testOfertarEnSubasta(authHeaders, subId, figuritasOfrecidasIds) {
    const res = http.post(`${BASE}/subastas/${subId}/ofertas`, JSON.stringify({
        figuritas_ofrecidas_id: figuritasOfrecidasIds
    }), { headers: { ...authHeaders, 'Content-Type': 'application/json' } });
    check(res, {
        '[ofertar] status 201 o 4xx': (r) => r.status === 201 || r.status >= 400,
    });
}

export function testEditarOferta(authHeaders, subId, ofertaId, figuritasOfrecidasIds) {
    const res = http.patch(`${BASE}/subastas/${subId}/ofertas/${ofertaId}`, JSON.stringify({
        figuritas_ofrecidas_id: figuritasOfrecidasIds
    }), { headers: { ...authHeaders, 'Content-Type': 'application/json' } });
    check(res, {
        '[editar-oferta] status 204 o 4xx': (r) => r.status === 204 || r.status >= 400,
    });
}

export function testCancelarSubasta(authHeaders, subId) {
    const res = http.patch(`${BASE}/subastas/${subId}/cancelar`, null, { headers: authHeaders });
    check(res, {
        '[cancelar-subasta] status 204 o 4xx': (r) => r.status === 204 || r.status >= 400,
    });
}

export function testCerrarSubasta(authHeaders, subId) {
    const res = http.patch(`${BASE}/subastas/${subId}/cerrar`, null, { headers: authHeaders });
    check(res, {
        '[cerrar-subasta] status 204 o 4xx': (r) => r.status === 204 || r.status >= 400,
    });
}

export default function () {
    const token = login(BASE, USUARIO);
    if (!token) return;
    const authHeaders = { 'Cookie': `token=${token}` };

    const figRes = http.get(`${BASE}/figuritas`, { headers: authHeaders });
    const figuritas = figRes.json();
    if (!Array.isArray(figuritas) || figuritas.length < 2) return;

    const yoRes = http.get(`${BASE}/yo`, { headers: authHeaders });
    const yo = yoRes.json();
    const miId = yo && yo.id ? yo.id : 'self';

    testCrearPropuesta(authHeaders, miId, figuritas[0].id, [figuritas[1].id]);

    const propRes = http.get(`${BASE}/propuestas`, { headers: authHeaders });
    const propBody = propRes.json();
    if (propBody && propBody.contenido && propBody.contenido.length > 0) {
        const propId = propBody.contenido[0].id;
        testCancelarPropuesta(authHeaders, propId);
    }

    const subastaCreada = testCrearSubasta(authHeaders, figuritas[0].id, [figuritas[1].id]);

    const subRes = http.get(`${BASE}/subastas`, { headers: authHeaders });
    const subBody = subRes.json();
    if (subBody && subBody.contenido && subBody.contenido.length > 0) {
        const sub = subBody.contenido[0];
        testOfertarEnSubasta(authHeaders, sub.id, [figuritas[1].id]);
        testCerrarSubasta(authHeaders, sub.id);
    }

    sleep(1);
}
