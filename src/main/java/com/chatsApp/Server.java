package com.chatsApp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import com.chatsApp.security.Encryption;

public class Server implements Runnable {

    private int PORT;
    private Set<ClientHandler> CLIENTS = new HashSet<>();
    private ServerSocket SERVER_SOCKET;

    public Server(int PORT) {
        this.PORT = PORT;
    }

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
        private ObjectOutputStream OUT;
        private ObjectInputStream IN;
        private Encryption ENCRYPTION;

        public ClientHandler(Socket CLIENT) {
            this.CLIENT = CLIENT;
        }

        @Override
        public void run() {
            try {
                OUT = new ObjectOutputStream(CLIENT.getOutputStream());
                IN = new ObjectInputStream(CLIENT.getInputStream());

                // HANDSHAKE

                ENCRYPTION = new Encryption(256)
                        .generateKeys()
                        .sendPublicKey(OUT)
                        .getPeerPublicKey(IN)
                        .getSharedSecret();

                sendMessage("--- ENCRYPTION: ECDH(X25519) + AES-GCM ---");
                sendMessage("[SERVER] : Please enter your username.");
                USERNAME = new String(ENCRYPTION.decrypt((byte[]) IN.readObject()));

                sendMessage("[SERVER] : You are connected to chat.");
                broadcast("[SERVER] : " + USERNAME + " has connected to chat.", this);
                System.out.println("User '" + USERNAME + "' has connected to chat.");

                String message;
                while ((message = new String(ENCRYPTION.decrypt((byte[]) IN.readObject()))) != null) {
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
            } catch (Exception e) {
                e.printStackTrace();
                endConnection();
            }
        }

        public void sendMessage(String message) {
            try {
                OUT.writeObject(ENCRYPTION.encrypt(message.getBytes()));
                OUT.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
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
