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
        //Client client = new Client(55555);
        (new Thread(server)).start();
        /*
        (new Thread(client)).start();
        client.connectToServer(InetAddress.getLocalHost(), 44444, "ddd");

        String msg = "";
        Scanner sc = new Scanner(System.in);
        while (!msg.equals("exit")) {
            msg = sc.nextLine();
            client.sendString(msg);
        }
        client.turnOff();
        server.turnOff();*/
        //Thread.sleep(60000);
        //server.turnOff();
    }
}
