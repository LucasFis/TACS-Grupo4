package app.servicios;

import app.dto.OperacionesDto;

public interface UsuarioService {
    OperacionesDto getOperacionesUsuario(String userId);
}
