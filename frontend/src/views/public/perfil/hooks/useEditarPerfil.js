import { useState } from 'react'
import { editarPerfil } from '@/services/perfilService.js'
import { editarContrasenia } from '@/services/usuarioService.js'
import { useToast } from '@/contexts/toastContext.jsx'
import { useError } from '@/contexts/errorContext.jsx'

export const useEditarPerfil = (perfil, columnaContraseniaRef) => {
  const [nombreEditando, setNombreEditando] = useState(perfil.nombre ?? '')
  const [nombreUsuarioEditando, setNombreUsuarioEditando] = useState(perfil.nombre_usuario ?? '')
  const [contraseniaActual, setContraseniaActual] = useState('')
  const [contraseniaNueva, setContraseniaNueva] = useState('')
  const [mediosEditando, setMediosEditando] = useState([...(perfil.medios_de_contacto ?? [])])
  const [guardado, setGuardado] = useState(null)

  const { showToast } = useToast()
  const { handleError } = useError()

  const guardarCambios = async () => {
    const errores = columnaContraseniaRef.current?.obtenerErrores()

    if (errores?.nombreUsuario || errores?.contraseniaNueva || errores?.contraseniaActual) {
      columnaContraseniaRef.current?.forzarTocado()
      showToast('Revisá los campos marcados en rojo', 'error')
      return
    }

    try {
      await editarPerfil({
        nombre: nombreEditando,
        nombreUsuario: nombreUsuarioEditando,
        mediosDeContacto: mediosEditando,
      })
      if (contraseniaNueva) {
        await editarContrasenia({ contraseniaActual, contraseniaNueva })
      }
      setGuardado({
        nombre: nombreEditando,
        nombre_usuario: nombreUsuarioEditando,
        medios_de_contacto: mediosEditando,
      })
      showToast('Perfil actualizado correctamente', 'success')
    } catch (error) {
      showToast(handleError(error, (m) => {}), 'error')
    }
  }

  return {
    nombreEditando,
    setNombreEditando,
    nombreUsuarioEditando,
    setNombreUsuarioEditando,
    contraseniaActual,
    setContraseniaActual,
    contraseniaNueva,
    setContraseniaNueva,
    mediosEditando,
    setMediosEditando,
    guardarCambios,
    guardado,
  }
}