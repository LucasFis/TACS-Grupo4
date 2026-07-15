import { api, handleAxiosError } from "@/services/api.js";

export const obtenerNotificaciones = async (filtros = {}, signal) => {
    try {
        const filtrosLimpios = Object.fromEntries(
            Object.entries(filtros).filter(([_, value]) => value !== "" && value != null)
        );
        const { data } = await api.get("/perfil/notificaciones", { params: filtrosLimpios, signal });
        return data;
    } catch (error) {
        handleAxiosError(error);
    }
};

export const marcarTodasLeidas = async () => {
    try {
        await api.patch("/perfil/notificaciones/leidas");
    } catch (error) {
        handleAxiosError(error);
    }
};
