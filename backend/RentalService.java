import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

public class RentalService {

    private static final List<String> VEHICLE_TYPES = Arrays.asList("Car", "SUV", "Bike", "Scooter", "Van");

    private final FileManager fileManager;
    private final ArrayList<User> users;
    private final ArrayList<Vehicle> vehicles;
    private final ArrayList<Booking> bookings;
    private final ArrayList<Rental> rentals;

    private int lastUserNo = 0;
    private int lastVehicleNo = 100;
    private int lastBookingNo = 1000;
    private int lastRentalNo = 5000;

    public RentalService(FileManager fileManager) {
        this.fileManager = fileManager;
        fileManager.prepareFiles();
        users = fileManager.loadUsers();
        vehicles = fileManager.loadVehicles();
        bookings = fileManager.loadBookings();
        rentals = fileManager.loadRentals();

        for (User u : users)       lastUserNo = Math.max(lastUserNo, numberPart(u.getId()));
        for (Vehicle v : vehicles) lastVehicleNo = Math.max(lastVehicleNo, numberPart(v.getId()));
        for (Booking b : bookings) lastBookingNo = Math.max(lastBookingNo, numberPart(b.getId()));
        for (Rental r : rentals)   lastRentalNo = Math.max(lastRentalNo, numberPart(r.getId()));
    }


    private int numberPart(String id) {
        try {
            return Integer.parseInt(id.substring(1));
        } catch (RuntimeException e) {
            return 0;
        }
    }

    private String clean(String value, String label, int maxLength) {
        if (value == null || value.trim().isEmpty()) {
            throw new RentalException(label + " is required");
        }
        String text = value.trim();
        if (text.length() > maxLength) {
            throw new RentalException(label + " must be at most " + maxLength + " characters");
        }
        if (text.contains("|") || text.contains("\n") || text.contains("\r")) {
            throw new RentalException(label + " contains invalid characters");
        }
        return text;
    }

    private void requireAdmin(User actor) {
        if (actor == null || !actor.getRole().equals("ADMIN")) {
            throw new RentalException("Only an admin can do this", 403);
        }
    }

    private void requireCustomer(User actor) {
        if (actor == null || !actor.getRole().equals("CUSTOMER")) {
            throw new RentalException("Only a customer can do this", 403);
        }
    }

