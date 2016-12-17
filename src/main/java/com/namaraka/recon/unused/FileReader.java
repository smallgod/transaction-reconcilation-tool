/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon.unused;

import com.namaraka.recon.model.v1_0.ReportDetails;
import com.google.gson.reflect.TypeToken;
import com.namaraka.recon.ApplicationPropertyLoader;
import com.namaraka.recon.IF.TxnRecordIF;
import com.namaraka.recon.constants.CenteServiceType;
import com.namaraka.recon.constants.ElmaField;
import com.namaraka.recon.constants.ErrorCategory;
import com.namaraka.recon.constants.ErrorCode;
import java.lang.reflect.Type;
import com.namaraka.recon.constants.FilePosition;
import com.namaraka.recon.constants.FileReconProgressEnum;
import com.namaraka.recon.constants.LinkType;
import com.namaraka.recon.constants.ReconEntityNames;
import com.namaraka.recon.constants.ReconStatus;
import com.namaraka.recon.constants.ReconType;
import com.namaraka.recon.constants.ReportType;
import com.namaraka.recon.dbaccess.DBManager;
import com.namaraka.recon.exceptiontype.MyCustomException;
import com.namaraka.recon.model.v1_0.BaseModel;
import com.namaraka.recon.model.v1_0.ElmaDetails;
import com.namaraka.recon.model.v1_0.ReconTransactionsTable;
import com.namaraka.recon.model.v1_0.ReconciliationDetails;
import com.namaraka.recon.model.v1_0.Linked;
import com.namaraka.recon.utilities.CallBack;
import com.namaraka.recon.utilities.FileProcessDeterminants;
import com.namaraka.recon.utilities.FileUtilities;
import com.namaraka.recon.utilities.GeneralUtils;
import com.namaraka.recon.utilities.GlobalAttributes;
import com.namaraka.recon.utilities.WriteFileTaskOLD;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.hibernate.Criteria;
import org.hibernate.StatelessSession;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author smallgod
 */
public class FileReader implements CallBack {

    private static final Logger logger = LoggerFactory.getLogger(FileReader.class);

    //private static boolean DB_TABLE_EMPTY = false; //initialise to DB table has records
    private static final Object READ_LOCK = new Object();

    private static final Type stringMapType = new TypeToken<Map<String, Object>>() { }.getType();

    private static final Type mapInMapType = new TypeToken<Map<String, Map<String, String>>>() { }.getType();

    public FileReader() { }

    
    void convertFileToCSV(){
        
        
    }

    private ReconciliationDetails setDefaultReconDetails(ReportDetails reportFileDetails) {

        String reconTitle = reportFileDetails.getReconTitle();
        String reconGroupID = reportFileDetails.getReconGroupID();
        ReportType reportType = reportFileDetails.getReportType();

        //entityName where the recon details are going to be persisted - put here logic for getting free recon table dynamically
        String entityName = ReconEntityNames.RECONCILIATION_TABLE1.getValue();//delete this later and find a dynamic option to get entity name from free tables

        ReconciliationDetails reconDetailsTable = new ReconciliationDetails();

        //when user clicks start recon - we will move from NEW to INPROGRESS
        reconDetailsTable.setReconStatus(ReconStatus.NEW);
        reconDetailsTable.setReconEntityName(entityName);
        reconDetailsTable.setReconGroupID(reconGroupID);

        String formattedReconTitle = GeneralUtils.replaceSpaces(reconTitle, "_");
        String dateTimeNow = GeneralUtils.getDateTimeNow("Africa/Kampala", "dd-MM-yyyy-HHmmss");
        String absReconFinalFileName = ApplicationPropertyLoader.RECONCILED_DIR + formattedReconTitle + "_" + dateTimeNow + GlobalAttributes.FILE_EXT_XLS; // remove this .xls in the near future
        String absExceptionsReconFinalFileName = ApplicationPropertyLoader.RECONCILED_DIR + formattedReconTitle + "_exceptions_" + dateTimeNow + GlobalAttributes.FILE_EXT_XLS; // remove this .xls in the near future

        reconDetailsTable.setFinalReconFileName(absReconFinalFileName);
        reconDetailsTable.setFinalExceptionsReconFileName(absExceptionsReconFinalFileName);

        if (reportType == ReportType.EQUINOX_SPECIAL) {
            reconDetailsTable.setReconType(ReconType.CENTENARY_SPECIAL);
        } else {
            reconDetailsTable.setReconType(ReconType.NORMAL);
        }

        return reconDetailsTable;
    }

    
    private ReconciliationDetails saveReportAndReconDetails(ReportDetails reportFileDetails) throws MyCustomException {

        logger.debug("saving report && recon details");
        
        FileProcessDeterminants fileProcessDeterminants = reportFileDetails.getFileProcessDeterminants();
        boolean invokedByURL = reportFileDetails.isInvokedByURL();
        LinkType linkedType = reportFileDetails.getLinkType();
        String reconGroupID = reportFileDetails.getReconGroupID();
        boolean isMaster = reportFileDetails.isIsMaster();
        String reportType = reportFileDetails.getReportType().getValue();

        ReconciliationDetails reconDetailsTable;

        if (linkedType == LinkType.LINKED) {
            DBManager.updateDatabaseModel(reportFileDetails); //object aready saved the first time URL invoked it, just update it
        } else {
            long generatedFileKeyID = DBManager.persistDatabaseModel(reportFileDetails);
        }

        if (fileProcessDeterminants.getFilePosition() == FilePosition.FIRST_FILE) {//recon NOT found save it         

            logger.info(">>>>>>>>> FILE POSITION --- FIRST FILE --- persisting to DB reconDetails");
            
            reconDetailsTable = setDefaultReconDetails(reportFileDetails);
            DBManager.persistDatabaseModel(reconDetailsTable);

        } else {
            logger.info(">>>>>>>>> FILE POSITION --- SUBSQUENT FILE --- Reading from DB reconDetails");
            
            reconDetailsTable = GeneralUtils.retrieveReconObjectFromDBHelper(reconGroupID);
        }

        if (isMaster) {
            
            reconDetailsTable.setReconMasterFileID(reportFileDetails.getFileID());
            reconDetailsTable.setReconMasterIdColName(reportFileDetails.getIDColumnName());//use this value e.g. "Transaction ID" if master file doesn't contain this record but we need a way to add the transaction ID value to the excel file            
            DBManager.updateDatabaseModel(reconDetailsTable);
        }
                
        logger.info(">>>>>>>>>>>>>>>>>> link Type    : " + linkedType);
        logger.info(">>>>>>>>>>>>>>>>>> file position: " + fileProcessDeterminants.getFilePosition());
        logger.info(">>>>>>>>>>>>>>>>>> file Type    : " + reportType);        
        logger.info(">>>>>>>>>>>>>>>>>> recon status : " + reconDetailsTable.getReconStatus());

        //return reconDetailsTable.getReconEntityName().trim();
        return reconDetailsTable;
    }

