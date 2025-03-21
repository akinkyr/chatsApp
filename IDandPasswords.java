import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Properties;

public class IDandPasswords {
    private HashMap<String, String> logininfo = new HashMap<>();

    public IDandPasswords() {
        loadCredentials();
        reloadCredentials();

    }
    public void reloadCredentials() {
        logininfo.clear();
        loadCredentials();
    }

    private void loadCredentials() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("login.properties")) {
            props.load(fis);
            for (String key : props.stringPropertyNames()) {
                logininfo.put(key, props.getProperty(key));
            }
        } catch (IOException e) {
            e.printStackTrace();
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


