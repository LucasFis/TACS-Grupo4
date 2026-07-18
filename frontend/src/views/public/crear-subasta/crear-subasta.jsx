import { useState } from 'react'
import { useNavigate } from 'react-router'
import { crearSubasta } from '@/services/subastasService.js'
import SelectorRepetidas from '@/components/ui/selector-repetidas/selector-repetidas.jsx'
import SelectorFaltantes from '@/components/ui/selector-faltantes/selector-faltantes.jsx'
import Button from '@/components/ui/button/button.jsx'
import { useError } from '@/contexts/errorContext.jsx'
import { useToast } from '@/contexts/toastContext.jsx'
import ModalInformativo from '@/components/ui/modales/modal-informativo/modal-informativo.jsx'
import styles from './crear-subasta.module.css'

// ─── Helpers ──────────────────────────────────────────────────────────────────

const calcularCierreEstimado = (horas) => {
  if (!horas) return null
  return new Date(Date.now() + horas * 3600000).toLocaleString('es-AR', {
    day: '2-digit',
    month: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const CALIFICACION_MIN_OPTS = [
  { label: 'Sin mínimo', value: 0 },
  { label: '1+', value: 1 },
  { label: '2+', value: 2 },
  { label: '3+', value: 3 },
  { label: '4+', value: 4 },
  { label: '5', value: 5 },
]

// ─── Paso 1: Figurita a subastar ──────────────────────────────────────────────

const PasoFigurita = ({ onChange }) => (
  <SelectorRepetidas modo="unica" bloqueadas={[]} onChange={onChange} metodoIntercambio="SUBASTA" mostrarAviso={true}/>
)

// ─── Paso 2: Duración ─────────────────────────────────────────────────────────

const PasoDuracion = ({ duracion, onCambiar }) => (
  <div className="d-flex flex-column gap-3">
    <div className="d-flex align-items-center gap-2">
      <input
        type="number"
        min="1"
        max="168"
        className="form-control"
        style={{ maxWidth: '120px' }}
        placeholder="Ej: 6"
        value={duracion ?? ''}
        onChange={(e) => {
          const num = parseInt(e.target.value, 10)
          onCambiar(isNaN(num) || num <= 0 ? null : num)
        }}
      />
      <span className="text-muted" style={{ fontSize: '0.85rem' }}>
        horas
      </span>
    </div>
    {duracion && (
      <p className={styles['duracion-hint']}>
        La subasta durará{' '}
        <strong>
          {duracion} {duracion === 1 ? 'hora' : 'horas'}
        </strong>
        .
      </p>
    )}
  </div>
)

// ─── Paso 3: Condiciones ──────────────────────────────────────────────────────

const PasoCondiciones = ({
  figuritasDeseadas,
  onCambiarFiguritas,
  calificacionMin,
  onCambiarCalificacion,
}) => (
  <div className="d-flex flex-column gap-4">
    <div className="d-flex flex-column gap-2">
      <div>
        <p className={styles['condiciones-titulo']}>Figuritas que aceptás como oferta</p>
        <p className={styles['condiciones-hint']}>
          El ofertante debe incluir al menos una de estas figuritas
        </p>
      </div>
      <SelectorFaltantes modo="multiple" onChange={onCambiarFiguritas} />
      {figuritasDeseadas.length === 0 && (
        <p className={styles['condiciones-sin-restriccion']}>
          Sin restricción — aceptás cualquier figurita
        </p>
      )}
    </div>

    <div className="d-flex flex-column gap-2">
      <div>
        <p className={styles['condiciones-titulo']}>Calificación mínima del ofertante</p>
        <p className={styles['condiciones-hint']}>
          Solo usuarios con esta calificación o más podrán ofertar
        </p>
      </div>
      <div className={styles['calificacion-opciones']}>
        {CALIFICACION_MIN_OPTS.map(({ label, value }) => {
          const activo = calificacionMin === value
          return (
            <button
              key={value}
              className={`btn btn-sm rounded-pill px-3 ${activo ? 'btn-success' : 'btn-outline-secondary'}`}
              onClick={() => onCambiarCalificacion(value)}
            >
              {label} ★
            </button>
          )
        })}
      </div>
      <p className={styles['calificacion-hint']}>
        {calificacionMin === 0
          ? 'Cualquier usuario puede ofertar en tu subasta'
          : `Solo usuarios con ${calificacionMin}★ o más pueden ofertar`}
      </p>
    </div>
  </div>
)

// ─── Resumen ──────────────────────────────────────────────────────────────────

const Resumen = ({ figurita, duracion, figuritasDeseadas, calificacionMin }) => {
  const filas = [
    {
      label: 'Figurita',
      valor: figurita?.jugador ?? null,
    },
    {
      label: 'Duración',
      valor: duracion ? `${duracion} ${duracion === 1 ? 'hora' : 'horas'}` : null,
    },
    {
      label: 'Cierre estimado',
      valor: calcularCierreEstimado(duracion),
    },
    {
      label: 'Figuritas deseadas',
      valor:
        figuritasDeseadas.length > 0
          ? figuritasDeseadas.map((f) => f.jugador).join(', ')
          : 'Sin restricción',
    },
    {
      label: 'Calificación mínima',
      valor: calificacionMin === 0 ? 'Sin mínimo' : `${calificacionMin}★ o más`,
    },
  ]

  return (
    <div className={styles.resumen}>
      <div className={styles['resumen-header']}>
        <span className={styles['resumen-header-titulo']}>Resumen de la subasta</span>
      </div>
      <div className={styles['resumen-body']}>
        {filas.map(({ label, valor }) => (
          <div key={label} className={styles['resumen-fila']}>
            <span className={styles['resumen-label']}>{label}</span>
            <span className={valor ? styles['resumen-valor'] : styles['resumen-vacio']}>
              {valor ?? '—'}
            </span>
          </div>
        ))}
      </div>
    </div>
  )
}

// ─── CrearSubasta ─────────────────────────────────────────────────────────────

const PASOS = [
  { numero: 1, label: 'Elegí la figurita a subastar' },
  { numero: 2, label: 'Duración' },
  { numero: 3, label: 'Condiciones para ofertar' },
]

const CrearSubasta = () => {
  const navigate = useNavigate()

  const [figurita, setFigurita] = useState(null)
  const [duracion, setDuracion] = useState(6)
  const [figuritasDeseadas, setFiguritasDeseadas] = useState([])
  const [calificacionMin, setCalificacionMin] = useState(1)
  const [loading, setLoading] = useState(false)
  const { handleError } = useError()
  const { showToast } = useToast()

  const handleFigurita = (seleccionadas) => setFigurita(seleccionadas[0] ?? null)

  const puedePublicar = figurita !== null && duracion !== null

  const handlePublicar = async () => {
    if (!puedePublicar) return
    try {
      setLoading(true)
      await crearSubasta({
        figurita_id: figurita.figurita_id,
        duracion_en_horas: duracion,
        figuritas_deseadas_ids: figuritasDeseadas.map((f) => f.id),
        calificacion_minima: calificacionMin,
      })
      showToast('Subasta creada correctamente', 'success')
      navigate('/subastas')
    } catch (e) {
      handleError(e, (err) => showToast(err.mensaje, 'error'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className={styles.page}>
      <div>
        <h1 className={styles['header-titulo']}>Crear subasta</h1>
        <p className={styles['header-subtitulo']}>Completá los pasos para publicar tu figurita</p>
      </div>

      {PASOS.map(({ numero, label }, i) => (
        <div key={numero}>
          <div className={styles.paso}>
            <div className={styles['paso-header']}>
              <div className={styles['paso-numero']}>{numero}</div>
              <p className={styles['paso-label']}>{label}</p>
            </div>
            <div className={styles['paso-contenido']}>
              {numero === 1 && <PasoFigurita onChange={handleFigurita} />}
              {numero === 2 && <PasoDuracion duracion={duracion} onCambiar={setDuracion} />}
              {numero === 3 && (
                <PasoCondiciones
                  figuritasDeseadas={figuritasDeseadas}
                  onCambiarFiguritas={setFiguritasDeseadas}
                  calificacionMin={calificacionMin}
                  onCambiarCalificacion={setCalificacionMin}
                />
              )}
            </div>
          </div>
          {i < PASOS.length - 1 && <hr className="my-1" />}
        </div>
      ))}

      <Resumen
        figurita={figurita}
        duracion={duracion}
        figuritasDeseadas={figuritasDeseadas}
        calificacionMin={calificacionMin}
      />

      <div className={styles.acciones}>
        <Button label="Cancelar" variante="secondary" onClick={() => navigate(-1)} />
        <Button
          label={loading ? 'Publicando...' : 'Publicar subasta ↗'}
          disabled={!puedePublicar || loading}
          onClick={handlePublicar}
        />
      </div>

      <ModalInformativo open={loading}>
        <h3>Publicando subasta...</h3>
        <p>Esto puede tardar unos segundos</p>
      </ModalInformativo>
    </main>
  )
}

export default CrearSubasta
