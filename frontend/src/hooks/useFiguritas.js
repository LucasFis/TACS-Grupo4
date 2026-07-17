import useQueryConError from '@/hooks/useQueryConError'
import { explorarFiguritas } from '@/services/explorarService'

const useFiguritas = (q, jugador, seleccion, numero, tipos, page) => {
  const { data, isLoading, error } = useQueryConError({
    queryKey: ['figuritas', { q, jugador, seleccion, numero, tipos, page }],
    queryFn: ({ signal }) => explorarFiguritas({ q, jugador, seleccion, numero, tipos, page }, signal),
  })

  return {
    figuritas: data?.contenido ?? [],
    totalPages: data?.totalPages ?? 0,
    totalElements: data?.totalElements ?? 0,
    loading: isLoading,
    error: !!error,
  }
}

export default useFiguritas
