import { derivarTiempo } from '../../../../../../utils/subastasTiempo.js'
import { calificarPerfil } from '../../../../../../services/perfilService.js'
import CalificarModal from '../../../../../../components/ui/calificar-modal/calificar-modal.jsx'
import CabeceraFigurita from '../../../../../../components/ui/cabecera-figurita/cabecera-figurita.jsx'
import BarraTiempo from '../../../../../../components/ui/barra-tiempo/barra-tiempo.jsx'
import EtiquetaFiguritasOfrecidas from '../../../../../../components/ui/etiqueta-figuritas-propuesta/etiqueta-figuritas-propuesta.jsx'
import Etiqueta from '../../../../../../components/ui/etiqueta/etiqueta.jsx'
import Button from '../../../../../../components/ui/button/button.jsx'
import { useState } from 'react'
import { useNavigate } from 'react-router'

const SubastaParticipo = ({ subasta, finalizada, onRefresh }) => {
  const { id, autor, figurita_subastada, fecha_cierre, tu_oferta, ya_calificado } = subasta
  const [mostrarCalificar, setMostrarCalificar] = useState(false)
  const navigate = useNavigate()

  const { tiempoRestante, finalizadaHace, finalizaPronto } = derivarTiempo({ fecha_cierre })

  const badgeEstado = finalizada
    ? null
    : finalizaPronto
      ? { label: 'Finaliza pronto', variante: 'advertencia' }
      : { label: 'Activa', variante: 'exito' }

  const handleCalificar = async ({ valor, descripcion }) => {
    await calificarPerfil({
      destinatarioId: autor.id,
      valor,
      descripcion,
      transactionId: id,
      tipoTransaccion: 'SUBASTA',
    })
    setMostrarCalificar(false)
    onRefresh()
  }

  const puedoCalificar = finalizada && tu_oferta?.seleccionada && !ya_calificado

  return (
    <div className="border rounded-3 overflow-hidden bg-white">
      <CabeceraFigurita figurita={figurita_subastada} badge={badgeEstado} />

      <BarraTiempo
        finalizada={finalizada}
        tiempoRestante={tiempoRestante}
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
    </div>
  )
}

export default SubastaParticipo
