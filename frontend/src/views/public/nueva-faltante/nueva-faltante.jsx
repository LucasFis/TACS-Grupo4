import {useState} from "react";
import SectionCard from "../../../components/ui/section-card/section-card.jsx";
import AutocompleteInput from "../../../components/ui/autocomplete-input/autocomplete-input.jsx";
import Button from "../../../components/ui/button/button.jsx";
import {buscarFiguritas} from "@/services/figuritaService.js";
import {agregarFaltante} from "@/services/coleccionService.js";
import {useToast} from "@/contexts/toastContext.jsx";
import {useError} from "@/contexts/errorContext.jsx";
import Breadcrumb from "@/components/ui/breadcrumb/breadcrumb.jsx";
import { IconoAdvertencia } from '@/components/ui/iconos/advertencia/advertencia.jsx'

const NuevaFaltante = () => {
    const [figurita, setFigurita] = useState(undefined);
    const [numero, setNumero]   = useState('');
    const [jugador, setJugador] = useState('');
    const [errorFormato, setErrorFormato] = useState('');
    const [tocado, setTocado] = useState(false);

    const {showToast} = useToast();
    const {handleError} = useError();

    const handleSelect = (fig) => {
        setFigurita(fig);
        setJugador(fig?.jugador ?? '');
        setNumero(fig?.id ?? '');
    };

    const buscarPorJugador = async (texto) => {
        return await buscarFiguritas({ jugador: texto });
    };

    const buscarPorNumero = async (texto) => {
        const formatoValido = /^[A-Z]{3}-\d+$/.test(texto);

        if (!formatoValido) {
            setErrorFormato('Formato inválido. Debe ser 3 letras mayúsculas, guión y número (Ej: ARG-10)');
            setTocado(true);
            return;
        }

        setErrorFormato('');
        const resultado = await buscarFiguritas({ id: texto });

        if (resultado.length === 0) {
            showToast('No se encontró una figurita con ese número', 'error');
            handleSelect(undefined);
            return;
        }

        handleSelect(resultado[0]);
    };

    const ejecutarFormulario = async () => {
        if (!figurita) {
            showToast('Debés buscar y seleccionar una figurita primero', 'error')
            return
        }

        try {
            await agregarFaltante(figurita)
            setNumero('')
            setJugador('')
            setFigurita(undefined)
            showToast("Faltante agregada correctamente","success")
        } catch (error) {
            showToast(handleError(error, (m) => {}),'error')
        }
    }

    return (
        <div className="d-flex flex-column gap-3">
            <Breadcrumb crumbs={[
                {name: "Mis figuritas", to: "/mis-figuritas"},
                {name: "Nueva faltante", to: "/mis-figuritas/nueva-faltante"}
            ]}/>
            <div
                className="d-flex flex-column align-items-center justify-content-center rounded-3 py-4"
                style={{ backgroundColor: '#f8f9fa', border: '1.5px dashed #ced4da', minHeight: '140px' }}
            >
                {figurita ? (
                    <>
                        <p className="mb-0 fw-semibold" style={{ fontSize: '1rem' }}>
                            {figurita.jugador}
                        </p>
                        <p className="mb-0 text-muted" style={{ fontSize: '0.8rem' }}>
                            {figurita.seleccion} · #{figurita.numero}
                        </p>
                    </>
                ) : (
                    <>
                        <span style={{ fontSize: '2rem' }}>🔍</span>
                        <p className="mb-0 text-muted mt-2" style={{ fontSize: '0.85rem' }}>
                            Buscá una figurita para ver la vista previa
                        </p>
                    </>
                )}
            </div>

            <SectionCard>
                <SectionCard.Section>
                    <p className="mb-3 fw-semibold text-uppercase" style={{ fontSize: '0.75rem', letterSpacing: '0.05em' }}>
                        Buscar figurita
                    </p>

                    <div className="mb-3">
                        <label className="form-label text-muted">Número de figurita</label>
                        <div className="d-flex align-items-center gap-3">
                            <input
                                type="text"
                                className={`form-control ${errorFormato ? 'border-danger' : ''}`}
                                placeholder="Ej: ARG-10"
                                value={numero}
                                onChange={(e) => {
                                    setNumero(e.target.value.toUpperCase());
                                    setFigurita(null);
                                    setJugador('');
                                    setErrorFormato('');
                                    setTocado(false);
                                }}
                                onBlur={() => {
                                    setTocado(true);
                                    if (numero && !/^[A-Z]{3}-\d+$/.test(numero)) {
                                        setErrorFormato('Formato inválido. Debe ser 3 letras mayúsculas, guión y número (Ej: ARG-10)');
                                    }
                                }}
                            />
                            <Button label={"Buscar"} onClick={() => buscarPorNumero(numero)}/>
                        </div>
                        {tocado && errorFormato && (
                            <small className="text-danger d-flex align-items-center gap-1 mt-1" style={{ fontSize: '0.85rem' }}>
                                <IconoAdvertencia /> {errorFormato}
                            </small>
                        )}
                    </div>

                    <AutocompleteInput
                        label="Nombre del jugador"
                        placeholder="Ej: Lionel Messi"
                        value={jugador}
                        onChange={(val) => { setJugador(val); setFigurita(null); }}
                        onSelect={handleSelect}
                        onSearch={buscarPorJugador}
                        debounceMs={300}
                        getLabel={(fig) => fig.jugador}
                    />
                </SectionCard.Section>
            </SectionCard>

            <Button
                label="Publicar faltante ↗"
                onClick={() => ejecutarFormulario()}
            />

        </div>
    );
}

export default NuevaFaltante;