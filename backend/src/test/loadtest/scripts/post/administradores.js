import http from 'k6/http';
import { check, sleep } from 'k6';
import { login } from '../helpers/auth.js';
import { optionsEscritura } from '../helpers/options.js';
import { usuarioAdmin } from '../helpers/usuarios.js';

export const options = optionsEscritura;

const BASE = 'http://backend-test:8080';

export function testRegistrarAdministrador(authHeaders) {
    const nombre = `lt_adm_${__VU}_${__ITER}`;
    const res = http.post(`${BASE}/administradores`, JSON.stringify({
        nombre: nombre,
        contrasenia: 'AdminLT1!',
        rol: 'ADMINISTRADOR',
    }), { headers: { ...authHeaders, 'Content-Type': 'application/json' } });
    check(res, {
        '[admin] status 204 o 403': (r) => r.status === 204 || r.status === 403,
    });
}

export default function () {
    const token = login(BASE, usuarioAdmin());
    if (!token) return;
    testRegistrarAdministrador({ 'Cookie': `token=${token}` });
    sleep(1);
}
