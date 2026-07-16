import SugerenciaCard from "@/views/public/sugerencias/sugerencia-card.jsx";
import {useState} from "react";
import { useQuery } from '@tanstack/react-query'
import Paginacion from "@/components/ui/paginacion/paginacion.jsx";
import { useError } from '@/contexts/errorContext.jsx'
import { useToast } from '@/contexts/toastContext.jsx'
import { buscarSugerencias } from '@/services/sugerenciasService.js'

const MostradorSugerencias = () => {

    const {handleError} = useError()
    const {showToast} = useToast()

    const [pagina, setPagina] = useState(1)

    const { data, isLoading, error } = useQuery({
        queryKey: ['sugerencias', { pagina, limite: 10 }],
        queryFn: ({ signal }) => buscarSugerencias({ pagina, limite: 10 }, signal),
        onError: (err) => {
            if (err.code === 'ERR_CANCELED') return
            showToast(handleError(err, () => {}), 'error')
        },
    })

    if (error) return <h2 className="text-center text-secondary">No se pudo cargar la información</h2>

    const sugerencias = data?.contenido ?? []
    const paginasTotales = data?.cantidad_de_paginas ?? 1

    return (
        <div className="d-flex flex-column gap-3">
            {isLoading ? (
                <h2>Cargando sugerencias...</h2>
            ) : sugerencias.length > 0 ? (
                <>
                    {sugerencias.map(s => (
                        <SugerenciaCard
                            key={s.sugerido.id}
                            id={s.id}
                            perfil={s.sugerido}
                            figuritasNecesarias={s.figuritas_necesarias}
                            figuritasRecomendadas={s.figuritas_recomendadas}
                            favorito={s.favorito}
                        />
                    ))}
                    <Paginacion page={pagina} totalPages={paginasTotales} onChange={setPagina} />
                </>
            ) : (
                <h2 className="text-center text-muted py-4 fw-light">No pudimos encontrar sugerencias!</h2>
            )}
        </div>
    )
}

export default MostradorSugerencias
