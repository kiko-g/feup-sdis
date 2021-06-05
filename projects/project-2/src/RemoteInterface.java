import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteInterface extends Remote {
    void backup(String path, int replication) throws RemoteException;
    void restore(String path) throws RemoteException;
    void delete(String path) throws RemoteException;
    void reclaim(int maximum_disk_space) throws RemoteException;
    void state() throws RemoteException;
    void chord() throws RemoteException;
}