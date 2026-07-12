import http from 'k6/http';
import { check, sleep } from 'k6';
import { login } from '../helpers/auth.js';
import { optionsDefault } from '../helpers/options.js';

export const options = optionsDefault;

const BASE = 'http://backend-test:8080';
const USUARIO = { nombre: 'lucas_fis', contrasenia: 'Gordo123!' };

export function testLogin() {
    const res = http.post(`${BASE}/login`, JSON.stringify(USUARIO), {
        headers: { 'Content-Type': 'application/json' },
    });
    check(res, {
        'login: status 204': (r) => r.status === 204,
        'login: Set-Cookie presente': (r) => r.headers['Set-Cookie'] !== undefined,
    });
}

export default function () {
    testLogin();
    sleep(1);
}
