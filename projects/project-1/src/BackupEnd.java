import java.io.IOException;
import java.net.DatagramPacket;
import java.util.List;

public class BackupEnd implements Runnable {
    int senderId;
    DatagramPacket packet;
    List<Thread> threadList;

    BackupEnd(List<Thread> threadList, int senderId) {
        this.threadList = threadList;
        this.senderId = senderId;
    }

    @Override
    public void run() {
        for(Thread thread : threadList) {
            try {
                thread.join();
            }
            catch(InterruptedException e) { e.printStackTrace(); }
        }

        try {
            Utils.mergeFiles(threadList.size(),senderId);
        } catch(IOException e) {
            e.printStackTrace();
        }

        System.out.println("Finished backup");
    }
}

