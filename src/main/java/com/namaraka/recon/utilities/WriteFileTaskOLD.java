/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon.utilities;

import com.namaraka.recon.exceptiontype.MyCustomException;
import com.namaraka.recon.model.v1_0.ReconciliationDetails;
import java.io.IOException;
import javax.servlet.ServletException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author smallgod
 */
public class WriteFileTaskOLD implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(WriteFileTaskOLD.class);

    private final ReconciliationDetails reconDetails;
    private final boolean startedByURL;

    public WriteFileTaskOLD(ReconciliationDetails reconDetails, boolean startedByURL) {
        this.reconDetails = reconDetails;
        this.startedByURL = startedByURL;
    }

    @Override
    public synchronized void run() {

        try {

            //startedByURL == false if method - writeFileTask was called by FileReader reconcileFile method and not startRecon method
            boolean isStarted = GeneralUtils.startRecon(reconDetails, startedByURL);
            
            logger.info(">>>> We are here - --- WRITE-FILE-TASK-OLD --- Recon STARTED: " + isStarted);

        } catch (MyCustomException ex) {
            logger.error("MyCustomExc thrown: " + ex.getErrorDetails());
            try {
                throw new ServletException("CustomException", ex);
            } catch (ServletException servletExc) {
                logger.error("Failed to throw servlet exception back: " + servletExc.getMessage());
            }
        } catch (IOException ex) {
        
        logger.error("MyCustomExc thrown: " + ex.getMessage());
            try {
                throw new ServletException("CustomException", ex);
            } catch (ServletException servletExc) {
                logger.error("Failed to throw servlet exception back: " + servletExc.getMessage());
            }
        
        }
    }
}
