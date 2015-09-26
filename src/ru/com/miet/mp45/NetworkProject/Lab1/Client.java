package ru.com.miet.mp45.NetworkProject.Lab1;

import java.io.IOException;
import java.net.*;

/**
 * Created by BALALAIKA on 24.09.2015.
 */
public class Client extends Thread {
    private DatagramSocket socket = null;
    private InetSocketAddress serverAddress = null;
    private int serverPort = 0;
    private int clientPort = 0;
    private boolean establishedConnection = false;
    private boolean isRunning = false;

    public Client(int port) throws SocketException {
        socket = new DatagramSocket(port);
        clientPort = port;
        isRunning = true;
    }

    public void tryConnectTo(InetAddress address, int port) throws IOException{
        serverAddress = new InetSocketAddress(address, port);
        sendMessage(new NetworkMessage(NetworkMessage.TypeOfMessage.REQUESTCONNECTION,
                                       "Client", serverAddress));
    }

    public void sendStr(String str) throws IOException {
        if (!establishedConnection)
            ;//throwing exception
        else {
            sendMessage(new NetworkMessage(NetworkMessage.TypeOfMessage.MESSAGE,
                                           str, serverAddress));
        }
    }

    public void disconnectFromServer() throws IOException {
        sendMessage(new NetworkMessage(NetworkMessage.TypeOfMessage.DISCONNECT,
                                       "", new InetSocketAddress(socket.getInetAddress(), clientPort)));
        establishedConnection = false;
    }

    public void closeClient() {
        socket.close();
        isRunning = false;
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

    public void stopClient() {
        isRunning = false;
    }

    private void sendMessage(NetworkMessage msg) throws IOException {
        if (establishedConnection || msg.getType() == NetworkMessage.TypeOfMessage.REQUESTCONNECTION) {
            DatagramPacket sendPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length, serverAddress);
            socket.send(sendPacket);
        }
    }

    private void receiveAndExecuteMessage() throws IOException {
        byte[] bytes = new byte[NetworkMessage.sizeOfMessage];
        DatagramPacket receivePacket = new DatagramPacket(bytes, bytes.length);
        socket.receive(receivePacket);
        NetworkMessage msg = new NetworkMessage(bytes);
        switch (msg.getType()) {
            case MESSAGE:
                System.out.println(msg.getMessage() + " from " + msg.getSenderAddress().toString());
                break;
            case DISCONNECT:

                break;
            case REQUESTCONNECTION:
                if (msg.getMessage().equals("Success")) {
                    System.out.println("You're connected");
                    establishedConnection = true;
                }
                else {
                    establishedConnection = false;
                    System.out.println(msg.getMessage());
                }

                break;
            case NEWCLIENT:
                break;
            case CLOSESERVER:
                establishedConnection = false;
                break;
        }
    }
}
