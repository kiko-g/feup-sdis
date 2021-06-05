import java.util.concurrent.ConcurrentHashMap;

public class GetChunkMessageThread implements Runnable {
    private Message message;
    private int senderId;
    private String address;
    private int port;
    private String fileId;
    private int chunkNo;
    private String protocolVersion;

    public GetChunkMessageThread(Message message) {
        this.message = message;
        String[] header = this.message.getHeader();
        this.senderId = Integer.parseInt(header[2]);
        this.address = header[3];
        this.port = Integer.parseInt(header[4]);
        this.fileId = header[5];
        this.chunkNo = Integer.parseInt(header[6]);
        this.protocolVersion = header[0];
    }

    @Override
    public void run() {
        String path = "peer_" + Peer.getPeerId() + "/backup/" + this.fileId + "_" + this.chunkNo;

        System.out.println("RECEIVED: " + this.protocolVersion + " GETCHUNK " + this.senderId + " " + this.address + " " + this.port + " " + this.fileId + " " + this.chunkNo);
        System.out.println("PATH: " + path);

        String chunkId = fileId + "_" + chunkNo;
        ConcurrentHashMap<String, Chunk> chunksStored = Peer.getStorage().getChunksStored();

        // checks if this peer has the chunk stored
        if(!(chunksStored.containsKey(chunkId))){
            System.out.println("Don't have chunk " + chunkNo + " stored");
            System.out.println();
            return;
        }

        byte[] body = chunksStored.get(chunkId).getChunkMessage(); // chamada do message builder e depois envia

        MessageBuilder messageBuilder = new MessageBuilder();
        byte[] message = messageBuilder.constructChunkMessage(
            Peer.getChordNode().getNodeInfo().getSocketAddress().getAddress().getHostAddress(),
            Peer.getChordNode().getNodeInfo().getPort(), this.fileId, this.chunkNo,
            body
        );

        Peer.getThreadExec().execute(new ThreadSendMessages(this.address, this.port, message));
    }
}
