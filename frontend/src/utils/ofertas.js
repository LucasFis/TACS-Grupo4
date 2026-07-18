export const obtenerBloqueadas = (repetidas, figuritasSolicitadas) => {
  if (!figuritasSolicitadas?.length) return []
  const idsRepetidas = new Set(repetidas.map((r) => r.figurita_id))
  return figuritasSolicitadas
    .filter((sol) => idsRepetidas.has(sol.id))
    .map((sol) => repetidas.find((r) => r.figurita_id === sol.id))
}

export const obtenerFaltantesRequeridas = (repetidas, figuritasSolicitadas) => {
  if (!figuritasSolicitadas?.length) return []
  const idsRepetidas = new Set(repetidas.map((r) => r.figurita_id))
  return figuritasSolicitadas.filter((sol) => !idsRepetidas.has(sol.id))
}

export const validarOferta = (repetidas, figuritasSolicitadas, calMinima, calificacionUsuario) => {
  const cumpleCalificacion =
    calMinima === 0 || (calificacionUsuario !== null && calificacionUsuario >= calMinima)
  const faltantesRequeridas = obtenerFaltantesRequeridas(repetidas, figuritasSolicitadas)
  const tieneTodasRequeridas = faltantesRequeridas.length === 0
  const puedeOfertar = cumpleCalificacion && tieneTodasRequeridas
  return { cumpleCalificacion, faltantesRequeridas, tieneTodasRequeridas, puedeOfertar }
}
