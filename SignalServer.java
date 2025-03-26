import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

public class SignalServer implements Runnable {

    public static ObjectInputStream IN;
    public static ObjectOutputStream OUT;
    public static int PORT = -1;
    public static int MAX_CLIENTS = 5;

    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-")) {
                switch (args[i]) {
                    case "-h":
                    case "--help":
                        System.out.println("Flag\tShort\tDescription\t\t\tDefault");
                        System.out.println("--port\t-p\tSpecify port to be used.\tNone");
                        System.out.println("--count\t-c\tMax number of accepted clients.\t5");
                        System.exit(0);
                        break;
                    case "-p":
                    case "--port":
                        PORT = Integer.parseInt(args[++i]);
                        break;
                    case "-c":
                    case "--count":
                        MAX_CLIENTS = Integer.parseInt(args[++i]);
                        break;
                }
            } else {
                System.out.println("Unknown flag, use '--help' to see the valid flags.");
                System.exit(1);
                break;
            }
        }
        if (PORT == -1) {
            System.out.println("Port wasn't specified, use '--port' to set the port.");
            System.exit(1);
        }

        new Thread(new SignalServer()).start();
    }

    @Override
    public void run() {
        System.out.println("[-] Listening on " + PORT);
        try (DatagramSocket serverSocket = new DatagramSocket(PORT)) {
            ConcurrentHashMap<String, InetSocketAddress> clients = new ConcurrentHashMap<>();
            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                serverSocket.receive(packet);

                String msg = new String(packet.getData());

                InetSocketAddress senderAddr = new InetSocketAddress(packet.getAddress(), packet.getPort());

                System.out.println("[-] " + senderAddr + " : " + msg);

                String[] clientArgs = msg.split(" ");
                if (msg.startsWith("REG")) {
                    clients.put(clientArgs[1], senderAddr);
                    System.out.println("[-] REG : " + senderAddr + " -> " + clientArgs[1]);
                } else if (msg.startsWith("REQ")) {
                    InetSocketAddress targetAddr = clients.get(clientArgs[1]);

                    if (targetAddr != null) {
                        String response = "ANS " + targetAddr.getAddress().getHostAddress() + " "
                                + targetAddr.getPort();
                        byte[] responseData = response.getBytes();
                        DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length,
                                senderAddr);
                        serverSocket.send(responsePacket);
                        System.out.println("[-] REQ : " + clientArgs[1] + " -> " + response);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
