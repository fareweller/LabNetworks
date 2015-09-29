package ru.com.miet.mp45.NetworkProject.Lab1;

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
    protected Map<InetSocketAddress, String> clients = null;

    protected TreeSet<Pair<NetworkMessage, InetSocketAddress>> receivedMessages = null;
    protected TreeSet<Pair<NetworkMessage, InetSocketAddress>> sendingMessages = null;

    protected int messageCnt = 0;

    protected Timer timer = null;
    protected TimerTask executeTask = null;
    protected TimerTask sendTask = null;

    public ChatActor(int port) throws UnknownHostException, SocketException{
        socket = new DatagramSocket(port, InetAddress.getLocalHost());
        this.port = port;
        clients = new TreeMap<InetSocketAddress, String>(new Comparator<InetSocketAddress>() {
            @Override
            public int compare(InetSocketAddress o1, InetSocketAddress o2) {
                return o1.getHostString().compareTo(o2.getHostString());
            }
        });

        receivedMessages = new TreeSet<Pair<NetworkMessage, InetSocketAddress>>(new Comparator<Pair<NetworkMessage, InetSocketAddress>>() {
            @Override
            public int compare(Pair<NetworkMessage, InetSocketAddress> o1, Pair<NetworkMessage, InetSocketAddress> o2) {
                return Long.signum(o1.getKey().getMessageId() - o2.getKey().getMessageId());
            }
        });
        sendingMessages = new TreeSet<Pair<NetworkMessage, InetSocketAddress>>(new Comparator<Pair<NetworkMessage, InetSocketAddress>>() {
            @Override
            public int compare(Pair<NetworkMessage, InetSocketAddress> o1, Pair<NetworkMessage, InetSocketAddress> o2) {
                return Long.signum(o1.getKey().getMessageId() - o2.getKey().getMessageId());
            }
        });

        isRunning = true;
        executeTask = new TimerTask() {
            @Override
            public void run() {
                executeHeadMessage();
            }
        };
        sendTask = new TimerTask() {
            @Override
            public void run() {
                sendFirstMessage();
            }
        };

        timer = new Timer();
        timer.schedule(executeTask, 100, 100);
        timer.schedule(sendTask, 100, 100);
    }

    protected abstract void executeHeadMessage();
    protected abstract void sendFirstMessage();

    protected void SendMessage(InetSocketAddress address, NetworkMessage msg) throws IOException {
        messageCnt++;
        NetworkMessage message = new NetworkMessage(msg.getType(), msg.getMessage(), messageCnt);
        sendingMessages.add(new Pair<NetworkMessage, InetSocketAddress>(message, address));
    }

    public void turnOff() {
        isRunning = false;
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
                receivedMessages.add(new Pair<NetworkMessage, InetSocketAddress>(new NetworkMessage(bytes),
                        new InetSocketAddress(receivePacket.getAddress(), receivePacket.getPort())));
            }
            catch (IOException e) {

            }
        }
    }
}
