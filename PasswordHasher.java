import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

public class PasswordHasher {

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
        Path path = Paths.get("login.properties");

        if (Files.exists(path)) {
            try (InputStream in = Files.newInputStream(path)) {
                props.load(in);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String hashedPassword = hashPassword(plainPassword);
        props.setProperty(userID, hashedPassword);

        try (OutputStream out = Files.newOutputStream(path)) {
            props.store(out, "Login credentials");
        } catch (IOException e) {
            e.printStackTrace();
        }


    }






    public static void main(String[] args) {

    }
}
