import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;

public class FileStorage implements java.io.Serializable {
    // Array to store all the files that the peer received as initiator
    private ArrayList<FileManager> filesStored;

    // stores the chunks that the peer has stored and the correspondent fileId
    // key = fileId_chunkNo; value = chunk
    private ConcurrentHashMap<String, Chunk> chunksStored;

    // stores the counter of all STORED messages that the peer has received for each file chunk
    // key = fileId_chunkNo; value = number of stored messages received
    private ConcurrentHashMap<String, Integer> storedMessagesReceived;

    // for each peer, stores all the chunks id that this peer has the perception that they have 
    // key = sendId; value = fileId_chunkNo 
    private ConcurrentHashMap<Integer, ArrayList<String>> chunksDistribution;

    // Array to store all the files that the peer restored as initiator
    private ArrayList<String> filesRestored;

    // table to store the chunk info for each chunk that the peer has asked restore
    // key = fileId_chunkNo; value = chunk_content
    private ConcurrentHashMap<String, byte[]> chunksRestored;

    // stores all the fileId of the files that the peer has deleted as initiator
    // key = fileId; value = peerId
    private ConcurrentHashMap<String, ArrayList<Integer>> filesDeleted;

    // for each key, stores the addresses of the nodes that have backed up the chunks
    // key = fileId_chunkNo; value = array with the addresses of the nodes that have backed up the file
    private ConcurrentHashMap<String, ArrayList<InetSocketAddress>> backupChunksDistribution;

    private int capacity;

    private static final long serialVersionUID = 4066270093854086490L;


    public FileStorage() {
        this.filesStored = new ArrayList<FileManager>();
        this.chunksStored = new ConcurrentHashMap<String, Chunk>();
        this.storedMessagesReceived = new ConcurrentHashMap<String, Integer>();
        this.chunksDistribution = new ConcurrentHashMap<Integer, ArrayList<String>>();
        this.filesRestored = new ArrayList<String>();
        this.chunksRestored = new ConcurrentHashMap<String, byte[]>();
        this.filesDeleted = new ConcurrentHashMap<String, ArrayList<Integer>>();
        this.backupChunksDistribution = new ConcurrentHashMap<String, ArrayList<InetSocketAddress>>();
        this.capacity = 1 * 1000 * 1000 * 1000; // 1B * 1000 (1KB) * 1000 (1MB) * 1000 (1GB) -> initially, all the peers have 1GB of capacity
    }


    public ArrayList<FileManager> getFilesStored() {
        return this.filesStored;
    }


    public ConcurrentHashMap<String,Chunk> getChunksStored() {
        return this.chunksStored;
    }


    public ConcurrentHashMap<String,Integer> getStoredMessagesReceived() {
        return this.storedMessagesReceived;
    }


    public ArrayList<String> getFilesRestored() {
        return this.filesRestored;
    }


    public ConcurrentHashMap<String,byte[]> getChunksRestored() {
        return this.chunksRestored;
    }


    public ConcurrentHashMap<String, ArrayList<Integer>> getFilesDeleted() {
        return this.filesDeleted;
    }

    public ConcurrentHashMap<String, ArrayList<InetSocketAddress>> getBackupChunksDistribution() {
        return this.backupChunksDistribution;
    }


    public Chunk getChunk(String fileId, int chunkNo) {
        String chunkId = fileId + "_" + chunkNo;
        Chunk chunk = this.chunksStored.get(chunkId);

        return chunk;
    }


    public ConcurrentHashMap<Integer,ArrayList<String>> getChunksDistribution() {
        return this.chunksDistribution;
    }


    public int getCapacity() {
        return this.capacity;
    }


    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    // adds a file to filesStored
    public void addFile(FileManager file) {
        this.filesStored.add(file);
    }


    // adds a chunks to chunksStored
    public void addChunk(Chunk chunk) {
        String fileId = chunk.getFileId();
        int chunkNo = chunk.getChunkNo();

        this.chunksStored.put((fileId + "_" + chunkNo), chunk);
    }


