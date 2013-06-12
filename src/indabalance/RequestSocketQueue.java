/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package indabalance;

import java.net.Socket;

/**
 *
 * @author sash
 */
public interface RequestSocketQueue {
    
    public void addSocketToQueue(Socket requestSocket);
    
    public void returnSocketToQueue(Socket requestSocket);
    
}
