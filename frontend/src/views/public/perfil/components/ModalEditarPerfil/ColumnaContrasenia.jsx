import { useState, useImperativeHandle, forwardRef } from 'react'
import { verificarNombre } from '@/services/usuarioService.js'
import { useToast } from '@/contexts/toastContext.jsx'
import { useError } from '@/contexts/errorContext.jsx'
import TextoInput from '@/components/ui/input-texto/input-texto.jsx'
import ContraseniaInput from '@/components/ui/input-contrasenia/input-contrasenia.jsx'
import styles from './columna.module.css'

const validarNombreUsuario = (nombre) => ({
  sinEspacios: !/\s/.test(nombre),
  longitudMinima: nombre.length >= 3,
  caracteresValidos: /^[a-zA-Z0-9_.]*$/.test(nombre),
})

const validarContrasenia = (contrasenia) => ({
  longitud: contrasenia.length >= 8,
  mayuscula: /[A-Z]/.test(contrasenia),
  minuscula: /[a-z]/.test(contrasenia),
  numero: /[0-9]/.test(contrasenia),
  especial: /[^A-Za-z0-9]/.test(contrasenia),
})

const getErrorContrasenia = (checks) => {
  if (!checks.longitud) return 'La contraseña es demasiado corta'
  const faltantes = []
  if (!checks.mayuscula) faltantes.push('una mayúscula')
  if (!checks.minuscula) faltantes.push('una minúscula')
  if (!checks.numero) faltantes.push('un número')
  if (!checks.especial) faltantes.push('un carácter especial')
  if (faltantes.length > 0) return `La contraseña necesita ${faltantes.join(', ')}`
  return null
}

const ColumnaContrasenia = forwardRef(({
  nombreUsuarioEditando,
  setNombreUsuarioEditando,
  contraseniaActual,
  setContraseniaActual,
  contraseniaNueva,
  setContraseniaNueva,
  nombreUsuarioOriginal,
}, ref) => {
  const { showToast } = useToast()
  const { handleError } = useError()

  const [tocadoNombreUsuario, setTocadoNombreUsuario] = useState(false)
  const [nombreUsuarioTomado, setNombreUsuarioTomado] = useState(false)
  const [tocadoActual, setTocadoActual] = useState(false)
  const [tocadoNueva, setTocadoNueva] = useState(false)

  const checksNombre = validarNombreUsuario(nombreUsuarioEditando)
  const nombreFormatoValido = nombreUsuarioEditando.length > 0 && Object.values(checksNombre).every(Boolean)
  const nombreEsValido = nombreFormatoValido && !nombreUsuarioTomado

  const getErrorNombreUsuario = () => {
    if (nombreUsuarioEditando.length === 0) return 'El nombre de usuario no puede estar vacío'
    if (!checksNombre.sinEspacios) return 'El nombre de usuario no puede contener espacios'
    if (!checksNombre.longitudMinima) return 'El nombre de usuario debe tener al menos 3 caracteres'
    if (!checksNombre.caracteresValidos) return 'El nombre de usuario contiene caracteres no permitidos'
    if (nombreUsuarioTomado) return 'Este nombre de usuario ya está en uso'
    return null
  }

  const checksContrasenia = validarContrasenia(contraseniaNueva)
  const contraseniaNuevaEsValida = Object.values(checksContrasenia).every(Boolean)
  const errorNueva = contraseniaNueva.length > 0 ? getErrorContrasenia(checksContrasenia) : null
  const errorActual =
    tocadoActual && contraseniaNueva.length > 0 && contraseniaActual.length === 0
      ? 'Ingresá tu contraseña actual para cambiarla'
      : null

  useImperativeHandle(ref, () => ({
    forzarTocado: () => {
      setTocadoNombreUsuario(true)
      setTocadoActual(true)
      setTocadoNueva(true)
    },
    obtenerErrores: () => ({
      nombreUsuario: getErrorNombreUsuario(),
      contraseniaNueva: errorNueva,
      contraseniaActual:
        contraseniaNueva.length > 0 && contraseniaActual.length === 0
          ? 'Ingresá tu contraseña actual para cambiarla'
          : null,
    }),
  }))

  const handleBlurNombreUsuario = async () => {
    setTocadoNombreUsuario(true)
    if (!nombreFormatoValido) return
    if (nombreUsuarioEditando === nombreUsuarioOriginal) return
    try {
      const existe = await verificarNombre(nombreUsuarioEditando)
      setNombreUsuarioTomado(existe)
    } catch (error) {
      showToast(handleError(error, () => {}), 'error')
    }
  }

  return (
    <div className="col-12 col-md-6">
      <div className="d-flex align-items-center gap-2 mb-3">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="#175A2D" viewBox="0 0 16 16">
          <path d="M8 1a2 2 0 0 1 2 2v4H6V3a2 2 0 0 1 2-2Zm3 6V3a3 3 0 0 0-6 0v4a2 2 0 0 0-2 2v5a2 2 0 0 0 2 2h6a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2Z" />
        </svg>
        <span className={styles['seccion-titulo']}>Cuenta</span>
      </div>
      <p className={styles['seccion-subtitulo']}>Actualizá tu nombre de usuario y contraseña</p>

      <TextoInput
        name="nombreUsuario"
        label="Nombre de usuario"
        placeholder={nombreUsuarioOriginal}
        value={nombreUsuarioEditando}
        onChange={(e) => {
          setNombreUsuarioTomado(false)
          setNombreUsuarioEditando(e.target.value)
        }}
        onBlur={handleBlurNombreUsuario}
        tocado={tocadoNombreUsuario}
        esValido={nombreEsValido}
        error={getErrorNombreUsuario()}
        hint='Sin espacios. Solo letras, números, "_" o ".".'
      />

      <ContraseniaInput
        name="contraseniaActual"
        label="Contraseña actual"
        placeholder="Dejar vacío para no cambiar"
        value={contraseniaActual}
        onChange={(e) => setContraseniaActual(e.target.value)}
        onBlur={() => setTocadoActual(true)}
        tocado={tocadoActual}
        esValido={contraseniaActual.length > 0}
        error={errorActual}
      />

      <ContraseniaInput
        name="contraseniaNueva"
        label="Nueva contraseña"
        placeholder="Dejar vacío para no cambiar"
        value={contraseniaNueva}
        onChange={(e) => setContraseniaNueva(e.target.value)}
        onBlur={() => setTocadoNueva(true)}
        tocado={tocadoNueva}
        esValido={contraseniaNueva.length > 0 && contraseniaNuevaEsValida}
        error={errorNueva}
        hint="Al menos 8 caracteres, mayúscula, minúscula, número y carácter especial."
      />
    </div>
  )
})

export default ColumnaContrasenia