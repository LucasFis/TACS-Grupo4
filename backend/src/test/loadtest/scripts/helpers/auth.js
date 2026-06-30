import http from 'k6/http';
import { check } from 'k6';

export function login(base, usuario) {
    const res = http.post(
        `${base}/login`,
        JSON.stringify(usuario),
        { headers: { 'Content-Type': 'application/json' } }
    );

    const loginOk = check(res, {
        'login: status 204': (r) => r.status === 204,
        'login: Set-Cookie presente': (r) => r.headers['Set-Cookie'] !== undefined,
    });

    if (!loginOk) {
        console.error(`login falló — status: ${res.status}, body: ${res.body}`);
        return null;
    }

    const token = res.headers['Set-Cookie'].split(';')[0].split('=')[1];

    if (!token) {
        console.error(`login: no se pudo extraer token de Set-Cookie: ${res.headers['Set-Cookie']}`);
        return null;
    }

    return token;
}