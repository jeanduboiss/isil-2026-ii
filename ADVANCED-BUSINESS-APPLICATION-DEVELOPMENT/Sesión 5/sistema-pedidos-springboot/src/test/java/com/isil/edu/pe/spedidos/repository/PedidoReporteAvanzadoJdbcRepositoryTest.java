package com.isil.edu.pe.spedidos.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.repository.projection.ClienteFrecuenteProjection;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pruebas de integración (H2) de las consultas Spring JDBC agregadas en la PA3.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PedidoReporteAvanzadoJdbcRepositoryTest {

  @Autowired
  private PedidoReporteJdbcRepository repository;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void setUp() {
    jdbcTemplate.update("DELETE FROM pedido");
    insertarPedido("Ana Torres", "LAP-001", "Laptop", "2500.00", 2, "5000.00");
    insertarPedido("Ana Torres", "MOU-001", "Mouse", "50.00", 2, "100.00");
    insertarPedido("Ana Torres", "TEC-001", "Teclado", "120.00", 1, "120.00");
    insertarPedido("Carlos Perez", "MOU-001", "Mouse", "50.00", 3, "150.00");
    insertarPedido("Luis Quispe", "LAP-001", "Laptop", "2500.00", 1, "2500.00");
  }

  @Test
  @DisplayName("Return only matching orders sorted by id desc when searching by customer and minimum total")
  void returnOnlyMatchingOrdersSortedByIdDescWhenSearchingByCustomerAndMinimumTotal() {
    // Arrange
    String cliente = "ana";
    BigDecimal totalMinimo = new BigDecimal("110.00");

    // Act
    List<Pedido> pedidos = repository.buscarPedidosPorClienteYTotalMinimo(cliente, totalMinimo);

    // Assert: el pedido de 100.00 de Ana y los pedidos de otros clientes quedan fuera.
    assertEquals(2, pedidos.size());
    assertTrue(pedidos.stream().allMatch(p -> "Ana Torres".equals(p.getCliente())));
    assertTrue(pedidos.stream().allMatch(p -> p.getTotal().compareTo(totalMinimo) >= 0));
    assertTrue(pedidos.get(0).getId() > pedidos.get(1).getId());
    assertEquals("TEC-001", pedidos.get(0).getProductoCodigo());
  }

  @Test
  @DisplayName("Treat SQL fragments as literal values when searching by customer and minimum total")
  void treatSqlFragmentsAsLiteralValuesWhenSearchingByCustomerAndMinimumTotal() {
    // Arrange: si el valor se concatenara, esta condición devolvería todos los pedidos.
    String clienteMalicioso = "' OR '1'='1";

    // Act
    List<Pedido> pedidos = repository.buscarPedidosPorClienteYTotalMinimo(
        clienteMalicioso, BigDecimal.ZERO);

    // Assert
    assertTrue(pedidos.isEmpty());
  }

  @Test
  @DisplayName("Return aggregated values only for customers reaching minimum orders in frequent customers report")
  void returnAggregatedValuesOnlyForCustomersReachingMinimumOrdersInFrequentCustomersReport() {
    // Arrange: Ana tiene 3 pedidos; Carlos y Luis solo 1.
    int minPedidos = 2;

    // Act
    List<ClienteFrecuenteProjection> clientes = repository.listarClientesFrecuentes(minPedidos);

    // Assert
    assertEquals(1, clientes.size());
    ClienteFrecuenteProjection ana = clientes.get(0);
    assertEquals("Ana Torres", ana.cliente());
    assertEquals(3L, ana.totalPedidos());
    assertEquals(5L, ana.totalUnidades());
    assertEquals(0, new BigDecimal("5220.00").compareTo(ana.importeAcumulado()));
    assertEquals(0, new BigDecimal("1740.00").compareTo(ana.ticketPromedio()));
  }

  @Test
  @DisplayName("Return every customer ordered by accumulated amount when minimum orders is one")
  void returnEveryCustomerOrderedByAccumulatedAmountWhenMinimumOrdersIsOne() {
    // Arrange
    int minPedidos = 1;

    // Act
    List<ClienteFrecuenteProjection> clientes = repository.listarClientesFrecuentes(minPedidos);

    // Assert: orden por importe acumulado descendente.
    assertEquals(3, clientes.size());
    assertEquals("Ana Torres", clientes.get(0).cliente());
    assertEquals("Luis Quispe", clientes.get(1).cliente());
    assertEquals("Carlos Perez", clientes.get(2).cliente());
  }

  private void insertarPedido(String cliente, String productoCodigo, String productoNombre,
      String precioUnitario, int cantidad, String total) {
    jdbcTemplate.update("""
        INSERT INTO pedido
            (cliente, producto_codigo, producto_nombre, precio_unitario, cantidad, total, fecha)
        VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
        """,
        cliente, productoCodigo, productoNombre,
        new BigDecimal(precioUnitario), cantidad, new BigDecimal(total));
  }
}
