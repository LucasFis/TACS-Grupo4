import http from 'k6/http';
import { check, sleep } from 'k6';
import { login } from '../helpers/auth.js';
import { optionsDefault } from '../helpers/options.js';

export const options = optionsDefault;

const BASE = 'http://backend-test:8080';
const USUARIO = { nombre: 'lucas_fis', contrasenia: 'Gordo123!' };

export function testEditarPerfil(authHeaders) {
    const res = http.put(`${BASE}/perfil`, JSON.stringify({
        nombre: 'Lucas',
        nombre_usuario: USUARIO.nombre,
        medios_de_contacto: [],
    }), { headers: { ...authHeaders, 'Content-Type': 'application/json' } });
    check(res, {
        '[editar-perfil] status 200 o 204': (r) => r.status === 200 || r.status === 204,
    });
}

export default function () {
    const token = login(BASE, USUARIO);
    if (!token) return;
    testEditarPerfil({ 'Cookie': `token=${token}` });
    sleep(1);
}
