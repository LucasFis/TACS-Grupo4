package app.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Document(collection = "calificaciones")
@CompoundIndex(name = "calificacion_destinatario", def = "{'destinatario.$id': 1}")
@CompoundIndex(name = "calificacion_autor_tipo_tx", def = "{'autor.$id': 1, 'tipoTransaccion': 1, 'transaccionId': 1}")
public class Calificacion {
  @Id
  private String id;
  @JsonIgnore
  @DBRef
  private Perfil autor;

  @DBRef
  private Perfil destinatario;
  //valor es un entero de 1 a 5
  private Integer valor;
  private String descripcion;
  //id subasta o id propuesta de intercambio
  private String transaccionId;
  private MetodoIntercambio tipoTransaccion;
}
