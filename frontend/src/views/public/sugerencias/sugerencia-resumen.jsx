import PerfilSimple from '@/components/ui/perfil-simple/perfil-simple.jsx'
import FiguritaRecomendadaCard from '@/views/public/sugerencias/figurita-recomendada-card.jsx'
import styles from '@/views/public/sugerencias/sugerencia-card.module.css'

const SugerenciaResumen = ({figuritasNecesarias, figuritasRecomendadas, perfil}) => {
  return (
    <>
      <div className="d-flex align-items-center justify-content-between">
        <PerfilSimple perfil={perfil} />
      </div>

      <hr className="my-3" />

      <div className="d-flex flex-column justify-content-between">
        <div className="d-flex flex-row justify-content-between gap-2">
          <div className="small text-muted mb-1">A ÉL/ELLA LE INTERESA</div>
          <div className="small text-muted mb-1">ÉL/ELLA TIENE</div>
        </div>
        <div className="d-flex flex-row align-items-center gap-2">
          <div className="flex-grow-1">
            {figuritasNecesarias.map((fig) => (
              <FiguritaRecomendadaCard fig={fig} key={fig.id} />
            ))}
          </div>

          <div className={styles.swapIcon}>⇄</div>

          <div className="flex-grow-1 text-end">
            {figuritasRecomendadas.map((fig) => (
              <FiguritaRecomendadaCard fig={fig} key={fig.id} verde={false} />
            ))}
          </div>
        </div>
      </div>
    </>
  )
}

export default SugerenciaResumen