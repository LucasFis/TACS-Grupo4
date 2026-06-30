const COLORES = {
  dorado: 'var(--color-terciary)',
  exito: 'var(--color-secondary)',
  negro: 'var(--color-subtitle)',
}

const COLORES_NUMERO = {
  gris: 'text-muted',
  blanco: 'text-white',
  normal: 'text-body',
}

const Estrellas = ({
  calificacion,
  variante = 'dorado',
  mostrarNumero = false,
  decimales = 2,
  varianteNumero = 'gris',
}) => {
  const sinDatos = calificacion == null
  const valor = sinDatos ? 0 : Number(calificacion) || 0
  const fullStars = Math.floor(valor)
  const emptyStars = 5 - fullStars
  const color = COLORES[variante] ?? COLORES.dorado
  const colorNumero = COLORES_NUMERO[varianteNumero] ?? COLORES_NUMERO.gris

  return (
    <span className="d-flex align-items-center gap-1">
      <span style={{ color }}>
        {'★'.repeat(fullStars)}
        {'☆'.repeat(emptyStars)}
      </span>
      {mostrarNumero && (
        <span className={`fs-6 ${colorNumero}`}>
          {sinDatos ? '-' : valor.toFixed(decimales)}
        </span>
      )}
    </span>
  )
}

export default Estrellas