    /**
     * Reconcile the given file
     *
     * @param reportFileDetails
     * @throws MyCustomException
     */
//    public void reconcileFile(ReportDetails reportFileDetails) throws MyCustomException {
//
//        ReconciliationDetails reconDetails = saveReportAndReconDetails(reportFileDetails);
//        //String entityUsed = reconDetailsTable.getReconEntityName().trim();
//        //reportFileDetails.setTempEntityName(entityUsed); //temporarily store the entity name in the report details object
//        DateTime timeNow = new DateTime();
//
//        synchronized (GlobalAttributes.readMutexObjects.get(reportFileDetails.getReconGroupID())) {
//
//            try {
//
//                //if db table size is 0 for a particular report file keyID, this is the first file we are writing to that table so we wont need to do any checks for existing txns
//                //long numOfRecordsInDB = DBManager.countRecords(reportFileDetails.getTempEntityName());
//                //long numOfRecordsInDB = DBManager.countRecords(ReconTransactionsTable.class, "reportFileKeyID", reportFileDetails.getId());
//                //List<ReportDetails> reportFileDetailsKeyIDList = DBManager.fetchOnlyColumn(ReportDetails.class, "id", "reconGroupID", reportFileDetails.getReconGroupID());
//                //long numOfRecordsInDB = DBManager.countRecords(ReconTransactionsTable.class, "reportFileKeyID", reportFileDetailsKeyIDList);
//                long numOfRecordsInDB = DBManager.countRecords(ReconTransactionsTable.class, "reconGroupID", reportFileDetails.getReconGroupID());
//
//                if (numOfRecordsInDB == -1) {
//                    logger.warn("error occurred trying to get the row count of entity: " + reportFileDetails.getTempEntityName());
//                }
//
//                int recordsIterated = DBManager.insertOrUpdateRecord(this, reportFileDetails, numOfRecordsInDB);
//
//                //file recon COMPLETED
//                GeneralUtils.setFileReconProgressHelper(reportFileDetails, "fileID", FileReconProgressEnum.COMPLETED);
//
//                //GlobalFileProcessAttributes.totalNumberRecordsIterated += recordsIterated;
//                GlobalAttributes.increment(reportFileDetails.getReconGroupID(), recordsIterated, GlobalAttributes.totalNumberRecordsIterated);
//
//                logger.info("\n\n========================================== End of this Reconciliation ========================================\n");
//                
//                logger.info("Number of records iterated : " + recordsIterated + " \n");
//                logger.info("ReconGroup ID              : " + reportFileDetails.getReconGroupID() + " \n");
//                logger.info("Recon file Name            : " + reportFileDetails.getFileName() + " \n");
//                logger.info("Records found in DB before : " + numOfRecordsInDB + " \n");
//                logger.info("Recon Total time (plus thread wait time) : " + GeneralUtils.timeTakenToNow(timeNow) + " \n\n");
//
//                logger.info("GlobalTotalRecords for reconID - " + reportFileDetails.getReconGroupID() + ": " + GlobalAttributes.totalUnreconciledRecords.get(reportFileDetails.getReconGroupID()).get());
//                
//                ReconStatus totalReconProgress = reconDetails.getReconStatus();                
//                
//                logger.info(">>>>>> RECON PROGRESS INSIDE RECONCILE_FILE() method: " + totalReconProgress + " <<<<<<<<<<<<");
//
//                if (totalReconProgress != ReconStatus.COMPLETED) {
//                    //start the recon if & only if other files are done and reconcile button has been clicked
//                    Runnable writeFileTask = new WriteFileTaskOLD(reconDetails, Boolean.FALSE);
//                    GeneralUtils.executeTask(writeFileTask); //no need to synchronize here 
//                }
//
//            } catch (NullPointerException npe) {
//                npe.printStackTrace();
//                logger.error("NullPointerException: " + npe.getMessage());
//            } catch (IllegalArgumentException ex) {
//                ex.printStackTrace();
//                logger.error("IllegalArgumentException: " + ex.getMessage());
//            } catch (Exception ex) {
//                ex.printStackTrace();
//                logger.error("Exception: " + ex.getCause().getMessage());
//            }
//        }
//
//        logger.info("Coming out of Synchronised section!!!");
//    }

