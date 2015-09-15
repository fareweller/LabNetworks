package ru.com.miet.mp45.NetworkProject.Lab1;

import sun.nio.ch.Net;

import java.io.*;

/**
 * Created by BALALAIKA on 14.09.2015.
 */
public class NetworkMessage {
    private enum TypeOfMessage {Connect, Message, Disconnect}
    private TypeOfMessage type;
    private String msg;
    private String from;

    public NetworkMessage(TypeOfMessage typeParam, String msgParam, String fromParam) {
        type = typeParam;
        msg = msgParam;
        from = fromParam;
    }

    public NetworkMessage(byte[] bytes) throws IOException, ClassNotFoundException{
        ByteArrayInputStream b = new ByteArrayInputStream(bytes);
        ObjectInputStream o = new ObjectInputStream(b);
        NetworkMessage message = (NetworkMessage)o.readObject();
        type = message.getType();
        msg = message.getMsg();
        from = message.getFrom();
    }

    public byte[] getBytes() throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream o = new ObjectOutputStream(b);
        o.writeObject(this);
        return b.toByteArray();
    }

    public TypeOfMessage getType() {
        return type;
    }

    public String getMsg() {
        return msg;
    }

    public String getFrom() {
        return from;
    }
}
