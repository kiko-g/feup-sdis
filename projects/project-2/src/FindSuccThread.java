import java.math.BigInteger;

public class FindSuccThread implements Runnable {
    Message message;
    public FindSuccThread(Message message) {
        this.message = message;
    }

	@Override
	public void run() {
        //System.out.println("INSIDE FindSuccThread");
        // Version + FINDSUCC + nodeId + address + port + isFinger (+ <fingerPos>)*
        String[] header = this.message.getHeader();
        
        if(Boolean.parseBoolean(header[5])) {
            System.out.println("RECEIVED: " + header[0] + " " + header[1] + " " + header[2] + " " + header[3] + " " + header[4] + " " + Boolean.parseBoolean(header[5]) + " " + header[6]);
        }
        else {
            System.out.println("RECEIVED: " + header[0] + " " + header[1] + " " + header[2] + " " + header[3] + " " + header[4] + " " + header[5]);
        }

        
        BigInteger nodeId = new BigInteger(header[2]);
        String address = header[3];
        int port = Integer.parseInt(header[4]);

        if(!Boolean.parseBoolean(header[5])){
            NodeInfo nodeInfo = Peer.getChordNode().findSuccessor(address, port, nodeId, false, -1);

            if(nodeInfo != null) {
                MessageBuilder messageBuilder = new MessageBuilder();
                byte[] response = messageBuilder.constructSuccessorFoundMessage(nodeInfo, false, -1);
                Peer.getThreadExec().execute(new ThreadSendMessages(address, port, response));
            }
        }
        else {
            NodeInfo nodeInfo = Peer.getChordNode().findSuccessor(address, port, nodeId, true, Integer.parseInt(header[6]));

            if(nodeInfo != null) {
                MessageBuilder messageBuilder = new MessageBuilder();
                byte[] response = messageBuilder.constructSuccessorFoundMessage(nodeInfo, true, Integer.parseInt(header[6]));
                Peer.getThreadExec().execute(new ThreadSendMessages(address, port, response));
            }
        }
	}
}