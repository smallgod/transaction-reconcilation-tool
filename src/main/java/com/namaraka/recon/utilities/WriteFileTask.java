/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon.utilities;

import com.google.gson.reflect.TypeToken;
import com.namaraka.recon.constants.ErrorCategory;
import com.namaraka.recon.constants.ErrorCode;
import com.namaraka.recon.constants.ReconStatus;
import com.namaraka.recon.dbaccess.DBManager;
import com.namaraka.recon.exceptiontype.MyCustomException;
import com.namaraka.recon.model.v1_0.ReconciliationDetails;
import com.namaraka.recon.model.v1_0.ReportDetails;
import com.namaraka.recon.model.v1_0.TemporaryRecords;
import com.opencsv.CSVWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.ServletException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author smallgod
 */
public class WriteFileTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(WriteFileTask.class);

    private static final Type objectMapType = new TypeToken<Map<Object, Object>>() {
    }.getType();
    private static final Type stringArrayType = new TypeToken<String[]>() {
    }.getType();

    private static final Type collectionType = new TypeToken<Collection<String>>() {
    }.getType();
    
    private static final Type stringMapType = new TypeToken<Map<String, Object>>() {
    }.getType();

    private final ReconciliationDetails reconDetails;

    public WriteFileTask(final ReconciliationDetails reconDetails) {

        this.reconDetails = reconDetails;
    }

    @Override
    public synchronized void run() {

        try {

            writeFiles();

        } catch (MyCustomException ex) {
            try {
                throw new ServletException("CustomException", ex);
            } catch (ServletException servletExc) {
                logger.error("Failed to throw servlet exception back: " + servletExc.getMessage());
            }
        }
    }

    public void writeFiles() throws MyCustomException {

        logger.info("Thread waiting at synchronized block " + Thread.currentThread().getName() + " AND reconstatus is: " + reconDetails.getReconStatus());

            String reconGroupID = reconDetails.getReconGroupID();
            ReconStatus updatedReconStatus = GeneralUtils.getReconProgressFromDB(reconGroupID);
            boolean isFileProcessingDone = GeneralUtils.isFileProcessingDone(reconGroupID, Boolean.TRUE);
            
            if (!isFileProcessingDone || updatedReconStatus != ReconStatus.NEW) {

                logger.info("Release resources immediately, another observer is already writing OR the next observer will try to handle this write request");
                return; //only continue if all files are processed && reocn is not in progress
            
            }
        //synchronised can start HERE
        synchronized (GlobalAttributes.WRITE_MUTEX) {
            
            logger.info("Now inside synchronzed block: "  + Thread.currentThread().getName() + " AND reconstatus is: " + reconDetails.getReconStatus() );
            //we will lock here on the same MUTEX as the 
            //READ MUTEX because this block should only run at the end of any READ file task
            //No point in this method running before a READ File tast is INcomplete
            //get latest reconobj from DB

            
            //Fetch updated Values
            reconGroupID = reconDetails.getReconGroupID();
            updatedReconStatus = GeneralUtils.getReconProgressFromDB(reconGroupID);
            isFileProcessingDone = GeneralUtils.isFileProcessingDone(reconGroupID, Boolean.TRUE);
            
            logger.info("Updated ReconStatus  : " + updatedReconStatus);
            logger.info("isFileProcessingDone : " + isFileProcessingDone);

            // if (!isFileProcessingDone || updatedReconStatus == ReconStatus.INPROGRESS) {
            if (!isFileProcessingDone || updatedReconStatus != ReconStatus.NEW) {

                logger.info("Method will return now b'se, another observer is already writing OR the next observer will try to handle this write request");
                return; //only continue if all files are processed && reocn is not in progress
            
            } else{
                
                logger.debug("Continuing to setReconStatus to INPROGRESS!!");
            }
            
            logger.info("Going to be doing some File writing in a few, so Setting ReconStatus to INPROGRESS, dont interfere");

            reconDetails.setReconStatus(ReconStatus.INPROGRESS);
            //update
            DBManager.updateDatabaseModel(reconDetails);
            
            
            logger.debug("DB reconStatus updated to INPROGRESS, yeyyyyyyy !!");

            String callingFilesJson = reconDetails.getCallingFiles();
            
            //List<String> callingFilesList = GeneralUtils.convertFromJson(callingFilesJson, collectionType);
            
            Map<String, Object> callingFilesMap = GeneralUtils.convertFromJson(callingFilesJson, stringMapType);
            Collection<String> callingFilesMapKeys = callingFilesMap.keySet();
            
            List<String> callingFilesList = new ArrayList<>();
            for(String key : callingFilesMapKeys){
                
                List<String> list = GeneralUtils.convertDelimeteredStringToList(key, '-');
                callingFilesList.addAll(list);
            }
            
            //List<String> callingFilesList = GeneralUtils.convertCollectionToList(callingFilesMapKeys);
            
            logger.info("done coversion from JSON, callingFilesList size is: " + callingFilesList.size() + " callingFilesList: " + callingFilesList);

            final Collection<ReportDetails> exceptionsReportFileDetailsList = DBManager.retrieveAllDatabaseRecords(ReportDetails.class, "fileID", callingFilesList);
            final int numOfFiles = (int) DBManager.countRecords(ReportDetails.class, "reconGroupID", reconGroupID, "isToBeReconciled", Boolean.TRUE);

            logger.info("exceptionsReportFileDetailsList size() is: " + exceptionsReportFileDetailsList.size());
            
            if(exceptionsReportFileDetailsList.isEmpty()){
                logger.warn("Didnt get any exceptions using callingFilesList: " + callingFilesList  + ", exceptionsReportFileDetailsList.size() == 0 ");
            }
            logger.info("numOfFiles retrieved == " + numOfFiles + " Going to set GlobalAttributes.numberOfFilesInRecon and recongroupid: " + reconGroupID);
            
            /*final int totalExceptions = DBManager.countRecords(TemporaryRecords.class, "fileID", callingFilesList);
             final Collection<?> numRecordsList = DBManager.fetchOnlyColumnWithCollection(ReportDetails.class, "numberOfRecords", "fileID", callingFilesList);
            
             int totalRecords = 0;
             for (Object numRecord : numRecordsList) {
             totalRecords += (int) numRecord;
             }*/
            GlobalAttributes.setNewValue(reconGroupID, numOfFiles, GlobalAttributes.numberOfFilesInRecon); //num of files in this recon
            
            logger.info("GlobalAttributes.numberOfFilesInRecon, set to new value: " + numOfFiles + ", about to bulf-fetch Temporary Records");

            List<TemporaryRecords> allRecords = DBManager.bulkFetchSelectedColumns(TemporaryRecords.class);
            //List<TemporaryRecords> allRecords = DBManager.bulkFetchSelectedColumns(TemporaryRecords.class, fileID);

            Set<String> setOfNonExceptionIDs = new HashSet<>();
            Set<String> setOfAllIDs = new HashSet<>();

            ConcurrentHashMap<String, AtomicInteger> generatedIDCounter = new ConcurrentHashMap<>();

            Map<String, Map<String, String>> fileIDAndGeneratedIDs = new HashMap<>();

            logger.info("Going to iterate over Temporary records");
            for (TemporaryRecords tempRecord : allRecords) {

                String generatedID = tempRecord.getGeneratedID();
                String fileID = tempRecord.getFileID();
                String fileDetails = tempRecord.getRowDetails();

                int idCount = GlobalAttributes.incrementAndGet(generatedID, generatedIDCounter);

                if (idCount == numOfFiles) {

                    boolean isNonException = tempRecord.isIsFailedOrSuccessful();

                    if (isNonException) {
                        setOfNonExceptionIDs.add(generatedID);
                    }
                }

                setOfAllIDs.add(generatedID);

                GeneralUtils.addNonExistentID(fileID, generatedID, fileDetails, fileIDAndGeneratedIDs);

            }

            final Set<String> setOfAllFileExceptionIDs = GeneralUtils.complement(setOfAllIDs, setOfNonExceptionIDs);

            int numOfExceptions = setOfAllFileExceptionIDs.size();
            
           
            //these are the records that need to be written to the files - same as the number of exceptions since we are writing only exceptions
            //GlobalAttributes.setNewValue(reconGroupID, numOfExceptions, GlobalAttributes.totalReconciledToBeWritten);

            //total number of exceptions in this file
            GlobalAttributes.setNewValue(reconGroupID, numOfExceptions, GlobalAttributes.exceptionsCount);
            
            logger.info("Total Files records count    : " + setOfAllIDs.size());
            logger.info("Total exception records count: " + numOfExceptions);
            //logger.debug("GlobalAttributes.totalReconciledToBeWritten same as total exceptions records count: " + GlobalAttributes.totalReconciledToBeWritten);
            logger.info("GlobalAttributes.exceptionsCount same as total exceptions records count           : " + GlobalAttributes.exceptionsCount);

            List<ExceptionsFile> exceptionFiles = new ArrayList<>();
            
            int myNumOfRecordsTester = 0;
            for (ReportDetails reportFileDetails : exceptionsReportFileDetailsList) {
                
                logger.info("Now iterating through exceptionsFileDetailList, fileID: " + reportFileDetails.getFileID());

                //totalRecords += reportFileDetails.getNumberOfRecords();
                //file details
                String fileID = reportFileDetails.getFileID();
                int numOfFileRecords = reportFileDetails.getNumberOfRecords();
                String exceptionsFilePath = reportFileDetails.getExceptionsFilePath();

                myNumOfRecordsTester += numOfFileRecords;
                //exceptions header-column data
                String headerColNamesJson = reportFileDetails.getFileHeaderNames();
                String[] headerColumns = GeneralUtils.convertFromJson(headerColNamesJson, stringArrayType);

//                List<String> exceptions = DBManager.bulkFetchExceptions(TemporaryRecords.class, "rowDetails", "generatedID", fileID, numOfFiles);
//                int numOfFileExceptions = exceptions.size();
                List<String> exceptions = new ArrayList<>();
                int numOfFileExceptions = 0;
                Map<String, String> mapOfAllThisFileRecords = fileIDAndGeneratedIDs.get(fileID);

                for (String generatedID : setOfAllFileExceptionIDs) {

                    String rowDetails = mapOfAllThisFileRecords.get(generatedID);

                    if (rowDetails != null) {
                        exceptions.add(rowDetails);
                        numOfFileExceptions++;
                        
                        GlobalAttributes.increment(reconGroupID, GlobalAttributes.totalReconciledToBeWritten);
                    }
                }

                double exceptionRate = ((numOfFileExceptions * 100.0d) / numOfFileRecords);

                DecimalFormat decimalFormat = new DecimalFormat("0.00");
                String formattedValue = decimalFormat.format(exceptionRate);

                logger.info("------------------------------------------------------------------------");
                logger.info(">>>>>>>>>>>>>>>>> FILE - RECONCILIATION STATISTICS <<<<<<<<<<<<<<<<<<<<<");
                logger.info("------------------------------------------------------------------------");
                logger.info("File-ID           : " + fileID);
                logger.info("Total Records     : " + numOfFileRecords);
                logger.info("Total Exceptions  : " + numOfFileExceptions);
                logger.info("Exception-rate    : " + formattedValue + "%");
                logger.info("Exception-filepath: " + exceptionsFilePath);
                logger.info("------------------------------------------------------------------------");

                File exceptionFile = new File(exceptionsFilePath);
                CSVWriter writer = null;
                
                logger.debug("Now going to start writing exceptions file, path: " + exceptionFile.getAbsolutePath());

                try {
                    exceptionFile.createNewFile();
                    writer = new CSVWriter(new java.io.FileWriter(exceptionFile));

                    writer.writeNext(headerColumns);

                    List<String[]> exceptionData = GeneralUtils.convertFromJson(exceptions, stringArrayType);

                    //writer.writeAll(exceptionData);
                    for (String[] exceptionRow : exceptionData) {

                        writer.writeNext(exceptionRow);

                        //for every exception written, increment this map
                        GlobalAttributes.increment(reconGroupID, GlobalAttributes.fileWriteProgressIndicator);

                        logger.debug("FileWriteProgress: " + GlobalAttributes.fileWriteProgressIndicator.get(reconGroupID).get());
                    }

                } catch (IOException ex) {
                    throw new MyCustomException("IO Error", ErrorCode.COMMUNICATION_ERR, "IOException writing CSV file: " + ex.getMessage(), ErrorCategory.SERVER_ERR_TYPE);

                } catch (Exception ex) {
                    throw new MyCustomException("Error", ErrorCode.INTERNAL_ERR, "Exception reading CSV file: " + ex.getMessage(), ErrorCategory.SERVER_ERR_TYPE);

                } finally {
                    if (writer != null) {
                        try {
                            writer.close();
                        } catch (IOException ex) {
                            logger.error("An IO error occurred while trying to close writer stream: " + ex.getMessage());
                        }
                    }
                }

                ExceptionsFile exceptionsFile = new ExceptionsFile();
                exceptionsFile.setFileID(fileID);
                exceptionsFile.setReportDetails(reportFileDetails);
                exceptionsFile.setNoOfExceptions(numOfFileExceptions);
                exceptionsFile.setIsWrittenToFile(Boolean.TRUE);

                exceptionFiles.add(exceptionsFile);

            }
            
            logger.debug("Total number of all file records as gotten from reportDetails objects (reportFileDetails.getNumberOfRecords()) : " + myNumOfRecordsTester);
            logger.debug("Total number of all file records as put in the Temporary DB, should be same as above                           : " + setOfAllIDs.size());

            //set END-Time
            GlobalAttributes.setReconTimeTracker(Boolean.FALSE, reconGroupID, GlobalAttributes.totalReconTimeTracker);
            GlobalAttributes.exceptionsFilesDetails.put(reconGroupID, exceptionFiles);
            
            logger.info("updating Recon in DB to Completed, reconID - " + reconGroupID);
           reconDetails.setReconStatus(ReconStatus.COMPLETED);
            DBManager.updateDatabaseModel(reconDetails);
            
            logger.info("Unregistering all Observers now...");
            GlobalAttributes.newReconStarted.unregisterAllObservers();
            
            //empty DBs --- this is very bad but a hack to make things work for now
            //DBManager.deleteAllRecords("temporary_records");
            //DBManager.deleteAllRecords("recon_details_entity");
            
            //logger.info("Resetting the global maps for ID: " + reconGroupID);
            //GlobalAttributes.resetGlobalAttributes(reconGroupID);
            //logger.info("Done resetting Global attributes");
        }
        
        logger.info("Thread is NOW out of synchronized block " + Thread.currentThread().getName() + " AND reconstatus is: " + reconDetails.getReconStatus());

    }
}
