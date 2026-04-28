import styles from './figurita-tabs.module.css';

/**
 * FiguritaTabs
 *
 * Props:
 *  - activeTab   {string}    'repetidas' | 'faltantes' | 'propuestas'
 *  - onTabChange {function}  (tabKey: string) => void
 *  - stats       {object}    { publicadas, disponibles, enSubasta }
 */
// const TABS = [
//   { key: 'repetidas',  label: 'Repetidas' },
//   { key: 'faltantes',  label: 'Faltantes' },
//   { key: 'propuestas', label: 'Propuestas' },
// ];

const FiguritaTabs = ({ tabs, activeTab = 'repetidas', onTabChange, stats = {} }) => {
  const { publicadas = 0, disponibles = 0, enSubasta = 0 } = stats;

  return (
    <div className={styles.wrapper}>
      {/* Tab bar */}
      <div className={styles.tabBar}>
        {tabs.map(({ key, label }) => (
          <button
            key={key}
            className={`${styles.tab} ${activeTab === key ? styles.tabActive : ''}`}
            onClick={() => onTabChange?.(key)}
          >
            {label}
          </button>
        ))}
      </div>

      {/* Stats strip */}
      <div className={styles.statsRow}>
        <div className={styles.statBox}>
          <span className={styles.statValue}>{publicadas}</span>
          <span className={styles.statLabel}>Publicadas</span>
        </div>

        <div className={styles.statDivider} />

        <div className={styles.statBox}>
          <span className={styles.statValue}>{disponibles}</span>
          <span className={styles.statLabel}>Disponibles</span>
        </div>

        <div className={styles.statDivider} />

        <div className={styles.statBox}>
          <span className={`${styles.statValue} ${styles.statValueWarning}`}>
            {enSubasta}
          </span>
          <span className={styles.statLabel}>En subasta</span>
        </div>
      </div>
    </div>
  );
};

export default FiguritaTabs;
