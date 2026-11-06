package com.isil.edu.pe.spedidos.controller;

import com.isil.edu.pe.spedidos.exception.ParametroReporteInvalidoException;
import com.isil.edu.pe.spedidos.service.PedidoReporteService;
import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Vista JSP del módulo de consulta y supervisión de pedidos (PA3).
 * Reutiliza PedidoReporteService: no contiene SQL ni reglas de validación.
 */
@Controller
@RequestMapping("/reportes")
public class PedidoReporteWebController {

  private static final Logger log = LoggerFactory.getLogger(PedidoReporteWebController.class);

  private static final String VISTA = "reportes-pedidos";

  private static final int MIN_PEDIDOS_POR_DEFECTO = 2;

  private final PedidoReporteService reporteService;

  public PedidoReporteWebController(PedidoReporteService reporteService) {
    this.reporteService = reporteService;
  }

  /**
   * Carga el resumen existente, la búsqueda avanzada (si se envió cliente)
   * y los clientes frecuentes según minPedidos.
   */
  @GetMapping("/pedidos")
  public String verReportes(
      @RequestParam(required = false) String cliente,
      @RequestParam(required = false) BigDecimal totalMinimo,
      @RequestParam(defaultValue = "" + MIN_PEDIDOS_POR_DEFECTO) int minPedidos,
      Model model,
      HttpServletResponse response) {

    model.addAttribute("resumen", reporteService.obtenerResumen());
    model.addAttribute("clienteIngresado", cliente);
    model.addAttribute("totalMinimoIngresado", totalMinimo);
    model.addAttribute("minPedidosIngresado", minPedidos);

    try {
      if (cliente != null) {
        BigDecimal monto = totalMinimo != null ? totalMinimo : BigDecimal.ZERO;
        model.addAttribute("pedidosEncontrados",
            reporteService.buscarPedidosPorClienteYTotalMinimo(cliente, monto));
      }
      model.addAttribute("clientesFrecuentes",
          reporteService.listarClientesFrecuentes(minPedidos));
    } catch (ParametroReporteInvalidoException e) {
      response.setStatus(HttpStatus.BAD_REQUEST.value());
      model.addAttribute("error", e.getMessage());
    }
    return VISTA;
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ModelAndView manejarParametroNoNumerico(MethodArgumentTypeMismatchException e) {
    ModelAndView vista = new ModelAndView(VISTA, HttpStatus.BAD_REQUEST);
    vista.addObject("resumen", reporteService.obtenerResumen());
    vista.addObject("minPedidosIngresado", MIN_PEDIDOS_POR_DEFECTO);
    vista.addObject("error",
        "El valor ingresado en " + e.getName() + " debe ser numérico.");
    return vista;
  }

  @ExceptionHandler(DataAccessException.class)
  public ModelAndView manejarErrorDeDatos(DataAccessException e) {
    log.error("No se pudo cargar la vista de reportes.", e);
    ModelAndView vista = new ModelAndView("error", HttpStatus.SERVICE_UNAVAILABLE);
    vista.addObject("error",
        "La base de datos de reportes no está disponible. Intente nuevamente.");
    return vista;
  }
}
