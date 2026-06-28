import { useEffect, useState } from 'react'
import { obtenerEstadisticas } from '@/services/administradorService.js'

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
  const [stats, setStats] = useState(null)
  const [cargando, setCargando] = useState(true)
  const [recargando, setRecargando] = useState(false)
  const [error, setError] = useState(null)

  useEffect(() => {
    if (!desde || !hasta || desde > hasta) return

    if (stats) {
      setRecargando(true)
    } else {
      setCargando(true)
    }
    setError(null)
    obtenerEstadisticas(desde, hasta)
      .then(setStats)
      .catch((err) => setError(err?.message ?? 'Error al cargar estadísticas'))
      .finally(() => {
        setCargando(false)
        setRecargando(false)
      })
  }, [desde, hasta])

  const aplicar = () => {
    if (!pendingDesde || !pendingHasta || pendingDesde > pendingHasta) return
    setDesde(pendingDesde)
    setHasta(pendingHasta)
  }

  return {
    stats,
    cargando,
    recargando,
    error,
    desde: pendingDesde,
    setDesde: setPendingDesde,
    hasta: pendingHasta,
    setHasta: setPendingHasta,
    rangoInvalido: pendingDesde && pendingHasta && pendingDesde > pendingHasta,
    aplicar,
  }
}

export default useEstadisticasAdmin
