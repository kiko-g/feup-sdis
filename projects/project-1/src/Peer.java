import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Peer {
    private static int port, senderId;
    private static DatagramSocket socket;   // socket for communication between server and client
    private static int portMC, portMDB, portMDR;
    private static String ipMC, ipMDB, ipMDR;
    private static double availableSpace=100000;
    private static MulticastThread MC;
    private static MulticastThread MDB;
    private static DatagramPacket packet;
    public static void main(String[] args) throws IOException {
        port = Integer.parseInt(args[0]);
        senderId = Integer.parseInt(args[1]);
        ipMC = args[2];
        ipMDR = args[4];
        ipMDB = args[6];
        portMC = Integer.parseInt(args[3]);
        portMDR = Integer.parseInt(args[5]);
        portMDB = Integer.parseInt(args[7]);
        String directoryName = "../../resources/peer" + senderId + "/";
        File directory = new File(directoryName);
        if (!directory.exists()) {
            directory.mkdir();
        }

        File file = new File(directoryName+"current_storage.txt");
        if(!file.exists()) {
            file.createNewFile();
            FileWriter fileWriter = new FileWriter("../../resources/peer"+senderId+"/current_storage.txt");
            fileWriter.write("0");
            fileWriter.close();
        }
        MC = new MC(senderId, portMC, ipMC, portMDR, ipMDR,portMDB, ipMDB);
        MDB = new MDB(senderId, portMDB, ipMDB, portMC, ipMC,availableSpace);


        // attempt to open socket
        try {
            socket = new DatagramSocket(port);
        } catch(SocketException e) {
            e.printStackTrace();
            System.err.println("\nFailed to open datagram socket");
        }


        // process requests from client
        try {
            processRequests();
        }
        catch (IOException e) {
            System.out.println("Communication with client error");
            System.exit(-4);
        }

        socket.close();
    }



    private static void processRequests() throws IOException {

        while(true) {
        System.out.println("> " + senderId + " listening to requests...");
            byte[] data = new byte[512];
            packet = new DatagramPacket(data, data.length);
            socket.receive(packet); // receive from test app

            String request = new String(packet.getData());
            // System.out.println("Peer " + senderId + ": [Unicast UDP message received from client] >> " + request);

            //String reply = "Peer " + senderId + ": Received the message from client through port " + port;
            sendReply("End",packet);//unicast reply

            // parse fields for message
            String operation = request.split(" ")[0];


            switch (operation) {
                case Utils.BACKUP:
                    processBackup(request);
                    break;

                case Utils.DELETE:
                    processDelete(request);
                    break;

                case Utils.RESTORE:
                    processRestore(request);
                    break;

                case Utils.STATE:
                    processState();
                    break;

                case Utils.RECLAIM:
                    processReclaim(request);
                    break;

                default:
                    System.out.println(operation);
            }

        }
    }


    private static void processBackup(String request) {
        String filePath = request.split(" ")[1];
        String fileId = getFileId(request);
        if(!fileId.equals("")) {
            int replicationDegree = Integer.parseInt(String.valueOf(request.split(" ")[2].charAt(0)));
            List<Thread> threadList = new ArrayList<>();
            List<byte[]> chunks = FileChunkManager.createChunks(filePath);

            for (int i = 0; i < chunks.size(); i++) {
                Message message = new Message("1.0", MessageType.PUTCHUNK, senderId, fileId, i, replicationDegree, chunks.get(i));
                Thread thread = new Thread(new StoredHandler(ipMC, portMC, replicationDegree, fileId, i, senderId, filePath, message, ipMDB, portMDB, true));
                thread.start();
                threadList.add(thread);
            }

            BackupEnd backupEnd = new BackupEnd(threadList,senderId);
            Thread endBackupThread = new Thread(backupEnd);
            endBackupThread.start();
        }


    }



    private static void processDelete(String request) throws IOException {
        String fileId = getFileId(request);
        if(!fileId.equals("")) {
            Message message = new Message("1.0", MessageType.DELETE, senderId, fileId);
            message.send(ipMC, portMC);
        }


    }

    private static void processRestore(String request) throws IOException {
        MulticastThread MDR = new MDR(senderId, portMDR, ipMDR);
        String filePath = request.split(" ")[1];
        String fileId = getFileId(request);

        boolean allFiles;
        int chunks = FileChunkManager.countChunks(filePath);

        while(true) {
            allFiles = false;
            for(int i = 0; i < chunks; i++) {
                if(!new File("../../resources/peer"+senderId+"/"+fileId+"("+i+")").exists()) {
                    allFiles = true;

                    Message message = new Message("1.0", MessageType.GETCHUNK, senderId, fileId, i);
                    message.send(ipMC, portMC);
                    message.send(ipMC, portMC);
                    message.send(ipMC, portMC);
                    message.send(ipMC, portMC);
                }
            }
            if(!allFiles) break;

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        String[] filePathParts = filePath.split("/");
        mergeFiles(chunks, fileId, filePathParts[filePathParts.length - 1]);
    }


    private static void processReclaim(String request) {
        double spaceInKBytes = Double.parseDouble(request.split(" ")[1]);
        MDB.setAvailableSpace(spaceInKBytes);
        String filePath="../../resources/peer" + senderId + "/current_storage.txt";
        Path path = Paths.get(filePath);

        AsynchronousFileChannel channel = null;
        try {
            channel = AsynchronousFileChannel.open(path, StandardOpenOption.READ);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        assert channel != null;
        Future<Integer> result=channel.read(buffer,0);
        while(!result.isDone()) {}
        buffer.flip();

        byte[] byteArray = new byte[buffer.remaining()];
        buffer.get(byteArray);
        String content = Utils.byteArrayToString(byteArray);
        buffer.clear();

        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        double folderSpace = Double.parseDouble(content);
        if(folderSpace>spaceInKBytes) {
            ReclaimSpace reclaimSpace = new ReclaimSpace(folderSpace-spaceInKBytes, folderSpace, senderId, ipMC, portMC);
            Thread reclaimThread = new Thread(reclaimSpace);
            reclaimThread.start();
        }
/*        total_space=spaceInKBytes;
        getUsedSpace();*/
    }
    private static void processState() throws IOException {
        System.out.println("STATE");
        System.out.println("---------");
        System.out.println("Back up initiated for:");

        HashMap<String, List<Chunk>> fileMap = getBackedUp();
        for(String key : fileMap.keySet()) {
            String filePath = fileMap.get(key).get(0).filePath;
            int desiredDeg = fileMap.get(key).get(0).desRepDeg;

            System.out.println("File "+filePath+" with id "+key+" desired replication degree: "+desiredDeg);
            for(Chunk chunk:fileMap.get(key)) {
                System.out.println("   Chunk nº"+chunk.id+" perceived replication degree: "+chunk.repDeg);
            }
        }

        System.out.println("---------");
        List<Chunk>stored_chunks = getStored();
        if(stored_chunks==null) {
            System.out.println("Chunks stored: none");
        }
        else {
            System.out.println("Chunks stored: " + stored_chunks.size());
            for(Chunk stored_chunk : stored_chunks) {
                System.out.println("Chunk nº " + stored_chunk.id + " of file " + stored_chunk.fileId +
                    " has size " + stored_chunk.size + "KB");
            }
        }
    }

    private static HashMap<String, List<Chunk>> getBackedUp() throws FileNotFoundException {
        HashMap<String, List<Chunk>> map = new HashMap<>();
        File file = new File("../../resources/peer" + senderId + "/replication_degrees.txt");
        if(!file.exists()) return map;

        Scanner reader = new Scanner(file);
        while(reader.hasNextLine()) {
            String data = reader.nextLine();
            String [] line=data.split(" ");

            if(map.containsKey(line[1])) {
                List<Chunk> list=map.get(line[1]);
                list.add(new Chunk(Integer.parseInt(line[2]), Integer.parseInt(line[3]), Integer.parseInt(line[4]), line[0]));
                map.replace(line[1],list);
            } else {
                List<Chunk> aux= new ArrayList<>();
                aux.add(new Chunk(Integer.parseInt(line[2]), Integer.parseInt(line[3]), Integer.parseInt(line[4]), line[0]));
                map.put(line[1],aux);
            }
        }

        return map;
    }

    private static List<Chunk> getStored() {
        List<Chunk> list = new ArrayList<>();

        final File folder = new File("../../resources/peer" + senderId);
        final File[] files = folder.listFiles((dir, name) -> {
            String regex = "[a-zA-Z_0-9]{64}\\([0-9]*\\)";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(name);
            return matcher.find();
        });

        if(files == null) return null;
        for (final File file : files) {
            Path path = Paths.get("../../resources/peer"+senderId+"/"+file.getName());
            double size = 0;
            try {
                size = Files.size(path);
            } catch(IOException e) { e.printStackTrace(); }
            size = size/1000;
            list.add(new Chunk(file.getName().split("\\(")[0], Integer.parseInt(file.getName().split("\\(")[1].split("\\)")[0]), size));
        }

        return list;
    }



    private static String getFileId(String request) {
        long millisec;

        String filePath = request.split(" ")[1];
        File f = new File(filePath);
        if(!f.exists()) {
            System.out.println("File does not exist");
            return "";
        }
        millisec = f.lastModified();
        Date date = new Date(millisec);

        return Utils.hash(filePath + date);
    }


    private static void sendReply(String reply, DatagramPacket packet) throws IOException {
        packet.setData(reply.getBytes());
        socket.send(packet);
    }





    // CLEAN THESE LATER
    private static  void sendMulticast(Message message) throws IOException {
        InetAddress group = null;
        DatagramSocket socket = null;

        try {
            socket = new DatagramSocket();
            group = InetAddress.getByName(ipMDB);
        } catch (UnknownHostException | SocketException e) {
            e.printStackTrace();
        }

        byte[] byteMsg = message.convertToBytes();
        DatagramPacket packet = new DatagramPacket(byteMsg, byteMsg.length, group, portMDB);

        try {
            assert socket != null;
            //System.out.println("Multicast message sent");
            socket.send(packet);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        // exit procedures
        socket.close();
    }

    private static void mergeFiles(int chunks, String fileId, String fileName) throws IOException {
        String directoryName = "../../resources/peer" + senderId + "/";
        FileOutputStream fos = new FileOutputStream(directoryName + fileName);
        try {
            for(int i=0; i<chunks; i++) {
                byte[] byteChunkPart;
                String filePath = directoryName + fileId + "(" + i + ")";

                File inputFile = new File(filePath);

                FileInputStream inputStream = new FileInputStream(inputFile);
                int fileSize = (int) inputFile.length();
                byteChunkPart = new byte[fileSize];
                inputStream.read(byteChunkPart, 0, fileSize);
                fos.write(byteChunkPart);
                inputFile.delete(); //delete chunk
            }
            fos.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static String generateReply(String request) {
       /* String[] requestArgs = request.split(" ");  //<Operation> (<File Path>) (<Replication Degree>)
        if(requestArgs.length < 2) return "-1";     // exit right away

        switch(requestArgs[0]) {
            case Utils.BACKUP:
                processBackup(requestArgs[1], Integer.parseInt(requestArgs[2]));
                break;

            case Utils.DELETE:
                processDelete(requestArgs[1]);
                break;

            case Utils.RESTORE:
                processRestore(requestArgs[1]);
                break;

            case Utils.RECLAIM:
                processReclaim(Integer.parseInt(requestArgs[1]));
                break;

            case Utils.STATE:
                processState();
                break;

            default:
                System.out.println("Invalid requested operation");
                return "-1";
        }*/

        return "ok";
    }
}
