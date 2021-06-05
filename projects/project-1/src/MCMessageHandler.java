import java.io.File;
import java.io.FileInputStream;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MCMessageHandler implements Runnable {
    int port, portMDR, portMDB, senderId;
    int availableSpace;
    String ipAddress, ipMDR, ipMDB;
    DatagramPacket packet;
    List<byte[]> allChunks = new ArrayList<>();

    MCMessageHandler(DatagramPacket packet, String ipAddress, int port, String ipMDR, int portMDR, String ipMDB, int portMDB, int senderId) {
        this.port = port;
        this.packet = packet;
        this.ipAddress = ipAddress;
        this.ipMDR = ipMDR;
        this.portMDR = portMDR;
        this.ipMDB = ipMDB;
        this.portMDB = portMDB;
        this.senderId = senderId;
    }

    @Override
    public void run() {
        String messageString = new String(packet.getData(), packet.getOffset(), packet.getLength());
        MessageHeader header = new Message(messageString).getHeader();

        // check peer identity
        if(!(header.getSenderId() == senderId)) {
            if(header.getMessageType().name().equals(Utils.DELETE)) {
                double space=0;
                final File folder = new File("../../resources/peer" + senderId);
                final File[] files = folder.listFiles((dir, name) -> {
                    String regex = header.getFileId();
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(name);
                    return matcher.find();
                });

                if(files == null) return;
                for (final File file : files) {
                    space=space+file.getTotalSpace();
                    //System.out.println("Space: "+file.getTotalSpace());
                    file.delete();
                }




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

                path = Paths.get("../../resources/peer"+senderId+"/current_storage.txt");
                channel = null;
                try {
                    channel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                double last=folderSpace - space;
                if( last<0) last=0;
                buffer= ByteBuffer.allocate(1024);
                buffer.put(String.valueOf(last).getBytes(StandardCharsets.UTF_8));
                buffer.flip();
                assert channel != null;
                Future<Integer> fut = channel.write(buffer,0);

                while(!fut.isDone()) {}
                buffer.clear();

                try {
                    channel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                List<Chunk> stored = Utils.getStored(senderId);
                Utils.writeStored(senderId, stored);
            }

            if(header.getMessageType().name().equals(Utils.GETCHUNK)) {
                ExecutorService executor = Executors.newSingleThreadExecutor();
                ChunkCounter chunkCounter=new ChunkCounter(port, ipAddress, header.getFileId(),header.getChunkNo());
                Future<String> future = executor.submit(chunkCounter);

                int chunkMessages = 0;
                try {
                    int random = (int) (Math.random()*400);
                    future.get(random, TimeUnit.MILLISECONDS);
                    executor.shutdown();
                } catch(InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                } catch(TimeoutException e) {
                    chunkMessages=chunkCounter.counter;
                }
               // System.out.println("Stored messages received in file "+ header.getFileId()+"("+header.getChunkNo()+"): " + storedMessages);

                if(chunkMessages == 0) {
                    List<byte[]> chunks = FileChunkManager.createChunks("../../resources/peer" + senderId+"/"+header.getFileId()+"("+header.getChunkNo()+")");
                    if(chunks.size() != 0) {
                        //System.out.print("Incoming message before sending: "+header.toString());
                        Message message = new Message("1.0", MessageType.CHUNK, senderId, header.getFileId(), header.getChunkNo(), chunks.get(0));
                        try {
                            message.send(ipMDR, portMDR);
                        } catch (IOException e) { e.printStackTrace(); }
                    }
                }
            }

            if(header.getMessageType().name().equals(Utils.REMOVED)) {
                //System.out.println("Received remove msg");
                ExecutorService executor = Executors.newSingleThreadExecutor();
                PutchunkCounter putchunkCounter=new PutchunkCounter(port, ipAddress, header.getFileId(),header.getChunkNo(),header.getSenderId());
                Future<String> future = executor.submit(putchunkCounter);

                int storedMessages = 0;
                try {
                    int random = (int) (Math.random()*400);
                    future.get(random, TimeUnit.MILLISECONDS);
                    executor.shutdown();
                } catch(InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                } catch(TimeoutException e) {
                    storedMessages = putchunkCounter.counter;
                }

                if(Utils.hasStored(senderId,header.getFileId(), header.getChunkNo()) && storedMessages==0) {
                    String path="../../resources/peer"+senderId+"/"+header.getFileId()+"("+header.getChunkNo()+")";
                    File inputFile = new File(path);

                    byte[] byteChunkPart;
                    FileInputStream inputStream;
                    int readLength = Utils.CHUNK_SIZE_B;
                    int prevSize = 0, chunkCount = 0, read;
                    int fileSize = (int) inputFile.length();
                    byte[] content = null;

                    try {
                        inputStream = new FileInputStream(inputFile);
                        byteChunkPart = new byte[readLength];
                        inputStream.read(byteChunkPart, 0, readLength);
                        content = byteChunkPart;
                        inputStream.close();
                    }
                    catch (IOException exception) {
                        exception.printStackTrace();
                    }

                    Message message = new Message("1.0", MessageType.PUTCHUNK,senderId, header.getFileId(), header.getChunkNo(), 1 ,content);
                    Executors.newFixedThreadPool(Utils.THREADS_PER_CHANNEL);
                    Thread thread = new Thread(new StoredHandler(ipAddress, port, 1, header.getFileId(), header.getChunkNo(), senderId, path, message, ipMDB, portMDB, false));
                    thread.start();

                    //System.out.println("IP: "+ipMDB+" PORT: "+portMDB);
                    //BackupEnd backupEnd = new BackupEnd(threadList,senderId);
                    //Thread endBackupThread = new Thread(backupEnd);
                    //endBackupThread.start();
                }
            }
        }
    }
}

