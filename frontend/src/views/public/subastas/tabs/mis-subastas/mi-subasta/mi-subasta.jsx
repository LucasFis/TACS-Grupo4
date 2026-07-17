import { useState, useEffect } from 'react'
import ConfirmModal from '../../../../../../components/ui/confirm-modal/confirm-modal.jsx'
import CalificarModal from '../../../../../../components/ui/calificar-modal/calificar-modal.jsx'
import CabeceraFigurita from '../../../../../../components/ui/cabecera-figurita/cabecera-figurita.jsx'
import BarraTiempo from '../../../../../../components/ui/barra-tiempo/barra-tiempo.jsx'
import Button from '../../../../../../components/ui/button/button.jsx'
import Oferta from './oferta/oferta.jsx'
import {
  seleccionarOferta,
  rechazarOferta,
  cancelarSubasta,
  cerrarSubasta,
} from '../../../../../../services/subastasService.js'
import { calificarPerfil } from '../../../../../../services/perfilService.js'
import { derivarTiempo } from '../../../../../../utils/subastasTiempo.js'
import { useNavigate } from 'react-router'
import { useToast } from '@/contexts/toastContext.jsx'
import { useError } from '@/contexts/errorContext.jsx'
import ModalInformativo from '../../../../../../components/ui/modales/modal-informativo/modal-informativo.jsx'
import styles from './mi-subasta.module.css'

const BADGE_ESTADO = {
  activa: { label: 'Activa', variante: 'exito' },
  finaliza_pronto: { label: 'Finaliza pronto', variante: 'advertencia' },
  adjudicada: { label: 'Adjudicada', variante: 'secundario' },
}

const MODAL_CONFIG = {
  adjudicar: {
    titulo: 'Adjudicar oferta',
    mensaje: '¿Querés seleccionar esta oferta? Podés cambiarla antes de cerrar la subasta.',
    labelConfirmar: 'Adjudicar',
  },
  rechazar: {
    titulo: 'Rechazar oferta',
    mensaje: '¿Querés rechazar esta oferta? Esta acción no se puede deshacer.',
    labelConfirmar: 'Rechazar',
  },
  cancelar: {
    titulo: 'Cancelar subasta',
    mensaje:
      '¿Querés cancelar esta subasta? Todas las ofertas serán rechazadas. Esta acción no se puede deshacer.',
    labelConfirmar: 'Cancelar subasta',
  },
  cerrar: {
    titulo: 'Cerrar subasta',
    mensaje:
      '¿Querés cerrar esta subasta? La oferta seleccionada será aceptada y el resto rechazadas.',
    labelConfirmar: 'Cerrar subasta',
  },
}

