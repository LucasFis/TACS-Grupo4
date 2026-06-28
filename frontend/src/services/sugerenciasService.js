import { api, handleAxiosError } from './api.js'

const SUGERENCIAS_URL = '/sugerencias'

export const buscarSugerencias = async ({ pagina, limite }) => {
  try {
    const { data } = await api.get(`${SUGERENCIAS_URL}`, {
      params: { pagina, limite },
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