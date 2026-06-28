package app.model.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Document(collection = "sugerencias")
@Builder
public class Sugerencia {
  @Id
  private String id;
  @DBRef
  Perfil sugerido;
  @DBRef
  Perfil autor;
  @DBRef
  List<Figurita> figuritasSugeridas;
  @DBRef
  List<Figurita> figuritasNecesarias;
  @Builder.Default
  Boolean favorito = false;
}
