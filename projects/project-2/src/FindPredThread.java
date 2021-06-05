import java.math.BigInteger;

public class FindPredThread implements Runnable {
    Message message;

    public FindPredThread(Message message) {
        this.message = message;
    }


	@Override
	public void run() {
		//System.out.println("INSIDE FindPredThread");
        // Version FINDSUCC + nodeId + address + port
        String[] header = this.message.getHeader();
        
        System.out.println("RECEIVED: " + header[0] + " " + header[1] + " " + header[2] + " " + header[3] + " " + header[4]);

        BigInteger nodeId = new BigInteger(header[2]);
        String address = header[3];
        int port = Integer.parseInt(header[4]);

        NodeInfo predecessor = Peer.getChordNode().getPredecessor();

        MessageBuilder messageBuilder = new MessageBuilder();
        byte[] response = messageBuilder.constructPredecessorFoundMessage(predecessor);
        Peer.getThreadExec().execute(new ThreadSendMessages(address, port, response));
	}
    
}