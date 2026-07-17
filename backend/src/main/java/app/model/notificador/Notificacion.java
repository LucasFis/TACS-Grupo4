package app.model.notificador;

import app.model.entities.Perfil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@Getter
@Setter
@Document(collection = "notificaciones")
public class Notificacion {
  @Id
  String id;
  Mensaje mensaje;
  @DBRef
  Perfil perfil;
  boolean leida = false;
  String link;

  // Constructor sin link para notificaciones genéricas
  public Notificacion(Mensaje mensaje, Perfil perfil) {
      this.mensaje = mensaje;
      this.perfil = perfil;
      this.leida = false;
  }

  public Notificacion(Mensaje mensaje, Perfil perfil, String link) {
      this.mensaje = mensaje;
      this.perfil = perfil;
      this.link = link;
      this.leida = false;
  }

  public void marcarLeida() {
      this.leida = true;
  }
}
