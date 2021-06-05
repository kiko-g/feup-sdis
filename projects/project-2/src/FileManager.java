import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;


public class FileManager implements java.io.Serializable {
    private String path;
    private int replication;
    private File file;
    private String fileID;
    private ArrayList<Chunk> fileChunks; 
    private int peerId;
    private static final long serialVersionUID = 4066270093854086490L;


    public FileManager(String path, int replication, int peerId) {
        this.path = path;
        this.replication = replication;
        this.fileChunks = new ArrayList<>();
        this.peerId = peerId;

        this.file = new File(path);
        String tempFileID = createFileID();
        this.fileID = createHash256(tempFileID);
        splitFile();
    }

    public FileManager(String path, int peerId) {
        this.path = path;
        this.fileChunks = new ArrayList<>();
        this.peerId = peerId;

        this.file = new File(path);
        String tempFileID = createFileID();
        this.fileID = createHash256(tempFileID);
        splitFile();
    }


    public ArrayList<Chunk> getFileChunks() {
        return this.fileChunks;
    }


    public String getFileID() {
        return this.fileID;
    }


    public String getPath() {
        return this.path;
    }


    public File getFile() {
        return this.file;
    }

    
    public int getReplication() {
        return this.replication;
    }
    

    // create fileId using filename, file parent, last modified time and file size
    public String createFileID() {
        String fileName = this.file.getName();
        String fileParent = this.file.getParent();
        long lastModifiedTime = this.file.lastModified();
        long fileSize = this.file.length();

        return this.peerId + "__" + fileParent + "__" + fileName + "__" + lastModifiedTime + "__" + fileSize;
    }


    // convert fileId into an hash
    // https://www.baeldung.com/sha-256-hashing-java
    public String createHash256(String toHash) {
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(toHash.getBytes(StandardCharsets.UTF_8));
            StringBuilder buf = new StringBuilder();

            for(byte hash : encodedHash) {
                String hex = Integer.toHexString(0xff & hash);
                if(hex.length() == 1) {
                    buf.append('0');
                }
                buf.append(hex);
            }
            return buf.toString();
        } catch(Exception e) {
            throw new RuntimeException(e);
        } 
    }


    // split file into small chunks
    // http://all-aboutl.blogspot.com/2012/06/how-to-split-large-files-into-smaller.html
    public void splitFile() {
        int readBytes;
        byte[] buf = new byte[64000];
        int chunkNo = 0;

        try(FileInputStream fileInputStream = new FileInputStream(this.file); BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) {
            while((readBytes = bufferedInputStream.read(buf)) > 0) {
                byte[] chunkMessage = Arrays.copyOf(buf, readBytes);

                Chunk chunk = new Chunk(fileID, chunkNo, chunkMessage, this.replication, readBytes, new InetSocketAddress(Peer.getChordNode().getNodeInfo().getIp(), Peer.getChordNode().getNodeInfo().getPort()));
                this.fileChunks.add(chunk);

                buf = new byte[64000];
                chunkNo++;
            }

            // The maximum size of each chunks 64KByte (where K stands for 1000). All chunks of a file, except possibly the last
            // one, have the maximum size. The size of the last chunk is always shorter than that size. If the file size is a 
            // multiple of the chunk size, the last chunk has size 0
            if(this.file.length() % 64000 == 0) {
                Chunk chunk = new Chunk(fileID, chunkNo, null, this.replication, 0, new InetSocketAddress(Peer.getChordNode().getNodeInfo().getIp(), Peer.getChordNode().getNodeInfo().getPort()));
                this.fileChunks.add(chunk);
                chunkNo++;
            }
        } catch(IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
