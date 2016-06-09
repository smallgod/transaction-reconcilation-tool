/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.LocalDateTime;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 *
 * @author smallgod
 */
public class TestClient {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        try {

            Server server = new Server(9007);

            WebAppContext context = new WebAppContext();

            context.setContextPath("/recontool");
            //context.setWar("src/main/recontool/war/aopc.war");
            context.setResourceBase("/home/smallgod/NetBeansProjects/recontool/src/main/recontool");
            context.setDescriptor("/home/smallgod/NetBeansProjects/recontool/src/main/recontool/WEB-INF/web.xml");
            context.setDefaultsDescriptor("/home/smallgod/NetBeansProjects/recontool/src/main/recontool/webdefault/webdefault.xml"); //copy from The location is JETTY_HOME/etc/webdefault.xml     
            context.setParentLoaderPriority(true);//make the ClassLoader behavior more akin to standard Java (as opposed to the reverse requirements for Servlets)

            server.setHandler(context);
            
            server.start();
            server.dumpStdErr();

            ApplicationPropertyLoader.loadInstance();
            
            System.out.println("<<<<<<<< The Joda-time is >>>>>> :: " + new LocalDateTime());

            server.join();

        } catch (Exception ex) {
            
            System.out.println("<<<<<<<< Exception starting server >>>>>> :: " + ex.getMessage());
            Logger.getLogger(TestClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
