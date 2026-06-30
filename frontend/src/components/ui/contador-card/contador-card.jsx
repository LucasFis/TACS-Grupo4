import styles from './contador-card.module.css'

const ContadorCard = ({ title, value, color }) => {
    return (
        <div className={`d-flex flex-column justify-content-between p-3 rounded-3 bg-body-secondary h-100 ${styles.card}`}>
            <div className="text-muted small fw-semibold">
                {title}
            </div>

            <div className={`fs-4 fw-bold ${color}`}>
                {value}
            </div>
        </div>
    );
};

export default ContadorCard;
