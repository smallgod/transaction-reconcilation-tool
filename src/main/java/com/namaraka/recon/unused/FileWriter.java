/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon.unused;

import com.namaraka.recon.utilities.ExceptionsFile;
import com.namaraka.recon.model.v1_0.ReportDetails;
import com.google.gson.reflect.TypeToken;
import com.namaraka.recon.constants.ErrorCategory;
import com.namaraka.recon.constants.ErrorCode;
import com.namaraka.recon.constants.FileExtension;
import com.namaraka.recon.constants.ReconStatus;
import com.namaraka.recon.constants.ReconType;
import com.namaraka.recon.constants.TransactionState;
import com.namaraka.recon.dbaccess.DBManager;
import com.namaraka.recon.exceptiontype.MyCustomException;
import com.namaraka.recon.model.v1_0.ReconTransactionsTable;
import com.namaraka.recon.model.v1_0.ReconciliationDetails;
import com.namaraka.recon.utilities.FIleColumns;
import com.namaraka.recon.utilities.FileUtilities;
import com.namaraka.recon.utilities.GeneralUtils;
import com.namaraka.recon.utilities.GlobalAttributes;
import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author smallGod
 */
public class FileWriter {

    private static final Logger logger = LoggerFactory.getLogger(FileWriter.class);
    private static final String ALLRECORDS_SHEETNAME = "Exceptions plus other records";
    private static final String EXCEPTIONS_SHEETNAME = "Exceptions records";

    private final ReconciliationDetails fileReconFileDetails;

    private static final Type stringMapType = new TypeToken<Map<String, String>>() {
    }.getType();

    private static final Type mapInMapType = new TypeToken<Map<String, Map<String, String>>>() {
    }.getType();

    public FileWriter(ReconciliationDetails fileReconFileDetails) {

        this.fileReconFileDetails = fileReconFileDetails;
    }

    public void writeRecordsToFile() throws MyCustomException {
        
        logger.debug("writeRecordsToFile method called");

        boolean isCalling = fileReconFileDetails.isIsCalling();

            if (isCalling) {
                logger.info("Going to write final 2 (call) recon files");
                writeCallReconFiles();
            } else {
                logger.info("Going to write final normal recon file");
                writeRecordsToFileNormally();
            }
        
    }

