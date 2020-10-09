package server;

import java.io.*;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.nio.file.Paths;

final class AuthManager {
    private static final String filename;
    private static final Map<String, BigInteger> clients;

    private AuthManager() {
        // empty constructor
    }

    static {
        filename = Paths.get(System.getProperty("user.dir"), "server_files", "auth_keys.txt").toString();
        clients = new HashMap<>();
    }

    synchronized static void load() {
        clients.clear();
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("");
            return;
        }
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
                BigInteger tInv;
                if (data.length >= 2) {
                    try {
                        tInv = new BigInteger(data[1]);
                    } catch (Exception e) {
                        System.out.println("Invalid data found in File");
                        continue;
                    }
                    clients.put(data[0], tInv);
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

    synchronized static BigInteger find(String clientId) {
        if (clients.containsKey(clientId)) {
            return clients.get(clientId);
        }
        return null;
    }

    synchronized static void addOrUpdate(String id, BigInteger tInv) {
        if (clients.containsKey(id)) {
            clients.replace(id, tInv);
        } else {
            clients.put(id, tInv);
        }
    }

    synchronized static void delete(String clientId) {
        clients.remove(clientId);
    }

    synchronized static void syncWithFile() {
        File file = new File(filename);
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    System.out.println("New File Couldn't Be Created");
                    return;
                }
            } catch (Exception e) {
                System.out.println("New File Couldn't Be Created");
                return;
            }
        }
        BufferedWriter buffer;
        try {
            buffer = new BufferedWriter(new FileWriter(file));
        } catch (Exception e) {
            System.out.println("Unable to update File Contents");
            return;
        }
        // empty file contents
        try {
            buffer.write("");
            buffer.flush();
        } catch (Exception e) {
            System.out.println("Error while Writing in File");
            return;
        }
        StringBuffer buf = new StringBuffer();
        clients.forEach((id, tInv) -> {
            buf.append(id);
            buf.append(' ');
            buf.append(tInv.toString());
            buf.append('\n');
        });
        try {
            buffer.write(buf.toString());
            buffer.close();
        } catch (Exception e) {
            System.out.println("Unable to Write Data");
        }
    }
}
