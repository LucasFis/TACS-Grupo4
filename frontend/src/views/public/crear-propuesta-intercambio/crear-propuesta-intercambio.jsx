import { useLocation } from 'react-router-dom'
import SectionCard from '@/components/ui/section-card/section-card.jsx'
import SectionTitle from '@/components/ui/section-title/section-title.jsx'
import SelectorRepetidas from '@/components/ui/selector-repetidas/selector-repetidas.jsx'
import Button from '@/components/ui/button/button.jsx'
import useCrearPropuesta from './useCrearPropuesta'
import UserChip from '@/components/ui/user-chip/user-chip.jsx'
import styles from './crear-propuesta-intercambio.module.css'

const CrearPropuestaIntercambio = () => {
  const { state } = useLocation()
  const figurita = state?.figurita
  const { seleccionadas, setSeleccionadas, enviar, enviando } = useCrearPropuesta(figurita)

  if (!figurita) return <h2>No se pudo cargar la figurita.</h2>

  return (
    <div className="d-flex flex-column gap-3">
      <div
        className={
          styles.figuritaAIntercambiar +
          ' p-2 d-flex flex-column justify-content-center align-items-center gap-2 w-100 rounded-2 mb-3'
        }
      >
        <img
          className={styles.figuritaImagen + ' bg-white rounded-3'}
          src={figurita.imagen_url || '/jugador-placeholder.png'}
          alt={figurita.jugador}
        />
        <h4 className={'text-white'}>{figurita.jugador}</h4>
        <h6 className={'text-white'}>{figurita.seleccion}</h6>
      </div>

      <SectionCard>
        <SectionTitle>PUBLICADA POR</SectionTitle>
        <SectionCard.Section>
          <UserChip nombre={figurita.nombre_usuario} reputacion={figurita.reputacion} />
        </SectionCard.Section>
      </SectionCard>

      <div className="d-flex flex-column gap-2">
        <p className={styles.crearPropuestaSeleccionTitulo}>
          Seleccioná las figuritas que querés ofrecer
        </p>
        <SelectorRepetidas
          modo="multiple"
          bloqueadas={[]}
          onChange={setSeleccionadas}
          metodoIntercambio="INTERCAMBIO"
          perfilId={figurita.perfil_id}
        />
      </div>

      <Button
        label="Enviar propuesta ↗"
        disabled={seleccionadas.length === 0 || enviando}
        onClick={enviar}
      />
    </div>
  )
}

export default CrearPropuestaIntercambio
