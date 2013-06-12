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
import java.util.List;

/**
 *
 * @author sash
 */
public class IncomingServerSocket extends ServerSocket implements Runnable, RequestSocketQueue {
    
    private List<Socket> incomingRequests;
    
    private ServerRoundRobinStrategy strategy;

    public IncomingServerSocket(int port, ServerRoundRobinStrategy strategy) throws IOException {
        super(port);
        this.strategy = strategy;
        this.incomingRequests = new ArrayList();
        new Thread(this).start();
    }
    
    public void listen() {
        boolean done = false;
        
        while (!done) {
            try {
                while (!isEmpty()) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ie) {
                    }
                }

                Socket requestSocket = accept();
                addSocketToQueue(requestSocket);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
    
    public synchronized boolean isEmpty() {
        return incomingRequests.isEmpty();
    }
    
    @Override
    public synchronized void addSocketToQueue(Socket requestSocket) {
        System.out.println("Adding socket to queue");
        incomingRequests.add(requestSocket);
        System.out.println("Adding socket to queue: done");
        notify();
    }

    @Override
    public synchronized void returnSocketToQueue(Socket requestSocket) {
        System.out.println("Returning socket to queue");
        incomingRequests.add(0, requestSocket);
        System.out.println("Returning socket to queue: done");
        notify();
    }
    
    public synchronized Socket pullSocketFromQueue() {
        Socket request = incomingRequests.get(0);
        incomingRequests.remove(0);
        
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
