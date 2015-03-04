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
    private int currentWorkerIndex = 0;
    private List<Worker> workers;
    
    public ServerRoundRobinStrategy() {
        servers = new ArrayList<Server>();
        workers = new ArrayList<Worker>();
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
    
    private synchronized void loadWorkers() {
        if (workers.isEmpty()) {
            for (Server server : servers) {
                for (Worker worker : server.workers()) {
                    workers.add(worker);
                }
            }
        }
    }
    
    public synchronized Worker nextWorker() {
        loadWorkers();
        
        int startWorkerIndex = currentWorkerIndex;
        
        while (!workers.get(currentWorkerIndex).isReady()) {
            currentWorkerIndex++;
            currentWorkerIndex %= workers.size();

            if (currentWorkerIndex == startWorkerIndex) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ie) {
                }
            }
        }
        
        Worker worker = workers.get(currentWorkerIndex);
        currentWorkerIndex++;
        currentWorkerIndex %= workers.size();
        
        return worker;
    }
    
    public String getStatus() {
        StringBuilder sb = new StringBuilder();
        
        for (Server server : servers) {
            sb.append(server.getStatus());
        }
        
        return sb.toString();
    }
    
}
