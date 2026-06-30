import { CardHero } from '@/components/ui/figurita-card/figurita-card'
import styles from '@/components/ui/figurita-card/figurita-card.module.css'

const FaltanteCard = ({ figurita }) => {
    const { numero, jugador, seleccion, imagen_url } = figurita

    return (
        <div className={styles.card}>
            <CardHero strCutout={imagen_url} jugador={jugador} numero={numero} tipo="faltante" />
            <div className={styles.body}>
                <p className={styles.cardName}>{jugador}</p>
                <div className={styles.metaRow}>
                    <p className={styles.cardSubtitle}>{seleccion}</p>
                    <span className={styles.cardExtra}>Buscando</span>
                </div>
            </div>
        </div>
    )
}

export default FaltanteCard
