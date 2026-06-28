import { api, handleAxiosError } from './api.js'

const mapearRanking = (lista) =>
  (lista ?? []).map((u) => ({
    perfilId: u.perfil_id,
    nombre: u.nombre,
    valor: u.valor,
    detalle: u.detalle ?? null,
  }))

export const obtenerEstadisticas = async (desde, hasta) => {
  try {
    const { data } = await api.get('/administrador/estadisticas', {
      params: { desde, hasta },
    })
    return {
      totalUsuarios: data.total_usuarios,
      totalFiguritasPublicadas: data.total_figuritas_publicadas,
      totalPropuestas: data.total_propuestas,
      totalSubastasActivas: data.total_subastas_activas,
      propuestasPorEstado: data.propuestas_por_estado,
      figuritasPorModalidad: data.figuritas_por_modalidad
        ? {
            soloIntercambio: data.figuritas_por_modalidad.solo_intercambio,
            soloSubasta: data.figuritas_por_modalidad.solo_subasta,
            ambos: data.figuritas_por_modalidad.ambos,
          }
        : undefined,
      rankings: data.rankings
        ? {
            topCreadoresDePropuestas: mapearRanking(data.rankings.top_creadores_de_propuestas),
            topIntercambiadores: mapearRanking(data.rankings.top_intercambiadores),
            mejorTasaAceptacion: mapearRanking(data.rankings.mejor_tasa_aceptacion),
            topSubastadores: mapearRanking(data.rankings.top_subastadores),
            mejorReputacion: mapearRanking(data.rankings.mejor_reputacion),
            topColeccionistas: mapearRanking(data.rankings.top_coleccionistas),
          }
        : undefined,
    }
  } catch (error) {
    handleAxiosError(error)
  }
}
