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
import com.namaraka.recon.utilities.GlobalAttributes;
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

    private final List<FileProcessingObserver> observers;
    private boolean saveReconCalled;
    private ReconciliationDetails reconDetails;

    public NewReconStarted() {

        this.observers = new ArrayList<>();
    }

    public void saveNewRecon(ReconciliationDetails reconDetails) throws MyCustomException {

        this.reconDetails = reconDetails;
        DBManager.persistDatabaseModel(reconDetails);

        this.saveReconCalled = Boolean.TRUE;

        //Once SaveNewRecon has been called, we can safely notify all Observers and hence call their startRecon methods
        notifyObservers();

        logger.debug("New Recon saved in DB with status - NEW and all observers notified to call their startRecon methods");
    }

//    public ReconciliationDetails saveNewRecon(final String reconGroupID, final String callingFilesJson) throws MyCustomException {
//
//        reconDetails = new ReconciliationDetails();
//
//        reconDetails.setCallingFiles(callingFilesJson);
//        reconDetails.setReconStatus(ReconStatus.NEW);
//        reconDetails.setReconGroupID(reconGroupID);
//
//        DBManager.persistDatabaseModel(reconDetails);
//
//        this.saveReconCalled = Boolean.TRUE;
//        
//        //Once SaveNewRecon has been called, we can safely notify all Observers and hence call their startRecon methods
//        notifyObservers();
//        
//        logger.debug("New Recon saved in DB with status - NEW and all observers notified to call their startRecon methods");
//
//        return reconDetails;
//    }
    @Override
    public synchronized void register(FileProcessingObserver observer) {

        synchronized (GlobalAttributes.OBSERVER_MUTEX) {

            if (observer == null) {
                throw new NullPointerException("Null Observer");
            } else if (!observers.contains(observer)) {

                logger.info("Here adding an obzerver!!");
                observers.add(observer);
            } else {
                logger.info("NOT adding observing since it is already added!!");
            }
        }
    }

    @Override
    public synchronized void unregister(FileProcessingObserver observer) {

        synchronized (GlobalAttributes.OBSERVER_MUTEX) {
            observers.remove(observer);
        }
    }
    
    @Override
    public synchronized void unregisterAllObservers() {

        synchronized (GlobalAttributes.OBSERVER_MUTEX) {
            
            if(observers != null){
                observers.clear();
            }
        }
    }

    @Override
    public synchronized void notifyObservers() throws MyCustomException {

        synchronized (GlobalAttributes.OBSERVER_MUTEX) {

            List<FileProcessingObserver> observersLocal = null;
            //synchronization is used to make sure any observer registered after message is received is not notified

            if (!saveReconCalled) {
                return;
            }
            observersLocal = new ArrayList<>(this.observers);
            this.saveReconCalled = false;

            logger.info(">>>> Going to notify: " + observersLocal.size() + " observerz");

            for (FileProcessingObserver obj : observersLocal) {
                obj.startRecon();

                logger.info("Observer with FileID: " + ((ReadFileTask) obj).getReportFileDetails().getFileID() + " has been notified to startRecon, successfuly");
            }

            logger.info(">>>> DONE notifying ALL observerz to startRecon");

        }
    }

    @Override
    public Object getUpdate(FileProcessingObserver observer) {
        return this.reconDetails;
    }
}
