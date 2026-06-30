import styles from './PerfilStats.module.css'

const PerfilStats = ({ stats }) => {

  return (
    <div className={styles.wrapper}>
      <div className={`mx-auto d-flex text-white ${styles.inner}`}>
        {stats.map((stat, i) => (
          <div key={i} className={styles['stat-item']}>
            <div className={styles['stat-valor']}>{stat.valor}</div>
            <div className={styles['stat-nombre']}>{stat.nombre}</div>
          </div>
        ))}
      </div>
    </div>
  )
}

export default PerfilStats
