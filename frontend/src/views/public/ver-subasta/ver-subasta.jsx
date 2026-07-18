import styles from './ver-subasta.module.css'
import indicatorStyles from '@/components/ui/actualizando-indicator/actualizando-indicator.module.css'
import { useParams } from 'react-router'
import { useEffect, useState } from 'react'
import useQueryConError from '@/hooks/useQueryConError'
import { buscarSubasta, seleccionarOferta, cancelarSubasta, cerrarSubasta } from '@/services/subastasService.js'
import Breadcrumb from '@/components/ui/breadcrumb/breadcrumb.jsx'
import SectionCard from '@/components/ui/section-card/section-card.jsx'
import PerfilSimple from '@/components/ui/perfil-simple/perfil-simple.jsx'
import FiguritaChip from '@/components/ui/figurita-chip/figurita-chip.jsx'
import OfertaCard from './oferta-card.jsx'
import TuOfertaCard from './tu-oferta-card.jsx'
import Button from '@/components/ui/button/button.jsx'
import { useAuth } from '@/contexts/userContext.jsx'
import { useNavigate } from 'react-router-dom'
import { useToast } from '@/contexts/toastContext.jsx'
import { useError } from '@/contexts/errorContext.jsx'
import ModalInformativo from '@/components/ui/modales/modal-informativo/modal-informativo.jsx'
import ConfirmModal from '@/components/ui/confirm-modal/confirm-modal.jsx'
import { Spinner } from '@/components/ui/spinner/spinner.jsx'

