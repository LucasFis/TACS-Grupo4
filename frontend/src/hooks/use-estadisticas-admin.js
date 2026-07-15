import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { obtenerEstadisticas } from '@/services/administradorService.js'
import { useError } from '@/contexts/errorContext.jsx'
import { useToast } from '@/contexts/toastContext.jsx'

const formatDate = (date) => date.toLocaleDateString('en-CA')

const getDefaultRange = () => {
  const hasta = new Date()
  const desde = new Date()
  desde.setDate(desde.getDate() - 6)
  return { desde: formatDate(desde), hasta: formatDate(hasta) }
}

const useEstadisticasAdmin = () => {
  const defaultRange = getDefaultRange()
  const [desde, setDesde] = useState(defaultRange.desde)
  const [hasta, setHasta] = useState(defaultRange.hasta)
  const [pendingDesde, setPendingDesde] = useState(defaultRange.desde)
  const [pendingHasta, setPendingHasta] = useState(defaultRange.hasta)

  const { handleError } = useError()
  const { showToast } = useToast()

  const { data: stats, isLoading, isFetching, error } = useQuery({
    queryKey: ['estadisticas', { desde, hasta }],
    queryFn: ({ signal }) => obtenerEstadisticas(desde, hasta, signal),
    enabled: !!desde && !!hasta && desde <= hasta,
    onError: (error) => {
      if (error.code === 'ERR_CANCELED') return
      showToast(handleError(error, () => {}), 'error')
    },
  })

  const aplicar = () => {
    if (!pendingDesde || !pendingHasta || pendingDesde > pendingHasta) return
    setDesde(pendingDesde)
    setHasta(pendingHasta)
  }

  return {
    stats,
    cargando: isLoading,
    recargando: isFetching && !isLoading,
    error: error?.message ?? null,
    desde: pendingDesde,
    setDesde: setPendingDesde,
    hasta: pendingHasta,
    setHasta: setPendingHasta,
    rangoInvalido: pendingDesde && pendingHasta && pendingDesde > pendingHasta,
    aplicar,
  }
}

export default useEstadisticasAdmin
