import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class MessageBuilder {
    public MessageBuilder() {

    }


    // Version FINDSUCC + nodeId + address + port + isFinger (+ <fingerPos>)*
    public byte[] constructFindSuccessorMessage(NodeInfo nodeInfo, Boolean isFinger, int fingerPos) {
        String message = "1.0 FINDSUCC " + nodeInfo.getNodeId() + " " + nodeInfo.getIp() + " " + nodeInfo.getPort() + " " + isFinger;
        
        if(isFinger) {
            message += " " + fingerPos + " \r\n\r\n";
        }
        else {
            message += " \r\n\r\n";
        }
        
        
        System.out.println("SENT: " + message);
        return message.getBytes();
    }


    // Version SUCCFOUND + nodeId + address + port + isFinger (+ <fingerPos>)* 
    public byte[] constructSuccessorFoundMessage(NodeInfo nodeInfo, Boolean isFinger, int fingerPos) {
        //System.out.println("INSIDE SUCCFOUND");
        String message = "1.0 SUCCFOUND " + nodeInfo.getNodeId() + " " + nodeInfo.getIp() + " " + nodeInfo.getPort() + " " + isFinger;
        
        if(isFinger) {
            message += " " + fingerPos + " \r\n\r\n";
        }
        else {
            message += " \r\n\r\n";
        }

        System.out.println("SENT: " + message);
        return message.getBytes();
    }


    // Version + FINDPRED + nodeId + address + port 
    public byte[] constructFindPredecessorMessage(NodeInfo nodeInfo) {
        //System.out.println("INSIDE FINDPRED");
        String message = "1.0 FINDPRED " + nodeInfo.getNodeId() + " " + nodeInfo.getIp() + " " + nodeInfo.getPort() + " \r\n\r\n";
        System.out.println("SENT: " + message);
        return message.getBytes();
    }


    // Version + PREDFOUND + nodeId + address + port 
    public byte[] constructPredecessorFoundMessage(NodeInfo nodeInfo) {
        //System.out.println("INSIDE PREDFOUND");
        String message = "1.0 PREDFOUND " + nodeInfo.getNodeId() + " " + nodeInfo.getIp() + " " + nodeInfo.getPort() + " \r\n\r\n";
        System.out.println("SENT: " + message);
        return message.getBytes();
    }
    
    
    // Version + NOTIFY + nodeId + address + port 
    public byte[] constructNotifyMessage(NodeInfo nodeInfo) {
        //System.out.println("INSIDE NOTIFY");
        String message = "1.0 NOTIFY " + nodeInfo.getNodeId() + " " + nodeInfo.getIp() + " " + nodeInfo.getPort() + " \r\n\r\n";
        System.out.println("SENT: " + message);
        return message.getBytes();
    }


    // Version + PREDALIVE + nodeId + address + port 
    public byte[] constructPredAliveMessage(NodeInfo nodeInfo) {
        //System.out.println("INSIDE PREDALIVE");
        String message = "1.0 PREDALIVE " + nodeInfo.getNodeId() + " " + nodeInfo.getIp() + " " + nodeInfo.getPort() + " \r\n\r\n";
        System.out.println("SENT: " + message);
        return message.getBytes();
    }

    // Version + ALIVE + nodeId + address + port
    public byte[] constructAliveMessage(NodeInfo nodeInfo) {
        //System.err.println("INSIDE ALIVE");
        String message = "1.0 ALIVE " + nodeInfo.getNodeId() + " " + nodeInfo.getIp() + " " + nodeInfo.getPort() + " \r\n\r\n";
        System.out.println("SENT: " + message);
        return message.getBytes();
    }

    // <Version> PUTCHUNK <SenderId> <Address> <Port> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF> <Body>
    public byte[] constructPutChunkMessage(Peer peer, String fileId, Chunk chunk) {
        //System.out.println("INSIDE PUTCHUNK MESSAGE");
        String version = peer.getProtocolVersion();
        int peerId = peer.getPeerId();
        String address = peer.getAddress().getHostAddress();
        int port = peer.getTcpPort();
        int chunkNo = chunk.getChunkNo();
        int replication = chunk.getReplication();
        String header = version + " PUTCHUNK " + peerId + " " + address + " " + port + " " + fileId + " " + chunkNo + " " + replication + " \r\n\r\n";
        
        try {
            byte[] headerBytes = header.getBytes(StandardCharsets.US_ASCII);
            byte[] body = chunk.getChunkMessage();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(headerBytes);
            outputStream.write(body);
            byte[] message = outputStream.toByteArray();
            System.out.println("SENT: " + header);

            return message;
        } catch(Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        } 

        return null;
    }


    // <Version> PUTCHUNK <SenderId> <Address> <Port> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF> <Body>
    public byte[] constructPutChunkMessage(String address, int port, String fileId, int chunkNo, int replication_degree, byte[] body) {
        String header = "1.0 PUTCHUNK " + Peer.getPeerId() + " " + address + " " + port + " " + fileId + " " + chunkNo + " " + replication_degree + " \r\n\r\n";

        try {
            byte[] headerBytes = header.getBytes(StandardCharsets.US_ASCII);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(headerBytes);
            outputStream.write(body);
            System.out.println("SENT: " + header);

            return outputStream.toByteArray();
        } catch(Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        } 

        return null;
    }


    // Version STORED <SenderId> <Address> <Port> <FileId> <ChunkNo>
    public byte[] constructStoredMessage(String address, int port, String fileId, int chunkNo) {
        String message = "1.0 STORED " + Peer.getPeerId() + " " + address + " " + port + " " + fileId + " " + chunkNo + " \r\n\r\n";
        System.out.println("SENT: " + message);
        return message.getBytes();
    }


    // Version REMOVED <SenderId> <Address> <Port> <FileId> <ChunkNo>
    public byte[] constructRemovedMessage(Peer peer, String fileId, int chunkNo) {
        String message = "1.0 REMOVED " + peer.getPeerId() + " " + peer.getChordNode().getNodeInfo().getIp() + " " + peer.getChordNode().getNodeInfo().getPort() + " " + fileId + " " + chunkNo + " \r\n\r\n";
        System.out.println("SENT: " + message);
        return message.getBytes();
    }


    // Version DELETE <SenderId> <Address> <Port> <FileId> <CRLF><CRLF>
    public byte[] constructDeleteMessage(String address, int port, String fileId) {
        String message = "1.0 DELETE " + Peer.getPeerId() + " " + address + " " + port + " " + fileId + " \r\n\r\n";
        System.out.println("SENT: " + message);
        return message.getBytes();
    }


    // Version GETCHUNK <SenderId> <Address> <Port> <FileId> <ChunkNo>
    public byte[] constructGetChunkMessage(String address, int port, String fileId, int chunkNo) {
        String header = "1.0 GETCHUNK " + Peer.getPeerId() + " " + address + " " + port + " " + fileId + " " + chunkNo + " \r\n\r\n";

        try {
            byte[] headerBytes = header.getBytes(StandardCharsets.US_ASCII);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(headerBytes);
            System.out.println("SENT: " + header);

            return outputStream.toByteArray();
        } catch(Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        return null;
    }


    // Version CHUNK <SenderId> <Address> <Port> <FileId> <ChunkNo>
    public byte[] constructChunkMessage(String address, int port, String fileId, int chunkNo, byte[] body) {
        String header = "1.0 CHUNK " + Peer.getPeerId() + " " + address + " " + port + " " + fileId + " " + chunkNo + " \r\n\r\n";

        try {
            byte[] headerBytes = header.getBytes(StandardCharsets.US_ASCII);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(headerBytes);
            outputStream.write(body);
            return outputStream.toByteArray();
        } catch(Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
}