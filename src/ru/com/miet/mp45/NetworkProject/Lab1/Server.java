package ru.com.miet.mp45.NetworkProject.Lab1;

import javafx.util.Pair;

import java.io.IOException;
import java.net.*;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by BALALAIKA on 24.09.2015.
 */
public class Server extends Thread {

    private TreeMap<InetSocketAddress, String> clients = null;
    private TreeMap<InetSocketAddress, String> bannedClients = null;

    private DatagramSocket socket = null;
    private int serverPort = 0;
    private boolean isRunning = false;

    public Server(int port) throws SocketException {
        clients = new TreeMap<InetSocketAddress, String>(new Comparator<InetSocketAddress>() {
            @Override
            public int compare(InetSocketAddress o1, InetSocketAddress o2) {
                return o1.getHostString().compareTo(o2.getHostString());
            }
        });
        bannedClients = new TreeMap<InetSocketAddress, String>(new Comparator<InetSocketAddress>() {
            @Override
            public int compare(InetSocketAddress o1, InetSocketAddress o2) {
                return o1.getHostString().compareTo(o2.getHostString());
            }
        });

        socket = new DatagramSocket(port);
        serverPort = port;
        isRunning = true;
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                receiveAndExecuteMessage();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void banClient(InetSocketAddress client) {
        if (clients.containsKey(client)) {
            if (!bannedClients.containsKey(client)) {
                bannedClients.put(client, clients.get(client));
            }
            clients.remove(client);
        }
    }

    public void unbanClient(InetSocketAddress client) {
        if (bannedClients.containsKey(client)) {
            if (!clients.containsKey(client)) {
                clients.put(client, bannedClients.get(client));
            }
            bannedClients.remove(client);
        }
    }

    public void stopServer() {
        isRunning = false;
    }

    private void receiveAndExecuteMessage() throws IOException {
        byte[] bytes = new byte[NetworkMessage.sizeOfMessage];
        DatagramPacket receivePacket = new DatagramPacket(bytes, bytes.length);
        socket.receive(receivePacket);

        NetworkMessage msg = new NetworkMessage(bytes);
        switch (msg.getType()) {
            case MESSAGE:
                for (Map.Entry<InetSocketAddress, String> entry : clients.entrySet()) {
                    DatagramPacket sendPacket = new DatagramPacket(bytes, bytes.length, entry.getKey());
                    socket.send(sendPacket);
                }
                break;
            case DISCONNECT:
                for (Map.Entry<InetSocketAddress, String> entry : clients.entrySet()) {
                    DatagramPacket sendPacket = new DatagramPacket(bytes, bytes.length, entry.getKey());
                    socket.send(sendPacket);
                }
                break;
            case REQUESTCONNECTION:
                NetworkMessage answer = null;
                if (!bannedClients.containsKey(msg.getSenderAddress())) {
                    if (!clients.containsKey(msg.getSenderAddress())) {
                        answer = new NetworkMessage(NetworkMessage.TypeOfMessage.REQUESTCONNECTION,
                                                    "Success", new InetSocketAddress(socket.getInetAddress(), serverPort));
                        clients.put(msg.getSenderAddress(), msg.getMessage());
                    }
                }
                else {
                    answer = new NetworkMessage(NetworkMessage.TypeOfMessage.REQUESTCONNECTION,
                                                "You're banned!", new InetSocketAddress(socket.getInetAddress(), serverPort));
                }
                DatagramPacket p = new DatagramPacket(answer.getBytes(), NetworkMessage.sizeOfMessage, receivePacket.getSocketAddress());
                socket.send(p);
                break;
        }
    }
}
