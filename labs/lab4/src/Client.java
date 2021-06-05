import java.rmi.*;
import java.util.*;
import java.rmi.registry.*;

public class Client {
    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("Wrong number of arguments");
            printUsage();
            System.exit(-1);
        }

        String host = args[0];
        String remoteObjectName = args[1];
        String operation = args[2];
        String DNSName = args[3];

        try {
            Registry registry = LocateRegistry.getRegistry(host);
            RemoteInterface stub = (RemoteInterface) registry.lookup(remoteObjectName);

            // select operation
            switch(operation) {
                case "register":
                    if(args.length < 5) {
                        System.out.println("Wrong number of arguments");
                        printUsage();
                        System.exit(-1);
                    }

                    String IPAddress = args[4];
                    String serverReply = stub.register(DNSName, IPAddress);
                    showRequestAndReply(operation, Arrays.asList(DNSName, IPAddress), serverReply);
                    break;

                case "lookup":
                    serverReply = stub.lookup(DNSName);
                    showRequestAndReply(operation, Collections.singletonList(DNSName), serverReply);
                    break;

                default:
                    System.out.println("Invalid requested operation");
                    printUsage();
                    System.exit(-2);
            }
        }
        catch(UnknownHostException e) {
            e.printStackTrace();
            System.out.println("Host: " +  host + " is unknown");
        }
        catch(NotBoundException e) {
            e.printStackTrace();
            System.out.println("Service name (remote object name: " +  remoteObjectName + ") is not bound.");
            System.out.println("Service name from Client might not match service name for Server");
        }
        catch(RemoteException e) {
            e.printStackTrace();
            System.out.println("Error in RMI lookup/register");
        }
    }



    public static void showRequestAndReply(String operation, List<String> extraArgs, String serverReply) {
        System.out.println("Request :: Reply");

        StringJoiner result = new StringJoiner(" ");
        result.add(operation);
        for(String extraArg : extraArgs) {
            result.add(extraArg);
        }
        result.add("::").add(serverReply);

        System.out.println(result.toString());
    }


    private static void printUsage() {
        System.out.println("Improper usage of RMI client!");
        System.out.println("Use 4 arguments for 'lookup' operation or 5 for 'register' operation (any extra args will be ignored)\n");
        System.out.println("Follow this format:");
        System.out.println("<Host> <RemoteObjectName> <Operation> <DNS Name> (<IP Address>)\n");
        System.out.println("Examples:");
        System.out.println("localhost My_DNS_Service lookup www.alpha.com");
        System.out.println("127.0.0.1 My_DNS_Service register www.zeta.com 26.0.0.0");
    }
}