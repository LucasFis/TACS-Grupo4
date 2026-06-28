package app.middlewares;

import app.dto.response.ErrorResponse;
import app.exceptions.BadRequestException;
import app.exceptions.ForbiddenException;
import app.exceptions.NotFoundException;
import app.exceptions.UnauthorizedException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Manejador global de excepciones de la API. Captura las excepciones lanzadas
 * por los controladores y las convierte en respuestas HTTP estructuradas
 * como {@link ErrorResponse}, con el código de estado, mensaje y timestamp
 * correspondientes.
 */
@RestControllerAdvice
public class ErrorHandler {

  /**
   * Maneja errores de recurso no encontrado (404).
   *
   * @param ex excepción lanzada
   * @return respuesta 404 con el mensaje de error
   */
  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
    ErrorResponse errorResponse = new ErrorResponse(
        HttpStatus.NOT_FOUND.value(),
        ex.getMessage(),
        Map.of(),
        LocalDateTime.now()
    );
    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(errorResponse);
  }

  /**
   * Maneja errores de solicitud incorrecta (400).
   *
   * @param ex excepción lanzada
   * @return respuesta 400 con el mensaje de error
   */
  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ErrorResponse> handleBadRequest(
      BadRequestException ex
  ) {

    ErrorResponse error = new ErrorResponse(
        HttpStatus.BAD_REQUEST.value(),
        ex.getMessage(),
        Map.of(),
        LocalDateTime.now()
    );

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(error);
  }

  /**
   * Maneja errores de autenticación (401).
   *
   * @param ex excepción lanzada
   * @return respuesta 401 con el mensaje de error
   */
  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ErrorResponse> handleUnathorized(
      UnauthorizedException ex
  ) {

    ErrorResponse error = new ErrorResponse(
        HttpStatus.UNAUTHORIZED.value(),
        ex.getMessage(),
        Map.of(),
        LocalDateTime.now()
    );

    return ResponseEntity
        .status(HttpStatus.UNAUTHORIZED)
        .body(error);
  }

  /**
   * Maneja errores de acceso prohibido (403).
   *
   * @param ex excepción lanzada
   * @return respuesta 403 con el mensaje de error
   */
  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ErrorResponse> handleForbidden(
      ForbiddenException ex
  ) {

    ErrorResponse error = new ErrorResponse(
        HttpStatus.FORBIDDEN.value(),
        ex.getMessage(),
        Map.of(),
        LocalDateTime.now()
    );

    return ResponseEntity
        .status(HttpStatus.FORBIDDEN)
        .body(error);
  }

  /**
   * Maneja errores de validación de argumentos en los controladores.
   * Devuelve los errores de campo individuales en el mapa {@code errors}.
   *
   * @param ex excepción lanzada por validación fallida
   * @return respuesta 400 con los errores de validación por campo
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
    Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
        .collect(Collectors.toMap(
            FieldError::getField,
            e -> e.getDefaultMessage() != null ? e.getDefaultMessage() : "inválido",
            (a, b) -> a
        ));
    ErrorResponse error = new ErrorResponse(
        HttpStatus.BAD_REQUEST.value(),
        "Error de validación",
        errors,
        LocalDateTime.now()
    );
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  /**
   * Maneja errores de tipo inválido en parámetros de ruta o query.
   *
   * @param ex excepción lanzada
   * @return respuesta 400 indicando el parámetro y valor inválido
   */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
    ErrorResponse error = new ErrorResponse(
        HttpStatus.BAD_REQUEST.value(),
        "Valor inválido para el parámetro '" + ex.getName() + "': " + ex.getValue(),
        Map.of(),
        LocalDateTime.now()
    );
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  /**
   * Maneja errores de argumento ilegal (400).
   *
   * @param ex excepción lanzada
   * @return respuesta 400 con el mensaje de error
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
    ErrorResponse error = new ErrorResponse(
        HttpStatus.BAD_REQUEST.value(),
        ex.getMessage(),
        Map.of(),
        LocalDateTime.now()
    );
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  /**
   * Maneja errores por cookie faltante en la petición (400).
   *
   * @param ex excepción lanzada
   * @return respuesta 400 indicando la cookie faltante
   */
  @ExceptionHandler(MissingRequestCookieException.class)
  public ResponseEntity<ErrorResponse> handleMissingCookie(MissingRequestCookieException ex) {
    ErrorResponse error = new ErrorResponse(
        HttpStatus.BAD_REQUEST.value(),
        ex.getMessage(),
        Map.of(),
        LocalDateTime.now()
    );
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  /**
   * Maneja cualquier excepción no capturada por los handlers específicos.
   * Retorna un error 500 genérico.
   *
   * @param ex excepción no esperada
   * @return respuesta 500 con mensaje genérico
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleInternalServerError(
      Exception ex
  ) {

    ex.printStackTrace();

    ErrorResponse error = new ErrorResponse(
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "Ocurrió un error interno del servidor",
        Map.of(),
        LocalDateTime.now()
    );

    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(error);
  }
}
