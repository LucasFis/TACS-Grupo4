export const resolverTipo = (metodos = []) => {
  if (!Array.isArray(metodos)) return 'intercambio'
  if (metodos.includes('INTERCAMBIO') && metodos.includes('SUBASTA')) return 'ambos'
  return metodos.includes('SUBASTA') ? 'subasta' : 'intercambio'
}
