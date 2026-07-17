import { api, handleAxiosError } from './api.js'

const TIPO_MAP = {
  intercambio: 'INTERCAMBIO',
  subasta: 'SUBASTA',
}

const buildParams = ({ q, jugador, seleccion, numero, tipos, page, size }) => {
  const tipoParams = tipos && tipos.length > 0 ? tipos.map((t) => TIPO_MAP[t]) : undefined
  if (q) {
    return { q, tipo: tipoParams, pagina: page, tamanioPagina: size }
  }
  return {
    jugador: jugador || undefined,
    seleccion: seleccion ? seleccion.toUpperCase() : undefined,
    numero: numero || undefined,
    tipo: tipoParams,
    pagina: page,
    tamanioPagina: size,
  }
}

export const explorarFiguritas = async ({
  q,
  jugador,
  seleccion,
  numero,
  tipos = [],
  page = 0,
  size = 12,
} = {}, signal) => {
  try {
    const { data } = await api.get('/figuritas/intercambiables', {
      params: buildParams({ q, jugador, seleccion, numero, tipos, page, size }),
      signal,
    })
    return {
      contenido: data.contenido,
      totalElements: data.cantidad_de_elementos,
      totalPages: data.cantidad_de_paginas,
    }

  } catch (error) {
    handleAxiosError(error)
  }
}
