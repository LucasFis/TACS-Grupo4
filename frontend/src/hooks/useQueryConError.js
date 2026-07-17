import { useEffect } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useError } from '@/contexts/errorContext.jsx'
import { useToast } from '@/contexts/toastContext.jsx'

const useQueryConError = ({ showToastOnError = true, ...options }) => {
  const { handleError } = useError()
  const { showToast } = useToast()

  const result = useQuery(options)

  useEffect(() => {
    if (result.error) {
      if (result.error.code === 'ERR_CANCELED') return
      if (showToastOnError) {
        showToast(handleError(result.error, () => {}), 'error')
      } else {
        handleError(result.error, () => {})
      }
    }
  }, [result.error])

  return result
}

export default useQueryConError
