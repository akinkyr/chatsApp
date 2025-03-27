import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class SignalServer implements Runnable {

    private static int PORT = -1;
    private static int MAX_CLIENTS = 5;
    private ConcurrentHashMap<String, String> CLIENT_TABLE = new ConcurrentHashMap<>();
    private HashSet<ClientHandler> CLIENTS = new HashSet<>();

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

        SignalServer server = new SignalServer();
        server.run();
    }

    @Override
    public void run() {
        System.out.println("[-] Listening on " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (CLIENTS.size() < MAX_CLIENTS) {
                Socket client = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(client);
                CLIENTS.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class ClientHandler implements Runnable {
        private Socket socket;
        private ObjectInputStream in;
        private ObjectOutputStream out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                out = new ObjectOutputStream(this.socket.getOutputStream());
                in = new ObjectInputStream(this.socket.getInputStream());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                while (true) {
                    String user = (String) in.readObject();
                    if (user.equals("END")) {
                        end();
                        break;
                    }
                    String[] userTask = user.split(" ");
                    if (userTask[0].equals("REG")) {
                        String addr = socket.getInetAddress().getHostAddress() + " " + socket.getPort();
                        CLIENT_TABLE.put(userTask[1], addr);
                        System.out.println("[-] REG : " + userTask[1] + " <-> " + addr);
                    } else if (userTask[0].equals("REQ")) {
                        String addr = CLIENT_TABLE.get(userTask[1]);
                        out.writeObject("ANS " + addr);
                        out.flush();
                        System.out.println("[-] REQ : " + userTask[1] + " <-> " + addr);
                    } else {
                        System.out.println("[!] Invalid option received.");

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        public void end() throws Exception {
            in.close();
            out.close();
            CLIENTS.remove(this);
            socket.close();

        }
    }
}
