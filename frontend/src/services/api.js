import axios from "axios";
import {logout} from "@/services/sesionService.js";
import {useAuth} from "@/contexts/userContext.jsx";

const api = axios.create({
    baseURL: import.meta.env.VITE_BACKEND_URI || 'http://localhost:8080',
    timeout: 30000,
    headers: {
        "Content-Type": "application/json",
    },
    withCredentials: true,
    paramsSerializer: { indexes: null },
});

export const ping = async () => {
    try {
        await api.get("/ping")
    } catch (error) {
        handleAxiosError(error);
    }
};

//Esta funcion, lo que hace es mapear el error de axios a un error de nuestro "dominio".
const handleAxiosError = (error) => {

    if (error.code === "ERR_NETWORK") {
        throw {
            type: "SERVER_DOWN",
            message: "Servidor no disponible",
        };
    }

    if (error.response) {
        const { status, data } = error.response;

        switch (status) { //Errores que deben quitarte de la pagina
            case 401:
                throw { type: "UNAUTHORIZED", message: "No se encontro una sesión" };

            case 403:
                throw {type: "FORBIDDEN", message: "Accion no permitida"}

            case 500:
                throw {
                    type: "INTERNAL_SERVER_ERROR",
                    message: "Ocurrió un error interno"
                };
        }

        throw { //Errores que deben mostrar el mensaje en la misma pagina
            type: "API_ERROR",
            status,
            message: data.message || "Error del servidor",
            errors: data.errors || {}
        };
    }

    if (error.request) {
        throw {
            type: "SERVER_DOWN",
            message: "Servidor no disponible",
        };
    }

    throw {
        type: "UNKNOWN",
        message: "Error inesperado",
    };
};


// Cuando el JWT expira/es inválido, dispara el evento "logout" una sola vez.
// La flag evita el loop: 401 → logout → navigate → más 401 → loop.
let disparandoLogout = false

api.interceptors.response.use(
  (response) => response,

  (error) => {
    if (error.response?.status === 401 && !disparandoLogout) {
      disparandoLogout = true
      window.dispatchEvent(new Event('logout'))
      setTimeout(() => {
        disparandoLogout = false
      }, 2000)
    }

    return Promise.reject(error)
  },
)


export {api, handleAxiosError};