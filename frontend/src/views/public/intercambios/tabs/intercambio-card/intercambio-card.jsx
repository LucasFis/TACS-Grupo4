import Button from '@/components/ui/button/button.jsx'
import SectionCard from '@/components/ui/section-card/section-card.jsx'
import HeaderUsuarioEstado from './header-usuario-estado.jsx'
import { useNavigate } from 'react-router'
import {
  cancelarPropuesta,
  aceptarPropuesta,
  rechazarPropuesta,
  verificarConflictos,
} from '@/services/propuestasService.js'
import { calificarPerfil } from '@/services/perfilService.js'
import CalificarModal from '@/components/ui/calificar-modal/calificar-modal.jsx'
import { useState } from 'react'
import ConfirmModal from '@/components/ui/confirm-modal/confirm-modal.jsx'
import { useError } from '@/contexts/errorContext.jsx'
import { useToast } from '@/contexts/toastContext.jsx'
import { construirAdvertenciaConflictos } from '@/utils/conflictos.js'

const ChipFigurita = ({ figurita }) => (
  <div className="border rounded p-2 mb-1 d-flex align-items-center gap-2">
    <small className="text-muted">#{figurita.numero}</small>
    <span>{figurita.jugador}</span>
    {figurita.seleccion && <small className="text-muted">· {figurita.seleccion}</small>}
  </div>
)

