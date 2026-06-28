import useEstadisticasAdmin from '@/hooks/use-estadisticas-admin.js'
import StatCard from './stat-card.jsx'
import SeccionBarras from './seccion-barras.jsx'
import TopSelecciones from './top-selecciones.jsx'
import RankingUsuarios from './ranking-usuarios.jsx'
import AdministradorSkeleton from './administrador-skeleton.jsx'
import FiltroPeriodo from './filtro-periodo.jsx'
import PropuestasPorEstado from './propuestas-por-estado.jsx'
import styles from './administrador.module.css'

const TARJETAS_GLOBALES = (stats) => [
  { icono: 'bi-people-fill', numero: stats.totalUsuarios, label: 'Usuarios registrados' },
  { icono: 'bi-collection-fill', numero: stats.totalFiguritasPublicadas, label: 'Figuritas publicadas' },
  { icono: 'bi-stopwatch-fill', numero: stats.totalSubastasActivas, label: 'Subastas activas' },
]

const MODALIDADES = [
  { label: 'Solo intercambio', key: 'soloIntercambio', color: '#175a2d' },
  { label: 'Solo subasta', key: 'soloSubasta', color: '#d49a2c' },
  { label: 'Ambos', key: 'ambos', color: '#7c3aed' },
]

const Administrador = () => {
  const {
    stats, cargando, recargando, error,
    desde, setDesde, hasta, setHasta,
    rangoInvalido, aplicar,
  } = useEstadisticasAdmin()

  return (
    <div className={styles.page}>
      <div className={styles.hero}>
        <div className={styles.heroInner}>
          <div className="d-flex align-items-center gap-2 mb-1">
            <h1 className={styles.heroTitulo}>Panel de administración</h1>
            <span className={styles.adminBadge}>Admin</span>
          </div>
          <p className={styles.heroSubtitulo}>Estadísticas de la plataforma Figus Mundial 2026</p>

          <FiltroPeriodo
            desde={desde}
            setDesde={setDesde}
            hasta={hasta}
            setHasta={setHasta}
            rangoInvalido={rangoInvalido}
            aplicar={aplicar}
            recargando={recargando}
          />
        </div>
      </div>

      <div className={styles.contenido}>
        {cargando && <AdministradorSkeleton />}
        {error && <div className="alert alert-danger">{error}</div>}

        {!cargando && !error && stats && (
          <>
            {/* Estadísticas globales */}
            <p className={styles.seccionHeading}>Estadísticas atemporales</p>

            <div className="row g-3 mb-3">
              {TARJETAS_GLOBALES(stats).map((t) => (
                <div className="col-4" key={t.label}>
                  <StatCard {...t} destacado />
                </div>
              ))}
            </div>

            <div className="row g-3 mb-4">
              <div className="col-12 col-md-6">
                <SeccionBarras
                  titulo="Figuritas por modalidad"
                  icono="bi-pie-chart-fill"
                  config={MODALIDADES}
                  data={stats.figuritasPorModalidad ?? {}}
                />
              </div>
              <div className="col-12 col-md-6">
                <div className={styles.colApilada}>
                  <RankingUsuarios
                    titulo="Mejor reputación"
                    icono="bi-star-fill"
                    iconoColor="#d49a2c"
                    items={stats.rankings?.mejorReputacion}
                    sufijo=" ⭐"
                    decimales={1}
                  />
                  <RankingUsuarios
                    titulo="Más figuritas publicadas"
                    icono="bi-trophy-fill"
                    iconoColor="#d49a2c"
                    items={stats.rankings?.topColeccionistas}
                  />
                </div>
              </div>
            </div>

            {/* Estadísticas del período */}
            <div className={styles.delPeriodoRow}>
              <p className={styles.delPeriodoLabel}>Estadísticas del período</p>
              <span className={styles.delPeriodoNota}>según fecha de creación</span>
            </div>

            <div className="row g-3 mb-3">
              <div className="col-12 col-md-6">
                <PropuestasPorEstado
                  propuestasPorEstado={stats.propuestasPorEstado}
                  totalPropuestas={stats.totalPropuestas}
                />
              </div>

              <div className="col-12 col-md-6">
                <div className={styles.colApilada}>
                  <RankingUsuarios
                    titulo="Top intercambiadores"
                    icono="bi-arrow-left-right"
                    items={stats.rankings?.topIntercambiadores}
                  />
                  <RankingUsuarios
                    titulo="Top creadores de propuestas"
                    icono="bi-person-plus-fill"
                    items={stats.rankings?.topCreadoresDePropuestas}
                  />
                </div>
              </div>
            </div>

            <div className="row g-3 mb-4">
              <div className="col-12 col-md-6">
                <RankingUsuarios
                  titulo="Top subastadores"
                  icono="bi-hammer"
                  items={stats.rankings?.topSubastadores}
                />
              </div>
              <div className="col-12 col-md-6">
                <RankingUsuarios
                  titulo="Mejor tasa de aceptación"
                  icono="bi-check-circle-fill"
                  items={stats.rankings?.mejorTasaAceptacion}
                  sufijo="%"
                  decimales={0}
                />
              </div>
            </div>

            {stats.topSelecciones?.length > 0 && (
              <div className="row g-3">
                <div className="col-12">
                  <TopSelecciones data={stats.topSelecciones} />
                </div>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  )
}

export default Administrador
