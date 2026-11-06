package com.isil.edu.pe.spedidos.exception;

/**
 * Indica que un parámetro de consulta de reportes no cumple
 * las reglas de negocio. Se traduce a HTTP 400 en la API y en la vista.
 */
public class ParametroReporteInvalidoException extends RuntimeException {

  private final String parametro;

  public ParametroReporteInvalidoException(String parametro, String message) {
    super(message);
    this.parametro = parametro;
  }

  public String getParametro() {
    return parametro;
  }
}
