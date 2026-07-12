import http from 'k6/http';
import { check, sleep } from 'k6';
import { login } from '../helpers/auth.js';
import { optionsEscritura } from '../helpers/options.js';
import { usuarioRandom } from '../helpers/usuarios.js';

export const options = optionsEscritura;

const BASE = 'http://backend-test:8080';

export function testLogin() {
    const res = http.post(`${BASE}/login`, JSON.stringify(usuarioRandom()), {
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
