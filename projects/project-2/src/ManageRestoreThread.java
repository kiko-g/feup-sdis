import java.util.concurrent.ConcurrentHashMap;
import java.util.*;
import java.io.File;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

public class ManageRestoreThread implements Runnable {
    private FileManager fileManager;

    public ManageRestoreThread(FileManager fileManager) {
        this.fileManager = fileManager;
    }


    // thread that checks if all chunks of a file are restored and then join all of then in a file in order to restore that. Finally, creates the restored file in the restore sub directory
    @Override
    public void run() {
        int chunksNumber = this.fileManager.getFileChunks().size();
        ConcurrentHashMap<String, byte[]> fileChunks;
        int n=0;
        // checks if all file chunks are restored
        while(true) {
            ConcurrentHashMap<String,byte[]> allChunks = Peer.getStorage().getChunksRestored();
            fileChunks = new ConcurrentHashMap<String, byte[]>();

            for(String key : allChunks.keySet()) {
                if((key.split("_")[0]).equals(this.fileManager.getFileID())) {
                    fileChunks.put(key, allChunks.get(key));
                }
            }


            if(fileChunks.size() != chunksNumber) {
                if(n >= 10) {
                    Peer.getStorage().deleteFileRestored(this.fileManager.getFileID());
                    Peer.getStorage().deleteChunksRestored(this.fileManager.getFileID());
                    System.out.println("Could not restore all chunks. Exiting...");

                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                n++;
            }
            else {
                System.out.println("All chunks restored, going to create file");
                break;
            }
        }


        // create the chunk file in the peer directory
        String dir = "peer_" + Peer.getPeerId();
        String restoreDir = "peer_" + Peer.getPeerId() + "/" + "restore";
        String file = "peer_" + Peer.getPeerId() + "/" + "restore" + "/" + this.fileManager.getFile().getName();
        File directory = new File(dir);
        File restoreDirectory = new File(restoreDir);
        File f = new File(file);

        try{
            if (!directory.exists()){
                directory.mkdir();
                restoreDirectory.mkdir();
                f.createNewFile();
            }
            else {
                if (directory.exists()) {
                    if(! restoreDirectory.exists()) {
                        restoreDirectory.mkdir();
                    }
                    f.createNewFile();
                }
            }


            FileOutputStream fos = new FileOutputStream(f);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            ArrayList<byte[]> chunks = new ArrayList<byte[]>();

            for(int i = 0; i < fileChunks.size(); i++) {
                for(String key : fileChunks.keySet()) {
                    if(Integer.parseInt(key.split("_")[1]) == i) {
                        chunks.add(fileChunks.get(key));
                    }
                }
            }

            for(int i = 0; i < chunks.size(); i++) {
                bos.write(chunks.get(i));
            }

            bos.close();
            System.out.println("Restore finished");
            System.out.println();

        } catch(Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

    }
}