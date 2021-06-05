import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    public static final String BACKUP = "BACKUP";
    public static final String RESTORE = "RESTORE";
    public static final String DELETE = "DELETE";
    public static final String RECLAIM = "RECLAIM";
    public static final String STATE = "STATE";
    public static final String CHUNK = "CHUNK";
    public static final String GETCHUNK = "GETCHUNK";
    public static final String REMOVED = "REMOVED";
    public static final int BUFFER_SIZE = 64500;
    public static final int MAX_BUFFER_SIZE = 65355;
    public static final int CHUNK_SIZE_B = 64000;
    public static final int CHUNK_SIZE_KB = 64;
    public static final char[] CRLF = { 0xD, 0xA };
    public static final char[] DOUBLE_CRLF = { 0xD, 0xA, 0xD, 0xA };
    public static final int THREADS_PER_CHANNEL = 20;

    public static String hash(String originalString) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(originalString.getBytes(StandardCharsets.UTF_8));

            StringBuilder resultString = new StringBuilder(2 * encodedHash.length);
            for(byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if(hex.length() == 1) resultString.append('0');
                resultString.append(hex);
            }

            return resultString.toString();
        }
        catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String readFile(String filePath) {
        StringBuilder data = new StringBuilder();

        try {
            File fp = new File(filePath.split(" ")[0]);
            Scanner reader = new Scanner(fp);
            while (reader.hasNextLine()) {
                data.append(reader.nextLine());
            }
            reader.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("An error occurred reading the file");
            e.printStackTrace();
        }

        return data.toString();
    }

    public static String byteArrayToString(byte[] byteArray) {
        return new String(byteArray, StandardCharsets.ISO_8859_1);
    }

    public static long getFileSizeBytes(String filepath) {
        Path path = Paths.get(filepath);
        long bytes = 0;

        try {
            bytes = Files.size(path);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return bytes;
    }

    public static void mergeFiles(int chunks, int senderId) throws IOException {
        String directoryName = "../../resources/peer" + senderId + "/";
        FileOutputStream fos = new FileOutputStream(directoryName + "replication_degrees.txt");
        try {
            for(int i=0; i<chunks; i++) {
                byte[] byteChunkPart;
                String filePath = directoryName + "replication_degrees" + i+".txt";

                File inputFile = new File(filePath);

                FileInputStream inputStream = new FileInputStream(inputFile);
                int fileSize = (int) inputFile.length();
                byteChunkPart = new byte[fileSize];
                inputStream.read(byteChunkPart, 0, fileSize);
                fos.write(byteChunkPart);
                inputFile.delete(); //delete chunk
                inputStream.close();
            }
            fos.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static double getFileSizeKBytes(String filepath) {
        return (double)getFileSizeBytes(filepath) / 1000;
    }

    public static double getPeerFolderSpace(int senderId) {
        double result = 0;
        final File folder = new File("../../resources/peer" + senderId);

        if(!folder.exists())
            return 0;

        final File[] files = folder.listFiles((dir, name) -> {
            String regex = "[a-zA-Z_0-9]{64}\\([0-9]*\\)";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(name);
            return matcher.find();
        });

        //if(files == null) return null;
        for (final File file : files) {
            Path path = Paths.get("../../resources/peer"+senderId+"/"+file.getName());

            double size = 0;
            try {
                size = Files.size(path);
            } catch(IOException e) { e.printStackTrace(); }
            size = size/1000;
            result=result+size;

        }
        return result;
    }
}
