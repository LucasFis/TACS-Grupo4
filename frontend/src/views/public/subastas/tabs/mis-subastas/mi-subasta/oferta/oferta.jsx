import Etiqueta from '../../../../../../../components/ui/etiqueta/etiqueta.jsx'
import './oferta.css'

const Oferta = ({ oferta, onAdjudicar, onRechazar }) => (
  <div
    className={`d-flex align-items-center gap-2 rounded-3 px-2 py-2 ${oferta.seleccionada ? 'oferta-seleccionada' : 'oferta-default'}`}
  >
    <div className="avatar d-flex align-items-center justify-content-center rounded-circle flex-shrink-0">
      {oferta.autor?.iniciales}
    </div>

    <div className="flex-grow-1 overflow-hidden">
      <p className="nombre-oferta mb-0 fw-semibold">
        {oferta.autor?.nombre}
        <span className="calificacion ms-1 text-warning">
          ★ {oferta.autor?.calificacion_media?.toFixed(1) ?? '-'}
        </span>
      </p>
      <p className="label-oferta mb-0 text-muted text-truncate">
        {oferta.figuritas_ofrecidas?.length > 0
          ? oferta.figuritas_ofrecidas.map((f) => f.jugador).join(', ')
          : 'Sin figuritas ofrecidas'}
      </p>
    </div>

    {oferta.seleccionada && <Etiqueta label="Seleccionada" variante="exito" />}
    {!oferta.seleccionada && (
      <button className="btn-adjudicar btn btn-outline-secondary btn-sm" onClick={onAdjudicar}>
        Adjudicar
      </button>
    )}

    {
    <button className="btn btn-outline-secondary btn-sm px-2" onClick={onRechazar}>
      ✕
    </button>
     }

  </div>
)

export default Oferta
