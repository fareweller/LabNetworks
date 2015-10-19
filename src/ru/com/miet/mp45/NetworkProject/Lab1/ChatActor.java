package ru.com.miet.mp45.NetworkProject.Lab1;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;

import java.io.IOException;
import java.net.*;
import java.util.*;

/**
 * Created by BALALAIKA on 28.09.2015.
 */
public abstract class ChatActor implements Runnable {
    protected DatagramSocket socket = null;
    protected int port = 0;
    protected boolean isRunning = false;
    protected boolean isReceiving = false;
    protected Map<InetSocketAddress, String> clients = null;

    protected TreeSet<Pair<NetworkMessage, InetSocketAddress>> executedMessages = null;
    protected TreeSet<Pair<NetworkMessage, InetSocketAddress>> sendingMessages = null;
    protected TreeSet<Pair<NetworkMessage, InetSocketAddress>> receivedMessages = null;
    protected int messageCnt = 0;

    protected Timer timer = null;
    protected TimerTask executeTask = null;
    protected TimerTask sendTask = null;

    public ChatActor(int port) throws UnknownHostException, SocketException{
        socket = new DatagramSocket(port);
        this.port = port;
        isRunning = true;
        isReceiving = true;
    }

    protected abstract void executeHeadMessage();
    protected abstract void sendFirstMessage();
    public abstract void executeAll() throws IOException;

    protected void SendMessage(InetSocketAddress address, NetworkMessage.TypeOfMessage type, String msg) throws IOException {
        messageCnt++;
        NetworkMessage message = new NetworkMessage(type, msg, messageCnt);
        sendingMessages.add(new Pair<NetworkMessage, InetSocketAddress>(message, address));
    }

    public void turnOff() throws IOException {
        executeAll();
        isRunning = false;
        if (timer != null)
            timer.cancel();
        socket.close();
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                byte[] bytes = new byte[NetworkMessage.sizeOfMessage];
                DatagramPacket receivePacket = new DatagramPacket(bytes, bytes.length);
                socket.receive(receivePacket);
                Pair<NetworkMessage, InetSocketAddress> msg = new Pair<NetworkMessage, InetSocketAddress>(new NetworkMessage(bytes),
                        new InetSocketAddress(receivePacket.getAddress(), receivePacket.getPort()));
                if (isReceiving || msg.getKey().getType() == NetworkMessage.TypeOfMessage.ACK)
                    receivedMessages.add(msg);
            }
            catch (IOException e) {

            }
        }
    }
}
