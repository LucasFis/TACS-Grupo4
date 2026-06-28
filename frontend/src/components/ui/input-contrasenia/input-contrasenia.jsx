import { useState } from 'react'
import { IconoOjoTachado } from '@/components/ui/iconos/ojo-tachado/ojo-tachado.jsx'
import { IconoOjo } from '@/components/ui/iconos/ojo/ojo.jsx'
import { IconoAdvertencia } from '@/components/ui/iconos/advertencia/advertencia.jsx'
import { IconoVerificado } from '@/components/ui/iconos/verificado/verificado.jsx'
import styles from './input-contrasenia.module.css'

/**
 * @param {string}   name        - Nombre del campo (para el input)
 * @param {string}   label       - Etiqueta visible
 * @param {string}   value       - Valor controlado
 * @param {function} onChange    - Handler del padre
 * @param {function} onBlur     - Handler del padre
 * @param {boolean}  tocado      - Si el campo fue tocado (para mostrar errores)
 * @param {boolean}  esValido    - Si el valor cumple todas las validaciones
 * @param {string|null} error    - Mensaje de error (null si no hay)
 * @param {string}   placeholder
 * @param {string}   hint        - Texto de ayuda debajo del campo (opcional)
 */
const ContraseniaInput = ({
  name,
  label,
  value,
  onChange,
  onBlur,
  tocado,
  esValido,
  error,
  placeholder = '********',
  hint,
}) =>{
  const [mostrar, setMostrar] = useState(false)

  const tieneError = tocado && error
  const tieneExito = tocado && esValido

  return (
    <div className="mb-3">
      <label className="form-label">{label}</label>

      <div className="input-group">
        <input
          type={mostrar ? 'text' : 'password'}
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

        <button
          type="button"
          className="btn"
          onClick={() => setMostrar((prev) => !prev)}
          style={{ borderColor: 'var(--border-color-dark)', color: 'var(--color-subtitle)' }}
          aria-label={mostrar ? 'Ocultar contraseña' : 'Mostrar contraseña'}
          tabIndex={-1}
        >
          {mostrar ? <IconoOjoTachado /> : <IconoOjo />}
        </button>
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

export default ContraseniaInput