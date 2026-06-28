package app.servicios;

import app.dto.SesionDto;
import app.model.entities.Perfil;
import app.model.entities.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.time.Duration;

@Service
public class ServicioJwt {

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.expiration}")
  private Duration expiration;

  /**
   * Genera un token JWT con los claims usuarioId, rol, perfilId y colId.
   *
   * @param usuario usuario autenticado del cual se extraen id y rol
   * @param perfil  perfil asociado al usuario del cual se extraen perfilId y colId
   * @return token JWT firmado con los datos de sesión
   */
  public String generarToken(
      Usuario usuario,
      Perfil perfil
  ) {

    return Jwts.builder()
        .setSubject(usuario.getId())

        .claim("usuarioId", usuario.getId())
        .claim("rol", usuario.getRol().toString())
        .claim("perfilId", perfil.getId())
        .claim("colId", perfil.getColeccion().getId())

        .setIssuedAt(new Date())

        .setExpiration(
            new Date(
                System.currentTimeMillis()
                    + expiration.toMillis()
            )
        )

        .signWith(
            getSignKey(),
            SignatureAlgorithm.HS256
        )

        .compact();
  }

  /**
   * Crea la clave de firma HMAC para el token.
   */
  private Key getSignKey() {

    return Keys.hmacShaKeyFor(
        secret.getBytes(StandardCharsets.UTF_8)
    );
  }

  /**
   * Método que valida el token. Valida tanto por expiración como por firma (si fue modificado).
   */
  public Claims validarToken(String token) {

    return Jwts.parserBuilder()
        .setSigningKey(getSignKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  /**
   * Método que valida el token y devuelve los datos guardados en el.
   */
  public SesionDto obtenerSesion(String token) {

    Claims claims = validarToken(token);

    return new SesionDto(
        claims.get("usuarioId", String.class),
        claims.get("rol", String.class),
        claims.get("perfilId", String.class),
        claims.get("colId", String.class)
    );
  }

  /**
   * Parsea el token JWT y devuelve todos sus atributos (claims).
   *
   * @param token el token JWT a parsear
   * @return claims contenidos en el token
   */
  private Claims obtenerAtributos(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(getSignKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  /**
   * Extrae el identificador de la colección almacenado en el token JWT.
   *
   * @param token el token JWT del cual extraer el claim
   * @return identificador de la colección
   */
  public String getColeccionId(String token) {
    return obtenerAtributos(token)
        .get("colId", String.class);
  }

  /**
   * Extrae el identificador de usuario almacenado en el token JWT.
   *
   * @param token el token JWT del cual extraer el claim
   * @return identificador del usuario
   */
  public String getUsuarioId(String token) {
    return obtenerAtributos(token)
        .get("usuarioId", String.class);
  }

  /**
   * Extrae el rol de usuario almacenado en el token JWT.
   *
   * @param token el token JWT del cual extraer el claim
   * @return rol del usuario (ej. "ADMINISTRADOR", "USUARIO")
   */
  public String getRol(String token) {
    return obtenerAtributos(token)
        .get("rol", String.class);
  }

  /**
   * Extrae el identificador de perfil almacenado en el token JWT.
   *
   * @param token el token JWT del cual extraer el claim
   * @return identificador del perfil
   */
  public String getPerfilId(String token) {
    return obtenerAtributos(token)
        .get("perfilId", String.class);
  }
}
