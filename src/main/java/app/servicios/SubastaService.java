package app.servicios;

import app.dto.SubastaDto;
import app.model.entities.Figurita;
import app.model.entities.Propuesta;
import app.model.entities.Subasta;

import java.time.LocalDateTime;
import java.util.List;

public interface SubastaService {
  SubastaDto crearSubasta(String userId, LocalDateTime fechaInicio, LocalDateTime fechaFin,
                          String figuritaSubastadaId, Propuesta propuestaGanadora);

  SubastaDto ofertarEnSubasta(String userId, String usuarioDestinoId,
                               String subastaId, List<Object> rawFiguritasId);
}
