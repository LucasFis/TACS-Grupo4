import styles from './error-interno.module.css'
import { useNavigate } from 'react-router'

const ErrorInterno = () => {
  const navigate = useNavigate()

  return (
    <div className={styles.container}>
      <div className={styles.contenido}>

        <div className={styles.icono} aria-hidden="true">
          <svg viewBox="0 0 80 80" xmlns="http://www.w3.org/2000/svg" className={styles.svg}>
            <circle cx="40" cy="40" r="38" fill="#175a2d" />
            <circle cx="40" cy="40" r="30" fill="#0e3d1e" />
            {/* Engranaje */}
            <path
              d="M40 26 L42 22 L38 22 Z M40 54 L42 58 L38 58 Z M26 40 L22 42 L22 38 Z M54 40 L58 42 L58 38 Z
                 M29.5 29.5 L26.5 26 L24 28.5 Z M50.5 50.5 L53.5 54 L56 51.5 Z
                 M29.5 50.5 L26 53.5 L28.5 56 Z M50.5 29.5 L54 26.5 L51.5 24 Z"
              fill="#b1f5c7"
            />
            <circle cx="40" cy="40" r="10" fill="#0e3d1e" stroke="#b1f5c7" strokeWidth="3" />
            <circle cx="40" cy="40" r="4" fill="#b1f5c7" />
            {/* Rayo de error */}
            <line x1="40" y1="33" x2="40" y2="47" stroke="#d49a2c" strokeWidth="3" strokeLinecap="round" />
            <circle cx="40" cy="51" r="2" fill="#d49a2c" />
          </svg>
        </div>

        <span className={styles.codigo}>500</span>
        <h1 className={styles.titulo}>Error interno del servidor</h1>
        <p className={styles.descripcion}>
          Ocurrió un problema inesperado y no pudimos procesar tu solicitud. Intentá nuevamente en unos momentos.
        </p>

        <button
          onClick={() => navigate('/explorar')}
          className={styles.boton}
        >
          Volver al inicio
        </button>

      </div>
    </div>
  )
}

export default ErrorInterno