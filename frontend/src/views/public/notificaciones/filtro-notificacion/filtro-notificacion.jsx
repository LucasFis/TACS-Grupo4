import FilterChip from '@/components/ui/filter-chip/filter-chip.jsx'
import styles from './filtro-notificacion.module.css'

const TEXTOS_LEIDA = {
  'null': 'Todas',
  'true': 'Leídas',
  'false': 'No leídas',
}

const VALORES_LEIDA = {
  'null': null,
  'true': true,
  'false': false,
}

const FiltroNotificacion = ({ leida, onChangeLeida }) => {
  const leidaKey = leida === null ? 'null' : String(leida)

  return (
    <div className={styles.filtrosCard}>
      <div className={styles.filtrosRow}>
        <span className={styles.filtrosLabel}>Filtros:</span>
        {Object.entries(TEXTOS_LEIDA).map(([key, label]) => (
          <FilterChip
            key={key}
            label={label}
            selected={leidaKey === key}
            onClick={() => onChangeLeida(VALORES_LEIDA[key])}
          />
        ))}
      </div>
    </div>
  )
}

export default FiltroNotificacion
