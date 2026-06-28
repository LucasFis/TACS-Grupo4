import { useMediosContacto } from '../../hooks/useMediosContacto.js'
import ItemMedioContacto from './ItemMedioContacto.jsx'
import styles from './columna.module.css'

const ColumnaDatosPerfil = ({
  nombreEditando,
  setNombreEditando,
  mediosEditando,
  setMediosEditando,
}) => {
  const {
    indiceMedioEditando,
    medioEditandoData,
    setMedioEditandoData,
    nuevoMedioTipo,
    setNuevoMedioTipo,
    nuevoMedioValor,
    setNuevoMedioValor,
    errorNuevoMedio,
    errorMedioEditando,
    agregarMedio,
    eliminarMedio,
    confirmarEdicionMedio,
    iniciarEdicionMedio,
    cancelarEdicionMedio,
  } = useMediosContacto(mediosEditando, setMediosEditando)

  return (
    <div className="col-12 col-md-6">
      {/* Perfil público */}
      <div className="d-flex align-items-center gap-2 mb-3">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          width="16"
          height="16"
          fill="#175A2D"
          viewBox="0 0 16 16"
        >
          <path d="M8 8a3 3 0 1 0 0-6 3 3 0 0 0 0 6Zm2-3a2 2 0 1 1-4 0 2 2 0 0 1 4 0Zm4 8c0 1-1 1-1 1H3s-1 0-1-1 1-4 6-4 6 3 6 4Zm-1-.004c-.001-.246-.154-.986-.832-1.664C11.516 10.68 10.029 10 8 10c-2.029 0-3.516.68-4.168 1.332-.678.678-.83 1.418-.832 1.664h10Z" />
        </svg>
        <span className={styles['seccion-titulo']}>Perfil público</span>
      </div>
      <p className={styles['seccion-subtitulo']}>Este nombre será visible para otros usuarios</p>
      <label className="form-label">Nombre</label>
      <input
        className="form-control mb-5"
        value={nombreEditando}
        onChange={(e) => setNombreEditando(e.target.value)}
      />

      <hr className={styles.hr} />

      {/* Medios de contacto */}
      <div className="d-flex align-items-center gap-2 mb-3">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          width="16"
          height="16"
          fill="#175A2D"
          viewBox="0 0 16 16"
        >
          <path d="M0 4a2 2 0 0 1 2-2h12a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2V4Zm2-1a1 1 0 0 0-1 1v.217l7 4.2 7-4.2V4a1 1 0 0 0-1-1H2Zm13 2.383-4.708 2.825L15 11.105V5.383Zm-.034 6.876-5.64-3.471L8 9.583l-1.326-.795-5.64 3.47A1 1 0 0 0 2 13h12a1 1 0 0 0 .966-.741ZM1 11.105l4.708-2.897L1 5.383v5.722Z" />
        </svg>
        <span className={styles['seccion-titulo']}>Medios de contacto</span>
      </div>
      <p className={styles['seccion-subtitulo']}>Gestioná cómo los demás pueden contactarte</p>

      {mediosEditando.length === 0 && (
        <p className={`text-muted mb-4 ${styles['sin-medios']}`}>Sin medios configurados</p>
      )}

      <div className="d-flex flex-column gap-4 mb-4">
        {mediosEditando.map((m, i) => (
          <div key={i} className="d-flex align-items-start gap-2">
            <ItemMedioContacto
              medio={m}
              estaEditando={indiceMedioEditando === i}
              medioEditandoData={medioEditandoData}
              setMedioEditandoData={setMedioEditandoData}
              onConfirmar={() => confirmarEdicionMedio(i)}
              onCancelar={cancelarEdicionMedio}
              onEditar={(medioData) => iniciarEdicionMedio(i, medioData)}
              onEliminar={() => eliminarMedio(i)}
              errorMedioEditando={errorMedioEditando}
            />
          </div>
        ))}
      </div>

      {/* Agregar nuevo medio */}
      <div className="row g-2">
        <div className="col-12 col-sm-auto">
          <select
            className={`form-select w-100 ${styles['select-tipo']}`}
            value={nuevoMedioTipo}
            onChange={(e) => setNuevoMedioTipo(e.target.value)}
          >
            <option value="TELEGRAM">Telegram</option>
            <option value="MAIL">Mail</option>
          </select>
        </div>
        <div className="col">
          <input
            className={`form-control w-100 ${errorNuevoMedio ? 'is-invalid' : ''}`}
            placeholder={nuevoMedioTipo === 'TELEGRAM' ? 'Ej: @usuario' : 'Ej: juan@mail.com'}
            value={nuevoMedioValor}
            onChange={(e) => setNuevoMedioValor(e.target.value)}
          />
          {errorNuevoMedio && <div className="invalid-feedback">{errorNuevoMedio}</div>}
        </div>
        <div className="col-12 col-sm-auto">
          <button className={`btn w-100 ${styles['btn-agregar']}`} onClick={agregarMedio}>
            + Agregar
          </button>
        </div>
      </div>
    </div>
  )
}

export default ColumnaDatosPerfil
