/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon;

import com.google.gson.reflect.TypeToken;
import com.namaraka.recon.constants.DateTimeZones;
import com.namaraka.recon.constants.ReconStatus;
import com.namaraka.recon.constants.ReconProgressJsonKeys;
import com.namaraka.recon.exceptiontype.MyCustomException;
import com.namaraka.recon.feedback.ReconProgress;
import com.namaraka.recon.feedback.ReconProgressWrapper;
import com.namaraka.recon.model.v1_0.ReconciliationDetails;
import com.namaraka.recon.model.v1_0.ReportDetails;
import com.namaraka.recon.utilities.ExceptionsFile;
import com.namaraka.recon.utilities.GeneralUtils;
import com.namaraka.recon.utilities.GlobalAttributes;
import com.namaraka.recon.utilities.StartEndTime;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author smallgod
 */
public class Progress extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(Progress.class);
    private static final long serialVersionUID = -3634921260574361429L;
    private static final Type stringMapType = new TypeToken<Map<String, Object>>() {
    }.getType();

    private static final int READ_PERCENTAGE = 95;
    private static final int WRITE_PERCENTAGE = 5;
    private static final int COMPLETED_PERCENTAGE = READ_PERCENTAGE + WRITE_PERCENTAGE;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");

        //String recongroupid = request.getParameter("reconid"); //5544
        String recongroupid = "";

        StringBuilder sb = new StringBuilder();
        String s;
        while ((s = request.getReader().readLine()) != null) {
            sb.append(s);
        }

        String jsonStringRequest = sb.toString();

        logger.debug(GeneralUtils.toPrettyJsonFormat(jsonStringRequest));

        Map<String, String> jsonDetails = GeneralUtils.convertFromJson(jsonStringRequest, stringMapType);

        for (String jsonKey : jsonDetails.keySet()) {

            String jsonValue = jsonDetails.get(jsonKey);
            logger.debug("key - " + jsonKey + " : " + "value - " + jsonValue);

            ReconProgressJsonKeys reconProgressEnum = ReconProgressJsonKeys.convertToEnum(jsonKey);

            switch (reconProgressEnum) {

                case RECON_GROUP_ID:
                    recongroupid = jsonValue;
                    break;

                default:
                    logger.warn("unknown recon progress jsonKey: " + jsonKey);
            }
        }

        if (recongroupid == null || recongroupid.isEmpty()) {
            logger.error("ReconGroup ID is NULL or Empty");
            return;
            //throw new Mycustom
        }

        
        ReconciliationDetails reconDetails = GlobalAttributes.reconDetailsStore.get(recongroupid);
        String totalNumOfFilesInRecon = String.valueOf(GlobalAttributes.numberOfFilesInRecon.get(recongroupid));
                
        int totalRecordsInThisRecon = GlobalAttributes.totalRecordsToBeRead.get(recongroupid).get();
        int recordsSoFarRead = GlobalAttributes.fileReadProgressIndicator.get(recongroupid).get();        
        
        int recordsSoFarWritten = GlobalAttributes.fileWriteProgressIndicator.get(recongroupid).get();
        int totalReconciledRecordsInDB = GlobalAttributes.totalReconciledToBeWritten.get(recongroupid).get();
        //int actualTotalProcessed = GlobalAttributes.totalUnreconciledRecords.get(recongroupid).get();
        int exceptionsCount = GlobalAttributes.exceptionsCount.get(recongroupid).get();
        StartEndTime startEndTime = GlobalAttributes.totalReconTimeTracker.get(recongroupid);

        DateTime startTime = null;
        String timeStarted = null;
        
        if (startEndTime != null) {
            
            startTime = startEndTime.getStartTime();
            timeStarted = GeneralUtils.formatDateTime(startTime, DateTimeZones.KAMPALA.getValue(), "dd-MM-yyyy HH:mm:ss");
        }

        logger.info("START TIME :::: " + startTime);

        String timeEnded;

        int exceptionRate;
        int fileReadPercentProgress;
        int fileWritePercentProgress;
        int totalProgress;
        float roundProgress;

        if (totalRecordsInThisRecon <= 0) {
            logger.error("total Rrecods sum cannot be zero or less");
            return;
        }

        try {

            fileReadPercentProgress = (recordsSoFarRead * READ_PERCENTAGE) / totalRecordsInThisRecon;

            if (totalReconciledRecordsInDB == 0) {
                fileWritePercentProgress = 0;
                exceptionRate = 0;
            } else {
                fileWritePercentProgress = (recordsSoFarWritten * WRITE_PERCENTAGE) / totalReconciledRecordsInDB;
                exceptionRate = (exceptionsCount * 100) / totalReconciledRecordsInDB; //we only start counting exceptions when writing to files
                
                logger.info("fileWritePercentProgress   :: " + fileWritePercentProgress);
                logger.info("recordsSoFarWritten        :: " + recordsSoFarWritten);
                logger.info("totalReconciledRecordsInDB :: " + totalReconciledRecordsInDB);
            
            }

            logger.info("RecordsProcessedProgress :: " + fileReadPercentProgress);
            logger.info("FileWrite Progress       :: " + fileWritePercentProgress);
            
            totalProgress = fileReadPercentProgress   + fileWritePercentProgress;
            
            logger.info("totalProgress            :: " + totalProgress);

            roundProgress = Math.round(totalProgress);

        } catch (ArithmeticException ex) {
            logger.error("arithmetic exception trying to get percentage progress: " + ex.getMessage());
            return;
        }

        //{{"exception_files":{"iscalling":true, "basic_recon":{"exceptionsonly":"filename", "alltxns":"filename"}, "call_recon":{"222-224":"filename", "224-222":"filename", "221-244":"filename", "244-221":"filename"}}}}
        ReconProgressWrapper reconWrapper = new ReconProgressWrapper();
        ReconProgress reconProgress = new ReconProgress();

        Map<String, Object> exceptionFiles = new HashMap<>();

        reconProgress.setRecordsProcessed(String.valueOf(totalRecordsInThisRecon));
        reconProgress.setNumOfFiles(totalNumOfFilesInRecon);
        reconProgress.setPercentage(String.valueOf(totalProgress));

        //reconProgress.setFinalReconFileName(allRecordsPathFromFolder);
        //reconProgress.setExceptionsFilePathName(exceptionsPathFromFolder);
        reconProgress.setTimeStarted(timeStarted);
        reconProgress.setTestString("rounded progress: " + roundProgress + ", compound-processed: " + recordsSoFarRead + ", compounded: " + totalRecordsInThisRecon + ", written: " + recordsSoFarWritten + ", recordsInDB: " + totalReconciledRecordsInDB);

        logger.info(">>>>>>>>>>>>> start TIME <<<<<<<<<<<<<< " + startTime);

        if (totalProgress == COMPLETED_PERCENTAGE) {

            DateTime endTime = null;
            if (startEndTime != null) {
                endTime = startEndTime.getEndTime();
            }

            logger.info("End TIME :::: " + endTime);

            timeEnded = GeneralUtils.formatDateTime(endTime, DateTimeZones.KAMPALA.getValue(), "dd-MM-yyyy HH:mm:ss");

            reconProgress.setStatus(ReconStatus.COMPLETED.getValue());
            reconProgress.setTimeEnded(timeEnded);
            reconProgress.setTimeTaken(GeneralUtils.timeTakenToNow(startTime, endTime));
            reconProgress.setTotalFinalRecords(String.valueOf(recordsSoFarWritten));
            reconProgress.setExceptionRate(exceptionRate + "%");
            reconProgress.setExceptionCount(String.valueOf(exceptionsCount));

            if (reconDetails.isIsCalling()) {

                Map<String, String> callRecon = new HashMap<>();
                List<ExceptionsFile> exceptionFileList = GlobalAttributes.exceptionsFilesDetails.get(recongroupid);
                
                for (ExceptionsFile exceptionFile : exceptionFileList) {

                    String fileID = exceptionFile.getFileID();

                    ReportDetails reportDetails = exceptionFile.getReportDetails();
                    String filePath = reportDetails.getExceptionsFilePath();
                    String pathFromFolder = filePath.substring(filePath.lastIndexOf('/') + 1);

                    callRecon.put(fileID, pathFromFolder);
                }
                exceptionFiles.put("iscalling", Boolean.TRUE);
                exceptionFiles.put("call_recon", callRecon);

            } else {

                String allRecordsFilePath = GlobalAttributes.allRecordsAbsFinalFilePath;
                String exceptionsFilePath = GlobalAttributes.OnlyExceptionsAbsFinalFilePath;

                String allPathFromFolder = allRecordsFilePath.substring(allRecordsFilePath.lastIndexOf('/') + 1);
                String exceptionsPathFromFolder = exceptionsFilePath.substring(exceptionsFilePath.lastIndexOf('/') + 1);

                Map<String, String> basicRecon = new HashMap<>();
                basicRecon.put("allfilepath", allPathFromFolder);
                basicRecon.put("exceptionsfilepath", exceptionsPathFromFolder);

                exceptionFiles.put("iscalling", Boolean.FALSE);
                exceptionFiles.put("basic_recon", basicRecon);

            }

            reconProgress.setExceptionFiles(exceptionFiles);

        } else {
            reconProgress.setStatus(ReconStatus.INPROGRESS.getValue());
            reconProgress.setTimeTaken(GeneralUtils.timeTakenToNow(startTime));
            reconProgress.setTimeEnded("n/a");
            reconProgress.setTotalFinalRecords("n/a");
            reconProgress.setExceptionRate("n/a");
            reconProgress.setExceptionCount("n/a");
        }

        logger.info("We GOT herererererererre>>>>>>>>>>>>>>>>");

        reconWrapper.setReconProgress(reconProgress);

        final String jsonResponse = GeneralUtils.convertToJson(reconWrapper, ReconProgressWrapper.class);

        try {
            GeneralUtils.writeResponse(response, jsonResponse);
        } catch (MyCustomException ex) {
            logger.error("error sending progress indicator response: " + ex.getErrorDetails());
        }

        //for debugging purpose      
        if (recordsSoFarWritten == totalReconciledRecordsInDB) {

            if (COMPLETED_PERCENTAGE != 100) {
                logger.error("Something wrong happenend - all records written but percentage not 100%");
            } else {
                logger.info("we are GOOOOOD!!");
            }
        }

//        boolean test1 = Boolean.FALSE, test2 = Boolean.FALSE;
//        if(recordsSoFarWritten == totalReconciledToBeWritten){
//            test1 = Boolean.TRUE;
//        }
//        if(COMPLETED_PERCENTAGE == 100){
//            test2 = Boolean.TRUE;
//        }
//        
//        if((test1 & test2) == Boolean.TRUE){
//            logger.info("we are GOOOOOD!!");
//        }else{
//            logger.error("Something wrong happenend - all records written but percentage not 100%");
//        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
