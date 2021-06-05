import java.io.File;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class PutChunkMessageThread implements Runnable {
    private Message message;
    private String[] header;
    private byte[] body;
    private int senderId;
    private String fileId;
    private int chunkNo;
    private int replication_degree;
    private String protocolVersion;
    private String address;
    private int port;

    // <Version> PUTCHUNK <SenderId> <Address> <Port> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF> <Body>
    public PutChunkMessageThread(Message message) {
        this.message = message;
        this.header = this.message.getHeader();
        this.body = this.message.getBody();
        this.protocolVersion = this.header[0];
        this.senderId = Integer.parseInt(this.header[2]);
        this.address = this.header[3];
        this.port = Integer.parseInt(this.header[4]);
        this.fileId = this.header[5];
        this.chunkNo = Integer.parseInt(this.header[6]);
        this.replication_degree = Integer.parseInt(this.header[7]);
    }


	@Override
	public void run() {
        if(this.address.equals(Peer.getChordNode().getNodeInfo().getIp()) && this.port == Peer.getChordNode().getNodeInfo().getPort()) {
            System.out.println("Same peer as initiator. Can't backup chunk.");
            return;
        }
        
        System.out.println("RECEIVED: " + this.protocolVersion + " PUTCHUNK " + this.senderId + " " + this.address + " " + this.port + " " + this.fileId + " " + this.chunkNo + " " + this.replication_degree);
        System.out.println();

        //check if the peer already has stored this chunk
        // in this case, forwards the message to its successor
        if(Peer.getStorage().hasChunk(this.fileId, this.chunkNo) == true) {
            System.out.println("Already has chunk");
            System.out.println();

            Random r = new Random();
            int low = 0;
            int high = 400;
            int result = r.nextInt(high-low) + low;

            MessageBuilder builder = new MessageBuilder();
            byte[] toSend = builder.constructStoredMessage(Peer.getChordNode().getNodeInfo().getIp(), Peer.getChordNode().getNodeInfo().getPort(), this.fileId, this.chunkNo);
            Peer.getThreadExec().schedule(new ThreadSendMessages(this.address, this.port, toSend), result, TimeUnit.MILLISECONDS);

            // necessario enviar mensagem de stored?
            
            MessageBuilder messageBuilder = new MessageBuilder();
            byte[] message = messageBuilder.constructPutChunkMessage(this.address, this.port, this.fileId, this.chunkNo, this.replication_degree, this.body);

            NodeInfo receiver = Peer.getChordNode().getFingerTable().get(0);

            Peer.getThreadExec().execute(new ThreadSendMessages(receiver.getIp(), receiver.getPort(), message));
            
            return;
        }

        // checks if the peer has free space to save the chunk
        if(!(Peer.getStorage().checkIfHasSpace(this.body.length))) {
            System.out.println("Doesn't have space to store chunk " + this.chunkNo);
            System.out.println();

            MessageBuilder messageBuilder = new MessageBuilder();
            byte[] message = messageBuilder.constructPutChunkMessage(this.address, this.port, this.fileId, this.chunkNo, this.replication_degree, this.body);

            NodeInfo receiver = Peer.getChordNode().getFingerTable().get(0);

            Peer.getThreadExec().execute(new ThreadSendMessages(receiver.getIp(), receiver.getPort(), message));

            return;
        }


        Chunk chunk = new Chunk(this.fileId, this.chunkNo, this.body, this.replication_degree, this.body.length, new InetSocketAddress(this.address, this.port));

        Peer.getStorage().addChunk(chunk);

        // create the chunk file in the peer directory
        String dir = "peer_" + Peer.getPeerId();
        String backupDir = "peer_" + Peer.getPeerId() + "/" + "backup";
        String file = "peer_" + Peer.getPeerId() + "/" + "backup" + "/" + this.fileId + "_" + this.chunkNo;
        File directory = new File(dir);
        File backupDirectory = new File(backupDir);
        File f = new File(file);

        try{
            if (!directory.exists()){
                directory.mkdir();
                backupDirectory.mkdir();
                f.createNewFile();
            } 
            else {
                if (directory.exists()) {
                    if(backupDirectory.exists()) {
                        f.createNewFile();
                    }
                    else {
                        backupDirectory.mkdir();
                        f.createNewFile();
                    }
                } 
            }

            FileOutputStream fos = new FileOutputStream(f);
            fos.write(this.body);
            fos.close();

        } catch(Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }


        Random r = new Random();
        int low = 0;
        int high = 400;
        int result = r.nextInt(high-low) + low;

        MessageBuilder builder = new MessageBuilder();
        byte[] toSend = builder.constructStoredMessage(Peer.getChordNode().getNodeInfo().getIp(), Peer.getChordNode().getNodeInfo().getPort(), this.fileId, this.chunkNo);
        Peer.getThreadExec().schedule(new ThreadSendMessages(this.address, this.port, toSend), result, TimeUnit.MILLISECONDS);
	}
}