const MiSubasta = ({ subasta, finalizada, onRefresh, finalizadaHace }) => {
  const {
    id: subastaId,
    figurita_subastada,
    fecha_cierre,
    ofertas,
    oferta_ganadora,
    ya_calificado,
  } = subasta

  const [tiempoRestante, setTiempoRestante] = useState(subasta.tiempo_restante)
  const navigate = useNavigate()
  const { showToast } = useToast()
  const { handleError } = useError()
  const [modal, setModal] = useState(null)
  const [mostrarCalificar, setMostrarCalificar] = useState(false)
  const [procesando, setProcesando] = useState(false)
  const [accionProcesando, setAccionProcesando] = useState('')

  const finalizaPronto = tiempoRestante > 0 && tiempoRestante <= 3600;

  const estadoKey = finalizada ? 'adjudicada' : finalizaPronto ? 'finaliza_pronto' : 'activa'
  const badge = BADGE_ESTADO[estadoKey]
  const haySeleccionada = ofertas?.some((o) => o.seleccionada)
  const config = modal ? MODAL_CONFIG[modal.tipo] : null

  const ACCION_LABELS = {
    adjudicar: 'Adjudicando oferta...',
    rechazar: 'Rechazando oferta...',
    cancelar: 'Cancelando subasta...',
    cerrar: 'Cerrando subasta...',
  }

  const handleConfirmar = async () => {
    try {
      setProcesando(true)
      setAccionProcesando(ACCION_LABELS[modal.tipo] ?? 'Procesando...')
      if (modal.tipo === 'adjudicar') await seleccionarOferta(subastaId, modal.ofertaId)
      else if (modal.tipo === 'rechazar') await rechazarOferta(subastaId, modal.ofertaId)
      else if (modal.tipo === 'cancelar') await cancelarSubasta(subastaId)
      else if (modal.tipo === 'cerrar') await cerrarSubasta(subastaId)
      setModal(null)
      onRefresh()
    } catch (error) {
      showToast(handleError(error, () => {}), 'error')
    } finally {
      setProcesando(false)
    }
  }

  const handleCalificar = async ({ valor, descripcion }) => {
    try {
      setProcesando(true)
      await calificarPerfil({
        destinatarioId: oferta_ganadora.autor.id,
        valor,
        descripcion,
        transactionId: subastaId,
        tipoTransaccion: 'SUBASTA',
      })
      setMostrarCalificar(false)
      onRefresh()
    } catch (error) {
      showToast(handleError(error, () => {}), 'error')
    } finally {
      setProcesando(false)
    }
  }

    useEffect(() => {
      if (tiempoRestante <= 0) return

      const interval = setInterval(() => {
        setTiempoRestante((prev) => Math.max(prev - 1, 0))
      }, 1000)

      return () => clearInterval(interval)
    }, [tiempoRestante])

    const formatearTiempo = (segundos) => {
        if (segundos <= 0) return '00:00:00'
        const horas = Math.floor(segundos / 3600)
        const minutos = Math.floor((segundos % 3600) / 60)
        const segs = segundos % 60
        return `${horas.toString().padStart(2, '0')}:${minutos.toString().padStart(2, '0')}:${segs.toString().padStart(2, '0')}`
    }

  return (
    <>
      <div className="border rounded-3 overflow-hidden bg-white">
        <CabeceraFigurita figurita={figurita_subastada} badge={badge} />

        <BarraTiempo
          finalizada={finalizada}
          tiempoRestante={formatearTiempo(tiempoRestante)}
          finalizadaHace={finalizadaHace}
          derecha={
            !finalizada && (
              <>
                <strong>{ofertas?.length ?? 0}</strong>{' '}
                {ofertas?.length === 1 ? 'oferta recibida' : 'ofertas recibidas'}
              </>
            )
          }
        />

        {/* Activa: lista de ofertas */}
        {!finalizada && (
          <div className="px-3 py-2 d-flex flex-column gap-2 border-top">
            <p className={`${styles.textoChico} mb-0 text-muted`}>Ofertas recibidas</p>
            {ofertas?.length > 0 ? (
              ofertas.map((oferta) => (
                <Oferta
                  key={oferta.id}
                  oferta={oferta}
                  onAdjudicar={() => setModal({ tipo: 'adjudicar', ofertaId: oferta.id })}
                  onRechazar={() => setModal({ tipo: 'rechazar', ofertaId: oferta.id })}
                />
              ))
            ) : (
              <p className={`${styles.textoGanador} mb-0 text-muted text-center py-2`}>
                Todavía no recibiste ofertas para esta subasta.
              </p>
            )}
          </div>
        )}

        {/* Finalizada: ganador o sin ganador */}
        {finalizada && (
          <div className={`${styles.textoGanador} px-3 py-2 border-top`}>
            {oferta_ganadora ? (
              <>
                <span className="text-muted">Ganador: </span>
                <strong>{oferta_ganadora.autor.nombre}</strong>
              </>
            ) : (
              <span className="text-muted">Sin oferta ganadora</span>
            )}
          </div>
        )}

        <div className="px-3 py-2 d-flex gap-2 border-top">
          {finalizada ? (
            <>
              <Button
                label="Ver resumen"
                variante="secundarioBorde"
                className="flex-fill"
                onClick={() => navigate(`/subastas/${subastaId}`)}
              />
              {oferta_ganadora && !ya_calificado && (
                <Button
                  label="Calificar usuario"
                  variante="secundarioBorde"
                  className="flex-fill"
                  onClick={() => setMostrarCalificar(true)}
                />
              )}
            </>
          ) : (
            <>
              <Button
                label="Ver detalle"
                variante="secundarioBorde"
                className="flex-fill"
                onClick={() => navigate(`/subastas/${subastaId}`)}
              />
              <Button
                label="Cancelar subasta"
                variante="peligroBorde"
                className="flex-fill"
                onClick={() => setModal({ tipo: 'cancelar' })}
              />
              {haySeleccionada && (
                <Button
                  label="Cerrar subasta"
                  variante="exito"
                  className="flex-fill"
                  onClick={() => setModal({ tipo: 'cerrar' })}
                />
              )}
            </>
          )}
        </div>
      </div>

      <ConfirmModal
        show={modal !== null}
        titulo={config?.titulo}
        mensaje={config?.mensaje}
        labelConfirmar={config?.labelConfirmar}
        onConfirmar={handleConfirmar}
        onCancelar={() => setModal(null)}
      />

      {oferta_ganadora && (
        <CalificarModal
          show={mostrarCalificar}
          usuario={oferta_ganadora.autor.nombre}
          onConfirmar={handleCalificar}
          onCancelar={() => setMostrarCalificar(false)}
        />
      )}

      <ModalInformativo open={procesando}>
        <h3>{accionProcesando}</h3>
        <p>Esto puede tardar unos segundos</p>
      </ModalInformativo>
    </>
  )
}

export default MiSubasta
