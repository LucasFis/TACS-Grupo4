package app.dto.request;

import app.model.entities.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class UsuarioRequest {
  @NotBlank
  @Size(min = 3)
  @Pattern(regexp = "^[a-zA-Z0-9_.]*$", message = "El nombre solo puede contener letras, números, _ o .")
  String nombre;

  @NotBlank
  @Size(min = 8)
  @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).+$", message = "La contraseña debe incluir mayúscula, minúscula, número y carácter especial")
  String contrasenia;

  @NotNull
  Rol rol;
}
