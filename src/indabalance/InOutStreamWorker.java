/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package indabalance;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;

/**
 *
 * @author sash
 */
public class InOutStreamWorker extends Thread {

    private Socket inSocket;
    private Socket outSocket;
    private boolean directionToServer;
    private Worker worker;

    public InOutStreamWorker(Worker worker, boolean directionToServer) {
        super();
        this.directionToServer = directionToServer;
        this.worker = worker;
        this.inSocket = null;
        this.outSocket = null;
        start();
    }

    public synchronized void setSockets(Socket inSocket, Socket outSocket) {
        this.inSocket = inSocket;
        this.outSocket = outSocket;
        notify();
    }

    public void reset() {
        try {
            outSocket.getOutputStream().close();
        } catch (Exception e) {
        }

        try {
            inSocket.getInputStream().close();
        } catch (Exception e) {
        }

        inSocket = null;
        outSocket = null;
    }

    public void close() {
        if (outSocket != null) {
            try {
                outSocket.shutdownOutput();
            } catch (Exception e) {
            }
        }
    }

    private synchronized void waitForSocketsToBeSet() {
        while ((inSocket == null) || (outSocket == null)) {
            try {
                wait();
            } catch (InterruptedException ie) {
            }
        }
    }

    @Override
    public void run() {
        byte[] buffer = new byte[10240]; // 10kB

        // TODO add stopping
        while (true) {
            waitForSocketsToBeSet();

            try {
                int bytesRead;

                //System.out.println((directionToServer ? "IN " : "OUT ") + "reading bytes");
                if ((inSocket == null) || (outSocket == null)) {
                    System.err.println(new Date().toString() + "   " + worker.getName() + " socket null");
                }
                
                if (buffer == null) {
                    System.err.println(new Date().toString() + "   " + worker.getName() + " buffer null");
                }
                
                while ((bytesRead = inSocket.getInputStream().read(buffer)) >= 0) {
                    //System.out.println((directionToServer ? "IN " : "OUT ") + "Read " + bytesRead + " bytes");
                    try {
                        //System.out.println((directionToServer ? "IN " : "OUT ") + "writing bytes");
                        outSocket.getOutputStream().write(buffer, 0, bytesRead);
                        //System.out.println((directionToServer ? "IN " : "OUT ") + "Wrote " + bytesRead + " bytes");
                    } catch (IOException ioe) {
                        System.err.println(new Date().toString() + "   " + worker.getName());
                        ioe.printStackTrace();
                        if (directionToServer) {
                            worker.notifyError();
                        }
                    }
                }
            } catch (SocketException se) {
                // Exception will be raised almost every time close() is called
            } catch (IOException ioe) {
                System.err.println(new Date().toString() + "   " + worker.getName());
                ioe.printStackTrace();
            } catch (RuntimeException e) {
                System.err.println(new Date().toString() + "   " + worker.getName());
                e.printStackTrace();
            }

            worker.notifyDone();
            reset();
        }
    }

    public boolean isReady() {
        return (inSocket == null) && (outSocket == null);
    }
}