    // checks if the peer already has the chunk stored
    public boolean hasChunk(String fileId, int chunkNo) {
        String chunkId = fileId + "_" + chunkNo;

        if(this.chunksStored.containsKey(chunkId)) {
            return true;
        }
        else {
            return false;
        }
    }


    // increments the number of stored messages received for the chunkNo of the file fileId coming from peer with id senderId
    public synchronized void incrementStoredMessagesReceived(int senderId, String fileId, int chunkNo) {
        String chunkId = fileId + "_" + chunkNo;

        if(this.storedMessagesReceived.containsKey(chunkId)) {
            int total = this.storedMessagesReceived.get(chunkId) + 1;
            this.storedMessagesReceived.put(chunkId, total);
            // System.out.println("Regist exists. Times: " + this.storedMessagesReceived.get(chunkId));
        }
        else {
            this.storedMessagesReceived.put(chunkId, 1);
            // System.out.println("Not exists regist");
        }
    }


    // adds the chunkid to the senderId list of chunks backed up in chunksDistribution table
    public synchronized void addChunksDistribution(int senderId, String fileId, int chunkNo) {
        String chunkId = fileId + "_" + chunkNo;

        if(this.chunksDistribution.containsKey(senderId)) {
            ArrayList<String> f = this.chunksDistribution.get(senderId);
            if(!(f.contains(chunkId))) {
                //System.out.println("Add regist to sender");
                this.chunksDistribution.get(senderId).add(chunkId);
            }
            /*else {
                System.out.println("registed");
            }*/
        }
        else {
            ArrayList<String> app = new ArrayList<String>();
            app.add(chunkId);
            this.chunksDistribution.put(senderId, app);
            //System.out.println("Add sender and regist");
        }
    
        // System.out.println("-----REGISTS ADD CHUNKS DISTRIBUTION-------------");
        // for(Integer key : this.chunksDistribution.keySet()) {
        //     System.out.println(key + ": " + this.chunksDistribution.get(key));  
        // }
        // System.out.println("--------------------------");
        // System.out.println("Contains: " + this.storedMessagesReceived.containsKey(chunkId));
    }


    // deletes the chunkId from all lists inside chunksDistribution table
    public void deleteChunksDistribution(String fileId) {
        for(Integer key : this.chunksDistribution.keySet()) {
            ArrayList<String> f = this.chunksDistribution.get(key);
            if(f.size() > 0) {
                for(int i = 0; i < f.size(); i++) {
                    String chunkId = f.get(i);
                    String fileIdStored = chunkId.split("_")[0];
                    if(fileIdStored.equals(fileId)) {
                        this.chunksDistribution.get(key).remove(chunkId);
                    }
                }
            }
        }
        // System.out.println("-----REGISTS DELETE CHUNKS DISTRIBUTION-------------");
        // for(Integer key : this.chunksDistribution.keySet()) {
        //     System.out.println(key + ": " + this.chunksDistribution.get(key));  
        // }
        // System.out.println("--------------------------");
    }

    // deletes all the chunks of the file fileId from the peerId list of chunks backed up in chunksDistribution table
    public void deleteChunksDistribution(String fileId, int peerId) {
        ArrayList<String> chunks = this.chunksDistribution.get(peerId);
       
        if(chunks.size() > 0) {
            for(int i = 0; i < chunks.size(); i++) {
                String chunkId = chunks.get(i);
                String fileIdStored = chunkId.split("_")[0];
                if(fileIdStored.equals(fileId)) {
                    this.chunksDistribution.get(peerId).remove(chunkId);
                }
            }
        }
        // System.out.println("-----REGISTS DELETE CHUNKS DISTRIBUTION-------------");
        // for(Integer key : this.chunksDistribution.keySet()) {
        //     System.out.println(key + ": " + this.chunksDistribution.get(key));  
        // }
        // System.out.println("--------------------------");
    }


