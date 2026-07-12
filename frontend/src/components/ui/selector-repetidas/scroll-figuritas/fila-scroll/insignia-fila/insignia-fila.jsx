import styles from './insignia-fila.module.css'

const InsigniaFila = ({ fig, bloqueada, bloqueadaSinStock, seleccionada, sinStock, disponibles }) => {
  // Si el item está seleccionado y disponibles llegó a 0, es porque el usuario usó su último
  // ejemplar — no es "sin stock", sino "0 restantes". Solo mostramos "Sin stock" cuando el
  // item NO está seleccionado y realmente no hay copias libres para elegir.
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