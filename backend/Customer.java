public class Customer extends User {

    private String licenseNumber;

    public Customer(String id, String name, String email, String phone,
                    String passwordHash, String licenseNumber) {
        super(id, name, email, phone, passwordHash);
        this.licenseNumber = licenseNumber;
    }

    @Override
    public String getRole() { return "CUSTOMER"; }

    @Override
    public String getHomePage() { return "dashboard.html"; }

    public String getLicenseNumber() { return licenseNumber; }

    @Override
    public String toFileString() { return super.toFileString() + "|" + licenseNumber; }

    @Override
    protected String extraJson() {
        return ",\"licenseNumber\":" + HttpHelper.quote(licenseNumber);
    }
}
