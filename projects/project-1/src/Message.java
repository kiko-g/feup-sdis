import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;


public class Message {
    private byte[] body;
    private MessageHeader header;

    public Message(String message) {
        String[] lines = message.split("\\r\\n",2);
        header = new MessageHeader(lines[0]);
        body = lines[1].getBytes(StandardCharsets.ISO_8859_1);
    }

    public Message(byte[] byteArray) {
        String doubleCRLF = "\r\n\r\n";
        String message = Utils.byteArrayToString(byteArray);
        ArrayList<String> split = new ArrayList<>(Arrays.asList(message.split(doubleCRLF, 2)));

        header = new MessageHeader(split.remove(0));

        if (split.size() != 0) {
            System.out.println("I have body");
            body = split.get(0).getBytes(StandardCharsets.ISO_8859_1);
        }
    }

    public Message(String version, MessageType messageType, int senderId, String fileId, int chunkNo, int replicationDeg, byte[] body) {
        this.header = new MessageHeader(version + " " + messageType.name() + " " + senderId + " " + fileId + " " + chunkNo + " " + replicationDeg);
        this.body = body;
    }
    public Message(String version, MessageType messageType, int senderId, String fileId, int chunkNo, byte[] body) {
        this.header = new MessageHeader(version + " " + messageType.name() + " " + senderId + " " + fileId + " " + chunkNo);
        this.body = body;
    }

    public Message(String version, MessageType messageType, int senderId, String fileId, int chunkNo) {
        this.header = new MessageHeader(version + " " + messageType.name() + " " + senderId + " " + fileId + " " + chunkNo);
        this.body = null;
    }

    public Message(String version, MessageType messageType, int senderId, String fileId) {
        this.header = new MessageHeader(version + " " + messageType.name() + " " + senderId + " " + fileId);
        this.body = null;
    }

    public void send(String IPAddress, int port) throws IOException {
        InetAddress group = InetAddress.getByName(IPAddress);
        DatagramSocket socket = new DatagramSocket();

        byte[] byteMessage = this.convertToBytes();
        DatagramPacket packet = new DatagramPacket(byteMessage, byteMessage.length, group, port);
        //System.out.print("Outgoing message: "+header.toString());

        try {
            socket.send(packet);
        }
        catch (IOException e) {
            //e.printStackTrace();
            System.out.println("Unable to send multicast message");
        }

        socket.close();
    }



    public void setBody(byte[] body) {
        this.body = body;
    }

    public void setHeader(MessageHeader header) {
        this.header = header;
    }

    public byte[] getBody() {
        return body;
    }

    public String getBodyAsString() {
        return Utils.byteArrayToString(body);
    }

    public MessageHeader getHeader() {
        return header;
    }


    @Override
    public String toString() {
        return header.toString() + String.valueOf(Utils.CRLF) + getBodyAsString();
    }


    public byte[] convertToBytes() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write(header.toString().getBytes(StandardCharsets.ISO_8859_1));
        stream.write(String.valueOf(Utils.CRLF).getBytes(StandardCharsets.ISO_8859_1));
        if(body != null)
        stream.write(body);

        return stream.toByteArray();
    }
}
