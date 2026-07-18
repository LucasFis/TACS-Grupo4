import { useEffect, useRef, useState } from 'react'
import useQueryConError from '@/hooks/useQueryConError'
import { crearOferta } from '@/services/subastasService.js'
import { buscarPerfil } from '@/services/perfilService.js'
import { buscarRepetidas } from '@/services/coleccionService.js'
import SelectorRepetidas from '@/components/ui/selector-repetidas/selector-repetidas.jsx'
import FiguritaChip from '@/components/ui/figurita-chip/figurita-chip.jsx'
import Button from '@/components/ui/button/button.jsx'
import { useError } from '@/contexts/errorContext.jsx'
import { useToast } from '@/contexts/toastContext.jsx'
import { Spinner } from '@/components/ui/spinner/spinner.jsx'
import ModalInformativo from '@/components/ui/modales/modal-informativo/modal-informativo.jsx'
import styles from './crear-oferta-modal.module.css'

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

const CrearOfertaModal = ({ abierto, onCerrar, subId, subasta, onExito }) => {
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

  const { data: perfil, isLoading: cargandoPerfil } = useQueryConError({
    queryKey: ['perfil'],
    queryFn: ({ signal }) => buscarPerfil(undefined, signal),
    enabled: abierto,
  })

  useEffect(() => {
    if (!abierto) return
    const handler = (e) => { if (e.key === 'Escape') onCerrar() }
    window.addEventListener('keydown', handler)
    return () => window.removeEventListener('keydown', handler)
  }, [abierto, onCerrar])

  useEffect(() => {
    document.body.style.overflow = abierto ? 'hidden' : ''
    return () => { document.body.style.overflow = '' }
  }, [abierto])

  useEffect(() => {
    if (abierto) setFiguritasExtra([])
  }, [abierto])

  const handleBackdropClick = (e) => {
    if (e.target === backdropRef.current) onCerrar()
  }

  const cargando = cargandoRepetidas || cargandoPerfil

  const repetidas = repetidasData?.contenido ?? []
  const calificacionUsuario = perfil?.calificacion_media ?? null

  const calMinima = subasta?.calificacion_minima_solicitada ?? 0
  const cumpleCalificacion =
    calMinima === 0 || (calificacionUsuario !== null && calificacionUsuario >= calMinima)
  const bloqueadas = obtenerBloqueadas(repetidas, subasta?.figuritas_solicitadas)
  const faltantesRequeridas = obtenerFaltantesRequeridas(repetidas, subasta?.figuritas_solicitadas)
  const tieneTodasRequeridas = faltantesRequeridas.length === 0
  const puedeOfertar = cumpleCalificacion && tieneTodasRequeridas

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
          <button className={styles.cerrar} onClick={onCerrar}>✕</button>
        </div>

        <hr className={styles.divisor} />

        {cargando ? (
          <div className="d-flex justify-content-center py-4">
            <Spinner />
          </div>
        ) : (
          <>
            {/* Condiciones */}
            <div className={styles.seleccion}>
              <div className={styles.seleccionBloque}>
                <p className={styles.seleccionLabel}>Condiciones para ofertar</p>

                <div className="d-flex flex-column gap-3">
                  <div className="d-flex flex-column gap-2">
                    <p className={styles.label}>Figuritas requeridas</p>
                    {subasta.figuritas_solicitadas.length > 0 ? (
                      <>
                        {subasta.figuritas_solicitadas.map((fig, i) => (
                          <FiguritaChip key={i} fig={fig} variante="verde" />
                        ))}
                        <p className="mb-0 text-muted" style={{ fontSize: '0.72rem' }}>
                          El ofertante debe incluir al menos una de estas figuritas
                        </p>
                      </>
                    ) : (
                      <p className="mb-0 fst-italic text-muted" style={{ fontSize: '0.85rem' }}>
                        Sin restricción — el ofertante puede ofrecer cualquier figurita
                      </p>
                    )}
                  </div>

                  <div className={`${styles.calificacionDivider} d-flex align-items-center justify-content-between`}>
                    <div>
                      <p className={styles.label}>Calificación mínima</p>
                      <p className="mb-0 text-muted" style={{ fontSize: '0.72rem' }}>
                        {calMinima <= 1
                          ? 'Cualquier usuario puede ofertar'
                          : 'Solo usuarios con esta calificación pueden ofertar'}
                      </p>
                    </div>
                    {calMinima <= 1 ? (
                      <div className={styles.calSinMinimo}>Sin mínimo</div>
                    ) : (
                      <div className={`${styles.calBadge} d-flex align-items-center gap-1`}>
                        <span className={styles.calBadgeNumero}>{calMinima}</span>
                        <span className={styles.calBadgeLabel}>★ o más</span>
                      </div>
                    )}
                  </div>
                </div>
              </div>
            </div>

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
                  <p className={styles.seleccionTitulo}>
                    Seleccioná las figuritas que querés ofrecer
                  </p>
                  {bloqueadas.length > 0 && (
                    <p className={styles.seleccionHint}>
                      Las marcadas como <strong>Requerida</strong> se incluyen automáticamente. Podés
                      sumar más si querés.
                    </p>
                  )}
                </div>

                <SelectorRepetidas
                  modo="multiple"
                  bloqueadas={bloqueadas}
                  onChange={setFiguritasExtra}
                  metodoIntercambio="SUBASTA"
                  perfilId={subasta.perfil.id}
                />
              </div>
            )}

            <hr className={styles.divisor} />

            <div className={styles.footer}>
              <button className={styles.cancelar} onClick={onCerrar}>Cancelar</button>
              <Button
                label="Enviar oferta"
                disabled={!puedeOfertar || [...bloqueadas, ...figuritasExtra].length === 0}
                onClick={onEnviar}
              />
            </div>
          </>
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
