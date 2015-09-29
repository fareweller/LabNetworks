package ru.com.miet.mp45.NetworkProject.Lab1;

import javafx.util.Pair;
import java.io.IOException;
import java.net.*;
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
        SendMessage(serverAddress, new NetworkMessage(NetworkMessage.TypeOfMessage.REQUESTCONNECTION, "Client", 0));
    }

    public void sendString(String str) {
        if (established && serverAddress != null) {
            try {
                SendMessage(serverAddress, new NetworkMessage(NetworkMessage.TypeOfMessage.MESSAGE, str, 0));
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
                    SendMessage(message.getValue(), new NetworkMessage(NetworkMessage.TypeOfMessage.ACK, String.valueOf(message.getKey().getMessageId()), 0));
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            switch (message.getKey().getType()) {
                case REQUESTCONNECTION:
                    if (message.getKey().getMessage().equals("Success")) {
                        System.out.println("You've connected");
                        established = true;
                    } else {
                        if (message.getKey().getMessage().equals("You've banned")) {
                            serverAddress = null;
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
