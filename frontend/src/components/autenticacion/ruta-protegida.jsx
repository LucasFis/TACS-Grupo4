import {Outlet, useLocation} from "react-router-dom";
import {Navigate} from "react-router";
import {useAuth} from "@/contexts/userContext.jsx";

const RutaProtegida = () => {
    const { user } = useAuth();
    const location = useLocation();

    if (!user) return <Navigate to="/acceso-denegado" replace />;

    if (user.rol === "ADMINISTRADOR" && !location.pathname.startsWith("/perfil"))
        return <Navigate to="/acceso-denegado" replace />;

    return <Outlet />;
}

export default RutaProtegida;