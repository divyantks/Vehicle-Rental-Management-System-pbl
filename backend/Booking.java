import java.time.LocalDate;

public class Booking implements Storable {

    public static final String BOOKED = "BOOKED";
    public static final String CANCELLED = "CANCELLED";
    public static final String RETURNED = "RETURNED";

    private final String id;
    private final String customerId;
    private final String vehicleId;
    private final String startDate;    
    private final int days;
    private final double totalCost;
    private String status;              


    private String customerName = "";
    private String vehicleName = "";


    public Booking(String id, String customerId, String vehicleId, String startDate, int days, double totalCost) {
        this(id, customerId, vehicleId, startDate, days, totalCost, BOOKED);
    }


    public Booking(String id, String customerId, String vehicleId, String startDate,int days, double totalCost, String status) {
        this.id = id;
        this.customerId = customerId;
        this.vehicleId = vehicleId;
        this.startDate = startDate;
        this.days = days;
        this.totalCost = totalCost;
        this.status = status;
    }

    public String getId()         { return id; }
    public String getCustomerId() { return customerId; }
    public String getVehicleId()  { return vehicleId; }
    public String getStartDate()  { return startDate; }
    public int getDays()          { return days; }
    public double getTotalCost()  { return totalCost; }
    public String getStatus()     { return status; }
    public boolean isBooked()     { return BOOKED.equals(status); }

    public LocalDate getStartLocalDate() { return LocalDate.parse(startDate); }
    public LocalDate getEndLocalDate()   { return getStartLocalDate().plusDays(days); }

    public boolean isOwnedBy(String userId) { return customerId.equals(userId); }

    public void setCustomerName(String name) { this.customerName = name; }
    public void setVehicleName(String name)  { this.vehicleName = name; }


    public void cancel() {
        if (!isBooked()) {
            throw new RentalException("Only a BOOKED booking can be cancelled (this one is " + status + ")", 409);
        }
        status = CANCELLED;
    }


    public void markReturned() {
        if (!isBooked()) {
            throw new RentalException("Only a BOOKED booking can be returned (this one is " + status + ")", 409);
        }
        status = RETURNED;
    }

    @Override
    public String toFileString() {
        return id + "|" + customerId + "|" + vehicleId + "|" + startDate + "|" + days + "|" + totalCost + "|" + status;
    }

    @Override
    public String toJson() {
        return "{\"id\":" + HttpHelper.quote(id)
                + ",\"customerId\":" + HttpHelper.quote(customerId)
                + ",\"customerName\":" + HttpHelper.quote(customerName)
                + ",\"vehicleId\":" + HttpHelper.quote(vehicleId)
                + ",\"vehicleName\":" + HttpHelper.quote(vehicleName)
                + ",\"startDate\":" + HttpHelper.quote(startDate)
                + ",\"endDate\":" + HttpHelper.quote(getEndLocalDate().toString())
                + ",\"days\":" + days
                + ",\"totalCost\":" + totalCost
                + ",\"status\":" + HttpHelper.quote(status) + "}";
    }
}
