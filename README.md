# Vehicle Rental Management System (Java OOP project)

## Run (needs JDK 11+)
From THIS folder (the project root):

    javac -d out backend/*.java
    java -cp out Main

Open http://localhost:8080

Default admin: admin@rental.com / admin123 (created automatically on first run).
Customers register on the Register page. Data is saved in data/*.txt and survives restarts.
To reset everything, stop the server and delete data/*.txt.

## Structure
- frontend/  HTML, CSS, JS (served by the Java server)
- backend/   Core Java (15 files)
- data/      pipe-separated text files

## OOP map (for the viva)
- Encapsulation: private fields everywhere; User has no password-hash getter; Rental has getters only; Booking has no setStatus().
- Inheritance: Admin and Customer extend User (Customer adds licenseNumber).
- Abstraction: abstract class User (getRole, getHomePage); interface Storable (toFileString, toJson).
- Overriding: getRole, getHomePage, toFileString, extraJson (Admin/Customer); toFileString/toJson (Vehicle, Booking, Rental).
- Overloading: Vehicle.calculateCost(days) / (days, lateDays); RentalService.searchVehicles(keyword) / (keyword, type); constructors in Vehicle and Booking; HttpHelper.sendOk.
- Exceptions: RentalException(message, httpStatus). IOException becomes status 500 in FileManager.
- Collections: ArrayList of User/Vehicle/Booking/Rental, HashMap for sessions and form data.
- File handling: FileManager only (BufferedReader/BufferedWriter).

## Rules
- Cancel: only a BOOKED booking, on or before its start date. Return: only a BOOKED booking, on or after its start date.
- Late return: extra days cost 1.5 x daily price. CANCELLED and RETURNED bookings can never change again.
- Admin can't delete a vehicle or change its price while it has an active booking.