const IntercambioCard = ({ intercambio, tipo = 'RECIBIDAS', onActualizado }) => {
  const [showCalificacion, setShowCalificacion] = useState(false)
  const [confirmAction, setConfirmAction] = useState(null)
  const [conflictosData, setConflictosData] = useState(null)

  const { handleError } = useError()
  const { showToast } = useToast()

  const navigate = useNavigate()

  const esRecibida = tipo === 'RECIBIDAS'
  const esEnviada = tipo === 'ENVIADAS'

  const izq = esRecibida ? intercambio.figuritas_ofrecidas || [] : [intercambio.figurita_buscada]
  const der = esRecibida ? [intercambio.figurita_buscada] : intercambio.figuritas_ofrecidas || []

  const estado = intercambio.estado
  const usuarioRelacionado = esRecibida ? intercambio.autor : intercambio.destinatario
  const perfilCalificado = esRecibida ? intercambio.autor.id : intercambio.destinatario.id

  const puedeCancelar = esEnviada && estado === 'PENDIENTE'
  const puedeRechazar = esRecibida && estado === 'PENDIENTE'
  const puedeAceptar = esRecibida && estado === 'PENDIENTE'
  const puedeCalificar = estado === 'ACEPTADO' && !intercambio.ya_calificado

  const advertencia = construirAdvertenciaConflictos(conflictosData)

  const ejecutarCancelar = async () => {
    try {
      await cancelarPropuesta(intercambio.id)
      setConfirmAction(null)
      showToast('Propuesta cancelada correctamente', 'success')
      onActualizado?.()
    } catch (error) {
      showToast(handleError(error, (m) => {}),'error')
    }
  }

  const ejecutarAceptar = async () => {
    try {
      await aceptarPropuesta(intercambio.id)
      setConfirmAction(null)
      setConflictosData(null)
      showToast('Propuesta aceptada correctamente', 'success')
      onActualizado?.()
    } catch (error) {
      showToast(handleError(error, (m) => {}),'error')
    }
  }

  const ejecutarRechazar = async () => {
    try {
      await rechazarPropuesta(intercambio.id)
      setConfirmAction(null)
      showToast('Propuesta rechazada correctamente', 'success')
      onActualizado?.()
    } catch (error) {
      showToast(handleError(error, (m) => {}),'error')
    }
  }

  const handleClickAceptar = async () => {
    try {
      const data = await verificarConflictos(intercambio.id)
      setConflictosData(data)
      setConfirmAction('ACEPTAR')
    } catch (error) {
      showToast(handleError(error, (m) => {}),'error')
    }
  }

  const confirmConfig = {
    CANCELAR: {
      titulo: 'Cancelar propuesta',
      mensaje: '¿Seguro que querés cancelar esta propuesta?',
      labelConfirmar: 'Sí, cancelar',
      onConfirmar: ejecutarCancelar,
    },
    ACEPTAR: {
      titulo: 'Aceptar intercambio',
      mensaje: '¿Querés aceptar este intercambio?',
      labelConfirmar: 'Aceptar',
      onConfirmar: ejecutarAceptar,
      advertencia,
    },
    RECHAZAR: {
      titulo: 'Rechazar intercambio',
      mensaje: '¿Seguro que querés rechazar esta propuesta?',
      labelConfirmar: 'Rechazar',
      onConfirmar: ejecutarRechazar,
    },
  }

  const handleCalificar = async ({ valor, descripcion }) => {
    try {
      await calificarPerfil({
        destinatarioId: perfilCalificado,
        valor,
        descripcion,
        transactionId: intercambio.id,
        tipoTransaccion: 'INTERCAMBIO',
      })
      setShowCalificacion(false)
      showToast('Calificación realizada correctamente.')
      onActualizado?.()
    } catch (error) {
      showToast(handleError(error, (m) => {}),'error')
    }
  }

  return (
    <>
      <SectionCard>
        <SectionCard.Section>
          <HeaderUsuarioEstado estado={estado} destinatario={usuarioRelacionado} />
        </SectionCard.Section>

        <SectionCard.Section>
          <div className="row mt-2">
            <div className="col">
              <small className="text-uppercase text-muted fw-semibold">Vos recibís</small>
              {izq.map((f) => (
                <ChipFigurita key={f.id} figurita={f} />
              ))}
            </div>
            <div className="col-auto d-flex align-items-center">⇄</div>
            <div className="col">
              <small className="text-uppercase text-muted fw-semibold">Vos entregás</small>
              {der.map((f) => (
                <ChipFigurita key={f.id} figurita={f} />
              ))}
            </div>
          </div>
        </SectionCard.Section>

        <SectionCard.Section>
          <div className="d-flex gap-2 mt-3">
            <Button
              className="flex-fill"
              label="Ver detalle"
              variante="secundarioBorde"
              onClick={() => navigate(`/intercambios/${intercambio.id}`)}
            />
            {puedeCancelar && (
              <Button
                className="flex-fill"
                label="Cancelar"
                variante="peligroBorde"
                onClick={() => setConfirmAction('CANCELAR')}
              />
            )}
            {puedeCalificar && (
              <Button
                className="flex-fill"
                label="Calificar usuario"
                variante="secundarioBorde"
                onClick={() => setShowCalificacion(true)}
              />
            )}
            {puedeRechazar && (
              <Button
                className="flex-fill"
                label="Rechazar"
                variante="peligroBorde"
                onClick={() => setConfirmAction('RECHAZAR')}
              />
            )}
            {puedeAceptar && (
              <Button
                className="flex-fill"
                label="Aceptar"
                variante="exitoBorde"
                onClick={handleClickAceptar}
              />
            )}
          </div>
        </SectionCard.Section>
      </SectionCard>

      <CalificarModal
        show={showCalificacion}
        usuario={usuarioRelacionado.nombre}
        onCancelar={() => setShowCalificacion(false)}
        onConfirmar={handleCalificar}
      />

      <ConfirmModal
        show={!!confirmAction}
        titulo={confirmConfig[confirmAction]?.titulo}
        mensaje={confirmConfig[confirmAction]?.mensaje}
        labelConfirmar={confirmConfig[confirmAction]?.labelConfirmar}
        onConfirmar={confirmConfig[confirmAction]?.onConfirmar}
        onCancelar={() => { setConfirmAction(null); setConflictosData(null) }}
        advertencia={confirmConfig[confirmAction]?.advertencia}
      />
    </>
  )
}

export default IntercambioCard
