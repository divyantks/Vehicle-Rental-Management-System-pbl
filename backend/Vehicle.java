public class Vehicle implements Storable {

    public static final double LATE_FEE_MULTIPLIER = 1.5;

    private final String id;
    private String brand;
    private String model;
    private String type;
    private double pricePerDay;
    private boolean available;

    public Vehicle(String id, String brand, String model, String type, double pricePerDay) {
        this(id, brand, model, type, pricePerDay, true);
    }

    public Vehicle(String id, String brand, String model, String type, double pricePerDay, boolean available) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.type = type;
        setPricePerDay(pricePerDay);
        this.available = available;
    }

    public String getId()          { return id; }
    public String getBrand()       { return brand; }
    public String getModel()       { return model; }
    public String getType()        { return type; }
    public double getPricePerDay() { return pricePerDay; }
    public boolean isAvailable()   { return available; }

    public void setBrand(String brand) { this.brand = brand; }
    public void setModel(String model) { this.model = model; }
    public void setType(String type)   { this.type = type; }
    public void setAvailable(boolean available) { this.available = available; }

    public void setPricePerDay(double pricePerDay) {
        if (!(pricePerDay > 0)) {
            throw new IllegalArgumentException("Price per day must be greater than 0");
        }
        this.pricePerDay = pricePerDay;
    }

    public double calculateCost(int days) {
        return Math.round(days * pricePerDay * 100) / 100.0;
    }

    public double calculateCost(int days, int lateDays) {
        double lateCharge = lateDays * pricePerDay * LATE_FEE_MULTIPLIER;
        return Math.round((calculateCost(days) + lateCharge) * 100) / 100.0;
    }

    @Override
    public String toFileString() {
        return id + "|" + brand + "|" + model + "|" + type + "|" + pricePerDay + "|" + available;
    }

    @Override
    public String toJson() {
        return "{\"id\":" + HttpHelper.quote(id)
                + ",\"brand\":" + HttpHelper.quote(brand)
                + ",\"model\":" + HttpHelper.quote(model)
                + ",\"type\":" + HttpHelper.quote(type)
                + ",\"pricePerDay\":" + pricePerDay
                + ",\"available\":" + available + "}";
    }
}
