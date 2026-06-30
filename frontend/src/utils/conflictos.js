export function construirAdvertenciaConflictos(conflictosData) {
  if (!conflictosData?.tieneConflictos) return null
  const { figuritasEnConflicto } = conflictosData
  const nombres = figuritasEnConflicto.map(f => f.jugador).join(', ')
  const plural = figuritasEnConflicto.length > 1
  return `${nombres} también está${plural ? 'n' : ''} involucrada${plural ? 's' : ''} en otras transacciones pendientes.`
}
