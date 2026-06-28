import { useRef } from 'react'
import styles from './administrador.module.css'

const FiltroPeriodo = ({ desde, setDesde, hasta, setHasta, rangoInvalido, aplicar, recargando }) => {
  const desdeRef = useRef(null)
  const hastaRef = useRef(null)

  return (
    <>
      <div className={styles.heroPeriodoFiltro}>
        <div className={styles.heroPeriodoCampo} onClick={() => desdeRef.current?.showPicker()}>
          <i className="bi bi-calendar3" style={{ color: '#9ca3af', fontSize: '0.9rem', flexShrink: 0 }} />
          <div style={{ display: 'flex', flexDirection: 'column', flex: 1 }}>
            <span className={styles.heroPeriodoCampoLabel}>Desde</span>
            <input
              ref={desdeRef}
              id="desde"
              type="date"
              className={styles.heroPeriodoInput}
              value={desde}
              onChange={(e) => setDesde(e.target.value)}
            />
          </div>
        </div>
        <span style={{ color: '#d1d5db', flexShrink: 0 }}>—</span>
        <div className={styles.heroPeriodoCampo} onClick={() => hastaRef.current?.showPicker()}>
          <i className="bi bi-calendar3" style={{ color: '#9ca3af', fontSize: '0.9rem', flexShrink: 0 }} />
          <div style={{ display: 'flex', flexDirection: 'column', flex: 1 }}>
            <span className={styles.heroPeriodoCampoLabel}>Hasta</span>
            <input
              ref={hastaRef}
              id="hasta"
              type="date"
              className={styles.heroPeriodoInput}
              value={hasta}
              onChange={(e) => setHasta(e.target.value)}
            />
          </div>
        </div>
        <button
          className={styles.aplicarBtn}
          onClick={aplicar}
          disabled={!!rangoInvalido || recargando}
        >
          {recargando
            ? <span className="spinner-border spinner-border-sm" role="status" />
            : 'Aplicar'}
        </button>
      </div>
      {rangoInvalido && (
        <span style={{ color: '#fca5a5', fontSize: '0.82rem' }}>
          &quot;Desde&quot; no puede ser posterior a &quot;Hasta&quot;.
        </span>
      )}
    </>
  )
}

export default FiltroPeriodo
