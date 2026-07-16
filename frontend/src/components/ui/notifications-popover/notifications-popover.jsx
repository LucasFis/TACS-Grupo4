import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { obtenerNotificaciones, marcarTodasLeidas } from '@/services/notificacionesService.js'
import { useAuth } from '@/contexts/userContext.jsx'
import { useError } from '@/contexts/errorContext.jsx'
import { useToast } from '@/contexts/toastContext.jsx'
import { Spinner } from '@/components/ui/spinner/spinner.jsx'

const NotificationsPopover = () => {
  const { tieneSesion } = useAuth()
  const { handleError } = useError()
  const { showToast } = useToast()
  const navigate = useNavigate()

  const [abierto, setAbierto] = useState(false)
  const [cargando, setCargando] = useState(false)
  const [notificaciones, setNotificaciones] = useState([])
  const [noLeidas, setNoLeidas] = useState(0)
  const wrapperRef = useRef(null)

  useEffect(() => {
    if (tieneSesion) {
      const cargarNotificaciones = async () => {
        setCargando(true)
        try {
          const data = await obtenerNotificaciones()
          setNotificaciones(Array.isArray(data) ? data : data?.contenido ?? [])
          if (!Array.isArray(data)) {
            setNoLeidas(data?.no_leidas ?? 0)
          }
        } catch (error) {
          showToast(handleError(error, () => {}), 'error')
        } finally {
          setCargando(false)
        }
      }
      cargarNotificaciones()
    }
  }, [tieneSesion])

  useEffect(() => {
    const handleClickFuera = (e) => {
      if (wrapperRef.current && !wrapperRef.current.contains(e.target)) {
        setAbierto(false)
      }
    }
    document.addEventListener('mousedown', handleClickFuera)
    return () => document.removeEventListener('mousedown', handleClickFuera)
  }, [abierto])

  const toggleNotificaciones = async () => {
    if (abierto) {
      setAbierto(false)
    } else {
      setAbierto(true)
      setCargando(true)
      try {
        const data = await obtenerNotificaciones()
        setNotificaciones(Array.isArray(data) ? data : data?.contenido ?? [])
        if (!Array.isArray(data)) {
          setNoLeidas(data?.no_leidas ?? 0)
        }
      } catch (error) {
        showToast(handleError(error, () => {}), 'error')
      } finally {
        setCargando(false)
      }
    }
  }

  const handleMarcarTodasLeidas = async () => {
    try {
      await marcarTodasLeidas()
      setNotificaciones((prev) => prev.map((n) => ({ ...n, leida: true })))
      setNoLeidas(0)
    } catch (error) {
      showToast(handleError(error, () => {}), 'error')
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
      setAbierto(false)
      navigate(n.link)
      if (notificaciones.length > 0) {
        try {
          await marcarTodasLeidas()
          setNotificaciones((prev) => prev.map((x) => ({ ...x, leida: true })))
          setNoLeidas(0)
        } catch (error) {
          showToast(handleError(error, () => {}), 'error')
        }
      }
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
          <div className="navbar-notifications-actions">
            <button
              className="navbar-notifications-ver-todas"
              onClick={() => {
                setAbierto(false)
                navigate('/notificaciones')
              }}
              type="button"
            >
              Ver todas
            </button>
            {noLeidas > 0 && (
              <button
                className="navbar-notifications-marcar-leidas"
                onClick={handleMarcarTodasLeidas}
                type="button"
              >
                Marcar leídas
              </button>
            )}
          </div>

          <div className="navbar-notifications-header">Notificaciones</div>

          {cargando ? (
            <Spinner />
          ) : notificaciones.length === 0 ? (
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
