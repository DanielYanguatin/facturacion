package com.facturacion.infrastructure.druid;

import com.facturacion.application.ports.output.DruidAnalyticsPort;
import com.facturacion.application.ports.output.InfraestructuraNoDisponibleException;
import com.facturacion.application.ports.output.TopProductoAnalitico;
import com.facturacion.domain.model.EventoAnaliticoFactura;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DruidHttpAdapter implements DruidAnalyticsPort {

    private final String routerUrl;
    private final String overlordUrl;
    private final String datasource;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public DruidHttpAdapter(
            @Value("${app.druid.router-url:http://localhost:8888}") String routerUrl,
            @Value("${app.druid.overlord-url:http://localhost:8081}") String overlordUrl,
            @Value("${app.druid.datasource:facturas_analiticas}") String datasource,
            ObjectMapper objectMapper
    ) {
        this.routerUrl = routerUrl.replaceAll("/+$", "");
        this.overlordUrl = overlordUrl.replaceAll("/+$", "");
        this.datasource = datasource;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(4))
                .build();
    }

    @Override
    public void ingestarEventos(List<EventoAnaliticoFactura> eventos) {
        if (eventos == null || eventos.isEmpty()) {
            return;
        }

        try {
            StringBuilder sb = new StringBuilder();
            for (EventoAnaliticoFactura ev : eventos) {
                sb.append(objectMapper.writeValueAsString(ev)).append("\n");
            }

            ObjectNode spec = objectMapper.createObjectNode();
            spec.put("type", "index_parallel");

            ObjectNode specBody = spec.putObject("spec");

            ObjectNode dataSchema = specBody.putObject("dataSchema");
            dataSchema.put("dataSource", datasource);

            ObjectNode timestampSpec = dataSchema.putObject("timestampSpec");
            timestampSpec.put("column", "fecha_factura");
            timestampSpec.put("format", "iso");

            ObjectNode dimensionsSpec = dataSchema.putObject("dimensionsSpec");
            ArrayNode dims = dimensionsSpec.putArray("dimensions");
            dims.addObject().put("type", "long").put("name", "id_factura");
            dims.add("numero_factura");
            dims.add("id_cliente");
            dims.add("categoria_cliente");
            dims.add("id_producto");
            dims.add("nombre_producto");
            dims.add("categoria_producto");
            dims.addObject().put("type", "double").put("name", "precio_unitario");
            dims.addObject().put("type", "long").put("name", "cantidad");
            dims.addObject().put("type", "double").put("name", "total_producto");
            dims.addObject().put("type", "double").put("name", "total_factura");

            dataSchema.putArray("metricsSpec");

            ObjectNode granularitySpec = dataSchema.putObject("granularitySpec");
            granularitySpec.put("type", "uniform");
            granularitySpec.put("segmentGranularity", "DAY");
            granularitySpec.put("queryGranularity", "NONE");
            granularitySpec.put("rollup", false);

            ObjectNode ioConfig = specBody.putObject("ioConfig");
            ioConfig.put("type", "index_parallel");
            ioConfig.put("appendToExisting", true);
            ObjectNode inputSource = ioConfig.putObject("inputSource");
            inputSource.put("type", "inline");
            inputSource.put("data", sb.toString());
            ObjectNode inputFormat = ioConfig.putObject("inputFormat");
            inputFormat.put("type", "json");

            ObjectNode tuningConfig = specBody.putObject("tuningConfig");
            tuningConfig.put("type", "index_parallel");
            tuningConfig.put("maxRowsPerSegment", 5000000);

            String requestBody = objectMapper.writeValueAsString(spec);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(routerUrl + "/druid/indexer/v1/task"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                // Si el router no delega el task, probar directamente al overlord
                HttpRequest retryOverlord = HttpRequest.newBuilder()
                        .uri(URI.create(overlordUrl + "/druid/indexer/v1/task"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .timeout(Duration.ofSeconds(15))
                        .build();
                HttpResponse<String> retryResp = httpClient.send(retryOverlord, HttpResponse.BodyHandlers.ofString());
                if (retryResp.statusCode() >= 400) {
                    throw new RuntimeException("Respuesta de error de Druid Task: " + retryResp.statusCode() + " -> " + retryResp.body());
                }
            }
        } catch (Exception exc) {
            throw new InfraestructuraNoDisponibleException("Apache-Druid", "Error al enviar micro-lote analitico a Apache Druid", exc);
        }
    }

    @Override
    public List<TopProductoAnalitico> consultarProductosMasVendidos(int limite) {
        String sql = String.format("""
                SELECT
                    id_producto,
                    nombre_producto,
                    categoria_producto,
                    SUM(cantidad) AS unidades_vendidas,
                    SUM(total_producto) AS total_vendido
                FROM "%s"
                GROUP BY id_producto, nombre_producto, categoria_producto
                ORDER BY unidades_vendidas DESC
                LIMIT %d
                """, datasource, Math.max(1, limite));

        try {
            JsonNode rows = ejecutarConsultaSql(sql);
            if (rows == null || !rows.isArray()) {
                return Collections.emptyList();
            }

            List<TopProductoAnalitico> resultado = new ArrayList<>();
            for (JsonNode row : rows) {
                String id = row.path("id_producto").asText("");
                String nombre = row.path("nombre_producto").asText("Desconocido");
                String cat = row.path("categoria_producto").asText("Sin categoria");
                long unidades = row.path("unidades_vendidas").asLong(0L);
                BigDecimal total = BigDecimal.valueOf(row.path("total_vendido").asDouble(0.0));
                resultado.add(new TopProductoAnalitico(id, nombre, cat, unidades, total));
            }
            return resultado;
        } catch (Exception exc) {
            // Si el datasource todavia no tiene segmentos creados, retornar vacio sin romper
            return Collections.emptyList();
        }
    }

    @Override
    public List<Map<String, Object>> consultarRegistrosRecientes(int limite) {
        String sql = String.format("""
                SELECT
                    "__time" AS fecha,
                    id_factura,
                    numero_factura,
                    id_cliente,
                    categoria_cliente,
                    id_producto,
                    nombre_producto,
                    categoria_producto,
                    precio_unitario,
                    cantidad,
                    total_producto,
                    total_factura
                FROM "%s"
                ORDER BY "__time" DESC
                LIMIT %d
                """, datasource, Math.max(1, limite));

        try {
            JsonNode rows = ejecutarConsultaSql(sql);
            if (rows == null || !rows.isArray()) {
                return Collections.emptyList();
            }
            return objectMapper.convertValue(rows, new TypeReference<>() {});
        } catch (Exception exc) {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean estaDisponible() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(routerUrl + "/status/health"))
                    .GET()
                    .timeout(Duration.ofSeconds(3))
                    .build();
            HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200;
        } catch (Exception ignored) {
            return false;
        }
    }

    private JsonNode ejecutarConsultaSql(String sql) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("query", sql);
        body.put("resultFormat", "object");

        String payload = objectMapper.writeValueAsString(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(routerUrl + "/druid/v2/sql"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .timeout(Duration.ofSeconds(10))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return objectMapper.readTree(response.body());
        }
        return null;
    }
}
