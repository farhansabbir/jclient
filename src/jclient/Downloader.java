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
    private Updater updater;
    
    public Downloader(String url, Updater updater)
    {
        
    }
    
    public void run()
    {
        
    }
    
}
