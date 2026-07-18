import { CardHero, CARD_CLASS } from '@/components/ui/figurita-card/figurita-card'
import styles from '@/components/ui/figurita-card/figurita-card.module.css'
import { resolverTipo } from '@/utils/figuritas'

const RepetidaCard = ({ figurita, onEditar }) => {
    const { figurita_id, numero, jugador, seleccion, cantidad_existente, cantidad_reservada, metodos, imagen_url } = figurita
    const tipo = resolverTipo(metodos)
    const disponibles = cantidad_existente - cantidad_reservada
    const sinDisponibles = disponibles <= 0

    return (
        <div className={`${styles.card} ${CARD_CLASS[tipo] ?? ''}`}>
            <CardHero strCutout={imagen_url} jugador={jugador} numero={numero ?? figurita_id} tipo={tipo} />
            <div className={styles.body}>
                <p className={styles.cardName}>{jugador}</p>
                <div className={styles.metaRow}>
                    <p className={styles.cardSubtitle}>{seleccion}</p>
                    <span className={`${styles.disponibles} ${sinDisponibles ? styles.sinStockDisp : ''}`}>{disponibles} disp.</span>
                </div>
                <div className={styles.metaRow}>
                    <span style={{ fontSize: '0.72rem', color: '#9ca3af' }}>
                        Existentes: {cantidad_existente}
                    </span>
                </div>
                <div className={styles.metaRow}>
                    <span style={{ fontSize: '0.72rem', color: '#9ca3af' }}>
                        Reservadas: {cantidad_reservada}
                    </span>
                </div>
                <button className={styles.actionBtnEdit} onClick={onEditar}>
                    Editar ✏️
                </button>
            </div>
        </div>
    )
}

export default RepetidaCard
