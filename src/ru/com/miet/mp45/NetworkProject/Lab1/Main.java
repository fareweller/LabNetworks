package ru.com.miet.mp45.NetworkProject.Lab1;

import java.net.SocketException;

/**
 * Created by BALALAIKA on 13.09.2015.
 */
public class Main {
    public static void main(String[] argv) {
        try {
            UDPServer server = new UDPServer(12345);
        }
        catch (SocketException e) {
            System.out.println(e.getMessage());
        }
    }
}
