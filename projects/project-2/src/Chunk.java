import java.net.InetSocketAddress;

public class Chunk implements java.io.Serializable {
    // Each chunk is identified by the pair (fileId, chunkNo)
    private String fileId;
    private int chunkNo;
    private byte[] chunkMessage;
    private int replication;
    private int size;
    private InetSocketAddress address;
    private static final long serialVersionUID = 4066270093854086490L;  


    public Chunk(String fileId, int chunkNo, byte[] chunkMessage, int replication, int size) {
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.chunkMessage = chunkMessage;
        this.replication = replication;
        this.size = size;
    }

    public Chunk(String fileId, int chunkNo, byte[] chunkMessage, int replication, int size, InetSocketAddress address) {
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.chunkMessage = chunkMessage;
        this.replication = replication;
        this.size = size;
        this.address = address;
    }


    public String getFileId() {
        return this.fileId;
    }


    public int getChunkNo() {
        return this.chunkNo;
    }


    public byte[] getChunkMessage() {
        return this.chunkMessage;
    }


    public int getReplication() {
        return this.replication;
    }
    

    public int getSize() {
        return this.size;
    }

    public String getIp() {
        return this.address.getAddress().getHostAddress();
    }

    public int getPort() {
        return this.address.getPort();
    }
}
