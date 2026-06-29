import Estrellas from '@/components/ui/estrellas/estrellas.jsx'
import styles from './PerfilHeader.module.css'

const PerfilHeader = ({
  perfil,
  loading,
  promedio,
  reviews,
  perfilId,
  esAdmin,
  onEditarClick,
  onCerrarSesionClick,
}) => {
  return (
    <div className={styles.wrapper}>
      <div
        className={`mx-auto d-flex justify-content-between align-items-center px-4 py-4 ${styles.inner}`}
      >
        <div className="d-flex align-items-center gap-3 text-white">
          <div className={styles.avatar}>
            {!loading && perfil?.nombre
              ? perfil.nombre
                  .trim()
                  .split(/\s+/)
                  .map((p) => p[0].toUpperCase())
                  .join('')
                  .slice(0, 2)
              : '?'}
          </div>

          <div>
            {loading ? (
              <h2>Cargando datos...</h2>
            ) : (
              <>
                <h2 className={`mb-0 fw-bold ${styles.nombre}`}>{perfil?.nombre}</h2>
                {!esAdmin && (
                  <div className="d-flex align-items-center gap-1">
                    <Estrellas calificacion={promedio} mostrarNumero={true} varianteNumero="blanco"/>
                    <span>({reviews.cantidad_de_elementos})</span>
                  </div>
                )}
              </>
            )}
          </div>
        </div>

        <div className="d-flex align-items-center gap-3">
          <button className={`btn btn-warning ${styles['btn-editar']}`} onClick={onEditarClick}>
            Editar perfil
          </button>

          {perfilId != null && (
            <button
              className={`btn btn-outline-light ${styles['btn-editar']}`}
              onClick={onCerrarSesionClick}
            >
              Cerrar sesion
            </button>
          )}
        </div>
      </div>
    </div>
  )
}

export default PerfilHeader
