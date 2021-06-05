import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;

public class Server {
    public static void main(String[] args) {
        // check arguments
        if(args.length < 1) {
            System.out.println("Wrong number of arguments!");
            printUsage();
            System.exit(-1);
        }

        // parse arguments
        String remoteObjectName = args[0]; // service name

        try {
            RemoteObject remoteObject = new RemoteObject();
            RemoteInterface stub = (RemoteInterface) UnicastRemoteObject.exportObject(remoteObject, 4455);

            Registry registry = LocateRegistry.createRegistry(1099); // locate codebase
            registry.bind(remoteObjectName, stub);
        }
        catch(AlreadyBoundException e) {
            e.printStackTrace();
            System.out.println("Service name (remote object name: " +  remoteObjectName + ") is already bound.");
            System.out.println("Maybe choose another service");
        }
        catch(RemoteException e) {
            e.printStackTrace();
            System.out.println("Error in RMI Registry");
        }
    }



    private static void printUsage() {
        System.out.println("Improper usage of server!");
        System.out.println("Use 1 argument (any extra args will be ignored)\n");
        System.out.println("Follow this format:");
        System.out.println("<Remote Object Name>\n");
        System.out.println("Examples:");
        System.out.println("My_DNS_Service");
    }
}