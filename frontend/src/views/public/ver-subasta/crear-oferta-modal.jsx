import { useEffect, useRef, useState } from 'react'
import useQueryConError from '@/hooks/useQueryConError'
import { crearOferta } from '@/services/subastasService.js'
import { buscarRepetidas } from '@/services/coleccionService.js'
import SectionTitle from '@/components/ui/section-title/section-title.jsx'
import SectionCard from '@/components/ui/section-card/section-card.jsx'
import SelectorRepetidas from '@/components/ui/selector-repetidas/selector-repetidas.jsx'
import Button from '@/components/ui/button/button.jsx'
import { useError } from '@/contexts/errorContext.jsx'
import { useToast } from '@/contexts/toastContext.jsx'
import { Spinner } from '@/components/ui/spinner/spinner.jsx'
import ModalInformativo from '@/components/ui/modales/modal-informativo/modal-informativo.jsx'
import styles from './crear-oferta-modal.module.css'
import { obtenerBloqueadas } from '@/utils/ofertas.js'

const CrearOfertaModal = ({ abierto, onCerrar, subId, subasta, onExito, puedeOfertar, motivo }) => {
  const backdropRef = useRef(null)
  const [figuritasExtra, setFiguritasExtra] = useState([])
  const [procesando, setProcesando] = useState(false)
  const { handleError } = useError()
  const { showToast } = useToast()

  const { data: repetidasData, isLoading: cargandoRepetidas } = useQueryConError({
    queryKey: ['repetidas', { pagina: 1, limite: 10 }],
    queryFn: ({ signal }) => buscarRepetidas({ pagina: 1, limite: 10 }, signal),
    enabled: abierto,
  })

  useEffect(() => {
    if (!abierto) return
    const handler = (e) => {
      if (e.key === 'Escape') onCerrar()
    }
    window.addEventListener('keydown', handler)
    return () => window.removeEventListener('keydown', handler)
  }, [abierto, onCerrar])

  useEffect(() => {
    document.body.style.overflow = abierto ? 'hidden' : ''
    return () => {
      document.body.style.overflow = ''
    }
  }, [abierto])

  useEffect(() => {
    if (abierto) setFiguritasExtra([])
  }, [abierto])

  const handleBackdropClick = (e) => {
    if (e.target === backdropRef.current) onCerrar()
  }

  const cargando = cargandoRepetidas

  const repetidas = repetidasData?.contenido ?? []
  const calMinima = subasta?.calificacion_minima_solicitada ?? 0
  const bloqueadas = obtenerBloqueadas(repetidas, subasta?.figuritas_solicitadas)

  const onEnviar = async () => {
    try {
      setProcesando(true)
      const ids = [...bloqueadas, ...figuritasExtra].map((f) => f.figurita_id ?? f.id)
      await crearOferta(subId, ids)
      showToast('Oferta creada correctamente', 'success')
      onExito()
      onCerrar()
    } catch (e) {
      handleError(e, (err) => showToast(err.mensaje, 'error'))
    } finally {
      setProcesando(false)
    }
  }

  if (!abierto) return null

  return (
    <div
      ref={backdropRef}
      className={styles.backdrop}
      onClick={handleBackdropClick}
      role="dialog"
      aria-modal="true"
      aria-label="Proponer oferta"
    >
      <div className={styles.modal}>
        <div className={styles.header}>
          <h5 className="mb-0">Proponer oferta</h5>
          <button className={styles.cerrar} onClick={onCerrar}>
            ✕
          </button>
        </div>

        <hr className={styles.divisor} />

        {cargando ? (
          <div className="d-flex justify-content-center py-4">
            <Spinner />
          </div>
        ) : (
          <div className="d-flex flex-column gap-3">
            <SectionCard>
              <SectionTitle>CONDICIONES PARA OFERTAR</SectionTitle>
              <SectionCard.Section>
                <div className="d-flex flex-column gap-3">
                  <div className="d-flex flex-column gap-2">
                    <p className={styles.label}>Figuritas requeridas</p>
                    {subasta.figuritas_solicitadas.length > 0 ? (
                      <>
                        <div className="d-flex flex-column gap-2">
                          {subasta.figuritas_solicitadas.map((fig, i) => (
                            <div key={i} className={styles.figRequerida}>
                              <div className={styles.figNumero}>{fig.numero}</div>
                              <div>
                                <p className={styles.figJugador}>{fig.jugador}</p>
                                <p className={styles.figSeleccion}>{fig.seleccion}</p>
                              </div>
                            </div>
                          ))}
                        </div>
                        <p className={styles.hint}>
                          El ofertante debe incluir al menos una de estas figuritas
                        </p>
                      </>
                    ) : (
                      <p className={styles.sinRestriccion}>
                        Sin restricción — el ofertante puede ofrecer cualquier figurita
                      </p>
                    )}
                  </div>

                  <div className={styles.calificacionDivider}>
                    <div>
                      <p className={styles.label}>Calificación mínima</p>
                      <p className={styles.hint}>
                        {calMinima <= 1
                          ? 'Cualquier usuario puede ofertar'
                          : 'Solo usuarios con esta calificación pueden ofertar'}
                      </p>
                    </div>
                    {calMinima <= 1 ? (
                      <span className={styles.calSinMinimo}>Sin mínimo</span>
                    ) : (
                      <div className={styles.calBadge}>
                        <span className={styles.calBadgeNumero}>{calMinima}</span>
                        <span className={styles.calBadgeLabel}>★ o más</span>
                      </div>
                    )}
                  </div>
                </div>
              </SectionCard.Section>
            </SectionCard>

            {/* Alerta si no puede ofertar */}
            {!puedeOfertar && motivo && (
              <div className="alert alert-warning">{motivo}</div>
            )}

            {/* Selección */}
            <div className="d-flex flex-column gap-3">
              <SectionCard>
                <SectionTitle>SELECCIONÁ LAS FIGURITAS QUE QUERÉS OFRECER</SectionTitle>
                <SectionCard.Section>
                  {bloqueadas.length > 0 && (
                    <p className={styles.seleccionHint}>
                      Las marcadas como <strong>Requerida</strong> se incluyen automáticamente.
                      Podés sumar más si querés.
                    </p>
                  )}
                  <div className="mt-2">
                    <SelectorRepetidas
                      modo="multiple"
                      bloqueadas={bloqueadas}
                      onChange={setFiguritasExtra}
                      metodoIntercambio="SUBASTA"
                      perfilId={subasta.perfil.id}
                      mensajeVacio="No tenés repetidas publicadas para subasta que coincidan con las faltantes del perfil. Publicá repetidas para poder ofrecer."
                      mostrarAviso={true}
                    />
                  </div>
                </SectionCard.Section>
              </SectionCard>
            </div>

            <hr className={styles.divisor} />

            <div className={styles.footer}>
              <button className={styles.cancelar} onClick={onCerrar}>
                Cancelar
              </button>
              <Button
                label="Enviar oferta"
                disabled={!puedeOfertar || [...bloqueadas, ...figuritasExtra].length === 0}
                onClick={onEnviar}
              />
            </div>
          </div>
        )}
      </div>

      <ModalInformativo open={procesando}>
        <h3>Enviando oferta...</h3>
        <p>Esto puede tardar unos segundos</p>
      </ModalInformativo>
    </div>
  )
}

export default CrearOfertaModal
