import styles from './vista-no-encontrada.module.css'
import PaqueteVacio from './paquete-vacio.jsx'

const VistaNoEncontrada = () => {
  return (
    <div className={styles.container}>
      <div className={styles.wrapper}>

        <div className={styles.ilustracion}>
          <PaqueteVacio className={styles.paquete} />
        </div>

        <div className={styles.contenido}>
          <span className={styles.codigo}>404</span>
          <h1 className={styles.titulo}>Página no encontrada</h1>
          <p className={styles.descripcion}>
            La página que buscás no existe o fue movida. Revisá la dirección o volvé al inicio.
          </p>
          <a href="/" className={styles.boton}>
            Volver al inicio
          </a>
        </div>

      </div>
    </div>
  )
}

export default VistaNoEncontrada