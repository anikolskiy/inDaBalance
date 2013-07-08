/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package indabalance;

import indabalance.strategy.ServerRoundRobinStrategy;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import org.ho.yaml.Yaml;

/**
 *
 * @author sash
 */
public class InDaBalance {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ArrayList<Thread> threads = new ArrayList<Thread>();
        
        try {
            HashMap config = (HashMap) Yaml.load(new File("src/config.yml"));
            
            Set<String> balancers = config.keySet();
            
            for (String balancerName : balancers) {
                HashMap balancerConfig = (HashMap) config.get(balancerName);
                int balancerPort      = Integer.parseInt(balancerConfig.get("port").toString());
                int connectionTimeout = 20;
                
                if (balancerConfig.get("connection_timeout") != null) {
                    connectionTimeout = Integer.parseInt(balancerConfig.get("connection_timeout").toString());
                }
                

                ServerRoundRobinStrategy strategy = new ServerRoundRobinStrategy();
                final IncomingServerSocket iss = new IncomingServerSocket(balancerPort, strategy);
                
                Set<String> servers = balancerConfig.keySet();
                
                for (String serverName : servers) {
                    if ("port".equals(serverName)) {
                        continue;
                    }
                    
                    HashMap serverConfig = (HashMap) balancerConfig.get(serverName);
                    
                    String host  = (String) serverConfig.get("host");
                    String portsConfig = (String) serverConfig.get("ports");
                    
                    Server server = new Server();
                    
                    String[] ports = portsConfig.split(",");
                    
                    for (String port : ports) {
                        Worker worker = new Worker(host, Integer.parseInt(port), connectionTimeout, balancerName + "_" + host + "_" + port, iss);
                        server.addWorker(worker);
                    }
                    
                    strategy.addServer(server);
                }
                
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        iss.listen();
                    }
                });
                
                thread.start();
                threads.add(thread);
            }
            
            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException ie) {
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        
    }
}
