import styles from './acceso-denegado.module.css'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '@/contexts/userContext.jsx'

const AccesoDenegado = () => {
  const navigate = useNavigate()
  const { user } = useAuth()

  return (
    <div className={styles.container}>
      <div className={styles.contenido}>

        <div className={styles.icono} aria-hidden="true">
          <svg viewBox="0 0 80 80" xmlns="http://www.w3.org/2000/svg" className={styles.svg}>
            <circle cx="40" cy="40" r="38" fill="#175a2d" />
            <circle cx="40" cy="40" r="30" fill="#0e3d1e" />
            {/* Candado */}
            <rect x="27" y="38" width="26" height="20" rx="3" fill="#b1f5c7" />
            <path
              d="M32 38 V32 Q32 24 40 24 Q48 24 48 32 V38"
              fill="none"
              stroke="#b1f5c7"
              strokeWidth="3.5"
              strokeLinecap="round"
            />
            <circle cx="40" cy="48" r="3" fill="#0e3d1e" />
            <rect x="39" y="48" width="2" height="5" rx="1" fill="#0e3d1e" />
            {/* X dorada si no tiene permisos */}
            {user && (
              <>
                <line x1="33" y1="25" x2="47" y2="39" stroke="#d49a2c" strokeWidth="3" strokeLinecap="round" />
                <line x1="47" y1="25" x2="33" y2="39" stroke="#d49a2c" strokeWidth="3" strokeLinecap="round" />
              </>
            )}
          </svg>
        </div>

        <span className={styles.codigo}>403</span>

        {user ? (
          <>
            <h1 className={styles.titulo}>Acceso denegado</h1>
            <p className={styles.descripcion}>
              No tenés los privilegios necesarios para acceder a esta página.
            </p>
            <button onClick={() => navigate('/explorar')} className={styles.boton}>
              Volver al inicio
            </button>
          </>
        ) : (
          <>
            <h1 className={styles.titulo}>Necesitás iniciar sesión</h1>
            <p className={styles.descripcion}>
              Esta página requiere una cuenta activa para poder acceder.
            </p>
            <div className={styles.acciones}>
              <button onClick={() => navigate('/login')} className={styles.boton}>
                Iniciar sesión
              </button>
              <button onClick={() => navigate('/registrar')} className={`${styles.boton} ${styles.botonSecundario}`}>
                Registrarse
              </button>
            </div>
          </>
        )}

      </div>
    </div>
  )
}

export default AccesoDenegado