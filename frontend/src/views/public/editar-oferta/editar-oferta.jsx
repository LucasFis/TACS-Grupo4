import { useNavigate, useParams } from 'react-router-dom'
import { useState } from 'react'
import useQueryConError from '@/hooks/useQueryConError'
import { editarOferta, cancelarOferta } from '@/services/subastasService.js'
import { buscarSubasta } from '@/services/subastasService.js'
import SectionTitle from '@/components/ui/section-title/section-title.jsx'
import SectionCard from '@/components/ui/section-card/section-card.jsx'
import SelectorRepetidas from '@/components/ui/selector-repetidas/selector-repetidas.jsx'
import Button from '@/components/ui/button/button.jsx'
import ConfirmModal from '@/components/ui/confirm-modal/confirm-modal.jsx'
import ModalInformativo from '@/components/ui/modales/modal-informativo/modal-informativo.jsx'
import { useError } from '@/contexts/errorContext.jsx'
import { useToast } from '@/contexts/toastContext.jsx'
import { Spinner } from '@/components/ui/spinner/spinner.jsx'
import styles from './editar-oferta.module.css'
import { obtenerBloqueadas } from '@/utils/ofertas.js'

const mismasFiguritas = (anteriores, actuales) => {
  if (anteriores.length !== actuales.length) return false
  const idsAnteriores = new Set(anteriores.map((f) => f.figurita_id ?? f.id))
  return actuales.every((f) => idsAnteriores.has(f.figurita_id ?? f.id))
}

const EditarOferta = () => {
  const navigate = useNavigate()
  const { subId, ofertaId } = useParams()
  const { handleError } = useError()
  const { showToast } = useToast()

  const [figuritasExtra, setFiguritasExtra] = useState([])
  const [procesando, setProcesando] = useState(false)
  const [accionProcesando, setAccionProcesando] = useState('')
  const [showEliminar, setShowEliminar] = useState(false)

  const {
    data: subasta,
    isLoading: cargando,
    isFetching,
  } = useQueryConError({
    queryKey: ['subasta', subId],
    queryFn: ({ signal }) => buscarSubasta({ subId }, signal),
    showToastOnError: false,
  })

  if (cargando) return <Spinner />
  if (!subasta) return <h2>No se pudo cargar la subasta.</h2>

  const oferta = subasta.ofertas.find((o) => o.id === ofertaId)
  if (!oferta) return isFetching ? <Spinner /> : <h2>No se encontró la oferta.</h2>

  const ofrecidasAnteriores = oferta.figuritas_ofrecidas.map((f) => ({
    ...f,
    figurita_id: f.id,
  }))

  const calMinima = subasta.calificacion_minima_solicitada ?? 0
  const figuritas_solicitadas = subasta.figuritas_solicitadas ?? []
  const bloqueadas = obtenerBloqueadas(ofrecidasAnteriores, figuritas_solicitadas)
  const todasSeleccionadas = [...bloqueadas, ...figuritasExtra]
  const sinCambios = mismasFiguritas(ofrecidasAnteriores, todasSeleccionadas)
  const figurita = subasta.figurita_subastada ?? subasta.figurita

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
      setProcesando(true)
      setAccionProcesando('Guardando oferta...')
      await editarOferta(
        subasta.id,
        oferta.id,
        todasSeleccionadas.map((f) => f.figurita_id ?? f.id),
      )
      showToast('Oferta editada correctamente', 'success')
      navigate('/subastas')
    } catch (e) {
      handleError(e, (err) => showToast(err.mensaje, 'error'))
    } finally {
      setProcesando(false)
    }
  }

  const onEliminar = async () => {
    try {
      setShowEliminar(false)
      setProcesando(true)
      setAccionProcesando('Eliminando oferta...')
      await cancelarOferta(subasta.id, oferta.id)
      navigate('/subastas')
    } catch (e) {
      handleError(e, (err) => showToast(err.mensaje, 'error'))
    } finally {
      setProcesando(false)
    }
  }

  return (
    <div className="container py-4 px-3 px-md-4">
      <div className="d-flex flex-column gap-3">
        <div
          className={`${styles.figuritaSubastada} p-2 d-flex flex-column justify-content-center align-items-center gap-2 w-100 rounded-2 mb-3`}
        >
          <img
            className={`${styles.figuritaImagen} bg-white rounded-3`}
            src={figurita?.imagen_url || '/jugador-placeholder.png'}
            alt={figurita?.jugador}
          />
          <h4>{figurita?.jugador}</h4>
          <h6>{figurita?.seleccion}</h6>
        </div>

        <SectionCard>
          <SectionTitle>CONDICIONES PARA OFERTAR</SectionTitle>
          <SectionCard.Section>
            <div className="d-flex flex-column gap-3">
              <div className="d-flex flex-column gap-2">
                <p className={styles.label}>Figuritas requeridas</p>
                {figuritas_solicitadas.length > 0 ? (
                  <>
                    <div className="d-flex flex-column gap-2">
                      {figuritas_solicitadas.map((fig, i) => (
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

        <SectionCard>
          <SectionTitle>Editá las figuritas que querés ofrecer</SectionTitle>
          <SectionCard.Section>
            {bloqueadas.length > 0 && (
              <p className={styles.seleccionHint}>
                Las marcadas como <strong>Requerida</strong> se incluyen automáticamente.
              </p>
            )}
            <div className="mt-2">
              <SelectorRepetidas
                modo="multiple"
                bloqueadas={bloqueadas}
                onChange={setFiguritasExtra}
                seleccionadasIniciales={ofrecidasAnteriores.filter(
                  (f) => !bloqueadas.some((b) => b.figurita_id === f.figurita_id),
                )}
                metodoIntercambio="SUBASTA"
                perfilId={subasta.perfil.id}
                mensajeVacio="No tenés repetidas publicadas para subasta que coincidan con las faltantes del perfil. Publicá repetidas para poder ofrecer."
                mostrarAviso={true}
              />
            </div>
          </SectionCard.Section>
        </SectionCard>

        <div className="d-flex flex-column gap-2">
          <div className="d-flex gap-2 justify-content-between">
            <Button label="Cancelar" variante="secundarioBorde" onClick={() => navigate(-1)} />
            <Button label="Guardar cambios ↗" disabled={procesando} onClick={onEnviar} />
          </div>
          <Button
            label="Eliminar oferta"
            variante="peligroBorde"
            disabled={procesando}
            onClick={() => setShowEliminar(true)}
          />
        </div>

        <ConfirmModal
          show={showEliminar}
          titulo="¿Eliminar oferta?"
          mensaje="Esta acción no se puede deshacer. Tu oferta será cancelada y la subasta continuará sin tu participación."
          labelConfirmar="Sí, eliminar"
          onConfirmar={onEliminar}
          onCancelar={() => setShowEliminar(false)}
        />

        <ModalInformativo open={procesando}>
          <h3>{accionProcesando}</h3>
          <p>Esto puede tardar unos segundos</p>
        </ModalInformativo>
      </div>
    </div>
  )
}

export default EditarOferta
