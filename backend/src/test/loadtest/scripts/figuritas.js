import http from 'k6/http';
import { check, sleep } from 'k6';
import { login } from './helpers/auth.js';
import {optionsDefault} from './helpers/options.js'
import {checkHttp} from "./helpers/checks.js";

export const options = optionsDefault

const BASE = 'http://backend-test:8080';
const USUARIO = { nombre: 'lucas_fis', contrasenia: 'Gordo123!' };

export function testFiguritas(authHeaders) {
    const res = http.get(`${BASE}/figuritas`, { headers: authHeaders });
    const body = res.json();

    checkHttp(res, 200);
    check(body, {
        '[figuritas] es un array':     (f) => Array.isArray(f),
        '[figuritas] no esta vacio':   (f) => f.length > 0,
        '[figuritas] con id definido': (f) => f.every(fig => fig.id !== null && fig.id !== ""),
        '[figuritas] con numero':      (f) => f.every(fig => typeof fig.numero === 'number'),
        '[figuritas] con jugador':     (f) => f.every(fig => typeof fig.jugador === 'string'),
    });
}

export default function () {
    const cookie = login(BASE, USUARIO);
    testFiguritas({ 'Cookie': `token=${cookie}` });
    sleep(1);
}