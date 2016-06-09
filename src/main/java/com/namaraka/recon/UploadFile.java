/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon;

import com.namaraka.recon.IF.TxnRecordIF;
import com.namaraka.recon.constants.LinkType;
import com.namaraka.recon.dbaccess.DBManager;
import com.namaraka.recon.exceptiontype.MyCustomException;
import com.namaraka.recon.feedback.ReconProgress;
import com.namaraka.recon.feedback.ReconProgressWrapper;
import com.namaraka.recon.model.v1_0.ReportDetails;
import com.namaraka.recon.utilities.CallBack;
import com.namaraka.recon.utilities.FileUtilities;
import com.namaraka.recon.utilities.GeneralUtils;
import com.namaraka.recon.utilities.ReadFileTask;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.hibernate.StatelessSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.Future;

/**
 *
 * @author smallgod
 */
@WebServlet(asyncSupported = true)
public class UploadFile extends HttpServlet implements CallBack {

    private static final Logger logger = LoggerFactory.getLogger(UploadFile.class);
    private static final long serialVersionUID = 6594216941764776653L;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        Transformer aTransformer;

        response.setContentType("application/json");
        //logRequestInfo(request);
        //printRequesterHeaderInfo(request);

        //String linkIdLabel = GlobalAttributes.CELL_MNOID_COL;//This is in the Cellulant report (Payer txn id) for linking Pull bank reports and mno repots that lack the bank report
        //String descriptionColumnName = GlobalAttributes.DESCRIPTION_COL; //This is in the Cellulant report (Payer txn id) for linking Pull bank reports and mno repots that lack the bank report
        ReconProgressWrapper reconWrapper = new ReconProgressWrapper();
        ReconProgress reconProgress = new ReconProgress();
        reconWrapper.setReconProgress(reconProgress); //set to default recon progress        

        try {

            final String jsonResponse = GeneralUtils.convertToJson(reconWrapper, ReconProgressWrapper.class);

            final String fileDetailsJsonString = GeneralUtils.getJsonStringFromRequest(request);
            GeneralUtils.writeResponse(response, jsonResponse);

            //final String fileDetailsJsonString = "{\"reportname\":\"" + reportTitle + "\",\"idlabel\":\"" + idLabel + "\",\"hasstatus\":\"" + hasStatus + "\",\"linkidlabel\":\"" + linkIdLabel + "\",\"amount\":\"" + amountLabel + "\",\"status\":\"" + status + "\",\"filename\":\"" + filename + "\",\"pending\":\"" + pending + "\",\"success\":\"" + success + "\",\"failed\":\"" + failed + "\",\"fileid\":\"" + fileID + "\",\"reconid\":\"" + recongroupid + "\",\"action\":\"" + action + "\",\"recontitle\":\"" + recontitle + "\",\"filepath\":\"" + filepath + "\",\"ismaster\":\"" + ismaster + "\",\"linktype\":\"" + linkType + "\",\"description\":\"" + descriptionColumnName + "\",\"reporttype\":\"" + reportType + "\"}";      
            ReportDetails reportFileDetails = GeneralUtils.recieveReportFileDetails(fileDetailsJsonString);

            Runnable readFileTask = new ReadFileTask(reportFileDetails);
            Future future = GeneralUtils.executeTask(readFileTask);
            //future.cancel(Boolean.TRUE);
            
        } catch (MyCustomException ex) {
            throw new ServletException("CustomException", ex);
        }
    }


    
    @Override
    public int processLinkFileRecords(ReportDetails reportFileDetails, StatelessSession tempSession) throws MyCustomException {

        LinkType linkedType = reportFileDetails.getLinkType();
        Sheet sheet = reportFileDetails.getSheet();

        Iterator<Row> rowIterator = FileUtilities.getWorkBookRowIteratorHelper(sheet);

        List<String> rowHeaderNames = null;
        List<String> rowHeaderValues;

        boolean firstRow = true;
        int count = 0;

        while (rowIterator.hasNext()) {

            if (firstRow) {

                rowHeaderNames = GeneralUtils.getCellValuesHelper(rowIterator);

                if (linkedType == LinkType.LINKED) {

                    ArrayList<String> headerNamesArrayList = GeneralUtils.convertListToArrayList(rowHeaderNames);
                    //reportFileDetails.setCellHeaderNames(headerNamesArrayList);

                    String fileHeaderMapString = GeneralUtils.getFileHeaderNamesHelper(rowHeaderNames);
                    reportFileDetails.setFileHeaderNames(fileHeaderMapString);
                    DBManager.updateDatabaseModel(reportFileDetails);
                }

                firstRow = false;
                continue; //after retrieving file  headers just proceed to getting values
            } else {
                rowHeaderValues = GeneralUtils.getCellValuesHelper(rowIterator);
            }

            TxnRecordIF reconTransRow = GeneralUtils.addFileFieldsHelper(rowHeaderNames, rowHeaderValues, reportFileDetails, tempSession);

            tempSession.insert(reconTransRow);

            count++;
        }
        return count;
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

    @Override
    public void execute(Object data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int readRecordsFromFile(ReportDetails reportFileDetails, StatelessSession tempSession, long currentNumberOfRecordsInDB) throws MyCustomException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int readRecordsFromDB(ReportDetails reportFileDetails, StatelessSession tempSession, long currentNumberOfRecordsInDB) throws MyCustomException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
