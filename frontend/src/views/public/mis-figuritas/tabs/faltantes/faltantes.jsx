import useFiguritasPaginadas from '../../../hooks/useFiguritasPaginadas';
import FiguritaCard from '../../../components/ui/figurita-card/FiguritaCard';
import styles from './Faltantes.module.css';

// ── API ───────────────────────────────────────────────────────────────────────
// Reemplazá esta función con tu llamada real al backend.
const fetchFaltantes = async (page) => {
  const response = await fetch(`/api/figuritas/faltantes?page=${page}&limit=8`);
  if (!response.ok) throw new Error('Error al cargar faltantes');
  return response.json(); // { data: [...], hasMore: true/false }
};

// ── Component ─────────────────────────────────────────────────────────────────
const Faltantes = () => {
  const { figuritas, loading, error, hasMore, loadMore } = useFiguritasPaginadas(fetchFaltantes);

  if (error) {
    return <p className={styles.error}>{error}</p>;
  }

  return (
    <div className={styles.wrapper}>
      {/* Cards grid */}
      <div className="row g-3">
        {figuritas.map((fig) => (
          <div key={fig.id} className="col-6 col-md-4 col-lg-3">
            <FiguritaCard
              number={fig.number}
              type={fig.type}
              emoji={fig.emoji}
              name={fig.name}
              subtitle={fig.subtitle}
              available={fig.available}
              extra={fig.extra}
              actionLabel={fig.actionLabel}
              onAction={() => console.log('acción faltante', fig.id)}
            />
          </div>
        ))}
      </div>

      {/* Loading skeleton */}
      {loading && (
        <div className={`row g-3 ${styles.skeletonRow}`}>
          {[...Array(4)].map((_, i) => (
            <div key={i} className="col-6 col-md-4 col-lg-3">
              <div className={styles.skeleton} />
            </div>
          ))}
        </div>
      )}

      {/* Load more */}
      {!loading && hasMore && (
        <div className={styles.loadMoreWrapper}>
          <button className={styles.loadMoreBtn} onClick={loadMore}>
            Ver más
          </button>
        </div>
      )}

      {/* Empty state */}
      {!loading && figuritas.length === 0 && (
        <div className={styles.emptyState}>
          <span>🎉</span>
          <p>¡Tenés todas las figuritas!</p>
        </div>
      )}
    </div>
  );
};

export default Faltantes;
