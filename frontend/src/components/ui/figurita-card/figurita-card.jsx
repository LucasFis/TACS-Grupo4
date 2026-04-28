import styles from './figurita-card.module.css';

/**
 * FiguritaCard
 *
 * Props:
 *  - number      {string|number}  Número de figurita (ej: 14)
 *  - type        {string}         'intercambio' | 'subasta' | 'ambos'
 *  - emoji       {string}         Emoji o ícono representativo
 *  - name        {string}         Nombre del jugador / estadio
 *  - subtitle    {string}         "País · Posición" o categoría
 *  - available   {number}         Cantidad disponible
 *  - actionLabel {string}         Texto del botón principal
 *  - onAction    {function}       Callback del botón
 *  - extra       {string}         Texto extra debajo de "Disponibles" (ej: "Subasta: 2h 14m")
 */
const TYPE_LABELS = {
  intercambio: { label: 'intercambio', className: styles.badgeIntercambio },
  subasta:     { label: 'subasta',     className: styles.badgeSubasta },
  ambos:       { label: 'ambos',       className: styles.badgeAmbos },
};

const FiguritaCard = ({
  number,
  type = 'intercambio',
  emoji,
  name,
  subtitle,
  available,
  actionLabel = 'Ver propuestas',
  onAction,
  extra,
}) => {
  const badge = TYPE_LABELS[type] ?? TYPE_LABELS.intercambio;

  return (
    <div className={`${styles.card} ${type === 'subasta' ? styles.cardSubasta : ''}`}>
      {/* Header */}
      <div className={styles.cardHeader}>
        <span className={styles.cardNumber}>#{number}</span>
        <span className={`${styles.badge} ${badge.className}`}>{badge.label}</span>
      </div>

      {/* Emoji / avatar */}
      <div className={styles.cardEmoji}>{emoji}</div>

      {/* Info */}
      <div className={styles.cardInfo}>
        <p className={styles.cardName}>{name}</p>
        <p className={styles.cardSubtitle}>{subtitle}</p>
      </div>

      {/* Footer */}
      <div className={styles.cardFooter}>
        {extra ? (
          <span className={styles.cardExtra}>{extra}</span>
        ) : (
          available !== undefined && (
            <span className={styles.cardAvailable}>Disponibles: {available}</span>
          )
        )}
        <button className={styles.actionBtn} onClick={onAction}>
          {actionLabel}
        </button>
      </div>
    </div>
  );
};

export default FiguritaCard;
