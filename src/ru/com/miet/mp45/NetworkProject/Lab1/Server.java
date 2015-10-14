package ru.com.miet.mp45.NetworkProject.Lab1;

import javafx.util.Pair;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.function.Predicate;

/**
 * Created by BALALAIKA on 24.09.2015.
 */
public class Server extends ChatActor {
    protected Map<InetSocketAddress, String> bannedClients = null;

    public Server(int port) throws UnknownHostException, SocketException {
        super(port);

        sendingMessages = new TreeSet<Pair<NetworkMessage, InetSocketAddress>>(new Comparator<Pair<NetworkMessage, InetSocketAddress>>() {
            @Override
            public int compare(Pair<NetworkMessage, InetSocketAddress> o1, Pair<NetworkMessage, InetSocketAddress> o2) {
                if (o1.getKey().getMessageId() != o2.getKey().getMessageId())
                    return Long.signum(o1.getKey().getMessageId() - o2.getKey().getMessageId());
                return o1.getValue().toString().compareTo(o2.getValue().toString());
            }
        });

        receivedMessages = new TreeSet<Pair<NetworkMessage, InetSocketAddress>>(new Comparator<Pair<NetworkMessage, InetSocketAddress>>() {
            @Override
            public int compare(Pair<NetworkMessage, InetSocketAddress> o1, Pair<NetworkMessage, InetSocketAddress> o2) {
                if (o1.getKey().getMessageId() != o2.getKey().getMessageId())
                    return Long.signum(o1.getKey().getMessageId() - o2.getKey().getMessageId());
                return o1.getValue().toString().compareTo(o2.getValue().toString());
            }
        });

        executedMessages = new TreeSet<Pair<NetworkMessage, InetSocketAddress>>(new Comparator<Pair<NetworkMessage, InetSocketAddress>>() {
            @Override
            public int compare(Pair<NetworkMessage, InetSocketAddress> o1, Pair<NetworkMessage, InetSocketAddress> o2) {
                if (o1.getKey().getMessageId() != o2.getKey().getMessageId())
                    return Long.signum(o1.getKey().getMessageId() - o2.getKey().getMessageId());
                return o1.getValue().toString().compareTo(o2.getValue().toString());
            }
        });

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

        timer = new Timer();
        timer.schedule(executeTask, 100, 100);
        timer.schedule(sendTask, 100, 100);
    }

    @Override
    public void executeAll() throws IOException {
        for (Map.Entry<InetSocketAddress, String> entry : clients.entrySet()) {
            SendMessage(entry.getKey(), NetworkMessage.TypeOfMessage.DISCONNECT, "Server");
        }
        isReceiving = false;
        timer.cancel();
        timer = null;
        while (!receivedMessages.isEmpty() || !sendingMessages.isEmpty());

        sendingMessages = null;
        receivedMessages = null;
        executedMessages = null;
    }

    @Override
    public void executeHeadMessage() {
        if (!receivedMessages.isEmpty()) {
            Pair<NetworkMessage, InetSocketAddress> message = receivedMessages.pollFirst();
            if (message.getKey().getType() != NetworkMessage.TypeOfMessage.ACK) {
                try {
                    SendMessage(message.getValue(), NetworkMessage.TypeOfMessage.ACK, String.valueOf(message.getKey().getMessageId()));
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (executedMessages.contains(message))
                return;
            switch (message.getKey().getType()) {
                case REQUESTCONNECTION:
                    try {
                        if (!bannedClients.containsKey(message.getValue())) {
                            if (!clients.containsKey(message.getValue())) {
                                for (Map.Entry<InetSocketAddress, String> entry : clients.entrySet()) {
                                    SendMessage(entry.getKey(), NetworkMessage.TypeOfMessage.CONNECT, message.getKey().getMessage());
                                }
                                SendMessage(message.getValue(), NetworkMessage.TypeOfMessage.REQUESTCONNECTION, "Success");
                                clients.put(message.getValue(), message.getKey().getMessage());
                                for (Map.Entry<InetSocketAddress, String> entry : clients.entrySet()) {
                                    SendMessage(message.getValue(), NetworkMessage.TypeOfMessage.CONNECT, entry.getValue());
                                }

                            } else {
                                SendMessage(message.getValue(), NetworkMessage.TypeOfMessage.REQUESTCONNECTION, "You've already connected");
                            }
                        } else {
                            SendMessage(message.getValue(), NetworkMessage.TypeOfMessage.REQUESTCONNECTION, "You've banned!!!");
                        }
                    }
                    catch (IOException e) {

                    }
                    break;
                case MESSAGE:
                    for (Map.Entry<InetSocketAddress, String> entry : clients.entrySet()) {
                        try {
                            SendMessage(entry.getKey(), message.getKey().getType(), message.getKey().getMessage());
                        }
                        catch (IOException e) {

                        }
                    }
                    break;
                case ACK:
                    final long id = Long.parseLong(message.getKey().getMessage());
                    sendingMessages.removeIf(new Predicate<Pair<NetworkMessage, InetSocketAddress>>() {
                        @Override
                        public boolean test(Pair<NetworkMessage, InetSocketAddress> networkMessageInetSocketAddressPair) {
                            return networkMessageInetSocketAddressPair.getKey().getMessageId() == id;
                        }
                    });
                    break;
                case DISCONNECT:

                    break;
            }
            executedMessages.add(message);
        }
    }

    @Override
    public void sendFirstMessage() {
        if (!sendingMessages.isEmpty()) {
            try {
                Pair<NetworkMessage, InetSocketAddress> message = sendingMessages.first();
                DatagramPacket sendPacket =
                        new DatagramPacket(message.getKey().getBytes(), message.getKey().getBytes().length, message.getValue());
                socket.send(sendPacket);
                if (message.getKey().getType() == NetworkMessage.TypeOfMessage.ACK) {
                    final long id = message.getKey().getMessageId();
                    sendingMessages.removeIf(new Predicate<Pair<NetworkMessage, InetSocketAddress>>() {
                        @Override
                        public boolean test(Pair<NetworkMessage, InetSocketAddress> networkMessageInetSocketAddressPair) {
                            return networkMessageInetSocketAddressPair.getKey().getMessageId() == id;
                        }
                    });
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
