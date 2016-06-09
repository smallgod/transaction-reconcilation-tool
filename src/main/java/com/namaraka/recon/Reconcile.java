/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.namaraka.recon.IF.FileProcessingObserver;
import com.namaraka.recon.constants.ErrorCategory;
import com.namaraka.recon.constants.ErrorCode;
import com.namaraka.recon.constants.ReconStatus;
import com.namaraka.recon.constants.ReconcileCmd;
import com.namaraka.recon.constants.StartReconJsonKeys;
import com.namaraka.recon.dbaccess.DBManager;
import com.namaraka.recon.exceptiontype.MyCustomException;
import com.namaraka.recon.model.v1_0.ReconciliationDetails;
import com.namaraka.recon.utilities.GeneralUtils;
import com.namaraka.recon.utilities.GlobalAttributes;
import com.namaraka.recon.utilities.WriteFileTask;
import com.namaraka.recon.utilities.WriteFileTaskOLD;
import com.namaraka.recon.utilities.WriteMutexClass;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FileProcessingObserved
 *
 * @author smallgod
 */
public class Reconcile extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(Reconcile.class);
    private static final long serialVersionUID = 6878402116788085872L;

    private static final Type stringMapType = new TypeToken<Map<String, Object>>() {
    }.getType();
    private static final Type stringArrayType = new TypeToken<String[]>() {
    }.getType();
    private static final Type collectionType = new TypeToken<Collection<String>>() {
    }.getType();

    private List<FileProcessingObserver> observers;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try {

            //String callingFilesList = "{\"200-201\":\"\"}";//The ideal solution is this --->>>  {"callingfiles":{"212-213":"", "214-215":""}    //these are file IDs
            String reconGroupID = "";
            String recontitle = "";
            ReconcileCmd action = ReconcileCmd.START;
            boolean isCalling = false;
            List<String> callingFilesList = null;
            String callingFilesJson = "";

            final String fileDetailsJsonString = GeneralUtils.getJsonStringFromRequest(request);

            logger.info(GeneralUtils.toPrettyJsonFormat(fileDetailsJsonString));

            Map<String, Object> fileDetails;
            fileDetails = GeneralUtils.convertFromJson(fileDetailsJsonString, stringMapType);

            for (String jsonKey : fileDetails.keySet()) {

                Object jsonValue = fileDetails.get(jsonKey);

                logger.info("key - " + jsonKey + " : " + "value - " + jsonValue);

                StartReconJsonKeys startReconEnum = StartReconJsonKeys.convertToEnum(jsonKey);

                switch (startReconEnum) {

                    case RECON_GROUP_ID:
                        reconGroupID = (String) jsonValue;
                        break;

                    case REPORT_TITLE:
                        recontitle = (String) jsonValue;
                        break;

                    case ACTION:
                        action = ReconcileCmd.valueOf((String) jsonValue);
                        break;

                    case IS_CALLING:

                        isCalling = Boolean.valueOf((String) jsonValue);
                        break;

                    case CALLING_FILES:

                        callingFilesList = (ArrayList<String>) jsonValue;  //json got like so - {"200", "201"}
                        callingFilesJson = GeneralUtils.convertToJson(jsonValue, collectionType);

                        break;

                    default:
                        logger.warn("unknown fileDetails jsonKey: " + jsonKey);
                }
            }

            if (action == ReconcileCmd.START) {

                if (reconGroupID == null || reconGroupID.isEmpty()) {
                    logger.error("ReconGroup ID is NULL or Empty");
                    return;
                    //throw new Mycustom
                }

                //for synchronisation, ideally this guys should only be initialised once
                GlobalAttributes.writeMutexObjects.put(reconGroupID, new WriteMutexClass(reconGroupID));

                GeneralUtils.initGlobalMap(GlobalAttributes.fileWriteProgressIndicator, reconGroupID);
                GeneralUtils.initGlobalMap(GlobalAttributes.totalReconciledToBeWritten, reconGroupID);
                GeneralUtils.initGlobalMap(GlobalAttributes.exceptionsCount, reconGroupID);

                //start/end time
                //total exceptions
                //total records
                //exception rate
                //total time taken to reconcile
                //set START-Time
                GlobalAttributes.setReconTimeTracker(Boolean.TRUE, reconGroupID, GlobalAttributes.totalReconTimeTracker);
                //final int numOfFiles = GlobalAttributes.numberOfFilesInRecon.get(reconGroupID).get();
                //final int numOfFilesDONE = GlobalAttributes.numberOfFilesInReconDONE.get(reconGroupID).get();      

                //save it now before entering long blocking sync block
                //NewReconStarted newReconStarted = new NewReconStarted();
                //register observers here
                //get ReportDetails list from memory for registration here
                try (PrintWriter out = response.getWriter()) {

                    out.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><ns2:oliwakabiboyi version=\"1.0\" xmlns:ns2=\"http://reconpro.namara.com\"><obubaka>Kyikyi Tooto? oli steady papa!!</obubaka></ns2:oliwakabiboyi>");
                    out.close();
                }

                Thread.sleep(3000L); //just wait a little bit to notify observers so that they can get done with what they are doing

                //calling this method will notify all observers that it is okay to write if reading ALL files is complete
                ReconciliationDetails reconDetails = GlobalAttributes.newReconStarted.saveNewRecon(reconGroupID, callingFilesJson);
                GlobalAttributes.reconDetailsStore.put(reconGroupID, reconDetails);

                // Let's NOT write here, the observers will write
                //writeFiles(reconDetails);
                return;

            } else if (action == ReconcileCmd.CANCEL) {

                return;
            }
            
            
            
            
            

            if (reconGroupID == null || reconGroupID.isEmpty()) {
                logger.error("ReconGroup ID is NULL or Empty");
                return;
                //throw new Mycustom
            }

            List<?> reconDetailsList = DBManager.getRecordsEqualToPropertyValue(ReconciliationDetails.class, "reconGroupID", reconGroupID);

            try (PrintWriter out = response.getWriter()) {

                if (reconDetailsList.isEmpty()) {
                    out.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><ns2:oliwakabiboyi version=\"1.0\" xmlns:ns2=\"http://reconpro.namara.com\"><obubaka>oli bikyi boyiii, man eyooo recon teliiyooooo</obubaka></ns2:oliwakabiboyi>");
                    out.close();

                    logger.error("No such Reconciliation group exists - provide valid reconID");
                    throw new MyCustomException("Failed to start Recon", ErrorCode.BAD_REQUEST_ERR, "Reconciliation cannot start - failed to find matching recon for ID: " + reconGroupID, ErrorCategory.CLIENT_ERR_TYPE);

                } else {

                    out.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><ns2:oliwakabiboyi version=\"1.0\" xmlns:ns2=\"http://reconpro.namara.com\"><obubaka>Kyikyi Tooto? oli steady papa!!</obubaka></ns2:oliwakabiboyi>");
                }

                out.close();
            }

            //ReconcileRequest reconcileRequest = new ReconcileRequest(action, reconGroupID, isCalling);
            final ReconciliationDetails reconDetails = (ReconciliationDetails) reconDetailsList.get(0);

            //reconDetails.setCallingFiles(callingFilesList);
            reconDetails.setIsCalling(isCalling);
            reconDetails.setAction(action);

            DBManager.updateDatabaseModel(reconDetails);

            GlobalAttributes.reconDetailsStore.put(reconGroupID, reconDetails);

            try {

                if (action == ReconcileCmd.START) {

                    ReconStatus reconProgress = reconDetails.getReconStatus();

                    if (reconProgress == ReconStatus.NEW) { // progress MUST have not been set - should be NEW then we update it to INVOKED

                        //reconDetails.setNoOfFilesInReconGroup(numOfFiles); //set the number of files in this recongroup
                        //updateReconProgress(reconDetails, ReconStatus.INVOKED);
                        logger.info("updated recon progress to INVOKED>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ::: " + reconDetails.getReconStatus());

                        //GlobalAttributes.setNewValue(reconGroupID, numOfFiles, GlobalAttributes.numberOfFilesInRecon);
                        //GlobalAttributes.setReconTimeTracker(Boolean.TRUE, reconGroupID, GlobalAttributes.totalReconTimeTracker);
                    } else {
                        logger.warn("Recon with ID: " + reconGroupID + " Can't START - current state is either already COMPLETED or in NON-suitable status to start");
                        return;
                    }

                    logger.info(">>>>>>>>>>>>>>>>>>>>> About to call writeFileTask to execute <<<<<<<<<<<<<<<<<<<<<");
                    //Runnable writeFileTask = new WriteFileTaskOLD(reconDetails, Boolean.TRUE);
                    //GeneralUtils.executeTask(writeFileTask); //no need to synchronize here 
                    
                    Runnable writeFileTask = new WriteFileTask(reconDetails);
                    GeneralUtils.executeTask(writeFileTask); //no need to synchronize here 


                    logger.info(">>>>>>>>>>>>>>>>>>>>>  Going to reconcile LinkedFILE");
                    GeneralUtils.reconcileLinkedFile(reconGroupID); //make sure if there is a LINKED file in this report, it is reconciled

                } else if (action == ReconcileCmd.CANCEL) {

                    //InitApp.getFileUploadExecService().shutdownNow();
                    GeneralUtils.removeReconDetailsFromGlobalMaps(reconGroupID);
                }

            } catch (MyCustomException ex) {
                throw new ServletException("CustomException", ex);
            }

            if (response.isCommitted()) {
                logger.info("Response committed!!");
            } else {
                logger.info("Response NOT committed!!");
            }

        } catch (MyCustomException ex) {
            logger.error("An error is thrown here (string) :: " + ex.toString());
            logger.error("An error is thrown here          :: " + ex.getErrorDetails());
            throw new ServletException("CustomException", ex);
        } catch (JsonSyntaxException | NullPointerException | IOException | ServletException ex) {

            //organise the chaos here with handling exceptions
            //logger.error("error here: " + ex.getMessage());
            ex.printStackTrace();
            //throw new MyCustomException("NPE", ErrorCode.SERVER_ERR, "NullPointerException reading CSV file: " + ex.getMessage(), ErrorCategory.SERVER_ERR_TYPE);

        } catch (InterruptedException ex) {
            ex.printStackTrace();
            //throw new MyCustomException("NPE", ErrorCode.SERVER_ERR, "NullPointerException reading CSV file: " + ex.getMessage(), ErrorCategory.SERVER_ERR_TYPE);
        }
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
