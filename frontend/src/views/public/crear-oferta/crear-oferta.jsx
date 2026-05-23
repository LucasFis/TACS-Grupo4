import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { buscarSubasta, crearOferta } from '@/services/subastasService.js'
import { buscarPerfil } from '@/services/perfilService.js'
import { buscarRepetidas } from '@/services/coleccionService.js'
import SectionTitle from '@/components/ui/section-title/section-title.jsx'
import SectionCard from '@/components/ui/section-card/section-card.jsx'
import SelectorRepetidas from '@/components/ui/selector-repetidas/selector-repetidas.jsx'
import Button from '@/components/ui/button/button.jsx'
import { useError } from '@/contexts/errorContext.jsx'
import { useToast } from '@/contexts/toastContext.jsx'
import styles from './crear-oferta.module.css'

const obtenerBloqueadas = (repetidas, figuritasSolicitadas) => {
  if (!figuritasSolicitadas?.length) return []
  const idsRepetidas = new Set(repetidas.map((r) => r.figurita_id))
  return figuritasSolicitadas
    .filter((sol) => idsRepetidas.has(sol.id))
    .map((sol) => repetidas.find((r) => r.figurita_id === sol.id))
}

const obtenerFaltantesRequeridas = (repetidas, figuritasSolicitadas) => {
  if (!figuritasSolicitadas?.length) return []
  const idsRepetidas = new Set(repetidas.map((r) => r.figurita_id))
  return figuritasSolicitadas.filter((sol) => !idsRepetidas.has(sol.id))
}

