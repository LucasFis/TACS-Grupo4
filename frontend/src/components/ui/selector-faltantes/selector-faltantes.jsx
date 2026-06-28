import { useCallback, useEffect, useState } from 'react'
import { buscarFaltantes } from '@/services/coleccionService.js'
import ScrollFiguritas from '@/components/ui/selector-repetidas/scroll-figuritas/scroll-figuritas.jsx'
import { useError } from '@/contexts/errorContext.jsx'
import { useToast } from '@/contexts/toastContext.jsx'
import styles from './selector-faltantes.module.css'

const LIMITE = 10

const SelectorFaltantes = ({ modo = 'multiple', onChange }) => {
  const [figuritas, setFiguritas] = useState([])
  const [total, setTotal] = useState(null)
  const [loading, setLoading] = useState(false)
  const [seleccionadas, setSeleccionadas] = useState([])
  const { handleError } = useError()
  const { showToast } = useToast()

  const fetchFaltantes = useCallback(async (busqueda = '') => {
    setLoading(true)
    try {
      const payload = await buscarFaltantes({ jugador: busqueda, pagina: 1, limite: LIMITE })
      setFiguritas(payload.contenido ?? [])
      setTotal(payload.cantidad_de_elementos ?? null)
    } catch (e) {
      handleError(e, (err) => showToast(err.mensaje, 'error'))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchFaltantes()
  }, [fetchFaltantes])

  const toggle = (fig) => {
    setSeleccionadas((prev) => {
      const existe = prev.some((f) => f.id === fig.id)
      const next =
        modo === 'unica'
          ? existe ? [] : [fig]
          : existe
            ? prev.filter((f) => f.id !== fig.id)
            : [...prev, fig]
      return next  // ✅ solo actualiza el estado
    })
  }

// Notifica al padre después del render
  useEffect(() => {
    onChange?.(seleccionadas)
  }, [seleccionadas, onChange])

  return (
    <div className="d-flex flex-column gap-3">
      <ScrollFiguritas
        figuritas={figuritas}
        loading={loading}
        totalDisponibles={total}
        onBuscar={fetchFaltantes}
        modo={modo}
        seleccionadas={seleccionadas}
        onToggle={toggle}
        bloqueadas={[]}
        titulo="Tus faltantes"
        placeholder="Buscar en tus faltantes..."
      />

      {seleccionadas.length > 0 && (
        <div className={styles.tags}>
          {seleccionadas.map((f) => (
            <span key={f.id} className={styles.tag}>
              {f.jugador}
              <button className={styles.tagQuitar} onClick={() => toggle(f)}>
                ×
              </button>
            </span>
          ))}
        </div>
      )}
    </div>
  )
}

export default SelectorFaltantes
