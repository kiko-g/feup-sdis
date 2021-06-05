import java.io.IOException;
import java.net.*;
//TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2>

public class TestApp {
    private static int port;
    private static String host;
    private static DatagramSocket clientSocket;
    private static InetAddress address;

    public static void main(String[] args) {
        String peerAccessPoint = args[0];           //host:port
        String operation = args[1].toUpperCase();   //operation

        if(!peerAccessPoint.contains(":")) {
            host = "localhost";
            port = Integer.parseInt(peerAccessPoint);
        }
        else {
            host = peerAccessPoint.split(":")[0];
            port = Integer.parseInt(peerAccessPoint.split(":")[1]);
        }


        try {
            address = InetAddress.getByName(host);
            clientSocket = new DatagramSocket();
        }
        catch(SocketException e) {
            System.out.println("Error opening Socket in port " + port);
            e.printStackTrace();
        }
         catch(UnknownHostException e) {
             System.out.println("Unknown host " + host);
            e.printStackTrace();
        }


        switch(operation) {
            case Utils.BACKUP:
                checkUsage(operation, args);
                requestBackup(args[2], Integer.parseInt(args[3]));
                break;

            case Utils.RESTORE:
                checkUsage(operation, args);
                requestRestore(args[2]);
                break;

            case Utils.DELETE:
                checkUsage(operation, args);
                requestDelete(args[2]);
                break;

            case Utils.STATE:
                checkUsage(operation, args);
                requestState();
                break;

            case Utils.RECLAIM:
                checkUsage(operation, args);
                requestReclaim(Integer.parseInt(args[2]));
                break;

            default:
                checkUsage("none", null);
                break;
        }

        clientSocket.close();
    }


    private static void requestBackup(String filePath, int replicationDegree) {
        communicate("BACKUP "+filePath + " " + replicationDegree);
    }

    private static void requestRestore(String filePath) {
        communicate("RESTORE "+filePath + " ");
    }

    private static void requestDelete(String filePath) {
        communicate("DELETE "+filePath+" ");
    }

    private static void requestReclaim(int diskSpaceInKB) {
        communicate("RECLAIM "+diskSpaceInKB);
    }

    private static void requestState() {
        communicate("STATE ");
    }

    private static void communicate(String requestString) {
        // extracted method from request backup (might be helpful for other requests - less duplicated code)
        byte[] outgoingBuffer = requestString.getBytes();
        byte[] incomingBuffer = new byte[256];

        DatagramPacket requestPacket = new DatagramPacket(outgoingBuffer,outgoingBuffer.length, address, port);
        DatagramPacket responsePacket = new DatagramPacket(incomingBuffer, incomingBuffer.length);

        try {
            clientSocket.send(requestPacket);
            clientSocket.receive(responsePacket);
            System.out.println(Utils.byteArrayToString(responsePacket.getData())); //print answer
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }



    private static void checkUsage(String operation, String[] testAppArgs) {
        switch(operation) {
            case Utils.BACKUP:
                if(testAppArgs.length != 4) printUsage(operation);
                break;

            case Utils.RESTORE:
            case Utils.RECLAIM:
            case Utils.DELETE:
               /* if(testAppArgs.length != 3) printUsage(operation);*/
                break;

            case Utils.STATE:
                if(testAppArgs.length != 2) printUsage(operation);
                break;

            default:
                printUsage(operation);
                break;
        }
    }


    private static void printUsage(String operation) {
        switch(operation) {
            case Utils.BACKUP:
                System.out.println("\nUse 4 arguments for 'BACKUP' operation");
                System.out.println("Follow this format:");
                System.out.println("<Peer Access Point> BACKUP <Path to file> <Replication Degree>\n");
                System.out.println("Examples:");
                System.out.println("java TestApp 1923 backup path/to/file.pdf 2");
                System.exit(-2);
                break;

            case Utils.RESTORE:
                System.out.println("\nUse 3 arguments for 'RESTORE' operation");
                System.out.println("Follow this format:");
                System.out.println("<Peer Access Point> RESTORE <Path to file>\n");
                System.out.println("Examples:");
                System.out.println("java TestApp 1923 restore path/to/file.pdf");
                System.exit(-3);
                break;

            case Utils.DELETE:
                System.out.println("\nUse 3 arguments for 'DELETE' operation");
                System.out.println("Follow this format:");
                System.out.println("<Peer Access Point> DELETE <Path to file>\n");
                System.out.println("Examples:");
                System.out.println("java TestApp 1923 delete path/to/file.pdf");
                System.exit(-4);
                break;

            case Utils.RECLAIM:
                System.out.println("\nUse 3 arguments for 'RECLAIM' operation");
                System.out.println("Follow this format:");
                System.out.println("<Peer Access Point> RECLAIM <Max disk space used (in KByte)>\n");
                System.out.println("Examples:");
                System.out.println("java TestApp 1923 reclaim 0");
                System.exit(-5);
                break;

            case Utils.STATE:
                System.out.println("\nUse 2 arguments for 'STATE' operation");
                System.out.println("Follow this format:");
                System.out.println("<Peer Access Point> STATE\n");
                System.out.println("Examples:");
                System.out.println("java TestApp 1923 state");
                System.exit(-6);
                break;

            default:
                System.out.println("\nInvalid requested operation.");
                System.out.println("Available operations:");
                System.out.println("- Backup");
                System.out.println("- Restore");
                System.out.println("- Delete");
                System.out.println("- Reclaim");
                System.out.println("- State");
                System.out.println("Found: " + operation);
                System.exit(-1);
                break;
        }
    }
}