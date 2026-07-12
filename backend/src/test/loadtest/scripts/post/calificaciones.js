import http from 'k6/http';
import { check, sleep } from 'k6';
import { login } from '../helpers/auth.js';
import { optionsDefault } from '../helpers/options.js';
import { usuarioRandom } from '../helpers/usuarios.js';

export const options = optionsDefault;

const BASE = 'http://backend-test:8080';

export function testCalificarPerfil(authHeaders) {
    const res = http.post(`${BASE}/perfil/calificaciones`, JSON.stringify({
        valor: 5,
        destinatario_id: 'nonexistent',
        transaction_id: 'fake_tx',
        tipo_transaccion: 'INTERCAMBIO',
    }), { headers: { ...authHeaders, 'Content-Type': 'application/json' } });
    check(res, {
        '[calificar] responde': (r) => r.status >= 200,
    });
}

export default function () {
    const token = login(BASE, usuarioRandom());
    if (!token) return;
    testCalificarPerfil({ 'Cookie': `token=${token}` });
    sleep(1);
}
