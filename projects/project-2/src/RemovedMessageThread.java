import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class RemovedMessageThread implements Runnable {
    private Message message;
    private String[] header;
    private int senderId;
    private String fileId;
    private int chunkNo;
    private int replication_degree;
    private String protocolVersion;
    private String address;
    private int port;
    
    // Version REMOVED <SenderId> <Address> <Port> <FileId> <ChunkNo>
    public RemovedMessageThread(Message message) {
        this.message = message;
        this.header = this.message.getHeader();
        this.protocolVersion = this.header[0];
        this.senderId = Integer.parseInt(this.header[2]);
        this.address = this.header[3];
        this.port = Integer.parseInt(header[4]);
        this.fileId = this.header[5];
        this.chunkNo = Integer.parseInt(header[6]);
    }


	@Override
	public void run() {
		System.out.println("RECEIVED: " + this.protocolVersion + " REMOVED " + this.senderId + " " + this.address + " " + this.port + " " + this.fileId + " " + this.chunkNo);
        System.out.println();
        
        Peer.getStorage().removeBackupFileDistribution(this.address, this.port, this.fileId, this.chunkNo);
        String chunkId = this.fileId + "_" + this.chunkNo;
        int storedReplications = Peer.getStorage().getBackupChunksDistribution().get(chunkId).size();
        
        ArrayList<FileManager> files = Peer.getStorage().getFilesStored();

        FileManager file = null;

        for(int i = 0; i < files.size(); i++) {
            if(files.get(i).getFileID().equals(this.fileId)) {
                file = files.get(i);
                break;
            }
        }

        if(file == null) {
            return;
        }

        Chunk chunk = null;

        for(int i = 0; i < file.getFileChunks().size(); i++) {
            if(file.getFileChunks().get(i).getChunkNo() == this.chunkNo) {
                chunk = file.getFileChunks().get(i);
                break;
            }
        }

        if(chunk == null) {
            return;
        }

        if(chunk.getReplication() <= storedReplications) {
            System.out.println("Correct replication. Doesn't need to replicate.");
            return;
        }

        MessageBuilder messageBuilder = new MessageBuilder();
        byte[] message = messageBuilder.constructPutChunkMessage(Peer.getChordNode().getNodeInfo().getIp(), Peer.getChordNode().getNodeInfo().getPort(), this.fileId, this.chunkNo, this.replication_degree, chunk.getChunkMessage());

        int replicationsNeeded = chunk.getReplication() - storedReplications;

        for(int i = 0; i < replicationsNeeded; i++) {
            NodeInfo receiver;
            if(i < Peer.getChordNode().getFingerTableLength()) {
                receiver = Peer.getChordNode().getFingerTable().get(i);
            }
            else {
                receiver = Peer.getChordNode().getSuccessor();
            }
            
            // send threads
            Peer.getThreadExec().execute(new ThreadSendMessages(receiver.getIp(), receiver.getPort(), message));
            Peer.getThreadExec().schedule(new ThreadCountStored(chunk.getReplication(), file.getFileID(), i, message, receiver), 1, TimeUnit.SECONDS);
        }
	}
    
}