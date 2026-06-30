import styles from './figurita-chip.module.css'

const VARIANTE_CLASS = {
  verde: styles.itemVerde,
  azul: styles.itemAzul,
  neutro: styles.itemNeutro,
}

const FiguritaChip = ({ fig, variante = 'verde' }) => {
  const iniciales = fig.jugador?.split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase()

  return (
    <div className={`${VARIANTE_CLASS[variante]} d-flex align-items-center justify-content-between gap-2`}>
      <div className="d-flex align-items-center gap-2">
        <div className={styles.avatarCirculo}>
          {fig.imagen_url
            ? <img src={fig.imagen_url} alt={fig.jugador} className={styles.avatarImg} />
            : <span className={styles.avatarIniciales}>{iniciales}</span>
          }
        </div>
        <div>
          <div className={styles.jugadorNombre}>{fig.jugador}</div>
          {fig.numero && <div className={styles.jugadorNumero}>#{fig.numero}</div>}
        </div>
      </div>
      <span className={styles.seleccion}>{fig.seleccion}</span>
    </div>
  )
}

export default FiguritaChip
