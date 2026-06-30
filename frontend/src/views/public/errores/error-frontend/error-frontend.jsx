import styles from './error-frontend.module.css'

const ErrorFrontend = () => {
  const recargar = () => window.location.reload()

  return (
    <div className={styles.container}>
      <div className={styles.contenido}>

        <div className={styles.icono} aria-hidden="true">
          <svg viewBox="0 0 80 80" xmlns="http://www.w3.org/2000/svg" className={styles.svg}>
            <circle cx="40" cy="40" r="38" fill="#175a2d" />
            <circle cx="40" cy="40" r="30" fill="#0e3d1e" />
            {/* Ventana/browser roto */}
            <rect x="22" y="26" width="36" height="28" rx="3" fill="none" stroke="#b1f5c7" strokeWidth="2.5" />
            <line x1="22" y1="33" x2="58" y2="33" stroke="#b1f5c7" strokeWidth="2.5" />
            <circle cx="27" cy="30" r="1.5" fill="#b1f5c7" />
            <circle cx="32" cy="30" r="1.5" fill="#b1f5c7" />
            <circle cx="37" cy="30" r="1.5" fill="#b1f5c7" />
            {/* X dorada dentro de la ventana */}
            <line x1="32" y1="39" x2="48" y2="49" stroke="#d49a2c" strokeWidth="3" strokeLinecap="round" />
            <line x1="48" y1="39" x2="32" y2="49" stroke="#d49a2c" strokeWidth="3" strokeLinecap="round" />
          </svg>
        </div>

        <span className={styles.codigo}>Error</span>
        <h1 className={styles.titulo}>Algo salió mal</h1>
        <p className={styles.descripcion}>
          Ocurrió un error inesperado en la aplicación. Intentá recargar la página.
        </p>
        <button onClick={recargar} className={styles.boton}>
          Recargar página
        </button>

      </div>
    </div>
  )
}

export default ErrorFrontend