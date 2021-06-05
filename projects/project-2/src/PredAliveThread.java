import java.math.BigInteger;

public class PredAliveThread implements Runnable{
    Message message;

    public PredAliveThread(Message message) {
        this.message = message;
    }


    @Override
	public void run() {
        //System.out.println("INSIDE PredAliveThread");
        // Version PREDFOUND + nodeId + address + port
        String[] header = this.message.getHeader();
        
        System.out.println("RECEIVED: " + header[0] + " " + header[1] + " " + header[2] + " " + header[3] + " " + header[4]);

        BigInteger nodeId = new BigInteger(header[2]);
        String address = header[3];
        int port = Integer.parseInt(header[4]);

        MessageBuilder messageBuilder = new MessageBuilder();
        byte[] response = messageBuilder.constructAliveMessage(Peer.getChordNode().getNodeInfo());
        Peer.getThreadExec().execute(new ThreadSendMessages(address, port, response));
	}
}
