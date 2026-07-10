import { useEffect, useState } from 'react'
import { buscarSubastas } from '../../../../../services/subastasService.js'
import MiSubasta from './mi-subasta/mi-subasta.jsx'
import Paginacion from '../../../../../components/ui/paginacion/paginacion.jsx'
import FiltroSubasta from '../../filtro-subasta/filtro-subasta.jsx'
import { useAuth } from '@/contexts/userContext.jsx'
import { useError } from '@/contexts/errorContext.jsx'
import { useToast } from '@/contexts/toastContext.jsx'
import { derivarTiempo } from '../../../../../utils/subastasTiempo.js'

const MisSubastas = () => {
  const [data, setData] = useState({})
  const [loading, setLoading] = useState(true)
  const [estado, setEstado] = useState('ACTIVA')
  const [pagina, setPagina] = useState(1)
  const [refresh, setRefresh] = useState(0)
  const [paramsFigurita, setParamsFigurita] = useState({})

  const { user } = useAuth()
  const { handleError } = useError()
  const { showToast } = useToast()

  useEffect(() => {
    const cargar = async () => {
      try {
        setLoading(true)
        const res = await buscarSubastas({ autorId: user.perfil_id, estado, pagina, limite: 5, ...paramsFigurita })
        setData(res)
      } catch (error) {
        showToast(handleError(error, (m) => {}),'error')
      } finally {
        setLoading(false)
      }
    }
    cargar()
  }, [estado, pagina, refresh, paramsFigurita])

  const cambiarEstado = (nuevoEstado) => {
    if (estado === nuevoEstado) return
    setEstado(nuevoEstado)
    setPagina(1)
  }

  return (
    <div className="container-fluid px-0 d-flex flex-column gap-4">
      <FiltroSubasta
        estado={estado}
        onChangeEstado={cambiarEstado}
        onAplicarFigurita={(p) => { setParamsFigurita(p); setPagina(1) }}
      />

      {loading ? (
        <div className="d-flex flex-column gap-3">
          {[...Array(3)].map((_, i) => (
            <div key={i} className="rounded-3 placeholder-glow border" style={{ height: '180px' }}>
              <div className="placeholder w-100 h-100 rounded-3" />
            </div>
          ))}
        </div>
      ) : data.contenido?.length > 0 ? (
        <>
          <div className="d-flex flex-column gap-3">
            {data.contenido.map((sub) => (
              <MiSubasta
                key={sub.id}
                subasta={sub}
                finalizada={estado === 'FINALIZADA'}
                finalizadaHace={sub.finalizada_hace}
                onRefresh={() => setRefresh(r => r + 1)}
              />
            ))}
          </div>
          <div className="pt-3 d-flex justify-content-center">
            <Paginacion
              page={pagina}
              totalPages={data.cantidad_de_paginas ?? 1}
              onChange={setPagina}
            />
          </div>
        </>
      ) : (
        <div className="text-center text-muted py-5">
          <div className="fs-1">📭</div>
          <p className="mb-0">
            {estado === 'ACTIVA' ? 'No tenés subastas activas' : 'No tenés subastas finalizadas'}
          </p>
        </div>
      )}
    </div>
  )
}

export default MisSubastas
