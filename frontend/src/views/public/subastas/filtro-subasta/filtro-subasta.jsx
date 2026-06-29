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

  const handleQuitar = () => {
    resetFigurita()
    onAplicarFigurita({})
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

      <div className={styles.seccionBusqueda}>
        <FilterChip
          label="Subastada"
          selected={tipoFigurita === 'SUBASTADA'}
          onClick={() => toggleTipo('SUBASTADA')}
        />
        <FilterChip
          label="Ofertada"
          selected={tipoFigurita === 'OFERTADA'}
          onClick={() => toggleTipo('OFERTADA')}
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

export default FiltroSubasta
