import http from 'k6/http';
import { check, sleep } from 'k6';
import { optionsDefault } from '../helpers/options.js';
import { usuarioRandom } from '../helpers/usuarios.js';

export const options = optionsDefault;

const BASE = 'http://backend-test:8080';

export function testVerificarNombre() {
    const res = http.head(`${BASE}/usuarios/${usuarioRandom().nombre}`);
    check(res, {
        '[head] status 200 o 404': (r) => r.status === 200 || r.status === 404,
    });
}

export default function () {
    testVerificarNombre();
    sleep(1);
}
