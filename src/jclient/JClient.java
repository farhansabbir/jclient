/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jclient;

import java.net.*;
import java.io.*;
import java.util.logging.*;
/**
 *
 * @author fsabbir
 */
public class JClient {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        String proxyHost = System.getProperty("proxyhost");
        String proxyPort = System.getProperty("proxyport");
        String centralserver = System.getProperty("url");
        String objectfile = System.getProperty("objectfile");
        
        System.setProperty("http.proxyHost", proxyHost);
        System.setProperty("http.proxyport", proxyPort);
        String centralurl = "http://" + centralserver + "/";

        Updater updater = new Updater(centralurl, objectfile);
    }
}
