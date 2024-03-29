/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jclient;

import java.net.*;
import java.io.*;
import java.util.Hashtable;


/**
 *
 * @author fsabbir
 */
public class Updater implements Runnable{
    
    private HttpURLConnection HTTP_CONN;
    private boolean FIRST_RUN, CHANGE_LOADED;
    private URL URL;
    private String OBJECT_FILE;
    private Thread THREAD;
    private Hashtable DOWNLOADED_OBJECTS, OLD_OBJECTS, CHANGED_OBJECTS;
    private Launcher LAUNCHER;
    
    
    public Updater(String url, String objectfile)
    {
        try
        {
            this.URL = new URL(url);
            this.OBJECT_FILE = objectfile;
            this.DOWNLOADED_OBJECTS = new Hashtable();
            this.OLD_OBJECTS = new Hashtable();
            this.CHANGED_OBJECTS = new Hashtable();
            this.FIRST_RUN = true;
            this.CHANGE_LOADED = true;
            this.THREAD = new Thread(this);
            this.THREAD.start();
        }
        catch(Exception ioex)
        {
            
        }
    }
    
    public Updater(String url)
    {
        try
        {
            this.URL = new URL(url);
            this.OBJECT_FILE = "objs.lst";
            this.DOWNLOADED_OBJECTS = new Hashtable();
            this.OLD_OBJECTS = new Hashtable();
            this.CHANGED_OBJECTS = new Hashtable();
            this.FIRST_RUN = true;
            this.CHANGE_LOADED = true;
            this.THREAD = new Thread(this);
            this.THREAD.start();
        }
        catch(Exception ioex)
        {
            
        }
    }
    
    public void loadUnload(String ObjectName, String Version, int AddRemove)
    {
        // AddRemove = 0 Remove
        // AddRemove = 1 Add/Download
    }
    
    /**
     * This method checks the objects file for new objects available, populates DOWNLOADED_OBJECTS only
     * and calls checkLocalObjects to verify and download what needs to be downloaded.
     */
    
    public void readObjects()
    {
        this.HTTP_CONN = null;
        try
        {
            this.HTTP_CONN = (java.net.HttpURLConnection)(new URL(this.URL+this.OBJECT_FILE).openConnection());
            this.HTTP_CONN.setUseCaches(false);
            if(this.HTTP_CONN == null)
            {
                System.err.println("There was a problem connecting to " + this.URL);
            }
            else
            {
                if(this.HTTP_CONN.getResponseCode()!=HttpURLConnection.HTTP_OK)
                {
                    System.err.println("Something is wrong! Connection to " + this.URL + " is not OK.");
                }
                else
                {
                    this.DOWNLOADED_OBJECTS.clear();
                    DataInputStream in = new DataInputStream(this.HTTP_CONN.getInputStream());
                    String str;
                    while((str = in.readLine()) != null)
                    {
                        java.util.StringTokenizer tokens = new java.util.StringTokenizer(str);
                        while(tokens.hasMoreTokens())
                        {
                            String key = tokens.nextToken();
                            String value = tokens.nextToken();
                            this.DOWNLOADED_OBJECTS.put(key, value);
                        }
                    }
                    in.close();
                    this.checkLocalObjects();
                }
            }
            this.HTTP_CONN.disconnect();
        }
        catch(Exception httpex)
        {
            System.err.println(httpex);
        }
    }
    
    // just in case method; might need in the future; but currently handling thru thread wait and notify
    public synchronized boolean isChangeLoaded()
    {
        return this.CHANGE_LOADED;
    }
    
    // just in case method; might need in the future; but currently handling thru thread wait and notify
    public synchronized void setChangeLoaded(boolean change)
    {
        this.CHANGE_LOADED = change;
        this.CHANGED_OBJECTS.clear();
    }
    
    public synchronized Hashtable getChange()
    {
        if(!this.CHANGE_LOADED)
        {
            try
            {
                wait();
            }
            catch(InterruptedException e)
            {
                
            }
        }
        return this.CHANGED_OBJECTS;
    }
    
    /**
     * Below method checks and populates CHANGED_OBJECTS and OLD_OBJECTS from DOWNLOADED_OBJECTS read in readObjects()
     */
    