    @Override
    public void execute(Object data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int readRecordsFromDB(ReportDetails reportFileDetails, StatelessSession tempSession, long numOfRecords) {

        logger.info("REEEAADDDDIIIIIIIIIIIIIIIIIIING from DAAAAAAAAAATAAAAAABASSSSSSSSSSSSSSEEEEEEEEEEEE");
        logger.info("report type: " + reportFileDetails.getReportType());
        LinkType linkType = reportFileDetails.getLinkType();

        //this is going to work just like readRecordsFromFile works 
        //but only that this time we are not iterating through a file but a DB
        String reconID = reportFileDetails.getReconGroupID();
        //String rowHeaderNamesString = reportFileDetails.getMasterFileFields();
        ReportType reportType = reportFileDetails.getReportType();
        FileProcessDeterminants fileProcessDeterminants = reportFileDetails.getFileProcessDeterminants();

        //Map<String, String> rowHeaderNamesMap = GeneralUtils.convertFromJson(rowHeaderNamesString, stringMapType);
        //Collection<String> rowHeaderNames = rowHeaderNamesMap.keySet();
        //List<String> rowHeaderNames = reportFileDetails.getCellHeaderNames();
        Set<Linked> setOfLinkedReportRows = DBManager.bulkFetchByPropertyName(Linked.class, "reconGroupID", reconID, tempSession);

        ReconTransactionsTable reconTransTableRow;
        int count = 0;

        for (Linked linkedFileRow : setOfLinkedReportRows) {

            count++;

            reconTransTableRow = buildReconTransRowFromDB(linkedFileRow);

            if (linkType == LinkType.LINKED) {
                GeneralUtils.assignIDfromLinker(reconTransTableRow, tempSession);
            }

            //List<String> rowHeaderValues = reconTransTableRow.getCellValues();
            String idValue = reconTransTableRow.getIDValue();

            logger.info("::::::::::::::::::::::::::::::::");
            logger.info(":: file ID      > " + reportFileDetails.getFileID());
            logger.info(":: id value     > " + idValue);
            logger.info(":: Txn status   > " + reconTransTableRow.getTempTransStatusHolder());
            logger.info(":: All statuses > " + reconTransTableRow.getTransactionStatuses());
            logger.info(":: file type    > " + reportType);
            logger.info(":: file path    > " + reportFileDetails.getAbsoluteFilePath());
            logger.info("::::::::::::::::::::::::::::::::");

            if (idValue == null || (idValue.isEmpty()) || numOfRecords == 0) {

                tempSession.insert(reconTransTableRow);

            } //Record already in DB or we need to check for ID presence in DB
            else {

                Criteria criteria = tempSession.createCriteria(ReconTransactionsTable.class);
                criteria.add(Restrictions.eq("reconGroupID", reconID));

                CenteServiceType serviceType = reconTransTableRow.getCenteServiceType();

                //Elma AIRTIME Ids are longer than Equinox AIRTIME Ids
                if (serviceType == CenteServiceType.AIRTIME) {

                    String shortIdValue = idValue;
                    if (idValue.length() > 4) {
                        shortIdValue = idValue.substring(2);
                    }
                    criteria.add(Restrictions.like("IDValue", shortIdValue, MatchMode.END));

                } else {
                    criteria.add(Restrictions.eq("IDValue", idValue));
                }

                if (reportType == ReportType.ELMA || reportType == ReportType.EQUINOX_SPECIAL) {
                    criteria.add(Restrictions.eq("amount", reconTransTableRow.getAmount()));
                }

                List<?> dbReconTransRowsList = criteria.list();
                int recordsFound = dbReconTransRowsList.size();

                if (dbReconTransRowsList.isEmpty()) {

                    tempSession.insert(reconTransTableRow);

                } else if (recordsFound == 1) {

                    ReconTransactionsTable recordToUpdate = (ReconTransactionsTable) dbReconTransRowsList.get(0);
                    ReconTransactionsTable updatedDbRecord = updateDbRecordHelper(recordToUpdate, reconTransTableRow, reportFileDetails);

                    tempSession.update(updatedDbRecord);
                } else {
                    logger.warn("size of returned DB rows is not 1 nor 0 - i.e. multiple records with same ID exist in DB - size: " + dbReconTransRowsList.size() + " - Going to update all of them");

                    for (Object dbRecordObject : dbReconTransRowsList) {

                        ReconTransactionsTable recordToUpdate = (ReconTransactionsTable) dbRecordObject;
                        ReconTransactionsTable updatedDbRecord = updateDbRecordHelper(recordToUpdate, reconTransTableRow, reportFileDetails);
                        tempSession.update(updatedDbRecord);
                    }
                    //throw new MyCustomException("Multiple instances of same ID Value found in DB", ErrorCode.SERVER_ERR, "Multiple instances of same ID Value found in DB " + idValue, ErrorCategory.SERVER_ERR_TYPE);
                }
            }

            /*BaseModel objectToSave = null;

             if (reportType == ReportType.EQUINOX_SPECIAL) {
             objectToSave = GeneralUtils.buildEquinoxDetailsHelper(rowHeaderNames, rowHeaderValues, idValue, reportFileDetails);
             } else if (reportType == ReportType.ELMA) {
             objectToSave = buildElmaDetailsHelper(rowHeaderNames, rowHeaderValues, idValue, reportFileDetails);
             }

             if (objectToSave != null) {
             tempSession.insert(objectToSave);
             }*/
            int increment = fileProcessDeterminants.getProcessingEffort().getProcessingEffortValue();
            int currentCount = GlobalAttributes.increment(reconID, increment, GlobalAttributes.fileReadProgressIndicator);

            logger.info(">>>> PROGRESS for " + fileProcessDeterminants.getFilePosition() + " is: " + currentCount);

        }

        if (reportFileDetails.getLinkType() == LinkType.LINKED) {
            GlobalAttributes.linkedFileReconciled.put(reconID, Boolean.TRUE);
        }

        reportFileDetails.setIsToBeReconciled(Boolean.TRUE);
        tempSession.update(reportFileDetails);

        logger.info("DONE!!! Reconciling file: " + reportFileDetails.getFileName());

        return count;

    }

    @Override
    public int readRecordsFromFile(ReportDetails reportFileDetails, StatelessSession tempSession, long numOfRecords) throws MyCustomException {

        logger.info("REEEAADDDDIIIIIIIIIIIIIIIIIIING from FIIIIIIIIIIIILEEEEEEEEEEEE");

        ReportType reportType = reportFileDetails.getReportType();
        String reconID = reportFileDetails.getReconGroupID();
        FileProcessDeterminants fileProcessDeterminants = reportFileDetails.getFileProcessDeterminants();

        Sheet sheet = reportFileDetails.getSheet();
        Iterator<Row> rowIterator = FileUtilities.getWorkBookRowIteratorHelper(sheet);

        List<String> rowHeaderNames = null;
        List<String> rowHeaderValues;
        List<Linked> tempRecordsList;

        boolean firstRow = true;
        int count = 0;

        while (rowIterator.hasNext()) {

            count++;

            if (firstRow) { //we are reading the first row it must be the headers to the file

                rowHeaderNames = GeneralUtils.getCellValuesHelper(rowIterator);

                ArrayList<String> headerNamesArrayList = GeneralUtils.convertListToArrayList(rowHeaderNames);
                //reportFileDetails.setCellHeaderNames(headerNamesArrayList);

                String fileHeaderMapString = GeneralUtils.getFileHeaderNamesHelper(rowHeaderNames);
                reportFileDetails.setFileHeaderNames(fileHeaderMapString);
                DBManager.updateDatabaseModel(reportFileDetails);

                //tempRecordsList = DBManager.retrieveAllDatabaseRecords(Linked.class, "reconGroupID", reconID);
                //DBManager.bulkFetchByPropertyName(null, reconID, READ_LOCK)
                firstRow = false;
                continue; //after retrieving file  headers just proceed to getting values
            } else {
                rowHeaderValues = GeneralUtils.getCellValuesHelper(rowIterator);
            }

            TxnRecordIF reconTransRow = GeneralUtils.addFileFieldsHelper(rowHeaderNames, rowHeaderValues, reportFileDetails, tempSession);

            ReconTransactionsTable reconTransTableRow = (ReconTransactionsTable) reconTransRow;
            String idValue = reconTransTableRow.getIDValue();

            logger.info("::::::::::::::::::::::::::::::::");
            logger.info(":: file ID      > " + reportFileDetails.getFileID());
            logger.info(":: id value     > " + idValue);
            logger.info(":: Txn status   > " + reconTransTableRow.getTempTransStatusHolder());
            logger.info(":: All statuses > " + reconTransTableRow.getTransactionStatuses());
            logger.info(":: file type    > " + reportType);
            logger.info(":: file path    > " + reportFileDetails.getAbsoluteFilePath());
            logger.info("::::::::::::::::::::::::::::::::");

            if (idValue == null || (idValue.isEmpty()) || numOfRecords == 0) {

                tempSession.insert(reconTransTableRow);

            } //Record already in DB or we need to check for ID presence in DB
            else {

                Criteria criteria = tempSession.createCriteria(ReconTransactionsTable.class);
                criteria.add(Restrictions.eq("reconGroupID", reconID));

                CenteServiceType serviceType = reconTransTableRow.getCenteServiceType();

                //Elma AIRTIME Ids are longer than Equinox AIRTIME Ids
                if (serviceType == CenteServiceType.AIRTIME) {

                    String shortIdValue = idValue;
                    if (idValue.length() > 4) {
                        shortIdValue = idValue.substring(2);
                    }
                    criteria.add(Restrictions.like("IDValue", shortIdValue, MatchMode.END));

                } else {
                    criteria.add(Restrictions.eq("IDValue", idValue));
                }

                if (reportType == ReportType.ELMA || reportType == ReportType.EQUINOX_SPECIAL) {
                    criteria.add(Restrictions.eq("amount", reconTransTableRow.getAmount()));
                }

                List<?> dbReconTransRowsList = criteria.list();
                int recordsFound = dbReconTransRowsList.size();

                if (dbReconTransRowsList.isEmpty()) {

                    tempSession.insert(reconTransTableRow);

                } else if (recordsFound == 1) {

                    ReconTransactionsTable dbRecord = (ReconTransactionsTable) dbReconTransRowsList.get(0);
                    ReconTransactionsTable updatedDbRecord = updateDbRecordHelper(dbRecord, reconTransTableRow, reportFileDetails);

                    tempSession.update(updatedDbRecord);
                } else {
                    logger.warn("size of returned DB rows is not 1 nor 0 - i.e. multiple records with same ID exist in DB - size: " + dbReconTransRowsList.size() + " - Going to update all of them");

                    for (Object dbRecordObject : dbReconTransRowsList) {

                        ReconTransactionsTable recordToUpdate = (ReconTransactionsTable) dbRecordObject;
                        ReconTransactionsTable updatedDbRecord = updateDbRecordHelper(recordToUpdate, reconTransTableRow, reportFileDetails);
                        tempSession.update(updatedDbRecord);
                    }
                    //throw new MyCustomException("Multiple instances of same ID Value found in DB", ErrorCode.SERVER_ERR, "Multiple instances of same ID Value found in DB " + idValue, ErrorCategory.SERVER_ERR_TYPE);
                }
            }

            /*BaseModel objectToSave = null;

             if (reportType == ReportType.EQUINOX_SPECIAL) {
             objectToSave = GeneralUtils.buildEquinoxDetailsHelper(rowHeaderNames, rowHeaderValues, idValue, reportFileDetails);
             } else if (reportType == ReportType.ELMA) {
             objectToSave = buildElmaDetailsHelper(rowHeaderNames, rowHeaderValues, idValue, reportFileDetails);
             }

             if (objectToSave != null) {
             tempSession.insert(objectToSave);
             }*/
            int increment = fileProcessDeterminants.getProcessingEffort().getProcessingEffortValue();
            int currentCount = GlobalAttributes.increment(reconID, increment, GlobalAttributes.fileReadProgressIndicator);
            logger.info(">>>> PROGRESS for " + fileProcessDeterminants.getFilePosition() + " is: " + currentCount);

        }//end while loop

        if (reportFileDetails.getLinkType() == LinkType.LINKED) {
            logger.info("SETTTTINGGGGG LinkedFileReconciled to TRUE here in reading from FILE>>>>>>>>>>>>>>>>>>>>");
            GlobalAttributes.linkedFileReconciled.put(reconID, Boolean.TRUE);
        }
        reportFileDetails.setIsToBeReconciled(Boolean.TRUE);
        tempSession.update(reportFileDetails);

        logger.info("DONE!!! Reconciling file: " + reportFileDetails.getFileName());

        return count - 1;

    }

    private boolean checkIfExists(List<ReportType> list1, List<ReportType> list2) {

        for (ReportType object1 : list1) {

            for (ReportType object2 : list2) {
                if (object1 == object2) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkIfExists(ReportType object1, List<ReportType> list2) {

        for (ReportType object2 : list2) {
            if (object1 == object2) {
                return true;
            }
        }
        return false;
    }

    private ElmaDetails buildElmaDetailsHelper(Collection<String> rowHeaderNames, List<String> rowHeaderValues, String reconTransId, ReportDetails reportDetails) {

        logger.info("rowHeaderNames : " + rowHeaderNames.toString() + " and SIZE: " + rowHeaderNames.size());
        logger.info("rowHeaderValues: " + rowHeaderValues.toString() + " and SIZE: " + rowHeaderValues.size());

        ElmaDetails elmaDetails = new ElmaDetails();
        Map<String, String> allFileColumnsMap = new HashMap<>();
        String paymentDetails = null;

        int i = 0;

        for (String rowHeaderName : rowHeaderNames) {

            ElmaField eqionoxColLabel = ElmaField.convertToEnum(rowHeaderName);

            String columnValue = rowHeaderValues.get(i);

            allFileColumnsMap.put(rowHeaderName, columnValue);

            switch (eqionoxColLabel) {

                case ACCOUNT_ID:
                    elmaDetails.setAccountNumber(columnValue);
                    break;

                case AMOUNT:
                    elmaDetails.setAmount(columnValue);
                    break;

                case PAY_DETAILS:
                    elmaDetails.setPaymentDetails(columnValue);
                    paymentDetails = columnValue;
                    break;

                case DATE:
                    elmaDetails.setDateCreated(columnValue);
                    break;

                case TRANS_REF:
                    elmaDetails.setTransactionReference(columnValue);
                    break;

                case STATUS:
                    elmaDetails.setStatus(columnValue);
                    break;

                default:
                    break;
            }

            i++;
        }
        //CenteServiceType serviceType = retrieveServiceType(paymentDetails);
        //elmaDetails.setCenteServiceType(serviceType);

        String allFileColumns = GeneralUtils.convertToJson(allFileColumnsMap, stringMapType);
        elmaDetails.setAllFileColumns(allFileColumns);

        elmaDetails.setReconTransID(reconTransId);
        elmaDetails.setReportDetails(reportDetails);

        return elmaDetails;
    }

    @Override
    public int processLinkFileRecords(ReportDetails reportFileDetails, StatelessSession tempSession) throws MyCustomException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     *
     * @param recordToUpdate
     * @param updatingRecord
     * @param reportFileDetails
     * @return
     */
    private ReconTransactionsTable updateDbRecordHelper(ReconTransactionsTable recordToUpdate, ReconTransactionsTable updatingRecord, ReportDetails reportFileDetails) {

        String fileID = reportFileDetails.getFileID();
        LinkType linkType = reportFileDetails.getLinkType();

        String dbTransStatuses = recordToUpdate.getTransactionStatuses(); //pick existing statuses from DB
        Map<String, String> existingStatusesFromDB = GeneralUtils.convertFromJson(dbTransStatuses, stringMapType);

        String fileRowDetailsFromDB = recordToUpdate.getFileRecordsDetails();
        Map<String, Map<String, String>> dbRowDetails = GeneralUtils.convertFromJson(fileRowDetailsFromDB, mapInMapType);

        String nonDBTransStatuses = updatingRecord.getTransactionStatuses();
        Map<String, String> nonDBStatusesMap = GeneralUtils.convertFromJson(nonDBTransStatuses, stringMapType);
        String nonDBStatusValue = nonDBStatusesMap.get(fileID);

        String fileRowDetails = updatingRecord.getFileRecordsDetails();
        Map<String, Map<String, String>> nonDbRowDetails = GeneralUtils.convertFromJson(fileRowDetails, mapInMapType);

        /*String enumStringStatus;
        
         if(linkType == LinkType.LINKED){ //status is picked from a DB table already assigned - no need to assign it again
         enumStringStatus = nonDBStatusValue;
         }else{
         enumStringStatus = GeneralUtils.assignStatus(nonDBStatusValue, IDValue, amountValue, reportFileDetails);
         }*/
        existingStatusesFromDB.put(fileID, nonDBStatusValue);//add another json element

        String updatedTransStatusJson = GeneralUtils.convertToJson(existingStatusesFromDB, stringMapType);

        //set transaction statuses in DB
        recordToUpdate.setTransactionStatuses(updatedTransStatusJson);

        Map<String, String> fileRecordToAdd = nonDbRowDetails.get(fileID);
        dbRowDetails.put(fileID, fileRecordToAdd);

        String updatedFileRowDetails = GeneralUtils.convertToJson(dbRowDetails, mapInMapType);
        recordToUpdate.setFileRecordsDetails(updatedFileRowDetails);

        //===>> 
        //think about this logic, when reading master file, Get all ids in DB and store them in temp map
        //get a union of IDs in this map and those in the master file map
        //loop through this union setting all appropriate fields
        //this will help us to update existing IDs and also insert all fields for IDs that only exist in master
        //e.g. comments field so that we dont have to keep setting comments for each file we reconcile as it is now
        //find a way to deal with null IDs in the DB so that we can use sets        
        //<<===
        //ID value has already been set by the general helper
        //status value has already been set by the general helper
        //add comments && isReconStatusStatusInAllFiles
        //get status from current object & set it on the dbObject
        //get master file details from current object & set it on the dbObject
        //get isRecordExistsInMaster details from current object & set it on the dbObject
        if (reportFileDetails.isIsMaster()) {

            //Collection<String> statuses = existingStatusesFromDB.values();       
            //List<String> statuses = new ArrayList<>(existingStatusesFromDB.values());
            /*logger.debug("status values size: " + existingStatusesFromDB.values().size());

             Set<String> statuses = new HashSet<>(existingStatusesFromDB.values()); //remove any duplicate statuses

             logger.debug("status set size: " + statuses.size());

             ReconFileStatuses isReconStatusStatusInAllFiles;

             if (statuses.size() == 1) { //uniform status for all reconciled files
             isReconStatusStatusInAllFiles = ReconFileStatuses.OK;
             } else {
             isReconStatusStatusInAllFiles = ReconFileStatuses.NOK;//some files with OK and some NOK
             }

             //set comments && isReconStatusStatusInAllFiles
             setRemainingMasterFieldsHelper(recordToUpdate, isReconStatusStatusInAllFiles);
             */
            //get master file details from current object & add it to the dbObject
            //get isRecordOkInMaster details from current object && add it to the dbObject
            recordToUpdate.setIsRecordExistinMasterFile(updatingRecord.isIsRecordExistinMasterFile());

        }

        return recordToUpdate;
    }

    private ReconTransactionsTable buildReconTransRowFromDB(Linked linkedFileRow) {

        ReconTransactionsTable reconTransRow = new ReconTransactionsTable();

        String specialIDValue = linkedFileRow.getSpecialIDValue();
        String IDValue = linkedFileRow.getIDValue();
        String linkIDValue = linkedFileRow.getLinkIDValue();

        String amount = linkedFileRow.getAmount();
        String transactionDescription = linkedFileRow.getTransactionDescription();
        String reportFileKeyID = linkedFileRow.getReportFileKeyID();
        String reconGroupID = linkedFileRow.getReconGroupID();
        String transactionStatuses = linkedFileRow.getTransactionStatuses();
        String tempTransStatusHolder = linkedFileRow.getTempTransStatusHolder();
        String comments = linkedFileRow.getComments();
        String fileRecordsDetails = linkedFileRow.getFileRecordsDetails();
        boolean isMarkedAsException = linkedFileRow.isIsMarkedAsException();
        boolean isRecordExistinMasterFile = linkedFileRow.isIsRecordExistinMasterFile();
        CenteServiceType centeServiceType = linkedFileRow.getCenteServiceType();
        ArrayList<String> cellValues = linkedFileRow.getCellValues();

        reconTransRow.setSpecialIDValue(specialIDValue);
        reconTransRow.setIDValue(IDValue);
        reconTransRow.setLinkIDValue(linkIDValue);
        reconTransRow.setAmount(amount);
        reconTransRow.setTransactionDescription(transactionDescription);
        reconTransRow.setReportFileKeyID(reportFileKeyID);
        reconTransRow.setReconGroupID(reconGroupID);
        reconTransRow.setTransactionStatuses(transactionStatuses);
        reconTransRow.setTempTransStatusHolder(tempTransStatusHolder);
        reconTransRow.setComments(comments);
        reconTransRow.setFileRecordsDetails(fileRecordsDetails);
        reconTransRow.setIsMarkedAsException(isMarkedAsException);
        reconTransRow.setIsRecordExistinMasterFile(isRecordExistinMasterFile);
        reconTransRow.setCenteServiceType(centeServiceType);
        reconTransRow.setCellValues(cellValues);

        return reconTransRow;
    }

    /*private List<String> getCellValuesHelperOriginal(Iterator<Row> rowIterator) {

     //if this is the very first row with data in this report, then get all the headers
     //we will use them as the JSON Id-names for our JSON data
     //For each row, iterate through each columns
     Row row = rowIterator.next();
     Iterator<Cell> cellIterator = row.cellIterator();
     List<String> rowValues = new ArrayList<>();

     while (cellIterator.hasNext()) {

     Cell cell = cellIterator.next();

     //int index = cell.getColumnIndex();
     CellReference cellRef = new CellReference(row.getRowNum(), cell.getColumnIndex());

     //System.out.print(cellRef.formatAsString());
     //System.out.print(" - ");
     Object headerNameOrValue;

     switch (cell.getCellType()) {

     case Cell.CELL_TYPE_BOOLEAN:

     //booleanValue = cell.getBooleanCellValue();
     headerNameOrValue = cell.getBooleanCellValue();
     logger.debug(headerNameOrValue + "\t\t");

     break;

     case Cell.CELL_TYPE_NUMERIC:

     if (DateUtil.isCellDateFormatted(cell)) {
     //dateValue = cell.getDateCellValue();
     headerNameOrValue = cell.getDateCellValue();
     logger.debug(headerNameOrValue + "\t\t");

     } else {
     //numericValue = cell.getNumericCellValue();
     headerNameOrValue = cell.getNumericCellValue();
     logger.debug(headerNameOrValue + "\t\t");
     }

     break;
     case Cell.CELL_TYPE_STRING:

     //stringValue = cell.getStringCellValue();
     //stringValue = cell.getRichStringCellValue().getString();
     headerNameOrValue = cell.getRichStringCellValue().getString();
     logger.debug(headerNameOrValue + "\t\t");
     break;

     case Cell.CELL_TYPE_BLANK:

     headerNameOrValue = "";
     logger.debug(headerNameOrValue + "\t\t");
     break;

     case Cell.CELL_TYPE_ERROR:
     headerNameOrValue = "";
     logger.debug(headerNameOrValue + "\t\t");
     break;

     case Cell.CELL_TYPE_FORMULA:
     headerNameOrValue = cell.getCellFormula();
     logger.debug(headerNameOrValue + "\t\t");
     break;

     default:
     System.out.println();
     headerNameOrValue = "";
     logger.debug(headerNameOrValue + "\t\t");
     }
     String stringedValue = String.valueOf(headerNameOrValue);

     if (stringedValue == null || stringedValue.trim().isEmpty()) {
     logger.debug(">>>>> Got a Cell with null or empty value");
     stringedValue = "-"; //just put a place holder
     } else {
     logger.debug(">>>>> Got a valid value: " + stringedValue);
     }

     rowValues.add(String.valueOf(stringedValue));
     }
     System.out.println();

     return rowValues;
     }*/
    /*private void fetchingCells(Workbook workBook) {

     Sheet sheet = workBook.getSheetAt(WORKSHEET_INDEX);

     //Decide which rows to process
     int rowStart = Math.min(15, sheet.getFirstRowNum());
     int rowEnd = Math.max(1400, sheet.getLastRowNum());

     for (int rowNum = rowStart; rowNum < rowEnd; rowNum++) {
     Row r = sheet.getRow(rowNum);

     int lastColumn = Math.max(r.getLastCellNum(), MY_MINIMUM_COLUMN_COUNT);

     for (int cellNum = 0; cellNum < lastColumn; cellNum++) {
     Cell c = r.getCell(cellNum, Row.RETURN_BLANK_AS_NULL);
     if (c == null) {
     // The spreadsheet is empty in this cell
     } else {
     // Do something useful with the cell's contents
     }
     }
     }
     }*/
    /*private ReconTransactionsTable addOtherFileFieldsHelper(List<String> rowHeaderNames, List<String> rowHeaderValues, ReconTransactionsTable reconTransTableRow, ReportDetails reportFileDetails) {

     String statusColumnName = reportFileDetails.getStatusColumnName();
     String IDcolumnName = reportFileDetails.getIDColumnName();

     String IDColumnValue = null;
     String nonDBTransStatuses = null;

     int i = 0;
     for (String rowHeaderName : rowHeaderNames) {

     if (rowHeaderName.equalsIgnoreCase(IDcolumnName)) {
     IDColumnValue = rowHeaderValues.get(i);
     } else if (rowHeaderName.equalsIgnoreCase(statusColumnName)) {
     nonDBTransStatuses = rowHeaderValues.get(i);
     }
     i++;
     }

     if (IDColumnValue == null || nonDBTransStatuses == null) {
     //cell without an ID or status values - how do we treat this? all the rest of the fields are there but the id field
     //throw new MyCustomException(d, ErrorCode.LOGIN_ERR, d, ErrorCategory.SERVER_ERR_TYPE);
     }

     reconTransTableRow.setIDValue(IDColumnValue);

     List<ReconTransactionsTable> reconTransTableFromDBList;
     reconTransTableFromDBList = DBManager.getRecordsEqualToPropertyValue(ReconTransactionsTable.class, "IDValue", IDColumnValue); //IDValue shouldn't be hardcoded

     Map<String, String> txnStatusMapFromDB;
     if (reconTransTableFromDBList.size() > 0) {

     ReconTransactionsTable reconTranTableRowFromDB = reconTransTableFromDBList.get(0); //if more than one, this is bad - means the value is not unique
     String dbTransStatuses = reconTranTableRowFromDB.getTransactionStatuses();

     txnStatusMapFromDB = GeneralUtils.convertFromJson(dbTransStatuses, stringMapType);
     } else {
     txnStatusMapFromDB = new HashMap<>();
     }

     String status;

     if (nonDBTransStatuses.equalsIgnoreCase(reportFileDetails.getSuccessStatusValue())) {
     status = ReconFileStatuses.OK.getValue();
     } else {
     status = ReconFileStatuses.NOK.getValue();
     }

     txnStatusMapFromDB.put(reportFileDetails.getFileID(), status);
     String updatedTransStatusJson = GeneralUtils.convertToJson(txnStatusMapFromDB, stringMapType);

     reconTransTableRow.setTransactionStatuses(updatedTransStatusJson);

     //create some JSON here
     return reconTransTableRow;
     }*/

    /*private void setMasterFileFieldsHelper(List<String> rowHeaderNames, List<String> rowHeaderValues, ReconTransactionsTable reconTransTableRow, ReportDetails reportFileDetails) {

     Map<String, String> fileRecordsDetailsMap = new HashMap<>();

     for (int i = 0; i < rowHeaderNames.size(); i++) {
     logger.debug("adding record: " + rowHeaderNames.get(i) + " <> " + rowHeaderValues.get(i));
     fileRecordsDetailsMap.put(rowHeaderNames.get(i), rowHeaderValues.get(i));
     }

     String fileRecordsDetails = GeneralUtils.convertToJson(fileRecordsDetailsMap, stringMapType);
     reconTransTableRow.setMasterFileRecords(fileRecordsDetails);

     String reconIdValue = reconTransTableRow.getIDValue(); //this field value is set when adding the otherFields Values
     List<ReconTransactionsTable> reconTransTableFromDBList;
     reconTransTableFromDBList = DBManager.getRecordsEqualToPropertyValue(ReconTransactionsTable.class, "IDValue", reconIdValue); //reconIDValue shouldn't be hardcoded

     Map<String, String> txnStatusMapFromDB;
     if (reconTransTableFromDBList.size() > 0) {

     ReconTransactionsTable reconTranTableRowFromDB = reconTransTableFromDBList.get(0); //if more than one, this is bad - means the value is not unique
     String dbTransStatuses = reconTranTableRowFromDB.getTransactionStatuses();

     txnStatusMapFromDB = GeneralUtils.convertFromJson(dbTransStatuses, stringMapType);
     } else {
     txnStatusMapFromDB = new HashMap<>();
     }

     //Collection<String> statuses = existingStatusesFromDB.values();       
     //List<String> statuses = new ArrayList<>(existingStatusesFromDB.values());
     logger.debug("status values size: " + txnStatusMapFromDB.values().size());
     Set<String> statuses = new HashSet<>(txnStatusMapFromDB.values()); //remove any duplicate statuses
     logger.debug("status set size: " + statuses.size());
     if (statuses.size() > 1) { //some files with OK and some NOK
     reconTransTableRow.setIsMarkedAsException(false);
     }

     String fileID = reportFileDetails.getFileID();
     String masterFileRecordStatus = txnStatusMapFromDB.get(fileID);

     ReconFileStatuses reconStatus;
     if (masterFileRecordStatus == null || masterFileRecordStatus.isEmpty()) {
     reconStatus = ReconFileStatuses.NOK;
     } else {
     reconStatus = ReconFileStatuses.convertToEnum(masterFileRecordStatus);
     }

     //setRemainingMasterFieldsHelper(reconTransTableRow, reconStatus);
     }*/

    /*private ReconTransactionsTable setRemainingMassterFieldsHelper(ReconTransactionsTable reconTransTableRow, ReconFileStatuses reconStatus) {

     String comments;
     switch (reconStatus) {

     case OK:
     reconTransTableRow.setIsMarkedAsException(Boolean.TRUE); //record exists with similar status in all reconciled files
     comments = "All files have matching status";
     break;
     case NOK:
     reconTransTableRow.setIsMarkedAsException(Boolean.FALSE);
     comments = "Found mismatching file status in some files";
     break;

     default:
     reconTransTableRow.setIsMarkedAsException(Boolean.FALSE);
     comments = "Failed to understanding the statuses in some files";
     break;
     }

     reconTransTableRow.setComments(comments);

     return reconTransTableRow;
     }*/
    /*void addMasterFileFields(boolean isMasterIDvalueExists, List<String> rowHeaderNames, List<String> rowHeaderValues, ReportDetails reportFileDetails, ReconTransactionsTable nonDBreconTransObject, ReconTransactionsTable dbReconTransObject) {

     Map<String, String> masterRecordsMap = new HashMap<>();

     for (int x = 0; x < rowHeaderNames.size(); x++) {
     logger.debug("adding record: " + rowHeaderNames.get(x) + " <> " + rowHeaderValues.get(x));
     masterRecordsMap.put(rowHeaderNames.get(x), rowHeaderValues.get(x));
     }

     //set master file records
     String masterFileRecords = GeneralUtils.convertToJson(masterRecordsMap, stringMapType);
     nonDBreconTransObject.setFileRecordsDetails(masterFileRecords);

     //set isExistsInMaster
     //ID value has already been set by the general helper
     //status value has already been set by the general helper
     if (isMasterIDvalueExists) { //record with this ID exists in DB - update

     //add comments
     //add existsInAllFiles - 
     //get status from current object & set it on the dbObject
     } else { //record with this ID doesn't exist in DB - just insert 

     //add comments
     //add existsInAllFiles - false
     //reconTransTableRow.setIsRecordOKinAllFiles(Boolean.FALSE);
     }

     //set isRecordOk in master
     //reconTransTableRow.setIsRecordOKinMasterFile(true);
     //nonDBreconTransObject.setIsRecordSameStatusinAllFiles(false); //default - record is NOK - works if record doesn't exist in DB i.e. only exists in master file
     //nonDBreconTransObject = setRemainingMasterFieldsHelper(nonDBreconTransObject, ReconFileStatuses.NOK); //if isRecordOk == false, then automatically this is NOK
     logger.debug("master fields: " + nonDBreconTransObject.getFileRecordsDetails());

     }*/
}