    /**
     * @throws MyCustomException
     */
    private void writeRecordsToFileNormally() throws MyCustomException {

        String reconGroupID = fileReconFileDetails.getReconGroupID();
        ReconType reconType = fileReconFileDetails.getReconType();
        boolean isCalling = fileReconFileDetails.isIsCalling();
        String callingFiles = fileReconFileDetails.getCallingFiles(); //this is a JSON string map
        String masterFileID = fileReconFileDetails.getReconMasterFileID();
        String masterIdColName = fileReconFileDetails.getReconMasterIdColName();

        final String allRecordsfileToWrite = fileReconFileDetails.getFinalReconFileName();
        final String exceptionsFileToWrite = fileReconFileDetails.getFinalExceptionsReconFileName();

        final DateTime timeNow = new DateTime();

        GlobalAttributes.allRecordsAbsFinalFilePath = allRecordsfileToWrite;
        GlobalAttributes.OnlyExceptionsAbsFinalFilePath = exceptionsFileToWrite;

        String fileExtension;
        FileOutputStream allRecordsFileOUTstream = null;
        FileOutputStream exceptionsFileOUTstream = null;
        FileExtension fileExtConstant;

        try {

            fileExtension = FileUtilities.getFileExtension(allRecordsfileToWrite);
            fileExtConstant = FileExtension.convertToEnum(fileExtension);

            File allRecordsReconFile = new File(allRecordsfileToWrite);
            File exceptionsReconFile = new File(exceptionsFileToWrite);

            allRecordsReconFile.createNewFile();
            exceptionsReconFile.createNewFile();
            //finalReconFile.mkdirs();
            allRecordsFileOUTstream = new FileOutputStream(allRecordsReconFile);
            exceptionsFileOUTstream = new FileOutputStream(exceptionsReconFile);

            Workbook allRecordsWorkBook = FileUtilities.getWorkBookInstanceHelper(fileExtConstant);
            Workbook exceptionsWorkBook = FileUtilities.getWorkBookInstanceHelper(fileExtConstant);

            int allRowNum = 0;
            int excRowNum = 0;

            String[] columnHeaderNamesArray = null;
            final String masterFileHeaders = (String) DBManager.fetchOnlyColumn(ReportDetails.class, "fileHeaderNames", "isMaster", Boolean.TRUE).get(0);
            Map<String, String> masterFileHeaderMap = GeneralUtils.convertFromJson(masterFileHeaders, stringMapType);
            Collection<String> masterFileHeaderNames = masterFileHeaderMap.keySet();
            final Collection<String> defaultMasterFileHeaderNameValues = masterFileHeaderMap.values();

            final CellStyle exceptionsStyle = setExceptionsCellStyle(allRecordsWorkBook);
            final CellStyle allRecordsHeaderStyle = setHeadersCellStyle(allRecordsWorkBook);
            final CellStyle exceptionsHeaderStyle = setHeadersCellStyle(exceptionsWorkBook);
            final Sheet allRecordsSheet = allRecordsWorkBook.createSheet(ALLRECORDS_SHEETNAME);
            final Sheet exceptionsSheet = exceptionsWorkBook.createSheet(EXCEPTIONS_SHEETNAME);

            List<ReportDetails> reportDetailsList = DBManager.getRecordsEqualToPropertyValue(ReportDetails.class, "reconGroupID", reconGroupID);
            final Collection<ReconTransactionsTable> recordsList = DBManager.getRecordsEqualToPropertyValue(ReconTransactionsTable.class, "reconGroupID", reconGroupID);
            GlobalAttributes.setNewValue(reconGroupID, recordsList.size(), GlobalAttributes.totalReconciledToBeWritten);
            final Iterator<ReconTransactionsTable> recordsIterator = recordsList.iterator();

            Map<String, String> extraColsMap = new HashMap<>();
            //Collection<String> fileIDs = new ArrayList<>();

            for (ReportDetails reportDetailRow : reportDetailsList) {

                String fileID = reportDetailRow.getFileID();
                String extraColumn = fileID + "-" + reportDetailRow.getReportTitle();

                //fileIDs.add(fileID);
                extraColsMap.put(extraColumn, fileID);
            }

            logger.info("extra cols map: " + extraColsMap);

            while (recordsIterator.hasNext()) {

                ReconTransactionsTable reconTransTable = recordsIterator.next();
                String allFileColumnsMap = reconTransTable.getFileRecordsDetails();
                Map<String, Map<String, String>> fileColumnDetails = GeneralUtils.convertFromJson(allFileColumnsMap, mapInMapType);

                Map<String, String> fileColDetailsMap = fileColumnDetails.get(masterFileID);

                //we need to reset masterFileHeaderNameValues for @ iteration            
                Collection<String> masterFileHeaderNameValues;
                if (fileColDetailsMap != null && fileColDetailsMap.size() > 0) {
                    masterFileHeaderNameValues = fileColDetailsMap.values(); //record in master
                } else {
                    masterFileHeaderNameValues = defaultMasterFileHeaderNameValues; //record NOT in master
                }

                Row allRecordsRow;
                Row exceptionsRow;

                if (allRowNum == 0) {

                    allRecordsRow = allRecordsSheet.createRow(allRowNum);
                    exceptionsRow = exceptionsSheet.createRow(allRowNum);

                    //extra fields of displaynames for statuses
                    /*for (String extraColumn : extraColsMap.keySet()) {
                     logger.info("adding extra column: " + extraColumn);
                     boolean added = false;
                     try {
                     added = masterFileHeaderNames.add(extraColumn);
                     } catch (Exception ex) {
                     logger.error("exception trying to add: " + added);
                     ex.printStackTrace();
                     }
                     }*/
                    GeneralUtils.populateExcelFileHeadings(masterFileHeaderNames, extraColsMap.keySet(), allRecordsRow, allRecordsHeaderStyle);
                    GeneralUtils.populateExcelFileHeadings(masterFileHeaderNames, extraColsMap.keySet(), exceptionsRow, exceptionsHeaderStyle);

                    columnHeaderNamesArray = GeneralUtils.convertToStringArray(masterFileHeaderNames);

                    allRowNum++;
                    excRowNum++;

                } //after writing the first header allRecordsRow, we also need to write the first allRecordsRow line at the same time since we called rowIterator.next() which 
                //starts from the first record in the DB that is actually not a header record but actual values record

                allRecordsRow = allRecordsSheet.createRow(allRowNum);
                exceptionsRow = exceptionsSheet.createRow(excRowNum);

                String statusesJson = reconTransTable.getTransactionStatuses();//in the form  ->   {"220":"OK","221":"OK","222":"UNKNOWN"}    - PENDING status is converted to UNKNOWN

                Map<String, String> statusesMap = GeneralUtils.convertFromJson(statusesJson, stringMapType);

                //= DBManager.fetchOnlyColumn(ReportDetails.class, "fileID", "reconGroupID", reconGroupID);
                Map<String, String> newStatusesMap = addMissingStatuses(extraColsMap.values(), statusesMap);

                Collection<String> allStatusValues = newStatusesMap.values();

                String[] allStatusesForThisRow = GeneralUtils.convertToStringArray(allStatusValues);
                boolean markedAsException = markAsException(allStatusesForThisRow);

                if (markedAsException) {
                    reconTransTable.setIsMarkedAsException(Boolean.TRUE); //later save the updated reconTransTable object to help with creation of second exceptions file
                    GlobalAttributes.increment(reconGroupID, 1, GlobalAttributes.exceptionsCount); //increment exceptions count

                } else {
                    reconTransTable.setIsMarkedAsException(Boolean.FALSE);
                }

                //now let's add the column values to each of the cells in this allRecordsRow starting with the main fields, then to the extra added fields
                int columnCount = 0;
                boolean isExistInMaster = reconTransTable.isIsRecordExistinMasterFile();
                for (String columnValue : masterFileHeaderNameValues) {

                    String columnHeaderName = columnHeaderNamesArray[columnCount].trim();
                    String cellValue = columnValue;
                    if (!isExistInMaster) { //if record not in master and cell column is txn ID - we need to include an ID under this cell

                        if (columnHeaderName.equalsIgnoreCase(masterIdColName)) {
                            cellValue = String.valueOf(reconTransTable.getIDValue());
                        }
                    }

                    //if (extraColsMap.containsKey(columnValue)) { //retrieving the extra column added
                    //    String fileid = extraColsMap.get(columnValue);
                    //    cellValue = newStatusesMap.get(fileid);
                    //}
                    logger.info("col header: " + columnHeaderName);
                    logger.info("col value : " + cellValue);

                    Cell cell = addCell(allRecordsRow, cellValue, columnCount);
                    colourExceptionCell(markedAsException, cell, exceptionsStyle);

                    if (markedAsException) {

                        addCell(exceptionsRow, cellValue, columnCount); //add exceptions file cell
                    }

                    columnCount++;
                }

                //now let's add the extra columns - i.e. the status columns cells and colour/format them if need be
                for (String fileId : extraColsMap.values()) {

                    String status = newStatusesMap.get(fileId);

                    Cell cell = addCell(allRecordsRow, status, columnCount);
                    colourExceptionCell(markedAsException, cell, exceptionsStyle);

                    if (markedAsException) {
                        addCell(exceptionsRow, status, columnCount); //add exceptions file cell
                    }
                    columnCount++;
                }

                //update reconTransTable allRecordsRow in DB on a temp session with isRecordSameStatusInAllFiles value && then when out of this loop, commit the transactions in DB i.e. after flushing out to excel work book
                // - this will help us to generate the second file that only has excepti0ns
                allRowNum++; //go to next allRecordsRow
                if (markedAsException) {
                    excRowNum++; // only increment exceptions file count if we have added this record to the exceptions file
                }

                //GlobalFileProcessAttributes.finalFileWriteProgressIndicator ++;
                GlobalAttributes.increment(reconGroupID, GlobalAttributes.fileWriteProgressIndicator);
            }

            logger.info("Final file progress indicator: >>>: " + GlobalAttributes.fileWriteProgressIndicator.get(reconGroupID).get());
            logger.info("Final file total records size: >>>: " + GlobalAttributes.totalReconciledToBeWritten.get(reconGroupID).get());

            allRecordsWorkBook.write(allRecordsFileOUTstream);
            exceptionsWorkBook.write(exceptionsFileOUTstream);

            allRecordsFileOUTstream.flush();
            exceptionsFileOUTstream.flush();

            GlobalAttributes.setReconTimeTracker(Boolean.FALSE, reconGroupID, GlobalAttributes.totalReconTimeTracker);
            GeneralUtils.updateReconProgress(fileReconFileDetails, ReconStatus.COMPLETED);

            logger.info("\n======================== End of this file Recon ========================\n");
            logger.info("Time Recon ended: " + GeneralUtils.timeTakenToNow(timeNow) + " \n");

            //commit the updates above on the reconTransTable to DB - this will help us to generate the second file that only has exceptins
        } catch (FileNotFoundException ex) {
            throw new MyCustomException("File not found Error while writing to file", ErrorCode.SERVER_ERR, "Error occurred while writing to the final recon file: " + ex.getMessage(), ErrorCategory.SERVER_ERR_TYPE);

        } catch (IOException ex) {
            throw new MyCustomException("IO Error while writing to file", ErrorCode.SERVER_ERR, "Error occurred while writing to the final recon file: " + ex.getMessage(), ErrorCategory.SERVER_ERR_TYPE);

        } catch (IllegalArgumentException ex) {
            throw new MyCustomException("Exception while writing to file", ErrorCode.SERVER_ERR, "Error occurred while writing to the final recon file: " + ex.getMessage(), ErrorCategory.SERVER_ERR_TYPE);

        } finally {
            try {
                if (allRecordsFileOUTstream != null) {
                    allRecordsFileOUTstream.close();
                }
                if (exceptionsFileOUTstream != null) {
                    exceptionsFileOUTstream.close();
                }
            } catch (IOException ex) {
                logger.error("IOException closing file stream: " + ex.getMessage());
            }
        }
    }
    
