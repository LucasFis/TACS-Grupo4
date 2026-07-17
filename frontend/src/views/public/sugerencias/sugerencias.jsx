import SectionTitle from "@/components/ui/section-title/section-title.jsx";
import ContadorCard from "@/components/ui/contador-card/contador-card.jsx";
import Button from "@/components/ui/button/button.jsx";
import styles from './sugerencias.module.css';
import {useState} from "react";
import { useQueryClient } from '@tanstack/react-query'
import useQueryConError from '@/hooks/useQueryConError'
import {buscarContadoresSugerencias} from "@/services/perfilService.js";
import {recalcularSugerencias} from "@/services/sugerenciasService.js";
import ExtraInfo from "@/components/ui/extra-info/extra-info.jsx";
import MostradorSugerencias from "./tabs/mostrador-sugerencias.jsx";
import { useToast } from '@/contexts/toastContext.jsx'
import { useError } from '@/contexts/errorContext.jsx'

const Sugerencias = () => {

    const {showToast} = useToast()
    const {handleError} = useError()
    const queryClient = useQueryClient()

    const [recalculando, setRecalculando] = useState(false)
    const [revision, setRevision] = useState(0)

    const { data: contadores, isLoading, error } = useQueryConError({
        queryKey: ['contadoresSugerencias'],
        queryFn: ({ signal }) => buscarContadoresSugerencias(signal),
    })

    const handleRecalcular = async () => {
        try {
            setRecalculando(true)
            await recalcularSugerencias()
            queryClient.invalidateQueries({ queryKey: ['contadoresSugerencias'] })
            setRevision(r => r + 1)
            showToast('Sugerencias recalculadas', 'success')
        } catch (error) {
            showToast(handleError(error, () => {}), 'error')
        } finally {
            setRecalculando(false)
        }
    }

    return (
        <main className="container py-4 px-3 px-md-4">
            <div className="mx-auto" style={{ maxWidth: "900px" }}>
                <div className="d-flex flex-column gap-4">
                    <SectionTitle>
                        Sugerencias
                        <Button variante="primario" className="btn-sm" disabled={recalculando} onClick={handleRecalcular}>
                            {recalculando ? 'Recalculando...' : 'Recalcular sugerencias'}
                        </Button>
                    </SectionTitle>

                    <div className={styles.statGrid + " d-grid gap-3"}>
                        {isLoading ? (
                            <h2>Cargando estadisticas...</h2>
                        ) : error ? (
                            <p className="text-center text-secondary">No se pudo cargar la información</p>
                        ) : (
                            (contadores ?? []).map((st, index) => (
                                <ContadorCard key={index} title={st.nombre} value={st.valor} />
                            ))
                        )}
                    </div>

                    <ExtraInfo>
                        <h6 className="m-0"><strong>¿Cómo funciona?</strong></h6>
                        <p>El sistema analiza tus figuritas faltantes y repetidas, y las cruza con las colecciones
                            de todos los usuarios. Podes recalcular las sugerencias manualmente en cualquier momento,
                          o esperar a que se actualicen automaticamente cada 24hs. Si queres conservar una sugerencia, dale al favorito!</p>
                    </ExtraInfo>

                    <MostradorSugerencias key={revision} />
                </div>
            </div>
        </main>
    )
}

export default Sugerencias
