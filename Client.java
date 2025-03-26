import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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

        try (DatagramSocket socket = new DatagramSocket(PORT)) {

            PORT = socket.getLocalPort();

            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            System.out.println("Choose an option\n- 'Host'\n- 'Connect'");
            String choice = scanner.nextLine();

            String peerId;

            if (choice.equalsIgnoreCase("host")) {
                System.out.println("Enter your ID.");
                String id = scanner.nextLine();

                sendSignal(socket, "REG " + id);

                PeerServer peerServer = new PeerServer(PORT);
                peerServer.run();

            } else if (choice.equalsIgnoreCase("connect")) {
                System.out.println("Enter ID of the peer to establish connection.");
                peerId = new String(scanner.nextLine());

                sendSignal(socket, "REQ " + peerId);

                socket.receive(packet);
                String response = new String(packet.getData(), 0, packet.getLength());
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
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        scanner.close();
    }

    public static void sendSignal(DatagramSocket socket, String msg) throws Exception {
        byte[] data = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(SERVER_IP), SERVER_PORT);
        socket.send(packet);
    }
}
