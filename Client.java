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
        System.out.println("[CLIENT] : Enter the port of the server");
        PORT = scanner.nextInt();

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

        MessageReceiver receive = new MessageReceiver();
        receive.start();

        // Send messages
        String message;
        while ((message = scanner.nextLine()) != null) {
            OUT.println(message);
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

    class MessageReceiver extends Thread {
        private volatile boolean running = true;

        public void run() {
            try {
                String incoming;
                while (running && (incoming = IN.readLine()) != null) {
                    System.out.println(incoming);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void stopThread() {
            running = false;
        }
    }
}
