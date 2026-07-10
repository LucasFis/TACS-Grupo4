import Etiqueta from '../../../../../../../components/ui/etiqueta/etiqueta.jsx'
import Estrellas from '@/components/ui/estrellas/estrellas.jsx'
import FiguritaChip from '@/components/ui/figurita-chip/figurita-chip.jsx'
import styles from './oferta.module.css'

const Oferta = ({ oferta, onAdjudicar, onRechazar }) => (
  <div
    className={`d-flex align-items-center gap-2 rounded-3 px-2 py-2 ${oferta.seleccionada ? styles.ofertaSeleccionada : styles.ofertaDefault}`}
  >
    <div className={`${styles.avatar} d-flex align-items-center justify-content-center rounded-circle flex-shrink-0`}>
      {oferta.autor?.iniciales}
    </div>

    <div className="flex-grow-1 overflow-hidden">
      <p className={`${styles.nombreOferta} mb-0 fw-semibold`}>
        {oferta.autor?.nombre}
      </p>
      <Estrellas calificacion={oferta.autor?.calificacion_media} mostrarNumero={true} />
      <div className="mt-1 d-flex flex-column gap-1">
        {oferta.figuritas_ofrecidas?.length > 0
          ? oferta.figuritas_ofrecidas.map((f) => <FiguritaChip key={f.id} fig={f} variante="neutro" />)
          : <span className={`${styles.labelOferta} text-muted`}>Sin figuritas ofrecidas</span>
        }
      </div>
    </div>

    {oferta.seleccionada && <Etiqueta label="Seleccionada" variante="exito" />}
    {!oferta.seleccionada && (
      <button className={`${styles.btnAdjudicar} btn btn-outline-secondary btn-sm`} onClick={onAdjudicar}>
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
