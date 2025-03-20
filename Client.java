import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Scanner;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.spec.SecretKeySpec;

public class Client implements Runnable {
    public String IP_ADRESS;
    public int PORT = 30000;
    private Socket SOCKET;
    private ObjectInputStream IN;
    private ObjectOutputStream OUT;
    private SecretKeySpec secretKey;

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

    @Override
    public void run() {

        // Connnection Adress
        Scanner scanner = new Scanner(System.in);
        System.out.println("[CLIENT] : Enter the IP adress of the server");
        IP_ADRESS = scanner.nextLine();
        System.out.println("[CLIENT] : Enter the port of the server");
        PORT = scanner.nextInt();
        scanner.nextLine();

        // Setup connection
        try {
            this.SOCKET = new Socket(IP_ADRESS, PORT);
            IN = new ObjectInputStream(SOCKET.getInputStream());
            OUT = new ObjectOutputStream(SOCKET.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            endConnection();
        }

        // Handshake

        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
            keyPairGenerator.initialize(256);
            KeyPair clientKeyPair = keyPairGenerator.generateKeyPair();
            PublicKey ClientPublicKey = clientKeyPair.getPublic();
            PrivateKey ClientPrivateKey = clientKeyPair.getPrivate();

            PublicKey serverPublicKey = (PublicKey) IN.readObject();

            OUT.writeObject(ClientPublicKey);
            OUT.flush();

            KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");
            keyAgreement.init(ClientPrivateKey);
            keyAgreement.doPhase(serverPublicKey, true);
            byte[] sharedSecret = keyAgreement.generateSecret();

            secretKey = new SecretKeySpec(sharedSecret, 0, 32, "AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("[CLIENT] Handshake successful");

        // Receive messages

        MessageReceiver receive = new MessageReceiver();
        receive.start();

        // Send messages
        String message;
        while ((message = scanner.nextLine()) != null) {
            sendMessage(message);
            if (message.equals("/exit")) {
                receive.stopThread();
                try {
                    receive.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            }
            System.out.println("[CLIENT] [SENT] : " + message);
        }
        endConnection();
        scanner.close();
    }

    public void endConnection() {
        try {
            if (!SOCKET.isClosed()) {
                SOCKET.close();
            }
            IN.close();
            OUT.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        try {
            byte[] encryptedMessage = encryptMessage(message, secretKey);
            OUT.writeObject(encryptedMessage);
            OUT.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] encryptMessage(String message, SecretKeySpec secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(message.getBytes());
    }

    public String decryptMessage(byte[] encryptedMessage, SecretKeySpec secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(encryptedMessage);
        return new String(decryptedBytes);
    }

    class MessageReceiver extends Thread {
        private volatile boolean running = true;

        public void run() {
            try {
                String incoming;
                while (running && (incoming = decryptMessage((byte[]) IN.readObject(), secretKey)) != null) {
                    System.out.println(incoming);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void stopThread() {
            running = false;
        }
    }
}
