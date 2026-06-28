import { useState } from 'react'
import SectionCard from '../../../components/ui/section-card/section-card.jsx'
import AutocompleteInput from '../../../components/ui/autocomplete-input/autocomplete-input.jsx'
import Button from '../../../components/ui/button/button.jsx'
import { buscarFiguritas } from '@/services/figuritaService.js'
import { agregarRepetida } from '@/services/coleccionService.js'
import { useToast } from '@/contexts/toastContext.jsx'
import { useError } from '@/contexts/errorContext.jsx'
import Breadcrumb from '@/components/ui/breadcrumb/breadcrumb.jsx'
import { IconoAdvertencia } from '@/components/ui/iconos/advertencia/advertencia.jsx'

const NuevaRepetida = () => {
  const [figurita, setFigurita] = useState(undefined)
  const [numero, setNumero] = useState('')
  const [jugador, setJugador] = useState('')
  const [cantidad, setCantidad] = useState(1)
  const [modosIntercambio, setModosIntercambio] = useState(['SUBASTA', 'INTERCAMBIO'])
  const [errorFormato, setErrorFormato] = useState('')
  const [tocado, setTocado] = useState(false)
  const { showToast } = useToast()
  const { handleError } = useError()

  const handleSelect = (fig) => {
    setFigurita(fig)
    setJugador(fig?.jugador ?? '')
    setNumero(fig?.id ?? '')
  }

  const buscarPorJugador = async (texto) => {
    return await buscarFiguritas({ jugador: texto })
  }

  const buscarPorNumero = async (texto) => {
    const formatoValido = /^[A-Z]{3}-\d+$/.test(texto)

    if (!formatoValido) {
      setErrorFormato('Formato inválido. Debe ser 3 letras mayúsculas, guión y número (Ej: ARG-10)')
      setTocado(true)
      return
    }

    setErrorFormato('')
    const resultado = await buscarFiguritas({ id: texto })

    if (resultado.length === 0) {
      showToast('No se encontró una figurita con ese número', 'error')
      handleSelect(undefined)
      return
    }

    handleSelect(resultado[0])
  }

  const toggleMetodo = (metodo) => {
    setModosIntercambio((prev) =>
      prev.includes(metodo) ? prev.filter((m) => m !== metodo) : [...prev, metodo],
    )
  }

  const ejecutarFormulario = async () => {
    if (!figurita) {
      showToast('Debés buscar y seleccionar una figurita primero', 'error')
      return
    }

    if (modosIntercambio.length === 0) {
      showToast('Debés seleccionar al menos un método de intercambio', 'error')
      return
    }

    if (Number(cantidad) < 1) {
      showToast('La cantidad debe ser al menos 1', 'error')
      return
    }

    try {
      await agregarRepetida({
        id: figurita.id,
        cantidad: cantidad,
        modosIntercambio: modosIntercambio,
      })
      setNumero('')
      setJugador('')
      setModosIntercambio([])
      setFigurita(undefined)

      showToast('Repetida agregada correctamente', 'success')
    } catch (error) {
      showToast(
        handleError(error, (m) => {}),
        'error',
      )
    }
  }

  return (
    <div className="d-flex flex-column gap-3">
      <Breadcrumb
        crumbs={[
          { name: 'Mis figuritas', to: '/mis-figuritas' },
          { name: 'Nueva repetida', to: '/mis-figuritas/nueva-repetida' },
        ]}
      />
      <div
        className="d-flex flex-column align-items-center justify-content-center rounded-3 py-4"
        style={{ backgroundColor: '#f8f9fa', border: '1.5px dashed #ced4da', minHeight: '140px' }}
      >
        {figurita ? (
          <>
            <p className="mb-0 fw-semibold" style={{ fontSize: '1rem' }}>
              {figurita.jugador}
            </p>
            <p className="mb-0 text-muted" style={{ fontSize: '0.8rem' }}>
              {figurita.seleccion} · #{figurita.numero}
            </p>
          </>
        ) : (
          <>
            <span style={{ fontSize: '2rem' }}>🔍</span>
            <p className="mb-0 text-muted mt-2" style={{ fontSize: '0.85rem' }}>
              Buscá una figurita para ver la vista previa
            </p>
          </>
        )}
      </div>

      <SectionCard>
        <SectionCard.Section>
          <div className="d-flex flex-column w-100 gap-3">
            <p
              className="mb-3 fw-semibold text-uppercase"
              style={{ fontSize: '0.75rem', letterSpacing: '0.05em' }}
            >
              Buscar figurita
            </p>

            <div className="mb-3">
              <label className={'form-label text-muted'}>Numero de figurita</label>
              <div className={'d-flex align-items-center gap-3'}>
                <input
                  type="text"
                  className={`form-control ${errorFormato ? 'border-danger' : ''}`}
                  placeholder="Ej: ARG-10"
                  value={numero}
                  onChange={(e) => {
                    setNumero(e.target.value.toUpperCase())
                    setFigurita(null)
                    setJugador('')
                    setErrorFormato('')
                    setTocado(false)
                  }}
                  onBlur={() => {
                    setTocado(true)
                    if (numero && !/^[A-Z]{3}-\d+$/.test(numero)) {
                      setErrorFormato('Formato inválido. Debe ser 3 letras mayúsculas, guión y número (Ej: ARG-10)')
                    }
                  }}
                />
                <Button label={'Buscar'} onClick={() => buscarPorNumero(numero)} />
              </div>
              {tocado && errorFormato && (
                <small className="text-danger d-flex align-items-center gap-1 mt-1" style={{ fontSize: '0.85rem' }}>
                  <IconoAdvertencia /> {errorFormato}
                </small>
              )}
            </div>

            <AutocompleteInput
              label="Nombre del jugador"
              placeholder="Ej: Lionel Messi"
              value={jugador}
              onChange={(val) => {
                setJugador(val)
                setFigurita(null)
              }}
              onSelect={handleSelect}
              onSearch={buscarPorJugador}
              debounceMs={300}
              getLabel={(fig) => fig.jugador}
            />

            <div className="mb-3 ">
              <label className={'form-label text-muted'}>Cantidad Disponible</label>
              <div className={'d-flex align-items-center gap-3'}>
                <input
                  type="number"
                  className="form-control"
                  placeholder="Cantidad disponible"
                  value={cantidad}
                  min={1}
                  onChange={(e) => setCantidad(e.target.value)}
                  onBlur={() => {
                    if (!cantidad || Number(cantidad) < 1) setCantidad(1)
                  }}
                />
              </div>
            </div>

            <div>
              <label className="form-label text-muted">Métodos de intercambio</label>

              <div className="d-flex flex-column gap-2">
                <div className="form-check">
                  <input
                    className="form-check-input"
                    type="checkbox"
                    id="subasta"
                    checked={modosIntercambio.includes('SUBASTA')}
                    onChange={() => toggleMetodo('SUBASTA')}
                  />
                  <label className="form-check-label" htmlFor="subasta">
                    Subasta
                  </label>
                </div>

                <div className="form-check">
                  <input
                    className="form-check-input"
                    type="checkbox"
                    id="intercambio"
                    checked={modosIntercambio.includes('INTERCAMBIO')}
                    onChange={() => toggleMetodo('INTERCAMBIO')}
                  />
                  <label className="form-check-label" htmlFor="intercambio">
                    Intercambio
                  </label>
                </div>
              </div>
            </div>
          </div>
        </SectionCard.Section>
      </SectionCard>

      <Button
        label="Publicar Repetida ↗"
        onClick={() => ejecutarFormulario()}
      />
    </div>
  )
}

export default NuevaRepetida
