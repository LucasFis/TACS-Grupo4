import { useState } from 'react'

const useFiltroIntercambio = () => {
  const [tipoFigurita, setTipoFigurita] = useState(null)
  const [figuritaSeleccionada, setFiguritaSeleccionada] = useState(null)
  const [jugadorQuery, setJugadorQuery] = useState('')

  const toggleTipo = (key) => {
    if (tipoFigurita === key) return
    setTipoFigurita(key)
    setJugadorQuery(figuritaSeleccionada?.jugador ?? '')
  }

  const seleccionarFigurita = (fig) => {
    setFiguritaSeleccionada(fig)
    setJugadorQuery(fig.jugador)
  }

  const aplicar = () => {
    if (!tipoFigurita || !figuritaSeleccionada) return {}
    const key = tipoFigurita === 'BUSCADA' ? 'idFiguritaBuscada' : 'idFiguritaPropuesta'
    return { [key]: figuritaSeleccionada.id }
  }

  const resetFigurita = () => {
    setTipoFigurita(null)
    setFiguritaSeleccionada(null)
    setJugadorQuery('')
  }

  const chipsActivos = () => {
    if (!tipoFigurita || !figuritaSeleccionada) return []
    const label = tipoFigurita === 'BUSCADA' ? 'Buscada' : 'Propuesta'
    return [{
      campo: 'tipoFigurita',
      valor: tipoFigurita,
      label: `${figuritaSeleccionada.jugador} (${label})`,
    }]
  }

  const tieneFiltroFigurita = !!(tipoFigurita && figuritaSeleccionada)

  return {
    tipoFigurita,
    figuritaSeleccionada,
    jugadorQuery,
    tieneFiltroFigurita,
    setJugadorQuery,
    toggleTipo,
    seleccionarFigurita,
    aplicar,
    resetFigurita,
    chipsActivos,
  }
}

export default useFiltroIntercambio
