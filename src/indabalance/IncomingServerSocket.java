/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package indabalance;

import indabalance.strategy.ServerRoundRobinStrategy;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author sash
 */
public class IncomingServerSocket extends ServerSocket implements Runnable, RequestSocketQueue {
    
    private String name;
    
    private List<Socket> incomingRequests;
    
    private ServerRoundRobinStrategy strategy;

    public IncomingServerSocket(String name, int port, ServerRoundRobinStrategy strategy) throws IOException {
        super(port);
        this.name = name;
        this.strategy = strategy;
        this.incomingRequests = new ArrayList();
        new Thread(this).start();
    }
    
    public void listen() {
        boolean done = false;
        
        while (!done) {
            try {
                Socket requestSocket = accept();
                addSocketToQueue(requestSocket);
            } catch (IOException ioe) {
                System.err.println(new Date().toString() + "   IN " + name);
                ioe.printStackTrace();
            }
        }
    }
    
    public synchronized boolean isEmpty() {
        return incomingRequests.isEmpty();
    }
    
    @Override
    public synchronized void addSocketToQueue(Socket requestSocket) {
        System.out.println(new Date().toString() + "   IN: " + name + " " + getLocalPort() + " Adding socket to queue");
        incomingRequests.add(requestSocket);
        System.out.println(new Date().toString() + "   IN: " + name + " " + getLocalPort() + " Adding socket to queue: done, queue size: " + incomingRequests.size());
        notify();
    }

    @Override
    public synchronized void returnSocketToQueue(Socket requestSocket) {
        System.out.println(new Date().toString() + "   IN: " + name + " " + getLocalPort() + " Returning socket to queue");
        incomingRequests.add(0, requestSocket);
        System.out.println(new Date().toString() + "   IN: " + name + " " + getLocalPort() + " Returning socket to queue: done, queue size: " + incomingRequests.size());
        notify();
    }
    
    public synchronized Socket pullSocketFromQueue() {
        Socket request = incomingRequests.get(0);
        incomingRequests.remove(0);
        System.out.println(new Date().toString() + "   IN: " + name + " " + getLocalPort() + " Pulling socket from queue done, queue size: " + incomingRequests.size());
        
        return request;
    }
    
    public synchronized void processSocketsInQueue() {
        while (true) {
            while (!incomingRequests.isEmpty()) {
                strategy.nextWorker().startRequestProcessing(pullSocketFromQueue());
            }

            try {
                wait();
            } catch (InterruptedException ie) {
            }
        }
    }
    
    @Override
    public void run() {
        processSocketsInQueue();
    }
    
}
