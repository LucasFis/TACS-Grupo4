import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import Breadcrumb from '../../../components/ui/breadcrumb/breadcrumb.jsx'
import SectionCard from '../../../components/ui/section-card/section-card.jsx'
import PerfilSimple from '../../../components/ui/perfil-simple/perfil-simple.jsx'
import FiguritaChip from '@/components/ui/figurita-chip/figurita-chip.jsx'
import Button from '../../../components/ui/button/button.jsx'
import OfertaCard from './oferta-card.jsx'
import ConfirmModal from '@/components/ui/confirm-modal/confirm-modal.jsx'
import ModalInformativo from '@/components/ui/modales/modal-informativo/modal-informativo.jsx'
import { Spinner } from '@/components/ui/spinner/spinner.jsx'

import {
  obtenerPropuesta,
  aceptarPropuesta,
  rechazarPropuesta,
  cancelarPropuesta,
  verificarConflictos,
} from '@/services/propuestasService.js'

import { useError } from '@/contexts/errorContext.jsx'
import { useToast } from '@/contexts/toastContext.jsx'
import { construirAdvertenciaConflictos } from '@/utils/conflictos.js'

const ESTADO_STYLE = {
  ACEPTADO:  { backgroundColor: '#E1F5EE', color: '#085041' },
  RECHAZADO: { backgroundColor: '#FAECE7', color: '#712B13' },
  CANCELADO: { backgroundColor: '#F1EFE8', color: '#444441' },
  PENDIENTE: { backgroundColor: '#FAEEDA', color: '#633806' },
}

