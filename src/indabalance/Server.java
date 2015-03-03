/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package indabalance;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sash
 */
public class Server {
    private List<Worker> workers;

    public Server() {
        workers = new ArrayList();
    }
    
    public boolean addWorker(Worker worker) {
        if (!workers.contains(worker)) {
            workers.add(worker);
            return true;
        }

        return false;
    }
    
    public boolean removeWorker(Worker worker) {
        if (workers.contains(worker)) {
            workers.remove(worker);
            return true;
        }

        return false;
    }
    
    public List<Worker> workers() {
        return workers;
    }
    
    public String getStatus() {
        StringBuilder sb = new StringBuilder();

        for (Worker worker : workers) {
            sb.append(worker.getStatus());
            sb.append("\n");
        }
        
        return sb.toString();
    }
}
