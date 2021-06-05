import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class MDBMessageHandler implements Runnable {
    int port;
    int senderId;
    String ipAddress;
    DatagramPacket packet;
    List<byte[]> allChunks = new ArrayList<>();
    double availableSpace;

    MDBMessageHandler(DatagramPacket packet, String ipAddress, int port, int senderId, double availableSpace) {
        this.port = port;
        this.packet = packet;
        this.senderId = senderId;
        this.ipAddress = ipAddress;
        this.availableSpace = availableSpace;
    }

    public void setAvailableSpace(double availableSpace) {
        this.availableSpace = availableSpace;
    }

    @Override
    public void run() {
        String messageString = new String(packet.getData(), packet.getOffset(), packet.getLength());
        MessageHeader header = new Message(messageString).getHeader();

        // check peer identity
        if(!(header.getSenderId() == senderId)) {
            //System.out.print("> Peer " + senderId + ": [Multicast UDP message received from peer" + header.getSenderId() + "] >> " + header);
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<String> future = executor.submit(new StoredCounter(port, ipAddress, header.getFileId(),header.getChunkNo(),header.getSenderId(), header.getReplicationDeg()));

            int storedMessages = 0;
            try {
                int random = (int) (Math.random()*400);
                String ret = future.get(random, TimeUnit.MILLISECONDS);
                storedMessages = Integer.parseInt(ret);

                executor.shutdown();
            } catch(InterruptedException | ExecutionException e) {
                e.printStackTrace();
            } catch(TimeoutException ignored) {}


            if(storedMessages < header.getReplicationDeg()) {

                String filePath="../../resources/peer"+senderId+"/current_storage.txt";
                Path path = Paths.get(filePath);

                AsynchronousFileChannel channel=null;

                try {
                    channel = AsynchronousFileChannel.open(path, StandardOpenOption.READ);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteBuffer buffer = ByteBuffer.allocate(1024);
                assert channel != null;
                Future<Integer> result=channel.read(buffer,0);
                while(!result.isDone()) {}
                buffer.flip();

                String content;
                byte[]arr = new byte[buffer.remaining()];
                buffer.get(arr);
                content = Utils.byteArrayToString(arr);

                buffer.clear();
                try {
                    channel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                double folderSpace=Double.parseDouble(content);

                if(folderSpace + packet.getLength() < availableSpace && !Utils.hasStored(senderId,header.getFileId(),header.getChunkNo())) {
                    MessageHeader messageHeader = new MessageHeader("1.0", MessageType.STORED, senderId, header.getFileId(), header.getChunkNo());
                    try {
                        messageHeader.send(ipAddress, port);
                        //System.out.println("Sent stored message");
                    }
                    catch(IOException e) { e.printStackTrace(); }

                    try {
                        channel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    byte[] writableBody = new byte[packet.getLength()];
                    System.arraycopy(packet.getData(), 0, writableBody, 0, packet.getLength());
                    int bodyStart = FileChunkManager.findBodyStart(writableBody);

                    double aux = packet.getLength() - bodyStart;
                    folderSpace = folderSpace + aux/1000;
                    // System.out.println("Folder space: " + String.valueOf(folderSpace));
                    buffer= ByteBuffer.allocate(1024);
                    buffer.put(String.valueOf(folderSpace).getBytes(StandardCharsets.UTF_8));
                    buffer.flip();

                    Future<Integer> fut = channel.write(buffer, 0);
                    while(!fut.isDone()) {}
                    buffer.clear();
                    try {
                        channel.close();
                    } catch (IOException e) { e.printStackTrace(); }

                    FileChunkManager.createFile(bodyStart, writableBody, header, senderId, header.getReplicationDeg());
                }
            }
        }
    }
}