    private void writeCallReconFilesh() throws MyCustomException {
        
        final String reconGroupID = fileReconFileDetails.getReconGroupID();
        final String callingFilesString = fileReconFileDetails.getCallingFiles(); //this is a JSON string map {"232-222":"", "
        final int numOfFiles = fileReconFileDetails.getNoOfFilesInReconGroup();
     
    }

    /**
     * @throws MyCustomException
     */
    private void writeCallReconFiles() throws MyCustomException {
        
        logger.debug("writeCallReconFiles method called!");

        final String reconGroupID = fileReconFileDetails.getReconGroupID();
        final String callingFilesString = fileReconFileDetails.getCallingFiles(); //this is a JSON string map {"232-222":"", "
        final Map<String, Object> callingFiles = GeneralUtils.convertFromJson(callingFilesString, stringMapType);
        final Collection<ReconTransactionsTable> recordsList = DBManager.getRecordsEqualToPropertyValue(ReconTransactionsTable.class, "reconGroupID", reconGroupID);
        final Collection<ReportDetails> reportFileDetailsList = DBManager.retrieveAllDatabaseRecords(ReportDetails.class, "reconGroupID", reconGroupID);

        GlobalAttributes.setNewValue(reconGroupID, recordsList.size(), GlobalAttributes.totalReconciledToBeWritten);

        Map<String, ReportDetails> fileIDDetailsMap = new HashMap<>();

        for (ReportDetails reportDetails : reportFileDetailsList) {
            fileIDDetailsMap.put(reportDetails.getFileID(), reportDetails);
        }

        List<ExceptionsFile> exceptionFiles = new ArrayList<>();
        ExceptionsFile exceptionsFile1;
        ExceptionsFile exceptionsFile2;
        int noOfExceptions1 = 1;
        int noOfExceptions2 = 1;

        Collection<String> fileIDs = callingFiles.keySet();
        for (String fileIDMerge : fileIDs) {

            exceptionsFile1 = new ExceptionsFile();
            exceptionsFile2 = new ExceptionsFile();

            String[] extractedfileIDs = fileIDMerge.split("-");
            String fileID1 = extractedfileIDs[0]; //splitting "232-222"
            String fileID2 = extractedfileIDs[1]; //splitting "232-222"  

            exceptionsFile1.setFileID(fileID1);
            exceptionsFile2.setFileID(fileID2);

            ReportDetails reportDetailsFile1 = fileIDDetailsMap.get(fileID1);
            ReportDetails reportDetailsFile2 = fileIDDetailsMap.get(fileID2);

            String exceptionsFilePath1 = reportDetailsFile1.getExceptionsFilePath();
            String exceptionsFilePath2 = reportDetailsFile2.getExceptionsFilePath();

            exceptionsFile1.setReportDetails(reportDetailsFile1);
            exceptionsFile2.setReportDetails(reportDetailsFile2);

            String fileExtension;
            FileOutputStream exceptionsFileOUTstream1 = null;
            FileOutputStream exceptionsFileOUTstream2 = null;
            FileExtension fileExtConstant;

            try {

                fileExtension = FileUtilities.getFileExtension(exceptionsFilePath1);
                fileExtConstant = FileExtension.convertToEnum(fileExtension);

                File exceptionFile1 = new File(exceptionsFilePath1);
                File exceptionFile2 = new File(exceptionsFilePath2);

                logger.info("------------------------------------");
                logger.info(">>>> exceptions files to write <<<<< ");
                logger.info("file1: " + exceptionsFilePath1);
                logger.info("file2: " + exceptionsFilePath2);
                logger.info("------------------------------------");

                exceptionFile1.createNewFile();
                exceptionFile2.createNewFile();
                //finalReconFile.mkdirs();
                exceptionsFileOUTstream1 = new FileOutputStream(exceptionFile1);
                exceptionsFileOUTstream2 = new FileOutputStream(exceptionFile2);

                Workbook workBookExcepFile1 = FileUtilities.getWorkBookInstanceHelper(fileExtConstant);
                Workbook workBookExcepFile2 = FileUtilities.getWorkBookInstanceHelper(fileExtConstant);

                final CellStyle headerStyleExcepFile1 = setHeadersCellStyle(workBookExcepFile1);
                final CellStyle headerStyleExcepFile2 = setHeadersCellStyle(workBookExcepFile2);

                final Sheet sheetFile1 = workBookExcepFile1.createSheet(reportDetailsFile1.getReportTitle());
                final Sheet sheetFile2 = workBookExcepFile2.createSheet(reportDetailsFile2.getReportTitle());

                final Iterator<ReconTransactionsTable> recordsIterator = recordsList.iterator();

                while (recordsIterator.hasNext()) {

                    ReconTransactionsTable reconTransTable = recordsIterator.next();
                    String statusesJson = reconTransTable.getTransactionStatuses();//in the form  ->   {"220":"OK","221":"OK","222":"UNKNOWN"}    - PENDING status is converted to UNKNOWN

                    Map<String, String> statusesMap = GeneralUtils.convertFromJson(statusesJson, stringMapType);

                    Collection<String> recordFileIDs = fileIDDetailsMap.keySet();   //All keys/FileIDs
                    Map<String, String> newStatusesMap = addMissingStatuses(recordFileIDs, statusesMap);

                    String status1 = newStatusesMap.get(fileID1);
                    String status2 = newStatusesMap.get(fileID2);

                    String allFileColumnsMap = reconTransTable.getFileRecordsDetails();
                    Map<String, Map<String, String>> fileColumnDetails = GeneralUtils.convertFromJson(allFileColumnsMap, mapInMapType);

                    Map<String, String> fileColDetailsMap1 = fileColumnDetails.get(fileID1);
                    Map<String, String> fileColDetailsMap2 = fileColumnDetails.get(fileID2);

                    String successStatus = TransactionState.SUCCESS.getValue();
                    String failStatus = TransactionState.FAIL.getValue();
                    String missingStatus = TransactionState.MISSING_TXN.getValue();

                    logger.info("new statuses: " + newStatusesMap.toString() + " amount: " + reconTransTable.getAmount() + " old statuses: " + reconTransTable.getTransactionStatuses());

                    if (status1.equalsIgnoreCase(successStatus) && status2.equalsIgnoreCase(successStatus)) {
                        logger.info("Not writing to file: Both successful !!\n");
                    } else if (status1.equalsIgnoreCase(failStatus) && status2.equalsIgnoreCase(failStatus)) {
                        logger.info("Not writing to file: Both failed !!\n");
                    } else if (status1.equalsIgnoreCase(missingStatus)) {
                        writeARowToFile(sheetFile2, noOfExceptions2, fileColDetailsMap2, headerStyleExcepFile2);
                        noOfExceptions2++;
                    } else if (status2.equalsIgnoreCase(missingStatus)) {
                        writeARowToFile(sheetFile1, noOfExceptions1, fileColDetailsMap1, headerStyleExcepFile1);
                        noOfExceptions1++;
                    } else {

                        logger.info("status1: " + status1 + " -- Status2: " + status2 + " --- writing exception in both files");

                        writeARowToFile(sheetFile1, noOfExceptions1, fileColDetailsMap1, headerStyleExcepFile1);
                        noOfExceptions1++;

                        writeARowToFile(sheetFile2, noOfExceptions2, fileColDetailsMap2, headerStyleExcepFile2);
                        noOfExceptions2++;

                    }

                    GlobalAttributes.increment(reconGroupID, GlobalAttributes.fileWriteProgressIndicator);
                }

                logger.info("Final file progress indicator: >>>: " + GlobalAttributes.fileWriteProgressIndicator.get(reconGroupID).get());
                logger.info("Final file total records size: >>>: " + GlobalAttributes.totalReconciledToBeWritten.get(reconGroupID).get());

                /*
                 headerNames = fileIDDetailsMap.get(elmaFileID).getFileHeaderNames();
                 Map<String, String> elmaHeaders = GeneralUtils.convertFromJson(headerNames, stringMapType);
                 Collection<String> headers = elmaHeaders.keySet();

                 List<ElmaDetails> elmaDetailsList = DBManager.retrieveAllDatabaseRecords(ElmaDetails.class, "reconTransID", elmaExceptionTransIds);
                 writeRowsToFile(sheetFile1, elmaDetailsList, headers, headerStyleExcepFile2);*/
                workBookExcepFile1.write(exceptionsFileOUTstream1);
                workBookExcepFile2.write(exceptionsFileOUTstream2);

                exceptionsFileOUTstream1.flush();
                exceptionsFileOUTstream2.flush();

                exceptionsFile1.setNoOfExceptions(noOfExceptions1);
                exceptionsFile1.setIsWrittenToFile(Boolean.TRUE);

                exceptionsFile2.setNoOfExceptions(noOfExceptions2);
                exceptionsFile2.setIsWrittenToFile(Boolean.TRUE);

                exceptionFiles.add(exceptionsFile1);
                exceptionFiles.add(exceptionsFile2);

                //commit the updates above on the reconTransTable to DB - this will help us to generate the second file that only has exceptins
            } catch (FileNotFoundException ex) {
                logger.error("FileNotFoundException: " + ex.getMessage());
                throw new MyCustomException("File not found Error while writing to file", ErrorCode.SERVER_ERR, "Error occurred while writing to the final recon file: " + ex.getMessage(), ErrorCategory.SERVER_ERR_TYPE);

            } catch (IOException ex) {
                logger.error("IOException: " + ex.getMessage());
                throw new MyCustomException("IO Error while writing to file", ErrorCode.SERVER_ERR, "Error occurred while writing to the final recon file: " + ex.getMessage(), ErrorCategory.SERVER_ERR_TYPE);

            } catch (IllegalArgumentException ex) {
                logger.error("IllegalArgumentException: " + ex.getMessage());
                throw new MyCustomException("Exception while writing to file", ErrorCode.SERVER_ERR, "Error occurred while writing to the final recon file: " + ex.getMessage(), ErrorCategory.SERVER_ERR_TYPE);

            } finally {
                try {
                    if (exceptionsFileOUTstream1 != null) {
                        exceptionsFileOUTstream1.close();
                    }
                    if (exceptionsFileOUTstream2 != null) {
                        exceptionsFileOUTstream2.close();
                    }
                } catch (IOException ex) {
                    logger.error("IOException closing file stream: " + ex.getMessage());
                }
            }

            logger.info("\n======================== End of this file Recon ========================\n");
            logger.info("Time Recon ended: " + GeneralUtils.timeTakenToNow(new DateTime()) + " \n");

            logger.info("UPDATING RECON PROGRESS TO COMMPLEEEEEEEETTTTTTTTTTEEEEEEEEEEEDDDDDDDDDDD");
            
            GeneralUtils.updateReconProgress(fileReconFileDetails, ReconStatus.COMPLETED);
            GlobalAttributes.setReconTimeTracker(Boolean.FALSE, reconGroupID, GlobalAttributes.totalReconTimeTracker);
            GlobalAttributes.exceptionsFilesDetails.put(reconGroupID, exceptionFiles);
        }
    }

