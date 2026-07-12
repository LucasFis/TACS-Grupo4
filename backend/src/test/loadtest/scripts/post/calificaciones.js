import http from 'k6/http';
import { check, sleep } from 'k6';
import { login } from '../helpers/auth.js';
import { optionsEscrituraConEfectos } from '../helpers/options.js';
import { usuarioRandom } from '../helpers/usuarios.js';

export const options = optionsEscrituraConEfectos;

const BASE = 'http://backend-test:8080';

const PERFILES = [
    'lucas-id-001',
    'sofia-id-001',
    'matias-id-001',
    'juan-id-001',
    'valentina-id-001',
    'diego-id-001',
];

function pick(arr) {
    return arr[Math.floor(Math.random() * arr.length)];
}

export function testCalificarPerfil(authHeaders) {
    const res = http.post(`${BASE}/perfil/calificaciones`, JSON.stringify({
        valor: 5,
        destinatario_id: pick(PERFILES),
        transaction_id: `tx_${__VU}_${__ITER}_${Date.now()}`,
        tipo_transaccion: 'INTERCAMBIO',
    }), { headers: { ...authHeaders, 'Content-Type': 'application/json' } });
    errorLog(res)
    check(res, {
        '[calificar] responde': (r) => r.status >= 200 && r.status < 500,
    });
}

function errorLog(res) {
    if (res.status>= 400) console.log(res.body);
}

export default function () {
    const token = login(BASE, usuarioRandom());
    if (!token) return;
    testCalificarPerfil({ 'Cookie': `token=${token}` });
    sleep(1);
}
