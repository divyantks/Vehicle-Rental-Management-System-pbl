import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class FileManager {

    private final String folder;

    public FileManager(String folder) {
        this.folder = folder;
    }

    private File file(String name) {
        return new File(folder, name);
    }


    public void prepareFiles() {
        new File(folder).mkdirs();
        boolean firstRun = !file("vehicles.txt").exists();
        try {
            for (String name : new String[]{"users.txt", "vehicles.txt", "bookings.txt", "rentals.txt"}) {
                file(name).createNewFile();
            }
        } catch (IOException e) {
            throw new RentalException("Could not create data files", 500);
        }

        if (file("users.txt").length() == 0) {
            ArrayList<User> users = new ArrayList<>();
            users.add(new Admin("U1", "System Admin", "admin@rental.com", "9000000000",
                    User.hashPassword("admin123")));
            saveUsers(users);
        }
        if (firstRun) {
            ArrayList<Vehicle> sample = new ArrayList<>();
            sample.add(new Vehicle("V101", "Toyota", "Innova", "Car", 2500));
            sample.add(new Vehicle("V102", "Maruti", "Swift", "Car", 1500));
            sample.add(new Vehicle("V103", "Hyundai", "Creta", "SUV", 2200));
            sample.add(new Vehicle("V104", "Royal Enfield", "Classic 350", "Bike", 900));
            sample.add(new Vehicle("V105", "Honda", "Activa", "Scooter", 500));
            saveVehicles(sample);
        }
    }

    private ArrayList<String[]> readRows(String fileName, int fieldCount) {
        ArrayList<String[]> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file(fileName)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split("\\|", -1);   
                if (parts.length == fieldCount) {
                    rows.add(parts);
                } else {
                    System.out.println("Skipping damaged line in " + fileName + ": " + line);
                }
            }
        } catch (IOException e) {
            throw new RentalException("Could not read a data file", 500);
        }
        return rows;
    }

    public ArrayList<User> loadUsers() {
        ArrayList<User> list = new ArrayList<>();
        for (String[] r : readRows("users.txt", 7)) {
            if (r[1].equals("ADMIN")) {
                list.add(new Admin(r[0], r[2], r[3], r[4], r[5]));
            } else {
                list.add(new Customer(r[0], r[2], r[3], r[4], r[5], r[6]));
            }
        }
        return list;
    }

    public ArrayList<Vehicle> loadVehicles() {
        ArrayList<Vehicle> list = new ArrayList<>();
        for (String[] r : readRows("vehicles.txt", 6)) {
            try {
                list.add(new Vehicle(r[0], r[1], r[2], r[3], Double.parseDouble(r[4]), Boolean.parseBoolean(r[5])));
            } catch (IllegalArgumentException e) {
                System.out.println("Skipping vehicle with bad number: " + r[0]);
            }
        }
        return list;
    }

    public ArrayList<Booking> loadBookings() {
        ArrayList<Booking> list = new ArrayList<>();
        for (String[] r : readRows("bookings.txt", 7)) {
            try {
                list.add(new Booking(r[0], r[1], r[2], r[3], Integer.parseInt(r[4]), Double.parseDouble(r[5]), r[6]));
            } catch (NumberFormatException e) {
                System.out.println("Skipping booking with bad number: " + r[0]);
            }
        }
        return list;
    }

    public ArrayList<Rental> loadRentals() {
        ArrayList<Rental> list = new ArrayList<>();
        for (String[] r : readRows("rentals.txt", 7)) {
            try {
                list.add(new Rental(r[0], r[1], r[2], r[3], r[4], Integer.parseInt(r[5]), Double.parseDouble(r[6])));
            } catch (NumberFormatException e) {
                System.out.println("Skipping rental with bad number: " + r[0]);
            }
        }
        return list;
    }

    public void saveUsers(ArrayList<User> list)       { save("users.txt", list); }
    public void saveVehicles(ArrayList<Vehicle> list) { save("vehicles.txt", list); }
    public void saveBookings(ArrayList<Booking> list) { save("bookings.txt", list); }
    public void saveRentals(ArrayList<Rental> list)   { save("rentals.txt", list); }

    private void save(String fileName, ArrayList<? extends Storable> list) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file(fileName)))) {
            for (Storable item : list) {
                writer.write(item.toFileString());   
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RentalException("Could not write a data file", 500);
        }
    }
}
