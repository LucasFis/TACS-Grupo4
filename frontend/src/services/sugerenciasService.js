import { api, handleAxiosError } from './api.js'

const SUGERENCIAS_URL = '/sugerencias'

export const buscarSugerencias = async ({ pagina, limite }, signal) => {
  try {
    const { data } = await api.get(`${SUGERENCIAS_URL}`, {
      params: { pagina, limite },
      signal,
    })
    return data
  } catch (error) {
    handleAxiosError(error)
  }
}

export const alternarFavorito = async ({sugerenciaId}) => {
  try {
    const { data } = await api.patch(`${SUGERENCIAS_URL}/${sugerenciaId}/favorito`)
    return data
  } catch (error) {
    handleAxiosError(error)
  }
}

export const recalcularSugerencias = async () => {
  try {
    await api.post(`${SUGERENCIAS_URL}/recalcular`)
  } catch (error) {
    handleAxiosError(error)
  }
}