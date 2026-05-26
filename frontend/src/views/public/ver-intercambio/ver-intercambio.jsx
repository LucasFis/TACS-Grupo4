    import { useEffect, useState } from "react";
    import { useParams, useNavigate } from "react-router-dom";
    import Breadcrumb from "../../../components/ui/breadcrumb/breadcrumb.jsx";
    import SectionCard from "../../../components/ui/section-card/section-card.jsx";
    import SectionTitle from "../../../components/ui/section-title/section-title.jsx";
    import PerfilSimple from "../../../components/ui/perfil-simple/perfil-simple.jsx";
    import Button from "../../../components/ui/button/button.jsx";

    import {
        obtenerPropuesta,
        aceptarPropuesta,
        rechazarPropuesta,
        cancelarPropuesta,
    } from "../../../services/propuestasService.js";

    import FiguritaCard from "./figurita-card.jsx";
    import OfertaCard from "./oferta-card.jsx";

    import styles from "./ver-intercambio.module.css";

    const VerIntercambio = () => {

        const { intercambioId } = useParams();
        const navigate = useNavigate();
        const [cargando, setCargando] = useState(true);
        const [propuesta, setPropuesta] = useState(null);

        const cargarIntercambio = async () => {
            try {
                setCargando(true);
                const data = await obtenerPropuesta(intercambioId);
                setPropuesta(data);
            } catch (e) {
                console.error(e);
            } finally {
                setCargando(false);
            }
        };

        useEffect(() => {
            cargarIntercambio();
        }, [intercambioId]);

        const handleAceptar = async () => {
            try {
                await aceptarPropuesta(propuesta.id);

                setPropuesta((prev) => ({
                    ...prev,
                    estado: "ACEPTADO"
                }));
            } catch (e) {
                console.error(e);
            }
        };

        const handleRechazar = async () => {
            try {
                await rechazarPropuesta(propuesta.id);

                setPropuesta((prev) => ({
                    ...prev,
                    estado: "RECHAZADO"
                }));
            } catch (e) {
                console.error(e);
            }
        };

        const handleCancelar = async () => {
            try {
                await cancelarPropuesta(propuesta.id);
                setPropuesta((prev) => ({
                    ...prev,
                    estado: "CANCELADO"
                }));
            } catch (e) {
                console.error(e);
            }
        };

        if (cargando) {
            return (
                <div className="container py-4">
                    <h3>Cargando intercambio...</h3>
                </div>
            );
        }

        if (!propuesta) {
            return (
                <div className="container py-4">
                    <h3>No se encontró el intercambio</h3>
                </div>
            );
        }

        const esRecibida = propuesta.tipo === "RECIBIDA";
        const esEnviada = propuesta.tipo === "ENVIADA";

        //Para que no aparezcan los botones, si ya fue ceptada o rechazada.
        const estadoActual = propuesta?.estado;
        const estaPendiente = estadoActual === "PENDIENTE" && esRecibida;
        const puedeCancelar = estadoActual === "PENDIENTE" && esEnviada;

        return (
            <div className="container py-4 px-3 px-md-4">

                <Breadcrumb
                    crumbs={[
                        { name: "Intercambios", to: "/intercambios" },
                        { name: `#${propuesta.id}`, to: `/intercambios/${propuesta.id}` },
                    ]}
                />

                <SectionCard>

                    <SectionTitle>
                        ESTADO DEL INTERCAMBIO
                    </SectionTitle>

                    <SectionCard.Section>

                        <div className="d-flex justify-content-between align-items-center">

                            <div>
                                <small className="text-muted">
                                    Estado actual
                                </small>

                                <h4 className="mb-0">
                                    {propuesta.estado ?? "PENDIENTE"}
                                </h4>
                            </div>

                            {/* Botones de cambio de estado */}
                            {
                                estaPendiente && (
                                    <div>
                                        <Button
                                            label={"Aceptar"}
                                            className={"me-2"}
                                            onClick={handleAceptar}
                                        />

                                        <Button
                                            label={"Rechazar"}
                                            onClick={handleRechazar}
                                        />
                                    </div>
                                )
                            }

                            {puedeCancelar && (
                                <Button
                                    label={"Cancelar"}
                                    onClick={handleCancelar}
                                />
                            )}

                        </div>

                    </SectionCard.Section>

                </SectionCard>

                <SectionCard>

                    <SectionTitle>
                        USUARIO
                    </SectionTitle>

                    <SectionCard.Section>
                        <PerfilSimple perfil={ esRecibida ? propuesta.autor : propuesta.destinatario}/>
                    </SectionCard.Section>

                </SectionCard>

                <SectionCard>
                    <SectionTitle>
                        {esRecibida ? "FIGURITA QUE VOS ENTREGÁS" : "FIGURITA QUE SOLICITÁS"}
                    </SectionTitle>
                    <SectionCard.Section>
                        <FiguritaCard figurita={propuesta.figurita_buscada} />
                    </SectionCard.Section>
                </SectionCard>

                <SectionCard>
                    <SectionTitle>
                        {`${esRecibida ? "FIGURITAS QUE VOS RECIBÍS" : "FIGURITAS QUE OFRECÉS"} (${propuesta.figuritas_ofrecidas.length})`}
                    </SectionTitle>
                    <SectionCard.Section>
                        <div className="d-flex flex-column gap-3">
                            {propuesta.figuritas_ofrecidas.map((fig) => (
                                <FiguritaCard key={fig.id} figurita={fig} />
                            ))}
                        </div>
                    </SectionCard.Section>
                </SectionCard>

                <SectionCard>

                    <SectionTitle>
                        RESUMEN DE LA PROPUESTA
                    </SectionTitle>

                    <SectionCard.Section>

                        <OfertaCard propuesta={propuesta} tipo={propuesta.tipo} />

                    </SectionCard.Section>

                </SectionCard>

            </div>
        );
    };

    export default VerIntercambio;