    /**
     *
     * @param exceptionsSheet
     * @param bankReportDetailsList
     * @param headerNames
     * @param headerCellStyle
     */
    void writeRowsToFile(Sheet exceptionsSheet, List<? extends FIleColumns> bankReportDetailsList, Collection<String> headerNames, CellStyle headerCellStyle) {

        int excRowNum = 0;
        Row headerRow = exceptionsSheet.createRow(excRowNum++);
        GeneralUtils.populateExcelFileHeadings(headerNames, headerRow, headerCellStyle);

        for (FIleColumns bankReportDetails : bankReportDetailsList) {

            Row exceptionsRow = exceptionsSheet.createRow(excRowNum);

            String allFileColumns = bankReportDetails.getAllFileColumns();
            Map<String, String> fileColumns = GeneralUtils.convertFromJson(allFileColumns, stringMapType);
            Collection<String> columnValues = fileColumns.values();

            int columnCount = 0;
            for (String cellValue : columnValues) {

                Cell cell = addCell(exceptionsRow, cellValue, columnCount);
                //colourExceptionCell(markedAsException, cell, exceptionsStyle);
                columnCount++;
            }

            excRowNum++;
        }

    }

    /**
     *
     * @param exceptionsSheet
     * @param rowNum
     * @param reconTransTable
     * @param headerNames
     * @param headerCellStyle
     */
    void writeARowToFile(Sheet exceptionsSheet, int rowNum, Map<String, String> fileColumns, CellStyle headerCellStyle) {

        if (fileColumns == null || fileColumns.isEmpty()) {
            logger.info("Gotten NULL or empty fileColumns");
        } else {

            if (rowNum == 1) { //before writing first row of record, add headings
                GeneralUtils.populateExcelFileHeadings(fileColumns.keySet(), exceptionsSheet, headerCellStyle);
            }

            Row row = exceptionsSheet.createRow(rowNum);
            int columnCount = 0;
            Collection<String> columnValues = fileColumns.values();

            for (String cellValue : columnValues) {
                addCell(row, cellValue, columnCount);
                //colourExceptionCell(markedAsException, cell, exceptionsStyle);
                columnCount++;
            }
        }
    }
    
