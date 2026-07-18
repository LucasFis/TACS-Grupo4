import Estrellas from '@/components/ui/estrellas/estrellas.jsx'
import { Spinner } from '@/components/ui/spinner/spinner.jsx'
import { useAuth } from '@/contexts/userContext.jsx'
import styles from './PerfilHeader.module.css'

const PerfilHeader = ({
  perfil,
  loading,
  promedio,
  reviews,
  perfilId,
  esMiPerfil,
  onEditarClick,
  onCerrarSesionClick,
}) => {
  const { user } = useAuth()
  const esAdmin = user?.rol === 'ADMINISTRADOR'
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
              <Spinner />
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

        {esMiPerfil && (
          <div className="d-flex align-items-center gap-3">
            <button
              className={`btn btn-warning ${styles['btn-editar']}`}
              onClick={onEditarClick}
            >
              Editar perfil
            </button>

            <button
              className={`btn btn-outline-light ${styles['btn-editar']}`}
              onClick={onCerrarSesionClick}
            >
              Cerrar sesion
            </button>
          </div>
        )}
      </div>
    </div>
  )
}

export default PerfilHeader
