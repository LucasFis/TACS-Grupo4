import { useState } from 'react'
import Button from '../../../components/ui/button/button.jsx'
import styles from './sugerencia-card.module.css'
import ProponerIntercambioModal from '@/views/public/sugerencias/proponer-intercambio-modal.jsx'
import FiguritaChip from '@/components/ui/figurita-chip/figurita-chip.jsx'
import PerfilSimple from '@/components/ui/perfil-simple/perfil-simple.jsx'
import { crearPropuesta } from '@/services/propuestasService.js'
import { alternarFavorito } from '@/services/sugerenciasService.js'
import { useToast } from '@/contexts/toastContext.jsx'
import { useError } from '@/contexts/errorContext.jsx'
import ModalInformativo from '@/components/ui/modales/modal-informativo/modal-informativo.jsx'

const SugerenciaCard = ({ id, perfil, figuritasRecomendadas, figuritasNecesarias, favorito }) => {
  const [modalAbierto, setModalAbierto] = useState(false)
  const [esFavorito, setEsFavorito] = useState(favorito)
  const [procesando, setProcesando] = useState(false)
  const { showToast } = useToast()
  const { handleError } = useError()

  const handleToggleFavorito = async () => {
    const prev = esFavorito
    try {
      setEsFavorito(!prev)
      await alternarFavorito({ sugerenciaId: id })
    } catch (error) {
      setEsFavorito(prev)
      showToast(handleError(error, () => {}), 'error')
    }
  }

  const handleProponer = async ({ repetidas, faltantes } = {}) => {
    try {
      setProcesando(true)
      await crearPropuesta(perfil.id, faltantes[0].id, repetidas.map(re => re.figurita_id))
      showToast('Propuesta enviada correctamente', 'success')
    } catch (error) {
      showToast(handleError(error, () => {}), 'error')
      throw error
    } finally {
      setProcesando(false)
    }
  }

  return (
    <>
      <div className={styles.card}>

        <div className={styles.cardHeader}>
          <PerfilSimple perfil={perfil} />
          <button
            onClick={handleToggleFavorito}
            className={styles.botonFavorito}
            aria-label={esFavorito ? 'Quitar de favoritos' : 'Agregar a favoritos'}
          >
            {esFavorito ? '★' : '☆'}
          </button>
        </div>

        <div className={styles.cardBody}>
          <div className={styles.columns}>
            <div className={styles.column}>
              <p className="label-seccion">Le interesa</p>
              <div className="d-flex flex-column gap-1">
                {figuritasNecesarias.map(fig => <FiguritaChip fig={fig} key={fig.id} />)}
              </div>
            </div>
            <div className={styles.swapIcon}>⇄</div>
            <div className={styles.column}>
              <p className="label-seccion">Él/ella tiene</p>
              <div className="d-flex flex-column gap-1">
                {figuritasRecomendadas.map(fig => <FiguritaChip fig={fig} key={fig.id} variante="azul" />)}
              </div>
            </div>
          </div>
        </div>

        <div className={styles.cardFooter}>
          <Button onClick={() => setModalAbierto(true)} variante="terciario" className="btn-sm">Proponer intercambio</Button>
        </div>
      </div>

      <ProponerIntercambioModal
        abierto={modalAbierto}
        onCerrar={() => setModalAbierto(false)}
        perfil={perfil}
        id={id}
        favorito={esFavorito}
        figuritasNecesarias={figuritasNecesarias}
        figuritasRecomendadas={figuritasRecomendadas}
        onProponer={handleProponer}
      />

      <ModalInformativo open={procesando}>
        <h3>Enviando propuesta...</h3>
        <p>Esto puede tardar unos segundos</p>
      </ModalInformativo>
    </>
  )
}

export default SugerenciaCard
