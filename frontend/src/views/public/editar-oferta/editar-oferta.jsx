import { useLocation, useNavigate, useParams } from 'react-router-dom'
import { useState } from 'react'
import { editarOferta, cancelarOferta } from '@/services/subastasService.js'
import SectionTitle from '@/components/ui/section-title/section-title.jsx'
import SectionCard from '@/components/ui/section-card/section-card.jsx'
import SelectorRepetidas from '@/components/ui/selector-repetidas/selector-repetidas.jsx'
import Button from '@/components/ui/button/button.jsx'
import ConfirmModal from '@/components/ui/confirm-modal/confirm-modal.jsx'
import { useError } from '@/contexts/errorContext.jsx'
import { useToast } from '@/contexts/toastContext.jsx'
import styles from './editar-oferta.module.css'

const obtenerBloqueadas = (repetidas, figuritasSolicitadas) => {
  if (!figuritasSolicitadas?.length) return []
  const idsRepetidas = new Set(repetidas.map((r) => r.figurita_id))
  return figuritasSolicitadas
    .filter((sol) => idsRepetidas.has(sol.id))
    .map((sol) => repetidas.find((r) => r.figurita_id === sol.id))
}

const mismasFiguritas = (anteriores, actuales) => {
  if (anteriores.length !== actuales.length) return false
  const idsAnteriores = new Set(anteriores.map((f) => f.figurita_id ?? f.id))
  return actuales.every((f) => idsAnteriores.has(f.figurita_id ?? f.id))
}

const EditarOferta = () => {
  const navigate = useNavigate()
  const { state } = useLocation()
  const { handleError } = useError()
  const { showToast } = useToast()

  const subasta = state?.subasta
  const oferta = state?.oferta

  const [figuritasExtra, setFiguritasExtra] = useState([])
  const [loadingEnviar, setLoadingEnviar] = useState(false)
  const [loadingEliminar, setLoadingEliminar] = useState(false)
  const [showEliminar, setShowEliminar] = useState(false)

  if (!subasta || !oferta) return <h2>No se pudo cargar la oferta.</h2>

  const ofrecidasAnteriores = oferta.figuritas_ofrecidas.map((f) => ({
    ...f,
    figurita_id: f.id,
  }))

  const calMinima = subasta.calificacion_minima_solicitada ?? 0
  const bloqueadas = obtenerBloqueadas(ofrecidasAnteriores, subasta.figuritas_solicitadas)
  const todasSeleccionadas = [...bloqueadas, ...figuritasExtra]
  const sinCambios = mismasFiguritas(ofrecidasAnteriores, todasSeleccionadas)

  const onEnviar = async () => {
    if (sinCambios) {
      showToast('No modificaste ninguna figurita', 'warning')
      return
    }
    if (todasSeleccionadas.length === 0) {
      showToast('Tenés que seleccionar al menos una figurita', 'warning')
      return
    }
    try {
      setLoadingEnviar(true)
      await editarOferta(
        subasta.id,
        oferta.id,
        todasSeleccionadas.map((f) => f.figurita_id ?? f.id),
      )
      navigate('/subastas')
    } catch (e) {
      handleError(e, (err) => showToast(err.mensaje, 'error'))
    } finally {
      setLoadingEnviar(false)
    }
  }

  const onEliminar = async () => {
    try {
      setLoadingEliminar(true)
      await cancelarOferta(subasta.id, oferta.id)
      navigate('/subastas')
    } catch (e) {
      handleError(e, (err) => showToast(err.mensaje, 'error'))
    } finally {
      setLoadingEliminar(false)
    }
  }

  return (
    <div className="container py-4 px-3 px-md-4">
      <div className="d-flex flex-column gap-3">
        {/* Preview */}
        <div
          className={`${styles.figuritaSubastada} p-2 d-flex flex-column justify-content-center align-items-center gap-2 w-100 rounded-2 mb-3`}
        >
          <div className={`${styles.figuritaImagen} bg-white rounded-3`} />
          <h4>{subasta.figurita.jugador}</h4>
          <h6>{subasta.figurita.seleccion}</h6>
        </div>

        {/* Condiciones */}
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

        {/* Selección */}
        <div className="d-flex flex-column gap-3">
          <div>
            <p className={styles.seleccionTitulo}>Editá las figuritas que querés ofrecer</p>
            {bloqueadas.length > 0 && (
              <p className={styles.seleccionHint}>
                Las marcadas como <strong>Requerida</strong> se incluyen automáticamente.
              </p>
            )}
          </div>

          <SelectorRepetidas
            modo="multiple"
            bloqueadas={bloqueadas}
            onChange={setFiguritasExtra}
            seleccionadasIniciales={ofrecidasAnteriores.filter(
              (f) => !bloqueadas.some((b) => b.figurita_id === f.figurita_id),
            )}
          />
        </div>

        {/* Acciones */}
        <div className="d-flex flex-column gap-2">
          <div className="d-flex gap-2 justify-content-between">
            <Button label="Cancelar" variant="secondary" onClick={() => navigate(-1)} />
            <Button
              label={loadingEnviar ? 'Guardando...' : 'Guardar cambios ↗'}
              disabled={loadingEnviar}
              onClick={onEnviar}
            />
          </div>
          <Button
            label={loadingEliminar ? 'Eliminando...' : 'Eliminar oferta'}
            variant="danger"
            onClick={() => setShowEliminar(true)}
          />
        </div>

        <ConfirmModal
          show={showEliminar}
          titulo="¿Eliminar oferta?"
          mensaje="Esta acción no se puede deshacer. Tu oferta será cancelada y la subasta continuará sin tu participación."
          labelConfirmar={loadingEliminar ? 'Eliminando...' : 'Sí, eliminar'}
          onConfirmar={onEliminar}
          onCancelar={() => setShowEliminar(false)}
        />
      </div>
    </div>
  )
}

export default EditarOferta
