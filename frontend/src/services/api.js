import axios from "axios";
import {logout} from "@/services/sesionService.js";
import {useAuth} from "@/contexts/userContext.jsx";

const api = axios.create({
    baseURL: import.meta.env.VITE_BACKEND_URI || "/api",
    timeout: 40000,
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

//Lo que hace esto, es que cuando se detecta que el JWT dejo de ser invalido, lanza un evento para
//setear en undefined al user.
api.interceptors.request.use(config => {
    console.log(`[API] ${config.method?.toUpperCase()} ${config.baseURL}${config.url}`);
    return config;
});

api.interceptors.response.use(
    response => {
        console.log(`[API] ${response.status} ${response.config.url}`);
        return response;
    },

    async error => {
        const status = error.response?.status;
        const url = error.config?.url;
        console.error(`[API] ERROR ${status ?? "network"} ${url}`, error.message);

        if (status === 401) {

            try {
                await logout();
            } finally {
                window.dispatchEvent(
                    new Event("logout")
                );
            }
        }

        return Promise.reject(error);
    }
);


export {api, handleAxiosError};