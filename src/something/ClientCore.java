package something;

import java.io.*;
import java.net.*;

 public class ClientCore {

    public enum Msg {connect, msg, disconnect}

    public void sendMessage(Msg type, String message,DatagramSocket clientSocket, InetAddress IPAddress) throws Exception{
        byte[] sendData = message.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
        clientSocket.send(sendPacket);
    }
    public String recieveMessage(DatagramSocket clientSocket)throws Exception{
        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        clientSocket.receive(receivePacket);
        return new String(receivePacket.getData());

    }

    public static void main(String args[]) throws Exception {
        ClientCore messenger = new ClientCore();
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName("localhost");
        String sentence;
        while (true){
            sentence = inFromUser.readLine();
            if(sentence == "exit") break;
            messenger.sendMessage(Msg.msg, sentence, clientSocket, IPAddress);
            String recievedSentence = messenger.recieveMessage(clientSocket);
            System.out.println("FROM SERVER:" + recievedSentence);
        }
        clientSocket.close();
    }
}