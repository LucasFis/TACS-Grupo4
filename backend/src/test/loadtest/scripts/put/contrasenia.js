import http from 'k6/http';
import { check, sleep } from 'k6';
import { login } from '../helpers/auth.js';
import { optionsEscrituraConEfectos } from '../helpers/options.js';
import { usuarioRandom } from '../helpers/usuarios.js';

export const options = optionsEscrituraConEfectos;

const BASE = 'http://backend-test:8080';

export function testEditarContrasenia(authHeaders, usuario) {
    const res = http.put(`${BASE}/usuarios/contrasenia`, JSON.stringify({
        contrasenia_actual: usuario.contrasenia,
        contrasenia_nueva: 'NewLT1234!',
    }), { headers: { ...authHeaders, 'Content-Type': 'application/json' } });
    check(res, {
        '[contrasenia] status 204 o 400': (r) => r.status === 204 || r.status === 400,
    });
    if (res.status === 204) {
        http.put(`${BASE}/usuarios/contrasenia`, JSON.stringify({
            contrasenia_actual: 'NewLT1234!',
            contrasenia_nueva: usuario.contrasenia,
        }), { headers: { ...authHeaders, 'Content-Type': 'application/json' } });
    }
}

export default function () {
    const usuario = usuarioRandom();
    const token = login(BASE, usuario);
    if (!token) return;
    testEditarContrasenia({ 'Cookie': `token=${token}` }, usuario);
    sleep(1);
}
