import Etiqueta from '../etiqueta/etiqueta'
import styles from './cabecera-figurita.module.css'

const CabeceraFigurita = ({ figurita, badge }) => (
  <div className="d-flex justify-content-between align-items-center px-3 py-2 border-bottom">
    <div className="d-flex align-items-center gap-2">
      <div className={`${styles.icono} d-flex align-items-center justify-content-center rounded-circle border`}>
        {figurita.imagen_url
          ? <img src={figurita.imagen_url} alt={figurita.jugador} className={styles.iconoImg} />
          : '⚽'
        }
      </div>
      <div>
        <p className={`${styles.titulo} mb-0 fw-semibold`}>{figurita.jugador}</p>
        <p className={`${styles.subtitulo} mb-0 text-muted`}>
          {figurita.seleccion} · #{figurita.numero}
        </p>
      </div>
    </div>
    {badge && <Etiqueta label={badge.label} variante={badge.variante} />}
  </div>
)

export default CabeceraFigurita
