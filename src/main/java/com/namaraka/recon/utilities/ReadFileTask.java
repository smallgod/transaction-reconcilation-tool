/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon.utilities;

import com.google.gson.reflect.TypeToken;
import com.namaraka.recon.ApplicationPropertyLoader;
import com.namaraka.recon.IF.FileProcessingObserved;
import com.namaraka.recon.IF.FileProcessingObserver;
import com.namaraka.recon.NewReconStarted;
import com.namaraka.recon.constants.ErrorCategory;
import com.namaraka.recon.constants.ErrorCode;
import com.namaraka.recon.constants.FileExtension;
import com.namaraka.recon.constants.FileReconProgressEnum;
import com.namaraka.recon.constants.LinkType;
import com.namaraka.recon.constants.ReconStatus;
import com.namaraka.recon.constants.ReportType;
import com.namaraka.recon.constants.TransactionState;
import com.namaraka.recon.dbaccess.DBManager;
import com.namaraka.recon.exceptiontype.MyCustomException;
import com.namaraka.recon.model.v1_0.ReconciliationDetails;
import com.namaraka.recon.model.v1_0.ReportDetails;
import com.namaraka.recon.model.v1_0.TemporaryRecords;
import com.opencsv.CSVReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import javax.servlet.ServletException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author smallgod
 */
public class ReadFileTask implements Runnable, FileProcessingObserver {

    private static final Logger logger = LoggerFactory.getLogger(ReadFileTask.class);

    private static final Type objectMapType = new TypeToken<Map<Object, Object>>() {
    }.getType();
    private static final Type stringArrayType = new TypeToken<String[]>() {
    }.getType();

    private final ReportDetails reportFileDetails;
    private FileProcessingObserved newReconObserved;

    public ReadFileTask(final ReportDetails reportFileDetails) {

        this.reportFileDetails = reportFileDetails;
    }

    @Override
    public synchronized void run() {

        try {

            readFile();

        } catch (MyCustomException ex) {
            try {
                throw new ServletException("CustomException", ex);
            } catch (ServletException servletExc) {
                logger.error("Failed to throw servlet exception back: " + servletExc.getMessage());
            }
        }
    }

    /**
     *
     * @throws MyCustomException
     */
    private void readFile() throws MyCustomException {

        final LinkType linkType = reportFileDetails.getLinkType();
        final String reconGroupID = reportFileDetails.getReconGroupID();

        if (linkType != LinkType.LINKER) {
            reportFileDetails.setIsToBeReconciled(Boolean.TRUE); //Will be read into the Temp Recon DB
            GlobalAttributes.increment(reconGroupID, GlobalAttributes.numberOfFilesInRecon);
        } else {
            reportFileDetails.setIsToBeReconciled(Boolean.FALSE);
        }

        //save ReportDetails
        DBManager.persistDatabaseModel(reportFileDetails);

        //register observer to the subject
        GlobalAttributes.newReconStarted.register(this);

        //attach observer to subject
        this.setSubject(GlobalAttributes.newReconStarted);

        //synchronized (GlobalAttributes.getReadMutex(reconGroupID)) {
        synchronized (GlobalAttributes.MUTEX) { // correct this line to use the correct MUTEX such as the line below
            
            logger.debug("====================== Entering first synchronised block ========================= " + reportFileDetails.getFileID());

            String oldFileName = ApplicationPropertyLoader.UPLOADS_DIR + reportFileDetails.getFileName();
            String newFileName = ApplicationPropertyLoader.TEMP_DIR;

            final String fileExtension = FileUtilities.getFileExtension(oldFileName);

            if (fileExtension.equalsIgnoreCase(FileExtension.CSV.getValue())) {

                newFileName += FileUtilities.getFileNameAndType(oldFileName);

            } else {

                String newAbsoluteFileName = FileUtilities.changeFileTypeToCSV(oldFileName, FileExtension.CSV.getValue());
                newFileName += FileUtilities.getFileNameAndType(newAbsoluteFileName);
                //FileUtilities.createFileOnDisk(newFileName);
                FileUtilities.convertFileToCSV(oldFileName, newFileName);
            }

            final int numberOfRecords = FileUtilities.countNoOfLinesInFile(newFileName);
            final String exceptionsFile = GeneralUtils.generateExceptionsFilepath(reportFileDetails.getReportTitle(), GlobalAttributes.FILE_EXT_CSV);

            if (linkType != LinkType.LINKER) {
                //increment the compounded total counter
                GlobalAttributes.increment(reconGroupID, numberOfRecords, GlobalAttributes.totalRecordsToBeRead);
            }

            reportFileDetails.setExceptionsFilePath(exceptionsFile);
            reportFileDetails.setNumberOfRecords(numberOfRecords);
            reportFileDetails.setAbsoluteFilePath(oldFileName);
            reportFileDetails.setCsvEquivalentFile(newFileName);

            //update ReportDetails
            DBManager.updateDatabaseModel(reportFileDetails);

            logger.debug("OLD fileName ::: " + reportFileDetails.getAbsoluteFilePath());
            logger.debug("NEW fileName ::: " + reportFileDetails.getCsvEquivalentFile());

            logger.debug("========================= Coming out of first synchronised block ===================== " + reportFileDetails.getFileID());
        }

        //synchronized (GlobalAttributes.getReadMutex(reconGroupID)) {
        synchronized (GlobalAttributes.MUTEX) {

            //we will lock here on the same MUTEX as the 
            //WRITE MUTEX because this block a Write task should only attempt to run
            //only if there is no Running READ File task - hope it makes sense
            //No point in this method running while a READ File task is ongoing
            logger.debug(" ================ Entering Second synchronised block================ " + reportFileDetails.getFileID());

            if (linkType == LinkType.LINKED || linkType == LinkType.LINKER) {

                List<ReportDetails> reportFromDBList = getReportDetailsForLinkType(linkType, reconGroupID);

                if (reportFromDBList.size() > 0) {

                    ReportDetails reportDetailsFromDB = reportFromDBList.get(0);

                    //make sure linker file is read in first
                    ReportDetails linkerReport = getLinkerReport(reportDetailsFromDB, reportFileDetails);
                    readInCSVFile(linkerReport);

                    //read in second report (linked)
                    if (linkerReport == reportDetailsFromDB) {
                        readInCSVFile(reportFileDetails);
                    } else {
                        readInCSVFile(reportDetailsFromDB);
                    }
                }

                return;
            }

            readInCSVFile(reportFileDetails);

            logger.debug("========================= Done reading/saving file at  :: " + System.currentTimeMillis());
            logger.debug("======================= Coming out of second synchronised block======================== " + reportFileDetails.getFileID());
        }
    }

