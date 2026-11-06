package com.isil.edu.pe.spedidos.controller;

import com.isil.edu.pe.spedidos.controller.dto.ErrorReporteResponse;
import com.isil.edu.pe.spedidos.exception.ParametroReporteInvalidoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Traduce los errores de la API de reportes a respuestas JSON con códigos HTTP coherentes.
 */
@RestControllerAdvice(assignableTypes = PedidoReporteController.class)
public class PedidoReporteApiExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(PedidoReporteApiExceptionHandler.class);

  @ExceptionHandler(ParametroReporteInvalidoException.class)
  public ResponseEntity<ErrorReporteResponse> manejarParametroInvalido(
      ParametroReporteInvalidoException e) {
    return badRequest(e.getParametro(), e.getMessage());
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorReporteResponse> manejarParametroFaltante(
      MissingServletRequestParameterException e) {
    return badRequest(e.getParameterName(),
        "El parámetro " + e.getParameterName() + " es obligatorio.");
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorReporteResponse> manejarTipoInvalido(
      MethodArgumentTypeMismatchException e) {
    return badRequest(e.getName(),
        "El parámetro " + e.getName() + " tiene un formato numérico inválido.");
  }

  @ExceptionHandler(DataAccessException.class)
  public ResponseEntity<ErrorReporteResponse> manejarErrorDeDatos(DataAccessException e) {
    log.error("No se pudo ejecutar la consulta de reportes.", e);
    HttpStatus status = HttpStatus.SERVICE_UNAVAILABLE;
    return ResponseEntity.status(status).body(ErrorReporteResponse.of(status.value(),
        status.getReasonPhrase(), null,
        "La base de datos de reportes no está disponible. Intente nuevamente."));
  }

  private ResponseEntity<ErrorReporteResponse> badRequest(String parametro, String mensaje) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    return ResponseEntity.status(status).body(ErrorReporteResponse.of(status.value(),
        status.getReasonPhrase(), parametro, mensaje));
  }
}
