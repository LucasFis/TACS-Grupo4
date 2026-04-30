import {api, handleAxiosError } from "./api.js";

const COLECCIONES_URL = "/colecciones";

export const buscarFaltantes = async (colId) => {
    try {
        const { data } = await api.get(
            `${COLECCIONES_URL}/${colId}/faltantes`,
            {}
        );
        return data;
    } catch (error) {
        handleAxiosError(error);
    }
};

export const buscarRepetidas = async (colId, filtros) => {
    try {
        const { data } = await api.get(
            `${COLECCIONES_URL}/${colId}/repetidas`,
            {}
        );

        return data;
    } catch (error) {
        handleAxiosError(error);
    }
};