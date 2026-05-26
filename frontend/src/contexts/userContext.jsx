import {createContext, useContext, useEffect, useState} from "react";
import {buscarUsuario, logout} from "../services/sesionService.js";
import {useNavigate} from "react-router-dom";

const AuthContext = createContext(null)

export const AuthProvider = ({ children }) => {

    const [user, setUser] = useState(() => {
        const storedUser = localStorage.getItem("sesion");
        return storedUser ? JSON.parse(storedUser) : undefined;
    });

    const tieneSesion =  user !== undefined

    const navigate = useNavigate();

    const asignarUsuario = (user, reload = true) => {
        setUser(user)

        localStorage.setItem("sesion", JSON.stringify(user))
    }

    const verificarSesion = async () => {
        await buscarUsuario((obj) => asignarUsuario (obj, false))
    }

    useEffect(() => {
        if (user == null) return;
        verificarSesion()
    }, [])

    //Este useEffect es el que escucha cuando el interceptor de axios lanza el evento de logout.
    useEffect(() => {

        const listener = () => {
            setUser(undefined);
            localStorage.removeItem("sesion");
            logout().catch(() => {}); // borra el cookie HttpOnly en el servidor; si falla (sesión ya muerta) se ignora
            navigate("/")
        };

        window.addEventListener(
            "logout",
            listener
        );

        return () =>
            window.removeEventListener(
                "logout",
                listener
            );

    }, []);

    const updateUser = (fields) => {
        const updatedUser = {...user, ...fields};

        localStorage.setItem("sesion", JSON.stringify(updatedUser))

        setUser(updatedUser)
    }

    const cerrarSesion = async () => {
        setUser(undefined)
        localStorage.removeItem("sesion")

        await logout()

        navigate("/")
    }

    const returnValue = {
        user,
        tieneSesion,
        asignarUsuario,
        cerrarSesion,
    }

    return (
        <AuthContext.Provider value={returnValue}>
            {children}
        </AuthContext.Provider>
    )
}

export const useAuth = () => useContext(AuthContext);