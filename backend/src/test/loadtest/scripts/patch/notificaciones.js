import http from 'k6/http';
import { check, sleep } from 'k6';
import { login } from '../helpers/auth.js';
import { optionsDefault } from '../helpers/options.js';
import { usuarioRandom } from '../helpers/usuarios.js';

export const options = optionsDefault;

const BASE = 'http://backend-test:8080';

export function testMarcarLeidas(authHeaders) {
    const res = http.patch(`${BASE}/perfil/notificaciones/leidas`, null, { headers: authHeaders });
    check(res, {
        '[leidas] status 204': (r) => r.status === 204,
    });
}

export default function () {
    const token = login(BASE, usuarioRandom());
    if (!token) return;
    testMarcarLeidas({ 'Cookie': `token=${token}` });
    sleep(1);
}
