import http from 'k6/http';
import { check, sleep } from 'k6';
import { login } from './helpers/auth.js';
import {optionsDefault} from "./helpers/options.js";
import {checkHttp, checkPaginacion} from "./helpers/checks.js";

export const options = optionsDefault

const BASE = 'http://backend-test:8080';
const USUARIO = { nombre: 'lucas_fis', contrasenia: 'Gordo123!' };

export function testSugerencias(authHeaders) {
    const res = http.get(`${BASE}/perfil/sugerencias`, { headers: authHeaders });
    const body = res.json();

    checkHttp(res, 200);
    checkPaginacion(body);
    check(body.contenido, {
        '[sugerencias] tiene recomendadas': (s) => s.every(c => Array.isArray(c.figuritas_recomendadas)),
        '[sugerencias] tiene necesarias':   (s) => s.every(c => Array.isArray(c.figuritas_necesarias)),
        '[sugerencias] tiene perfil':       (s) => s.every(c => c.perfil != null),
    });
}

export default function () {
    const cookie = login(BASE, USUARIO);
    const authHeaders = { 'Cookie': `token=${cookie}` };

    testSugerencias(authHeaders)

    sleep(1);
}