import { useState, useEffect, useRef } from 'react'
import styles from './scroll-figuritas.module.css'
import FilaScroll from './fila-scroll/fila-scroll'
import CabeceraSeleccion from './cabecera-seleccion/cabecera-seleccion'

const Skeleton = () => (
  <div>
    {[...Array(4)].map((_, i) => (
      <div key={i} className={styles['scroll-skeleton-fila'] + ' placeholder-glow'}>
        <div className="placeholder rounded w-75" />
      </div>
    ))}
  </div>
)

const SinResultados = ({ mensaje }) => (
  <div className={styles['scroll-sin-resultados']}>
    {mensaje || 'No hay resultados'}
  </div>
)

const ScrollFiguritas = ({
  figuritas = [],
  seleccionadasIniciales = [],
  loading = false,
  totalDisponibles = null,
  onBuscar,
  debounceMs = 400,
  modo = 'unica',
  seleccionadas = [],
  onToggle,
  bloqueadas = [],
  titulo = '',
  placeholder = '',
  mensajeVacio = null,
}) => {
  const [busqueda, setBusqueda] = useState('')
  const debounceRef = useRef(null)

  useEffect(() => {
    if (!onBuscar) return
    clearTimeout(debounceRef.current)
    debounceRef.current = setTimeout(() => onBuscar(busqueda.trim()), debounceMs)
    return () => clearTimeout(debounceRef.current)
  }, [busqueda, onBuscar, debounceMs])

  const filtradas = onBuscar
    ? figuritas
    : figuritas.filter(
        (f) =>
          f.jugador.toLowerCase().includes(busqueda.toLowerCase()) ||
          f.numero?.toString().includes(busqueda),
      )
  const getId = (fig) => fig.figurita_id ?? fig.id
  const esBloqueada = (fig) => bloqueadas.some((b) => getId(b) === getId(fig))
  const estaSeleccionada = (fig) =>
    esBloqueada(fig) || seleccionadas.some((f) => getId(f) === getId(fig))
  const mostrandoParcial = totalDisponibles !== null && totalDisponibles > filtradas.length

  return (
    <div className="d-flex flex-column gap-3">
      <div className={styles['scroll-figuritas-buscador']}>
        <span className={styles['scroll-figuritas-buscador-icono']}>🔍</span>
        <input
          type="text"
          className="form-control ps-5"
          placeholder={placeholder}
          value={busqueda}
          onChange={(e) => setBusqueda(e.target.value)}
        />
      </div>

      <div className={styles['scroll-figuritas-lista']}>
        <div className={styles['scroll-figuritas-header']}>
          <span className={styles['scroll-figuritas-header-titulo']}>
            {titulo}
            {mostrandoParcial && (
              <span className={styles['scroll-figuritas-header-parcial']}>
                · {filtradas.length} de {totalDisponibles}
              </span>
            )}
          </span>
          <CabeceraSeleccion modo={modo} seleccionadas={seleccionadas} bloqueadas={bloqueadas} />
        </div>

        <div className={styles['scroll-figuritas-scroll']}>
          {loading ? (
            <Skeleton />
          ) : filtradas.length === 0 ? (
            <SinResultados mensaje={mensajeVacio} />
          ) : (
            filtradas.map((fig) => (
              <FilaScroll
                key={getId(fig)}
                fig={fig}
                modo={modo}
                bloqueada={esBloqueada(fig)}
                seleccionada={estaSeleccionada(fig)}
                onToggle={onToggle}
                eraInicial={seleccionadasIniciales.some((f) => getId(f) === getId(fig))}
              />
            ))
          )}
        </div>
      </div>
    </div>
  )
}

export default ScrollFiguritas
