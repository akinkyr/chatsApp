import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;

public class Client {
    public static int PORT;
    public static int SERVER_PORT;
    public static String SERVER_IP;

    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-")) {
                switch (args[i]) {
                    case "-h":
                    case "--help":
                        System.out.println("Flag\tShort\tDescription\t\t\tDefault");
                        System.exit(0);
                        break;
                    case "-a":
                    case "--adress":
                        String[] addr = args[++i].split(":");
                        SERVER_IP = addr[0];
                        SERVER_PORT = Integer.parseInt(addr[1]);
                        break;
                    case "-p":
                    case "--port":
                        PORT = Integer.parseInt(args[++i]);
                        break;

                }
            } else {
                System.out.println("Unknown flag, use '--help' to see the valid flags.");
                System.exit(1);
                break;
            }
        }

        Scanner scanner = new Scanner(System.in);

        if (PORT == 0) {
            Random random = new Random();
            PORT = random.nextInt(50000, 60000);
        }

        try {

            System.out.println("Choose an option\n- 'Host'\n- 'Connect'");
            String choice = scanner.nextLine();

            String peerId;

            Socket socket = new Socket(SERVER_IP, SERVER_PORT);

            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

            if (choice.equalsIgnoreCase("host")) {
                System.out.println("Enter your ID.");
                String id = scanner.nextLine();

                sendSignal("REG " + id + " " + socket.getInetAddress().toString().substring(1) + " " + PORT, out);

                PeerServer peerServer = new PeerServer(PORT);
                peerServer.run();

            } else if (choice.equalsIgnoreCase("connect")) {
                System.out.println("Enter ID of the peer to establish connection.");
                peerId = new String(scanner.nextLine());

                sendSignal("REQ " + peerId, out);

                String response = (String) in.readObject();
                if (response.startsWith("ANS")) {
                    String[] info = response.split(" ");
                    String peerIp = info[1];
                    int peerPort = Integer.parseInt(info[2]);

                    System.out.println("[-] Received Adress : " + peerIp + ":" + peerPort);
                    PeerClient peerClient = new PeerClient(peerIp, peerPort);
                    peerClient.run();
                } else {
                    System.out.println("[!] Invalid answer from Signal Server.");
                    System.exit(1);
                }
            } else {
                System.out.println("[!] Invalid option specified.");
                System.exit(1);
            }
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        scanner.close();
    }

    public static void sendSignal(String msg, ObjectOutputStream out) throws Exception {
        out.writeObject(msg);
        out.flush();
    }
}
