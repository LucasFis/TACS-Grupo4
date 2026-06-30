import { api, handleAxiosError } from './api.js'

const PERFIL_URL = '/perfil'

export const buscarContadoresSugerencias = async () => {
  try {
    const { data } = await api.get(`${PERFIL_URL}/contadores`, {})
    return data
  } catch (error) {
    handleAxiosError(error)
  }
}

export const calificarPerfil = async (
  { destinatarioId, valor, descripcion, transactionId, tipoTransaccion },
) => {
  await api.post(`/perfil/calificaciones`, {
    destinatario_id: destinatarioId,
    valor,
    descripcion,
    transaction_id: transactionId,
    tipo_transaccion: tipoTransaccion,
  })
}

export const buscarPerfil = async (perfilId) => {
  try {

    const url = perfilId
      ? `/perfil/${perfilId}`
      : '/perfil'

    const { data } = await api.get(url)

    return data

  } catch (error) {
    handleAxiosError(error)
  }
}

export const buscarCalificaciones = async (
    perfilId,
    filtros
) => {
    try{
    const url = perfilId
        ? `/perfil/${perfilId}/calificaciones`
        : '/perfil/calificaciones'

    const { data } = await api.get(url,{
        params:filtros
    })

    return data
    } catch (error){
        handleAxiosError(error)
    }

}

export const buscarContadores = async (perfilId) => {
  try {
    const url = perfilId
      ? `${PERFIL_URL}/${perfilId}/contadores`
      : `${PERFIL_URL}/contadores`

    const { data } = await api.get(url)
    return data
  } catch (error) {
    handleAxiosError(error)
  }
}

export const editarPerfil = async ({ nombre, nombreUsuario, mediosDeContacto }) => {
  try {
    await api.put(PERFIL_URL, {
      nombre,
      nombre_usuario: nombreUsuario,
      medios_de_contacto: mediosDeContacto,
    })
  } catch (error) {
    handleAxiosError(error)
  }
}

export const editarContrasenia = async ({ contraseniaActual, contraseniaNueva }) => {
  try {
    await api.put(`${PERFIL_URL}/contrasenia`, {
      contrasenia_actual: contraseniaActual,
      contrasenia_nueva: contraseniaNueva,
    })
  } catch (error) {
    handleAxiosError(error)
  }
}
