/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon;

import com.namaraka.recon.model.v1_0.ReportDetails;
import com.namaraka.recon.dbaccess.DBManager;
import com.namaraka.recon.exceptiontype.MyCustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author smallgod
 */
public class HandleFiles {

    private static final Logger logger = LoggerFactory.getLogger(HandleFiles.class);

    private HandleFiles() {
        //called only once
    }

    private static class HandleFilesSingletonHolder {

        private static final HandleFiles INSTANCE = new HandleFiles();
    }

    public static HandleFiles getInstance() {
        return HandleFilesSingletonHolder.INSTANCE;
    }

    protected Object readResolve() {
        return getInstance();
    }
    
    
    //start a thread to do this task cuz it can involve saving/uploading millions of records in a file    
    //by this time, the file is already saved to the disk
    public static void recieveReportFileUploads(ReportDetails reportFileDetails) throws MyCustomException{
        
        //save the report first to DB
        long objectId = DBManager.persistDatabaseModel(reportFileDetails);
        logger.debug("Report File details saved with ID:= " + objectId);
        
        //after saving the record, read the file from the disk for processing
        String statusColumnName = reportFileDetails.getStatusColumnName();
        boolean isMaster = reportFileDetails.isIsMaster();
        
        if(isMaster){ //do not process master now - wait to process master till the reconcileButton has been clicked
            
            //store in temporary map till reconcileButton has been hit - then retrieve from map and process
            
            
        } else{//process now if not master
            
        }
            
    }
        

}
