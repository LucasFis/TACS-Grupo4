import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { obtenerNotificaciones, marcarTodasLeidas } from '@/services/notificacionesService.js'
import { useAuth } from '@/contexts/userContext.jsx'
import { useError } from '@/contexts/errorContext.jsx'
import { useToast } from '@/contexts/toastContext.jsx'
import { Spinner } from '@/components/ui/spinner/spinner.jsx'
import styles from './notifications-popover.module.css'
import indicatorStyles from '@/components/ui/actualizando-indicator/actualizando-indicator.module.css'

const CACHE_KEY = 'notificaciones_cache'

const leerCache = () => {
  try {
    const raw = localStorage.getItem(CACHE_KEY)
    if (!raw) return null
    return JSON.parse(raw)
  } catch { return null } // JSON invÃ¡lido en localStorage
}

const guardarCache = (notificaciones, noLeidas) => {
  try {
    localStorage.setItem(CACHE_KEY, JSON.stringify({ notificaciones, noLeidas }))
  } catch { /* localStorage lleno o no disponible */ }
}

const NotificationsPopover = () => {
  const { tieneSesion } = useAuth()
  const { handleError } = useError()
  const { showToast } = useToast()
  const navigate = useNavigate()

  const [abierto, setAbierto] = useState(false)
  const [cargando, setCargando] = useState(false)
  const [actualizando, setActualizando] = useState(false)
  const [notificaciones, setNotificaciones] = useState([])
  const [noLeidas, setNoLeidas] = useState(0)
  const wrapperRef = useRef(null)
  const abortRef = useRef(null)

  useEffect(() => {
    if (!tieneSesion) return
    const cache = leerCache()
    if (cache) {
      setNotificaciones(cache.notificaciones)
      setNoLeidas(cache.noLeidas)
    }
    const cargar = async () => {
      if (!cache) setCargando(true)
      try {
        const data = await obtenerNotificaciones()
        const lista = Array.isArray(data) ? data : data?.contenido ?? []
        const leidas = Array.isArray(data) ? 0 : data?.no_leidas ?? 0
        setNotificaciones(lista)
        setNoLeidas(leidas)
        guardarCache(lista, leidas)
      } catch (error) {
        if (!cache) showToast(handleError(error, () => {}), 'error')
      } finally {
        setCargando(false)
      }
    }
    cargar()
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
      return
    }
    if (abortRef.current) abortRef.current.abort()
    const controller = new AbortController()
    abortRef.current = controller
    setAbierto(true)
    const cache = leerCache()
    if (cache) {
      setNotificaciones(cache.notificaciones)
      setNoLeidas(cache.noLeidas)
      setActualizando(true)
    } else {
      setCargando(true)
    }
    try {
      const data = await obtenerNotificaciones({}, controller.signal)
      if (controller.signal.aborted) return
      const lista = Array.isArray(data) ? data : data?.contenido ?? []
      const leidas = Array.isArray(data) ? 0 : data?.no_leidas ?? 0
      setNotificaciones(lista)
      setNoLeidas(leidas)
      guardarCache(lista, leidas)
    } catch (error) {
      if (controller.signal.aborted) return
      if (!cache) showToast(handleError(error, () => {}), 'error')
    } finally {
      if (!controller.signal.aborted) {
        setCargando(false)
        setActualizando(false)
      }
    }
  }

  const handleMarcarTodasLeidas = async () => {
    try {
      await marcarTodasLeidas()
      const actualizadas = notificaciones.map((n) => ({ ...n, leida: true }))
      setNotificaciones(actualizadas)
      setNoLeidas(0)
      guardarCache(actualizadas, 0)
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
          const actualizadas = notificaciones.map((x) => ({ ...x, leida: true }))
          setNotificaciones(actualizadas)
          setNoLeidas(0)
          guardarCache(actualizadas, 0)
        } catch (error) {
          showToast(handleError(error, () => {}), 'error')
        }
      }
    }
  }

  return (
    <div className={styles.wrapper} ref={wrapperRef}>
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
        <div className={styles.popover}>
          <div className={styles.actions}>
            <button
              className={styles.verTodas}
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
                className={styles.marcarLeidas}
                onClick={handleMarcarTodasLeidas}
                type="button"
              >
                Marcar leÃ­das
              </button>
            )}
          </div>

          <div className={styles.header}>Notificaciones</div>

          {actualizando && (
            <div className={indicatorStyles.actualizando}>
              <div className={indicatorStyles.spinnerChico} />
              <span>Actualizando...</span>
            </div>
          )}

          <div className={styles.lista}>
            {cargando ? (
              <div className={styles.cargando}><Spinner /></div>
            ) : notificaciones.length === 0 ? (
              <div className={styles.empty}>No tenÃ©s notificaciones</div>
            ) : (
              notificaciones.map((n) => (
                <div
                  key={n.id}
                  className={`${n.leida ? styles.itemLeida : styles.itemNoLeida}
                                  ${n.link ? styles.itemClickeable : ''} ${styles.item}`}
                  onClick={() => handleClickNotificacion(n)}
                >
                  <div className={styles.texto}>{n.cuerpo}</div>
                  <div className={styles.fecha}>{formatearFecha(n.fecha)}</div>
                  {n.leida && <span className={styles.leidaBadge}>leÃ­da</span>}
                </div>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  )
}

export default NotificationsPopover
