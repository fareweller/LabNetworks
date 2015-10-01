package ru.com.miet.mp45.NetworkProject.Lab1;

import javafx.util.Pair;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.function.Predicate;

/**
 * Created by BALALAIKA on 24.09.2015.
 */
public class Client extends ChatActor {
    private InetSocketAddress serverAddress = null;
    private boolean established = false;

    public Client(int port) throws UnknownHostException, SocketException{
        super(port);
    }

    public void connectToServer(InetAddress address, int port) throws IOException {
        serverAddress = new InetSocketAddress(address, port);

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

        timer = new Timer();
        timer.schedule(executeTask, 100, 100);
        timer.schedule(sendTask, 100, 100);
        SendMessage(serverAddress, NetworkMessage.TypeOfMessage.REQUESTCONNECTION, "Client");
    }

    public void sendString(String str) {
        if (established && serverAddress != null) {
            try {
                SendMessage(serverAddress, NetworkMessage.TypeOfMessage.MESSAGE, str);
            }
            catch (IOException e) {

            }
        }
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
                    if (message.getKey().getMessage().equals("Success")) {
                        System.out.println("You've connected");
                        established = true;
                    } else {
                        if (message.getKey().getMessage().equals("You've banned")) {
                            serverAddress = null;
                            sendingMessages = null;
                            receivedMessages = null;
                            executedMessages = null;
                            clients = null;
                            timer.cancel();
                            established = false;
                        }
                        System.out.println(message.getKey().getMessage());
                    }
                    break;
                case MESSAGE:
                    System.out.println(message.getKey().getMessage());
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
            }
            executedMessages.add(message);
        }
    }

    @Override
    public void sendFirstMessage() {
        if (!sendingMessages.isEmpty()) {
            try {
                Pair<NetworkMessage, InetSocketAddress> message = sendingMessages.first();

                if (established && serverAddress.equals(message.getValue()) ||
                    message.getKey().getType() == NetworkMessage.TypeOfMessage.REQUESTCONNECTION && serverAddress.equals(message.getValue())) {
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
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
