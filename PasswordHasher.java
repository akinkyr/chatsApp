import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

public class PasswordHasher {
    // This method is essentially the same as in your IDandPasswords class.
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(password.getBytes());
            byte[] digest = md.digest();
            // Convert the byte array to a hexadecimal string
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;


    }


    public static void writeCredentials(String userID, String plainPassword) {
        Properties props = new Properties();
        String hashedPassword = hashPassword(plainPassword);
        props.setProperty(userID, hashedPassword);
        try (OutputStream out = Files.newOutputStream(Paths.get("login.properties"))) {
            props.store(out, "Login credentials");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }





    public static void main(String[] args) {

    }
}
