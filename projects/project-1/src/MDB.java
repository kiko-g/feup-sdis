import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MDB extends MulticastThread {
    int portMC;
    String ipMC;
    double availableSpace;

    MDB(int senderId, int port, String IP, int portMC, String ipMC, double availableSpace) {
        this.IP = IP;
        this.port = port;
        this.ipMC = ipMC;
        this.portMC = portMC;
        this.senderId = senderId;
        this.thread = new Thread(this, "MDB Thread");
        this.thread.start();
        this.availableSpace = availableSpace;
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

            new Thread(new MDBMessageHandler(packet, ipMC, portMC, senderId, availableSpace)).start();
        }
    }


    @Override
    public void createSocket() {
        try {
            multicastSocket = new MulticastSocket(port);
            multicastSocket.joinGroup(InetAddress.getByName(IP));
        }
        catch (IOException e) {
            e.printStackTrace();
            System.err.println("MDB: Error in multicast socket");
        }
    }

    public void setAvailableSpace(double availableSpace) {
        this.availableSpace = availableSpace;
    }
}
