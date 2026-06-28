import FiguritaCard from '@/components/ui/figurita-card/figurita-card'
import FiguritaCardSkeleton from '@/components/ui/figurita-card/figurita-card-skeleton'
import Paginacion from '@/components/ui/paginacion/paginacion'
import styles from './explorar-resultados.module.css'

const ExplorarResultados = ({
  figuritas = [],
  totalElements,
  totalPages,
  page,
  onPageChange,
  loading,
  error,
}) => {
  if (error)
    return <p className="text-danger text-center py-4">No se pudieron cargar las figuritas</p>

  return (
    <>
      <p className={styles.resultsCount}>{totalElements} figuritas encontradas</p>

      <div className="row g-4">
        {loading
          ? [...Array(12)].map((_, i) => (
              <div key={i} className="col-12 col-md-6 col-lg-4">
                <FiguritaCardSkeleton />
              </div>
            ))
          : figuritas.map((fig) => (
              <div key={`${fig.figurita_id}-${fig.perfil_id}`} className="col-12 col-md-6 col-lg-4">
                <FiguritaCard
                  figuritaId={fig.figurita_id}
                  numero={fig.numero}
                  metodos={fig.metodos}
                  strCutout={fig.imagen_url}
                  jugador={fig.jugador}
                  seleccion={fig.seleccion}
                  cantidadExistente={fig.cantidad_existente}
                  nombreUsuario={fig.nombre_usuario}
                  reputacion={fig.reputacion}
                  figurita={fig}
                />
              </div>
            ))}
      </div>

      <Paginacion page={page} totalPages={totalPages} onChange={onPageChange} />
    </>
  )
}

export default ExplorarResultados
