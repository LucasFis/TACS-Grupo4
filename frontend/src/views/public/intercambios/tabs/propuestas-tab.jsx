import { useState } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import useQueryConError from '@/hooks/useQueryConError'
import IntercambioCard from './intercambio-card/intercambio-card.jsx'
import { buscarPropuestas } from '@/services/propuestasService.js'
import Paginacion from '@/components/ui/paginacion/paginacion.jsx'
import styles from '@/components/ui/actualizando-indicator/actualizando-indicator.module.css'

import FiltroIntercambio from '../filtro-intercambio/filtro-intercambio.jsx'

const TEXTOS_ESTADO = {
  '': 'Todas las propuestas',
  PENDIENTE: 'Esperando respuesta',
  ACEPTADO: 'Propuestas aceptadas',
  RECHAZADO: 'Propuestas rechazadas',
  CANCELADO: 'Propuestas canceladas',
}

const PropuestasTab = ({ tipo, estadoInicial = '' }) => {
  const [pagina, setPagina] = useState(1)
  const [estado, setEstado] = useState(estadoInicial)
  const [paramsFigurita, setParamsFigurita] = useState({})
  const queryClient = useQueryClient()
  const queryKey = ['propuestas', { pagina, limite: 10, estado, tipo, ...paramsFigurita }]

  const { data: propuestas, isLoading, isFetching } = useQueryConError({
    queryKey,
    queryFn: ({ signal }) => buscarPropuestas({ pagina, limite: 10, estado, tipo, ...paramsFigurita }, signal),
  })

  const cambiarFiltro = (nuevoEstado) => {
    setEstado((prev) => (prev === nuevoEstado ? prev : nuevoEstado))
    setPagina(1)
  }

  const handleAplicarFigurita = (params) => {
    setParamsFigurita(params)
    setPagina(1)
  }

  const onActualizado = () => {
    queryClient.invalidateQueries({ queryKey: ['propuestas'] })
  }

  return (
    <div className="container-fluid px-0 d-flex flex-column gap-4">
      <FiltroIntercambio
        estado={estado}
        onChangeEstado={cambiarFiltro}
        onAplicarFigurita={handleAplicarFigurita}
      />

      {isFetching && !isLoading && (
        <div className={styles.actualizando}>
          <div className={styles.spinnerChico} />
          <span>Actualizando...</span>
        </div>
      )}

      {isLoading ? (
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
            {TEXTOS_ESTADO[estado]} ({propuestas.cantidad_de_elementos})
          </p>
          <div className="d-flex flex-column gap-3">
            {propuestas.contenido.map((i) => (
              <IntercambioCard
                key={i.id}
                intercambio={i}
                tipo={tipo}
                onActualizado={onActualizado}
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
