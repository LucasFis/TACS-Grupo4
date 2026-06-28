import styles from './administrador.module.css'

const POSICION_COLORS = ['#d49a2c', '#94a3b8', '#b45309']

const RankingUsuarios = ({
  titulo,
  icono,
  iconoColor = 'var(--color-secondary)',
  items,
  sufijo = '',
  decimales = 0,
  dorado = false,
}) => {
  const lista = items ?? []

  return (
    <div className={`${styles.seccionCard} ${dorado ? styles.seccionCardDorada : ''}`}>
      <p className={styles.seccionTitulo}>
        {icono && (
          <i
            className={`bi ${icono} ${styles.seccionTituloIcono}`}
            style={{ color: iconoColor }}
          />
        )}
        {titulo}
      </p>

      {lista.length === 0 ? (
        <p className={`${styles.rankingVacio} mb-0`}>
          <i className="bi bi-bar-chart" />
          Sin datos para el período seleccionado.
        </p>
      ) : (
        <div>
          {lista.map((u, i) => (
            <div key={u.perfilId ?? `${u.nombre}-${i}`} className={styles.rankingItem}>
              <span
                className={styles.posicionBadge}
                style={{ background: POSICION_COLORS[i] ?? '#6c757d' }}
              >
                {i + 1}
              </span>
              <span className={styles.rankingNombre}>
                {u.nombre ?? 'Sin nombre'}
                {u.detalle && (
                  <span className={styles.rankingDetalle}> ({u.detalle})</span>
                )}
              </span>
              <span className={styles.rankingValor}>
                {u.valor.toFixed(decimales)}
                {sufijo}
              </span>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

export default RankingUsuarios
