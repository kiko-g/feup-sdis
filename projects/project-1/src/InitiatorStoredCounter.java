import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.Callable;

public class InitiatorStoredCounter implements Callable<String> {
    int port;
    int chunkNo;
    int counter;
    String IP;
    String fileID;
    MulticastSocket multicastSocket;

    InitiatorStoredCounter(int port, String IP, String fileID, int chunkNo) {
        this.IP = IP;
        this.port = port;
        this.fileID = fileID;
        this.chunkNo = chunkNo;
    }

    Callable<String> chunkCounterTask = () -> {
        byte[] buffer;
        DatagramPacket packet;
        try {
            this.multicastSocket = new MulticastSocket(this.port);
            this.multicastSocket.joinGroup(InetAddress.getByName(this.IP));
        } catch(IOException e) {
            e.printStackTrace();
            System.err.println("erro");
        }

        while(true) {

            buffer = new byte[Utils.MAX_BUFFER_SIZE];
            packet = new DatagramPacket(buffer, buffer.length);

            try {
                assert this.multicastSocket != null;
                this.multicastSocket.receive(packet);

            } catch(IOException e) {
                e.printStackTrace();
            }

            String messageString = new String(packet.getData(), packet.getOffset(), packet.getLength());
            MessageHeader header = new Message(messageString).getHeader();
            if(header.getFileId().equals(this.fileID) && header.getChunkNo() == this.chunkNo) {
                if(header.getMessageType().name().equals(MessageType.STORED.name())) {
                    this.counter++;
                }
            }
        }
    };


    @Override
    public String call() throws Exception {
        return this.chunkCounterTask.call();
    }
}