    // deletes a specific chunk of the file with fileId from the peerId list of chunks backed up in chunksDistribution table
    public void deleteSpecificChunksDistribution(String fileId, int chunkNo, int peerId) {
        String chunkId = fileId + "_" + chunkNo;
        if(this.chunksDistribution.size() > 0) {
            if(this.chunksDistribution.get(peerId).size() > 0) {
                this.chunksDistribution.get(peerId).remove(chunkId);
            }
        }
        
        // System.out.println("-----REGISTS DELETE SPECIFIC CHUNK-------------");
        // for(Integer key : this.chunksDistribution.keySet()) {
        //     System.out.println(key + ": " + this.chunksDistribution.get(key));  
        // }
        // System.out.println("--------------------------");
    }


    // delete chunk from chunksStored list
    public void deleteChunk(String chunkId) {
        this.chunksStored.remove(chunkId);
    }

    // delete file from filesStored list
    public void deleteFile(FileManager file) {
        for(int i = 0; i < filesStored.size(); i++) {
            if(filesStored.get(i).getFileID().equals(file.getFileID())) {
                this.filesStored.remove(i);
                return;
            }
        }

        // System.out.println("-----FILES DISTRIBUTION-------------");
        // for(int i = 0; i < this.filesStored.size(); i++) {
        //     System.out.println(i + ": " + this.filesStored.get(i));  
        // }
        // System.out.println("--------------------------");
    }

    public void deleteFile(String fileId) {
        for(int i = 0; i < filesStored.size(); i++) {
            if(filesStored.get(i).getFileID().equals(fileId)) {
                this.filesStored.remove(i);
                return;
            }
        }
    }


    // delete stored messages regist of chunkId in storedMessagesReceived
    public void deleteStoreMessage(String chunkId) {
        this.storedMessagesReceived.remove(chunkId);
    }


    // create a register in storedMessagesReceived in order to then increment the counter of stored messages of this chunk
    public synchronized void createRegisterToStore(String fileId, int chunkNo) {
        String chunkId = fileId + "_" + chunkNo;
        this.storedMessagesReceived.put(chunkId, 0);
    }


    // checks if the peer has this chunkId in storedMessagesReceived
    public boolean hasRegisterStore(String fileId, int chunkNo) {
        String chunkId = fileId + "_" + chunkNo;
        if(this.storedMessagesReceived.containsKey(chunkId)) {
            return true;
        }
        else {
            return false;
        }
    }


    // add a chunk to chunksRestored 
    public void addChunkToRestore(String chunkId, byte[] data) {
        this.chunksRestored.put(chunkId, data);
    }


    // add fileId to filesRestored 
    public void addFileToRestore(String fileId) {
        this.filesRestored.add(fileId);
    }

    public boolean hasRegisterToRestore(String chunkId) {
        if(this.chunksRestored.containsKey(chunkId)) {
            return true;
        }
        else {
            return false;
        }
    }

    public boolean hasFileToRestore(String fileId) {
        if(this.filesRestored.contains(fileId)) {
            return true;
        }
        else {
            return false;
        }
    }

    public void deleteFileRestored(String file) {
        this.filesRestored.remove(file);
    }

    public void deleteChunksRestored(String fileId) {
        for(String key : this.chunksRestored.keySet()) {
            if((key.split("_")[0]).equals(fileId)) {
                this.chunksRestored.remove(key);
            }
        }
    }

    public int getPeerOccupiedSpace() {
        int total = 0;
        for(String key : this.chunksStored.keySet()) {
            total += this.chunksStored.get(key).getSize();
        }

        return total;
    }

    public int getOccupiedSpace() {
        int occupiedSpace = 0; 
        for(String key : this.chunksStored.keySet()) {
            occupiedSpace += this.chunksStored.get(key).getSize();
        }
        return occupiedSpace;
    }

    public boolean checkIfHasSpace(int chunkSize) {
        int occupiedSpace = getOccupiedSpace();
        int finalOccupied = occupiedSpace + chunkSize;

        return finalOccupied < this.capacity;
    }

