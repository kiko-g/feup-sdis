import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class FileChunkManager {
    private String filePath;

    protected static List<byte[]> createChunks(String filePath) {
        File inputFile = new File(filePath);
        if(!inputFile.exists())
            return new ArrayList<>();

        byte[] byteChunkPart;
        FileInputStream inputStream;
        int readLength = Utils.CHUNK_SIZE_B;
        int prevSize = 0, chunkCount = 0, read;
        int fileSize = (int) inputFile.length();
        List<byte[]> content = new ArrayList<>();

        try {
            inputStream = new FileInputStream(inputFile);
            while (fileSize > 0) {
                if (fileSize <= readLength) {
                    readLength = fileSize;
                }
                byteChunkPart = new byte[readLength];
                read = inputStream.read(byteChunkPart, 0, readLength);
                prevSize = read;
                fileSize -= read;
                assert (read == byteChunkPart.length);
                chunkCount++;
                content.add(byteChunkPart);
            }

            inputStream.close();
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }

        if(prevSize == Utils.CHUNK_SIZE_B) content.add(new byte[0]); // add extra empty chunk on the event that space is multiple of 64K
        return content;
    }


    public static int countChunks(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        long result = Files.size(path);

        int ret = 0;
        while(result > 0) {
            ret++;
            result = result - Utils.CHUNK_SIZE_B;
        }

        return ret;
    }


    protected static void createFile(int bodyStart, byte[] content, MessageHeader header, int senderId, int replicationDeg) {
        String directoryName = "../../resources/peer" + senderId + "/";
        File directory = new File(directoryName);
        if (!directory.exists()) {
            directory.mkdir();
        }

        int bodySize = content.length - bodyStart;
        byte[] body = new byte[bodySize];
        System.arraycopy(content, bodyStart, body, 0, bodySize);

        try (FileOutputStream fos = new FileOutputStream(directoryName + header.getFileId() + "(" + header.getChunkNo()+")")) {
            fos.write(body);
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        if(replicationDeg==0)
            return;
        try {
            double size = bodySize;
            size = size/1000;

            /*File file=new File(directoryName+"stored_chunks.txt");
            file.createNewFile();*/

            String line = header.getFileId()+" "+header.getChunkNo()+" "+(int)size+" "+replicationDeg+"\n";
            String storedFilePath = directoryName + "stored_chunks.txt";


            if(new File(storedFilePath).exists()) {
                Files.write(Paths.get(storedFilePath), line.getBytes(), StandardOpenOption.APPEND);
            }
            else {
                FileWriter fileWriter = new FileWriter(storedFilePath);
                fileWriter.write(line);
                fileWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    protected static int findBodyStart(byte[] writableBody) {
        int bodyStart = 0;
        byte prev1 = writableBody[0];
        byte prev2 = writableBody[1];
        byte prev3 = writableBody[2];

        for (int i = 3; i < writableBody.length; i++) {
            if(writableBody[i] == (byte)0xA) {
                if(prev1 == (byte)0xD) {
                    if(prev2 == (byte)0xA) {
                        if(prev3 == (byte)0xD) {
                            bodyStart = i+1;
                            break;
                        }
                    }
                }
            }
            prev1 = writableBody[i-2];
            prev2 = writableBody[i-1];
            prev3 = writableBody[i];
        }

        return bodyStart;
    }
}
