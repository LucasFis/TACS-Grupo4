import { useQuery } from '@tanstack/react-query'
import { buscarCalificaciones } from '@/services/perfilService.js'
import { useError } from '@/contexts/errorContext.jsx'
import { useToast } from '@/contexts/toastContext.jsx'
import usePaginacion from '@/hooks/usePaginacion.js'

export const useCalificaciones = (perfilId) => {
  const { handleError } = useError()
  const { showToast } = useToast()
  const { pagina, setPagina } = usePaginacion()

  const { data: reviews, isLoading: loading } = useQuery({
    queryKey: ['calificaciones', perfilId, { pagina, limite: 10 }],
    queryFn: ({ signal }) => buscarCalificaciones(perfilId, { pagina, limite: 10 }, signal),
    onError: (error) => {
      if (error.code === 'ERR_CANCELED') return
      showToast(handleError(error, () => {}), 'error')
    },
  })

  return { reviews: reviews ?? {}, loading, pagina, setPagina }
}
