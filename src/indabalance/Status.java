/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package indabalance;

import indabalance.strategy.ServerRoundRobinStrategy;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sash
 */
public class Status {
    public static Status instance = null;
    
    public static Status getInstance() {
        if (instance == null) {
            instance = new Status();
        }
        
        return instance;
    }
    
    List<ServerRoundRobinStrategy> strategies;
    
    public Status() {
        this.strategies = new ArrayList<ServerRoundRobinStrategy>();
     
        try {
            final ServerSocket serverSocket = new ServerSocket(2999);
            
            Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            try {
                                Socket socket = serverSocket.accept();
                                socket.getOutputStream().write(getStatus().getBytes());
                                socket.close();
                            } catch (IOException ioe) {
                            }
                        }
                    }
                });
            
            thread.start();
        } catch (IOException ioe) {
        }
    }
    
    public void addStrategy(ServerRoundRobinStrategy strategy) {
        strategies.add(strategy);
    }
    
    public final String getStatus() {
        StringBuilder sb = new StringBuilder();
        
        for (ServerRoundRobinStrategy strategy : strategies) {
            sb.append(strategy.getStatus());
            sb.append("\n");
        }
        
        return sb.toString();
    }
    
    public final void printStatus() {
        System.out.println(getStatus());
    }
}
