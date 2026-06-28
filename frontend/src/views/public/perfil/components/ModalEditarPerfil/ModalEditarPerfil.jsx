import { useEffect, useRef } from 'react'
import Estrellas from '@/components/ui/estrellas/estrellas.jsx'
import { useEditarPerfil } from '../../hooks/useEditarPerfil.js'
import ColumnaDatosPerfil from './ColumnaDatosPerfil.jsx'
import ColumnaContrasenia from './ColumnaContrasenia.jsx'
import styles from './ModalEditarPerfil.module.css'

const ModalEditarPerfil = ({ perfil, reviews, promedio, onGuardar, onCerrar }) => {
  const columnaContraseniaRef = useRef(null)

  const {
    nombreEditando,
    setNombreEditando,
    nombreUsuarioEditando,
    setNombreUsuarioEditando,
    contraseniaActual,
    setContraseniaActual,
    contraseniaNueva,
    setContraseniaNueva,
    mediosEditando,
    setMediosEditando,
    guardarCambios,
    guardado,
  } = useEditarPerfil(perfil, columnaContraseniaRef)

  useEffect(() => {
    if (guardado) onGuardar(guardado)
  }, [guardado])

  return (
    <div className={styles.overlay} onClick={onCerrar}>
      <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
        <div className={styles.cabecera}>
          <h4 className={styles.titulo}>Editar perfil</h4>
          <p className={styles.subtitulo}>Actualizá tus datos y medios de contacto</p>
          <button className={styles['btn-cerrar']} onClick={onCerrar}>✕</button>
        </div>

        <div className={styles.cuerpo}>
          <div className={styles.preview}>
            <div className={styles['preview-avatar']}>{perfil.iniciales ?? '?'}</div>
            <div>
              <div className={styles['preview-nombre']}>{perfil.nombre}</div>
              <div className={styles['preview-usuario']}>@{perfil.nombre_usuario}</div>
              <div className={styles['preview-rating']}>
                <Estrellas calificacion={perfil.calificacion_media} />
                <span className={styles['preview-rating-count']}>
                  {promedio} ({reviews.cantidad_de_elementos ?? 0})
                </span>
              </div>
            </div>
          </div>

          <div className="row g-4 g-lg-5">
            <ColumnaDatosPerfil
              nombreEditando={nombreEditando}
              setNombreEditando={setNombreEditando}
              mediosEditando={mediosEditando}
              setMediosEditando={setMediosEditando}
            />
            <ColumnaContrasenia
              ref={columnaContraseniaRef}
              nombreUsuarioEditando={nombreUsuarioEditando}
              setNombreUsuarioEditando={setNombreUsuarioEditando}
              contraseniaActual={contraseniaActual}
              setContraseniaActual={setContraseniaActual}
              contraseniaNueva={contraseniaNueva}
              setContraseniaNueva={setContraseniaNueva}
              nombreUsuarioOriginal={perfil.nombre_usuario}
            />
          </div>
        </div>

        <div className={styles.footer}>
          <div className={`d-flex align-items-center gap-2 ${styles['footer-info']}`}>
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="#175A2D" viewBox="0 0 16 16">
              <path d="M5.338 1.59a61.44 61.44 0 0 0-2.837.856.481.481 0 0 0-.328.39c-.554 4.157.726 7.19 2.253 9.188a10.725 10.725 0 0 0 2.287 2.233c.346.244.652.42.893.533.12.057.218.095.293.118a.55.55 0 0 0 .101.025.615.615 0 0 0 .1-.025c.076-.023.174-.061.294-.118.24-.113.547-.29.893-.533a10.726 10.726 0 0 0 2.287-2.233c1.527-1.997 2.807-5.031 2.253-9.188a.48.48 0 0 0-.328-.39c-.651-.213-1.75-.56-2.837-.856C9.552 1.29 8.531 1.067 8 1.067c-.53 0-1.552.223-2.662.524zM5.072.56C6.157.265 7.31 0 8 0s1.843.265 2.928.56c1.11.3 2.229.655 2.887.87a1.54 1.54 0 0 1 1.044 1.262c.596 4.477-.787 7.795-2.465 9.99a11.775 11.775 0 0 1-2.517 2.453 7.159 7.159 0 0 1-1.048.625c-.28.132-.581.24-.829.24s-.548-.108-.829-.24a7.158 7.158 0 0 1-1.048-.625 11.777 11.777 0 0 1-2.517-2.453C1.928 10.487.545 7.169 1.141 2.692A1.54 1.54 0 0 1 2.185 1.43 62.456 62.456 0 0 1 5.072.56z" />
            </svg>
            Los cambios se guardan en tu perfil público
          </div>
          <div className="d-flex gap-2">
            <button className="btn btn-outline-secondary px-4" onClick={onCerrar}>Cancelar</button>
            <button className={`btn px-4 ${styles['btn-guardar']}`} onClick={guardarCambios}>
              Guardar cambios
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}

export default ModalEditarPerfil