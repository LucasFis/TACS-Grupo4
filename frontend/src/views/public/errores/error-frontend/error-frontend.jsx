import styles from './error-frontend.module.css'

const ErrorFrontend = () => {
  const recargar = () => window.location.reload()

  return (
    <div className={styles.container}>
      <div className={styles.contenido}>
        <span className={styles.codigo}>Error</span>
        <h1 className={styles.titulo}>Algo salió mal</h1>
        <p className={styles.descripcion}>
          Ocurrió un error inesperado. Intentá recargar la página.
        </p>
        <button onClick={recargar} className={styles.boton}>
          Recargar página
        </button>
      </div>
    </div>
  )
}

export default ErrorFrontend