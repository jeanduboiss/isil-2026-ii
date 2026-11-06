<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!doctype html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Reportes de Pedidos - ISIL</title>
    <c:url var="cssUrl" value="/assets/css/app.css"/>
    <link rel="stylesheet" href="${cssUrl}">
</head>
<body>
<div class="contenedor">
    <header class="cabecera">
        <div>
            <p class="eyebrow">Spring MVC · Spring Security · Spring JDBC</p>
            <h1>Consulta y supervisión de pedidos</h1>
            <p class="subtitulo">
                Reportes calculados con NamedParameterJdbcTemplate sobre la base de pedidos local.
            </p>
        </div>
        <c:url var="pedidosUrl" value="/pedidos"/>
        <c:url var="logoutUrl" value="/logout"/>
        <nav class="acciones">
            <a href="${pedidosUrl}">Volver a pedidos</a>
            <form method="post" action="${logoutUrl}">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <button type="submit">Cerrar sesión</button>
            </form>
        </nav>
        <div class="flujo">
            DispatcherServlet → PedidoReporteWebController → PedidoReporteService → PedidoReporteJdbcRepository → MySQL
        </div>
    </header>

    <c:if test="${not empty error}">
        <div class="alerta error">
            <c:out value="${error}"/>
        </div>
    </c:if>

    <section class="tarjeta">
        <h2>Resumen general</h2>
        <div class="indicadores">
            <div class="indicador">
                <span>Pedidos</span>
                <strong><c:out value="${resumen.totalPedidos}"/></strong>
            </div>
            <div class="indicador">
                <span>Unidades</span>
                <strong><c:out value="${resumen.totalUnidades}"/></strong>
            </div>
            <div class="indicador">
                <span>Importe acumulado</span>
                <strong>S/ <fmt:formatNumber value="${resumen.importeAcumulado}"
                                             minFractionDigits="2" maxFractionDigits="2"/></strong>
            </div>
            <div class="indicador">
                <span>Ticket promedio</span>
                <strong>S/ <fmt:formatNumber value="${resumen.ticketPromedio}"
                                             minFractionDigits="2" maxFractionDigits="2"/></strong>
            </div>
        </div>
    </section>

    <c:url var="reportesUrl" value="/reportes/pedidos"/>

    <section class="tarjeta">
        <h2>Búsqueda avanzada</h2>
        <form method="get" action="${reportesUrl}" class="form-grid">
            <label>
                Cliente
                <input name="cliente" maxlength="120" placeholder="Ej. Ana"
                       value="${fn:escapeXml(clienteIngresado)}">
            </label>
            <label>
                Total mínimo (S/)
                <input name="totalMinimo" type="number" step="0.01"
                       value="${empty totalMinimoIngresado ? 0 : totalMinimoIngresado}">
            </label>
            <input type="hidden" name="minPedidos" value="${minPedidosIngresado}">
            <button type="submit">Buscar</button>
        </form>

        <c:if test="${pedidosEncontrados ne null}">
            <div class="tabla-scroll">
                <table>
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Cliente</th>
                        <th>Producto</th>
                        <th>Cantidad</th>
                        <th>Total</th>
                        <th>Fecha</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="pedido" items="${pedidosEncontrados}">
                        <tr>
                            <td><c:out value="${pedido.id}"/></td>
                            <td><c:out value="${pedido.cliente}"/></td>
                            <td><c:out value="${pedido.productoCodigo}"/> - <c:out value="${pedido.productoNombre}"/></td>
                            <td><c:out value="${pedido.cantidad}"/></td>
                            <td>S/ <fmt:formatNumber value="${pedido.total}"
                                                     minFractionDigits="2" maxFractionDigits="2"/></td>
                            <td><c:out value="${pedido.fecha}"/></td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty pedidosEncontrados}">
                        <tr>
                            <td colspan="6" class="sin-datos">No hay pedidos que cumplan los filtros.</td>
                        </tr>
                    </c:if>
                    </tbody>
                </table>
            </div>
        </c:if>
    </section>

    <section class="tarjeta">
        <h2>Clientes frecuentes</h2>
        <form method="get" action="${reportesUrl}" class="form-grid">
            <label>
                Mínimo de pedidos
                <input name="minPedidos" type="number" step="1"
                       value="${fn:escapeXml(minPedidosIngresado)}">
            </label>
            <c:if test="${clienteIngresado ne null}">
                <input type="hidden" name="cliente" value="${fn:escapeXml(clienteIngresado)}">
                <input type="hidden" name="totalMinimo" value="${totalMinimoIngresado}">
            </c:if>
            <button type="submit">Consultar</button>
        </form>

        <div class="tabla-scroll">
            <table>
                <thead>
                <tr>
                    <th>Cliente</th>
                    <th>Pedidos</th>
                    <th>Unidades</th>
                    <th>Importe acumulado</th>
                    <th>Ticket promedio</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="frecuente" items="${clientesFrecuentes}">
                    <tr>
                        <td><c:out value="${frecuente.cliente}"/></td>
                        <td><c:out value="${frecuente.totalPedidos}"/></td>
                        <td><c:out value="${frecuente.totalUnidades}"/></td>
                        <td>S/ <fmt:formatNumber value="${frecuente.importeAcumulado}"
                                                 minFractionDigits="2" maxFractionDigits="2"/></td>
                        <td>S/ <fmt:formatNumber value="${frecuente.ticketPromedio}"
                                                 minFractionDigits="2" maxFractionDigits="2"/></td>
                    </tr>
                </c:forEach>
                <c:if test="${empty clientesFrecuentes}">
                    <tr>
                        <td colspan="5" class="sin-datos">Ningún cliente alcanza el mínimo indicado.</td>
                    </tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </section>
</div>
</body>
</html>
