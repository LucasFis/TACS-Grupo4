import { IconoAdvertencia } from '@/components/ui/iconos/advertencia/advertencia.jsx'
import { IconoVerificado } from '@/components/ui/iconos/verificado/verificado.jsx'
import styles from './input-texto.module.css'

/**
 * @param {string}      name
 * @param {string}      label
 * @param {string}      value
 * @param {function}    onChange
 * @param {function}    onBlur
 * @param {boolean}     tocado
 * @param {boolean}     esValido
 * @param {string|null} error
 * @param {string}      placeholder
 * @param {string}      type       - 'text' | 'email' | etc. (default: 'text')
 * @param {string}      hint
 */
const TextoInput = ({
  name,
  label,
  value,
  onChange,
  onBlur,
  tocado,
  esValido,
  error,
  placeholder,
  type = 'text',
  hint,
}) => {
  const tieneError = tocado && error
  const tieneExito = tocado && esValido

  return (
    <div className="mb-3">
      <label className="form-label">{label}</label>

      <div className="input-group">
        <input
          type={type}
          name={name}
          className={`form-control ${
            tieneError ? styles.inputInvalido : tieneExito ? styles.inputValido : ''
          }`}
          placeholder={placeholder}
          value={value}
          onChange={onChange}
          onBlur={onBlur}
          style={{
            borderColor: !tieneError && !tieneExito ? 'var(--border-color-dark)' : undefined,
          }}
        />

        {tieneExito && (
          <span
            className="input-group-text"
            style={{ backgroundColor: 'transparent', borderColor: 'var(--color-success)' }}
          >
            <IconoVerificado />
          </span>
        )}
      </div>

      {tieneError && (
        <small className={`form-text d-flex align-items-center gap-1 mt-1 ${styles.textoInvalido}`}>
          <IconoAdvertencia /> {error}
        </small>
      )}

      {hint && (
        <small className={`form-text d-block mt-1 ${styles.textoNeutro}`}>{hint}</small>
      )}
    </div>
  )
}

export default TextoInput