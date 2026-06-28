import BarraItem from './barra-item.jsx'
import styles from './administrador.module.css'

const SeccionBarras = ({ titulo, icono, iconoColor = 'var(--color-secondary)', config, data, dorado }) => {
  const items = config.map((c) => ({ ...c, valor: data[c.key] ?? 0 }))
  const total = items.reduce((s, i) => s + i.valor, 0)
  return (
    <div className={`${styles.seccionCard} ${dorado ? styles.seccionCardDorada : ''}`}>
      <p className={styles.seccionTitulo}>
        {icono && <i className={`bi ${icono} ${styles.seccionTituloIcono}`} style={{ color: iconoColor }} />}
        {titulo}
      </p>
      <div className="d-flex flex-column gap-3">
        {items.map((item) => (
          <BarraItem
            key={item.label}
            label={item.label}
            valor={item.valor}
            total={total}
            color={item.color}
          />
        ))}
      </div>
    </div>
  )
}

export default SeccionBarras
