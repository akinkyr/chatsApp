import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class PeerClient implements Runnable {
    private ObjectInputStream IN;
    private ObjectOutputStream OUT;
    private Socket SOCKET;

    public PeerClient(String ip, int port) {
        try {
            SOCKET = new Socket(ip, port);
            IN = new ObjectInputStream(SOCKET.getInputStream());
            OUT = new ObjectOutputStream(SOCKET.getOutputStream());
            OUT.flush();
        } catch (Exception e) {
            System.out.println("Error while creating PeerClient: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        System.out.println("[-] Started client.");

        // MESSAGE RECEIVING THREAD

        try {
            Thread messageReceiver = new Thread(() -> {
                try {
                    String msg;
                    while ((msg = (String) IN.readObject()) != null) {
                        System.out.println(msg);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            messageReceiver.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // SENDING MESSAGE

        Scanner scanner = new Scanner(System.in);
        String input;

        try {
            while ((input = scanner.nextLine()) != null) {
                if (input.equals("/exit")) {
                    System.out.println("[-] Exiting");
                    System.exit(0);
                }
                OUT.writeObject(input);
                OUT.flush();
                System.out.println("[-] Sent : " + input);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        scanner.close();
    }
}
