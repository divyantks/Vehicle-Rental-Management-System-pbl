import java.util.HashMap;
import java.util.UUID;

public class SessionManager {

    private final HashMap<String, User> sessions = new HashMap<>();

    public String createToken(User user) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, user);
        return token;
    }

    public User getUser(String token) {
        if (token == null) {
            return null;
        }
        return sessions.get(token);
    }

    public void remove(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }
}
