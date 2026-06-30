import { useState } from 'react'
import ConfirmModal from '@/components/ui/confirm-modal/confirm-modal.jsx'
import { usePerfil } from './hooks/usePerfil.js'
import { useCalificaciones } from './hooks/useCalificaciones.js'
import { useAuth } from '@/contexts/userContext.jsx'
import PerfilHeader from './components/PerfilHeader.jsx'
import PerfilStats from './components/PerfilStats.jsx'
import PerfilCalificaciones from './components/PerfilCalificaciones.jsx'
import ModalEditarPerfil from './components/ModalEditarPerfil/ModalEditarPerfil.jsx'
import { useParams } from 'react-router-dom'

const Perfil = () => {
  const [showModal, setShowModal] = useState(false)
  const [showConfirmModal, setShowConfirmModal] = useState(false)
  const { perfilId: perfilIdUrl } = useParams()
  const esMiPerfil = !perfilIdUrl

  const { perfil, setPerfil, loading, stats, promedio, perfilId, manejarCierreDeSesion } = usePerfil(perfilIdUrl)
  const { reviews, loading: loadingCalificaciones, pagina, setPagina } = useCalificaciones(perfilIdUrl)
  const { user } = useAuth()

  return (
    <div className="d-flex flex-column">
      <PerfilHeader
        perfil={perfil}
        loading={loading}
        promedio={promedio}
        reviews={reviews}
        perfilId={perfilId}
        esMiPerfil={esMiPerfil}
        onEditarClick={() => setShowModal(true)}
        onCerrarSesionClick={() => setShowConfirmModal(true)}
      />

      {user?.rol !== 'ADMINISTRADOR' && (
          <PerfilStats stats={stats}/>
        )
      }

      {user?.rol !== 'ADMINISTRADOR' && (
        <PerfilCalificaciones
          reviews={reviews}
          loadingNotificaciones={loadingCalificaciones}
          pagina={pagina}
          onPaginaChange={setPagina}
        />
      )}

      {esMiPerfil && showModal && (
        <ModalEditarPerfil
          perfil={perfil}
          reviews={reviews}
          promedio={promedio}
          onGuardar={(datosActualizados) => {
            setPerfil((prev) => ({ ...prev, ...datosActualizados }))
            setShowModal(false)
          }}
          onCerrar={() => setShowModal(false)}
        />
      )}
        {esMiPerfil && (
          <ConfirmModal
            show={showConfirmModal}
            titulo={'Esta seguro que quiere cerrar su sesion?'}
            labelConfirmar={'Aceptar'}
            onConfirmar={manejarCierreDeSesion}
            onCancelar={() => setShowConfirmModal(false)}
          />
        )}
    </div>
  )
}

export default Perfil
