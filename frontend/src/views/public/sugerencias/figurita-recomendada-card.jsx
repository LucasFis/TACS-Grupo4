import styles from "./sugerencia-card.module.css";

const FiguritaRecomendadaCard = ({ fig, verde = true }) => {
    const initials = fig.jugador?.split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase()

    return (
        <div className={(verde ? styles.itemGreen : styles.itemBlue) + " d-flex align-items-center justify-content-between gap-2"}>
            <div className="d-flex align-items-center gap-2">
                <div className={styles.avatarCirculo}>
                    {fig.imagen_url
                        ? <img src={fig.imagen_url} alt={fig.jugador} className={styles.avatarImg} />
                        : <span className={styles.avatarIniciales}>{initials}</span>
                    }
                </div>
                <div>
                    <div className={styles.jugadorNombre}>{fig.jugador}</div>
                    {fig.numero && <div className={styles.jugadorNumero}>#{fig.numero}</div>}
                </div>
            </div>
            <span className={styles.seleccion}>{fig.seleccion}</span>
        </div>
    )
}

export default FiguritaRecomendadaCard
