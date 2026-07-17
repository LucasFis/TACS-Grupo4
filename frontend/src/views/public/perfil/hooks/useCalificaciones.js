import useQueryConError from '@/hooks/useQueryConError'
import { buscarCalificaciones } from '@/services/perfilService.js'
import usePaginacion from '@/hooks/usePaginacion.js'

export const useCalificaciones = (perfilId) => {
  const { pagina, setPagina } = usePaginacion()

  const { data: reviews, isLoading: loading } = useQueryConError({
    queryKey: ['calificaciones', perfilId, { pagina, limite: 10 }],
    queryFn: ({ signal }) => buscarCalificaciones(perfilId, { pagina, limite: 10 }, signal),
  })

  return { reviews: reviews ?? {}, loading, pagina, setPagina }
}
