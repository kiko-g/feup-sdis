import java.net.*;
import java.util.HashMap;
import java.io.IOException;

public class Server {
    private static DatagramSocket socket; // socket for communication between server and client
    private static HashMap<String, String> addressTable; // dns table with pairs <Name, IP>

    public static void main(String[] args) {
        // check arguments
        if(args.length < 1) {
            System.out.println("Invalid number of arguments!");
            printUsage();
            System.exit(-1);
        }

        // attempt to parse port number
        // port number
        int port = Integer.parseInt(args[0]);
        if(port< 0 || port> 65535) {
            System.out.println("Port specified is invalid (should be a number between 0 and 65535)");
            System.exit(-2);
        }

        // attempt to open socket
        try {
            socket = new DatagramSocket(port);
        } catch(SocketException e) {
            System.out.println("Could not open datagram socket");
            System.exit(-3);
        }

        // create dns table with dummy values
        addressTable = new HashMap<>();
        addressTable.put("www.alpha.com", "1.0.0.0");
        addressTable.put("www.beta.com", "2.0.0.0");
        addressTable.put("www.charlie.com", "3.0.0.0");
        addressTable.put("www.delta.com", "4.0.0.0");

        // process requests from client
        try {
            processRequests();
        } 
        catch(IOException e) {
            System.out.println("Communication with client error");
            System.exit(-4);
        }

        socket.close();
    }


    private static void processRequests() throws IOException {
        byte[] data = new byte[512];
        DatagramPacket packet = new DatagramPacket(data, data.length);

        while(true) {
            System.out.println("\nListening to requests...");
            socket.receive(packet);

            String request = new String(packet.getData());
            System.out.println("Server: " + request.trim());

            String answer = generateAnswer(request.trim());
            sendAnswer(answer, packet);

            byte[] buffer = new byte[512];
            packet.setData(buffer);
        }
    }


    private static String generateAnswer(String request) {
        String[] requestArgs = request.split(" "); //< operation> <DNS> (<IP>)
        if(requestArgs.length < 2) return "-1"; // exit right away

        switch(requestArgs[0]) {
            case "register":
                if(requestArgs.length < 3) return "-1";
                return processRegister(requestArgs[1], requestArgs[2]);

            case "lookup":
                return processLookup(requestArgs[1]);

            default:
                System.out.println("Invalid requested operation");
                printUsage();
                return "-1";
        }
    }


    private static String processRegister(String DNSName, String IPAddress) {
        addressTable.put(DNSName, IPAddress); // register answer
        System.out.println("== Updated table ==="); // inform about the update
        addressTable.forEach((key, value) -> System.out.println(key + "\t" + value));

        return String.valueOf(addressTable.size());
    }

    private static String processLookup(String DNSName) {
        String IPAddress = addressTable.get(DNSName); // look for answer 
        return IPAddress != null ? (DNSName + " -> " + IPAddress) : "No entry"; // send answer
    }

    private static void sendAnswer(String answer, DatagramPacket packet) throws IOException {
        packet.setData(answer.getBytes());
        socket.send(packet);
    }


    private static void printUsage() {
        System.out.println("Improper usage of server!");
        System.out.println("Use 1 argument (any extra args will be ignored)\n");
        System.out.println("Follow this format:");
        System.out.println("<Port>\n");
        System.out.println("Examples:");
        System.out.println("8000");
    }
}