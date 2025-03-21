import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

import security.Encryption;

public class Client implements Runnable {
    public String IP_ADRESS;
    public int PORT = 30000;
    private Socket SOCKET;
    private ObjectInputStream IN;
    private ObjectOutputStream OUT;
    private Encryption ENCRYPTION;

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
            ENCRYPTION = new Encryption(256)
                    .generateKeys()
                    .getPeerPublicKey(IN)
                    .sendPublicKey(OUT)
                    .getSharedSecret();
        } catch (Exception e) {
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
            byte[] encryptedMessage = ENCRYPTION.encrypt(message.getBytes());
            OUT.writeObject(encryptedMessage);
            OUT.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class MessageReceiver extends Thread {
        private volatile boolean running = true;

        public void run() {
            try {
                String incoming;
                while (running && (incoming = new String(ENCRYPTION.decrypt((byte[]) IN.readObject()))) != null) {
                    System.out.println(incoming);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void stopThread() {
            running = false;
        }
    }
}
