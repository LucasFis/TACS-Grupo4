import Etiqueta from '../../../../../components/ui/etiqueta/etiqueta.jsx'
import PerfilSimple from '@/components/ui/perfil-simple/perfil-simple.jsx'

const VARIANTE_ESTADO = {
  PENDIENTE: 'advertencia',
  ACEPTADO: 'exito',
  RECHAZADO: 'peligro',
  CANCELADO: 'apagado',
}

const LABEL_ESTADO = {
  PENDIENTE: 'Pendiente',
  ACEPTADO: 'Aceptado',
  RECHAZADO: 'Rechazado',
  CANCELADO: 'Cancelado',
}

const HeaderUsuarioEstado = ({ destinatario, estado }) => (
  <div className="d-flex justify-content-between align-items-center">
    <PerfilSimple perfil={destinatario} />
    {estado && (
      <Etiqueta
        label={LABEL_ESTADO[estado] ?? estado}
        variante={VARIANTE_ESTADO[estado] ?? 'secundario'}
      />
    )}
  </div>
)

export default HeaderUsuarioEstado
