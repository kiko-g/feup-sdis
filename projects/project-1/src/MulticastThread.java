import java.net.*;

public abstract class MulticastThread implements Runnable {
    int port;
    String IP;
    Thread thread;
    int senderId;
    MulticastSocket multicastSocket;

    @Override
    public abstract void run();
    public abstract void createSocket();
    public abstract void setAvailableSpace(double spaceInKBytes);
}