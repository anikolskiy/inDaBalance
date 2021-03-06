/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package indabalance.strategy;

import indabalance.Server;
import indabalance.Worker;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sash
 */
public class ServerRoundRobinStrategy {

    private List<Server> servers;
    private int currentServerIndex = 0;
    
    public ServerRoundRobinStrategy() {
        servers = new ArrayList<Server>();
    }
    
    public synchronized boolean addServer(Server server) {
        if (!servers.contains(server)) {
            servers.add(server);
            return true;
        }

        return false;
    }
    
    public synchronized boolean removeServer(Server server) {
        if (servers.contains(server)) {
            servers.remove(server);
            return true;
        }

        return false;
    }
    
    public synchronized Worker nextWorker() {
        int startServerIndex = currentServerIndex;
        
        while (true) {
            Server server = nextServer();
            Worker worker = server.nextWorker();
            
            if (worker != null) {
                return worker;
            }
            
            if (currentServerIndex == startServerIndex) {
                try {
                    Thread.sleep(1); // TODO implement some better synchronization
                } catch (InterruptedException ie) {
                }
            }
        }
    }
    
    private synchronized Server nextServer() {
        currentServerIndex++;
        currentServerIndex %= servers.size();
        
        return servers.get(currentServerIndex);
    }
    
    public String getStatus() {
        StringBuilder sb = new StringBuilder();
        
        for (Server server : servers) {
            sb.append(server.getStatus());
        }
        
        return sb.toString();
    }
    
}
