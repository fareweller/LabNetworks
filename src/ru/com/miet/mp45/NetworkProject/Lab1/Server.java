package ru.com.miet.mp45.NetworkProject.Lab1;

import javafx.application.Platform;
import javafx.util.Pair;
import ru.com.miet.mp45.NetworkProject.Lab1.serverGui.ServerGui;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.function.Predicate;

/**
 * Created by BALALAIKA on 24.09.2015.
 */
public class Server extends ChatActor {
    private ServerGui serverGui = null;
    protected Map<InetSocketAddress, String> bannedClients = null;

    public Server(int port, ServerGui gui) throws UnknownHostException, SocketException {
        super(port);
        serverGui = gui;
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
        timer.schedule(executeTask, 50, 100);
        timer.schedule(sendTask, 50, 100);
    }

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
        sendingMessages.clear();
        receivedMessages.clear();
        executedMessages.clear();

        for (Map.Entry<InetSocketAddress, String> entry : clients.entrySet()) {
            SendMessage(entry.getKey(), NetworkMessage.TypeOfMessage.CLOSESERVER, "Server");
        }

        isReceiving = false;
        while (!sendingMessages.isEmpty()) {
            long cnt = 1000*1000;
            InetSocketAddress client = new InetSocketAddress(sendingMessages.first().getValue().getAddress(), sendingMessages.first().getValue().getPort());
            while (cnt > 0 && (!receivedMessages.isEmpty() || !sendingMessages.isEmpty()))
                cnt--;
            if (sendingMessages.first().getValue().equals(client))
                sendingMessages.pollFirst();
        }
        timer.cancel();
        timer = null;
        sendingMessages = null;
        receivedMessages = null;
        executedMessages = null;
    }

    @Override
    public void executeHeadMessage() {
        if (receivedMessages != null && !receivedMessages.isEmpty()) {
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
                                addClientToGUIList(message.getKey().getMessage());
                            } else {
                                SendMessage(message.getValue(), NetworkMessage.TypeOfMessage.REQUESTCONNECTION, "You've already connected");
                            }
                        } else {
                            SendMessage(message.getValue(), NetworkMessage.TypeOfMessage.REQUESTCONNECTION, "You've banned!!!");
                            addBannedClientToGUIList(message.getKey().getMessage());
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
                    InetSocketAddress inetSocketAddress = null;
                    for (Map.Entry<InetSocketAddress, String> entry : clients.entrySet()) {
                        try {
                            if (!entry.getValue().equals(message.getKey().getMessage()))
                                SendMessage(entry.getKey(), message.getKey().getType(), message.getKey().getMessage());
                            else
                                inetSocketAddress = entry.getKey();
                        }
                        catch (IOException e) {

                        }
                    }
                    clients.remove(inetSocketAddress);
                    removeClientFromGUIList(message.getKey().getMessage());
                    break;
            }
            executedMessages.add(message);
        }
    }
//Change ban-unban functions form string argument to inetaddress argument
    public void banClient(String nicknameToBan) throws IOException {
        for (Map.Entry<InetSocketAddress, String> entry : clients.entrySet() ) {
            if (entry.getValue().equals(nicknameToBan)) {
                SendMessage(entry.getKey(), NetworkMessage.TypeOfMessage.BAN, "You've banned!");
                while (sendingMessages.first().getKey().getType() != NetworkMessage.TypeOfMessage.BAN);
                try {
                    Thread.sleep(750);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (sendingMessages.first().getKey().getType() == NetworkMessage.TypeOfMessage.BAN)
                    sendingMessages.pollFirst();
                bannedClients.put(entry.getKey(), entry.getValue());
                clients.remove(entry.getKey());
                for (Map.Entry<InetSocketAddress, String> entry1 : clients.entrySet()) {
                    SendMessage(entry1.getKey(), NetworkMessage.TypeOfMessage.DISCONNECT, entry.getValue());
                }
                break;
            }
        }
        removeClientFromGUIList(nicknameToBan);
        addBannedClientToGUIList(nicknameToBan);
    }

    public void unbanClient(String nicknameToUnban) {
        for (Map.Entry<InetSocketAddress, String> entry : bannedClients.entrySet() ) {
            if (entry.getValue().equals(nicknameToUnban)) {
                bannedClients.remove(entry.getKey());
                break;
            }
        }
        removeBannedClientFromGUIList(nicknameToUnban);
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

    public void addClientToGUIList(final String name) {

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                serverGui.getConnectedClients().add(name);
                System.out.println(name + " has been add to connected");
            }
        });
    }
    public void addBannedClientToGUIList(final String name) {

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                serverGui.getBannedClients().add(name);
                System.out.println(name + " has been add to banned");
            }
        });
    }
    public void removeClientFromGUIList(final String name) {

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                serverGui.getConnectedClients().removeIf(new Predicate<String>() {
                    @Override
                    public boolean test(String s) {
                        return s.equals(name);
                    }
                });
            }
        });

        System.out.println(name + " has been removed from connected");
    }
    public void removeBannedClientFromGUIList(final String name) {

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                serverGui.getBannedClients().removeIf(new Predicate<String>() {
                    @Override
                    public boolean test(String s) {
                        return s.equals(name);
                    }
                });
            }
        });

        System.out.println(name + " has been removed from banned");
    }
}
