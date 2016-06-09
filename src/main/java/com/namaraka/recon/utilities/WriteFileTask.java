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

    void writeFiles() throws MyCustomException {

        logger.info("Thread waiting at synchronized block " + Thread.currentThread().getName() + " AND reconstatus is: " + reconDetails.getReconStatus());

        //synchronised can start HERE
        synchronized (GlobalAttributes.MUTEX) {
            //we will lock here on the same MUTEX as the 
            //READ MUTEX because this block should only run at the end of any READ file task
            //No point in this method running before a READ File tast is INcomplete
            //get latest reconobj from DB

            String reconGroupID = reconDetails.getReconGroupID();

            //fetch updated recon progress
            ReconStatus updatedReconStatus = GeneralUtils.getReconProgressFromDB(reconGroupID);

            boolean isFileProcessingDone = GeneralUtils.isFileProcessingDone(reconGroupID, Boolean.TRUE);

            // if (!isFileProcessingDone || updatedReconStatus == ReconStatus.INPROGRESS) {
            if (!isFileProcessingDone || updatedReconStatus != ReconStatus.NEW) {

                logger.debug("Method will return now b'se, another observer is already writing OR the next observer will try to handle this write request");
                return; //only continue if all files are processed && reocn is not in progress
            }

            reconDetails.setReconStatus(ReconStatus.INPROGRESS);
            //update
            DBManager.updateDatabaseModel(reconDetails);

            String callingFilesJson = reconDetails.getCallingFiles();

            List<String> callingFilesList = GeneralUtils.convertFromJson(callingFilesJson, collectionType);

            final Collection<ReportDetails> exceptionsReportFileDetailsList = DBManager.retrieveAllDatabaseRecords(ReportDetails.class, "fileID", callingFilesList);
            final int numOfFiles = (int) DBManager.countRecords(ReportDetails.class, "reconGroupID", reconGroupID, "isToBeReconciled", Boolean.TRUE);

            /*final int totalExceptions = DBManager.countRecords(TemporaryRecords.class, "fileID", callingFilesList);
             final Collection<?> numRecordsList = DBManager.fetchOnlyColumnWithCollection(ReportDetails.class, "numberOfRecords", "fileID", callingFilesList);
            
             int totalRecords = 0;
             for (Object numRecord : numRecordsList) {
             totalRecords += (int) numRecord;
             }*/
            GlobalAttributes.setNewValue(reconGroupID, numOfFiles, GlobalAttributes.numberOfFilesInRecon); //num of files in this recon

            List<TemporaryRecords> allRecords = DBManager.bulkFetchSelectedColumns(TemporaryRecords.class);
            //List<TemporaryRecords> allRecords = DBManager.bulkFetchSelectedColumns(TemporaryRecords.class, fileID);

            Set<String> setOfNonExceptionIDs = new HashSet<>();
            Set<String> setOfAllIDs = new HashSet<>();

            ConcurrentHashMap<String, AtomicInteger> generatedIDCounter = new ConcurrentHashMap<>();

            Map<String, Map<String, String>> fileIDAndGeneratedIDs = new HashMap<>();

            for (TemporaryRecords tempRecord : allRecords) {

                String generatedID = tempRecord.getGeneratedID();
                String fileID = tempRecord.getFileID();
                String fileDetails = tempRecord.getRowDetails();

                int idCount = GlobalAttributes.incrementAndGet(generatedID, generatedIDCounter);

                if (idCount == numOfFiles) {

                    boolean isFailedOrSuccessful = tempRecord.isIsFailedOrSuccessful();

                    if (isFailedOrSuccessful) {
                        setOfNonExceptionIDs.add(generatedID);
                    }
                }

                setOfAllIDs.add(generatedID);

                GeneralUtils.addNonExistentID(fileID, generatedID, fileDetails, fileIDAndGeneratedIDs);

            }

            final Set<String> setOfAllFileExceptionIDs = GeneralUtils.complement(setOfAllIDs, setOfNonExceptionIDs);

            int numOfExceptions = setOfAllFileExceptionIDs.size();

            //these are the records that need to be written to the files - same as the number of exceptions since we are writing only exceptins
            GlobalAttributes.setNewValue(reconGroupID, numOfExceptions, GlobalAttributes.totalReconciledToBeWritten);

            //total number of exceptions in this file
            GlobalAttributes.setNewValue(reconGroupID, numOfExceptions, GlobalAttributes.exceptionsCount);

            List<ExceptionsFile> exceptionFiles = new ArrayList<>();
            for (ReportDetails reportFileDetails : exceptionsReportFileDetailsList) {

                //totalRecords += reportFileDetails.getNumberOfRecords();
                //file details
                String fileID = reportFileDetails.getFileID();
                int numOfFileRecords = reportFileDetails.getNumberOfRecords();
                String exceptionsFilePath = reportFileDetails.getExceptionsFilePath();

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

            //set END-Time
            GlobalAttributes.setReconTimeTracker(Boolean.FALSE, reconGroupID, GlobalAttributes.totalReconTimeTracker);
            GlobalAttributes.exceptionsFilesDetails.put(reconGroupID, exceptionFiles);
        }
    }
}
