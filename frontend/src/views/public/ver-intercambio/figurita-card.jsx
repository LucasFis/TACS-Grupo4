import styles from './ver-intercambio.module.css'

const FiguritaCard = ({ figurita }) => {
  if (!figurita) return null

  return (
    <div
      className="d-flex align-items-center gap-3 rounded-3 p-3"
      style={{ backgroundColor: '#E1F5EE' }}
    >
      <div className={styles.numeroFigurita}>#{figurita.numero}</div>
      <img
        src={figurita.imagen_url || '/jugador-placeholder.png'}
        alt={figurita.jugador}
        className="rounded-3"
        style={{ width: 48, height: 48, objectFit: 'cover', flexShrink: 0 }}
      />
      <div>
        <p className="mb-0 fw-semibold" style={{ fontSize: '0.9rem', color: '#085041' }}>
          {figurita.jugador}
        </p>
        <p className="mb-0" style={{ fontSize: '0.75rem', color: '#0F6E56' }}>
          {figurita.seleccion}
          {figurita.posicion ? ` · ${figurita.posicion}` : ''}
        </p>
      </div>
    </div>
  )
}

export default FiguritaCard