    /**
     * 
     * @param absoluteFilePath
     * @param lineToWrite
     * @throws MyCustomException 
     */
    public static void writeCSVFile(String absoluteFilePath, String [] lineToWrite) throws MyCustomException{
        
        CSVWriter fileWriter = null;
        
        try {
            
            fileWriter = new CSVWriter(new java.io.FileWriter(absoluteFilePath), '\t');
            fileWriter.writeNext(lineToWrite);           
            
            /*for (Iterator iterator = results.iterator();
             iterator.hasNext();) {
             Employee employee = (Employee) iterator.next();
             System.out.print("First Name: " + employee.getFirstName());
             System.out.print("  Last Name: " + employee.getLastName());
             System.out.println("  Salary: " + employee.getSalary());
             }*/
            
        } catch (IOException ex) {
            throw new MyCustomException("Exception while writing to file", ErrorCode.SERVER_ERR, "Error occurred while writing to the final recon file: " + ex.getMessage(), ErrorCategory.SERVER_ERR_TYPE);

        } finally{
            try {
                if(fileWriter != null){
                    fileWriter.close();
                }
            } catch (IOException ex) {
                logger.error("Error while trying to close FileWriter stream " + ex.getMessage());
            }
        }        
    }
        

    /*void writeException(Map<String, String> statusesMap, Collection<String> recordFileIDs, Map<String, ReportDetails> fileIDExceptionsFileName) {

     for (String masterFileID : recordFileIDs) {

     ReportDetails reportDetails = fileIDExceptionsFileName.get(masterFileID);

     if (!(statusesMap.get(masterFileID).equalsIgnoreCase(ReconFileStatuses.OK.getValue()))) {
     //String exceptionsFileToWriteTo = fileIDDetailsMap.get(masterFileID);
     }
     }
     }*/
    /**
     *
     * @param allStatusesForThisRow
     * @return
     */
    public static boolean markAsException(String[] allStatusesForThisRow) {

        boolean markedAsException = Boolean.TRUE;

        int numOfStatuses = allStatusesForThisRow.length;

        if (numOfStatuses > 1) {

            Set<String> setOfStatuses = GeneralUtils.convertToSet(allStatusesForThisRow); //eliminate duplicates
            int setSize = setOfStatuses.size();

            if (setSize == 1) {

                if (setOfStatuses.contains(TransactionState.FAIL.getValue()) || setOfStatuses.contains(TransactionState.SUCCESS.getValue())) {
                    markedAsException = Boolean.FALSE; //Only at this point can a transaction/row be proven NOT to be an exception
                }
            }
        }
        logger.info("all statuses    : " + Arrays.toString(allStatusesForThisRow));
        logger.info("markedAsExceptin: " + markedAsException);

        return markedAsException;
    }

