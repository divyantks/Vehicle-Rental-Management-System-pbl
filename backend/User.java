import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class User implements Storable {

    private final String id;
    private String name;
    private String email;
    private String phone;
    private String passwordHash;     

    public User(String id, String name, String email, String phone, String passwordHash) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
    }

    public abstract String getRole();
    public abstract String getHomePage();

    public String getId()    { return id; }
    public String getName()  { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }


    public boolean checkPassword(String plainPassword) {
        if (plainPassword == null) {
            return false;
        }
        return passwordHash.equals(hashPassword(plainPassword));
    }

    public static String hashPassword(String plainPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    @Override
    public String toFileString() {
        return id + "|" + getRole() + "|" + name + "|" + email + "|" + phone + "|" + passwordHash;
    }

    @Override
    public final String toJson() {
        return "{"
                + "\"id\":" + HttpHelper.quote(id)
                + ",\"role\":" + HttpHelper.quote(getRole())
                + ",\"name\":" + HttpHelper.quote(name)
                + ",\"email\":" + HttpHelper.quote(email)
                + ",\"phone\":" + HttpHelper.quote(phone)
                + extraJson()
                + "}";
    }

    protected String extraJson() {
        return "";
    }
}
