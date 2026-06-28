import styles from './administrador.module.css'

const StatCard = ({ icono, numero, label, destacado }) => (
  <div className={`${styles.statCard} ${destacado ? styles.statCardDestacado : ''}`}>
    <div
      className={`${styles.statIconWrapper} ${destacado ? styles.statIconWrapperDestacado : ''}`}
    >
      <i
        className={`bi ${icono} ${styles.statIcon} ${destacado ? styles.statIconDestacado : ''}`}
      />
    </div>
    <div className={styles.statTexto}>
      <div className={`${styles.statNumero} ${destacado ? styles.statNumeroDestacado : ''}`}>
        {numero}
      </div>
      <div className={styles.statLabel}>{label}</div>
    </div>
  </div>
)

export default StatCard
