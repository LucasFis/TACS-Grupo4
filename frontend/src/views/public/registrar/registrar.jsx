import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { crearAdministrador, crearUsuario, verificarNombre } from '@/services/usuarioService.js'
import { useToast } from '@/contexts/toastContext.jsx'
import { useAuth } from '@/contexts/userContext.jsx'
import { useError } from '@/contexts/errorContext.jsx'
import ModalInformativo from '@/components/ui/modales/modal-informativo/modal-informativo.jsx'
import TextoInput from '@/components/ui/input-texto/input-texto.jsx'
import ContraseniaInput from '@/components/ui/input-contrasenia/input-contrasenia.jsx'

function Registrar() {
  const [formData, setFormData] = useState({
    nombre: '',
    contrasenia: '',
    confirmarContrasenia: '',
    rol: 'USUARIO',
  })

  const { handleError } = useError()

  const [onSubmit, setOnSubmit] = useState(false)

  const [nombreTomado, setNombreTomado] = useState(false)

  const [tocado, setTocado] = useState({
    nombre: false,
    contrasenia: false,
    confirmarContrasenia: false,
  })

  const { showToast } = useToast()
  const { user } = useAuth()
  const navigate = useNavigate()

  const validarNombre = (nombre) => {
    return {
      sinEspacios: !/\s/.test(nombre),
      longitudMinima: nombre.length >= 3,
      caracteresValidos: /^[a-zA-Z0-9_.]*$/.test(nombre),
    }
  }

  const nombreChecks = validarNombre(formData.nombre)
  const nombreFormatoValido =
    formData.nombre.length > 0 && Object.values(nombreChecks).every(Boolean)
  const nombreEsValido = nombreFormatoValido && !nombreTomado

  const getErrorNombre = () => {
    if (formData.nombre.length === 0) return 'El nombre de usuario no puede estar vacío'
    if (nombreTomado) return 'Este nombre de usuario ya está en uso'
    if (!nombreChecks.sinEspacios) return 'El nombre de usuario no puede contener espacios'
    if (!nombreChecks.longitudMinima) return 'El nombre de usuario debe tener al menos 3 caracteres'
    if (!nombreChecks.caracteresValidos)
      return 'El nombre de usuario contiene caracteres no permitidos'
    return null
  }

  const errorNombre = getErrorNombre()

  const validarContrasenia = (contrasenia) => {
    return {
      longitud: contrasenia.length >= 8,
      mayuscula: /[A-Z]/.test(contrasenia),
      minuscula: /[a-z]/.test(contrasenia),
      numero: /[0-9]/.test(contrasenia),
      especial: /[^A-Za-z0-9]/.test(contrasenia),
    }
  }

  const passwordChecks = validarContrasenia(formData.contrasenia)
  const passwordEsValida = Object.values(passwordChecks).every(Boolean)

  const getErrorContrasenia = () => {
    if (formData.contrasenia.length === 0) return 'La contraseña no puede estar vacía'
    if (!passwordChecks.longitud) return 'La contraseña es demasiado corta'

    const faltantes = []
    if (!passwordChecks.mayuscula) faltantes.push('una mayúscula')
    if (!passwordChecks.minuscula) faltantes.push('una minúscula')
    if (!passwordChecks.numero) faltantes.push('un número')
    if (!passwordChecks.especial) faltantes.push('un carácter especial')

    if (faltantes.length === 0) return null
    return `La contraseña necesita ${faltantes.join(', ')}`
  }

  const errorContrasenia = getErrorContrasenia()

  const contraseniasCoinciden =
    formData.confirmarContrasenia.length > 0 &&
    formData.contrasenia === formData.confirmarContrasenia

  const getErrorConfirmacion = () => {
    if (formData.confirmarContrasenia.length === 0) return 'Confirmá tu contraseña'
    if (!contraseniasCoinciden) return 'Las contraseñas no coinciden'
    return null
  }

  const errorConfirmacion = getErrorConfirmacion()

  const handleChange = (e) => {
    if (e.target.name === 'nombre') setNombreTomado(false)
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    })
  }

  const handleBlur = (e) => {
    setTocado((prev) => ({ ...prev, [e.target.name]: true }))
  }

  const handleBlurNombre = async (e) => {
    handleBlur(e)
    if (!nombreFormatoValido) return
    try {
      const existe = await verificarNombre(formData.nombre)
      setNombreTomado(existe)
    } catch (error) {
      showToast(
        handleError(error, () => {}),
        'error',
      )
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()

    setTocado({ nombre: true, contrasenia: true, confirmarContrasenia: true })

    if (errorNombre || errorContrasenia || errorConfirmacion) {
      showToast('Revisá los campos marcados en rojo', 'error')

      if (errorNombre) {
        document.querySelector('input[name="nombre"]')?.focus()
      } else if (errorContrasenia) {
        document.querySelector('input[name="contrasenia"]')?.focus()
      } else {
        document.querySelector('input[name="confirmarContrasenia"]')?.focus()
      }
      return
    }

    const usuario = {
      nombre: formData.nombre,
      contrasenia: formData.contrasenia,
      rol: formData.rol,
    }

    try {
      setOnSubmit(true)
      if (user?.rol === 'ADMINISTRADOR') {
        await crearAdministrador(usuario)
      } else {
        await crearUsuario(usuario)
      }
      showToast(`Usuario creado correctamente`)
      navigate('/')
    } catch (error) {
      showToast(
        handleError(error, () => {}),
        'error',
      )
    } finally {
      setOnSubmit(false)
    }
  }

  return (
    <div
      className="container-fluid d-flex justify-content-center align-items-center py-5"
      style={{
        minHeight: '100vh',
        backgroundColor: 'var(--color-background)',
      }}
    >
      <div
        className="card shadow p-4"
        style={{
          width: '100%',
          maxWidth: '450px',
          backgroundColor: 'var(--color-primary)',
          border: 'var(--border-weight) solid var(--border-color-light)',
          borderRadius: '16px',
        }}
      >
        <h1
          className="text-center mb-2"
          style={{
            color: 'var(--color-secondary)',
          }}
        >
          Registrarse
        </h1>

        <p
          className="text-center mb-4"
          style={{
            color: 'var(--color-subtitle)',
          }}
        >
          Creá una cuenta nueva
        </p>

        <form onSubmit={handleSubmit}>
          <TextoInput
            name="nombre"
            label="Nombre de usuario"
            placeholder="juanperez123"
            value={formData.nombre}
            onChange={handleChange}
            onBlur={handleBlurNombre}
            tocado={tocado.nombre}
            esValido={nombreEsValido}
            error={errorNombre}
            hint='Debe ser único, sin espacios. Solo letras, números, "_" o ".".'
          />

          <ContraseniaInput
            name="contrasenia"
            label="Contraseña"
            value={formData.contrasenia}
            onChange={handleChange}
            onBlur={handleBlur}
            tocado={tocado.contrasenia}
            esValido={passwordEsValida}
            error={errorContrasenia}
            hint="La contraseña debe tener al menos 8 caracteres, incluyendo una mayúscula, una minúscula, un número y un carácter especial."
          />

          <ContraseniaInput
            name="confirmarContrasenia"
            label="Confirmar contraseña"
            value={formData.confirmarContrasenia}
            onChange={handleChange}
            onBlur={handleBlur}
            tocado={tocado.confirmarContrasenia}
            esValido={contraseniasCoinciden}
            error={errorConfirmacion}
          />

          {user?.rol === 'ADMINISTRADOR' && (
            <div className="mb-4">
              <label className="form-label">Rol</label>
              <select
                className="form-select"
                name="rol"
                onChange={handleChange}
                value={formData.rol}
                style={{ borderColor: 'var(--border-color-dark)' }}
              >
                <option value="USUARIO">USUARIO</option>
                <option value="ADMINISTRADOR">ADMINISTRADOR</option>
              </select>
            </div>
          )}

          <button
            type="submit"
            className="btn w-100 fw-bold"
            disabled={onSubmit}
            style={{
              backgroundColor: 'var(--color-secondary)',
              color: 'white',
            }}
          >
            {onSubmit ? 'Creando cuenta...' : 'Crear cuenta'}
          </button>
        </form>

        <div className="text-center mt-4">
          <p>
            ¿Ya tenés cuenta?{' '}
            <Link
              to="/login"
              style={{
                color: 'var(--color-terciary)',
                textDecoration: 'none',
                fontWeight: '600',
              }}
            >
              Iniciar sesión
            </Link>
          </p>
        </div>
      </div>
      <ModalInformativo open={onSubmit}>
        <h3>Registrando usuario...</h3>
        <p>Esto puede tardar unos segundos</p>
      </ModalInformativo>
    </div>
  )
}

export default Registrar
