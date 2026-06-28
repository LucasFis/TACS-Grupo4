import Breadcrumb from "@/components/ui/breadcrumb/breadcrumb.jsx";
import ContadorCard from "@/components/ui/contador-card/contador-card.jsx";
import styles from './sugerencias.module.css';
import {useCallback, useEffect, useState} from "react";
import {buscarContadoresSugerencias} from "@/services/perfilService.js";
import ExtraInfo from "@/components/ui/extra-info/extra-info.jsx";
import MostradorSugerencias from "./tabs/mostrador-sugerencias.jsx";
import { useToast } from '@/contexts/toastContext.jsx'
import { useError } from '@/contexts/errorContext.jsx'

const Sugerencias = () => {


    const {showToast} = useToast()
    const {handleError, errorTemplate} = useError()

    const [cargando, setCargando] = useState(true)
    const [error, setError] = useState(errorTemplate())
    const [contadores, setContadores] = useState([])

    const cargarContadores = useCallback(async () => {
        try {
            setCargando(true)
            const payload = await buscarContadoresSugerencias()
            setContadores(payload)

        } catch (error) {
          showToast(handleError(error, setError),'error')
        } finally {
            setCargando(false)
        }
    })

    useEffect(() => {
        cargarContadores()
    }, []);

    const mostrarContadores = () => {
        return (
            <>
                {contadores.map((st, index) => <ContadorCard key={index} title={st.nombre} value={st.valor}/>)}
            </>
        )
    }

    return (
        <div className={styles.sugerenciasBody + " container py-4 px-3 px-md-4"}>
            <Breadcrumb className={styles.left}
                        crumbs={[{name: "Explorar", to: "/explorar"}, {name: "Sugerencias", to: "/sugerencias"}]}/>

            <h2 className={styles.left}><strong>Sugerencias</strong></h2>
            <p className={styles.left + " fs-5 opacity-75"}>Coincidencias entre tus faltantes y los repetidos de otros usuarios, y viceversa</p>

            <hr/>

            <div className="d-flex flex-column flex-nowrap gap-3 w-100">
                <div
                    className={styles.statGrid + " d-grid gap-3"}
                >
                    {cargando ? <h2>Cargando estadisticas...</h2> : error.codigo ? <p className="text-center text-secondary">No se pudo cargar la información</p> : mostrarContadores()}
                </div>
                <ExtraInfo>
                    <h6 className="m-0"><strong>¿Cómo funciona?</strong></h6>
                    <p>El sistema analiza tus figuritas faltantes y repetidas, y las cruza con las colecciones
                        de todos los usuarios. Ademas, se recalculan cada 24hs.
                      Si queres conservar una sugerencia, dale al favorito!</p>
                </ExtraInfo>

                <MostradorSugerencias />
            </div>
        </div>
    )
}

export default Sugerencias