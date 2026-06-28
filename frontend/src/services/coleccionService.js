import { api, handleAxiosError } from './api.js'

const COLECCIONES_URL = '/colecciones'

export const buscarFaltantes = async (filtros) => {
  try {
    const { data } = await api.get(`${COLECCIONES_URL}/faltantes`, { params: filtros })
    return data
  } catch (error) {
    handleAxiosError(error)
  }
}

export const buscarRepetidas = async (filtros) => {
  try {
    const { data } = await api.get(`${COLECCIONES_URL}/repetidas`, { params: filtros })

    return data
  } catch (error) {
    handleAxiosError(error)
  }
}

export const agregarFaltante = async (faltante) => {
  try {
    const { data } = await api.post(`${COLECCIONES_URL}/faltantes`, { fig_id: faltante.id })
    return data
  } catch (error) {
    handleAxiosError(error)
  }
}

export const agregarRepetida = async (repetida) => {
  try {
    const { data } = await api.post(`${COLECCIONES_URL}/repetidas`, {
      fig_id: repetida.id,
      cantidad_existente: repetida.cantidad,
      modos_intercambio: repetida.modosIntercambio,
    })

    return data
  } catch (error) {
    handleAxiosError(error)
  }
}

export const editarRepetida = async (figId, payload) => {
  try {
    const { data } = await api.patch(`${COLECCIONES_URL}/repetidas/${figId}`, {
      cantidad_nueva: payload.cantidadNueva,
      metodos: payload.metodos
    })

    return data
  } catch (error) {
    handleAxiosError(error)
  }
}
