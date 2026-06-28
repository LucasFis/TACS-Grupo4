const RepetidaCard = ({ figurita, onEditar }) => {
    const {
        figurita_id,
        jugador,
        seleccion,
        cantidad_existente,
        cantidad_reservada,
        metodos,
        foto_url,
    } = figurita;

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
                #{figurita_id}
            </span>

            <button
              className="btn btn-sm p-1 border-0 bg-transparent"
              onClick={onEditar}
              title="Editar"
            >
              ✏️
            </button>
          </div>

            <div className="card-body d-flex flex-column align-items-center gap-1 py-3 px-3">
                <div
                    className="rounded-circle d-flex align-items-center justify-content-center overflow-hidden border"
                    style={{ width: '52px', height: '52px', backgroundColor: '#e6f1fb', flexShrink: 0 }}
                >
                    {foto_url ? (
                        <img
                            src={foto_url}
                            alt={jugador}
                            className="rounded-circle"
                            style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                        />
                    ) : (
                        <span className="fw-semibold" style={{ fontSize: '0.85rem', color: '#185fa5' }}>
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

          <div className="card-footer bg-white px-3 py-2">
            <div
              className="d-flex align-items-center"
              style={{ justifyContent: 'space-between' }}
            >
              <span className="text-muted" style={{ fontSize: '0.75rem' }}>
                  Existentes: <strong className="text-dark">{cantidad_existente}</strong>
              </span>

              <span className="text-secondary" style={{ fontSize: '0.7rem' }}>
                Reservadas: {cantidad_reservada}
              </span>
            </div>

            <div className="d-flex flex-wrap gap-1 mt-2">
              {metodos.map((metodo) => (
                <span
                  key={metodo}
                  className="badge rounded-pill"
                  style={{
                    backgroundColor: '#e1f5ee',
                    color: '#0f6e56',
                    fontSize: '0.65rem'
                  }}
                >
                  {metodo.toLowerCase()}
                </span>
              ))}
            </div>
          </div>

        </div>
    );
};

export default RepetidaCard;