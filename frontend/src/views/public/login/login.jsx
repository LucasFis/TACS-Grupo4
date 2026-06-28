import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { buscarUsuario, iniciarSesion } from '@/services/sesionService.js'
import { useToast } from '@/contexts/toastContext.jsx'
import { useError } from '@/contexts/errorContext.jsx'
import ModalInformativo from '@/components/ui/modales/modal-informativo/modal-informativo.jsx'
import TextoInput from '@/components/ui/input-texto/input-texto.jsx'
import ContraseniaInput from '@/components/ui/input-contrasenia/input-contrasenia.jsx'
import { useAuth } from '@/contexts/userContext.jsx'

function Login() {
  const [formData, setFormData] = useState({
    nombre: '',
    contrasenia: '',
  })

  const { handleError, errorTemplate } = useError()

  const [errorState, setErrorState] = useState(
    errorTemplate({ nombre: undefined, contrasenia: undefined }),
  )
  const [onSubmit, setOnSubmit] = useState(false)

  const { showToast } = useToast()
  const { asignarUsuario } = useAuth()
  const navigate = useNavigate()

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()

    if (!formData.nombre || !formData.contrasenia) {
      setErrorState('Completa todos los campos')
      return
    }

    try {
      setOnSubmit(true)
      await iniciarSesion({
        nombre: e.target.nombre.value,
        contrasenia: e.target.contrasenia.value,
      })
      await buscarUsuario(asignarUsuario)
      showToast(`Sesion iniciada correctamente`, 'success')
      navigate('/')
    } catch (error) {
      showToast(handleError(error, setErrorState), 'error')
    } finally {
      setOnSubmit(false)
    }
  }

  return (
    <div
      className="container-fluid d-flex justify-content-center align-items-center"
      style={{
        minHeight: '100vh',
        backgroundColor: 'var(--color-background)',
      }}
    >
      <div
        className="card shadow p-4"
        style={{
          width: '100%',
          maxWidth: '420px',
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
          Iniciar sesión
        </h1>

        <p
          className="text-center mb-4"
          style={{
            color: 'var(--color-subtitle)',
          }}
        >
          Ingresá tus credenciales
        </p>

        <form onSubmit={(e) => handleSubmit(e)}>
          <TextoInput
            name="nombre"
            label="Nombre Usuario"
            placeholder="Juan_perez"
            value={formData.nombre}
            onChange={handleChange}
          />

          <ContraseniaInput
            name="contrasenia"
            label="Contraseña"
            value={formData.contrasenia}
            onChange={handleChange}
          />

          <button
            type="submit"
            className="btn w-100 fw-bold"
            style={{
              backgroundColor: 'var(--color-secondary)',
              color: 'white',
            }}
          >
            Iniciar sesión
          </button>
        </form>

        <div className="text-center mt-4">
          <p>
            ¿No tenés cuenta?{' '}
            <Link
              to="/registrar"
              style={{
                color: 'var(--color-terciary)',
                textDecoration: 'none',
                fontWeight: '600',
              }}
            >
              Registrarse
            </Link>
          </p>
        </div>
      </div>

      <ModalInformativo open={onSubmit}>
        <h3>Iniciando sesion...</h3>
        <p>Esto puede tardar unos segundos</p>
      </ModalInformativo>
    </div>
  )
}

export default Login
