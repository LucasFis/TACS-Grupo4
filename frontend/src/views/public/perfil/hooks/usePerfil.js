import { useQueryClient } from '@tanstack/react-query'
import useQueryConError from '@/hooks/useQueryConError'
import { buscarContadores, buscarPerfil } from '@/services/perfilService.js'
import { useAuth } from '@/contexts/userContext.jsx'
import { truncarADosDecimales } from '@/utils/estandarizar.js'

export const usePerfil = (perfilId) => {
  const { user, cerrarSesion } = useAuth()
  const queryClient = useQueryClient()

  const { data: perfil, isLoading: loading } = useQueryConError({
    queryKey: ['perfil', perfilId],
    queryFn: ({ signal }) => buscarPerfil(perfilId, signal),
  })

  const { data: stats } = useQueryConError({
    queryKey: ['contadores', perfilId],
    queryFn: ({ signal }) => buscarContadores(perfilId, signal),
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
