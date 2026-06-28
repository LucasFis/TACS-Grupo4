import http from 'k6/http';

export function login(base, usuario) {
    const res = http.post(
        `${base}/login`,
        JSON.stringify(usuario),
        { headers: { 'Content-Type': 'application/json' } }
    );
    return res.headers['Set-Cookie'].split(';')[0].split('=')[1];
}