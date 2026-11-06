package com.isil.edu.pe.spedidos.service.domain;

import java.math.BigDecimal;

/**
 * Indicadores consolidados de un cliente que alcanza
 * el umbral mínimo de pedidos.
 */
public record ClienteFrecuente(
    String cliente,
    long totalPedidos,
    long totalUnidades,
    BigDecimal importeAcumulado,
    BigDecimal ticketPromedio
) {
}
