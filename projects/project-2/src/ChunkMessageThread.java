public class ChunkMessageThread implements Runnable {
    private Message message;
    private int senderId;
    private String address;
    private int port;
    private String fileId;
    private int chunkNo;
    private String protocolVersion;
    private byte[] body;

    public ChunkMessageThread(Message message) {
        this.message = message;
        String[] header = this.message.getHeader();
        this.senderId = Integer.parseInt(header[2]);
        this.address = header[3];
        this.port = Integer.parseInt(header[4]);
        this.fileId = header[5];
        this.chunkNo = Integer.parseInt(header[6]);
        this.protocolVersion = header[0];
        this.body = this.message.getBody();
    }

    @Override
    public void run() {
        String chunkID = this.fileId + "_" + this.chunkNo;
        if(!Peer.getStorage().hasRegisterToRestore(chunkID)) {
            Peer.getStorage().addChunkToRestore(chunkID, this.body);

        }
        else {
            System.out.println("Chunk " + chunkNo + " not requested or already have been restored");
            System.out.println();
        }
    }
}
