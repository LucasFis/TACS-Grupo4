import { api, handleAxiosError } from "./api.js";

const PROPUESTAS_URL = "/propuestas";

export const buscarPropuestas = async (filtros) => {
    try {
        const filtrosLimpios = Object.fromEntries(
            Object.entries(filtros)
                .filter(([_, value]) => value !== "" && value != null)
        );
        const {data} = await api.get(PROPUESTAS_URL,
            {params: {...filtrosLimpios}}
        );
        return data
    } catch (error) {
        handleAxiosError(error);
    }
};

export const obtenerPropuesta = async (prop_id) => {
    try {
        const { data } = await api.get(`${PROPUESTAS_URL}/${prop_id}`);
        return data;
    } catch (error) {
        handleAxiosError(error);
    }
};

export const cancelarPropuesta = async (prop_id) => {
    try {
        const {data} = await api.patch(PROPUESTAS_URL + `/${prop_id}/cancelar`);
        return data
    } catch (error) {
        handleAxiosError(error);
    }
}

export const verificarConflictos = async (prop_id) => {
    try {
        const { data } = await api.get(`${PROPUESTAS_URL}/${prop_id}/conflictos`);
        return data;
    } catch (error) {
        handleAxiosError(error);
    }
};

export const aceptarPropuesta = async (prop_id) => {
    try {
        const {data} = await api.patch(PROPUESTAS_URL + `/${prop_id}/aceptar`);
        return data
    } catch (error) {
        handleAxiosError(error);
    }
}

export const rechazarPropuesta = async (prop_id) => {
    try {
        const {data} = await api.patch(PROPUESTAS_URL + `/${prop_id}/rechazar`);
        return data
    } catch (error) {
        handleAxiosError(error);
    }
}

export const crearPropuesta = async (destinatarioId, figuritaBuscadaId, figuritasOfrecidasIds) => {
    try {
        const { data } = await api.post(PROPUESTAS_URL, {
            destinatario_id: destinatarioId,
            figurita_buscada_id: figuritaBuscadaId,
            figuritas_ofrecidas_ids: figuritasOfrecidasIds,
        });
        return data;
    } catch (error) {
        handleAxiosError(error);
    }
};