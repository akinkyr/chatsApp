import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashSet;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.spec.SecretKeySpec;

public class Server implements Runnable {

    private int PORT;
    private Set<ClientHandler> CLIENTS = new HashSet<>();
    private ServerSocket SERVER_SOCKET;

    public Server(int PORT) {
        this.PORT = PORT;
    }

    public static void main(String[] args) {
        Server server = new Server(30000);
        server.run();
    }

    @Override
    public void run() {
        try {
            SERVER_SOCKET = new ServerSocket(PORT);
            System.out.println("Listening port on " + PORT);
            while (true) {
                while (CLIENTS.size() < 4) {
                    Socket client = SERVER_SOCKET.accept();
                    ClientHandler clientHandler = new ClientHandler(client);
                    CLIENTS.add(clientHandler);
                    new Thread(clientHandler).start();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            endServer();
        }
    }

    public void broadcast(String message, ClientHandler client) {
        for (ClientHandler clientHandler : CLIENTS) {
            if (clientHandler != client) {
                clientHandler.sendMessage(message);
            }
        }
    }

    public void endServer() {
        try {
            if (!SERVER_SOCKET.isClosed()) {
                SERVER_SOCKET.close();
            }
            for (ClientHandler client : CLIENTS) {
                client.endConnection();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class ClientHandler implements Runnable {

        private Socket CLIENT;
        private String USERNAME;
        private ObjectOutputStream OUT;
        private ObjectInputStream IN;
        private SecretKeySpec secretKey;

        public ClientHandler(Socket CLIENT) {
            this.CLIENT = CLIENT;
        }

        @Override
        public void run() {
            try {
                OUT = new ObjectOutputStream(CLIENT.getOutputStream());
                IN = new ObjectInputStream(CLIENT.getInputStream());

                // HANDSHAKE

                try {
                    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
                    keyPairGenerator.initialize(256);
                    KeyPair serverKeyPair = keyPairGenerator.generateKeyPair();
                    PublicKey serverPublicKey = serverKeyPair.getPublic();
                    PrivateKey serverPrivateKey = serverKeyPair.getPrivate();

                    OUT.writeObject(serverPublicKey);
                    OUT.flush();

                    PublicKey clientPublicKey = (PublicKey) IN.readObject();

                    KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");
                    keyAgreement.init(serverPrivateKey);
                    keyAgreement.doPhase(clientPublicKey, true);
                    byte[] sharedSecret = keyAgreement.generateSecret();
                    secretKey = new SecretKeySpec(sharedSecret, 0, 32, "AES");
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                }

                sendMessage("--- ENCRYPTION: ECDH(P-256) + AES-256 ---");
                sendMessage("[SERVER] : Please enter your username.");
                USERNAME = decryptMessage((byte[]) IN.readObject(), secretKey);

                sendMessage("[SERVER] : You are connected to chat.");
                broadcast("[SERVER] : " + USERNAME + " has connected to chat.", this);
                System.out.println("User '" + USERNAME + "' has connected to chat.");

                String message;
                while ((message = decryptMessage((byte[]) IN.readObject(), secretKey)) != null) {
                    if (message.equals("/exit")) {
                        broadcast("[SERVER] : " + USERNAME + " has left.", this);
                        System.out.println("User '" + USERNAME + "' has left.");
                        endConnection();
                        break;
                    }
                    message = "[MESSAGE] [" + USERNAME + "] " + " : " + message;
                    System.out.println(message);
                    broadcast(message, this);
                }
            } catch (IOException e) {
                e.printStackTrace();
                endConnection();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
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

        public void endConnection() {
            try {
                OUT.close();
                IN.close();
                if (!CLIENT.isClosed()) {
                    CLIENT.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            CLIENTS.remove(this);
        }
    }
}
