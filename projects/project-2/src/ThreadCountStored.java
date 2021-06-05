import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ThreadCountStored implements Runnable {
    private int replication;
    private String fileId;
    private int chunkNo;
    private byte[] message;
    private int tries = 0;
    private int time = 1;
    private NodeInfo receiver;

    public ThreadCountStored(int replication, String fileId, int chunkNo, byte[] message, NodeInfo receiver) {
        this.replication = replication;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.message = message;
        this.receiver = receiver;
    }

	@Override
	public void run() {
        ConcurrentHashMap<String, ArrayList<InetSocketAddress>> distribution = Peer.getStorage().getBackupChunksDistribution();
        String chunkId = this.fileId + "_" + this.chunkNo;
        int storedReplications = 0;
        if(distribution.containsKey(chunkId)) {
            storedReplications = distribution.get(chunkId).size();
        }

        if(storedReplications < this.replication && this.tries < 4) {
            // <Version> PUTCHUNK <SenderId> <Address> <Port> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF> <Body>
            Peer.getThreadExec().execute(new ThreadSendMessages(this.receiver.getIp(), this.receiver.getPort(), this.message));
            String[] messageArr = (new String(this.message).toString()).split(" ");
            System.out.println("SENT: "+ messageArr[0] + " " + messageArr[1] + " " + messageArr[2] + " " + messageArr[3] + " " + messageArr[4] + " " + messageArr[5] + " " + messageArr[6] + " " + messageArr[7]);
            this.time = this.time * 2;
            this.tries++;
            Peer.getThreadExec().schedule(this, this.time, TimeUnit.SECONDS);
        }

        if(this.tries >= 4) {
            System.out.println("Minimum replication not achieved");
            return;
        }
        else if(storedReplications >= this.replication) {
            System.out.println("Replication completed: " + storedReplications);
            System.out.println();
            return;
        }
	}

}
    