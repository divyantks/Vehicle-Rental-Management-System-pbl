import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws IOException {
        RentalService service = new RentalService(new FileManager("data"));
        SessionManager sessions = new SessionManager();

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api/", new ApiHandler(service, sessions));
        server.createContext("/", new StaticFileHandler("frontend"));
        server.start();   

        System.out.println("Vehicle Rental System is running.");
        System.out.println("Open http://localhost:8080 in your browser. Press Ctrl+C to stop.");
    }
}
