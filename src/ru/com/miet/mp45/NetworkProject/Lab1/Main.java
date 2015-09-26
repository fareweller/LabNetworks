package ru.com.miet.mp45.NetworkProject.Lab1;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Scanner;

/**
 * Created by BALALAIKA on 13.09.2015.
 */
public class Main {
    public static void main(String[] argv) throws IOException, InterruptedException{
        Server server = new Server(44444);
        Client client = new Client(55555);
        server.start();
        client.start();
        client.tryConnectTo(InetAddress.getByName("localhost"), 44444);

        Thread.sleep(1000);

        String msg = "";
        Scanner sc = new Scanner(System.in);
        while (!msg.equals("exit")) {
            msg = sc.nextLine();
            client.sendStr(msg);
        }
        client.disconnectFromServer();
        client.stopClient();
        server.stopServer();
    }
}
