import java.io.File;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
    public static void main(String[] args) {
        try {
            if(args.length > 4) {
                //System.out.println("SIZE: " + args.length);
                System.out.println("Usage: Client <peer_ap> <sub_protocol> [<opnd_1> [<opnd_2>]]");
                return;
            }

            String peer_ap = args[0];
            String subprotocol = args[1].toUpperCase();
            String file_path_name;
            int replication_degree;
            int maximum_disk_space;

            Registry registry = LocateRegistry.getRegistry("localhost");
            RemoteInterface stub = (RemoteInterface) registry.lookup(peer_ap);

            switch (subprotocol) {
                case "BACKUP":
                    if(args.length != 4) {
                        System.out.println("Invalid invocation for BACKUP subprotocol.");
                        System.out.println("Usage: Client <peer_ap> BACKUP <file_path_name> <replication_degree>");
                        return;
                    }

                    file_path_name = args[2];

                    File fileB = new File(file_path_name);
                    if(!fileB.exists()) {
                        System.out.println("File " + file_path_name + " not exists.");
                        return;
                    }

                    replication_degree = Integer.parseInt(args[3]);
                    if(replication_degree > 9 || replication_degree < 1) {
                        System.out.println("Replication degree must be between 1 and 9.");
                        return;
                    }

                    /*System.out.println("Peer_ap: " + peer_ap);
                    System.out.println("Subprotocol: " + subprotocol);
                    System.out.println("File path: " + file_path_name);
                    System.out.println("Replication: " + replication_degree);*/

                    stub.backup(file_path_name, replication_degree);
                    break;
                
                case "RESTORE":
                    if(args.length != 3) {
                        System.out.println("Invalid invocation for RESTORE subprotocol.");
                        System.out.println("Usage: Client <peer_ap> RESTORE <file_path_name>");
                        return;
                    }

                    file_path_name = args[2];

                    File fileR = new File(file_path_name);
                    if(!fileR.exists()) {
                        System.out.println("File " + file_path_name + " not exists.");
                        return;
                    }

                    /*System.out.println("Peer_ap: " + peer_ap);
                    System.out.println("Subprotocol: " + subprotocol);
                    System.out.println("File path: " + file_path_name);*/

                    stub.restore(file_path_name);
                    break;

                case "DELETE":
                    if(args.length != 3) {
                        System.out.println("Invalid invocation for DELETE subprotocol.");
                        System.out.println("Usage: Client <peer_ap> DELETE <file_path_name>");
                        return;
                    }

                    file_path_name = args[2];

                    File fileRm = new File(file_path_name);
                    if(!fileRm.exists()) {
                        System.out.println("File " + file_path_name + " not exists.");
                        return;
                    }

                    /*System.out.println("Peer_ap: " + peer_ap);
                    System.out.println("Subprotocol: " + subprotocol);
                    System.out.println("File path: " + file_path_name);*/

                    stub.delete(file_path_name);
                    break;

                case "RECLAIM":
                    if(args.length != 3) {
                        System.out.println("Invalid invocation for RECLAIM subprotocol.");
                        System.out.println("Usage: Client <peer_ap> RECLAIM <maximum_disk_space_KB>");
                        return;
                    }

                    maximum_disk_space = Integer.parseInt(args[2]);

                    if(maximum_disk_space < 0) {
                        System.out.println("Maximum disk space must be higher or equal to 0");
                    }

                    /*System.out.println("Peer_ap: " + peer_ap);
                    System.out.println("Subprotocol: " + subprotocol);
                    System.out.println("Maximum disk: " + maximum_disk_space);*/

                    stub.reclaim(maximum_disk_space);
                    break;

                case "STATE":
                    if(args.length != 2) {
                        System.out.println("Invalid invocation for STATE subprotocol.");
                        System.out.println("Usage: Client <peer_ap> STATE");
                        return;
                    }

                    /*System.out.println("Peer_ap: " + peer_ap);
                    System.out.println("Subprotocol: " + subprotocol);*/
                    
                    stub.state();
                    break;

                case "CHORD":
                    if(args.length != 2) {
                        System.out.println("Invalid invocation for CHORD subprotocol.");
                        System.out.println("Usage: Client <peer_ap> CHORD");
                        return;
                    }

                    stub.chord();
                    break;

                default:
                    break;
            }

        } catch(Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}