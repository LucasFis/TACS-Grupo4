package app.servicios;

import app.model.entities.Propuesta;

public interface IPropuestaService {
    public Propuesta crear(Propuesta propuesta);
    public Propuesta obtenerPorId(String id);
    public void aceptar(String id);
    public void rechazar(String id);
}
