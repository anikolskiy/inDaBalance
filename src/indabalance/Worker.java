/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package indabalance;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 *
 * @author sash
 */
public class Worker {
    
    private final static int RETRY_INTERVAL = 5000;
    
    private String host;
    
    private int port;
    
    private int connectionTimeout;
    
    private String name;
    
    private InOutStreamWorker inOutWorker;
    
    private InOutStreamWorker outInWorker;
    
    private boolean ready;
    
    private long nextRetry;
    
    private RequestSocketQueue requestSocketQueue;
    
    private Socket incomingRequestSocket;
    
    public Worker(String host, int port, int connectionTimeout, RequestSocketQueue requestSocketQueue) {
        this(host, port, connectionTimeout, "unknown worker", requestSocketQueue);
    }
    
    public Worker(String host, int port, int connectionTimeout, String name, RequestSocketQueue requestSocketQueue) {
        this.host = host;
        this.port = port;
        this.connectionTimeout = connectionTimeout;
        this.name = name;
        this.requestSocketQueue = requestSocketQueue;
        this.inOutWorker = new InOutStreamWorker(this, true);
        this.outInWorker = new InOutStreamWorker(this, false);
        this.ready = true;
        this.nextRetry = System.currentTimeMillis();
        this.incomingRequestSocket = null;
    }
    
    public void startRequestProcessing(Socket incomingRequestSocket) {
        try {
            ready = false;
            this.incomingRequestSocket = incomingRequestSocket;

            Socket outgoingSocket;
            
            try {
                long t = System.currentTimeMillis();
                System.out.println("Connecting...");
                SocketAddress sockaddr = new InetSocketAddress(host, port);
                outgoingSocket = new Socket();
                outgoingSocket.connect(sockaddr, connectionTimeout);
                System.out.println("Connected, time: " + (System.currentTimeMillis() - t) + "ms");
            } catch (IOException ioe) {
                ioe.printStackTrace();
                nextRetry = System.currentTimeMillis() + RETRY_INTERVAL;
                ready = true;
                requestSocketQueue.returnSocketToQueue(incomingRequestSocket);

                return;
            }

            inOutWorker.setSockets(incomingRequestSocket, outgoingSocket);
            outInWorker.setSockets(outgoingSocket, incomingRequestSocket);

            System.out.println(name + " started processing request");
        } catch (Throwable t) {
            t.printStackTrace();
            notifyError();
        }
    }
    
    public boolean isReady() {
        if (ready && inOutWorker.isReady() && outInWorker.isReady() && (System.currentTimeMillis() - nextRetry > 0)) {
            return true;
        }
        
        return false;
    }
    
    public void notifyError() {
        nextRetry = System.currentTimeMillis() + RETRY_INTERVAL;
        notifyDone();
    }
    
    public void notifyDone() {
        inOutWorker.close();
        outInWorker.close();

        if (ready) { // display this message only once
            System.out.println(name + " finished processing request");
        }

        incomingRequestSocket = null;
        ready = true;
    }
    
}
