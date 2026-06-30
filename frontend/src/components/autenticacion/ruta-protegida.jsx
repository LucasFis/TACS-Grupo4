import {Outlet, useLocation} from "react-router-dom";
import {Navigate} from "react-router";
import {useAuth} from "@/contexts/userContext.jsx";

const RUTAS_ADMIN = ["/perfil"];

const RutaProtegida = () => {
    const { user } = useAuth();
    const location = useLocation();

    if (!user) return <Navigate to="/acceso-denegado" replace />;

    if (user.rol === "ADMINISTRADOR" && !RUTAS_ADMIN.some(r => location.pathname === r || location.pathname.startsWith(r + '/')))
        return <Navigate to="/estadisticas" replace />;

    return <Outlet />;
}

export default RutaProtegida;