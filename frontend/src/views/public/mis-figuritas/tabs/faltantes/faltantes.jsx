import FiguritaCard from '../../../../../components/ui/figurita-card/figurita-card';
import styles from './faltantes.module.css';
import {useEffect, useState} from "react";
import {buscarFaltantes} from "../../../../../services/coleccionService.js";

// ── Component ─────────────────────────────────────────────────────────────────
const Faltantes = ({colId}) => {

    const [faltantes, setFaltantes] = useState([]);
    const [filtros, setFiltros] = useState({});

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(false);



    useEffect(() => {
        const cargarFaltantes = async (filtros = {}) => {
            try {
                setLoading(true)
                const faltantesApi = await buscarFaltantes(colId)
                setFaltantes(faltantesApi)
            } catch (error) {
                setError(true);
            } finally {
                setLoading(false)
            }
        }

        cargarFaltantes();
    }, [filtros]);

  if (error) {
    return <p className={styles.error}>{error}</p>;
  }

  return (
    <div className={styles.wrapper}>
      {/* Cards grid */}
      <div className="row g-3">
        {faltantes.map((fig) => (
          <div key={fig.id} className="col-6 col-md-4 col-lg-3">
            <FiguritaCard
              number={fig.id}
              // type={fig.type}
              // emoji={fig.emoji}
              // name={fig.name}
              // subtitle={fig.subtitle}
              // available={fig.available}
              // extra={fig.extra}
              // actionLabel={fig.actionLabel}
              // onAction={() => console.log('acción faltante', fig.id)}
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
      {/*{!loading && hasMore && (*/}
      {/*  <div className={styles.loadMoreWrapper}>*/}
      {/*    <button className={styles.loadMoreBtn} onClick={loadMore}>*/}
      {/*      Ver más*/}
      {/*    </button>*/}
      {/*  </div>*/}
      {/*)}*/}

      {/* Empty state */}
      {/*{!loading && figuritas.length === 0 && (*/}
      {/*  <div className={styles.emptyState}>*/}
      {/*    <span>🎉</span>*/}
      {/*    <p>¡Tenés todas las figuritas!</p>*/}
      {/*  </div>*/}
      {/*)}*/}
    </div>
  );
};

export default Faltantes;
