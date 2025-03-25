import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
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


    private static final Path PATH = Paths.get("login.ser");

    public static void writeCredentials(String userID, String plainPassword) {
        HashMap<String, String> credentials = new HashMap<>();

        if (Files.exists(PATH)) {
            try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(PATH))) {
                credentials = (HashMap<String, String>) in.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        String hashedPassword = hashPassword(plainPassword);
        credentials.put(userID, hashedPassword);

        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(PATH))) {
            out.writeObject(credentials);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
