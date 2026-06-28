import http from 'k6/http';
import { check, sleep } from 'k6';
import { login } from './helpers/auth.js';
import {optionsDefault} from "./helpers/options.js";
import {checkHttp, checkPaginacion} from "./helpers/checks.js";

export const options = optionsDefault

const BASE = 'http://backend-test:8080';
const USUARIO = { nombre: 'lucas_fis', contrasenia: 'Gordo123!' };

export function testSubastas(authHeaders) {
    const res = http.get(`${BASE}/subastas`, { headers: authHeaders });
    const body = res.json();

    checkHttp(res, 200);
    checkPaginacion(body);
    check(body.contenido, {
        '[subastas] tienen id':                 (s) => s.every(sub => sub.id !== null && sub.id !== ""),
        '[subastas] tienen figurita subastada': (s) => s.every(sub => sub.figurita_subastada != null),
        '[subastas] tienen cierre':             (s) => s.every(sub => sub.fecha_inicio != null && sub.fecha_cierre != null),
    });
}

export default function () {
    const cookie = login(BASE, USUARIO);
    const authHeaders = { 'Cookie': `token=${cookie}` };

    testSubastas(authHeaders)

    sleep(1);
}