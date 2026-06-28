import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Link, NavLink } from 'react-router-dom'
import './navbar.css'
import { useAuth } from '@/contexts/userContext.jsx'
import Button from '@/components/ui/button/button.jsx'
import { obtenerNotificaciones, marcarTodasLeidas } from '@/services/notificacionesService.js'
import { useToast } from '@/contexts/toastContext.jsx'
import { useError } from '@/contexts/errorContext.jsx'
import { buscarPerfil } from '@/services/perfilService.js'

const Navbar = () => {
  const { showToast } = useToast()
  const { handleError } = useError()

  const { user, tieneSesion } = useAuth()
  const [abierto, setAbierto] = useState(false)
  const [iniciales, setIniciales] = useState(undefined)
  const [notificaciones, setNotificaciones] = useState([])
  const wrapperRef = useRef(null)

  const NAV_LINKS = [
    { to: '/explorar', label: 'Explorar' },
    { to: '/mis-figuritas', label: 'Mis figuritas' },
    { to: '/intercambios', label: 'Intercambios' },
    { to: '/subastas', label: 'Subastas' },
    { to: '/estadisticas', label: 'Estadisticas', privilege: 'ADMINISTRADOR' },
    { to: '/registrar', label: 'Nuevo Admin', privilege: 'ADMINISTRADOR' },
  ]

  const noLeidas = Array.isArray(notificaciones) ? notificaciones.filter((n) => !n.leida).length : 0

  const cargarIniciales = async () => {
    try {
      const data = await buscarPerfil(user.perfilId)
      setIniciales(data.iniciales)
    } catch (error) {
      showToast(
        handleError(error, (m) => {}),
        'error',
      )
    }
  }

  // Carga inicial de notificaciones cuando hay sesión
  useEffect(() => {
    if (tieneSesion) {
      obtenerNotificaciones().then((data) => setNotificaciones(Array.isArray(data) ? data : []))
      cargarIniciales()
    }
  }, [tieneSesion])

  // Cierra el popover si se toca fuera
  useEffect(() => {
    const handleClickFuera = async (e) => {
      if (wrapperRef.current && !wrapperRef.current.contains(e.target)) {
        if (abierto) {
          await marcarTodasLeidas()
          setNotificaciones((prev) => prev.map((n) => ({ ...n, leida: true })))
        }
        setAbierto(false)
      }
    }
    document.addEventListener('mousedown', handleClickFuera)
    return () => document.removeEventListener('mousedown', handleClickFuera)
  }, [abierto])

  const toggleNotificaciones = async () => {
    if (abierto) {
      await marcarTodasLeidas()
      setNotificaciones((prev) => prev.map((n) => ({ ...n, leida: true })))
      setAbierto(false)
    } else {
      const data = await obtenerNotificaciones()
      setNotificaciones(Array.isArray(data) ? data : [])
      setAbierto(true)
    }
  }

  const formatearFecha = (fecha) => {
    if (!fecha) return ''
    return new Date(fecha).toLocaleDateString('es-AR', {
      day: '2-digit',
      month: 'short',
      hour: '2-digit',
      minute: '2-digit',
    })
  }

  const navigate = useNavigate()

  const handleClickNotificacion = async (n) => {
    if (n.link) {
      await marcarTodasLeidas()
      setNotificaciones((prev) => prev.map((x) => ({ ...x, leida: true })))
      setAbierto(false)
      navigate(n.link)
    }
  }

  const SesionActiva = () => (
    <div className="navbar-notifications-wrapper" ref={wrapperRef}>
      <button
        className="btn btn-link p-0 position-relative navbar-notification-btn"
        onClick={toggleNotificaciones}
        type="button"
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          width="22"
          height="22"
          fill="currentColor"
          viewBox="0 0 16 16"
        >
          <path d="M8 16a2 2 0 0 0 2-2H6a2 2 0 0 0 2 2zm.995-14.901a1 1 0 1 0-1.99 0A5.002 5.002 0 0 0 3 6c0 1.098-.5 6-2 7h14c-1.5-1-2-5.902-2-7a5.002 5.002 0 0 0-3.005-4.901z" />
        </svg>

        {noLeidas > 0 && (
          <span className="position-absolute top-0 start-100 translate-middle badge rounded-pill navbar-badge">
            {noLeidas}
          </span>
        )}
      </button>

      {abierto && (
        <div className="navbar-notifications-popover">
          <div className="navbar-notifications-header">Notificaciones</div>

          {notificaciones.length === 0 ? (
            <div className="navbar-notifications-empty">No tenés notificaciones</div>
          ) : (
            notificaciones.map((n) => (
              <div
                key={n.id}
                className={`navbar-notification-item ${!n.leida ? 'navbar-notification-item--no-leida' : ''}
                                ${n.link ? 'navbar-notification-item--clickeable' : ''}`}
                onClick={() => handleClickNotificacion(n)}
              >
                <div className="navbar-notification-texto">{n.cuerpo}</div>
                <div className="navbar-notification-fecha">{formatearFecha(n.fecha)}</div>
              </div>
            ))
          )}
        </div>
      )}

      <Link to="/perfil" className="navbar-avatar-link">
        <div className="rounded-circle d-flex align-items-center justify-content-center fw-bold navbar-avatar">
          {iniciales}
        </div>
      </Link>
    </div>
  )

  const SinSesion = () => (
    <>
      <Link to="/registrar">
        <Button label="Registrarse" />
      </Link>
      <Link to="/login">
        <Button label="Iniciar sesión" />
      </Link>
    </>
  )

  return (
    <nav className="navbar navbar-expand-lg navbar-custom">
      <div className="container navbar-container">
        <Link className="navbar-brand navbar-logo" to="/">
          <img
            src="/figunet_favicon.svg"
            alt="Figunet"
            height="32"
            width="32"
            style={{ borderRadius: '6px' }}
          />
          figunet.app
        </Link>
        {/* MOBILE */}
        <div className="d-flex align-items-center gap-3 ms-auto d-lg-none">
          {tieneSesion ? <SesionActiva /> : <SinSesion />}
          <button
            className="navbar-toggler border-0"
            type="button"
            data-bs-toggle="collapse"
            data-bs-target="#navbarContent"
          >
            <span className="navbar-toggler-icon navbar-toggler-custom" />
          </button>
        </div>

        {/* MENÚ CENTRAL */}
        <div className="collapse navbar-collapse" id="navbarContent">
          <ul className="navbar-nav mx-auto gap-2">
            {NAV_LINKS.map(({ to, label, privilege }) => {
              if (privilege && user?.rol !== privilege) return null

              return (
                <li className="nav-item" key={to}>
                  <NavLink
                    to={to}
                    className={({ isActive }) =>
                      `nav-link navbar-link ${isActive ? 'navbar-link--active' : ''}`
                    }
                  >
                    {label}
                  </NavLink>
                </li>
              )
            })}
          </ul>
        </div>

        {/* DESKTOP */}
        <div className="d-none d-lg-flex align-items-center gap-3">
          {tieneSesion ? <SesionActiva /> : <SinSesion />}
        </div>
      </div>
    </nav>
  )
}

export default Navbar
