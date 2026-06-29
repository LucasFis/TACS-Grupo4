const PaqueteVacio = ({ className }) => {
  return (
    <svg
      viewBox="0 0 200 280"
      xmlns="http://www.w3.org/2000/svg"
      className={className}
      aria-hidden="true"
    >
      {/* Sombra */}
      <ellipse cx="100" cy="268" rx="60" ry="8" fill="#00000018" />

      {/* Cuerpo del paquete */}
      <rect x="30" y="20" width="140" height="240" rx="10" ry="10" fill="#175a2d" />

      {/* Franja dorada superior */}
      <rect x="30" y="20" width="140" height="38" rx="10" ry="10" fill="#d49a2c" />
      <rect x="30" y="42" width="140" height="16" fill="#d49a2c" />

      {/* Franja dorada inferior */}
      <rect x="30" y="222" width="140" height="38" rx="10" ry="10" fill="#d49a2c" />
      <rect x="30" y="222" width="140" height="16" fill="#d49a2c" />

      {/* Texto "MUNDIAL" en la franja superior */}
      <text
        x="100"
        y="37"
        textAnchor="middle"
        fontFamily="Inter, sans-serif"
        fontWeight="800"
        fontSize="11"
        letterSpacing="3"
        fill="#175a2d"
      >
        MUNDIAL
      </text>

      {/* Área interior del sobre */}
      <rect x="46" y="72" width="108" height="140" rx="6" fill="#0e3d1e" />

      {/* Marco de figurita vacía */}
      <rect x="58" y="86" width="84" height="108" rx="4" fill="#0a2d16" strokeDasharray="4 3" stroke="#b1f5c7" strokeWidth="1.2" />

      {/* Signo de pregunta */}
      <text
        x="100"
        y="155"
        textAnchor="middle"
        fontFamily="Inter, sans-serif"
        fontWeight="900"
        fontSize="54"
        fill="#175a2d"
        opacity="0.5"
      >
        ?
      </text>

      {/* Estrellas decorativas */}
      <text x="46" y="68" fontSize="13" fill="#d49a2c" fontFamily="sans-serif">★</text>
      <text x="136" y="68" fontSize="13" fill="#d49a2c" fontFamily="sans-serif">★</text>

      {/* Texto inferior */}
      <text
        x="100"
        y="238"
        textAnchor="middle"
        fontFamily="Inter, sans-serif"
        fontWeight="700"
        fontSize="9"
        letterSpacing="2"
        fill="#175a2d"
      >
        PANINI · 2026
      </text>
    </svg>
  )
}

export default PaqueteVacio