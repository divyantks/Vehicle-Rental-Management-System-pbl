import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.HashMap;


public class ApiHandler implements HttpHandler {

    private final RentalService service;
    private final SessionManager sessions;

    public ApiHandler(RentalService service, SessionManager sessions) {
        this.service = service;
        this.sessions = sessions;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String key = method + " " + exchange.getRequestURI().getPath();
            HashMap<String, String> p = method.equals("GET")
                    ? HttpHelper.parseForm(exchange.getRequestURI().getRawQuery())
                    : HttpHelper.parseForm(HttpHelper.readBody(exchange));
            route(exchange, key, p);
        } catch (RentalException e) {
            HttpHelper.sendError(exchange, e.getStatus(), e.getMessage());     // 400, 401, 403, 404, 409, 500...
        } catch (IllegalArgumentException e) {
            HttpHelper.sendError(exchange, 400, "Invalid input: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            HttpHelper.sendError(exchange, 500, "Something went wrong on the server");
        }
    }

    private void route(HttpExchange ex, String key, HashMap<String, String> p) throws IOException {
        if (key.equals("POST /api/register")) {
            // the "role" field is never read: registration always creates a Customer
            Customer c = service.register(p.get("name"), p.get("email"), p.get("phone"),
                    p.get("license"), p.get("password"));
            HttpHelper.sendOk(ex, "Registration successful. Please log in.", c.toJson());
            return;
        }
        if (key.equals("POST /api/login")) {
            User user = service.login(p.get("email"), p.get("password"));
            String token = sessions.createToken(user);
            HttpHelper.sendOk(ex, "Login successful", "{\"token\":" + HttpHelper.quote(token)
                    + ",\"home\":" + HttpHelper.quote(user.getHomePage())
                    + ",\"user\":" + user.toJson() + "}");
            return;
        }

        User actor = sessions.getUser(HttpHelper.getToken(ex));
        if (actor == null) {
            throw new RentalException("Please log in first", 401);
        }

        switch (key) {
            case "POST /api/logout":
                sessions.remove(HttpHelper.getToken(ex));
                HttpHelper.sendOk(ex, "Logged out");
                break;

            case "GET /api/vehicles":
                HttpHelper.sendOk(ex, "OK", HttpHelper.listToJson(
                        service.searchVehicles(p.get("keyword"), p.get("type"))));
                break;
            case "GET /api/vehicles/details":
                HttpHelper.sendOk(ex, "OK", service.findVehicle(p.get("id")).toJson());
                break;
            case "POST /api/vehicles/add":
                HttpHelper.sendOk(ex, "Vehicle added", service.addVehicle(actor, p.get("brand"),
                        p.get("model"), p.get("type"), p.get("price")).toJson());
                break;
            case "POST /api/vehicles/update":
                HttpHelper.sendOk(ex, "Vehicle updated", service.updateVehicle(actor, p.get("id"),
                        p.get("brand"), p.get("model"), p.get("type"), p.get("price")).toJson());
                break;
            case "POST /api/vehicles/delete":
                service.deleteVehicle(actor, p.get("id"));
                HttpHelper.sendOk(ex, "Vehicle deleted");
                break;
            case "POST /api/vehicles/availability":
                HttpHelper.sendOk(ex, "Availability updated", service.setVehicleAvailability(actor,
                        p.get("id"), Boolean.parseBoolean(p.get("available"))).toJson());
                break;

            case "POST /api/bookings/book":
                HttpHelper.sendOk(ex, "Booking confirmed", service.bookVehicle(actor,
                        p.get("vehicleId"), p.get("startDate"), p.get("days")).toJson());
                break;
            case "GET /api/bookings/my":
                HttpHelper.sendOk(ex, "OK", HttpHelper.listToJson(service.myBookings(actor)));
                break;
            case "POST /api/bookings/cancel":
                HttpHelper.sendOk(ex, "Booking cancelled", service.cancelBooking(actor, p.get("bookingId")).toJson());
                break;
            case "POST /api/bookings/return":
                HttpHelper.sendOk(ex, "Vehicle returned", service.returnVehicle(actor, p.get("bookingId")).toJson());
                break;
            case "GET /api/rentals/my":
                HttpHelper.sendOk(ex, "OK", HttpHelper.listToJson(service.myRentals(actor)));
                break;

            case "GET /api/admin/dashboard":
                HttpHelper.sendOk(ex, "OK", HttpHelper.mapToJson(service.getDashboard(actor)));
                break;
            case "GET /api/admin/customers":
                HttpHelper.sendOk(ex, "OK", HttpHelper.listToJson(service.allCustomers(actor)));
                break;
            case "GET /api/admin/bookings":
                HttpHelper.sendOk(ex, "OK", HttpHelper.listToJson(service.allBookings(actor)));
                break;
            case "GET /api/admin/rentals":
                HttpHelper.sendOk(ex, "OK", HttpHelper.listToJson(service.allRentals(actor)));
                break;

            default:
                throw new RentalException("Unknown API endpoint", 404);
        }
    }
}
