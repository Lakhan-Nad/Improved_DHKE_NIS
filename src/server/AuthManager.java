package server;

import java.io.*;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

final class AuthManager {
    private static final String filename;
    private static final Map<String, ClientData> clients;

    private AuthManager() {
        // empty constructor
    }

    static {
        filename = "auth_keys.txt";
        clients = new HashMap<>();
    }

    synchronized static void load() {
        clients.clear();
        File file = new File(filename);
        BufferedReader buffer;
        try {
            buffer = new BufferedReader(new FileReader(file));
        } catch (Exception e) {
            System.out.println("NO previously loaded file found");
            return;
        }
        String st;
        do {
            try {
                st = buffer.readLine();
            } catch (Exception e) {
                System.out.println("Unfinished loading of file");
                return;
            }
            if (st != null) {
                String[] data = st.split("\\s+");
                BigInteger g, p, tInv;
                if (data.length >= 4) {
                    try {
                        g = new BigInteger(data[1]);
                        p = new BigInteger(data[2]);
                        tInv = new BigInteger(data[3]);
                    } catch (Exception e) {
                        System.out.println("Invalid data found in File");
                        continue;
                    }
                    ClientData client = new ClientData(data[0], g, p, tInv);
                    clients.put(data[0], client);
                }
            }
        } while (st != null);
        try {
            buffer.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    synchronized static ClientData find(String clientId) {
        if (clients.containsKey(clientId)) {
            return clients.get(clientId);
        }
        return null;
    }

    synchronized static void addOrUpdate(ClientData client) {
        if (clients.containsKey(client.id)) {
            clients.replace(client.id, client);
        } else {
            clients.put(client.id, client);
        }
    }

    synchronized static void delete(String clientId) {
        clients.remove(clientId);
    }

    synchronized static void syncWithFile() {
        File file = new File(filename);
        BufferedWriter buffer;
        try {
            buffer = new BufferedWriter(new FileWriter(file));
        } catch (Exception e) {
            System.out.println("Unable to update File Contents");
            return;
        }
        // empty file contents
        try{
            buffer.write("");
            buffer.flush();
        }catch (Exception e){
            System.out.println("Error while Writing in File");
            return;
        }
        StringBuffer buf = new StringBuffer();
        clients.forEach((k,client)->{
            buf.append(client.id);
            buf.append(' ');
            buf.append(client.getG().toString());
            buf.append(' ');
            buf.append(client.getP().toString());
            buf.append(' ');
            buf.append(client.getTInv().toString());
            buf.append('\n');
        });
        try{
            buffer.write(buf.toString());
            buffer.close();
        }catch (Exception e){
            System.out.println("Unable to Write Data");
        }
    }
}