const CrearOferta = () => {
  const { subId } = useParams()
  const navigate = useNavigate()

  const [subasta, setSubasta] = useState(null)
  const [repetidas, setRepetidas] = useState([])
  const [calificacionUsuario, setCalificacion] = useState(null)
  const [cargando, setCargando] = useState(true)
  const [figuritasExtra, setFiguritasExtra] = useState([])
  const { handleError } = useError()
  const { showToast } = useToast()

  useEffect(() => {
    const cargar = async () => {
      try {
        setCargando(true)
        const [payloadSubasta, payloadRepetidas, payloadPerfil] = await Promise.all([
          buscarSubasta({ subId }),
          buscarRepetidas({ pagina: 1, limite: 10 }),
          buscarPerfil(),
        ])
        setSubasta(payloadSubasta)
        setRepetidas(payloadRepetidas ?? [])
        setCalificacion(payloadPerfil?.calificacion_media ?? null)
      } catch (e) {
        handleError(e, (err) => showToast(err.mensaje, 'error'))
      } finally {
        setCargando(false)
      }
    }
    cargar()
  }, [subId])

  if (cargando) return <h2>Cargando...</h2>
  if (!subasta) return <h2>No se pudo cargar la subasta.</h2>

  // ── Validaciones ───────────────────────────────────────────────────────────
  const calMinima = subasta.calificacion_minima_solicitada ?? 0
  const cumpleCalificacion =
    calMinima === 0 || (calificacionUsuario !== null && calificacionUsuario >= calMinima)
  const bloqueadas = obtenerBloqueadas(repetidas, subasta.figuritas_solicitadas)
  const faltantesRequeridas = obtenerFaltantesRequeridas(repetidas, subasta.figuritas_solicitadas)
  const tieneTodasRequeridas = faltantesRequeridas.length === 0
  const puedeOfertar = cumpleCalificacion && tieneTodasRequeridas

  const onEnviar = async () => {
    try {
      const ids = [...bloqueadas, ...figuritasExtra].map((f) => f.figurita_id ?? f.id)
      await crearOferta(subId, ids)
      navigate('/subastas')
    } catch (e) {
      handleError(e, (err) => showToast(err.mensaje, 'error'))
    }
  }

  return (
    <div className="d-flex flex-column gap-3">
      {/* Preview figurita subastada */}
      <div
        className={
          styles.figuritaSubastada +
          ' p-2 d-flex flex-column justify-content-center align-items-center gap-2 w-100 rounded-2 mb-3'
        }
      >
        <div className={styles.figuritaImagen + ' bg-white rounded-3 '}></div>

        <h4 className={'text-white'}>{subasta.figurita.jugador}</h4>
        <h6 className={'text-white'}>{subasta.figurita.seleccion}</h6>
      </div>

      <SectionCard>
        <SectionTitle>CONDICIONES PARA OFERTAR</SectionTitle>
        <SectionCard.Section>
          <div className="d-flex flex-column gap-3">
            <div className="d-flex flex-column gap-2">
              <p className={styles.crearOfertaLabel}>Figuritas requeridas</p>
              {subasta.figuritas_solicitadas.length > 0 ? (
                <>
                  <div className="d-flex flex-column gap-2">
                    {subasta.figuritas_solicitadas.map((fig, i) => (
                      <div key={i} className={styles.crearOfertaFigRequerida}>
                        <div className={styles.crearOfertaFigNumero}>{fig.numero}</div>
                        <div>
                          <p className={styles.crearOfertaFigJugador}>{fig.jugador}</p>
                          <p className={styles.crearOfertaFigSeleccion}>{fig.seleccion}</p>
                        </div>
                      </div>
                    ))}
                  </div>
                  <p className={styles.crearOfertaHint}>
                    El ofertante debe incluir al menos una de estas figuritas
                  </p>
                </>
              ) : (
                <p className={styles.crearOfertaSinRestriccion}>
                  Sin restricción — el ofertante puede ofrecer cualquier figurita
                </p>
              )}
            </div>

            <div className={styles.crearOfertaCalificacionDivider}>
              <div>
                <p className={styles.crearOfertaLabel}>Calificación mínima</p>
                <p className={styles.crearOfertaHint}>
                  {calMinima <= 1
                    ? 'Cualquier usuario puede ofertar'
                    : 'Solo usuarios con esta calificación pueden ofertar'}
                </p>
              </div>
              {calMinima <= 1 ? (
                <span className={styles.crearOfertaCalSinMinimo}>Sin mínimo</span>
              ) : (
                <div className={styles.crearOfertaCalBadge}>
                  <span className={styles.crearOfertaCalBadgeNumero}>{calMinima}</span>
                  <span className={styles.crearOfertaCalBadgeLabel}>★ o más</span>
                </div>
              )}
            </div>
          </div>
        </SectionCard.Section>
      </SectionCard>

      {/* Alerta calificación */}
      {!cumpleCalificacion && (
        <div className="alert alert-warning">
          Tu calificación actual ({calificacionUsuario ?? 'sin datos'}★) es menor a la requerida (
          {calMinima}★). No podés ofertar en esta subasta.
        </div>
      )}

      {/* Alerta figuritas faltantes */}
      {cumpleCalificacion && !tieneTodasRequeridas && (
        <div className="alert alert-warning">
          No tenés todas las figuritas requeridas. Te falta
          {faltantesRequeridas.length > 1 ? 'n' : ''}:{' '}
          <strong>{faltantesRequeridas.map((f) => f.jugador).join(', ')}</strong>.
        </div>
      )}

      {/* Selección */}
      {cumpleCalificacion && tieneTodasRequeridas && (
        <div className="d-flex flex-column gap-3">
          <div>
            <p className={styles.crearOfertaSeleccionTitulo}>
              Seleccioná las figuritas que querés ofrecer
            </p>
            {bloqueadas.length > 0 && (
              <p className={styles.crearOfertaSeleccionHint}>
                Las marcadas como <strong>Requerida</strong> se incluyen automáticamente. Podés
                sumar más si querés.
              </p>
            )}
          </div>

          <SelectorRepetidas modo="multiple" bloqueadas={bloqueadas} onChange={setFiguritasExtra} />

          <Button
            label="Enviar oferta ↗"
            disabled={!puedeOfertar || [...bloqueadas, ...figuritasExtra].length === 0}
            onClick={onEnviar}
          />
        </div>
      )}
    </div>
  )
}

export default CrearOferta
