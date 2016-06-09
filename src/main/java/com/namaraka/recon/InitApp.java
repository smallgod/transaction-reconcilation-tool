/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author smallgod
 */
public class InitApp implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(InitApp.class);
    private static ExecutorService fileUploadExecService;
    private static final int NUMBER_OF_THREADS = 20;

    /**
     * @return the fileUploadExecService
     */
    public static ExecutorService getFileUploadExecService() {
        return fileUploadExecService;
    }

    /**
     * @param aFileUploadExecService the fileUploadExecService to set
     */
    private static void setFileUploadExecService(ExecutorService aFileUploadExecService) {
        fileUploadExecService = aFileUploadExecService;
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        System.out.println(">>>>>>>  context initialising.. <<<<<<<");
        ApplicationPropertyLoader.loadInstance(); //load all configs and initialise app 

        //might want to put this under the AppPropertyLoader class
        setFileUploadExecService(Executors.newFixedThreadPool(NUMBER_OF_THREADS));//improve on this, put value if need be in configs file. Also, use another thread policy or strategy

        logger.debug("InitApp servlet (ReconTool) context initialised");

    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

        try {
            getFileUploadExecService().shutdownNow();
        } catch (SecurityException ex) {
            logger.error("Error occurred trying to shutdown fileUploaderExecutor: " + ex.getMessage());
        }
        logger.debug("InitApp servlet (ReconTool) context destroyed");
    }
}
