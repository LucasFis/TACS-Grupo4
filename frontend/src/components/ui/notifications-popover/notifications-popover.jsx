import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { obtenerNotificaciones, marcarTodasLeidas } from '@/services/notificacionesService.js'
import { useAuth } from '@/contexts/userContext.jsx'
import { useError } from '@/contexts/errorContext.jsx'
import { useToast } from '@/contexts/toastContext.jsx'

const NotificationsPopover = () => {
  const { tieneSesion } = useAuth()
  const { handleError } = useError()
  const { showToast } = useToast()
  const navigate = useNavigate()

  const [abierto, setAbierto] = useState(false)
  const [notificaciones, setNotificaciones] = useState([])
  const wrapperRef = useRef(null)

  const noLeidas = Array.isArray(notificaciones) ? notificaciones.filter((n) => !n.leida).length : 0

  useEffect(() => {
    if (tieneSesion) {
      const cargarNotificaciones = async () => {
        try {
          const data = await obtenerNotificaciones()
          setNotificaciones(Array.isArray(data) ? data : data?.contenido ?? [])
        } catch (error) {
          showToast(handleError(error, () => {}), 'error')
        }
      }
      cargarNotificaciones()
    }
  }, [tieneSesion])

  useEffect(() => {
    const handleClickFuera = async (e) => {
      if (wrapperRef.current && !wrapperRef.current.contains(e.target)) {
        try {
          if (abierto && notificaciones.length > 0) {
            await marcarTodasLeidas()
            setNotificaciones((prev) => prev.map((n) => ({ ...n, leida: true })))
          }
        } catch (error) {
          showToast(handleError(error, () => {}), 'error')
        } finally {
          setAbierto(false)
        }
      }
    }
    document.addEventListener('mousedown', handleClickFuera)
    return () => document.removeEventListener('mousedown', handleClickFuera)
  }, [abierto])

  const toggleNotificaciones = async () => {
    if (abierto) {
      if (notificaciones.length > 0) {
        try {
          await marcarTodasLeidas()
          setNotificaciones((prev) => prev.map((n) => ({ ...n, leida: true })))
        } catch (error) {
          showToast(handleError(error, () => {}), 'error')
        } finally {
          setAbierto(false)
        }
      } else {
        setAbierto(false)
      }
    } else {
      try {
        const data = await obtenerNotificaciones()
        setNotificaciones(Array.isArray(data) ? data : data?.contenido ?? [])
      } catch (error) {
        showToast(handleError(error, () => {}), 'error')
      }
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

  const handleClickNotificacion = async (n) => {
    if (n.link) {
      if (notificaciones.length > 0) {
        try {
          await marcarTodasLeidas()
          setNotificaciones((prev) => prev.map((x) => ({ ...x, leida: true })))
        } catch (error) {
          showToast(handleError(error, () => {}), 'error')
        }
      }
      setAbierto(false)
      navigate(n.link)
    }
  }

  return (
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
          <button
            className="navbar-notifications-ver-todas"
            onClick={() => { setAbierto(false); navigate('/notificaciones'); }}
            type="button"
          >
            Ver todas
          </button>

          <div className="navbar-notifications-header">Notificaciones</div>

          {notificaciones.length === 0 ? (
            <div className="navbar-notifications-empty">No tenés notificaciones</div>
          ) : (
            notificaciones.map((n) => (
              <div
                key={n.id}
                className={`navbar-notification-item ${n.leida ? 'navbar-notification-item--leida' : 'navbar-notification-item--no-leida'}
                                ${n.link ? 'navbar-notification-item--clickeable' : ''}`}
                onClick={() => handleClickNotificacion(n)}
              >
                <div className="navbar-notification-texto">{n.cuerpo}</div>
                <div className="navbar-notification-fecha">{formatearFecha(n.fecha)}</div>
                {n.leida && <span className="navbar-notification-leida-badge">leída</span>}
              </div>
            ))
          )}
        </div>
      )}
    </div>
  )
}

export default NotificationsPopover
