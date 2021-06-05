import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class MessageHeader {
    private int chunkNo;
    private int senderId;
    private int replicationDeg;
    private String fileId;
    private String version;
    private MessageType messageType;


    // constructor with full header
    public MessageHeader(String header) {
        String[] headerParts = header.split("\\s+");

        this.version = headerParts[0];
        this.messageType = MessageType.valueOf(headerParts[1]);
        this.senderId = Integer.parseInt(headerParts[2]);
        this.fileId = headerParts[3];

        switch (this.messageType) {
            case PUTCHUNK:
                this.chunkNo = Integer.parseInt(headerParts[4]);
                this.replicationDeg = Integer.parseInt(headerParts[5]);
                break;

            case CHUNK:
            case GETCHUNK:
            case REMOVED:
            case STORED:
                this.chunkNo = Integer.parseInt(headerParts[4]);
                break;

            case DELETE:
            default:
                break;
        }
    }


    public MessageHeader(String version, MessageType messageType, int senderId, String fileId, int chunkNo) {
        this.version = version;
        this.messageType = messageType;
        this.senderId = senderId;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
    }



    @Override
    public String toString() {
        String result = version + " " + messageType.name() + " " + senderId + " " + fileId + " ";
        switch(messageType) {
            case PUTCHUNK:
                result += chunkNo + " " + replicationDeg;
                break;

            case GETCHUNK:
            case REMOVED:
            case STORED:
            case CHUNK:
                result += chunkNo;
                break;

            case DELETE:
                break;

            default:
                System.err.println("Error in message type field of message header");
                break;
        }

        result += " " + String.valueOf(Utils.CRLF);
        return result;
    }


    public void send(String IPAddress, int port) throws IOException {
        //System.out.print("Outgoing header: "+this.toString());
        InetAddress group = InetAddress.getByName(IPAddress);
        DatagramSocket socket = new DatagramSocket();

        byte[] byteMessage = this.convertToBytes();
        DatagramPacket packet = new DatagramPacket(byteMessage, byteMessage.length, group, port);

        try {
            socket.send(packet);
        }
        catch (IOException e) {
            e.printStackTrace();
            System.out.println("Unable to send multicast message");
        }

        socket.close();
    }

    public byte[] convertToBytes() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write(this.toString().getBytes(StandardCharsets.ISO_8859_1));

        return stream.toByteArray();
    }


    public int getChunkNo() {
        return chunkNo;
    }

    public int getReplicationDeg() {
        return replicationDeg;
    }

    public String getFileId() {
        return fileId;
    }

    public String getVersion() {
        return version;
    }

    public int getSenderId() {
        return senderId;
    }

    public MessageType getMessageType() {
        return messageType;
    }
}
