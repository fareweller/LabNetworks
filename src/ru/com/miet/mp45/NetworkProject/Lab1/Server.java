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
        bannedClients = new TreeMap<InetSocketAddress, String>(new Comparator<InetSocketAddress>() {
            @Override
            public int compare(InetSocketAddress o1, InetSocketAddress o2) {
                return o1.getHostString().compareTo(o2.getHostString());
            }
        });
    }

    @Override
    public void executeHeadMessage() {
        if (!receivedMessages.isEmpty()) {
            Pair<NetworkMessage, InetSocketAddress> message = receivedMessages.pollFirst();
            if (message.getKey().getType() != NetworkMessage.TypeOfMessage.ACK) {
                try {
                    SendMessage(message.getValue(), new NetworkMessage(NetworkMessage.TypeOfMessage.ACK, String.valueOf(message.getKey().getMessageId()), 0));
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            switch (message.getKey().getType()) {
                case REQUESTCONNECTION:
                    try {
                        if (!bannedClients.containsKey(message.getValue())) {
                            if (!clients.containsKey(message.getValue())) {
                                SendMessage(message.getValue(), new NetworkMessage(NetworkMessage.TypeOfMessage.REQUESTCONNECTION, "Success", 0));
                                clients.put(message.getValue(), message.getKey().getMessage());
                            } else {
                                SendMessage(message.getValue(), new NetworkMessage(NetworkMessage.TypeOfMessage.REQUESTCONNECTION, "You've already connected", 0));
                            }
                        } else {
                            SendMessage(message.getValue(), new NetworkMessage(NetworkMessage.TypeOfMessage.REQUESTCONNECTION, "You've banned!!!", 0));
                        }
                    }
                    catch (IOException e) {

                    }
                    break;
                case MESSAGE:
                    for (Map.Entry<InetSocketAddress, String> entry : clients.entrySet()) {
                        try {
                            SendMessage(entry.getKey(), message.getKey());
                        }
                        catch (IOException e) {

                        }
                    }
                    break;
                case ACK:
                    String str = message.getKey().getMessage();
                    final long id = Long.parseLong(str);
                    sendingMessages.removeIf(new Predicate<Pair<NetworkMessage, InetSocketAddress>>() {
                        @Override
                        public boolean test(Pair<NetworkMessage, InetSocketAddress> networkMessageInetSocketAddressPair) {
                            return networkMessageInetSocketAddressPair.getKey().getMessageId() == id;
                        }
                    });
                    break;
            }
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
