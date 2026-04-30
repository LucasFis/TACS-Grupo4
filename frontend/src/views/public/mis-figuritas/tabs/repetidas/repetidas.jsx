import {useEffect, useState} from 'react';
import FiguritaCard from '../../../../../components/ui/figurita-card/figurita-card';
import styles from './Repetidas.module.css';
import {buscarRepetidas} from "../../../../../services/coleccionService.js";

const Repetidas = ({colId}) => {

    const [repetidas, setRepetidas] = useState([]);
    const [filtros, setFiltros] = useState({});

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(false);

    useEffect(() => {
        const cargarRepetidas = async (filtros = {}) => {
            try {
                setLoading(true)
                const repetidasApi = await buscarRepetidas(colId, filtros)
                setRepetidas(repetidasApi)
            } catch (error) {
                setError(true);
            } finally {
                setLoading(false)
            }
        }

        cargarRepetidas();
    }, [filtros])

  if (error) {
    return <p className={styles.error}>{error}</p>;
  }

  return (
    <div className={styles.wrapper}>
      {/* Cards grid */}
      <div className={`row g-3`}>
        {repetidas.map((fig) => (
          <div key={fig.id} className="col-6 col-md-4 col-lg-3">
            <FiguritaCard
              number={fig.figurita.id}
              // type={fig.type}
              // emoji={fig.emoji}
              // name={fig.name}
              // subtitle={fig.subtitle}
              // available={fig.available}
              // extra={fig.extra}
              // actionLabel={fig.actionLabel}
              // onAction={() => console.log('acción repetida', fig.id)}
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
    </div>
  );
};

export default Repetidas;
