import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MC extends MulticastThread {
    private int timeout;
    private int storedReceived = 0;
    private int portMDR;
    private String ipMDR;
    private int portMDB;
    private String ipMDB;

    MC(int senderId, int port, String IP, int portMDR,String ipMDR, int portMDB,String ipMDB) {
        this.IP = IP;
        this.port = port;
        this.senderId = senderId;
        this.portMDR = portMDR;
        this.ipMDR = ipMDR;

        this.portMDR = portMDR;
        this.ipMDR = ipMDR;

        this.portMDB=portMDB;
        this.ipMDB=ipMDB;
        this.thread = new Thread(this, "MC Thread");
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


            new Thread(new MCMessageHandler(packet, IP, port, ipMDR, portMDR,ipMDB, portMDB, senderId)).start();
        }
    }


    public void createSocket() {
        try {
            multicastSocket = new MulticastSocket(port);
            multicastSocket.joinGroup(InetAddress.getByName(IP));
        }
        catch (IOException e) {
            e.printStackTrace();
            System.err.println("MC: Multicast socket error");
        }
    }

    public void setAvailableSpace(double availableSpace) { }
}
