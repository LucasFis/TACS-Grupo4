import { useState, useEffect } from 'react'
import IntercambioCard from './intercambio-card/intercambio-card.jsx'
import { buscarPropuestas } from '@/services/propuestasService.js'
import Paginacion from '@/components/ui/paginacion/paginacion.jsx'
import { useError } from '@/contexts/errorContext.jsx'
import FiltroIntercambio from '../filtro-intercambio/filtro-intercambio.jsx'

const TEXTOS_ESTADO = {
  '': 'Todas las propuestas',
  PENDIENTE: 'Esperando respuesta',
  ACEPTADO: 'Propuestas aceptadas',
  RECHAZADO: 'Propuestas rechazadas',
  CANCELADO: 'Propuestas canceladas',
}

const PropuestasTab = ({ tipo, estadoInicial = '' }) => {
  const [loading, setLoading] = useState(true)
  const [pagina, setPagina] = useState(1)
  const [propuestas, setPropuestas] = useState([])
  const [filtros, setFiltros] = useState({ estado: estadoInicial, tipo })
  const [paramsFigurita, setParamsFigurita] = useState({})
  const { handleError } = useError()

  const cargarPropuestas = async () => {
    try {
      setLoading(true)
      const res = await buscarPropuestas({ pagina, limite: 10, ...filtros, ...paramsFigurita })
      setPropuestas(res)
    } catch (error) {
      handleError(error, () => {})
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    cargarPropuestas()
  }, [pagina, filtros, paramsFigurita])

  useEffect(() => {
    setFiltros({ estado: estadoInicial, tipo })
    setParamsFigurita({})
  }, [tipo])

  const cambiarFiltro = (nuevoEstado) => {
    setFiltros((prev) => (prev.estado === nuevoEstado ? prev : { ...prev, estado: nuevoEstado }))
    setPagina(1)
  }

  const handleAplicarFigurita = (params) => {
    setParamsFigurita(params)
    setPagina(1)
  }

  return (
    <div className="container-fluid px-0 d-flex flex-column gap-4">
      <FiltroIntercambio
        estado={filtros.estado}
        onChangeEstado={cambiarFiltro}
        onAplicarFigurita={handleAplicarFigurita}
      />

      {loading ? (
        <div className="d-flex flex-column gap-3">
          {[...Array(3)].map((_, i) => (
            <div key={i} className="rounded-3 placeholder-glow border" style={{ height: '140px' }}>
              <div className="placeholder w-100 h-100 rounded-3" />
            </div>
          ))}
        </div>
      ) : propuestas?.contenido?.length > 0 ? (
        <>
          <p className="mb-0">
            {TEXTOS_ESTADO[filtros.estado]} ({propuestas.cantidad_de_elementos})
          </p>
          <div className="d-flex flex-column gap-3">
            {propuestas.contenido.map((i) => (
              <IntercambioCard
                key={i.id}
                intercambio={i}
                tipo={tipo}
                onActualizado={cargarPropuestas}
              />
            ))}
          </div>
          <div className="pt-3 d-flex justify-content-center">
            <Paginacion
              page={pagina}
              totalPages={propuestas.cantidad_de_paginas ?? 1}
              onChange={setPagina}
            />
          </div>
        </>
      ) : (
        <div className="text-center text-muted py-5">
          <div className="fs-1">📭</div>
          <p className="mb-0">No hay resultados...</p>
        </div>
      )}
    </div>
  )
}

export default PropuestasTab
