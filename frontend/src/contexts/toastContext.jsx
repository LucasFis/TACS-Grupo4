import { createContext, useContext, useState, useCallback } from "react";
import Toast from "../components/ui/toast/toast.jsx";

const ToastContext = createContext();

export const ToastProvider = ({ children }) => {
    const [toasts, setToasts] = useState([]);

    const showToast = useCallback((message, type = "success") => {
        const id = Date.now();
        const texto = message || (type === "error" ? "Ocurrió un error inesperado" : "Operación completada");
        setToasts(prev => [...prev, { id, message: texto, type }]);
    }, []);

    const removeToast = (id) => {
        setToasts(prev => prev.filter(t => t.id !== id));
    };

    return (
        <ToastContext.Provider value={{ showToast }}>
            {children}

            <div className="toast-container">
                {toasts.map(t => (
                    <Toast
                        key={t.id}
                        message={t.message}
                        type={t.type}
                        onClose={() => removeToast(t.id)}
                    />
                ))}
            </div>
        </ToastContext.Provider>
    );
};

export const useToast = () => useContext(ToastContext);