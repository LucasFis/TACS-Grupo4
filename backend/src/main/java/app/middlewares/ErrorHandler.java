package app.middlewares;

import app.dto.response.ErrorResponse;
import app.exceptions.BadRequestException;
import app.exceptions.ForbiddenException;
import app.exceptions.NotFoundException;
import app.exceptions.UnauthorizedException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
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
   * Desenvuelve excepciones generadas por proxies CGLIB (usados por
   * Spring Data MongoDB con {@code @DBRef(lazy = true)}). Cuando una
   * excepción de negocio se lanza dentro de un proxy lazy, Spring la
   * envuelve en InvocationTargetException → UndeclaredThrowableException,
   * ocultando el tipo real al ErrorHandler. Este handler extrae la causa
   * y delega al handler correspondiente.
   *
   * @param ex excepción envuelta por el proxy
   * @return respuesta HTTP correspondiente a la excepción original
   */
  @ExceptionHandler(UndeclaredThrowableException.class)
  public ResponseEntity<ErrorResponse> handleUndeclaredThrowable(
      UndeclaredThrowableException ex
  ) {
    Throwable cause = ex.getUndeclaredThrowable();
    if (cause instanceof InvocationTargetException ite) {
      cause = ite.getTargetException();
    }
    return delegateToHandler(cause);
  }

  /**
   * Desenvuelve {@link InvocationTargetException} que pueden surgir
   * cuando una excepción se lanza a través de invocación por reflexión.
   *
   * @param ex InvocationTargetException
   * @return respuesta HTTP correspondiente a la excepción envuelta
   */
  @ExceptionHandler(InvocationTargetException.class)
  public ResponseEntity<ErrorResponse> handleInvocationTarget(
      InvocationTargetException ex
  ) {
    return delegateToHandler(ex.getTargetException());
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

  /**
   * Delega una excepción desenvuelta al handler específico que le
   * corresponde. Si no coincide con ningún handler conocido, retorna 500.
   */
  private ResponseEntity<ErrorResponse> delegateToHandler(Throwable cause) {
    if (cause instanceof BadRequestException bre) {
      return handleBadRequest(bre);
    }
    if (cause instanceof NotFoundException nfe) {
      return handleNotFound(nfe);
    }
    if (cause instanceof ForbiddenException fe) {
      return handleForbidden(fe);
    }
    if (cause instanceof UnauthorizedException ue) {
      return handleUnathorized(ue);
    }
    if (cause instanceof IllegalArgumentException iae) {
      return handleIllegalArgument(iae);
    }
    if (cause != null) {
      cause.printStackTrace();
    }
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
