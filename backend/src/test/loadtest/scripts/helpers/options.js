// ─── Perfil LECTURA ────────────────────────────────────────────────────────
// GET / HEAD — sin efectos secundarios, aguanta carga alta.
export const optionsLectura = {
    stages: [
        { duration: '10s', target: 100 },
        { duration: '15s', target: 250 },
        { duration: '20s', target: 200 },
        { duration: '15s', target: 50 },
        { duration: '5s',  target: 0 },
    ],
    thresholds: {
        'http_req_failed': ['rate<0.01'],
        'checks':         ['rate>0.95'],
    },
};

// ─── Perfil ESCRITURA ─────────────────────────────────────────────────────
// POST / PUT que crean recursos nuevos o modifican datos simples.
// VUs moderados, thresholds un poco más relajados.
export const optionsEscritura = {
    stages: [
        { duration: '10s', target: 50 },
        { duration: '15s', target: 100 },
        { duration: '20s', target: 80 },
        { duration: '15s', target: 25 },
        { duration: '5s',  target: 0 },
    ],
    thresholds: {
        'http_req_failed': ['rate<0.05'],
        'checks':         ['rate>0.95'],
    },
};

// ─── Perfil ESCRITURA CON EFECTOS ─────────────────────────────────────────
// PATCH / DELETE que modifican estado existente (propuestas, subastas,
// colecciones, etc.). Menos VUs para reducir competencia por locks.
export const optionsEscrituraConEfectos = {
    stages: [
        { duration: '10s', target: 20 },
        { duration: '15s', target: 50 },
        { duration: '20s', target: 40 },
        { duration: '15s', target: 10 },
        { duration: '5s',  target: 0 },
    ],
    thresholds: {
        'http_req_failed': ['rate<0.1'],
        'checks':         ['rate>0.90'],
    },
};

// ─── Perfil PROBLEMÁTICO ──────────────────────────────────────────────────
// Endpoints con race conditions conocidas (notificaciones) o CPU-intensive
// (recalcular). VUs muy bajos, thresholds más permisivos.
export const optionsProblematico = {
    stages: [
        { duration: '10s', target: 10 },
        { duration: '15s', target: 25 },
        { duration: '20s', target: 20 },
        { duration: '15s', target: 5 },
        { duration: '5s',  target: 0 },
    ],
    thresholds: {
        'http_req_failed': ['rate<0.1'],
        'checks':         ['rate>0.85'],
    },
};

// Backward compatibility
export const optionsDefault = optionsLectura;
