package app.servicios;

import app.model.entities.Perfil;
import java.util.List;

public interface INotificacionService {
  public void notificarInteresados(List<Perfil> interesados, String cuerpo);
}
