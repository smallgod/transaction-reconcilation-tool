/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon.utilities;

import com.namaraka.recon.NewReconStarted;
import com.namaraka.recon.feedback.ReconProgress;
import com.namaraka.recon.model.v1_0.ReconciliationDetails;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.joda.time.DateTime;

/**
 *
 * @author smallgod
 */
public class GlobalAttributes {

    /*live params            ---- change these guys -------
    public static final String READ_DIR =       "/home/smallgod/NetBeansProjects/recontool/web/WEB-INF/reconfolder/"; //put this in the configs file
    public static final String SAVE_FINAL_DIR = "/home/smallgod/NetBeansProjects/recontool/web/WEB-INF/reconfolder/final_files/"; //put this in the configs file
    public static final String SAVE_DIR =       "/home/smallgod/NetBeansProjects/recontool/web/WEB-INF/reconfolder/final_files/"; //put this in the configs file
    public static final String TEMP_DIR =       "/srv/applications/namaraka/tempFiles/"; //put this in the configs file
     */
    //for test purposes
    /**/
    //public static final String SAVE_FINAL_DIR = "/home/smallgod/reconfolder/"; //put this in the configs file
    // public static final String READ_DIR =       "/home/smallgod/reconfolder/"; //put this in the configs file
    //public static final String SAVE_DIR =       "/home/smallgod/reconfolder/"; //put this in the configs file
    //public static final String TEMP_DIR =       "/home/smallgod/tempFiles/"; //put this in the configs file
    public static final int NUM_ROWS_CHUNK = 10000; //The batch size for number of records to be read at a time
    public static final int INDICATE_AT = 10; //The progress indicator moves every 10 records read
    public static final int HIBERNATE_JDBC_BATCH = 30; //same as the JDBC batch size in the hibernate config xml config file

    /**
     * Contains a mapping of the linkID to the ID value e.g. "Reciept Number" to
     * "Payer Txn ID"
     */
    public static Map<String, String> linkMap = new HashMap<>();

    //public static final String MUTEX = "MUTEX";
    public static final String READ_MUTEX = "READ_MUTEX";
    public static final String WRITE_MUTEX = "WRITE_MUTEX";
    public static final String OBSERVER_MUTEX = "OBSERVER_MUTEX";
    public static String allRecordsAbsFinalFilePath = "";
    public static String OnlyExceptionsAbsFinalFilePath = "";

    public static String exceptionsFilePathA = "";
    public static String exceptionsFilePathB = "";

    public static final String CELL_PAYERID_COL = "Payer Transaction ID";
    public static final String CELL_MNOID_COL = "Receipt Number";
    public static final String DESCRIPTION_COL = "description";

    public static final int WORKSHEET_INDEX = 0;
    public static final int FIRST_ROW_TO_GET = 0;
    public static int NUM_OF_HEADER_COLS;

    public static final char ORIGINAL_TRACER_NO_DELIMETER = '-';
    public static final int ID_BEGIN_INDEX = 0;
    public static final int ELMA_ID_BEGIN_INDEX = 4;

    public static final String FILE_EXT_XLS = ".xls"; //put thes in configs files for dynamism
    public static final String FILE_EXT_CSV = ".csv"; //put thes in configs files for dynamism

    //public static double currentNumberOfRecordsInDB = 0; //the records that have so far been written to the DB - this is also a performance determinant
    //public static double globalCompoundedTotalRecords = 0;
    //public static double globalTotalRecords = 0; //the global total of all records in all files
    //public static double progressIndicator = 0;
    //public static int totalNumberRecordsIterated = 0;
    //public static int totalNumRecordsInDB = 0;
    //public static double finalFileWriteProgressIndicator = 0;
    //public static Set<String> globalReconIDs = new HashSet<>();
    //public static Map<String, Map<ReconGlobalDetailKeys, Object>> globalReconDetails = new HashMap<>();
    public static final Map<String, Object> globalReconFileDetails = new HashMap<>();

    public static final Map<String, Object> linkerFileRead = new HashMap<>();
    public static final Map<String, Object> linkedFileRead = new HashMap<>();
    public static final Map<String, Object> linkedFileReconciled = new HashMap<>();

    public static final Map<String, StartEndTime> totalReconTimeTracker = new ConcurrentHashMap<>();
    public static final Map<String, ReconciliationDetails> reconDetailsStore = new ConcurrentHashMap<>();
    public static final Map<String, List<ExceptionsFile>> exceptionsFilesDetails = new ConcurrentHashMap<>();

    public static final ConcurrentHashMap<String, AtomicInteger> exceptionsCount = new ConcurrentHashMap<>();

    public static final ConcurrentHashMap<String, AtomicInteger> numberOfFilesInRecon = new ConcurrentHashMap<>();

    public static final ConcurrentHashMap<String, AtomicInteger> fileReadProgressIndicator = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, AtomicInteger> fileWriteProgressIndicator = new ConcurrentHashMap<>();

    public static final ConcurrentHashMap<String, AtomicInteger> totalRecordsToBeRead = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, AtomicInteger> totalReconciledToBeWritten = new ConcurrentHashMap<>();

    public static final Map<String, ReadMutexClass> readMutexObjects = new HashMap<>();
    public static final Map<String, WriteMutexClass> writeMutexObjects = new HashMap<>();

    //this will be observed by observers so that a Write recon can be called when file processing is DONE
    public static final NewReconStarted newReconStarted = new NewReconStarted();
    
    
    public static final Map<String, Object> reconProgress = new HashMap<>();
    public static final Map<String, Object> reconCompleted = new HashMap<>();
    

