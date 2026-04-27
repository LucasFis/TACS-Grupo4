package app.model.entities;

public enum MetodoIntercambio {
  SUBASTA,
  INTERCAMBIO,
  SUBASTA_E_INTERCAMBIO;

  public static MetodoIntercambio fromString(String value) {
    for (MetodoIntercambio metodo : MetodoIntercambio.values()) {
      if (metodo.name().equalsIgnoreCase(value)) {
        return metodo;
      }
    }
    throw new IllegalArgumentException("Valor inválido: " + value);
  }
}
