import { useState } from "react";
import { useQuery } from '@tanstack/react-query';
import { buscarFaltantes } from "@/services/coleccionService.js";
import FaltanteCard from "../../../../../components/ui/faltante-card/faltante-card.jsx";
import Paginacion from "../../../../../components/ui/paginacion/paginacion.jsx";
import { useNavigate } from "react-router";
import Button from "../../../../../components/ui/button/button.jsx";
import { useError } from "@/contexts/errorContext.jsx";
import { useToast } from '@/contexts/toastContext.jsx';
import SugerenciasBanner from './../../sugerencias-banner/sugerencias-banner.jsx'

const Faltantes = () => {
    const { handleError } = useError();
    const { showToast } = useToast();
    const [filtros, setFiltros] = useState({});
    const [pagina, setPagina] = useState(1);
    const navigate = useNavigate();

    const { data: faltantes, isLoading } = useQuery({
        queryKey: ['faltantes', { ...filtros, pagina, limite: 10 }],
        queryFn: ({ signal }) => buscarFaltantes({ ...filtros, pagina, limite: 10 }, signal),
        onError: (error) => {
            if (error.code === 'ERR_CANCELED') return;
            showToast(handleError(error, () => {}), 'error');
        },
    })

    return (
        <div className="container-fluid px-0 d-flex flex-column gap-4">

          <SugerenciasBanner />

            <div className="row justify-content-center">
                <div className="col-12 col-sm-8 col-md-6 col-lg-4">
                    <div
                        className="border rounded-4 p-4 text-center shadow-sm"
                        style={{ backgroundColor: "var(--color-primary)" }}
                    >
                        <p className="mb-1 fw-bold fs-2">
                            {faltantes?.cantidad_de_elementos ?? 0}
                        </p>
                        <p className="mb-0 text-muted">Me Faltan</p>
                    </div>
                </div>
            </div>

            <div className="d-flex justify-content-between align-items-center gap-3 flex-nowrap">
                <p className="mb-0">
                    {faltantes?.cantidad_de_elementos ?? 0} resultados encontrados
                </p>

                <div className="flex-shrink-0">
                    <Button
                        label="Agregar faltante ↗"
                        onClick={() => navigate("/mis-figuritas/nueva-faltante")}
                        variante="terciario"
                    />
                </div>
            </div>

            {isLoading ? (
                <div className="row g-4">
                    {[...Array(10)].map((_, i) => (
                        <div key={i} className="col-12 col-md-6 col-lg-4">
                            <div
                                className="rounded-4 placeholder-glow border"
                                style={{ height: "320px" }}
                            >
                                <div className="placeholder w-100 h-100 rounded-4"></div>
                            </div>
                        </div>
                    ))}
                </div>
            ) : (
                <>
                    <div className="row g-4">
                        {faltantes?.contenido?.length > 0 ? (
                            faltantes.contenido.map((fig) => (
                                <div
                                    key={fig.id}
                                    className="col-12 col-md-6 col-lg-4"
                                >
                                    <FaltanteCard figurita={fig} />
                                </div>
                            ))
                        ) : (
                            <div className="col-12 text-center text-muted py-5">
                                <div className="fs-1">📭</div>
                                <p className="mb-0">No hay resultados...</p>
                            </div>
                        )}
                    </div>

                    <Paginacion
                        page={pagina}
                        totalPages={faltantes?.cantidad_de_paginas ?? 1}
                        onChange={setPagina}
                    />
                </>
            )}
        </div>
    );
};

export default Faltantes;
