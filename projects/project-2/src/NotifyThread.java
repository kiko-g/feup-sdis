import java.math.BigInteger;
import java.net.InetSocketAddress;

public class NotifyThread implements Runnable {
    Message message;

    public NotifyThread(Message message) {
        this.message = message;
    }


    @Override
	public void run() {
        //System.out.println("INSIDE NotifyThread");
        // Version + NOTIFY + nodeId + address + port 
        String[] header = this.message.getHeader();
        
        System.out.println("RECEIVED: " + header[0] + " " + header[1] + " " + header[2] + " " + header[3] + " " + header[4]);

        BigInteger nodeId = new BigInteger(header[2]);
        String address = header[3];
        int port = Integer.parseInt(header[4]);

        NodeInfo node = new NodeInfo(nodeId, new InetSocketAddress(address, port));

        Peer.getChordNode().notify(node);
    }
}
