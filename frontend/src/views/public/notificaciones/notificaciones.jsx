import SectionTitle from "@/components/ui/section-title/section-title.jsx";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useQuery } from '@tanstack/react-query';
import { obtenerNotificaciones } from "@/services/notificacionesService.js";
import { useError } from "@/contexts/errorContext.jsx";
import { useToast } from "@/contexts/toastContext.jsx";
import Paginacion from '@/components/ui/paginacion/paginacion.jsx';
import FiltroNotificacion from './filtro-notificacion/filtro-notificacion.jsx';
import styles from './notificaciones.module.css';

const Notificaciones = () => {
  const navigate = useNavigate();
  const { handleError } = useError();
  const { showToast } = useToast();

  const [pagina, setPagina] = useState(1);
  const [leida, setLeida] = useState(null);

  const { data: notificaciones, isLoading } = useQuery({
    queryKey: ['notificaciones', { pagina, limite: 10, leida }],
    queryFn: ({ signal }) => {
      const filtros = { pagina, limite: 10 };
      if (leida !== null) filtros.leida = leida;
      return obtenerNotificaciones(filtros, signal);
    },
    onError: (error) => {
      if (error.code === 'ERR_CANCELED') return;
      showToast(handleError(error, () => {}), 'error');
    },
  })

  const cambiarFiltro = (nuevoLeida) => {
    setLeida((prev) => prev === nuevoLeida ? prev : nuevoLeida);
    setPagina(1);
  };

  const formatearFecha = (fecha) => {
    if (!fecha) return '';
    return new Date(fecha).toLocaleDateString('es-AR', {
      day: '2-digit',
      month: 'short',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const handleClickNotificacion = (n) => {
    if (n.link) {
      navigate(n.link);
    }
  };

  const contenido = notificaciones?.contenido ?? [];
  const totalPaginas = notificaciones?.cantidad_de_paginas ?? 1;

  return (
    <main className="container py-4 px-3 px-md-4">
      <div className="mx-auto" style={{ maxWidth: "900px" }}>
        <div className="d-flex flex-column gap-4">
          <SectionTitle>Notificaciones</SectionTitle>

          <FiltroNotificacion leida={leida} onChangeLeida={cambiarFiltro} />

          {isLoading ? (
            <div className="d-flex flex-column gap-3">
              {[...Array(3)].map((_, i) => (
                <div key={i} className="rounded-3 placeholder-glow border" style={{ height: '80px' }}>
                  <div className="placeholder w-100 h-100 rounded-3" />
                </div>
              ))}
            </div>
          ) : contenido.length > 0 ? (
            <>
              <div className={styles.lista}>
                {contenido.map((n) => (
                  <div
                    key={n.id}
                    className={`${styles.item} ${n.leida ? styles.leida : styles.noLeida} ${n.link ? styles.clickeable : ''}`}
                    onClick={() => handleClickNotificacion(n)}
                  >
                    <div className={styles.texto}>{n.cuerpo}</div>
                    <div className={styles.fecha}>{formatearFecha(n.fecha)}</div>
                    {n.leida && <span className={styles.leidaBadge}>leída</span>}
                  </div>
                ))}
              </div>
              <div className="pt-3 d-flex justify-content-center">
                <Paginacion
                  page={pagina}
                  totalPages={totalPaginas}
                  onChange={setPagina}
                />
              </div>
            </>
          ) : (
            <div className="text-center text-muted py-5">
              <p className="mb-0">No tenés notificaciones</p>
            </div>
          )}
        </div>
      </div>
    </main>
  );
};

export default Notificaciones;
