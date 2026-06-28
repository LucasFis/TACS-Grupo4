import { api, handleAxiosError } from './api.js'

export const crearUsuario = async ({ nombre, contrasenia }) => {
  try {
    const { data } = await api.post('/usuarios', { nombre, contrasenia, rol: 'USUARIO' })

    return data
  } catch (error) {
    handleAxiosError(error)
  }
}

export const crearAdministrador = async ({ nombre, contrasenia, rol }) => {
  try {
    const { data } = await api.post('/administradores', { nombre, contrasenia, rol })

    return data
  } catch (error) {
    handleAxiosError(error)
  }
}

export const editarContrasenia = async ({ contraseniaActual, contraseniaNueva }) => {
  try {
    await api.put('/usuarios/contrasenia', {
      contrasenia_actual: contraseniaActual,
      contrasenia_nueva: contraseniaNueva,
    })
  } catch (error) {
    handleAxiosError(error)
  }
}

export const verificarNombre = async (nombre) => {
  try {
    await api.head(`/usuarios/${nombre}`)
    return true
  } catch (error) {
    if (error.response?.status === 404) return false
    handleAxiosError(error)
  }
}
