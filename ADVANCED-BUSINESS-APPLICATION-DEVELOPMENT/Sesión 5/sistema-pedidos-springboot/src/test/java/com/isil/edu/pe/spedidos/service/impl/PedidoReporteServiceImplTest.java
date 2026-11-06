package com.isil.edu.pe.spedidos.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.isil.edu.pe.spedidos.exception.ParametroReporteInvalidoException;
import com.isil.edu.pe.spedidos.repository.PedidoReporteJdbcRepository;
import com.isil.edu.pe.spedidos.repository.projection.ClienteFrecuenteProjection;
import com.isil.edu.pe.spedidos.service.domain.ClienteFrecuente;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pruebas unitarias de las validaciones agregadas en la PA3 a PedidoReporteServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class PedidoReporteServiceImplTest {

  @Mock
  private PedidoReporteJdbcRepository jdbcRepository;

  @InjectMocks
  private PedidoReporteServiceImpl reporteService;

  @Test
  @DisplayName("Reject advanced search in PedidoReporteService when customer is blank")
  void rejectAdvancedSearchInPedidoReporteServiceWhenCustomerIsBlank() {
    // Arrange
    String cliente = "   ";

    // Act
    ParametroReporteInvalidoException error = assertThrows(
        ParametroReporteInvalidoException.class,
        () -> reporteService.buscarPedidosPorClienteYTotalMinimo(cliente, BigDecimal.TEN));

    // Assert
    assertEquals("cliente", error.getParametro());
    verify(jdbcRepository, never()).buscarPedidosPorClienteYTotalMinimo(anyString(), any());
  }

  @Test
  @DisplayName("Reject advanced search in PedidoReporteService when minimum total is negative")
  void rejectAdvancedSearchInPedidoReporteServiceWhenMinimumTotalIsNegative() {
    // Arrange
    BigDecimal totalMinimo = new BigDecimal("-1");

    // Act
    ParametroReporteInvalidoException error = assertThrows(
        ParametroReporteInvalidoException.class,
        () -> reporteService.buscarPedidosPorClienteYTotalMinimo("Ana", totalMinimo));

    // Assert
    assertEquals("totalMinimo", error.getParametro());
    verify(jdbcRepository, never()).buscarPedidosPorClienteYTotalMinimo(anyString(), any());
  }

  @Test
  @DisplayName("Reject frequent customers report in PedidoReporteService when minimum orders is zero")
  void rejectFrequentCustomersReportInPedidoReporteServiceWhenMinimumOrdersIsZero() {
    // Arrange
    int minPedidos = 0;

    // Act
    ParametroReporteInvalidoException error = assertThrows(
        ParametroReporteInvalidoException.class,
        () -> reporteService.listarClientesFrecuentes(minPedidos));

    // Assert
    assertEquals("minPedidos", error.getParametro());
    verify(jdbcRepository, never()).listarClientesFrecuentes(anyInt());
  }

  @Test
  @DisplayName("Map projection to domain in PedidoReporteService when minimum orders is valid")
  void mapProjectionToDomainInPedidoReporteServiceWhenMinimumOrdersIsValid() {
    // Arrange
    when(jdbcRepository.listarClientesFrecuentes(2)).thenReturn(List.of(
        new ClienteFrecuenteProjection("Ana Torres", 3, 5,
            new BigDecimal("5220.00"), new BigDecimal("1740.00"))));

    // Act
    List<ClienteFrecuente> clientes = reporteService.listarClientesFrecuentes(2);

    // Assert
    assertEquals(1, clientes.size());
    assertEquals("Ana Torres", clientes.get(0).cliente());
    assertEquals(3L, clientes.get(0).totalPedidos());
    assertEquals(0, new BigDecimal("1740.00").compareTo(clientes.get(0).ticketPromedio()));
    verify(jdbcRepository).listarClientesFrecuentes(2);
  }
}