const VerIntercambio = () => {
  const { intercambioId } = useParams()
  const [cargando, setCargando] = useState(true)
  const [propuesta, setPropuesta] = useState(null)
  const [showConfirmAceptar, setShowConfirmAceptar] = useState(false)
  const [conflictosData, setConflictosData] = useState(null)
  const [validandoConflictos, setValidandoConflictos] = useState(false)
  const [procesando, setProcesando] = useState(false)
  const [accionProcesando, setAccionProcesando] = useState('')

  const { handleError } = useError()
  const { showToast } = useToast()

  const cargarIntercambio = async () => {
    try {
      setCargando(true)
      const data = await obtenerPropuesta(intercambioId)
      setPropuesta(data)
    } catch (error) {
      showToast(handleError(error, () => {}), 'error')
    } finally {
      setCargando(false)
    }
  }

  useEffect(() => {
    cargarIntercambio()
  }, [intercambioId])

  const handleClickAceptar = async () => {
    try {
      setValidandoConflictos(true)
      const data = await verificarConflictos(propuesta.id)
      setConflictosData(data)
      setShowConfirmAceptar(true)
    } catch (error) {
      showToast(handleError(error, () => {}), 'error')
    } finally {
      setValidandoConflictos(false)
    }
  }

  const ejecutarAceptar = async () => {
    try {
      setShowConfirmAceptar(false)
      setConflictosData(null)
      setProcesando(true)
      setAccionProcesando('Aceptando intercambio...')
      await aceptarPropuesta(propuesta.id)
      setPropuesta((prev) => ({ ...prev, estado: 'ACEPTADO' }))
      showToast('Propuesta aceptada correctamente.')
    } catch (error) {
      showToast(handleError(error, () => {}), 'error')
    } finally {
      setProcesando(false)
    }
  }

  const ejecutarRechazar = async () => {
    try {
      setProcesando(true)
      setAccionProcesando('Rechazando intercambio...')
      await rechazarPropuesta(propuesta.id)
      setPropuesta((prev) => ({ ...prev, estado: 'RECHAZADO' }))
      showToast('Propuesta rechazada correctamente.')
    } catch (error) {
      showToast(handleError(error, () => {}), 'error')
    } finally {
      setProcesando(false)
    }
  }

  const ejecutarCancelar = async () => {
    try {
      setProcesando(true)
      setAccionProcesando('Cancelando intercambio...')
      await cancelarPropuesta(propuesta.id)
      setPropuesta((prev) => ({ ...prev, estado: 'CANCELADO' }))
      showToast('Propuesta cancelada correctamente.')
    } catch (error) {
      showToast(handleError(error, () => {}), 'error')
    } finally {
      setProcesando(false)
    }
  }

  if (cargando) return <div className="container py-4"><Spinner /></div>
  if (!propuesta) return <div className="container py-4"><h3>No se encontró el intercambio</h3></div>

  const esRecibida = propuesta.tipo === 'RECIBIDA'
  const esEnviada = propuesta.tipo === 'ENVIADA'
  const estaPendiente = propuesta.estado === 'PENDIENTE' && esRecibida
  const puedeCancelar = propuesta.estado === 'PENDIENTE' && esEnviada

  return (
    <div className="container py-4 px-3 px-md-4">
      <div className="mx-auto" style={{ maxWidth: '900px' }}>
        <Breadcrumb
          crumbs={[
            { name: 'Intercambios', to: '/intercambios' },
            { name: `#${propuesta.id}`, to: `/intercambios/${propuesta.id}` },
          ]}
        />

        <div className="d-flex flex-column gap-3">
          <SectionCard>
            <SectionCard.Section>
              <p className="label-seccion">Estado del intercambio</p>
              <div className="d-flex justify-content-between align-items-center mt-2">
                <span
                  className="badge fs-6 fw-semibold px-3 py-2 rounded-3"
                  style={ESTADO_STYLE[propuesta.estado] ?? ESTADO_STYLE.PENDIENTE}
                >
                  {propuesta.estado ?? 'PENDIENTE'}
                </span>

                {estaPendiente && (
                  <div className="d-flex gap-2">
                    <Button label="Rechazar" onClick={ejecutarRechazar} variante="peligroBorde" />
                    <Button label="Aceptar" onClick={handleClickAceptar} variante="exitoBorde" />
                  </div>
                )}
                {puedeCancelar && (
                  <Button label="Cancelar" onClick={ejecutarCancelar} variante="peligroBorde" />
                )}
              </div>
            </SectionCard.Section>
          </SectionCard>

          <SectionCard>
            <SectionCard.Section>
              <p className="label-seccion">Usuario</p>
              <div className="mt-2">
                <PerfilSimple perfil={esRecibida ? propuesta.autor : propuesta.destinatario} />
              </div>
            </SectionCard.Section>
          </SectionCard>

          <SectionCard>
            <SectionCard.Section>
              <p className="label-seccion">
                {esRecibida ? 'Figurita que vos entregás' : 'Figurita que solicitás'}
              </p>
              <div className="d-flex flex-column gap-1 mt-2">
                <FiguritaChip fig={propuesta.figurita_buscada} variante="verde" />
              </div>
            </SectionCard.Section>
          </SectionCard>

          <SectionCard>
            <SectionCard.Section>
              <p className="label-seccion">
                {esRecibida ? 'Figuritas que vos recibís' : 'Figuritas que ofrecés'} ({propuesta.figuritas_ofrecidas.length})
              </p>
              <div className="d-flex flex-column gap-1 mt-2">
                {propuesta.figuritas_ofrecidas.map((fig) => (
                  <FiguritaChip key={fig.id} fig={fig} variante="verde" />
                ))}
              </div>
            </SectionCard.Section>
          </SectionCard>

          <SectionCard>
            <SectionCard.Section>
              <p className="label-seccion">Resumen de la propuesta</p>
              <div className="mt-2">
                <OfertaCard propuesta={propuesta} tipo={propuesta.tipo} />
              </div>
            </SectionCard.Section>
          </SectionCard>
        </div>

        <ConfirmModal
          show={showConfirmAceptar}
          titulo="Aceptar intercambio"
          mensaje="¿Querés aceptar este intercambio?"
          labelConfirmar="Aceptar"
          onConfirmar={ejecutarAceptar}
          onCancelar={() => { setShowConfirmAceptar(false); setConflictosData(null) }}
          advertencia={construirAdvertenciaConflictos(conflictosData)}
        />

        <ModalInformativo open={validandoConflictos}>
          <h3>Validando conflictos...</h3>
        </ModalInformativo>

        <ModalInformativo open={procesando}>
          <h3>{accionProcesando}</h3>
          <p>Esto puede tardar unos segundos</p>
        </ModalInformativo>
      </div>
    </div>
  )
}

export default VerIntercambio
