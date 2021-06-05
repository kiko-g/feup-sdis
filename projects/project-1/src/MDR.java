import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MDR extends MulticastThread {
    MDR(int senderId, int port, String IP) {
        this.IP = IP;
        this.port = port;
        this.senderId = senderId;
        this.thread = new Thread(this, "MDR Thread");
        this.thread.start();
    }

    @Override
    public void run() {
        byte[] buffer;
        DatagramPacket packet;
        createSocket();
        while(true) {
            buffer = new byte[Utils.MAX_BUFFER_SIZE];
            packet = new DatagramPacket(buffer, buffer.length);

            try {
                assert multicastSocket != null;
                multicastSocket.receive(packet);

            }
            catch (IOException e) {
                e.printStackTrace();
            }

            new Thread(new MDRMessageHandler(packet, IP, port, senderId)).start();
        }
    }

    public void createSocket() {
        try {
            System.out.println("Created mdr");
            multicastSocket = new MulticastSocket(port);
            multicastSocket.joinGroup(InetAddress.getByName(IP));
        }
        catch (IOException e) {
            e.printStackTrace();
            System.err.println("MDR: Multicast socket error");
        }
    }

    public void setAvailableSpace(double availableSpace) { }
}
