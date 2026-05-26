import Breadcrumb from "@/components/ui/breadcrumb/breadcrumb.jsx";
import ContadorCard from "@/components/ui/contador-card/contador-card.jsx";
import styles from './sugerencias.module.css';
import TabsContainer from "@/components/ui/tabs-container/tabs-container.jsx";
import {useCallback, useEffect, useState} from "react";
import {buscarContadoresSugerencias} from "@/services/perfilService.js";
import ExtraInfo from "@/components/ui/extra-info/extra-info.jsx";
import MostradorSugerencias from "./tabs/mostrador-sugerencias.jsx";
import useUsuarioActual from "@/hooks/useUsuarioActual.js";

const Sugerencias = () => {

    const [cargando, setCargando] = useState(true)
    const [error, setError] = useState(false)
    const [contadores, setContadores] = useState([])
    const {userId} = useUsuarioActual()

    const TABS = [
        { key: 'todos', label: 'Todos', component: MostradorSugerencias, props: {} },
        { key:"1a1", label: "1 a 1", component: MostradorSugerencias, props: {tipo: "1a1", extraInfoChildren:<p>Sugerencias de intercambio uno a uno, para obtener dicha figurita solo se necesita una de las tuyas!</p>} },
        { key:"na1", label: "N a 1", component: MostradorSugerencias, props: {tipo: "Na1" , extraInfoChildren: <p>Sugerencias de intercambio muchos a uno, para obtener la figurita se necesitan mas de una de las tuyas!</p>} },
        { key:"1an", label: "1 a N", component: MostradorSugerencias, props: {tipo: "1aN", extraInfoChildren: <p>Sugerencias de intercambio uno a muchos, podrias obtener mas de una figurita a cambio de una de las tuyas!</p>}}
    ];

    const cargarContadores = useCallback(async () => {
        try {
            setCargando(true)
            const payload = await buscarContadoresSugerencias()
            setContadores(payload)

          // eslint-disable-next-line no-unused-vars
        } catch (error) {
            setError(true)
        } finally {
            setCargando(false)
        }
    })

    useEffect(() => {
        cargarContadores()
        /*
            stat: [{
                nombre: (string)
                 valor: (number)
            }]
        */
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
                    {cargando ? <h2>Cargando estadisticas...</h2> : error ? <p className="text-center text-secondary">No se pudo cargar la información</p> : mostrarContadores()}
                </div>
                <ExtraInfo>
                    <h6 className="m-0"><strong>¿Cómo funciona?</strong></h6>
                    <p>El sistema analiza tus figuritas faltantes y repetidas, y las cruza con las colecciones
                        de todos los usuarios.</p>
                </ExtraInfo>

                <TabsContainer tabs={TABS}/>
            </div>
        </div>
    )
}

export default Sugerencias