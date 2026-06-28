import { useState } from 'react'

const validarMedio = (tipo, valor) => {
  if (!valor.trim()) return 'El valor no puede estar vacío'
  if (tipo === 'MAIL') {
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(valor)) return 'Email inválido'
  }
  if (tipo === 'TELEGRAM') {
    if (!/^@[a-zA-Z0-9_]{2,}$/.test(valor)) return 'Debe tener formato @usuario'
  }
  return null
}

export const useMediosContacto = (mediosEditando, setMediosEditando) => {
  const [indiceMedioEditando, setIndiceMedioEditando] = useState(-1)

  const [medioEditandoDataState, setMedioEditandoDataState] = useState({
    medio_comunicacion: 'TELEGRAM',
    valor: '',
  })
  const [nuevoMedioTipoState, setNuevoMedioTipoState] = useState('TELEGRAM')
  const [nuevoMedioValorState, setNuevoMedioValorState] = useState('')
  const [errorNuevoMedio, setErrorNuevoMedio] = useState(null)
  const [errorMedioEditando, setErrorMedioEditando] = useState(null)

  const setNuevoMedioTipo = (v) => {
    setNuevoMedioTipoState(v)
    setErrorNuevoMedio(null)
  }
  const setNuevoMedioValor = (v) => {
    setNuevoMedioValorState(v)
    setErrorNuevoMedio(null)
  }
  const setMedioEditandoData = (updater) => {
    setMedioEditandoDataState(updater)
    setErrorMedioEditando(null)
  }

  const agregarMedio = () => {
    const error = validarMedio(nuevoMedioTipoState, nuevoMedioValorState)
    if (error) {
      setErrorNuevoMedio(error)
      return
    }
    setMediosEditando((prev) => [
      ...prev,
      { medio_comunicacion: nuevoMedioTipoState, valor: nuevoMedioValorState },
    ])
    setNuevoMedioValorState('')
    setErrorNuevoMedio(null)
  }

  const eliminarMedio = (i) => {
    setMediosEditando((prev) => prev.filter((_, idx) => idx !== i))
    if (indiceMedioEditando === i) setIndiceMedioEditando(-1)
  }

  const confirmarEdicionMedio = (i) => {
    const error = validarMedio(
      medioEditandoDataState.medio_comunicacion,
      medioEditandoDataState.valor,
    )
    if (error) {
      setErrorMedioEditando(error)
      return
    }
    setMediosEditando((prev) =>
      prev.map((m, idx) => (idx === i ? { ...medioEditandoDataState } : m)),
    )
    setIndiceMedioEditando(-1)
    setErrorMedioEditando(null)
  }

  const iniciarEdicionMedio = (i, medio) => {
    setIndiceMedioEditando(i)
    setMedioEditandoDataState({ ...medio })
    setErrorMedioEditando(null)
  }

  const cancelarEdicionMedio = () => {
    setIndiceMedioEditando(-1)
    setErrorMedioEditando(null)
  }

  return {
    indiceMedioEditando,
    medioEditandoData: medioEditandoDataState,
    setMedioEditandoData,
    nuevoMedioTipo: nuevoMedioTipoState,
    setNuevoMedioTipo,
    nuevoMedioValor: nuevoMedioValorState,
    setNuevoMedioValor,
    errorNuevoMedio,
    errorMedioEditando,
    agregarMedio,
    eliminarMedio,
    confirmarEdicionMedio,
    iniciarEdicionMedio,
    cancelarEdicionMedio,
  }
}
