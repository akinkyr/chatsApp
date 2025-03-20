import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class Server implements Runnable {

    private int PORT;
    private Set<ClientHandler> CLIENTS = new HashSet<>();
    private ServerSocket SERVER_SOCKET;

    public Server(int PORT) {
        this.PORT = PORT;
    }



    //Main Fonksiyonu
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
        private PrintWriter OUT;
        private BufferedReader IN;

        public ClientHandler(Socket CLIENT) {
            this.CLIENT = CLIENT;
        }

        @Override
        public void run() {
            try {
                OUT = new PrintWriter(CLIENT.getOutputStream(), true);
                IN = new BufferedReader(new InputStreamReader(CLIENT.getInputStream()));

                OUT.println("[SERVER] : Please enter your username.");
                USERNAME = IN.readLine();

                OUT.println("[SERVER] : you are connected to chat.");
                broadcast("[SERVER] : " + USERNAME + " has connected to chat.", this);
                System.out.println("User '" + USERNAME + "' has connected to chat.");

                String message;
                while ((message = IN.readLine()) != null) {
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
            }
        }

        public void sendMessage(String message) {
            OUT.println(message);
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
