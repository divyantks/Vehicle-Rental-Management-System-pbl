import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpHelper {

    public static String quote(String text) {
        if (text == null) {
            text = "";
        }
        StringBuilder result = new StringBuilder("\"");
        for (char c : text.toCharArray()) {
            switch (c) {
                case '"':  result.append("\\\""); break;
                case '\\': result.append("\\\\"); break;
                case '\n': result.append("\\n");  break;
                case '\r': result.append("\\r");  break;
                case '\t': result.append("\\t");  break;
                default:
                    if (c < 0x20) {
                        result.append(String.format("\\u%04x", (int) c));
                    } else {
                        result.append(c);
                    }
            }
        }
        result.append("\"");
        return result.toString();
    }

    public static HashMap<String, String> parseForm(String raw) {
        HashMap<String, String> values = new HashMap<>();
        if (raw == null || raw.isEmpty()) {
            return values;
        }
        for (String pair : raw.split("&")) {
            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String value = parts.length > 1 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
            values.put(key, value);
        }
        return values;
    }

    public static String readBody(HttpExchange exchange) throws IOException {
        byte[] bytes = exchange.getRequestBody().readNBytes(10001);
        if (bytes.length > 10000) {
            throw new RentalException("Request is too large", 413);
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static String getToken(HttpExchange exchange) {
        return exchange.getRequestHeaders().getFirst("X-Auth-Token");
    }

    public static void sendJson(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    public static void sendOk(HttpExchange exchange, String message, String dataJson) throws IOException {
        String data = (dataJson == null) ? "null" : dataJson;
        sendJson(exchange, 200, "{\"success\":true,\"message\":" + quote(message) + ",\"data\":" + data + "}");
    }

    public static void sendOk(HttpExchange exchange, String message) throws IOException {
        sendOk(exchange, message, null);
    }

    public static void sendError(HttpExchange exchange, int status, String message) throws IOException {
        sendJson(exchange, status, "{\"success\":false,\"message\":" + quote(message) + "}");
    }

    public static String listToJson(java.util.ArrayList<? extends Storable> list) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                json.append(",");
            }
            json.append(list.get(i).toJson());
        }
        return json.append("]").toString();
    }

    public static String mapToJson(Map<String, Object> map) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                json.append(",");
            }
            first = false;
            Object value = entry.getValue();
            json.append(quote(entry.getKey())).append(":");
            json.append(value instanceof String ? quote((String) value) : String.valueOf(value));
        }
        return json.append("}").toString();
    }
}