    //THE BELOW IS NOT BEING USED IN WORKING CODE - CAN DELETE, delete also references
    public static final ConcurrentHashMap<String, AtomicInteger> totalUnreconciledRecords = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, AtomicInteger> totalNumberRecordsIterated = new ConcurrentHashMap<>();

    public static void resetGlobalAttributes(String reconGroupID) {

        removeRecon(globalReconFileDetails, reconGroupID);
        removeRecon(linkerFileRead, reconGroupID);
        removeRecon(linkedFileRead, reconGroupID);
        removeRecon(linkedFileReconciled, reconGroupID);
        removeRecon(totalReconTimeTracker, reconGroupID);
        removeRecon(reconDetailsStore, reconGroupID);

        removeRecon(exceptionsFilesDetails, reconGroupID);
        removeRecon(exceptionsCount, reconGroupID);
        removeRecon(numberOfFilesInRecon, reconGroupID);

        removeRecon(fileReadProgressIndicator, reconGroupID);
        removeRecon(fileWriteProgressIndicator, reconGroupID);
        removeRecon(totalRecordsToBeRead, reconGroupID);

        removeRecon(totalReconciledToBeWritten, reconGroupID);
        removeRecon(readMutexObjects, reconGroupID);
        removeRecon(writeMutexObjects, reconGroupID);

        removeRecon(totalUnreconciledRecords, reconGroupID);
        removeRecon(totalNumberRecordsIterated, reconGroupID);

    }

    private static void removeRecon(Map<String, ?> collection, String reconGroupID) {

        try {
            if (collection != null) {
                if (collection.containsKey(reconGroupID)) {

                    collection.remove(reconGroupID);
                }
            }
        } catch (UnsupportedOperationException | ClassCastException | NullPointerException exception) {

            System.err.println("Error occurred while removing items from global map: " + exception.getMessage());
            exception.printStackTrace();
        }

    }

    /**
     *
     * @param reconGroupID
     * @return
     */
    public static final ReadMutexClass getReadMutex(String reconGroupID) {

        ReadMutexClass readMutex = GlobalAttributes.readMutexObjects.get(reconGroupID);
        if (readMutex == null) {
            readMutex = new ReadMutexClass(reconGroupID);
            GlobalAttributes.readMutexObjects.put(reconGroupID, readMutex);
        }
        return readMutex;
    }

    /**
     * Increment by given increment
     *
     * @param reconID
     * @param increment
     * @param hashMap
     * @return
     */
    public static int increment(String reconID, int increment, ConcurrentHashMap<String, AtomicInteger> hashMap) {

        AtomicInteger current = hashMap.get(reconID);

        if (current == null) {
            current = new AtomicInteger(0); //initialise map
            hashMap.put(reconID, current);
        }
        int value = current.addAndGet(increment);

        return value;
    }

    /**
     *
     * @param generatedID
     * @param hashMap
     * @return
     */
    public static int incrementAndGet(String generatedID, ConcurrentHashMap<String, AtomicInteger> hashMap) {

        AtomicInteger current = hashMap.get(generatedID);

        if (current == null) {
            current = new AtomicInteger(0);
            hashMap.put(generatedID, current);
        }
        int value = current.incrementAndGet();

        return value;
    }

    /**
     *
     * @param reconID
     * @param newValue
     * @param hashMap
     */
    public static void setNewValue(String reconID, int newValue, ConcurrentHashMap<String, AtomicInteger> hashMap) {

        AtomicInteger current = hashMap.get(reconID);

        if (current == null) {
            hashMap.put(reconID, new AtomicInteger(newValue));
        } else {
            current.set(newValue);
        }
    }

    /**
     * Set the new value in the map
     *
     * @param reconID
     * @param newValue
     * @param storageMap
     */
    public static void setNewValue(String reconID, Object newValue, Map<String, Object> storageMap) {

        storageMap.put(reconID, newValue);
    }

    /**
     * Sets both the start and end time of the reconciliation given by the
     * reconID
     *
     * @param isStartTime
     * @param reconID
     * @param storageMap
     */
    public static void setReconTimeTracker(boolean isStartTime, String reconID, Map<String, StartEndTime> storageMap) {

        DateTime timeToSet = new DateTime();
        StartEndTime startEndTime;

        if (isStartTime) {
            startEndTime = new StartEndTime();
            startEndTime.setStartTime(timeToSet);
        } else {
            startEndTime = storageMap.get(reconID);
            startEndTime.setEndTime(timeToSet);
        }

        storageMap.put(reconID, startEndTime);

    }

    /**
     * Increment by given increment
     *
     * @param reconID
     * @param increment
     * @param hashMap
     */
    public static void incrementOld(String reconID, int increment, ConcurrentHashMap<String, AtomicInteger> hashMap) {

        while (true) {
            AtomicInteger current = hashMap.get(reconID);
            int value = current.addAndGet(increment);
            if (value > 0) {
                break;
            }
        }
    }

    /**
     * Increment by default value - 1
     *
     * @param reconID
     * @param hashMap
     */
    public static void increment(String reconID, ConcurrentHashMap<String, AtomicInteger> hashMap) {

        while (true) {

            AtomicInteger current = hashMap.get(reconID);

            if (current == null) {
                hashMap.put(reconID, new AtomicInteger(0));
            } else {
                int value = current.incrementAndGet();
                if (value > 0) {
                    break;
                }
            }
        }
    }
}
