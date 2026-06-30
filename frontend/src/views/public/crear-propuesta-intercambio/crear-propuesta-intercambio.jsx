import { useLocation } from 'react-router-dom'
import SectionCard from '@/components/ui/section-card/section-card.jsx'
import SelectorRepetidas from '@/components/ui/selector-repetidas/selector-repetidas.jsx'
import Button from '@/components/ui/button/button.jsx'
import useCrearPropuesta from './useCrearPropuesta'
import PerfilSimple from '@/components/ui/perfil-simple/perfil-simple.jsx'
import styles from './crear-propuesta-intercambio.module.css'

const CrearPropuestaIntercambio = () => {
  const { state } = useLocation()
  const figurita = state?.figurita
  const { seleccionadas, setSeleccionadas, enviar, enviando } = useCrearPropuesta(figurita)

  if (!figurita) return <h2>No se pudo cargar la figurita.</h2>

  const perfilDuenio = {
    id: figurita.perfil_id,
    nombre: figurita.nombre_usuario,
    iniciales: figurita.nombre_usuario
      .split(' ')
      .map((w) => w[0])
      .join('')
      .slice(0, 2)
      .toUpperCase(),
    calificacion_media: figurita.reputacion,
  }

  return (
    <div className="container py-4 px-3 px-md-4">
      <div className="d-flex flex-column gap-3 mx-auto" style={{ maxWidth: '900px' }}>
        <div className={`${styles.hero} p-3 d-flex align-items-center gap-3`}>
          <img
            src={figurita.imagen_url || '/jugador-placeholder.png'}
            alt={figurita.jugador}
            className={styles.heroImagen}
          />
          <div>
            <p className={styles.heroNombre}>{figurita.jugador}</p>
            <p className={styles.heroSeleccion}>{figurita.seleccion}</p>
            <span className={styles.heroNumero}>#{figurita.numero}</span>
          </div>
        </div>

        <SectionCard>
          <SectionCard.Section>
            <p className="label-seccion">Publicado por</p>
            <div className="mt-2">
              <PerfilSimple perfil={perfilDuenio} />
            </div>
          </SectionCard.Section>
        </SectionCard>

        <SectionCard>
          <SectionCard.Section>
            <p className="label-seccion">Seleccioná las figuritas que querés ofrecer</p>
            <div className="mt-2">
              <SelectorRepetidas
                modo="multiple"
                bloqueadas={[]}
                onChange={setSeleccionadas}
                metodoIntercambio="INTERCAMBIO"
                perfilId={figurita.perfil_id}
              />
            </div>
          </SectionCard.Section>
        </SectionCard>

        <Button
          label="Enviar propuesta ↗"
          variante="primario"
          disabled={seleccionadas.length === 0 || enviando}
          onClick={enviar}
        />
      </div>
    </div>
  )
}

export default CrearPropuestaIntercambio
