import {Outlet} from "react-router-dom";
import {Navigate} from "react-router";
import {useAuth} from "@/contexts/userContext.jsx";

const RutaNoAdministrador = () => {
    const { user } = useAuth();

    return user?.rol === "ADMINISTRADOR"
        ? <Navigate to="/estadisticas" replace />
        : <Outlet />;
}

export default RutaNoAdministrador;
