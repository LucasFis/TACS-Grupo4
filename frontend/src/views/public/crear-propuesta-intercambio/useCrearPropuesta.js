import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { crearPropuesta } from '@/services/propuestasService.js'
import { buscarFaltantes } from '@/services/coleccionService.js'
import { useError } from '@/contexts/errorContext.jsx'
import { useToast } from '@/contexts/toastContext.jsx'

const useCrearPropuesta = (figurita) => {
  const [seleccionadas, setSeleccionadas] = useState([])
  const [enviando, setEnviando] = useState(false)
  const [validando, setValidando] = useState(true)
  const navigate = useNavigate()
  const { handleError } = useError()
  const { showToast } = useToast()

  useEffect(() => {
    if (!figurita) return

    const validarFaltante = async () => {
      try {
        const data = await buscarFaltantes({ limite: 300, pagina: 1 })
        const esFaltante = (data?.contenido ?? []).some((f) => f.id === figurita.figurita_id)
        if (!esFaltante) {
          showToast(
            `La figurita #${figurita.numero} de ${figurita.jugador} no está en tus faltantes`,
            'error',
          )
          navigate(-1)
        }
      } catch {
        // si falla la validación dejamos pasar; el backend rechazará si corresponde
      } finally {
        setValidando(false)
      }
    }

    validarFaltante()
  }, [figurita, navigate, showToast])

  const enviar = async () => {
    if (enviando) return
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
      handleError(e, (err) => {
        const texto = err.mensaje || 'No se pudo crear la propuesta'
        showToast(texto, 'error')
      })
    } finally {
      setEnviando(false)
    }
  }

  return { seleccionadas, setSeleccionadas, enviar, enviando, validando }
}

export default useCrearPropuesta
