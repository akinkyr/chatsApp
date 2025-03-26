import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

public class PeerServer implements Runnable {
    private ServerSocket SERVER_SOCKET;
    private HashSet<ClientHandler> CLIENTS = new HashSet<>();

    public PeerServer(int port) {
        try {
            SERVER_SOCKET = new ServerSocket(port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        System.out.println("[-] Listening on " + SERVER_SOCKET.getLocalPort());
        try {
            while (CLIENTS.size() < 5) {
                Socket client = SERVER_SOCKET.accept();
                System.out.println("[-] Accepted new user : " + client.getInetAddress());
                ClientHandler clientHandler = new ClientHandler(client);
                CLIENTS.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void broadcast(String msg, ClientHandler client) throws Exception {
        System.out.println(msg);
        for (ClientHandler c : CLIENTS) {
            if (c != client) {
                c.send(msg);
            }
        }
    }

    private class ClientHandler implements Runnable {
        private Socket socket;
        private ObjectInputStream in;
        private ObjectOutputStream out;
        private boolean runs = true;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                String msg;
                while ((msg = (String) in.readObject()) != null && runs) {
                    if (msg.equals("/exit")) {
                        endConnection();
                    }
                    broadcast(msg, this);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void endConnection() throws Exception {
            in.close();
            out.close();
            socket.close();
            CLIENTS.remove(this);
            runs = false;
        }

        public void send(Object obj) throws Exception {
            out.writeObject(obj);
            out.flush();
        }
    }
}
