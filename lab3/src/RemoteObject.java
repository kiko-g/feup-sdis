import java.util.*;
import java.rmi.RemoteException;


public class RemoteObject implements RemoteInterface {
    HashMap<String,String> DNSTable; // dns table with pairs <name, ip_address>

    public RemoteObject() {
        DNSTable = new HashMap<>();
        DNSTable.put("www.alpha.com", "1.0.0.0");
        DNSTable.put("www.beta.com", "2.0.0.0");
        DNSTable.put("www.charlie.com", "3.0.0.0");
        DNSTable.put("www.delta.com", "4.0.0.0");
    }

    public void showDNSTable() {
        System.out.println("\n============ Updated table ============");
        DNSTable.forEach((key, value) -> System.out.println(key + "\t" + value));
    }

    @Override
    public String lookup(String DNSName) throws RemoteException {
        if(!DNSTable.containsKey(DNSName)) {
            return "No entry";
        }

        String IPAddress = DNSTable.get(DNSName);
        return DNSName + " -> " + IPAddress;
    }

    @Override
    public String register(String DNSName, String IPAddress) throws RemoteException {
        if(DNSTable.containsKey(DNSName)) {
            return "DNS already registered (" + DNSName + ", " + IPAddress + ")";
        }

        DNSTable.put(DNSName, IPAddress);
        showDNSTable();

        return String.valueOf(DNSTable.size());
    }
}
