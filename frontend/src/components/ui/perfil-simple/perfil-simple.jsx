import styles from './perfil-simple.module.css'
import Estrellas from '../../../components/ui/estrellas/estrellas.jsx'
import { useNavigate } from 'react-router'

const PerfilSimple = ({ perfil }) => {
  const navigate = useNavigate()

  return (
    <div
      className={styles.avatarContainer + ' d-flex align-items-center gap-2'}
      onClick={() => navigate(`/perfil/${perfil.id}`)}
    >
      <div className={styles.avatar}>{perfil.iniciales || '?'}</div>
      <div>
        <h5 className="m-0">{perfil.nombre || 'Usuario'}</h5>
        <div className="fs-6 d-flex align-items-center gap-2">
          <Estrellas calificacion={perfil.calificacion_media ?? 0} mostrarNumero={true} />
        </div>
      </div>
    </div>
  )
}

export default PerfilSimple
