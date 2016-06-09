/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon;

import java.io.FileNotFoundException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.eclipse.jetty.plus.webapp.EnvConfiguration;
import org.eclipse.jetty.plus.webapp.PlusConfiguration;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Slf4jLog;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.FragmentConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.xml.XmlConfiguration;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author smallgod
 */
public class AppEntry implements Daemon, ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(AppEntry.class);

    private ExecutorService processorService;
   // private PaymentProcessor paymentProcessor;
    private Server server;

    public static DaemonContext context;

    
    @Override
    public void init(DaemonContext context) throws DaemonInitException {

        AppEntry.context = context;

        try {

            System.out.println("init Daemon method () called..");
            
            ApplicationPropertyLoader.loadInstance();

            //Log.setLog(new Slf4jLog());
            processorService = Executors.newSingleThreadExecutor();

            //has the actual worker threads that are going to be processing payments
            //paymentProcessor = new PaymentProcessor();

            //our server for accepting external requests
            server = new Server();

            // Resource jettyConfig = Resource.newSystemResource("jetty.xml");
            // XmlConfiguration configuration = new XmlConfiguration(jettyConfig.getInputStream());
            // Server server = (Server)configuration.configure();
            // Enable parsing of jndi-related parts of web.xml and jetty-env.xml
            Configuration.ClassList classlist = Configuration.ClassList.setServerDefault(server);
            System.out.println("Class list:: " + classlist.size() + " these are :: " + classlist.get(0) + " " + classlist.get(1) + " " + classlist.get(2) + " " + classlist.get(3) + " " + classlist.get(4));

//        classlist.addAfter(
//                "org.eclipse.jetty.webapp.FragmentConfiguration",
//                "org.eclipse.jetty.plus.webapp.EnvConfiguration",
//                "org.eclipse.jetty.plus.webapp.PlusConfiguration"
//        );
            ServerConnection serverConnector = ServerConnection.getInstance();
            ServerHandler serverHandler = ServerHandler.getInstance();

            //connectors        
            Connector httpsConnector = serverConnector.getHTTPSConnector(server);
            Connector adminConnector = serverConnector.getAdminDefaultConnector(server);
            Connector httpConnector = serverConnector.getDefaultConnector(server);

            //handlers
            Handler resourceHandler = serverHandler.getResourceHandler();
            Handler contextHandler = serverHandler.getContextHandler();
            Handler servletContextHandler = serverHandler.getServletContextHandler();
            Handler webAppContextHandlerProd = serverHandler.getWebAppContextHandlerProd();
            Handler webAppContextHandlerStaging = serverHandler.getWebAppContextHandlerStaging();

            server.addConnector(httpConnector);
            server.setHandler(webAppContextHandlerStaging);
            //server.addConnector(httpsConnector);
            //server.addConnector(adminConnector);

            //ContextHandlerCollection contexts = new ContextHandlerCollection();
            //contexts.setHandlers(new Handler[] { context0, webapp });
            //server.setHandler(contexts);
            //server.setConnectors(new Connector[]{ connector0, connector1, ssl_connector });
            // Extra options
            server.setDumpAfterStart(true);
            server.setDumpBeforeStop(true);
            server.setStopAtShutdown(true);

        } catch (FileNotFoundException fne) {

            System.err.println("Error initialising daemon: " + fne.getMessage());
            System.exit(1);

        } catch (Exception ex) {

            System.err.println("Error initialising daemon: " + ex.getMessage());
            System.exit(1);
        }

    }

    @Override
    public void start() {

        try {

            server.start();
            server.dumpStdErr();

            if (server.isStarted()) {

                System.err.println("Yoyoyoyoyoyoooooo!!!! Jetty server is started na bidi");
            }

            //Future executingProcess = processorService.submit(paymentProcessor);
            //server.join(); //think of putting this inside an executor so that it doesn't hang

        } catch (Exception ex) {

            System.err.println("Error starting server & processorService: " + ex.getMessage());
            System.exit(1);
        }
    }

    @Override
    public void stop() {

        try {

            System.out.println("STOPPING the Server & the ProcessorDaemon...");
            shutdown();
            logger.debug("Server & the ProcessorDaemon stopped.");

        } catch (Exception ex) {
            logger.error("Error occurred while shutting down: " + ex.getMessage());
        }
    }

    @Override
    public void destroy() {

        System.out.print("Destroying ReconTool daemon () method called..");
    }

    //ServertContextListener methods
    @Override //called before even the Jettyserver starts - be-ware when initialising stuff here
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        System.out.println("ServerletContext Initialised () method called");

    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

        System.out.println("ServletContext destroyed () method called");
    }

    /**
     * The following method shuts down an ExecutorService in two phases, first
     * by calling shutdown to reject incoming tasks, and then calling
     * shutdownNow, if necessary, to cancel any lingering tasks (timeToWait
     * time) elapses.
     *
     * @param pool the executor service pool
     */
    private void shutdownProcessor(final ExecutorService pool, long timeToWait, TimeUnit timeUnit) {

        logger.info("Executor pool waiting for tasks to complete");
        pool.shutdown(); // Disable new tasks from being submitted

        try {

            boolean terminatedOK = pool.awaitTermination(timeToWait, timeUnit);

            // Wait a while for existing tasks to terminate
            if (!terminatedOK) {

                // Wait a while for tasks to respond to being cancelled
                terminatedOK = pool.awaitTermination(++timeToWait, timeUnit);

                if (!terminatedOK) {
                    logger.warn("Executor waiting for pending tasks, another " + timeToWait + " " + timeUnit.toString() + "...");

                    pool.shutdownNow(); // Cancel currently executing tasks
                    logger.warn("Executor ShutdownNow with pending tasks");
                }

            } else {
                logger.info("Executor pool completed all tasks and has shut "
                        + "down normally");
            }
        } catch (InterruptedException ie) {
            logger.error("Executor pool shutdown error: " + ie.getMessage());
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();

            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Shutdown the server and the processor
     *
     * @throws Exception
     */
    private void shutdown() throws Exception {

        logger.debug("Daemon's Shutdown called!!");
        //paymentProcessor.stopProcessLoop(); //stop fetching payments
        ApplicationPropertyLoader.loadInstance().closeHibernateSessionFactory();
        server.stop(); //stop server
        shutdownProcessor(processorService, 3, TimeUnit.MINUTES); //shutdown the executor service

    }

}
