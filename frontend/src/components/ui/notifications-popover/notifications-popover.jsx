import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQueryClient } from '@tanstack/react-query'
import { obtenerNotificaciones, marcarTodasLeidas } from '@/services/notificacionesService.js'
import { useAuth } from '@/contexts/userContext.jsx'
import { useError } from '@/contexts/errorContext.jsx'
import { useToast } from '@/contexts/toastContext.jsx'
import useQueryConError from '@/hooks/useQueryConError'
import { Spinner } from '@/components/ui/spinner/spinner.jsx'
import styles from './notifications-popover.module.css'
import indicatorStyles from '@/components/ui/actualizando-indicator/actualizando-indicator.module.css'

const NotificationsPopover = () => {
  const { tieneSesion } = useAuth()
  const { handleError } = useError()
  const { showToast } = useToast()
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const [abierto, setAbierto] = useState(false)
  const wrapperRef = useRef(null)

  const { data, isLoading, isFetching, refetch } = useQueryConError({
    queryKey: ['notificaciones'],
    queryFn: ({ signal }) => obtenerNotificaciones({}, signal),
    enabled: tieneSesion,
  })

  const notificaciones = Array.isArray(data) ? data : data?.contenido ?? []
  const noLeidas = Array.isArray(data) ? 0 : data?.no_leidas ?? 0

  useEffect(() => {
    const handleClickFuera = (e) => {
      if (wrapperRef.current && !wrapperRef.current.contains(e.target)) {
        setAbierto(false)
      }
    }
    document.addEventListener('mousedown', handleClickFuera)
    return () => document.removeEventListener('mousedown', handleClickFuera)
  }, [])

  const toggleNotificaciones = () => {
    if (abierto) {
      setAbierto(false)
      return
    }
    setAbierto(true)
    refetch()
  }

  const handleMarcarTodasLeidas = async () => {
    try {
      await marcarTodasLeidas()
      queryClient.invalidateQueries({ queryKey: ['notificaciones'] })
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
          queryClient.invalidateQueries({ queryKey: ['notificaciones'] })
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

          {isFetching && !isLoading && (
            <div className={indicatorStyles.actualizando}>
              <div className={indicatorStyles.spinnerChico} />
              <span>Actualizando...</span>
            </div>
          )}

          <div className={styles.lista}>
            {isLoading ? (
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
