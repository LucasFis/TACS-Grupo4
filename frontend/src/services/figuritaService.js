import {api, handleAxiosError } from "./api.js";

const FIGURITAS_URL = "/figuritas";

export const buscarFiguritas = async () => {
    try {
        const { data } = await api.post(
            `${FIGURITAS_URL}/figuritas`,
            {}
        );
        return data;
    } catch (error) {
        handleAxiosError(error);
    }
};







