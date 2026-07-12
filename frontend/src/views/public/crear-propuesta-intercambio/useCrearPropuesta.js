import { useState, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import { crearPropuesta } from '@/services/propuestasService.js'
import { useError } from '@/contexts/errorContext.jsx'
import { useToast } from '@/contexts/toastContext.jsx'

const useCrearPropuesta = (figurita) => {
  const [seleccionadas, setSeleccionadas] = useState([])
  const [enviando, setEnviando] = useState(false)
  const enviandoRef = useRef(false)
  const navigate = useNavigate()
  const { handleError } = useError()
  const { showToast } = useToast()

  const enviar = async () => {
    if (enviandoRef.current) return
    enviandoRef.current = true
    setEnviando(true)
    try {
      await crearPropuesta(
        figurita.perfil_id,
        figurita.figurita_id,
        seleccionadas.map((f) => f.figurita_id),
      )
      showToast('Propuesta creada correctamente', 'success')
      navigate('/intercambios')
    } catch (e) {
      handleError(e, (err) => showToast(err.mensaje, 'error'))
    } finally {
      enviandoRef.current = false
      setEnviando(false)
    }
  }

  return { seleccionadas, setSeleccionadas, enviar, enviando }
}

export default useCrearPropuesta
