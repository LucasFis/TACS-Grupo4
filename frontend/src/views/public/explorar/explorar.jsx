import { useEffect, useRef, useState } from 'react'
import useFiguritas from '@/hooks/useFiguritas'
import usePaginacion from '@/hooks/usePaginacion'
import ExplorarSearch from './search/explorar-search'
import ExplorarFiltros from './filtros/explorar-filtros'
import ExplorarResultados from './resultados/explorar-resultados'
import styles from './explorar.module.css'

const FILTROS_INICIAL = { q: '', tipos: [], jugador: '', seleccion: '', numero: '' }

const Explorar = () => {
  const resultadosRef = useRef(null)
  const heroInputRef = useRef(null)
  const [filtros, setFiltros] = useState(FILTROS_INICIAL)
  const { pagina, setPagina, resetPagina } = usePaginacion()

  const { figuritas, totalPages, totalElements, loading, error } = useFiguritas(
    filtros.q,
    filtros.jugador,
    filtros.seleccion,
    filtros.numero,
    filtros.tipos,
    pagina,
  )

  const handleAplicar = (nuevosFiltros) => {
    setFiltros(nuevosFiltros)
    resetPagina()
  }

  useEffect(() => {
    resultadosRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [pagina])

  return (
    <main className={styles.page}>
      <ExplorarSearch
        ref={heroInputRef}
        onQueryChange={(q) => handleAplicar({ ...FILTROS_INICIAL, q })}
      />
      <div className={styles.container}>
        <ExplorarFiltros
          onAplicar={(panelFiltros) => {
            if (heroInputRef.current) heroInputRef.current.value = ''
            handleAplicar({ ...FILTROS_INICIAL, ...panelFiltros })
          }}
        />
        <div ref={resultadosRef} />
        <ExplorarResultados
          figuritas={figuritas}
          totalElements={totalElements}
          totalPages={totalPages}
          page={pagina}
          onPageChange={setPagina}
          loading={loading}
          error={error}
        />
      </div>
    </main>
  )
}

export default Explorar
