import { useEffect, useRef, useState } from 'react'
import PerfilSimple from '@/components/ui/perfil-simple/perfil-simple.jsx'
import FiguritaRecomendadaCard from '@/views/public/sugerencias/figurita-recomendada-card.jsx'
import SelectorRepetidas from '@/components/ui/selector-repetidas/selector-repetidas.jsx'
import SelectorFaltantes from '@/components/ui/selector-faltantes/selector-faltantes.jsx'
import Button from '@/components/ui/button/button.jsx'
import styles from './proponer-intercambio-modal.module.css'
import SugerenciaResumen from '@/views/public/sugerencias/sugerencia-resumen.jsx'

/**
 * Modal para proponer un intercambio a un perfil sugerido.
 *
 * Props:
 *  - abierto: boolean
 *  - onCerrar: () => void
 *  - perfil: objeto del perfil destino
 *  - figuritasNecesarias: figuritas que le interesan al otro
 *  - figuritasRecomendadas: figuritas que él tiene (te puede dar)
 *  - onProponer: ({ repetidas, faltantes }) => void
 */
const ProponerIntercambioModal = ({
  abierto,
  onCerrar,
  perfil,
  figuritasNecesarias = [],
  figuritasRecomendadas = [],
  onProponer,
}) => {
  const backdropRef = useRef(null)
  const [repetidas, setRepetidas] = useState([])
  const [faltantes, setFaltantes] = useState([])

  // Cerrar con Escape
  useEffect(() => {
    if (!abierto) return
    const handler = (e) => { if (e.key === 'Escape') onCerrar() }
    window.addEventListener('keydown', handler)
    return () => window.removeEventListener('keydown', handler)
  }, [abierto, onCerrar])

  // Bloquear scroll del body mientras está abierto
  useEffect(() => {
    document.body.style.overflow = abierto ? 'hidden' : ''
    return () => { document.body.style.overflow = '' }
  }, [abierto])

  const handleBackdropClick = (e) => {
    if (e.target === backdropRef.current) onCerrar()
  }

  const handleProponer = () => {
    onProponer?.({ repetidas, faltantes })
    onCerrar()
  }

  if (!abierto) return null

  return (
    <div
      ref={backdropRef}
      className={styles.backdrop}
      onClick={handleBackdropClick}
      role="dialog"
      aria-modal="true"
      aria-label={`Proponer intercambio a ${perfil?.nombre ?? 'perfil'}`}
    >
      <div className={styles.modal}>

        <SugerenciaResumen perfil={perfil} figuritasNecesarias={figuritasNecesarias} figuritasRecomendadas={figuritasRecomendadas} />

        <hr className={styles.divisor} />

        {/* SELECCIÓN */}
        <div className={styles.seleccion}>
          <div className={styles.seleccionBloque}>
            <p className={styles.seleccionLabel}>¿Qué le vas a ofrecer?</p>
            <SelectorRepetidas
              modo="multiple"
              metodoIntercambio="INTERCAMBIO"
              bloqueadas={[]}
              onChange={setRepetidas}
              perfilId={perfil?.id ?? null}
            />
          </div>

          <div className={styles.seleccionBloque}>
            <p className={styles.seleccionLabel}>¿Qué querés recibir?</p>
            <SelectorFaltantes
              modo="unica"
              onChange={setFaltantes}
            />
          </div>
        </div>

        <hr className={styles.divisor} />

        {/* FOOTER */}
        <div className={styles.footer}>
          <button className={styles.cancelar} onClick={onCerrar}>Cancelar</button>
          <Button onClick={handleProponer}>Confirmar intercambio</Button>
        </div>
      </div>
    </div>
  )
}

export default ProponerIntercambioModal
