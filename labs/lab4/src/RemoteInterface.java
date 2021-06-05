import java.rmi.*;

public interface RemoteInterface extends Remote {
    String lookup(String DNSName) throws RemoteException;
    String register(String DNSName, String IPAddress) throws RemoteException;
}