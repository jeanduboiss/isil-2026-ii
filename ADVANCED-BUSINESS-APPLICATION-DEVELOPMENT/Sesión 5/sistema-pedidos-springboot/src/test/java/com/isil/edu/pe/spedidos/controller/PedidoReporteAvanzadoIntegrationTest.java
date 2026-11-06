package com.isil.edu.pe.spedidos.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * Pruebas de integración de la API, la vista JSP y la seguridad del módulo de reportes (PA3).
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PedidoReporteAvanzadoIntegrationTest {

  private MockMvc mockMvc;

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
        .apply(springSecurity())
        .build();
    jdbcTemplate.update("DELETE FROM pedido");
    insertarPedido("Ana Torres", "LAP-001", "Laptop", "2500.00", 2, "5000.00");
    insertarPedido("Ana Torres", "MOU-001", "Mouse", "50.00", 2, "100.00");
    insertarPedido("Carlos Perez", "MOU-001", "Mouse", "50.00", 3, "150.00");
  }

  @Test
  @DisplayName("Return filtered orders JSON when CONSULTA requests advanced search")
  void returnFilteredOrdersJsonWhenConsultaRequestsAdvancedSearch() throws Exception {
    // Arrange / Act
    ResultActions response = mockMvc.perform(get("/api/v1/pedidos/busqueda-avanzada")
        .with(user("consulta").roles("CONSULTA"))
        .param("cliente", "Ana")
        .param("totalMinimo", "1000"));

    // Assert
    response.andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].cliente").value("Ana Torres"))
        .andExpect(jsonPath("$[0].total").value(5000.00));
  }

  @Test
  @DisplayName("Return HTTP 400 with error JSON when advanced search receives negative minimum total")
  void returnHttp400WithErrorJsonWhenAdvancedSearchReceivesNegativeMinimumTotal() throws Exception {
    // Arrange / Act
    ResultActions response = mockMvc.perform(get("/api/v1/pedidos/busqueda-avanzada")
        .with(user("consulta").roles("CONSULTA"))
        .param("cliente", "Ana")
        .param("totalMinimo", "-5"));

    // Assert
    response.andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.parametro").value("totalMinimo"))
        .andExpect(jsonPath("$.mensaje").isNotEmpty());
  }

  @Test
  @DisplayName("Return HTTP 400 when frequent customers endpoint receives non numeric minimum orders")
  void returnHttp400WhenFrequentCustomersEndpointReceivesNonNumericMinimumOrders() throws Exception {
    // Arrange / Act
    ResultActions response = mockMvc.perform(get("/api/v1/pedidos/clientes-frecuentes")
        .with(user("operador").roles("OPERADOR"))
        .param("minPedidos", "abc"));

    // Assert
    response.andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.parametro").value("minPedidos"));
  }

  @Test
  @DisplayName("Return frequent customers JSON when OPERADOR requests minimum two orders")
  void returnFrequentCustomersJsonWhenOperadorRequestsMinimumTwoOrders() throws Exception {
    // Arrange / Act
    ResultActions response = mockMvc.perform(get("/api/v1/pedidos/clientes-frecuentes")
        .with(user("operador").roles("OPERADOR"))
        .param("minPedidos", "2"));

    // Assert
    response.andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].cliente").value("Ana Torres"))
        .andExpect(jsonPath("$[0].totalPedidos").value(2))
        .andExpect(jsonPath("$[0].totalUnidades").value(4))
        .andExpect(jsonPath("$[0].importeAcumulado").value(5100.00))
        .andExpect(jsonPath("$[0].ticketPromedio").value(2550.00));
  }

  @Test
  @DisplayName("Render reports view with model data when CONSULTA opens PedidoReporteWebController")
  void renderReportsViewWithModelDataWhenConsultaOpensPedidoReporteWebController() throws Exception {
    // Arrange / Act
    ResultActions response = mockMvc.perform(get("/reportes/pedidos")
        .with(user("consulta").roles("CONSULTA"))
        .param("cliente", "Ana")
        .param("totalMinimo", "1000")
        .param("minPedidos", "2"));

    // Assert
    response.andExpect(status().isOk())
        .andExpect(view().name("reportes-pedidos"))
        .andExpect(model().attributeExists("resumen", "pedidosEncontrados", "clientesFrecuentes"))
        .andExpect(model().attribute("pedidosEncontrados", hasSize(1)))
        .andExpect(model().attribute("clientesFrecuentes", hasSize(1)));
  }

  @Test
  @DisplayName("Render reports view with HTTP 400 and error message when minimum orders is zero")
  void renderReportsViewWithHttp400AndErrorMessageWhenMinimumOrdersIsZero() throws Exception {
    // Arrange / Act
    ResultActions response = mockMvc.perform(get("/reportes/pedidos")
        .with(user("consulta").roles("CONSULTA"))
        .param("minPedidos", "0"));

    // Assert
    response.andExpect(status().isBadRequest())
        .andExpect(view().name("reportes-pedidos"))
        .andExpect(model().attributeExists("error", "resumen"));
  }

  @Test
  @DisplayName("Redirect anonymous user to login when accessing reports view")
  void redirectAnonymousUserToLoginWhenAccessingReportsView() throws Exception {
    // Arrange / Act
    ResultActions response = mockMvc.perform(get("/reportes/pedidos"));

    // Assert: el filtro de seguridad intercepta antes del controlador.
    response.andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/login"));
  }

  @Test
  @DisplayName("Reject frequent customers endpoint when user has no reporting role")
  void rejectFrequentCustomersEndpointWhenUserHasNoReportingRole() throws Exception {
    // Arrange / Act
    ResultActions response = mockMvc.perform(get("/api/v1/pedidos/clientes-frecuentes")
        .with(user("invitado").roles("INVITADO"))
        .param("minPedidos", "2"));

    // Assert
    response.andExpect(status().isForbidden());
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
