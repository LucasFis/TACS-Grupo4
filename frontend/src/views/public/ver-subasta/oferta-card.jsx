import PerfilSimple from '../../../components/ui/perfil-simple/perfil-simple.jsx'
import EtiquetaFiguritasPropuesta from '../../../components/ui/etiqueta-figuritas-propuesta/etiqueta-figuritas-propuesta.jsx'
import Button from '@/components/ui/button/button.jsx'
import styles from './oferta-card.module.css'

const OfertaCard = ({
  propuesta,
  position,
  puedeAdjudicar = false,
  onAdjudicar,
}) => {
    const seleccionada = propuesta.seleccionada
  return (
    <div
      className={`
        ${styles.ofertaCard}
        ${seleccionada ? styles.ofertaSeleccionada : ''}
        ps-3 pe-3 pt-1 pb-1 d-flex flex-row align-items-center gap-3
      `}
    >
      {position && (
        <div className={styles.position + ' p-3 d-flex align-items-center justify-content-center'}>
          {position}°
        </div>
      )}

      <div className="flex-grow-1">
        <PerfilSimple perfil={propuesta.autor} />
        <EtiquetaFiguritasPropuesta propuesta={propuesta} />
      </div>


       {propuesta.seleccionada && (
           <span className="badge bg-success">
               Seleccionada
           </span>
       )}

      {puedeAdjudicar && !propuesta.seleccionada && (
        <Button
          label="Adjudicar"
          variante="secundarioBorde"
          onClick={() => onAdjudicar(propuesta.id)}
        />
      )}
    </div>
  )
}

export default OfertaCard