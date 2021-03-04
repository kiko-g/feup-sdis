import java.net.*;
import java.util.*;
import java.io.IOException;

public class Client {
    private static int servicePort; // lookup server port number
    private static int multicastPort; // multicast port number
    private static String lookupAddress; // lookup server address
    private static String multicastAddress; // multicast address

    public static void main(String[] args) throws IOException, UnknownHostException {
        // check arguments
        if (args.length < 4) {
            System.out.println("Wrong number of arguments!\n");
            printUsage();
            System.exit(-1);
        }

        // parse arguments
        multicastAddress = args[0];

        try {
            multicastPort = Integer.parseInt(args[1]);
            if(multicastPort < 0 || multicastPort > 65535)
                throw new NumberFormatException("Ports must be integers between 0 and 65535");
        }
        catch(NumberFormatException e) {
            e.printStackTrace();
            System.out.println("Ports must be integers between 0 and 65535");
            System.exit(-2);
        }

        String operation = args[2];
        String DNSName = args[3];

        // open socket
        DatagramSocket socket = new DatagramSocket();

        // get lookup server information
        DatagramPacket multicastPacket = getServicePortFromLookupServer();
        lookupAddress = multicastPacket.getAddress().getHostAddress();
        servicePort = Integer.parseInt(new String(multicastPacket.getData()).trim());

        // print client request information
        System.out.println("Multicast port: " + multicastPort);
        System.out.println("Multicast address: " + multicastAddress);
        System.out.println("Requested operation: " + operation);
        System.out.println("DNS Name given: " + DNSName);
        System.out.println("Lookup address: " + lookupAddress + "\n");

        // select operation
        switch(operation) {
            case "register":
                if(args.length < 5) {
                    System.out.println("Wrong number of arguments");
                    printUsage();
                    System.exit(-1);
                }
                String IPAddress = args[4];
                requestRegister(DNSName, IPAddress, socket);
                break;

            case "lookup":
                requestLookup(DNSName, socket);
                break;

            default:
                System.out.println("Invalid requested operation");
                printUsage();
                System.exit(-2);
        }

        // receive and log answer
        System.out.println(getAnswer(socket));

        // exit procedures
        socket.close();
    }


    private static DatagramPacket getServicePortFromLookupServer() throws IOException {
        InetAddress address = InetAddress.getByName(multicastAddress); // IP address of the multicast group used by the server to advertise its service

        MulticastSocket multicastSocket = new MulticastSocket(multicastPort);
        multicastSocket.joinGroup(address); // Join the multicast group to learn the address of the socket that provides the service

        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        try {
            multicastSocket.setSoTimeout(5000); // 5 seconds
            multicastSocket.receive(packet);
        }
        catch (IOException e) {
            System.out.println("Error receiving response from the multicast server");
        }

        multicastSocket.leaveGroup(address);
        multicastSocket.close();

        return packet;
    }


    private static void requestLookup(String DNSName, DatagramSocket socket) throws UnknownHostException {
        //create buffer for lookup request
        StringJoiner request = new StringJoiner(" ");
        request.add("lookup").add(DNSName);

        byte[] buffer = request.toString().getBytes();

        InetAddress address = InetAddress.getByName(lookupAddress);
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, servicePort);

        try {
            socket.send(packet);
        }
        catch (IOException e) {
            System.out.println("Error sending lookup request");
        }
    }


    private static void requestRegister(String DNSName, String IPAddress, DatagramSocket socket) throws UnknownHostException {
        //create buffer for register request
        StringJoiner request = new StringJoiner(" ");
        request.add("register").add(DNSName).add(IPAddress);
        byte[] buffer = request.toString().getBytes();

        InetAddress address = InetAddress.getByName(lookupAddress);
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, servicePort);

        try {
            socket.send(packet);
        }
        catch (IOException e) {
            System.out.println("Error sending register request");
        }
    }



    private static String getAnswer(DatagramSocket socket) throws UnknownHostException {
        InetAddress address = InetAddress.getByName(lookupAddress);

        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, servicePort);

        try {
            socket.setSoTimeout(5000); //5 seconds
            socket.receive(packet);
        }
        catch (IOException e) {
            System.out.println("Error when trying to receive answer");
        }

        return new String(packet.getData());
    }


    private static void printUsage() {
        System.out.println("Improper usage of client!");
        System.out.println("Use 4 arguments for 'lookup' operation or 5 for 'register' operation (any extra args will be ignored)\n");
        System.out.println("Follow this format:");
        System.out.println("<Multicast Address> <Multicast Port> <Operation> <DNS Name> (<IP Address>)\n");
        System.out.println("Examples:");
        System.out.println("224.0.0.0 80 lookup www.alpha.com");
        System.out.println("224.0.0.0 80 register www.zeta.com 26.0.0.0");
    }
}