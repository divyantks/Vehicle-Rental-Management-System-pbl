public class Rental implements Storable {

    private final String id;
    private final String bookingId;
    private final String customerId;
    private final String vehicleId;
    private final String returnDate;
    private final int lateDays;
    private final double totalCost;

    private String customerName = "";
    private String vehicleName = "";

    public Rental(String id, String bookingId, String customerId, String vehicleId,
                  String returnDate, int lateDays, double totalCost) {
        this.id = id;
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.vehicleId = vehicleId;
        this.returnDate = returnDate;
        this.lateDays = lateDays;
        this.totalCost = totalCost;
    }

    public String getId()         { return id; }
    public String getBookingId()  { return bookingId; }
    public String getCustomerId() { return customerId; }
    public String getVehicleId()  { return vehicleId; }
    public String getReturnDate() { return returnDate; }
    public int getLateDays()      { return lateDays; }
    public double getTotalCost()  { return totalCost; }

    public void setCustomerName(String name) { this.customerName = name; }
    public void setVehicleName(String name)  { this.vehicleName = name; }

    public String makeReceipt() {
        return "===== RENTAL RECEIPT =====\n"
                + "Receipt No  : " + id + "\n"
                + "Booking ID  : " + bookingId + "\n"
                + "Customer    : " + customerName + "\n"
                + "Vehicle     : " + vehicleName + "\n"
                + "Return Date : " + returnDate + "\n"
                + "Late Days   : " + lateDays + "\n"
                + "Total Paid  : Rs. " + String.format("%.2f", totalCost) + "\n"
                + "==========================";
    }

    @Override
    public String toFileString() {
        return id + "|" + bookingId + "|" + customerId + "|" + vehicleId + "|" + returnDate + "|" + lateDays + "|" + totalCost;
    }

    @Override
    public String toJson() {
        return "{\"id\":" + HttpHelper.quote(id)
                + ",\"bookingId\":" + HttpHelper.quote(bookingId)
                + ",\"customerId\":" + HttpHelper.quote(customerId)
                + ",\"customerName\":" + HttpHelper.quote(customerName)
                + ",\"vehicleId\":" + HttpHelper.quote(vehicleId)
                + ",\"vehicleName\":" + HttpHelper.quote(vehicleName)
                + ",\"returnDate\":" + HttpHelper.quote(returnDate)
                + ",\"lateDays\":" + lateDays
                + ",\"totalCost\":" + totalCost
                + ",\"receipt\":" + HttpHelper.quote(makeReceipt()) + "}";
    }
}
