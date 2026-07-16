import { derivarTiempo } from '../../../../../../utils/subastasTiempo.js'
import { calificarPerfil } from '../../../../../../services/perfilService.js'
import CalificarModal from '../../../../../../components/ui/calificar-modal/calificar-modal.jsx'
import CabeceraFigurita from '../../../../../../components/ui/cabecera-figurita/cabecera-figurita.jsx'
import BarraTiempo from '../../../../../../components/ui/barra-tiempo/barra-tiempo.jsx'
import EtiquetaFiguritasOfrecidas from '../../../../../../components/ui/etiqueta-figuritas-propuesta/etiqueta-figuritas-propuesta.jsx'
import Etiqueta from '../../../../../../components/ui/etiqueta/etiqueta.jsx'
import Button from '../../../../../../components/ui/button/button.jsx'
import ModalInformativo from '../../../../../../components/ui/modales/modal-informativo/modal-informativo.jsx'
import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router'

const SubastaParticipo = ({ subasta, finalizada, finalizadaHace, onRefresh }) => {
  const { id, autor, figurita_subastada, fecha_cierre, tu_oferta, ya_calificado } = subasta
  const [mostrarCalificar, setMostrarCalificar] = useState(false)
  const [procesando, setProcesando] = useState(false)
  const navigate = useNavigate()

  const [tiempoRestante, setTiempoRestante] = useState(subasta.tiempo_restante)
  const finalizaPronto = tiempoRestante > 0 && tiempoRestante <= 3600

  const badgeEstado = finalizada
    ? null
    : finalizaPronto
      ? { label: 'Finaliza pronto', variante: 'advertencia' }
      : { label: 'Activa', variante: 'exito' }

  const handleCalificar = async ({ valor, descripcion }) => {
    try {
      setProcesando(true)
      await calificarPerfil({
        destinatarioId: autor.id,
        valor,
        descripcion,
        transactionId: id,
        tipoTransaccion: 'SUBASTA',
      })
      setMostrarCalificar(false)
      onRefresh()
    } catch (error) {
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

      return `${horas.toString().padStart(2, '0')}:${minutos
        .toString()
        .padStart(2, '0')}:${segs.toString().padStart(2, '0')}`
    }

  const puedoCalificar = finalizada && tu_oferta?.seleccionada && !ya_calificado

  return (
    <div className="border rounded-3 overflow-hidden bg-white">
      <CabeceraFigurita figurita={figurita_subastada} badge={badgeEstado} />

      <BarraTiempo
        finalizada={finalizada}
        tiempoRestante={formatearTiempo(tiempoRestante)}
        finalizadaHace={finalizadaHace}
        derecha={
          <>
            Publicada por <strong>{autor.nombre}</strong>
          </>
        }
      />

      <div className="px-3 py-2 d-flex justify-content-between align-items-center border-top">
        <span className="text-muted" style={{ fontSize: '0.82rem' }}>
          Tu oferta
        </span>
        <div className="d-flex align-items-center gap-2">
          <EtiquetaFiguritasOfrecidas propuesta={tu_oferta} />
          {tu_oferta?.seleccionada && <Etiqueta label="Mejor oferta" variante="exito" />}
        </div>
      </div>

      <div className="px-3 py-2 d-flex gap-2 border-top">
        <Button
          label={finalizada ? 'Ver resumen' : 'Ver subasta'}
          variante="secundarioBorde"
          className="flex-fill"
          onClick={() => navigate(`/subastas/${id}`)}
        />
        {finalizada
          ? puedoCalificar && (
              <Button
                label="Calificar usuario"
                variante="secundarioBorde"
                className="flex-fill"
                onClick={() => setMostrarCalificar(true)}
              />
            )
          : tu_oferta?.figuritas_ofrecidas?.length > 0 && (
              <Button
                label="Editar oferta"
                variante="secundarioBorde"
                className="flex-fill"
                onClick={() => navigate(`/subastas/${id}/ofertas/${tu_oferta.id}/editar`)}
              />
            )}
      </div>

      <CalificarModal
        show={mostrarCalificar}
        usuario={autor.nombre}
        onConfirmar={handleCalificar}
        onCancelar={() => setMostrarCalificar(false)}
      />

      <ModalInformativo open={procesando}>
        <h3>Calificando usuario...</h3>
        <p>Esto puede tardar unos segundos</p>
      </ModalInformativo>
    </div>
  )
}

export default SubastaParticipo
