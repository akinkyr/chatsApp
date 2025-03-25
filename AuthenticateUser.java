import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class AuthenticateUser {
    private HashMap<String, String> logininfo = new HashMap<>();

    public AuthenticateUser() {
        loadCredentials();
        reloadCredentials();

    }
    public void reloadCredentials() {
        logininfo.clear();
        loadCredentials();
    }

    private void loadCredentials() {
        Path path = Paths.get("login.ser");
        if (Files.exists(path)) {
            try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(path))) {
                logininfo = (HashMap<String, String>) in.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            logininfo = new HashMap<>();
        }
    }


    public boolean authenticate(String username, String password) {
        if (!logininfo.containsKey(username)) {
            return false;
        }
        String storedHash = logininfo.get(username);
        String hashedInput = PasswordHasher.hashPassword(password);
        return hashedInput != null && hashedInput.equals(storedHash);
    }

    public boolean authenticate(String username) {
        if (logininfo.containsKey(username)) {
            return false;
        }
        else
            return true;
    }

    protected HashMap<String, String> getLogininfo() {
        return logininfo;
    }
}