    public static boolean markedAsException(Collection<String> statuses, int numOfStatusColumns) {

        boolean markedAsException = Boolean.TRUE;

        int numOfStatuses = statuses.size();

        if (numOfStatuses == numOfStatusColumns) { //for each status, we add an extra column to show it (if not equal, means txn is missing/has no status in some files)

            Set<String> setOfStatuses = GeneralUtils.convertToSet(statuses); //eliminate duplicates
            int setSize = setOfStatuses.size();

            if (setSize == 1) {

                if (setOfStatuses.contains(TransactionState.FAIL.getValue()) || setOfStatuses.contains(TransactionState.SUCCESS.getValue())) {
                    markedAsException = Boolean.FALSE; //Only at this point can a transaction/row be proven NOT to be an exception
                }
            }
        }

        logger.info("all statuses    : " + statuses);
        logger.info("markedAsExceptin: " + markedAsException);

        return markedAsException;
    }

    /**
     *
     * @param fileIDs
     * @param statusesMap
     * @return
     */
    public static Map<String, String> addMissingStatuses(Collection<String> fileIDs, Map<String, String> statusesMap) { //some records do not exist in some files hence missing status - we insert 'UNKNOWN' for such records

        Map<String, String> newStatusMap = new HashMap<>();

        int numOfStatusesFound = statusesMap.size();// actual number of statuses found

        int numOfStatusesRetrieved = 0;
        //for (Map.Entry<String, ReportDetails> entry : fileIDDetailsMap.entrySet()){System.out.println(entry.getKey() + "/" + entry.getValue());}
        for (String fileID : fileIDs) {

            String status;
            //String masterFileID;            //some rows have more number of columnValues than others e.g.
            if (numOfStatusesRetrieved < numOfStatusesFound) { //keep retrieving until we reach end of array                

                if (statusesMap.containsKey(fileID)) {

                    status = statusesMap.get(fileID);
                    if (status == null || status.isEmpty()) { //make sure status got is not empty or null otherwise insert it as 'UNKNOWN'
                        status = TransactionState.MISSING_STATUS.getValue();
                    } else {
                        numOfStatusesRetrieved++;
                    }
                } else {
                    status = TransactionState.MISSING_TXN.getValue();
                }

            } else {
                status = TransactionState.MISSING_TXN.getValue();
            }
            newStatusMap.put(fileID, status);
        }
        return newStatusMap;
    }

