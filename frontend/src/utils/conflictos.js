export function construirAdvertenciaConflictos(conflictosData) {
  if (!conflictosData?.tieneConflictos) return null
  const { figuritasEnConflicto } = conflictosData
  const nombres = figuritasEnConflicto.map(f => f.jugador).join(', ')
  return `Tenés otros intercambios o subastas activas donde también podrías conseguir a ${nombres}.\n\nAceptar este intercambio no las cancela automáticamente. Recordá cancelarlas o rechazarlas manualmente después.`
}
