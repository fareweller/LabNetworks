package ru.com.miet.mp45.NetworkProject.Lab1;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Created by BALALAIKA on 24.09.2015.
 */
public class NetworkMessage {
    public enum TypeOfMessage {
        UNKNOWN(0),
        MESSAGE(1),
        REQUESTCONNECTION(2),
        DISCONNECT(3),
        CONNECT(4),
        NEWCLIENT(5),
        CLOSESERVER(6),
        ACK(7);
        private final int value;
        private TypeOfMessage(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
    }
    public final static int sizeOfMessage = 512;
    private byte[] bytes = null;
    private String message = null;
    private TypeOfMessage typeOfMessage = null;
    private long messageId = 0;

    public NetworkMessage(TypeOfMessage type, String msg, long id) throws IOException {
        typeOfMessage = type;
        message = msg;
        messageId = id;
        bytes = new byte[sizeOfMessage];

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(sizeOfMessage);
        byteStream.write(ByteBuffer.allocate(4).putInt(type.getValue()).array());
        byteStream.write(ByteBuffer.allocate(4).putInt(message.getBytes(Charset.forName("UTF-8")).length).array());
        byteStream.write(message.getBytes(Charset.forName("UTF-8")));
        byteStream.write(ByteBuffer.allocate(8).putLong(id).array());
        System.arraycopy(byteStream.toByteArray(), 0, bytes, 0, byteStream.size());
    }

    public NetworkMessage(byte[] byteArray) throws IOException {
        bytes = byteArray;
        ByteArrayInputStream byteStream = new ByteArrayInputStream(byteArray);

        int off = 0;
        byteStream.read(bytes, off, 4);
        typeOfMessage = TypeOfMessage.values()[ByteBuffer.wrap(bytes, off, 4).getInt()];

        off += 4;
        byteStream.read(bytes, off, 4);
        int len = ByteBuffer.wrap(bytes, off, 4).getInt();

        off += 4;
        byteStream.read(bytes, off, len);
        message = new String(bytes, off, len, StandardCharsets.UTF_8);

        off += 4;
        byteStream.read(bytes, off, 8);
        messageId = ByteBuffer.wrap(bytes, off, 8).getLong();
    }

    public byte[] getBytes() {
        return bytes;
    }

    public TypeOfMessage getType() {
        return typeOfMessage;
    }

    public String getMessage() {
        return message;
    }

    public long getMessageId() {
        return messageId;
    }
}
