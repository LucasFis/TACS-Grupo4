package app.middlewares;

import app.dto.response.ErrorResponse;
import app.exceptions.BadRequestException;
import app.exceptions.ForbiddenException;
import app.exceptions.NotFoundException;
import app.exceptions.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ManejadorDeErroresTest {

  private final ErrorHandler errorHandler = new ErrorHandler();

  // -------------------------
  // NOT FOUND
  // -------------------------
  @Test
  void handleNotFound_devuelve404() {
    NotFoundException ex = new NotFoundException("recurso no encontrado");

    ResponseEntity<ErrorResponse> response =
        errorHandler.handleNotFound(ex);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

    ErrorResponse body = response.getBody();
    assertNotNull(body);

    assertEquals(404, body.status());
    assertEquals("recurso no encontrado", body.message());
    assertEquals(Map.of(), body.errors());
    assertNotNull(body.timestamp());
  }

  // -------------------------
  // BAD REQUEST
  // -------------------------
  @Test
  void handleBadRequest_devuelve400() {
    BadRequestException ex = new BadRequestException("request inválida");

    ResponseEntity<ErrorResponse> response =
        errorHandler.handleBadRequest(ex);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

    ErrorResponse body = response.getBody();
    assertNotNull(body);

    assertEquals(400, body.status());
    assertEquals("request inválida", body.message());
    assertEquals(Map.of(), body.errors());
    assertNotNull(body.timestamp());
  }

  // -------------------------
  // UNAUTHORIZED
  // -------------------------
  @Test
  void handleUnauthorized_devuelve401() {
    UnauthorizedException ex = new UnauthorizedException("no autorizado");

    ResponseEntity<ErrorResponse> response =
        errorHandler.handleUnathorized(ex);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

    ErrorResponse body = response.getBody();
    assertNotNull(body);

    assertEquals(401, body.status());
    assertEquals("no autorizado", body.message());
    assertEquals(Map.of(), body.errors());
    assertNotNull(body.timestamp());
  }

  // -------------------------
  // FORBIDDEN
  // -------------------------
  @Test
  void handleForbidden_devuelve403() {
    ForbiddenException ex = new ForbiddenException("acceso denegado");

    ResponseEntity<ErrorResponse> response =
        errorHandler.handleForbidden(ex);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

    ErrorResponse body = response.getBody();
    assertNotNull(body);

    assertEquals(403, body.status());
    assertEquals("acceso denegado", body.message());
    assertEquals(Map.of(), body.errors());
    assertNotNull(body.timestamp());
  }

  // -------------------------
  // VALIDATION (MethodArgumentNotValidException)
  // -------------------------
  @Test
  void handleValidation_devuelve400ConErroresPorCampo() {
    BindingResult bindingResult = mock(BindingResult.class);
    FieldError fieldError1 = new FieldError("obj", "nombre", "el nombre es obligatorio");
    FieldError fieldError2 = new FieldError("obj", "email", "email inválido");
    when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

    MethodArgumentNotValidException ex =
        new MethodArgumentNotValidException((MethodParameter) null, bindingResult);

    ResponseEntity<ErrorResponse> response =
        errorHandler.handleValidation(ex);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

    ErrorResponse body = response.getBody();
    assertNotNull(body);

    assertEquals(400, body.status());
    assertEquals("Error de validación", body.message());
    assertEquals("el nombre es obligatorio", body.errors().get("nombre"));
    assertEquals("email inválido", body.errors().get("email"));
  }

  // -------------------------
  // TYPE MISMATCH (MethodArgumentTypeMismatchException)
  // -------------------------
  @Test
  void handleTypeMismatch_devuelve400() {
    MethodArgumentTypeMismatchException ex =
        mock(MethodArgumentTypeMismatchException.class);
    when(ex.getName()).thenReturn("id");
    when(ex.getValue()).thenReturn("abc");

    ResponseEntity<ErrorResponse> response =
        errorHandler.handleTypeMismatch(ex);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

    ErrorResponse body = response.getBody();
    assertNotNull(body);

    assertEquals(400, body.status());
    assertTrue(body.message().contains("id"));
    assertTrue(body.message().contains("abc"));
    assertEquals(Map.of(), body.errors());
    assertNotNull(body.timestamp());
  }

  // -------------------------
  // ILLEGAL ARGUMENT
  // -------------------------
  @Test
  void handleIllegalArgument_devuelve400() {
    IllegalArgumentException ex = new IllegalArgumentException("valor fuera de rango");

    ResponseEntity<ErrorResponse> response =
        errorHandler.handleIllegalArgument(ex);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

    ErrorResponse body = response.getBody();
    assertNotNull(body);

    assertEquals(400, body.status());
    assertEquals("valor fuera de rango", body.message());
    assertEquals(Map.of(), body.errors());
    assertNotNull(body.timestamp());
  }

  // -------------------------
  // MISSING COOKIE
  // -------------------------
  @Test
  void handleMissingCookie_devuelve400() {
    MissingRequestCookieException ex =
        mock(MissingRequestCookieException.class);
    when(ex.getMessage()).thenReturn("Cookie 'token' no encontrada");

    ResponseEntity<ErrorResponse> response =
        errorHandler.handleMissingCookie(ex);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

    ErrorResponse body = response.getBody();
    assertNotNull(body);

    assertEquals(400, body.status());
    assertEquals("Cookie 'token' no encontrada", body.message());
    assertEquals(Map.of(), body.errors());
    assertNotNull(body.timestamp());
  }

  // -------------------------
  // UNDECLARED THROWABLE (proxy CGLIB)
  // -------------------------
  @Test
  void handleUndeclaredThrowable_conInvocationTargetException_desenvuelve() {
    NotFoundException cause = new NotFoundException("no existe");
    InvocationTargetException ite = new InvocationTargetException(cause);
    UndeclaredThrowableException ex = new UndeclaredThrowableException(ite, null);

    ResponseEntity<ErrorResponse> response =
        errorHandler.handleUndeclaredThrowable(ex);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertEquals(404, response.getBody().status());
  }

  @Test
  void handleUndeclaredThrowable_conExcepcionDirecta_desenvuelve() {
    BadRequestException cause = new BadRequestException("bad");
    UndeclaredThrowableException ex = new UndeclaredThrowableException(cause, null);

    ResponseEntity<ErrorResponse> response =
        errorHandler.handleUndeclaredThrowable(ex);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void handleUndeclaredThrowable_excepcionDesconocida_devuelve500() {
    RuntimeException cause = new RuntimeException("algo raro");
    UndeclaredThrowableException ex = new UndeclaredThrowableException(cause, null);

    ResponseEntity<ErrorResponse> response =
        errorHandler.handleUndeclaredThrowable(ex);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  // -------------------------
  // INVOCATION TARGET
  // -------------------------
  @Test
  void handleInvocationTarget_conNotFoundException_devuelve404() {
    NotFoundException cause = new NotFoundException("no encontrado");
    InvocationTargetException ex = new InvocationTargetException(cause);

    ResponseEntity<ErrorResponse> response =
        errorHandler.handleInvocationTarget(ex);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertEquals(404, response.getBody().status());
  }

  @Test
  void handleInvocationTarget_conUnauthorizedException_devuelve401() {
    UnauthorizedException cause = new UnauthorizedException("unauth");
    InvocationTargetException ex = new InvocationTargetException(cause);

    ResponseEntity<ErrorResponse> response =
        errorHandler.handleInvocationTarget(ex);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  void handleInvocationTarget_conForbiddenException_devuelve403() {
    ForbiddenException cause = new ForbiddenException("forbidden");
    InvocationTargetException ex = new InvocationTargetException(cause);

    ResponseEntity<ErrorResponse> response =
        errorHandler.handleInvocationTarget(ex);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  void handleInvocationTarget_conIllegalArgument_devuelve400() {
    IllegalArgumentException cause = new IllegalArgumentException("bad arg");
    InvocationTargetException ex = new InvocationTargetException(cause);

    ResponseEntity<ErrorResponse> response =
        errorHandler.handleInvocationTarget(ex);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void handleInvocationTarget_excepcionDesconocida_devuelve500() {
    InvocationTargetException ex = new InvocationTargetException(new RuntimeException("x"));

    ResponseEntity<ErrorResponse> response =
        errorHandler.handleInvocationTarget(ex);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  // -------------------------
  // GENERIC EXCEPTION (500)
  // -------------------------
  @Test
  void handleException_devuelve500() {
    Exception ex = new RuntimeException("boom");

    ResponseEntity<ErrorResponse> response =
        errorHandler.handleInternalServerError(ex);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

    ErrorResponse body = response.getBody();
    assertNotNull(body);

    assertEquals(500, body.status());
    assertEquals("Ocurrió un error interno del servidor", body.message());
    assertEquals(Map.of(), body.errors());
    assertNotNull(body.timestamp());
  }
}
