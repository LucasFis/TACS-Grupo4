package app.servicios.impl;

import app.model.entities.*;
import app.servicios.IPropuestaService;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class PropuestaService implements IPropuestaService {

    private Map<String, Propuesta> propuestas = new HashMap<>();

    public Propuesta crear(Propuesta propuesta) {
        propuestas.put(propuesta.getId(), propuesta);
        return propuesta;
    }

    public Propuesta obtenerPorId(String id) {
        Propuesta propuesta = propuestas.get(id);

        if (propuesta == null) {
            throw new RuntimeException("Propuesta no encontrada");
        }

        return propuesta;
    }

    public void aceptar(String id) {
        Propuesta propuesta = obtenerPorId(id);
        propuesta.aceptar(propuesta.getUsuarioDestino());
    }

    public void rechazar(String id) {
        Propuesta propuesta = obtenerPorId(id);
        propuesta.rechazar(propuesta.getUsuarioDestino());
    }
}