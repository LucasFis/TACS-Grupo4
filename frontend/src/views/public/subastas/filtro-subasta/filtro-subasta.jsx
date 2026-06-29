import FilterChip from '../../../../components/ui/filter-chip/filter-chip.jsx'
import AutocompleteInput from '../../../../components/ui/autocomplete-input/autocomplete-input.jsx'
import FiltrosActivos from '../../explorar/filtros/FiltrosActivos.jsx'
import Button from '../../../../components/ui/button/button.jsx'
import { buscarFiguritas } from '@/services/figuritaService.js'
import useFiltroSubasta from './useFiltroSubasta'
import styles from './filtro-subasta.module.css'

const FiltroSubasta = ({ estado, onChangeEstado, onAplicarFigurita }) => {
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
  } = useFiltroSubasta()

  const handleAplicar = () => {
    const params = aplicar()
    onAplicarFigurita(params)
  }

  const handleQuitar = (campo, valor) => {
    resetFigurita()
    onAplicarFigurita({})
  }

  const handleSelect = (fig) => {
    seleccionarFigurita(fig)
  }

  return (
    <div className={styles.filtrosCard}>
      <div className={styles.filtrosRow}>
        <span className={styles.filtrosLabel}>Filtros:</span>
        <FilterChip
          label="Activas"
          selected={estado === 'ACTIVA'}
          onClick={() => onChangeEstado('ACTIVA')}
        />
        <FilterChip
          label="Finalizadas"
          selected={estado === 'FINALIZADA'}
          onClick={() => onChangeEstado('FINALIZADA')}
        />
      </div>

      <hr className={styles.divider} />

      <div className="d-flex flex-column gap-3">
        <div className={styles.filtrosRow}>
          <button
            className={`${styles.chip} ${tipoFigurita === 'SUBASTADA' ? styles.chipActive : ''}`}
            onClick={() => toggleTipo('SUBASTADA')}
          >
            Subastada
          </button>
          <button
            className={`${styles.chip} ${tipoFigurita === 'OFERTADA' ? styles.chipActive : ''}`}
            onClick={() => toggleTipo('OFERTADA')}
          >
            Ofertada
          </button>
        </div>

        <div className={styles.filtrosInputRow}>
          <AutocompleteInput
            value={jugadorQuery}
            onChange={setJugadorQuery}
            onSearch={async (texto) => await buscarFiguritas({ jugador: texto })}
            onSelect={handleSelect}
            getLabel={(fig) => `${fig.jugador} - ${fig.seleccion} #${fig.numero}`}
            placeholder="Buscar por nombre del jugador"
            disabled={!tipoFigurita}
          />
          <Button
            label="Aplicar"
            onClick={handleAplicar}
            disabled={!tipoFigurita || !tieneFiltroFigurita}
          />
        </div>

        <FiltrosActivos chips={chipsActivos()} onQuitar={handleQuitar} />
      </div>
    </div>
  )
}

export default FiltroSubasta
