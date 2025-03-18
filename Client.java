import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client implements Runnable {
    public String IP_ADRESS;
    public int PORT = 30000;
    private Socket SOCKET;
    private PrintWriter OUT;
    private BufferedReader IN;

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("[CLIENT] : Enter the IP adress of the server");
        IP_ADRESS = scanner.nextLine();

        // Setup connection
        try {
            this.SOCKET = new Socket(IP_ADRESS, PORT);
            IN = new BufferedReader(new InputStreamReader(SOCKET.getInputStream()));
            OUT = new PrintWriter(SOCKET.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
            endConnection();
        }
        // Receive messages

        Thread receiveThread = new Thread(() -> {
            try { // TODO THIS THREAD IS NOT CHECKING WHETHER THE SOCKET HAS CLOSED OR NOT WHEN
                  // EXITING.
                String incoming;
                while ((incoming = IN.readLine()) != null) {
                    System.out.println(incoming);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        receiveThread.start();

        // Send messages
        String message;
        while ((message = scanner.nextLine()) != null) {
            OUT.println(message);
            if (message.equals("/exit")) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
