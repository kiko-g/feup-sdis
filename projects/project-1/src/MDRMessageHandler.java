import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;

public class MDRMessageHandler implements Runnable {
    int port;
    int senderId;
    String ipAddress;
    DatagramPacket packet;
    List<byte[]> allChunks = new ArrayList<>();

    MDRMessageHandler(DatagramPacket packet, String ipAddress, int port, int senderId) {
        this.port = port;
        this.packet = packet;
        this.senderId = senderId;
        this.ipAddress = ipAddress;
    }

    @Override
    public void run() {
        String messageString = new String(packet.getData(), packet.getOffset(), packet.getLength());
        MessageHeader header = new Message(messageString).getHeader();

        // check peer identity
        if(!(header.getSenderId() == senderId)) {

            if(header.getMessageType().name().equals(MessageType.CHUNK.name())) {


                byte[] writableBody = new byte[packet.getLength()];
                System.arraycopy(packet.getData(), 0, writableBody, 0, packet.getLength());
                int bodyStart = FileChunkManager.findBodyStart(writableBody);
                FileChunkManager.createFile(bodyStart, writableBody, header, senderId,0);
            }
        }
    }
}

