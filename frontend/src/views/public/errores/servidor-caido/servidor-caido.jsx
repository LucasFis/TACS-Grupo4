import styles from './servidor-caido.module.css'
import { useLocation, useNavigate } from 'react-router-dom'
import { ping } from '@/services/api'
import { useState } from 'react'

const ServidorCaido = () => {
  const navigate = useNavigate()
  const location = useLocation()
  const [reintentando, setReintentando] = useState(false)

  const previousPage = location.state?.from || '/'

  const reintentar = async () => {
    setReintentando(true)
    try {
      await ping()
      navigate(previousPage)
    } catch {
      // sigue caído
    } finally {
      setReintentando(false)
    }
  }

  return (
    <div className={styles.container}>
      <div className={styles.contenido}>

        <div className={styles.icono} aria-hidden="true">
          <svg viewBox="0 0 80 80" xmlns="http://www.w3.org/2000/svg" className={styles.svg}>
            <circle cx="40" cy="40" r="38" fill="#175a2d" />
            <circle cx="40" cy="40" r="30" fill="#0e3d1e" />
            <path d="M24 38 Q40 22 56 38" stroke="#b1f5c7" strokeWidth="3.5" fill="none" strokeLinecap="round" />
            <path d="M29 44 Q40 33 51 44" stroke="#b1f5c7" strokeWidth="3.5" fill="none" strokeLinecap="round" />
            <circle cx="40" cy="51" r="3.5" fill="#b1f5c7" />
            <line x1="30" y1="26" x2="50" y2="46" stroke="#d49a2c" strokeWidth="3" strokeLinecap="round" />
            <line x1="50" y1="26" x2="30" y2="46" stroke="#d49a2c" strokeWidth="3" strokeLinecap="round" />
          </svg>
        </div>

        <span className={styles.codigo}>503</span>
        <h1 className={styles.titulo}>El servidor no responde</h1>
        <p className={styles.descripcion}>
          No pudimos conectarnos. Revisá tu conexión o esperá unos minutos e intentá de nuevo.
        </p>

        <button
          onClick={reintentar}
          className={styles.boton}
          disabled={reintentando}
        >
          {reintentando ? 'Reintentando...' : 'Reintentar'}
        </button>

      </div>
    </div>
  )
}

export default ServidorCaido