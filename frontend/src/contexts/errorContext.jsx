import { createContext, useContext } from "react";
import {useLocation, useNavigate} from "react-router-dom";

const ErrorContext = createContext(null);

//La idea de este contexto, es que mapee los errores para que se puedan mostrar en la UI.
export const ErrorProvider = ({ children }) => {
    const navigate = useNavigate();
    const location = useLocation();

    const errorTemplate = (fields = undefined) => {

        return {
            codigo: undefined,
            mensaje: undefined,
            errors: fields
        }
    }

    const handleError = (error, setter) => {
        switch (error.type) {

            case "SERVER_DOWN":
                navigate("/servidor-caido", {
                    state: {
                        from: location.pathname
                    }
                });
              return "El servidor no responde"

            case "FORBIDDEN":
                navigate("/unauthorized");
              return "Acceso denegado"

            case "INTERNAL_SERVER_ERROR":
                navigate("/error-interno");
                return "Error interno del servidor"


            default: {
                const erroresPorCampo = error.errors && Object.keys(error.errors).length > 0
                    ? Object.entries(error.errors)
                        .map(([campo, msg]) => `${campo}: ${msg}`)
                        .join(' | ')
                    : null

                const mensajeBase = error.message || "No se pudo completar la acción"
                const mensajeFinal = erroresPorCampo
                    ? `${mensajeBase} — ${erroresPorCampo}`
                    : mensajeBase

                const errorProcesado = {
                    codigo: error.code || error.status,
                    mensaje: mensajeFinal,
                    errors: error.errors
                }

                setter(errorProcesado)
                return errorProcesado.mensaje
            }
        }
    };

    return (
        <ErrorContext.Provider value={{ handleError, errorTemplate }}>
            {children}
        </ErrorContext.Provider>
    );
};

export const useError = () => useContext(ErrorContext);