    Cell addCell(Row row, String cellValue, int columnCount) {

        Cell cell = row.createCell(columnCount);
        cell.setCellValue(cellValue);

        return cell;
    }

    void colourExceptionCell(boolean isMarkedAsException, Cell cell, CellStyle exceptionsStyle) {

        if (isMarkedAsException) {
            cell.setCellStyle(exceptionsStyle);
        }
    }

    private static CellStyle setExceptionsCellStyle(Workbook workBook) throws IllegalStateException {

        //set font
        Font font = workBook.createFont();
        //font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        font.setColor(IndexedColors.RED.index);
        //font.setFontHeightInPoints((short) 24);
        font.setFontName("Courier New");
        font.setItalic(false);
        font.setStrikeout(false);

        //style this allRecordsRow - Aqua background
        CellStyle style = workBook.createCellStyle();
        //style.setFillBackgroundColor(IndexedColors.AQUA.getIndex());
        //style.setFillPattern(CellStyle.BIG_SPOTS);
        //style.setFillBackgroundColor(IndexedColors.DARK_TEAL.index);
        style.setFillBackgroundColor(IndexedColors.YELLOW.index);
        style.setFillPattern(CellStyle.FINE_DOTS);
        style.setFont(font);

        return style;
    }

    private static CellStyle setHeadersCellStyle(Workbook workBook) throws IllegalStateException {

        //set font
        Font font = workBook.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        font.setColor(IndexedColors.BLACK.index);
        //font.setFontHeightInPoints((short) 24);
        font.setFontName("Courier New");
        font.setItalic(false);
        font.setStrikeout(false);

        //style this allRecordsRow - Aqua background
        CellStyle style = workBook.createCellStyle();
        //style.setFillBackgroundColor(IndexedColors.AQUA.getIndex());
        //style.setFillPattern(CellStyle.BIG_SPOTS);
        //style.setFillBackgroundColor(IndexedColors.INDIGO.index);
        //style.setFillPattern(CellStyle.SPARSE_DOTS);
        style.setFont(font);

        return style;
    }

