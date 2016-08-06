/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon;

import org.eclipse.jetty.plus.webapp.EnvConfiguration;
import org.eclipse.jetty.plus.webapp.PlusConfiguration;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.FragmentConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebXmlConfiguration;

/**
 *
 * @author smallgod
 */
public class ServerHandler {

    //PUT IN CONFIGS FILE
    public static final String JETTY_HOME = System.getProperty("jetty.home", "..");

    private ServerHandler() {
        //put everything here that you need loaded once
    }

    private static class ServerHandlerSingletonHolder {

        private static final ServerHandler INSTANCE = new ServerHandler();
    }

    public static ServerHandler getInstance() {
        return ServerHandlerSingletonHolder.INSTANCE;
    }

    protected Object readResolve() {
        return getInstance();
    }

    /**
     * Looks for a matching file in the local directory to serve
     *
     * @return
     */
    Handler getResourceHandler() {

        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(true);
        resourceHandler.setWelcomeFiles(new String[]{"index.html"});
        resourceHandler.setResourceBase(".");

        return resourceHandler;

    }

    /**
     * A ContextHandler is a HandlerWrapper that responds only to requests that
     * have a URI prefix that matches the configured context path. Requests that
     * match the context path have their path methods updated accordingly, and
     * the following optional context features applied as appropriate: A Thread
     * Context classloader. A set of attributes A set of init parameters A
     * resource base (aka document root) A set of virtual host names. Requests
     * that don't match are not handled.
     *
     * @return
     */
    Handler getContextHandler() {

        ContextHandler contextHandler = new ContextHandler();
        contextHandler.setContextPath("/hello");
        contextHandler.setResourceBase(".");
        contextHandler.setClassLoader(Thread.currentThread().getContextClassLoader());
        return contextHandler;
    }

    /**
     * A ServletContextHandler is a specialization of ContextHandler with
     * support for standard servlets.
     *
     * @return
     */
    Handler getServletContextHandler() {

        ServletContextHandler servletHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletHandler.setContextPath("/context");
        //servletHandler.addServlet(new ServletHolder(new HelloServlet()), "/*");
        //servletHandler.addServlet(new ServletHolder(new HelloServlet("Buongiorno Mondo")), "/it/*");
        //servletHandler.addServlet(new ServletHolder(new HelloServlet("Bonjour le Monde")), "/fr/*");

        return servletHandler;
    }

    /**
     * A Web Applications contextHandler is a variation of ServletContextHandler
     * that uses the standard layout and web.xml to configure the servlets,
     * filters and other features
     *
     * @return
     */
    Handler getWebAppContextHandlerProd() {

        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/");
        webapp.setWar(JETTY_HOME + "/webapps/test.war");

        return webapp;
    }

    /**
     * If during development, you have not assembled your application into a WAR
     * file, you can run it from its source components with something like:
     *
     * @return
     */
    Handler getWebAppContextHandlerStaging() {

        WebAppContext webAppHandler = new WebAppContext();

        webAppHandler.setContextPath("/recontool");

        //webAppHandler.setWar(ApplicationPropertyLoader.loadInstance().getJarFolderName() + "/src/main/recontool/war/recontool.war");
        //webAppHandler.setResourceBase("/home/smallgod/NetBeansProjects/recontool/src/main/recontool/"); //get app dir variable location
        //webAppHandler.setDescriptor("/home/smallgod/NetBeansProjects/src/main/recontool/WEB-INF/web.xml");
        //webAppHandler.setResourceBase(ApplicationPropertyLoader.loadInstance().getJarFolderName() + "/src/main/recontool/"); //get app dir variable location
        webAppHandler.setResourceBase(ApplicationPropertyLoader.loadInstance().getJarFolderName() + "/src/main/recontool/"); //get app dir variable location
        webAppHandler.setDescriptor(ApplicationPropertyLoader.loadInstance().getJarFolderName() + "/src/main/recontool/WEB-INF/web.xml");

        System.out.println("resource base: " + webAppHandler.getResourceBase());

        // webAppHandler.setDefaultDescriptor(ApplicationPropertyLoader.loadInstance().getJarFolderName() + "/src/main/recontool/webdefault/webdefault.xml"); //copy from The location is JETTY_HOME/etc/webdefault.xml     
        webAppHandler.setParentLoaderPriority(true);//make the ClassLoader behavior more akin to standard Java (as opposed to the reverse requirements for Servlets)

//        Configuration configs[] = new Configuration[]{
//            
//            new FragmentConfiguration(),
//            new PlusConfiguration(),
//            new EnvConfiguration()
//        };
//        context.setConfigurations(configs);
        System.out.println(">>>> Resource base:: " + webAppHandler.getBaseResource() + " and context path: " + webAppHandler.getContextPath());

        return webAppHandler;
    }
}