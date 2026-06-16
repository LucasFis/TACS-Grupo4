const FaltanteCard = ({ figurita }) => {
    const { id, numero, jugador, seleccion, imagen_url } = figurita;

    const initials = jugador
        .split(' ')
        .map((w) => w[0])
        .join('')
        .slice(0, 2)
        .toUpperCase();

    return (
        <div className="card" style={{ width: '220px' }}>

            <div className="card-header bg-white border-bottom d-flex justify-content-between align-items-center py-2 px-3">
                <span className="text-muted" style={{ fontSize: '0.75rem' }}>
                    #{id}
                </span>
                <span
                    className="badge rounded-pill"
                    style={{ backgroundColor: '#e6f1fb', color: '#185fa5', fontSize: '0.7rem' }}
                >
                    faltante
                </span>
                <img
                    src={imagen_url || '/jugador-placeholder.png'}
                    alt={jugador}
                    className="rounded-2"
                    style={{ width: 40, height: 40, objectFit: 'cover', flexShrink: 0 }}
                />
                <div>
                    <p className="mb-0 fw-semibold" style={{ fontSize: '0.9rem' }}>
                        {jugador}
                    </p>
                    <p className="mb-0 text-muted" style={{ fontSize: '0.75rem' }}>
                        {seleccion}
                    </p>
                </div>
            </div>

            <div className="card-body d-flex flex-column align-items-center gap-1 py-3 px-3">
                <div
                    className="rounded-circle d-flex align-items-center justify-content-center overflow-hidden border"
                    style={{ width: '52px', height: '52px', backgroundColor: '#faeeda', flexShrink: 0 }}
                >
                    {imagen_url ? (
                        <img
                            src={imagen_url}
                            alt={jugador}
                            className="rounded-circle"
                            style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                        />
                    ) : (
                        <span className="fw-semibold" style={{ fontSize: '0.85rem', color: '#854f0b' }}>
                            {initials}
                        </span>
                    )}
                </div>
                <p className="mb-0 fw-semibold text-center" style={{ fontSize: '0.9rem' }}>
                    {jugador}
                </p>
                <p className="mb-0 text-muted text-center" style={{ fontSize: '0.75rem' }}>
                    {seleccion}
                </p>
            </div>

            <div className="card-footer bg-white d-flex justify-content-center align-items-center gap-2 px-3 py-2">
                <span className="text-muted" style={{ fontSize: '0.75rem' }}>
                    Buscando
                </span>
                <span style={{ fontSize: '0.75rem', color: '#854f0b' }}>●</span>
            </div>

        </div>
    );
};

export default FaltanteCard;