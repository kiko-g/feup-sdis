import java.net.InetAddress;

public class ThreadSendMessages implements Runnable {
    private byte[] message;
    private ChannelController channel;
    
    public ThreadSendMessages(String address, int port, byte[] message) {
        this.message = message;
        this.channel = new ChannelController(address, port);
    }

    @Override
    public void run() {
        this.channel.sendMessage(this.message);
    }
}
