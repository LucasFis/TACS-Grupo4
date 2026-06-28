import { useState } from 'react'
import Button from '../../../components/ui/button/button.jsx'
import styles from './sugerencia-card.module.css'
import ProponerIntercambioModal from '@/views/public/sugerencias/proponer-intercambio-modal.jsx'
import SugerenciaResumen from '@/views/public/sugerencias/sugerencia-resumen.jsx'
import { crearPropuesta } from '@/services/propuestasService.js'
import { useToast } from '@/contexts/toastContext.jsx'
import { useError } from '@/contexts/errorContext.jsx'

const SugerenciaCard = ({ perfil, figuritasRecomendadas, figuritasNecesarias }) => {
  const [modalAbierto, setModalAbierto] = useState(false)
  const {showToast} = useToast()
  const {handleError, errorTemplate} = useError()
  const [error, setErrorState] = useState(errorTemplate())

  const handleProponer = async ({ repetidas, faltantes } = {}) => {
    try {
      await crearPropuesta(perfil.id,  faltantes[0].id, repetidas.map(re => re.figurita_id))
      showToast(`Propuesta enviada correctamente`, "success")
    } catch (error) {
      showToast(handleError(error, setErrorState), "error")
    }
  }

  return (
    <>
      <div className={`p-3 ${styles.card}`}>

        {/* HEADER */}
        <SugerenciaResumen perfil={perfil} figuritasNecesarias={figuritasNecesarias} figuritasRecomendadas={figuritasRecomendadas} />

        <hr className="my-3" />

        <div className="d-flex justify-content-end align-items-center">
          <div className="d-flex gap-2">
            <Button onClick={() => setModalAbierto(true)}>Proponer intercambio</Button>
          </div>
        </div>
      </div>

      <ProponerIntercambioModal
        abierto={modalAbierto}
        onCerrar={() => setModalAbierto(false)}
        perfil={perfil}
        figuritasNecesarias={figuritasNecesarias}
        figuritasRecomendadas={figuritasRecomendadas}
        onProponer={handleProponer}
      />
    </>
  )
}

export default SugerenciaCard
