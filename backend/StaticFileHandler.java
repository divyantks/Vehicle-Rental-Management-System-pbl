import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StaticFileHandler implements HttpHandler {

    private final Path root;

    public StaticFileHandler(String folder) {
        this.root = Paths.get(folder).toAbsolutePath().normalize();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/")) {
            path = "/index.html";
        }
        try {
            Path file = root.resolve(path.substring(1)).normalize();
            // startsWith(root) blocks tricks like /../../data/users.txt
            if (exchange.getRequestMethod().equals("GET") && file.startsWith(root) && Files.isRegularFile(file)) {
                byte[] data = Files.readAllBytes(file);
                exchange.getResponseHeaders().set("Content-Type", contentType(file.toString()));
                exchange.sendResponseHeaders(200, data.length);
                try (OutputStream out = exchange.getResponseBody()) {
                    out.write(data);
                }
                return;
            }
        } catch (RuntimeException e) {
        }
        byte[] message = "404 - Page not found".getBytes();
        exchange.sendResponseHeaders(404, message.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(message);
        }
    }

    private String contentType(String name) {
        if (name.endsWith(".html")) return "text/html; charset=UTF-8";
        if (name.endsWith(".css"))  return "text/css; charset=UTF-8";
        if (name.endsWith(".js"))   return "application/javascript; charset=UTF-8";
        return "application/octet-stream";
    }
}