    // all downloads are carried out by looking into CHANGED_OBJECTS list.
    // the OLD_OBJECTS hold the last successfully used objects
    // the DOWNLOADED_OBJECTS is responsible to hold list of objects found in central server
    
    private void checkLocalObjects()
    {
        if(this.FIRST_RUN)
        {
            System.out.println("This is first run. Download all please.");
            synchronized(this.CHANGED_OBJECTS)
            {
                java.util.Enumeration enu = this.DOWNLOADED_OBJECTS.keys();
                while(enu.hasMoreElements())
                {
                    String key = (String)enu.nextElement();
                    String value = (String)this.DOWNLOADED_OBJECTS.get(key);
                    this.OLD_OBJECTS.put(key, value);
                    this.CHANGED_OBJECTS.put(key, value); // insert what I have to download
                    this.CHANGE_LOADED = false; // toggle the change state
                    this.CHANGED_OBJECTS.notifyAll(); // notify the downloader that a change is here
                }
                this.FIRST_RUN = false;
            }
            
        }
        else
        {
            // this is required to be sync between Launcher and Updater to keep track if
            // the changed objects array is updated or not. Based on the changed objects,
            // new jars are downloaded. This minimizes network usage and also downloads only what is required
            synchronized(this.CHANGED_OBJECTS)
            {
                java.util.Enumeration enu = this.DOWNLOADED_OBJECTS.keys();
                while(enu.hasMoreElements())
                {
                    String downkey = (String)enu.nextElement();
                    String downvalue = (String)this.DOWNLOADED_OBJECTS.get(downkey);
                    if(this.OLD_OBJECTS.containsKey(downkey)) // this shows OLD_OBJECTS already has the object
                    {
                        if(!(this.OLD_OBJECTS.get(downkey).equals(this.DOWNLOADED_OBJECTS.get(downkey)))) // check if object in OLD is same version as DOWNLOADED
                        {
                            try
                            {
                                if(!this.CHANGE_LOADED) // before making any changes in CHANGED_OBJECTS, see if its contents were downloaded
                                {
                                    this.CHANGED_OBJECTS.wait(); // wait for download to complete
                                }
                            }catch(java.lang.InterruptedException ex)
                            {
                                System.err.println(ex);
                            }    
                            this.CHANGED_OBJECTS.put(downkey, downvalue); // now put the object list in change order
                            this.CHANGE_LOADED = false; // toggle that the change needs to be downloaded again
                            this.CHANGED_OBJECTS.notifyAll(); // notify the download thread that new version is here
                            // later the downloader will update the OLD_OBJECTS as per the downloads made
                                
                        }
                    }
                    else // this shows that OLD_OJECTS do not contain the downloaded object reference at all. So its a new download
                    {
                        try
                        {
                            if(!this.CHANGE_LOADED) // check if previous download is complete
                            {
                                this.CHANGED_OBJECTS.wait(); // and wait
                                this.CHANGED_OBJECTS.put(downkey, downvalue); // now put the new download to change for downloader to download
                                this.CHANGE_LOADED = false; // toggle that new change is here for downloader to download
                                this.CHANGED_OBJECTS.notifyAll();
                            }
                        }catch(java.lang.InterruptedException ex)
                        {
                            System.err.println(ex);
                        }
                    }
                }
            }
            if(this.CHANGE_LOADED)
            {
                this.CHANGED_OBJECTS.clear();
            }
        }
    }
    
    public void run()
    {
        boolean CONTINUE = true;
        
        while(CONTINUE)
        {
            try
            {
                this.readObjects();
                java.util.Enumeration enu = this.CHANGED_OBJECTS.keys();
                /*System.out.println("Changed Objects");
                while(enu.hasMoreElements())
                {
                    String key = (String)enu.nextElement();
                    System.out.println("Key:" + key + ", Value:" + this.CHANGED_OBJECTS.get(key));
                }*/
                Thread.sleep(15000);
                //Thread.sleep(172800000); // this is 2 days * 24 hours * 60 minutes * 60 seconds * 1000 = 2 days in miliseconds
            }
            catch(Exception ex)
            {
                
            }
        }
    }
}
