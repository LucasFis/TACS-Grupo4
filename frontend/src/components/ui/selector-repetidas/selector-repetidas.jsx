import { useState, useEffect, useCallback } from 'react'
import { buscarRepetidas } from '@/services/coleccionService.js'
import ScrollFiguritas from './scroll-figuritas/scroll-figuritas.jsx'
import { useError } from '@/contexts/errorContext.jsx'
import { useToast } from '@/contexts/toastContext.jsx'
import styles from './selector-repetidas.module.css'

const LIMITE = 10

const SelectorRepetidas = ({
  modo = 'unica',
  bloqueadas = [],
  onChange,
  seleccionadasIniciales = [],
}) => {
  const [figuritas, setFiguritas] = useState([])
  const [loading, setLoading] = useState(false)
  const [seleccionadas, setSeleccionadas] = useState(seleccionadasIniciales)
  const [total, setTotal] = useState(null)
  const { handleError } = useError()
  const { showToast } = useToast()

  const fetchRepetidas = useCallback(async (busqueda = '') => {
    setLoading(true)
    try {
      const payload = await buscarRepetidas({ jugador: busqueda, pagina: 1, limite: LIMITE })
      setFiguritas(payload.contenido ?? [])
      setTotal(payload.cantidad_de_elementos ?? null)
    } catch (e) {
      handleError(e, (err) => showToast(err.mensaje, 'error'))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchRepetidas()
  }, [fetchRepetidas])

  const toggle = (fig) => {
    const esBloqueada = bloqueadas.some((b) => b.figurita_id === fig.figurita_id)
    if (esBloqueada) return

    setSeleccionadas((prev) => {
      const existe = prev.some((f) => f.figurita_id === fig.figurita_id)
      const next =
        modo === 'unica'
          ? existe
            ? []
            : [fig]
          : existe
            ? prev.filter((f) => f.figurita_id !== fig.figurita_id)
            : [...prev, fig]
      return next
    })
  }

  useEffect(() => {
    onChange?.(seleccionadas)
  }, [seleccionadas, onChange])

  const todasVisibles = [...bloqueadas, ...seleccionadas]

  return (
    <div className="d-flex flex-column gap-3">
      <ScrollFiguritas
        figuritas={figuritas}
        loading={loading}
        totalDisponibles={total}
        onBuscar={fetchRepetidas}
        modo={modo}
        seleccionadas={seleccionadas}
        onToggle={toggle}
        bloqueadas={bloqueadas}
        titulo="Tus repetidas"
        placeholder="Buscar en tus repetidas..."
      />

      {todasVisibles.length > 0 && (
        <div className={styles['selector-tags']}>
          {todasVisibles.map((f) => {
            const esBloqueada = bloqueadas.some((b) => b.figurita_id === f.figurita_id)
            return (
              <span
                key={f.figurita_id}
                className={`${styles['selector-tag']} ${esBloqueada ? styles['bloqueada'] : styles['opcional']}`}
              >
                {f.jugador}
                {!esBloqueada && (
                  <button className={styles['selector-tag-quitar']} onClick={() => toggle(f)}>
                    ×
                  </button>
                )}
              </span>
            )
          })}
        </div>
      )}
    </div>
  )
}

export default SelectorRepetidas
