package ru.com.miet.mp45.NetworkProject.Lab1;

import sun.reflect.generics.tree.Tree;

import java.net.*;
import java.io.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by BALALAIKA on 13.09.2015.
 */
public class UDPServer implements Runnable{
    private DatagramSocket serverSocket = null;
    private boolean running = false;
    private int port;
    private TreeMap<InetAddress, String> clients = null;
    private TreeMap<InetAddress, String> bannedClients = null;
    public UDPServer(int serverPort) throws SocketException {
        port = serverPort;
        serverSocket = new DatagramSocket(serverPort);
        clients = new TreeMap<InetAddress, String>();
        bannedClients = new TreeMap<InetAddress, String>();
    }

    public void sendToAllClients(NetworkMessage msg){
        try {
            byte[] bytes = msg.getBytes();
            for (Map.Entry<InetAddress, String> entry : clients.entrySet()) {
                DatagramPacket sendPacket =
                        new DatagramPacket(bytes, bytes.length, entry.getKey(), port);
                serverSocket.send(sendPacket);
            }
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void run(){
        running = true;
        while (running) {

        }

        serverSocket.close();
    }
}
