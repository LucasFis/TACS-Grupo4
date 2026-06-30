import PerfilSimple from '../../../components/ui/perfil-simple/perfil-simple.jsx'
import FiguritaChip from '@/components/ui/figurita-chip/figurita-chip.jsx'
import styles from './oferta-card.module.css'

const OfertaCard = ({ propuesta, tipo = 'RECIBIDA' }) => {
  const esRecibida = tipo === 'RECIBIDA'

  return (
    <div className={styles.card}>
      <div className="row align-items-start g-3">
        <div className="col">
          <PerfilSimple perfil={propuesta.autor} />
          <p className="label-seccion mt-2">{esRecibida ? 'Vos recibís' : 'Vos ofrecés'}</p>
          <div className="d-flex flex-column gap-1">
            {propuesta.figuritas_ofrecidas.map((fig) => (
              <FiguritaChip key={fig.id} fig={fig} variante="verde" />
            ))}
          </div>
        </div>

        <div className="col-auto d-flex align-items-center" style={{ paddingTop: '2.5rem' }}>
          <i className="ti ti-arrows-exchange" style={{ fontSize: '1.4rem', color: 'var(--color-text-secondary)' }} />
        </div>

        <div className="col">
          <PerfilSimple perfil={propuesta.destinatario} />
          <p className="label-seccion mt-2">{esRecibida ? 'Vos entregás' : 'Vos recibís'}</p>
          <div className="d-flex flex-column gap-1">
            <FiguritaChip fig={propuesta.figurita_buscada} variante="verde" />
          </div>
        </div>
      </div>
    </div>
  )
}

export default OfertaCard