    public int getPerceivedReplication(String chunkId) {
        for(String key : this.backupChunksDistribution.keySet()) {
            if(key.equals(chunkId)) {
                return this.backupChunksDistribution.get(key).size();
            }
        }
        return 0;
    }

    public void addDeletedFile(String fileId, int peerId) {
        if(!this.filesDeleted.containsKey(fileId)) {
            this.filesDeleted.put(fileId, new ArrayList<Integer>());
        }

        this.filesDeleted.get(fileId).add(peerId);
    }

    public ArrayList<String> getFilesToDelete(int peerId) {
        ArrayList<String> filesToDelete = new ArrayList<String>();

        if(this.chunksDistribution.containsKey(peerId)) {
            ArrayList<String> chunks = this.chunksDistribution.get(peerId);
            ArrayList<String> files = new ArrayList<String>();
            for(int i = 0; i < chunks.size(); i++) {
                String file = chunks.get(i).split("_")[0];
                if(!files.contains(file)) {
                    files.add(file);
                }
            }

            for(String key : this.filesDeleted.keySet()) {
                if(files.contains(key) && !this.filesDeleted.get(key).contains(peerId)) {
                    filesToDelete.add(key);
                }
            }
        }
        return filesToDelete;
    }

    public boolean hasDeletedFile(String fileId) {
        if(this.filesDeleted.containsKey(fileId)) {
            return true;
        }
        return false;
    }

    public void removeDeletedFile(String fileId) {
        this.filesDeleted.remove(fileId);
    }

    public void addBackupFileDistribution(String address, int port, String fileId, int chunkNo) {
        String chunkId = fileId + "_" + chunkNo;
        InetSocketAddress candidate = new InetSocketAddress(address, port);

        if(this.backupChunksDistribution.containsKey(chunkId)) {
            //System.out.println("HAS REGIST CHUNKID");
            ArrayList<InetSocketAddress> addresses = this.backupChunksDistribution.get(chunkId);

            if(addresses.size() > 0) {
                Boolean hasAddress = false;
                for(int i = 0; i < addresses.size(); i++) {
                    if(addresses.get(i).equals(candidate)) {
                        hasAddress = true;
                    }
                }

                if(!hasAddress) {
                    this.backupChunksDistribution.get(chunkId).add(candidate);
                }
            }
            else {
                this.backupChunksDistribution.get(chunkId).add(candidate);
            }
        }
        else {
            //System.out.println("DOESN'T HAVE REGIST");
            ArrayList<InetSocketAddress> app = new ArrayList<>();
            app.add(candidate);
            this.backupChunksDistribution.put(chunkId, app);
        }

        // System.out.println("-----BACKUP CHUNKS DISTRIBUTION-------------");
        // for(String key : this.backupChunksDistribution.keySet()) {
        //     System.out.println(key + ": " + this.backupChunksDistribution.get(key));  
        // }
        // System.out.println("--------------------------");
    }

    public void removeBackupFileDistribution(String address, int port, String fileId, int chunkNo) {
        String chunkId = fileId + "_" + chunkNo;
        InetSocketAddress toRemove = new InetSocketAddress(address, port);

        this.backupChunksDistribution.get(chunkId).remove(toRemove);
    }

    public void removeBackupChunksDistribution(String fileId) {
        for(ConcurrentHashMap.Entry<String, ArrayList<InetSocketAddress>> set : this.backupChunksDistribution.entrySet()) {
            String fId = set.getKey().split("_")[0];
            if(fId.equals(fileId)) {
                this.backupChunksDistribution.remove(set.getKey());
            }
        }

        // System.out.println("-----BACKUP CHUNKS DISTRIBUTION-------------");
        // for(String key : this.backupChunksDistribution.keySet()) {
        //     System.out.println(key + ": " + this.backupChunksDistribution.get(key));  
        // }
        // System.out.println("--------------------------");
    }
}

