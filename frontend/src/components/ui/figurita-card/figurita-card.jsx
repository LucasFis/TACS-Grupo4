import { resolverTipo } from '@/utils/figuritas'
import styles from './figurita-card.module.css'
import CardActionBtn from './card-action-buttons'
import UserChip from '@/components/ui/user-chip/user-chip'

const TYPE_LABELS = {
  intercambio: { label: 'intercambio', className: styles.badgeIntercambio },
  subasta: { label: 'subasta', className: styles.badgeSubasta },
  ambos: { label: 'ambos', className: styles.badgeAmbos },
}

const CARD_CLASS = {
  intercambio: styles.cardIntercambio,
  subasta: styles.cardSubasta,
  ambos: styles.cardAmbos,
}

const CardHero = ({ strCutout, jugador, numero, tipo }) => {
  const badge = TYPE_LABELS[tipo] ?? TYPE_LABELS.intercambio
  return (
    <div className={styles.hero}>
      <img src={strCutout || '/jugador-placeholder.png'} alt={jugador} className={styles.heroImg} />
      <div className={styles.heroOverlay}>
        <span className={styles.heroNumber}>#{numero}</span>
        <span className={`${styles.badge} ${badge.className}`}>{badge.label}</span>
      </div>
    </div>
  )
}

const CardBody = ({
  jugador,
  seleccion,
  cantidadExistente,
  nombreUsuario,
  reputacion,
  extra,
  tipo,
  figuritaId,
  figurita,
}) => (
  <div className={styles.body}>
    <p className={styles.cardName}>{jugador}</p>
    <div className={styles.metaRow}>
      <p className={styles.cardSubtitle}>{seleccion}</p>
      {extra ? (
        <span className={styles.cardExtra}>{extra}</span>
      ) : (
        cantidadExistente !== undefined && (
          <span className={styles.disponibles}>{cantidadExistente} disponibles</span>
        )
      )}
    </div>

    {nombreUsuario && (
      <>
        <hr className={styles.divider} />
        <UserChip nombre={nombreUsuario} reputacion={reputacion} />
        <hr className={styles.divider} />
      </>
    )}

    {(tipo === 'intercambio' || tipo === 'ambos') && (
      <CardActionBtn to="/intercambios/crear" label="Proponer intercambio ↗" state={{ figurita }} />
    )}
    {(tipo === 'subasta' || tipo === 'ambos') && (
      <CardActionBtn
        to={`/subastas?figurita=${figuritaId}`}
        label="Ver subasta ↗"
        variant="subasta"
      />
    )}
  </div>
)

const FiguritaCard = ({
  figuritaId,
  numero,
  metodos = [],
  strCutout,
  jugador,
  seleccion,
  cantidadExistente,
  nombreUsuario,
  reputacion,
  extra,
  figurita,
}) => {
  const tipo = resolverTipo(metodos)

  return (
    <div className={`${styles.card} ${CARD_CLASS[tipo] ?? ''}`}>
      <CardHero strCutout={strCutout} jugador={jugador} numero={numero} tipo={tipo} />
      <CardBody
        jugador={jugador}
        seleccion={seleccion}
        cantidadExistente={cantidadExistente}
        nombreUsuario={nombreUsuario}
        reputacion={reputacion}
        extra={extra}
        tipo={tipo}
        figuritaId={figuritaId}
        figurita={figurita}
      />
    </div>
  )
}

export default FiguritaCard
