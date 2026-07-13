import styles from './insignia-fila.module.css'

const InsigniaFila = ({ fig, bloqueada, bloqueadaSinStock, seleccionada, sinStock, disponibles }) => {
  const sinStockReal = sinStock && !seleccionada

  return (
    <div className={styles['scroll-badges']}>
      {bloqueada && (
        <span className={styles['scroll-badge'] + ' ' + (bloqueadaSinStock ? styles['advertencia'] : styles['requerida'])}>
          {bloqueadaSinStock ? "Sin stock" : "Requerida"}
        </span>
      )}
      <span className={styles['scroll-badge'] + ' ' + (sinStockReal ? styles['sin-stock-badge'] : styles['cantidad'])}>
        {sinStockReal ? "Sin stock" : (
          <>
            ×{disponibles}
            {(fig.cantidad_reservada > 0 || seleccionada) && (
              <span className={styles['scroll-badge-total']}> de {fig.cantidad_existente}</span>
            )}
          </>
        )}
      </span>
    </div>
  )
}

export default InsigniaFila