/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jclient;

import java.net.*;
import java.io.*;
import java.util.*;

/**
 *
 * @author fsabbir
 */
public class Downloader implements Runnable{
    
    private Hashtable OBJECTS;
    private boolean DONE;
    private HttpURLConnection HTTP_CONN;
    private Updater UPDATER;
    private Thread THREAD;
    
    public Downloader(String url, Updater updater)
    {
        this.UPDATER = updater;
        this.THREAD = new Thread(this);
        this.THREAD.start();
    }
    
    public void run()
    {
        System.out.println(this.UPDATER.isChangeLoaded());
        
    }
    
}
