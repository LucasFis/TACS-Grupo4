import { useEffect, useState } from 'react'
import { Link, NavLink } from 'react-router-dom'
import './navbar.css'
import { useAuth } from '@/contexts/userContext.jsx'
import Button from '@/components/ui/button/button.jsx'
import { useToast } from '@/contexts/toastContext.jsx'
import { useError } from '@/contexts/errorContext.jsx'
import { buscarPerfil } from '@/services/perfilService.js'
import NotificationsPopover from '@/components/ui/notifications-popover/notifications-popover.jsx'

const Navbar = () => {
  const { showToast } = useToast()
  const { handleError } = useError()

  const { user, tieneSesion } = useAuth()
  const [iniciales, setIniciales] = useState(undefined)

  const NAV_LINKS = [
    { to: '/explorar', label: 'Explorar' },
    { to: '/mis-figuritas', label: 'Mis figuritas' },
    { to: '/intercambios', label: 'Intercambios' },
    { to: '/subastas', label: 'Subastas' },
    { to: '/estadisticas', label: 'Estadisticas', privilege: 'ADMINISTRADOR' },
    { to: '/registrar', label: 'Nuevo Admin', privilege: 'ADMINISTRADOR' },
  ]

  const cargarIniciales = async () => {
    try {
      const data = await buscarPerfil(user.perfilId)
      setIniciales(data.iniciales)
    } catch (error) {
      showToast(
        handleError(error, () => {}),
        'error',
      )
    }
  }

  useEffect(() => {
    if (tieneSesion) {
      cargarIniciales()
    }
  }, [tieneSesion])

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
          {tieneSesion ? (
            <>
              <NotificationsPopover />
              <Link to="/perfil" className="navbar-avatar-link">
                <div className="rounded-circle d-flex align-items-center justify-content-center fw-bold navbar-avatar">
                  {iniciales}
                </div>
              </Link>
            </>
          ) : (
            <>
              <Link to="/registrar">
                <Button label="Registrarse" />
              </Link>
              <Link to="/login">
                <Button label="Iniciar sesión" />
              </Link>
            </>
          )}
        </div>

        {/* MENÚ CENTRAL */}
        <div className="collapse navbar-collapse" id="navbarContent">
          <ul className="navbar-nav mx-auto gap-2">
            {NAV_LINKS.map(({ to, label, privilege }) => {
              if (privilege && user?.rol !== privilege) return null
              if (user?.rol === 'ADMINISTRADOR' && !privilege) return null

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
          {tieneSesion ? (
            <>
              <NotificationsPopover />
              <Link to="/perfil" className="navbar-avatar-link">
                <div className="rounded-circle d-flex align-items-center justify-content-center fw-bold navbar-avatar">
                  {iniciales}
                </div>
              </Link>
            </>
          ) : (
            <>
              <Link to="/registrar">
                <Button label="Registrarse" />
              </Link>
              <Link to="/login">
                <Button label="Iniciar sesión" />
              </Link>
            </>
          )}
        </div>
      </div>
    </nav>
  )
}

export default Navbar
