package com.isil.edu.pe.spedidos.controller.dto;

import java.time.LocalDateTime;

/**
 * Cuerpo JSON devuelto cuando una consulta de reportes no puede atenderse.
 */
public record ErrorReporteResponse(
    int status,
    String error,
    String parametro,
    String mensaje,
    LocalDateTime fecha
) {

  public static ErrorReporteResponse of(int status, String error, String parametro,
      String mensaje) {
    return new ErrorReporteResponse(status, error, parametro, mensaje, LocalDateTime.now());
  }
}
