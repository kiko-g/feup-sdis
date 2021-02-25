import java.net.*;
import java.io.IOException;
import java.util.Arrays;

public class Client {
    private static int port; // port number
    private static String host; // host address


    public static void main(String[] args) throws SocketException, UnknownHostException {
        if (args.length < 4) {
            System.out.println("Wrong number of arguments");
            printUsage();
            System.exit(-1);
        }

        host = args[0];
        port = Integer.parseInt(args[1]);
        String operation = args[2];
        String DNSName = args[3];
        DatagramSocket socket = new DatagramSocket();

        // select operation
        switch(operation) {
            case "register":
                if(args.length < 5) {
                    System.out.println("Wrong number of arguments");
                    printUsage();
                    System.exit(-1);
                }
                String IPAddress = args[4];
                sendRegisterRequest(DNSName, IPAddress, socket);
                break;

            case "lookup":
                sendLookupRequest(DNSName, socket);
                break;

            default:
                System.out.println("Invalid requested operation");
                printUsage();
                System.exit(-2);
        }

        // receive and log answer
        String answer = receiveAnswer(socket);
        System.out.println(answer);
        socket.close();
    }



    private static void sendRegisterRequest(String DNSName, String IPAddress, DatagramSocket socket) throws UnknownHostException {
        String request = "register " + DNSName + " " + IPAddress; //create string for register request
        byte[] buffer = request.getBytes();

        InetAddress address = InetAddress.getByName(host);
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);

        try {
            socket.send(packet);
        }
        catch (IOException e) {
            System.out.println("Error sending register request");
        }
    }


    private static void sendLookupRequest(String DNSName, DatagramSocket socket) throws UnknownHostException {
        String request = "lookup " + DNSName; //create string for lookup request
        byte[] buffer = request.getBytes();

        InetAddress address = InetAddress.getByName(host);
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);

        try {
            socket.send(packet);
        }
        catch (IOException e) {
            System.out.println("Error sending lookup request");
        }
    }


    private static String receiveAnswer(DatagramSocket socket) throws UnknownHostException {
        InetAddress address = InetAddress.getByName(host);

        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);

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
        System.out.println("<Host> <Port> <Operation> <DNS Name> (<IP Address>)\n");
        System.out.println("Examples:");
        System.out.println("127.0.0.1 8000 lookup www.alpha.com");
        System.out.println("127.0.0.1 8000 register www.zeta.com 26.0.0.0");
    }
}