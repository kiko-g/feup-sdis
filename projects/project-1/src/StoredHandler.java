import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.Date;

public class StoredHandler implements Runnable {

    int port;
    String ipAddress;
    List<byte[]> allChunks = new ArrayList<>();
    int replicationDeg;
    Thread thread;
    String fileID, filePath;
    int chunkNo;
    int peerId;
    Message message;
    String ipMDB;
    int portMDB;
    boolean log;

    StoredHandler(String ipAddress, int port, int replicationDeg, String fileID, int chunkNo, int peerId, String filePath, Message message,String ipMDB, int portMDB, boolean log) {
        this.port = port;
        this.ipAddress = ipAddress;
        this.replicationDeg = replicationDeg;
        this.fileID = fileID;
        this.chunkNo = chunkNo;
        this.peerId = peerId;
        this.filePath = filePath;
        this.ipMDB = ipMDB;
        this.portMDB = portMDB;
        this.message = message;
        this.log = log;
    }

    @Override
    public void run() {
        int storedMessages = 0;
        Date startDate = new Date();

        while(storedMessages < replicationDeg) {
            for(int i = 0; i < 5;i++) {
                try {
                    message.send(ipMDB, portMDB);
                } catch (IOException e) { e.printStackTrace(); }

                ExecutorService executor = Executors.newSingleThreadExecutor();
                InitiatorStoredCounter initiatorStoredCounter=new InitiatorStoredCounter(port, ipAddress, fileID,chunkNo);
                Future<String> future = executor.submit(initiatorStoredCounter);

                try {
                    //System.out.print("Result: ");
                    future.get(1, TimeUnit.SECONDS);
                    storedMessages += initiatorStoredCounter.counter;
                    //System.out.println("Stored messages for chunk " + chunkNo + ": "+storedMessages);
                    executor.shutdown();
                } catch(InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                } catch(TimeoutException e) {
                    storedMessages += initiatorStoredCounter.counter;
                    if(initiatorStoredCounter.counter != 0) startDate = new Date();
                    //System.out.println("Stored messages for chunk " + chunkNo + ": "+storedMessages);
                }

                if(storedMessages >= replicationDeg) break;
            }

            Date now = new Date();
            if((int)((now.getTime() - startDate.getTime()) / 1000) > 30) {
                //System.out.println("Waited 10 seconds for STORED messages. Replication Degree compromised");
                break;
            }
        }

        String line = filePath + " " + fileID + " " + chunkNo + " " + storedMessages + " " + replicationDeg + "\n";
        String replicationFilePath = "../../resources/peer"+peerId+"/replication_degrees" + chunkNo + ".txt";
        try {
            File directory = new File("../../resources/peer" + peerId + "/");
            if(!directory.exists()) {
                directory.mkdir();
            }

            if(this.log) {
                if(new File(replicationFilePath).exists()) {
                    Files.write(Paths.get(replicationFilePath), line.getBytes(), StandardOpenOption.APPEND);
                }
                else {
                    FileWriter fileWriter = new FileWriter(replicationFilePath);
                    fileWriter.write(line);
                    fileWriter.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
