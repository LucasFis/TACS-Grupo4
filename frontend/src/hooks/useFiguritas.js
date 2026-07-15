import { useQuery } from '@tanstack/react-query'
import { explorarFiguritas } from '@/services/explorarService'
import { useError } from '@/contexts/errorContext.jsx'
import { useToast } from '@/contexts/toastContext.jsx'

const useFiguritas = (q, jugador, seleccion, numero, tipos, page) => {
  const { handleError } = useError()
  const { showToast } = useToast()

  const { data, isLoading, error } = useQuery({
    queryKey: ['figuritas', { q, jugador, seleccion, numero, tipos, page }],
    queryFn: ({ signal }) => explorarFiguritas({ q, jugador, seleccion, numero, tipos, page }, signal),
    onError: (error) => {
      if (error.code === 'ERR_CANCELED') return
      showToast(handleError(error, () => {}), 'error')
    },
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
