import { Outlet } from "react-router-dom";
import { Navigate } from "react-router";
import { useAuth } from "@/contexts/userContext.jsx";

const RutaConRol = ({ rol, redirigirA, invertir = false }) => {
    const { user } = useAuth();
    const tieneRol = user?.rol === rol;
    const permitir = invertir ? !tieneRol : tieneRol;
    return permitir ? <Outlet /> : <Navigate to={redirigirA} replace />;
};

export default RutaConRol;