    /**
     *
     * @param reportFromDB
     * @param currentReport
     * @return
     */
    ReportDetails getLinkerReport(ReportDetails reportFromDB, ReportDetails currentReport) {

        if (reportFromDB.getLinkType() == LinkType.LINKER) {
            return reportFromDB;
        } else {
            return currentReport;
        }
    }

    /**
     *
     * @param linkType
     * @param reconGroupID
     * @return
     */
    List<ReportDetails> getReportDetailsForLinkType(LinkType linkType, String reconGroupID) {

        List<ReportDetails> reportFromDBList;
        LinkType linkTypeToQuery;

        if (linkType == LinkType.LINKED) {

            linkTypeToQuery = LinkType.LINKER;

        } else {
            linkTypeToQuery = LinkType.LINKED;
        }

        reportFromDBList = DBManager.getRecordsEqualToPropertyValue(ReportDetails.class, "reconGroupID", reconGroupID, "linkType", linkTypeToQuery);

        return reportFromDBList;
    }

    /**
     * Read in a large CSV file in a chunk size of 10,000 at a time
     *
     * @param reportFileDetails
     * @throws MyCustomException
     */
    private void readInCSVFile(ReportDetails reportFileDetails) throws MyCustomException {

        ReportType reportType = reportFileDetails.getReportType();
        String csvFile = reportFileDetails.getCsvEquivalentFile();
        LinkType linkType = reportFileDetails.getLinkType();

        String[] headerColumnNames = null;

        List<TemporaryRecords> tempRecords = new ArrayList<>(0x2710); //read in 10,000 records

        int indexOfID = -1, indexOfAmount = -1, indexOfStatus = -1, indexOfDescription = -1, indexOfLinkID = -1;

        List<String> columnNamesArray;

        CSVReader csvReader = null;
        File file = null;

        try {

            //RandomAccessFile input = new RandomAccessFile("", "r");
            file = new File(csvFile);

            //lnr = new LineNumberReader(new FileReader(file), 1024);
            //lnr = new LineNumberReader(new FileReader(file));
            csvReader = new CSVReader(new java.io.FileReader(file));

            String[] line;
            int lineNumber = 1;

            while ((line = csvReader.readNext()) != null) {

                //while ((line = lnr.readLine()) != null) {
                //if (lnr.getLineNumber() == 1) {
                if (lineNumber == 1) {

                    headerColumnNames = line;

                    //check that the required columns are present before proceeding
                    GeneralUtils.checkRequiredColumns(reportFileDetails, headerColumnNames);

                    columnNamesArray = Arrays.asList(headerColumnNames);

                    //define the variables below
                    //define the ID column requirement -> all reports must have an ID & amount column
                    indexOfID = columnNamesArray.indexOf(reportFileDetails.getIDColumnName());
                    indexOfAmount = columnNamesArray.indexOf(reportFileDetails.getAmountColumnName());

                    //define the status column requirement -> all reports have status column except Equinox special
                    if (reportType != ReportType.EQUINOX_SPECIAL) {
                        indexOfStatus = columnNamesArray.indexOf(reportFileDetails.getStatusColumnName());
                    }

                    //define the linkID requirement
                    if (linkType == LinkType.LINKER) {

                        indexOfLinkID = columnNamesArray.indexOf(reportFileDetails.getLinkIDColumnName());
                    }

                    //define the amount and description requirement
                    if (reportType == ReportType.ELMA) {
                        indexOfDescription = columnNamesArray.indexOf(reportFileDetails.getDescriptionColumnName());
                    } else if (reportType == ReportType.EQUINOX) {

                    } else if (reportType == ReportType.EQUINOX_SPECIAL) {
                        indexOfDescription = columnNamesArray.indexOf(reportFileDetails.getDescriptionColumnName());
                    } else if (reportType == ReportType.DEFAULT) {

                    } else {

                    }

                    logger.debug("report type  : " + reportType);
                    logger.debug("linkType     : " + linkType);
                    logger.debug("indexOfAmount: " + indexOfAmount);
                    logger.debug("indexOfLinkID: " + indexOfLinkID);
                    logger.debug("indexOfStatus: " + indexOfStatus);
                    logger.debug("indexOfID    : " + indexOfID);
                    logger.debug("indexOfDesc  : " + indexOfDescription);

                    //indexOfLinkID = columnNamesArray.indexOf(reportFileDetails.getLinkIDColumnName());
                    //indexOfAmount = columnNamesArray.indexOf(reportFileDetails.getAmountColumnName());
                    //indexOfStatus = columnNamesArray.indexOf(reportFileDetails.getStatusColumnName());
                    //indexOfDescription = columnNamesArray.indexOf(reportFileDetails.getDescriptionColumnName());
                    logger.debug("Globalmap size :: " + GlobalAttributes.linkMap.size());

                } else {

                    String idValue = null;
                    String amount = null;
                    String description = null;
                    String status = null;
                    String linkID = null;

                    if (indexOfID > -1) {
                        idValue = line[indexOfID]; //mandatory
                    }
                    if (indexOfAmount > -1) {
                        amount = line[indexOfAmount]; //optional
                    }
                    if (indexOfDescription > -1) {
                        description = line[indexOfDescription]; //optional
                    }
                    if (indexOfStatus > -1) {
                        status = line[indexOfStatus]; //optional
                    }
                    if (indexOfLinkID > -1) {
                        linkID = line[indexOfLinkID]; //optional    
                    }

                    if (linkType == LinkType.LINKER) {

                        logger.debug("adding to map :: IDValue : " + idValue + " - linkID : " + linkID);
                        GlobalAttributes.linkMap.put(linkID, idValue); //just map the linkID to ID
                        continue;

                    } else if (linkType == LinkType.LINKED) {
                        idValue = GlobalAttributes.linkMap.get(idValue);
                    }

                    idValue = GeneralUtils.retrieveID(idValue, description, linkID, reportType);

                    //convert status to shortcode
                    TransactionState statusShortCodeEnum = GeneralUtils.assignStatusShortCode(status, idValue, amount, reportFileDetails);
                    String statusShortCode = statusShortCodeEnum.getShortCodeValue();

                    //generate compounded ID
                    String generatedID = GeneralUtils.generateID(idValue, amount, statusShortCode);

                    //we need to store this row as a (fileid + row details) column
                    //Object[][] rowDetailsArray = {{reportFileDetails.getFileID(), line}};
                    //Map<Object, Object> rowDetailsMap = ArrayUtils.toMap(rowDetailsArray);
                    ///String rowDetailsString = GeneralUtils.convertToJson(rowDetailsMap, objectMapType);
                    //let's store the row and ID separately
                    String rowDetailsString = GeneralUtils.convertToJson(line, stringArrayType);

                    String fileID = reportFileDetails.getFileID();

                    boolean isFailedOrSuccessful = GeneralUtils.isFailedOrSuccessful(statusShortCodeEnum);

                    TemporaryRecords tempRecordsObj = new TemporaryRecords(generatedID, isFailedOrSuccessful, fileID, rowDetailsString);
                    tempRecords.add(tempRecordsObj);

                    logger.debug("GENERATED ID VALUE  :::::  " + generatedID);
                    logger.debug("JSON Row :: " + rowDetailsString);

                    //Records are persisted to the DB for every 10,000 records
                    //if ((lnr.getLineNumber() % GlobalAttributes.NUM_ROWS_CHUNK) == 0) {
                    if ((lineNumber % GlobalAttributes.NUM_ROWS_CHUNK) == 0) {

                        logger.debug("Persisting to DB at row number <::> " + lineNumber);

                        //persist all the records in temp storage tempRecords to DB and clear the temp storage                        
                        DBManager.bulkInsert(tempRecords); //using stateless session
                        //DBManager.bulkInsertBatch(tempRecords); //flushing for every batch size reached                     

                        //tempRecords.clear();  OR use below                  
                        tempRecords = new ArrayList<>(0x2710); //reset storage back to empty and store fresh records
                    }

                    GlobalAttributes.increment(reportFileDetails.getReconGroupID(), 1, GlobalAttributes.fileReadProgressIndicator);
                }

                lineNumber++;
            }

            if (tempRecords.size() > 0) { //means we have un-persisted records in the last batch

                logger.debug("Persisting to DB at row number <::> " + lineNumber);

                //persist last batch of records in temp storage tempRecords to DB 
                DBManager.bulkInsert(tempRecords); //using stateless session
                //DBManager.bulkInsertBatch(tempRecords); //flushing for every batch size reached    

                tempRecords = null;
            }

            String headerColNamesJson = GeneralUtils.convertToJson(headerColumnNames, stringArrayType);

            reportFileDetails.setFileHeaderNames(headerColNamesJson);
            reportFileDetails.setFileReconProgress(FileReconProgressEnum.COMPLETED);

            DBManager.updateDatabaseModel(reportFileDetails);

        } catch (NullPointerException npe) {
            throw new MyCustomException("NPE", ErrorCode.SERVER_ERR, "NullPointerException reading CSV file: " + npe.getMessage(), ErrorCategory.SERVER_ERR_TYPE);
        } catch (FileNotFoundException ex) {
            throw new MyCustomException("FileNotFound Error", ErrorCode.COMMUNICATION_ERR, "FileNotFoundException reading CSV file: " + ex.getMessage(), ErrorCategory.SERVER_ERR_TYPE);
        } catch (IOException ex) {
            throw new MyCustomException("IO Error", ErrorCode.COMMUNICATION_ERR, "IOException reading CSV file: " + ex.getMessage(), ErrorCategory.SERVER_ERR_TYPE);

        } finally {

            try {

                if (csvReader != null) {
                    csvReader.close();
                }

            } catch (IOException ex) {
                logger.error("IOException trying to close CSVReader buffer: " + ex.getMessage());
            }

            try {

                if (file != null) {
                    file.delete();
                }

            } catch (Exception ex) {
                logger.error("Exception trying to delete tempFile: " + file.getAbsolutePath() + " : " + ex.getMessage());
            }
        }
    }

    @Override
    public void startRecon() throws MyCustomException {

        logger.info("Observer (ReadFileTask) of fileID: " + this.reportFileDetails.getFileID() + " has been called!!");

        ReconciliationDetails reconDetails = (ReconciliationDetails) newReconObserved.getUpdate(this);

        if (reconDetails == null) {

            logger.info("Reconciliation writing not started Recon is NULL");

        } else {

            String reconGroupID = reconDetails.getReconGroupID();

            //get the latest reconDetails object from the DB - the one passed is subject to changing status to INPROGRESS by other threads
            ReconStatus reconStatus = GeneralUtils.getReconProgressFromDB(reconGroupID);

            if (reconStatus == ReconStatus.NEW) {

                //try to write
                WriteFileTask writeTask = new WriteFileTask(reconDetails);
                Future future = GeneralUtils.executeTask(writeTask);

            } else {
                logger.info("Recon already in PROGRES or COMPLETED - : " + reconStatus + " -> Another observer already handling this or DONE");
                //next observer will try to handle this write request
            }
        }
    }

    @Override
    public void setSubject(FileProcessingObserved subject) {
        this.newReconObserved = subject;
    }

    public ReportDetails getReportFileDetails() {
        return reportFileDetails;
    }
}
