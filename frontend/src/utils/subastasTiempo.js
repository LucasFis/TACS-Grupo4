export function calcularTiempoRestante(fechaCierre) {
  const diff = new Date(fechaCierre) - new Date();
  if (diff <= 0) return null;

  const totalMin = Math.floor(diff / 60000);
  const horas = Math.floor(totalMin / 60);
  const minutos = totalMin % 60;

  if (horas >= 24) {
    const dias = Math.floor(horas / 24);
    return `${dias}d ${horas % 24}h`;
  }
  return `${horas}h ${minutos}m`;
}

export function calcularFinalizadaHace(fechaCierre) {
  const diff = new Date() - new Date(fechaCierre)

  const segundos = Math.floor(diff / 1000)
  const minutos = Math.floor(segundos / 60)
  const horas = Math.floor(minutos / 60)
  const dias = Math.floor(horas / 24)

  if (dias > 0) return `${dias}d`
  if (horas > 0) return `${horas}h`
  if (minutos > 0) return `${minutos}m`

  return 'Ahora'
}

// umbral: finaliza en menos de 2 horas
export function calcularFinalizaPronto(fechaCierre, umbralHoras = 2) {
  const diff = new Date(fechaCierre) - new Date();
  return diff > 0 && diff < umbralHoras * 3600000;
}

// Devuelve todos los derivados de tiempo a partir de las fechas crudas
export function derivarTiempo({ fecha_cierre }) {
  const finalizada = new Date(fecha_cierre) <= new Date();
  return {
    finalizada,
    tiempoRestante: finalizada ? null : calcularTiempoRestante(fecha_cierre),
    finalizadaHace: finalizada ? calcularFinalizadaHace(fecha_cierre) : null,
    finalizaPronto: finalizada ? false : calcularFinalizaPronto(fecha_cierre),
  };
}