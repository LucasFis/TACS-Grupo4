import { useQuery, useQueryClient } from '@tanstack/react-query'
import { buscarContadores, buscarPerfil } from '@/services/perfilService.js'
import { useAuth } from '@/contexts/userContext.jsx'
import { useError } from '@/contexts/errorContext.jsx'
import { useToast } from '@/contexts/toastContext.jsx'
import { truncarADosDecimales } from '@/utils/estandarizar.js'

export const usePerfil = (perfilId) => {
  const { handleError } = useError()
  const { showToast } = useToast()
  const { user, cerrarSesion } = useAuth()
  const queryClient = useQueryClient()

  const { data: perfil, isLoading: loading } = useQuery({
    queryKey: ['perfil', perfilId],
    queryFn: ({ signal }) => buscarPerfil(perfilId, signal),
    onError: (error) => {
      if (error.code === 'ERR_CANCELED') return
      showToast(handleError(error, () => {}), 'error')
    },
  })

  const { data: stats } = useQuery({
    queryKey: ['contadores', perfilId],
    queryFn: ({ signal }) => buscarContadores(perfilId, signal),
    onError: (error) => {
      if (error.code === 'ERR_CANCELED') return
      showToast(handleError(error, () => {}), 'error')
    },
  })

  const manejarCierreDeSesion = async () => {
    try {
      await cerrarSesion()
      showToast('Cierre de sesion exitoso', 'success')
    } catch {
      showToast('Error al cerrar sesion', 'error')
    }
  }

  return {
    perfil: perfil ?? {},
    setPerfil: (updater) => {
      queryClient.setQueryData(['perfil', perfilId], (old) =>
        typeof updater === 'function' ? updater(old) : updater
      )
    },
    loading,
    stats: stats ?? [],
    promedio: truncarADosDecimales(perfil?.calificacion_media),
    perfilId: user.perfil_id,
    manejarCierreDeSesion,
  }
}
