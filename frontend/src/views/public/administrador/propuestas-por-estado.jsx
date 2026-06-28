import styles from './administrador.module.css'

const ESTADOS = [
  { label: 'Pendientes',    key: 'pendientes',    color: '#d49a2c' },
  { label: 'Seleccionadas', key: 'seleccionadas', color: '#7c3aed' },
  { label: 'Aceptadas',     key: 'aceptadas',     color: '#198754' },
  { label: 'Rechazadas',    key: 'rechazadas',     color: '#dc3545' },
  { label: 'Canceladas',    key: 'canceladas',    color: '#6c757d' },
]

const PropuestasPorEstado = ({ propuestasPorEstado, totalPropuestas }) => (
  <div className={styles.propuestasCard}>
    <div className={styles.propuestasBarras}>
      <p className={styles.seccionTitulo}>
        <i className="bi bi-pie-chart-fill" style={{ color: 'var(--color-secondary)' }} />
        Propuestas por estado
      </p>
      <div className="d-flex flex-column gap-3">
        {ESTADOS.map(({ label, key, color }) => {
          const valor = propuestasPorEstado?.[key] ?? 0
          const pct = totalPropuestas > 0
            ? Math.min(100, Math.round((valor / totalPropuestas) * 100))
            : 0
          return (
            <div key={key} className="d-flex flex-column gap-1">
              <div className="d-flex justify-content-between align-items-center">
                <div className="d-flex align-items-center gap-2">
                  <span style={{
                    width: 9,
                    height: 9,
                    borderRadius: '50%',
                    background: color,
                    display: 'inline-block',
                    flexShrink: 0,
                  }} />
                  <span style={{ fontSize: '0.85rem', color: 'var(--color-text)' }}>{label}</span>
                </div>
                <span style={{ fontSize: '0.85rem', fontWeight: 600 }}>{valor}</span>
              </div>
              <div style={{ background: '#e9ecef', borderRadius: 4, height: 7 }}>
                <div style={{
                  width: `${pct}%`,
                  background: color,
                  borderRadius: 4,
                  height: '100%',
                  transition: 'width 0.4s ease',
                }} />
              </div>
            </div>
          )
        })}
      </div>
    </div>
    <div className={styles.totalPropuestasCard}>
      <i className="bi bi-tags-fill" style={{ color: '#d49a2c', fontSize: '1.4rem' }} />
      <div className={styles.totalPropuestasNumero}>{totalPropuestas}</div>
      <div className={styles.totalPropuestasLabel}>Propuestas realizadas</div>
    </div>
  </div>
)

export default PropuestasPorEstado
