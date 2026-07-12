import http from 'k6/http';
import { check, sleep } from 'k6';
import { optionsEscritura } from '../helpers/options.js';

export const options = optionsEscritura;

const BASE = 'http://backend-test:8080';

export function testRegistrarUsuario() {
    const nombre = `lt_${__VU}_${__ITER}`;
    const res = http.post(`${BASE}/usuarios`, JSON.stringify({
        nombre: nombre,
        contrasenia: 'Loadtest1!',
        rol: 'USUARIO',
    }), { headers: { 'Content-Type': 'application/json' } });
    check(res, {
        '[registrar] status 204': (r) => r.status === 204,
    });
}

export default function () {
    testRegistrarUsuario();
    sleep(1);
}
