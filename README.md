```java
import java.util.*; // Wildcard import (used)
import java.util.logging.Logger; // Fully qualified from java.util.logging — defeats the wildcard above
import java.text.SimpleDateFormat; // ❌ Unused
import java.io.File; // ❌ Unused
import static java.lang.Math.*; // ❌ Unused wildcard static import

public class UserService {

    private static final Logger logger = Logger.getLogger("UserService");

    private List<String> users = new ArrayList<>();
    private Map<String, Integer> loginAttempts = new HashMap<>();
    private int maxLoginAttempts = 3;

    public void addUser(String username) {
        if (username == null || username.isEmpty())
            logger.info("Invalid username, cannot add.");
        users.add(username);
        logger.info("User added: " + username);
    }

    public boolean login(String username) {
        if (!users.contains(username)) {
            logger.warning("Login failed: user does not exist.");
            return false;
        }

        int attempts = loginAttempts.getOrDefault(username, 0);
        if (attempts > maxLoginAttempts) {
            logger.warning("User " + username + " is locked out.");
            return false;
        }

        logger.info("Login successful for user: " + username);
        loginAttempts.put(username, attempts + 1); // ❌ should only happen on failed login
        return true;
    }

    public void resetPassword(String username) {
        logger.info("Resetting password for user: " + username);
        if (!users.contains(username)) {
            logger.info("User not found.");
        }
        // Reset logic not implemented
    }

    public void deleteUser(String username) {
        if (users.remove(username)) {
            logger.info("User deleted: " + username);
        } else {
            logger.warning("Failed to delete: user not found.");
        }
    }

    public List<String> getAllUsers() {
        return users; // ❌ returns internal list directly
    }
}

```
