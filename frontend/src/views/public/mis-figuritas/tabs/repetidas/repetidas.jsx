import { useEffect, useState } from "react";
import { buscarRepetidas, editarRepetida } from '@/services/coleccionService.js'
import RepetidaCard from "../../../../../components/ui/repetida-card/repetida-card.jsx";
import FilterChip from "../../../../../components/ui/filter-chip/filter-chip.jsx";
import Button from "../../../../../components/ui/button/button.jsx";
import { useNavigate } from "react-router";
import Paginacion from "../../../../../components/ui/paginacion/paginacion.jsx";
import {useError} from "@/contexts/errorContext.jsx";
import { useToast } from '@/contexts/toastContext.jsx'
import EditarRepetidaModal from '@/components/ui/editar-repetida-modal/editar-repetida-modal.jsx'

const Repetidas = () => {
    const [repetidas, setRepetidas] = useState({});
    const [filtros, setFiltros] = useState({
        metodoIntercambio: "",
    });

    const [showModal, setShowModal] = useState(false);
    const [repetidaSeleccionada, setRepetidaSeleccionada] = useState(undefined);

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(false);
    const [pagina, setPagina] = useState(1);

    const {handleError} = useError()
    const {showToast} = useToast()

    const navigate = useNavigate();

    useEffect(() => {
        const cargarRepetidas = async () => {
            try {
                setLoading(true);

                const repetidasApi = await buscarRepetidas({
                    ...filtros,
                    pagina,
                    limite: 10,
                });

                setRepetidas(repetidasApi);
            } catch (err) {
              showToast(handleError(err, setError),'error')
            } finally {
                setLoading(false);
            }
        };

        cargarRepetidas();
    }, [filtros, pagina]);

    const cambiarFiltro = (nuevoTipo) => {
        setFiltros((prev) => {
            if (prev.metodoIntercambio === nuevoTipo) return prev;

            return {
                ...prev,
                metodoIntercambio: nuevoTipo,
            };
        });

        setPagina(1);
    };

    const abrirModalEdicion = (figurita) => {
      setRepetidaSeleccionada(figurita);
      setShowModal(true);
    };

    const cerrarModalEdicion = () => {
      setShowModal(false);
      setRepetidaSeleccionada(null);
    };

  const guardarCambiosRepetida = async (payload) => {
    try {
      await editarRepetida(
        repetidaSeleccionada.figurita_id,
        payload
      );

      setRepetidas((prev) => ({
        ...prev,
        contenido: prev.contenido.map((item) =>
          item.figurita_id === repetidaSeleccionada.figurita_id
            ? {
              ...item,
              cantidad_existente: payload.cantidadNueva,
              metodos: payload.metodos,
            }
            : item
        ),
      }));

      showToast("Repetida actualizada", "success");
      cerrarModalEdicion();
    } catch (err) {

      showToast(handleError(err, () => {}), "error");
    }
  };

    if (error) {
        return (
            <div className="text-center text-danger py-4">
                Error al cargar repetidas
            </div>
        );
    }

    return (
        <div className="container-fluid px-0 d-flex flex-column gap-4">

            <div className="row g-3 justify-content-center">
                <div className="col-6 col-md-4">
                    <div
                        className="border rounded-4 p-4 text-center shadow-sm h-100"
                        style={{ backgroundColor: "var(--color-primary)" }}
                    >
                        <p className="mb-1 fw-bold fs-2">
                            {repetidas.publicadas ?? 0}
                        </p>
                        <p className="mb-0 text-muted">Publicadas</p>
                    </div>
                </div>

                <div className="col-6 col-md-4">
                    <div
                        className="border rounded-4 p-4 text-center shadow-sm h-100"
                        style={{ backgroundColor: "var(--color-primary)" }}
                    >
                        <p className="mb-1 fw-bold fs-2">
                            {repetidas.disponibles ?? 0}
                        </p>
                        <p className="mb-0 text-muted">Disponibles</p>
                    </div>
                </div>
            </div>

            <div className="d-flex flex-wrap justify-content-start gap-2">
                <FilterChip
                    label="Todas"
                    selected={
                        filtros.metodoIntercambio ===
                        ""
                    }
                    onClick={() =>
                        cambiarFiltro("")
                    }
                />

                <FilterChip
                    label="Intercambio"
                    selected={
                        filtros.metodoIntercambio === "INTERCAMBIO"
                    }
                    onClick={() => cambiarFiltro("INTERCAMBIO")}
                />

                <FilterChip
                    label="Subasta"
                    selected={
                        filtros.metodoIntercambio === "SUBASTA"
                    }
                    onClick={() => cambiarFiltro("SUBASTA")}
                />
            </div>

            <div className="d-flex justify-content-between align-items-center gap-3 flex-nowrap">
                <p className="mb-0">
                    {repetidas.cantidad_de_elementos ?? 0} resultados encontrados
                </p>

                <div className="flex-shrink-0">
                    <Button
                        label="Agregar repetida ↗"
                        onClick={() =>
                            navigate("/mis-figuritas/nueva-repetida")
                        }
                    />
                </div>
            </div>

            {loading ? (
                <div className="row g-3 justify-content-center">
                    {[...Array(8)].map((_, i) => (
                        <div
                            key={i}
                            className="col-6 col-md-4 col-lg-3"
                        >
                            <div
                                className="rounded-4 placeholder-glow border"
                                style={{ height: "220px" }}
                            >
                                <div className="placeholder w-100 h-100 rounded-4"></div>
                            </div>
                        </div>
                    ))}
                </div>
            ) : (
                <>
                    <div className="row g-3 justify-content-center">
                        {repetidas?.contenido?.length > 0 ? (
                            repetidas.contenido.map((fig) => (
                                <div
                                    key={fig.figurita_id}
                                    className="col-6 col-md-4 col-lg-3 d-flex justify-content-center"
                                >
                                    <RepetidaCard
                                      figurita={fig}
                                      onEditar={() => abrirModalEdicion(fig)}
                                    />
                                </div>
                            ))
                        ) : (
                            <div className="col-12 text-center text-muted py-5">
                                <div className="fs-1">📭</div>
                                <p className="mb-0">
                                    No hay resultados...
                                </p>
                            </div>
                        )}
                    </div>

                    <Paginacion
                        page={pagina}
                        totalPages={
                            repetidas.cantidad_de_paginas ?? 1
                        }
                        onChange={setPagina}
                    />
                </>
            )}

            {showModal && repetidaSeleccionada && (
              <EditarRepetidaModal
                figurita={repetidaSeleccionada}
                onClose={cerrarModalEdicion}
                onGuardar={guardarCambiosRepetida}
              />
            )}
        </div>
    );
};

export default Repetidas;