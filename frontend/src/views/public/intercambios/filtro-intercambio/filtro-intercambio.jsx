import FilterChip from '../../../../components/ui/filter-chip/filter-chip.jsx'
import AutocompleteInput from '../../../../components/ui/autocomplete-input/autocomplete-input.jsx'
import FiltrosActivos from '../../explorar/filtros/FiltrosActivos.jsx'
import Button from '../../../../components/ui/button/button.jsx'
import { buscarFiguritas } from '@/services/figuritaService.js'
import useFiltroIntercambio from './useFiltroIntercambio'
import styles from './filtro-intercambio.module.css'

const TEXTOS_ESTADO = {
  '': 'Todas',
  PENDIENTE: 'Pendientes',
  ACEPTADO: 'Aceptadas',
  RECHAZADO: 'Rechazadas',
  CANCELADO: 'Canceladas',
}

const FiltroIntercambio = ({ estado, onChangeEstado, onAplicarFigurita }) => {
  const {
    tipoFigurita,
    jugadorQuery,
    tieneFiltroFigurita,
    setJugadorQuery,
    toggleTipo,
    seleccionarFigurita,
    aplicar,
    resetFigurita,
    chipsActivos,
  } = useFiltroIntercambio()

  const handleAplicar = () => {
    const params = aplicar()
    onAplicarFigurita(params)
  }

  const handleQuitar = (campo, valor) => {
    if (campo === 'tipoFigurita') {
      resetFigurita()
      onAplicarFigurita({})
    }
  }

  return (
    <div className={styles.filtrosCard}>
      <div className={styles.filtrosRow}>
        <span className={styles.filtrosLabel}>Filtros:</span>
        {Object.entries(TEXTOS_ESTADO).map(([key, label]) => (
          <FilterChip
            key={key}
            label={label}
            selected={estado === key}
            onClick={() => onChangeEstado(key)}
          />
        ))}
      </div>

      <hr className={styles.divider} />

      <div className={styles.seccionBusqueda}>
        <FilterChip
          label="Buscada"
          selected={tipoFigurita === 'BUSCADA'}
          onClick={() => toggleTipo('BUSCADA')}
        />
        <FilterChip
          label="Propuesta"
          selected={tipoFigurita === 'PROPUESTA'}
          onClick={() => toggleTipo('PROPUESTA')}
        />
        <div style={{ flex: 1, minWidth: '220px' }}>
          <AutocompleteInput
            value={jugadorQuery}
            onChange={setJugadorQuery}
            onSearch={async (texto) => await buscarFiguritas({ jugador: texto })}
            onSelect={seleccionarFigurita}
            getLabel={(fig) => `${fig.jugador} - ${fig.seleccion} #${fig.numero}`}
            placeholder="Buscar por nombre del jugador"
            disabled={!tipoFigurita}
          />
        </div>
        <Button
          label="Aplicar"
          onClick={handleAplicar}
          disabled={!tipoFigurita || !tieneFiltroFigurita}
          variante="exito"
        />
      </div>

      <FiltrosActivos chips={chipsActivos()} onQuitar={handleQuitar} />
    </div>
  )
}

export default FiltroIntercambio
