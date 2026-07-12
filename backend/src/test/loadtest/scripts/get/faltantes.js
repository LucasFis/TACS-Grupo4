import http from 'k6/http';
import { check, sleep } from 'k6';
import { login } from '../helpers/auth.js';
import { optionsDefault } from '../helpers/options.js';
import { usuarioRandom } from '../helpers/usuarios.js';
import { checkHttp, checkPaginacion } from '../helpers/checks.js';

export const options = optionsDefault;

const BASE = 'http://backend-test:8080';

export function testMisFaltantes(authHeaders) {
    const res = http.get(`${BASE}/colecciones/faltantes`, { headers: authHeaders });
    const body = res.json();

    checkHttp(res, 200);
    checkPaginacion(body);
    check(body.contenido, {
        '[faltantes] con id definido': (f) => f.every(fig => fig.id !== null && fig.id !== ""),
        '[faltantes] con numero':      (f) => f.every(fig => typeof fig.numero === 'number'),
        '[faltantes] con jugador':     (f) => f.every(fig => typeof fig.jugador === 'string'),
    });
}

export default function () {
    const token = login(BASE, usuarioRandom());
    if (!token) return;
    testMisFaltantes({ 'Cookie': `token=${token}` });
    sleep(1);
}
