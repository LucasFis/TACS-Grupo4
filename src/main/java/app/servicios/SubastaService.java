package app.servicios;

import app.model.entities.Figurita;
import app.model.entities.Propuesta;
import app.model.entities.Subasta;

import java.time.LocalDateTime;
import java.util.List;

public interface SubastaService {
  Subasta crearSubasta(String userId, LocalDateTime fechaInicio, LocalDateTime fechaFin,
                              String figuritaSubastada, Propuesta propuestaGanadora);

  boolean ofertarEnSubasta(String userId, String usuarioDestinoId,
                               String subastaId, List<Object> rawFiguritasId);
}
