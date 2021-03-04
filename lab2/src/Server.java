import java.net.*;
import java.util.*;
import java.io.IOException;
import java.util.concurrent.*;

public class Server {
    private static int servicePort; // port number
    private static int multicastPort; // multicast port number
    private static String multicastAddress; // multicast address string
    private static DatagramSocket socket; // socket for communication
    private static MulticastSocket multicastSocket; // multicast socket to send server port
    private static ConcurrentHashMap<String, String> addressTable; // dns table with pairs <Name, IP>

    public static void main(String[] args) throws IOException {
        // check arguments
        if(args.length < 3) {
            System.out.println("Invalid number of arguments!");
            printUsage();
            System.exit(-1);
        }

        // parse arguments
        multicastAddress = args[1]; // 224.0.0.0 to 239.255.255.255

        try {
            servicePort = Integer.parseInt(args[0]);    // 0 to 65535
            multicastPort = Integer.parseInt(args[2]);  // 0 to 65535

            if((servicePort < 0 || servicePort> 65535) || (multicastPort < 0 || multicastPort > 65535))
                throw new NumberFormatException("Ports must be integers between 0 and 65535");
        }
        catch(NumberFormatException e) {
            e.printStackTrace();
            System.out.println("Ports must be integers between 0 and 65535");
            System.exit(-2);
        }

        // open sockets
        socket = new DatagramSocket(servicePort);
        multicastSocket = new MulticastSocket(multicastPort);

        // create dns table with dummy values
        addressTable = new ConcurrentHashMap<>();
        addressTable.put("www.alpha.com", "1.0.0.0");
        addressTable.put("www.beta.com", "2.0.0.0");
        addressTable.put("www.charlie.com", "3.0.0.0");
        addressTable.put("www.delta.com", "4.0.0.0");

        // get lookup server information
        String lookupAddress = InetAddress.getLocalHost().getHostAddress();

        // print client request information
        System.out.println("Service port: " + servicePort);
        System.out.println("Multicast port: " + multicastPort);
        System.out.println("Multicast address: " + multicastAddress);
        System.out.println("Lookup address: " + lookupAddress);


        // create a thread pool service to send server por periodically
        ScheduledExecutorService broadcaster = Executors.newScheduledThreadPool(5);
        broadcaster.scheduleAtFixedRate(
            () -> {
                byte[] content = Integer.toString(servicePort).getBytes();
                try {
                    DatagramPacket multicastPacket = new DatagramPacket(content, content.length, InetAddress.getByName(multicastAddress), multicastPort);

                    // build message to advertise the service on command line, periodically
                    StringJoiner message = new StringJoiner(" ");
                    message.add("multicast:")
                        .add(multicastAddress)
                        .add(Integer.toString(multicastPort))
                        .add(lookupAddress)
                        .add(Integer.toString(servicePort));
                    System.out.println(message.toString());

                    multicastSocket.setTimeToLive(1);
                    multicastSocket.send(multicastPacket);
                }
                catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Unable to send multicast");
                    System.exit(-4);
                }
            }, 0, 1, TimeUnit.SECONDS); // delay, period, units

        // listen and process requests from clients
        try {
            processRequests();
        }
        catch(IOException e) {
            System.out.println("Communication with client error");
            System.exit(-4);
        }


        // exit procedures
        broadcaster.shutdown();
        socket.close();
        multicastSocket.close();
    }


    private static void processRequests() throws IOException {
        byte[] data = new byte[512];
        DatagramPacket packet = new DatagramPacket(data, data.length);

        while(true) {
            System.out.println("\nListening to requests...");
            socket.receive(packet);

            String request = new String(packet.getData());
            System.out.println("\nServer: " + request.trim());

            String answer = generateAnswer(request.trim());
            sendAnswer(answer, packet);

            byte[] buffer = new byte[512];
            packet.setData(buffer);
        }
    }


    private static String generateAnswer(String request) {
        String[] requestArgs = request.split(" "); // <operation> <DNS> (<IP>)
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
        addressTable.put(DNSName, IPAddress); // register

        // inform about the register update
        System.out.println("============ Updated table ============");
        addressTable.forEach((key, value) -> System.out.println(key + "\t" + value));

        return String.valueOf(addressTable.size()); //send answer
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
        System.out.println("Use 3 arguments (any extra args will be ignored)\n");
        System.out.println("Follow this format:");
        System.out.println("<Service Port> <Multicast Address> <Multicast Port>\n");
        System.out.println("Examples:");
        System.out.println("1234 224.0.0.0 80");
    }
}