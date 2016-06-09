/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon;

import com.namaraka.recon.IF.FileProcessingObserved;
import com.namaraka.recon.IF.FileProcessingObserver;
import com.namaraka.recon.constants.ReconStatus;
import com.namaraka.recon.dbaccess.DBManager;
import com.namaraka.recon.exceptiontype.MyCustomException;
import com.namaraka.recon.model.v1_0.ReconciliationDetails;
import com.namaraka.recon.utilities.ReadFileTask;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author smallgod
 */
public class NewReconStarted implements FileProcessingObserved {
    
    private static final Logger logger = LoggerFactory.getLogger(NewReconStarted.class);

    private final Object MUTEX = new Object();
    private final List<FileProcessingObserver> observers;
    private boolean saveReconCalled;
    private ReconciliationDetails reconDetails;

    public NewReconStarted() {

        this.observers = new ArrayList<>();
    }

    ReconciliationDetails saveNewRecon(final String reconGroupID, final String callingFilesJson) throws MyCustomException {

        reconDetails = new ReconciliationDetails();

        reconDetails.setCallingFiles(callingFilesJson);
        reconDetails.setReconStatus(ReconStatus.NEW);
        reconDetails.setReconGroupID(reconGroupID);

        DBManager.persistDatabaseModel(reconDetails);

        this.saveReconCalled = Boolean.TRUE;
        notifyObservers();

        return reconDetails;
    }

    @Override
    public void register(FileProcessingObserver observer) {

        if (observer == null) {
            throw new NullPointerException("Null Observer");
        }
        synchronized (MUTEX) {
            if (!observers.contains(observer)) {
                observers.add(observer);
            }
        }
    }

    @Override
    public void unregister(FileProcessingObserver observer) {

        synchronized (MUTEX) {
            observers.remove(observer);
        }
    }

    @Override
    public void notifyObservers() throws MyCustomException{

        List<FileProcessingObserver> observersLocal = null;
        //synchronization is used to make sure any observer registered after message is received is not notified
        synchronized (MUTEX) {
            if (!saveReconCalled) {
                return;
            }
            observersLocal = new ArrayList<>(this.observers);
            this.saveReconCalled = false;
        }
        
        logger.info(">>>> Going to notify: " + observersLocal.size() + " observerz");
        
        for (FileProcessingObserver obj : observersLocal) {
            obj.startRecon(); 
            
            logger.info("Observer with FileID: " + ((ReadFileTask)obj).getReportFileDetails().getFileID() + " has been notified successfuly");
        }
        
        logger.info(">>>> DONE notifying ALL observerz to startRecon");
    }

    @Override
    public Object getUpdate(FileProcessingObserver observer) {
        return this.reconDetails;
    }

}
