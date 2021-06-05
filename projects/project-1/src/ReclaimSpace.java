import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Future;

public class ReclaimSpace implements Runnable {
    double space;
    double initialSpace;
    int senderId;
    String ipMC;
    int portMC;
    ReclaimSpace(double space, double initial_space, int senderId, String ipMC, int portMC) {
        this.space=space;
        this.initialSpace =initial_space;
        this.senderId=senderId;
        this.ipMC=ipMC;
        this.portMC=portMC;
    }

    @Override
    public void run() {
        double currentSpace=this.space;
        String filePath="../../resources/peer"+senderId+"/stored_chunks.txt";
        File file = new File(filePath);
        Path path = Paths.get(filePath);

        try {
            Scanner scanner = new Scanner(file);
            while(scanner.hasNextLine() && currentSpace > 0) {
                String data = scanner.nextLine();
                String [] line = data.split(" ");
                String fileId = line[0];
                String chunkNo = line[1];
                String space = line[2];
                File deleted = new File("../../resources/peer"+senderId+"/"+fileId+"("+chunkNo+")");
                if(deleted.exists()) {
                    currentSpace = currentSpace - Double.parseDouble(space);
                    if(deleted.delete());
                    Message message = new Message("1.0",MessageType.REMOVED,senderId,fileId,Integer.parseInt(chunkNo));
                    message.send(ipMC,portMC);
                }
            }
            scanner.close();

        } catch (IOException e) {
            e.printStackTrace();
        }



        path = Paths.get("../../resources/peer"+senderId+"/current_storage.txt");
        AsynchronousFileChannel channel = null;
        try {
           channel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteBuffer buffer;
        buffer= ByteBuffer.allocate(1024);
        buffer.put(String.valueOf(initialSpace - currentSpace).getBytes(StandardCharsets.UTF_8));
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
/*
        stored = Utils.getStored(senderId);
        Utils.writeStored(senderId, stored);

        stored = Utils.getStored(senderId);
        Utils.writeStored(senderId, stored);*/
    }
}
