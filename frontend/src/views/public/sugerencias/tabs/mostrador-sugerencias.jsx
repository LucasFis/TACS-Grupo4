import SugerenciaCard from "@/views/public/sugerencias/sugerencia-card.jsx";
import {useState} from "react";
import useQueryConError from '@/hooks/useQueryConError'
import Paginacion from "@/components/ui/paginacion/paginacion.jsx";
import { buscarSugerencias } from '@/services/sugerenciasService.js'

const MostradorSugerencias = () => {

    const [pagina, setPagina] = useState(1)

    const { data, isLoading, error } = useQueryConError({
        queryKey: ['sugerencias', { pagina, limite: 10 }],
        queryFn: ({ signal }) => buscarSugerencias({ pagina, limite: 10 }, signal),
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
