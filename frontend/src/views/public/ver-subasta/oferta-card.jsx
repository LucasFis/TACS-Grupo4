import PerfilSimple from '../../../components/ui/perfil-simple/perfil-simple.jsx'
import FiguritaChip from '@/components/ui/figurita-chip/figurita-chip.jsx'
import Button from '@/components/ui/button/button.jsx'
import styles from './oferta-card.module.css'

const OfertaCard = ({
  propuesta,
  position,
  puedeAdjudicar = false,
  onAdjudicar,
  onRechazar,
}) => {
  const seleccionada = propuesta.seleccionada

  return (
    <div
      className={`
        ${styles.ofertaCard}
        ${seleccionada ? styles.ofertaSeleccionada : ''}
        ps-3 pe-3 pt-2 pb-2 d-flex align-items-start gap-3
      `}
    >
      {position && (
        <div
          className={`${styles.position} d-flex align-items-center justify-content-center`}
        >
          {position}°
        </div>
      )}

      <div className="d-flex flex-column gap-2 flex-grow-1">
        <PerfilSimple perfil={propuesta.autor} />

        <div className="d-flex flex-column gap-1">
          {propuesta.figuritas_ofrecidas?.length > 0 ? (
            propuesta.figuritas_ofrecidas.map((fig) => (
              <FiguritaChip
                key={fig.id}
                fig={fig}
                variante="neutro"
              />
            ))
          ) : (
            <span
              className="text-muted"
              style={{ fontSize: '0.82rem' }}
            >
              Sin figuritas ofrecidas
            </span>
          )}
        </div>

        {propuesta.seleccionada && (
          <span className="badge bg-success">
            Seleccionada
          </span>
        )}

        {puedeAdjudicar && !propuesta.seleccionada && (
          <div className={styles.acciones}>
            <Button
              label="Seleccionar"
              variante="secundarioBorde"
              onClick={() => onAdjudicar(propuesta.id)}
            />
            <Button
              label="Rechazar"
              variante="peligroBorde"
              onClick={() => onRechazar(propuesta.id)}
            />
          </div>
        )}
      </div>
    </div>
  )
}

export default OfertaCard