    /**
     * Retrieve the ID column label name for a master - helpful when master
     * doesn't have a particular record
     *
     * @param reconGroupID
     * @return iDColumnName
     */
    private String getIDcolumnNameForMaster(String reconGroupID) {

        List<String> idColumnNameValues = DBManager.fetchOnlyColumn(ReportDetails.class, "IDColumnName", "isMaster", Boolean.TRUE, "reconGroupID", reconGroupID); /// this fetching from the DB is not good... if we have 1million records not in master, we will do 1 million fetches - NOT optimised AT ALL

        String iDColumnName = idColumnNameValues.get(0).trim(); //e.g. Transaction ID

        return iDColumnName;

    }

    /**
     *
     * @param fileIDDetailsMap
     * @param statusesMap
     * @return newStatusMap - with all fileIDs and statuses
     */
    /*public static Map<String, String> addMissingStatuses(Map<String, ReportDetails> fileIDDetailsMap, Map<String, String> statusesMap) { //some records do not exist in some files hence missing status - we insert 'UNKNOWN' for such records

     Map<String, String> newStatusMap = new HashMap<>();

     int numOfStatusesFound = statusesMap.size();// actual number of statuses found    
        
     Collection<String> recordFileIDs = fileIDDetailsMap.keySet();

     int numOfStatusesRetrieved = 0;
     //for (Map.Entry<String, ReportDetails> entry : fileIDDetailsMap.entrySet()){System.out.println(entry.getKey() + "/" + entry.getValue());}
     for (String masterFileID : recordFileIDs) {

     String status;
     //String masterFileID;            //some rows have more number of columnValues than others e.g.
     //{"220":"OK","221":"OK"}               and
     //{"220":"OK","221":"OK","222":"OK"}
     if (numOfStatusesRetrieved < numOfStatusesFound) { //keep retrieving until we reach end of array                

     if (statusesMap.containsKey(masterFileID)) {
                    
     status = statusesMap.get(masterFileID);
     if (status == null || status.isEmpty()) { //make sure status got is not empty or null otherwise insert it as 'UNKNOWN'
     status = TransactionState.MISSING_STATUS.getValue();
     } else {
     numOfStatusesRetrieved++;
     }
     } else {
     status = TransactionState.MISSING_TXN.getValue();
     }

     } else {
     status = TransactionState.MISSING_TXN.getValue();
     }
     newStatusMap.put(masterFileID, status);
     }
     return newStatusMap;
     }*/
    /*public static String[] insertMissingStatuses(Collection<String> columnValues, int numOfStatusColumnsToAdd) { //some records do not exist in some files hence missing status - we insert 'UNKNOWN' for such records

     String[] allStatusesForThisRow = new String[numOfStatusColumnsToAdd]; //num of columnValues should be equal to the added status columns or the number of files reconciled
     String[] allStatuses = GeneralUtils.convertToStringArray(columnValues);

     int numOfStatusesFound = allStatuses.length;//2
     int numOfStatusesRetrieved = 0;

     for (int x = 0; x < numOfStatusColumnsToAdd; x++) {

     String status;
     //some rows have more number of columnValues than others e.g.
     //{"220":"OK","221":"OK"}               and
     //{"220":"OK","221":"OK","222":"OK"}

     if (numOfStatusesRetrieved < numOfStatusesFound) { //keep retrieving until we reach end of array

     status = allStatuses[x];
     if (status == null || status.isEmpty()) { //make sure status got is not empty or null otherwise insert it as 'UNKNOWN'
     status = TransactionState.MISSING_STATUS.getValue();
     }
     numOfStatusesRetrieved++;

     } else {
     status = TransactionState.MISSING_TXN.getValue();
     }

     allStatusesForThisRow[x] = status;
     }

     return allStatusesForThisRow;
     }*/
}
