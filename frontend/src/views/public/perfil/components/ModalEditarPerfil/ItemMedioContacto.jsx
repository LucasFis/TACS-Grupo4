import styles from './ItemMedioContacto.module.css'

const IconTelegram = () => (
  <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" fill="white" viewBox="0 0 16 16">
    <path d="M16 8A8 8 0 1 1 0 8a8 8 0 0 1 16 0zM8.287 5.906c-.778.324-2.334.994-4.666 2.01-.378.15-.577.298-.595.442-.03.243.275.339.69.47l.175.055c.408.133.958.288 1.243.294.26.006.549-.1.868-.32 2.179-1.471 3.304-2.214 3.374-2.23.05-.012.12-.026.166.016.047.041.042.12.037.141-.03.129-1.227 1.241-1.846 1.817-.193.18-.33.307-.358.336a8.154 8.154 0 0 1-.188.186c-.38.366-.664.64.015 1.088.327.216.589.393.85.571.284.194.568.387.936.629.093.06.183.125.27.187.331.236.63.448.997.414.214-.02.435-.22.547-.82.265-1.417.786-4.486.906-5.751a1.426 1.426 0 0 0-.013-.315.337.337 0 0 0-.114-.217.526.526 0 0 0-.31-.093c-.3.005-.763.166-2.984 1.09z" />
  </svg>
)

const IconMail = () => (
  <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" fill="#212529" viewBox="0 0 16 16">
    <path d="M0 4a2 2 0 0 1 2-2h12a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2V4Zm2-1a1 1 0 0 0-1 1v.217l7 4.2 7-4.2V4a1 1 0 0 0-1-1H2Zm13 2.383-4.708 2.825L15 11.105V5.383Zm-.034 6.876-5.64-3.471L8 9.583l-1.326-.795-5.64 3.47A1 1 0 0 0 2 13h12a1 1 0 0 0 .966-.741ZM1 11.105l4.708-2.897L1 5.383v5.722Z" />
  </svg>
)

const ItemMedioContacto = ({
  medio,
  estaEditando,
  medioEditandoData,
  setMedioEditandoData,
  onConfirmar,
  onCancelar,
  onEditar,
  onEliminar,
  errorMedioEditando,
}) => {
  if (estaEditando) {
    return (
      <div className="d-flex flex-column gap-1 w-100">
        <div className="d-flex align-items-center gap-2">
          <select
            className={`form-select form-select-sm ${styles['select-editar']}`}
            value={medioEditandoData.medio_comunicacion}
            onChange={(e) =>
              setMedioEditandoData((prev) => ({ ...prev, medio_comunicacion: e.target.value }))
            }
          >
            <option value="TELEGRAM">Telegram</option>
            <option value="MAIL">Mail</option>
          </select>
          <input
            className={`form-control form-control-sm ${styles['input-editar']} ${errorMedioEditando ? 'is-invalid' : ''}`}
            value={medioEditandoData.valor}
            onChange={(e) => setMedioEditandoData((prev) => ({ ...prev, valor: e.target.value }))}
          />
          <button className="btn btn-sm btn-success" onClick={onConfirmar}>
            ✓
          </button>
          <button className="btn btn-sm btn-outline-secondary" onClick={onCancelar}>
            ✕
          </button>
        </div>
        {errorMedioEditando && (
          <div className="text-danger" style={{ fontSize: '0.8rem' }}>
            {errorMedioEditando}
          </div>
        )}
      </div>
    )
  }

  const esTelegram = medio.medio_comunicacion === 'TELEGRAM'

  return (
    <>
      <div
        className={`${styles.icono} ${esTelegram ? styles['icono-telegram'] : styles['icono-mail']}`}
      >
        {esTelegram ? <IconTelegram /> : <IconMail />}
      </div>
      <div className={styles['medio-info']}>
        <div className={styles['medio-valor']}>{medio.valor}</div>
        <span className={`badge mt-1 ${styles.badge}`}>{esTelegram ? 'Telegram' : 'Mail'}</span>
      </div>
      <button
        className={`btn btn-outline-secondary ${styles['btn-accion']}`}
        onClick={() => onEditar(medio)}
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          width="15"
          height="15"
          fill="currentColor"
          viewBox="0 0 16 16"
        >
          <path d="M12.146.146a.5.5 0 0 1 .708 0l3 3a.5.5 0 0 1 0 .708l-10 10a.5.5 0 0 1-.168.11l-5 2a.5.5 0 0 1-.65-.65l2-5a.5.5 0 0 1 .11-.168l10-10zM11.207 2.5 13.5 4.793 14.793 3.5 12.5 1.207 11.207 2.5zm1.586 3L10.5 3.207 4 9.707V10h.5a.5.5 0 0 1 .5.5v.5h.5a.5.5 0 0 1 .5.5v.5h.293l6.5-6.5zm-9.761 5.175-.106.106-1.528 3.821 3.821-1.528.106-.106A.5.5 0 0 1 5 12.5V12h-.5a.5.5 0 0 1-.5-.5V11h-.5a.5.5 0 0 1-.468-.325z" />
        </svg>
      </button>
      <button className={`btn btn-outline-danger ${styles['btn-accion']}`} onClick={onEliminar}>
        <svg
          xmlns="http://www.w3.org/2000/svg"
          width="15"
          height="15"
          fill="currentColor"
          viewBox="0 0 16 16"
        >
          <path d="M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5Zm2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5Zm3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0V6Z" />
          <path d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H6a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1h3.5a1 1 0 0 1 1 1v1ZM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4H4.118ZM2.5 3h11V2h-11v1Z" />
        </svg>
      </button>
    </>
  )
}

export default ItemMedioContacto