const VerSubasta = () => {
  const { subId } = useParams()
  const { user } = useAuth()
  const { showToast } = useToast()
  const { handleError } = useError()

  const [tiempo, setTiempo] = useState(0)
  const [subastaAbierta, setSubastaAbierta] = useState(false)
  const [procesando, setProcesando] = useState(false)
  const [modal, setModal] = useState(null)
  const [accionProcesando, setAccionProcesando] = useState('')
  const navigate = useNavigate()

  const { data: subasta, isLoading: cargando, isFetching, error, refetch } = useQueryConError({
    queryKey: ['subasta', subId],
    queryFn: ({ signal }) => buscarSubasta({ subId }, signal),
  })

  useEffect(() => {
    if (subasta) {
      setSubastaAbierta(subasta.tiempo_restante > 0)
      setTiempo(subasta.tiempo_restante)
    }
  }, [subasta])

  const procesarDuracion = () => {
    const horas = Math.floor(tiempo / 3600)
    const minutos = Math.floor((tiempo % 3600) / 60)
    const segundos = tiempo % 60
    return `${horas.toString().padStart(2, '0')}:${minutos.toString().padStart(2, '0')}:${segundos.toString().padStart(2, '0')}`
  }

  const calcularDuracionTotal = () => {
    const diffMs = new Date(subasta.cierre) - new Date(subasta.inicio)
    const horas = Math.floor(diffMs / (1000 * 60 * 60))
    const minutos = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60))
    return `${horas}h ${minutos}m`
  }

  const formatearFecha = (fecha) => {
    const f = new Date(fecha)
    const dia = f.getDate()
    const mes = f.toLocaleString('es-AR', { month: 'short' })
    const horas = f.getHours().toString().padStart(2, '0')
    const minutos = f.getMinutes().toString().padStart(2, '0')
    return `${dia} ${mes}, ${horas}:${minutos}`
  }

    const adjudicarOferta = async (ofertaId) => {
      try {
        setProcesando(true)
        await seleccionarOferta(subId, ofertaId)
        refetch()
      } catch (err) {
        showToast(handleError(err, () =>{}), 'error')
      } finally {
        setProcesando(false)
      }
    }

    const MODAL_CONFIG = {
      cancelar: {
        titulo: 'Cancelar subasta',
        mensaje: '¿Querés cancelar esta subasta? Todas las ofertas serán rechazadas. Esta acción no se puede deshacer.',
        labelConfirmar: 'Cancelar subasta',
      },
      cerrar: {
        titulo: 'Cerrar subasta',
        mensaje: '¿Querés cerrar esta subasta? La oferta seleccionada será aceptada y el resto rechazadas.',
        labelConfirmar: 'Cerrar subasta',
      },
    }

    const ACCION_LABELS = {
      cancelar: 'Cancelando subasta...',
      cerrar: 'Cerrando subasta...',
    }

    const haySeleccionada = subasta?.ofertas?.some((o) => o.seleccionada)
    const configModal = modal ? MODAL_CONFIG[modal] : null

    const handleConfirmar = async () => {
      try {
        setProcesando(true)
        setAccionProcesando(ACCION_LABELS[modal] ?? 'Procesando...')
        if (modal === 'cancelar') await cancelarSubasta(subId)
        else if (modal === 'cerrar') await cerrarSubasta(subId)
        setModal(null)
        refetch()
      } catch (err) {
        showToast(handleError(err, () => {}), 'error')
      } finally {
        setProcesando(false)
      }
    }

  useEffect(() => {
    if (tiempo <= 0) {
      setSubastaAbierta(false)
      return
    }

    const interval = setInterval(() => {
      setTiempo((prev) => {
        if (prev <= 1) {
          clearInterval(interval)
          setSubastaAbierta(false)
          return 0
        }

        return prev - 1
      })
    }, 1000)
    return () => clearInterval(interval)
  }, [tiempo])

  const ofertasOrdenadas = [...(subasta?.ofertas ?? [])].sort((a, b) => {
    if (a.seleccionada === b.seleccionada) return 0
    return a.seleccionada ? -1 : 1
  })

  const ofertaPropia = subasta?.ofertas.find((o) => o.autor.id === user.perfil_id)
  const esAutor = subasta?.perfil.id === user.perfil_id

  return (
    <div className="container py-4 px-3 px-md-4">
      <div className="mx-auto" style={{ maxWidth: '900px' }}>
        <Breadcrumb
          crumbs={[
            { name: 'Subasta', to: '/subastas' },
            { name: `#${subId}`, to: `/subastas/${subId}` },
          ]}
        />

        {esAutor && subastaAbierta && (
          <div className="d-flex gap-2 mb-3">
            <Button
              label="Cancelar subasta"
              variante="peligroBorde"
              className="flex-fill"
              onClick={() => setModal('cancelar')}
            />
            {haySeleccionada && (
              <Button
                label="Cerrar subasta"
                variante="exito"
                className="flex-fill"
                onClick={() => setModal('cerrar')}
              />
            )}
          </div>
        )}

        {cargando ? (
          <Spinner />
        ) : error ? (
          <h2 className="text-center text-secondary">No se pudo cargar la información</h2>
        ) : (
          <>
            {isFetching && (
              <div className={indicatorStyles.actualizando}>
                <div className={indicatorStyles.spinnerChico} />
                <span>Actualizando...</span>
              </div>
            )}

            {/* Hero del jugador */}
            <div className={`${styles.figuritaSubastada} p-3 d-flex align-items-center gap-3 mb-3`}>
              <img
                src={subasta.figurita.imagen_url || '/jugador-placeholder.png'}
                alt={subasta.figurita.jugador}
                className={styles.figuritaImagen}
              />
              <div>
                <p className={styles.figuritaNombre}>{subasta.figurita.jugador}</p>
                <p className={styles.figuritaSeleccion}>{subasta.figurita.seleccion}</p>
                <span className={styles.figuritaNumero}>#{subasta.figurita.numero}</span>
              </div>
            </div>

            {/* Tiempo restante */}
            <SectionCard>
              <SectionCard.Section>
                <p className="label-seccion">Tiempo restante</p>
                <div className="d-flex align-items-end gap-2">
                  <h2 className="mb-0">{procesarDuracion()}</h2>
                  <p className="mb-1 text-muted">HH:MM:SS</p>
                </div>
              </SectionCard.Section>
            </SectionCard>

            {/* Detalles */}
            <SectionCard>
              <SectionCard.Section>
                <p className="label-seccion">Detalles de la subasta</p>
                <div className="d-flex flex-column gap-3 mt-2">
                  <div className="d-flex">
                    <div className="w-50">
                      <h6 className="mb-1">Inicio</h6>
                      <p className="mb-0 text-muted">{formatearFecha(subasta.inicio)}</p>
                    </div>
                    <div className="w-50">
                      <h6 className="mb-1">Cierre</h6>
                      <p className="mb-0 text-muted">{formatearFecha(subasta.cierre)}</p>
                    </div>
                  </div>
                  <div className="d-flex">
                    <div className="w-50">
                      <h6 className="mb-1">Duración</h6>
                      <p className="mb-0 text-muted">{calcularDuracionTotal()}</p>
                    </div>
                    <div className="w-50">
                      <h6 className="mb-1">Ofertas recibidas</h6>
                      <p className="mb-0 text-muted">{subasta.ofertas.length} ofertas</p>
                    </div>
                  </div>
                </div>
              </SectionCard.Section>
            </SectionCard>

            {/* Publicado por */}
            <SectionCard>
              <SectionCard.Section>
                <p className="label-seccion">Publicado por</p>
                <div className="mt-2">
                  <PerfilSimple perfil={subasta.perfil} />
                </div>
              </SectionCard.Section>
            </SectionCard>

            {/* Condiciones */}
            <SectionCard>
              <SectionCard.Section>
                <p className="label-seccion">Condiciones para ofertar</p>

                <div className="d-flex flex-column gap-3 mt-2">
                  {/* Figuritas requeridas */}
                  <div className="d-flex flex-column gap-2">
                    <p className="label-seccion">Figuritas requeridas</p>
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

                  {/* Calificación mínima */}
                  <div className={`${styles.condicionesDivisor} d-flex align-items-center justify-content-between pt-2`}>
                    <div>
                      <p className="label-seccion">Calificación mínima</p>
                      <p className="mb-0 text-muted" style={{ fontSize: '0.72rem' }}>
                        {subasta.calificacion_minima_solicitada <= 1
                          ? 'Cualquier usuario puede ofertar'
                          : 'Solo usuarios con esta calificación pueden ofertar'}
                      </p>
                    </div>
                    {subasta.calificacion_minima_solicitada <= 1 ? (
                      <div className={`${styles.calBadge} ${styles.calSinMinimo}`}>
                        Sin mínimo
                      </div>
                    ) : (
                      <div className={`${styles.calBadge} ${styles.calConMinimo} d-flex align-items-center gap-1`}>
                        {subasta.calificacion_minima_solicitada}
                        <span className={styles.calConMinimoStar}>★ o más</span>
                      </div>
                    )}
                  </div>
                </div>
              </SectionCard.Section>
            </SectionCard>

            {/* Ofertas */}
            <SectionCard>
              <SectionCard.Section>
                <p className="label-seccion">
                  Ofertas {subastaAbierta ? 'actuales' : 'históricas'} ({subasta.ofertas.length})
                </p>
                <div className="d-flex flex-column gap-2 mt-2">
                  {ofertasOrdenadas.length > 0 ? (
                    ofertasOrdenadas.map((oferta, index) => (
                      <OfertaCard
                        key={oferta.id}
                        position={index + 1}
                        propuesta={oferta}
                        puedeAdjudicar={esAutor && subastaAbierta}
                        onAdjudicar={adjudicarOferta}
                      />
                    ))
                  ) : (
                    <p className="mb-0 text-muted text-center py-3">
                      Aún no hay ofertas
                    </p>
                  )}
                </div>
              </SectionCard.Section>

              {!esAutor && subastaAbierta && (
                <SectionCard.Section>
                  <p className="label-seccion">Tu oferta</p>
                  <div className="mt-2">
                    {ofertaPropia ? (
                      <TuOfertaCard oferta={ofertaPropia} subasta={subasta} subastaAbierta={subastaAbierta} />
                    ) : (
                      <div className="d-flex align-items-center gap-3">
                        <p className="mb-0 text-muted">¿Aún no ofertaste?</p>
                        <Button
                          label="Proponer oferta"
                          variante="terciario"
                          onClick={() => navigate(`/subastas/${subId}/crear-oferta`)}
                        />
                      </div>
                    )}
                  </div>
                </SectionCard.Section>
              )}
            </SectionCard>
          </>
        )}

        <ModalInformativo open={procesando}>
          <h3>{accionProcesando || 'Adjudicando oferta...'}</h3>
          <p>Esto puede tardar unos segundos</p>
        </ModalInformativo>

        <ConfirmModal
          show={modal !== null}
          titulo={configModal?.titulo}
          mensaje={configModal?.mensaje}
          labelConfirmar={configModal?.labelConfirmar}
          onConfirmar={handleConfirmar}
          onCancelar={() => setModal(null)}
        />
      </div>
    </div>
  )
}

export default VerSubasta
