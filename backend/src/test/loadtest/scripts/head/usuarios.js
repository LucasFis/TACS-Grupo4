import http from 'k6/http';
import { check, sleep } from 'k6';
import { optionsDefault } from '../helpers/options.js';

export const options = optionsDefault;

const BASE = 'http://backend-test:8080';
const USUARIO = { nombre: 'lucas_fis', contrasenia: 'Gordo123!' };

export function testVerificarNombre() {
    const res = http.head(`${BASE}/usuarios/${USUARIO.nombre}`);
    check(res, {
        '[head] status 200 o 404': (r) => r.status === 200 || r.status === 404,
    });
}

export default function () {
    testVerificarNombre();
    sleep(1);
}
