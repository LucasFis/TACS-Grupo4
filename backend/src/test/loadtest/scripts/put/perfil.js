import http from 'k6/http';
import { check, sleep } from 'k6';
import { login } from '../helpers/auth.js';
import { optionsDefault } from '../helpers/options.js';
import { usuarioRandom } from '../helpers/usuarios.js';

export const options = optionsDefault;

const BASE = 'http://backend-test:8080';

export function testEditarPerfil(authHeaders, usuario) {
    const res = http.put(`${BASE}/perfil`, JSON.stringify({
        nombre: 'Lucas',
        nombre_usuario: usuario.nombre,
        medios_de_contacto: [],
    }), { headers: { ...authHeaders, 'Content-Type': 'application/json' } });
    check(res, {
        '[editar-perfil] status 200 o 204': (r) => r.status === 200 || r.status === 204,
    });
}

export default function () {
    const usuario = usuarioRandom();
    const token = login(BASE, usuario);
    if (!token) return;
    testEditarPerfil({ 'Cookie': `token=${token}` }, usuario);
    sleep(1);
}
