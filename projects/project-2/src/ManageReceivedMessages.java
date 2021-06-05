import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ManageReceivedMessages implements Runnable {
    private Message message;

    public ManageReceivedMessages(Message message) {
        this.message = message;
    }

    
    // checks the message type and then creates a new thread to treat that message
    public void run() {
        //System.out.println("INSIDE MESSAGE MANAGER");
        // message: <Version> <MessageType> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF>
        String[] header = this.message.getHeader();
        //System.out.println("MESSAGE: " + header[0] + " " + header[1] + " " + header[2] + " " + header[3] + " " + header[4]);

        switch (header[1]) {
            case "FINDSUCC":
                Peer.getThreadExec().execute(new FindSuccThread(this.message));
                break;

            case "SUCCFOUND":
                Peer.getThreadExec().execute(new SuccFoundThread(this.message));
                break;

            case "FINDPRED":
                Peer.getThreadExec().execute(new FindPredThread(this.message));
                break;

            case "PREDFOUND":
                Peer.getThreadExec().execute(new PredFoundThread(this.message));
                break;

            case "NOTIFY":
                Peer.getThreadExec().execute(new NotifyThread(this.message));
                break;

            case "PREDALIVE":
                Peer.getThreadExec().execute(new PredAliveThread(this.message));
                break;

            case "ALIVE":
                Peer.getThreadExec().execute(new AliveThread(this.message));
                break;

            case "PUTCHUNK":
                /*Random r = new Random();
                int low = 0;
                int high = 400;
                int result = r.nextInt(high-low) + low;*/

                //Peer.getThreadExec().schedule((new PutChunkMessageThread(this.message)), result, TimeUnit.MILLISECONDS);
                Peer.getThreadExec().execute(new PutChunkMessageThread(this.message));
                break;

            case "STORED":
                Peer.getThreadExec().execute(new StoredMessageThread(this.message));
                break;

            case "DELETE":
                Peer.getThreadExec().execute(new DeleteMessageThread(this.message));
                break;

            case "GETCHUNK":
                Peer.getThreadExec().execute(new GetChunkMessageThread(this.message));
                break;

            case "CHUNK":
                Peer.getThreadExec().execute(new ChunkMessageThread(this.message));
                break;

            case "REMOVED":
                Peer.getThreadExec().execute(new RemovedMessageThread(this.message));
                break;

            default:
                System.out.println("Received Unknown Message Type");
                break;
        }


    }
    
}
