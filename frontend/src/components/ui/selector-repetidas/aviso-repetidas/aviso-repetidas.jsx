import { Link } from 'react-router-dom'
import styles from './aviso-repetidas.module.css'

/**
 * Aviso que se muestra junto a un SelectorRepetidas para aclarar que solo
 * aparecen las repetidas publicadas con el método correspondiente.
 *
 * @param {'intercambio' | 'subasta'} metodo - método de intercambio del contexto actual
 */
const AvisoRepetidas = ({ metodo }) => {
  const otroMetodo = metodo === 'intercambio' ? 'subasta' : 'intercambio'

  return (
    <div className={styles.aviso}>
      <span className={styles.avisoIcono}>⚠️</span>
      <p className={styles.avisoTexto}>
        <strong>Solo aparecen tus repetidas publicadas para {metodo}.</strong>{' '}
        Si una figurita no aparece acá, probablemente la cargaste solo como{' '}
        <strong>{otroMetodo}</strong>. Podés cambiar su método en{' '}
        <Link to="/mis-figuritas">Mis Figuritas</Link>.
      </p>
    </div>
  )
}

export default AvisoRepetidas