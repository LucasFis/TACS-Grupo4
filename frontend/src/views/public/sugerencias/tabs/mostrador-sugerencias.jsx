import SugerenciaCard from "@/views/public/sugerencias/sugerencia-card.jsx";
import {useCallback, useEffect, useState} from "react";
import {buscarSugerencias} from "@/services/perfilService.js";
import ExtraInfo from "@/components/ui/extra-info/extra-info.jsx";
import Paginacion from "@/components/ui/paginacion/paginacion.jsx";
import useUsuarioActual from "@/hooks/useUsuarioActual.js";

const MostradorSugerencias = ({tipo, extraInfoChildren}) => {

    const [cargando, setCargando] = useState(true)
    const [error, setError] = useState(false)
    const [sugerencias, setSugerencias] = useState([])
    const [pagina, setPagina] = useState(1)
    const [paginasTotales, setPaginasTotales] = useState(1)
    const {userId} = useUsuarioActual()

    const cargarSugerencias = useCallback(async () => {
        try {
            setCargando(true)
            const payload = await buscarSugerencias({ tipo, pagina: pagina, limite:10})
            setPaginasTotales(payload.cantidadDePaginas)
            setSugerencias(payload.contenido)

        } catch (error) {
            setError(true)
        } finally {
            setCargando(false)
        }
    })

    useEffect(() => {
        cargarSugerencias()
    }, [tipo]);

    const mostrarSugerencias = () => {
        if (error) return <h2 className="text-center text-secondary">No se pudo cargar la información</h2>
        return (
            <>
                {
                    extraInfoChildren ? <ExtraInfo>
                        {extraInfoChildren}
                    </ExtraInfo> : null
                }
                {
                    sugerencias.length > 0 ?
                        sugerencias.map(s => <SugerenciaCard key={s.perfil.id} perfil={s.perfil}
                                                     figuritasNecesarias = {s.figuritas_necesarias}
                                                     figuritasRecomendadas = {s.figuritas_recomendadas}/>
                        ) : <h2 className="text-center text-muted py-4 fw-light">No pudimos encontrar sugerencias!</h2>
                }
                <Paginacion page={pagina} totalPages={paginasTotales} onChange={setPagina}/>
            </>
        )
    }

    return (
        <div className="d-flex flex-column gap-3 ms-2 me-2">
            {cargando ? <h2>Cargando sugerencias...</h2> : mostrarSugerencias()}
        </div>
    )
}

export default MostradorSugerencias