    private LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(clean(text, "Start date", 10));
        } catch (DateTimeParseException e) {
            throw new RentalException("Date must look like 2026-12-31");
        }
    }

    private int parseDays(String text) {
        try {
            int days = Integer.parseInt(clean(text, "Number of days", 3));
            if (days < 1 || days > 30) {
                throw new RentalException("Days must be between 1 and 30");
            }
            return days;
        } catch (NumberFormatException e) {
            throw new RentalException("Number of days must be a whole number");
        }
    }

    private double parsePrice(String text) {
        try {
            double price = Double.parseDouble(clean(text, "Price per day", 10));
            if (!(price > 0 && price <= 100000)) {
                throw new RentalException("Price must be between 1 and 100000");
            }
            return price;
        } catch (NumberFormatException e) {
            throw new RentalException("Price must be a number");
        }
    }

    private String checkType(String text) {
        String type = clean(text, "Type", 20);
        for (String allowed : VEHICLE_TYPES) {
            if (allowed.equalsIgnoreCase(type)) {
                return allowed;
            }
        }
        throw new RentalException("Type must be one of: " + String.join(", ", VEHICLE_TYPES));
    }

    public Customer register(String name, String email, String phone, String license, String password) {
        name = clean(name, "Name", 50);
        email = clean(email, "Email", 60).toLowerCase();
        phone = clean(phone, "Phone", 10);
        license = clean(license, "Licence number", 30);
        if (!email.matches("[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+")) {
            throw new RentalException("Email format is not valid");
        }
        if (!phone.matches("\\d{10}")) {
            throw new RentalException("Phone must be exactly 10 digits");
        }
        if (password == null || password.length() < 6 || password.length() > 50) {
            throw new RentalException("Password must be 6 to 50 characters");
        }
        if (findUserByEmail(email) != null) {
            throw new RentalException("This email is already registered", 409);
        }

        Customer customer = new Customer("U" + (++lastUserNo), name, email, phone,
                User.hashPassword(password), license);
        users.add(customer);
        fileManager.saveUsers(users);
        return customer;
    }

    public User login(String email, String password) {
        User user = (email == null) ? null : findUserByEmail(email.trim());
        if (user == null || !user.checkPassword(password)) {
            throw new RentalException("Invalid email or password", 401);   // same message for both cases
        }
        return user;
    }

    private User findUserByEmail(String email) {
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                return u;
            }
        }
        return null;
    }

    private String userName(String id) {
        for (User u : users) {
            if (u.getId().equals(id)) {
                return u.getName();
            }
        }
        return "(unknown)";
    }

    public ArrayList<Vehicle> getAllVehicles() {
        return new ArrayList<>(vehicles);
    }

    public ArrayList<Vehicle> searchVehicles(String keyword) {
        return searchVehicles(keyword, "");
    }

    public ArrayList<Vehicle> searchVehicles(String keyword, String type) {
        String word = (keyword == null) ? "" : keyword.trim().toLowerCase();
        String wantedType = (type == null) ? "" : type.trim();
        ArrayList<Vehicle> result = new ArrayList<>();
        for (Vehicle v : vehicles) {
            boolean wordMatches = (v.getBrand() + " " + v.getModel()).toLowerCase().contains(word);
            boolean typeMatches = wantedType.isEmpty() || v.getType().equalsIgnoreCase(wantedType);
            if (wordMatches && typeMatches) {
                result.add(v);
            }
        }
        return result;
    }

    public Vehicle findVehicle(String id) {
        for (Vehicle v : vehicles) {
            if (v.getId().equals(id)) {
                return v;
            }
        }
        throw new RentalException("Vehicle not found", 404);
    }

    private String vehicleName(String id) {
        for (Vehicle v : vehicles) {
            if (v.getId().equals(id)) {
                return v.getBrand() + " " + v.getModel();
            }
        }
        return "(deleted vehicle)";
    }

    private boolean hasActiveBooking(String vehicleId) {
        for (Booking b : bookings) {
            if (b.getVehicleId().equals(vehicleId) && b.isBooked()) {
                return true;
            }
        }
        return false;
    }

    public Vehicle addVehicle(User actor, String brand, String model, String type, String priceText) {
        requireAdmin(actor);
        Vehicle vehicle = new Vehicle("V" + (++lastVehicleNo), clean(brand, "Brand", 30),
                clean(model, "Model", 30), checkType(type), parsePrice(priceText));
        vehicles.add(vehicle);
        fileManager.saveVehicles(vehicles);
        return vehicle;
    }

    public Vehicle updateVehicle(User actor, String id, String brand, String model, String type, String priceText) {
        requireAdmin(actor);
        Vehicle vehicle = findVehicle(id);
        String newBrand = clean(brand, "Brand", 30);
        String newModel = clean(model, "Model", 30);
        String newType = checkType(type);
        double newPrice = parsePrice(priceText);
        if (newPrice != vehicle.getPricePerDay() && hasActiveBooking(id)) {
            throw new RentalException("Cannot change the price while the vehicle is booked", 409);
        }
        vehicle.setBrand(newBrand);
        vehicle.setModel(newModel);
        vehicle.setType(newType);
        vehicle.setPricePerDay(newPrice);
        fileManager.saveVehicles(vehicles);
        return vehicle;
    }

    public void deleteVehicle(User actor, String id) {
        requireAdmin(actor);
        Vehicle vehicle = findVehicle(id);
        if (hasActiveBooking(id)) {
            throw new RentalException("Cannot delete a vehicle that has an active booking", 409);
        }
        vehicles.remove(vehicle);
        fileManager.saveVehicles(vehicles);
    }

    public Vehicle setVehicleAvailability(User actor, String id, boolean available) {
        requireAdmin(actor);
        Vehicle vehicle = findVehicle(id);
        if (available && hasActiveBooking(id)) {
            throw new RentalException("Vehicle has an active booking and cannot be made available", 409);
        }
        vehicle.setAvailable(available);
        fileManager.saveVehicles(vehicles);
        return vehicle;
    }


    public Booking bookVehicle(User actor, String vehicleId, String startDateText, String daysText) {
        requireCustomer(actor);                       // customer id comes from the token user only
        Vehicle vehicle = findVehicle(clean(vehicleId, "Vehicle", 20));
        int days = parseDays(daysText);
        LocalDate start = parseDate(startDateText);
        if (start.isBefore(LocalDate.now())) {
            throw new RentalException("Start date cannot be in the past");
        }
        if (!vehicle.isAvailable()) {
            throw new RentalException("This vehicle is not available", 409);
        }

        Booking booking = new Booking("B" + (++lastBookingNo), actor.getId(), vehicle.getId(),
                start.toString(), days, vehicle.calculateCost(days));
        vehicle.setAvailable(false);
        bookings.add(booking);
        fileManager.saveBookings(bookings);
        fileManager.saveVehicles(vehicles);
        fillNames(booking);
        return booking;
    }

    private Booking findOwnedBooking(User actor, String bookingId) {
        requireCustomer(actor);
        for (Booking b : bookings) {
            if (b.getId().equals(bookingId)) {
                if (!b.isOwnedBy(actor.getId())) {
                    throw new RentalException("This booking belongs to another customer", 403);
                }
                return b;
            }
        }
        throw new RentalException("Booking not found", 404);
    }

    public Booking cancelBooking(User actor, String bookingId) {
        Booking booking = findOwnedBooking(actor, bookingId);
        if (booking.isBooked() && LocalDate.now().isAfter(booking.getStartLocalDate())) {
            throw new RentalException("The rental has already started. Please return the vehicle instead", 409);
        }
        booking.cancel();                            
        freeVehicle(booking.getVehicleId());
        fileManager.saveBookings(bookings);
        fileManager.saveVehicles(vehicles);
        fillNames(booking);
        return booking;
    }

    public Rental returnVehicle(User actor, String bookingId) {
        Booking booking = findOwnedBooking(actor, bookingId);
        LocalDate today = LocalDate.now();
        if (booking.isBooked() && today.isBefore(booking.getStartLocalDate())) {
            throw new RentalException("The rental has not started yet. Cancel the booking instead", 409);
        }
        booking.markReturned();                      

        int lateDays = (int) Math.max(0, ChronoUnit.DAYS.between(booking.getEndLocalDate(), today));
        double cost = findVehicle(booking.getVehicleId()).calculateCost(booking.getDays(), lateDays);

        Rental rental = new Rental("R" + (++lastRentalNo), booking.getId(), actor.getId(),
                booking.getVehicleId(), today.toString(), lateDays, cost);
        freeVehicle(booking.getVehicleId());
        rentals.add(rental);
        fileManager.saveBookings(bookings);
        fileManager.saveVehicles(vehicles);
        fileManager.saveRentals(rentals);
        fillNames(rental);
        return rental;
    }

    private void freeVehicle(String vehicleId) {
        for (Vehicle v : vehicles) {
            if (v.getId().equals(vehicleId)) {
                v.setAvailable(true);
            }
        }
    }

    public ArrayList<Booking> myBookings(User actor) {
        requireCustomer(actor);
        ArrayList<Booking> result = new ArrayList<>();
        for (Booking b : bookings) {
            if (b.isOwnedBy(actor.getId())) {
                fillNames(b);
                result.add(b);
            }
        }
        Collections.reverse(result);                  
        return result;
    }

    public ArrayList<Rental> myRentals(User actor) {
        requireCustomer(actor);
        ArrayList<Rental> result = new ArrayList<>();
        for (Rental r : rentals) {
            if (r.getCustomerId().equals(actor.getId())) {
                fillNames(r);
                result.add(r);
            }
        }
        Collections.reverse(result);
        return result;
    }


    public ArrayList<User> allCustomers(User actor) {
        requireAdmin(actor);
        ArrayList<User> result = new ArrayList<>();
        for (User u : users) {
            if (u.getRole().equals("CUSTOMER")) {
                result.add(u);
            }
        }
        return result;
    }

    public ArrayList<Booking> allBookings(User actor) {
        requireAdmin(actor);
        ArrayList<Booking> result = new ArrayList<>(bookings);
        for (Booking b : result) {
            fillNames(b);
        }
        Collections.reverse(result);
        return result;
    }

    public ArrayList<Rental> allRentals(User actor) {
        requireAdmin(actor);
        ArrayList<Rental> result = new ArrayList<>(rentals);
        for (Rental r : result) {
            fillNames(r);
        }
        Collections.reverse(result);
        return result;
    }

    public LinkedHashMap<String, Object> getDashboard(User actor) {
        requireAdmin(actor);
        int availableCount = 0;
        for (Vehicle v : vehicles) {
            if (v.isAvailable()) availableCount++;
        }
        int activeCount = 0;
        for (Booking b : bookings) {
            if (b.isBooked()) activeCount++;
        }
        double revenue = 0;
        for (Rental r : rentals) {
            revenue += r.getTotalCost();
        }
        LinkedHashMap<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalVehicles", vehicles.size());
        stats.put("availableVehicles", availableCount);
        stats.put("totalCustomers", allCustomers(actor).size());
        stats.put("totalBookings", bookings.size());
        stats.put("activeBookings", activeCount);
        stats.put("totalRentals", rentals.size());
        stats.put("totalRevenue", Math.round(revenue * 100) / 100.0);
        return stats;
    }

    private void fillNames(Booking b) {
        b.setCustomerName(userName(b.getCustomerId()));
        b.setVehicleName(vehicleName(b.getVehicleId()));
    }

    private void fillNames(Rental r) {
        r.setCustomerName(userName(r.getCustomerId()));
        r.setVehicleName(vehicleName(r.getVehicleId()));
    }
}
