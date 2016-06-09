/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon.errorpage;
         
import com.namaraka.recon.exceptiontype.MyCustomException;
import com.namaraka.recon.model.v1_0.Errorresponse;
import com.namaraka.recon.utilities.BindXmlAndPojo;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author smallgod
 */
public class ErrorResponseHandler extends HttpServlet {

    private static final long serialVersionUID = 829659101704381997L;

    private static final Logger logger = LoggerFactory.getLogger(ErrorResponseHandler.class);
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processError(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        logger.debug("ErrorResponseHandler.java called!");
        
        response.setContentType("application/xml");
        response.setCharacterEncoding("UTF-8");

        Throwable throwable = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Class className     = (Class)     request.getAttribute(RequestDispatcher.ERROR_EXCEPTION_TYPE);//class name of the exception instance that caused the error (or null)
        String errorMessage = (String)    request.getAttribute(RequestDispatcher.ERROR_MESSAGE);//error message
        Integer statusCode  = (Integer)   request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);//status code of the error (e.g. 404, 500 etc.)
        String servletName  = (String)    request.getAttribute(RequestDispatcher.ERROR_SERVLET_NAME);//The Servlet name of the servlet that the errored request was dispatched to
        String requestUri   = (String)    request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);//URI of the errored request

        MyCustomException customException = (MyCustomException)throwable;
        //set the HTTP status code here
        //response.setStatus(customException.getHTTPStatusCode());
        Errorresponse errorResponse = customException.createErrorResponse();
        
        String xmlErrorResponse;
        
        try {
            xmlErrorResponse = BindXmlAndPojo.objectToXML(errorResponse, Errorresponse.class);
        } catch (MyCustomException ex) {
            //send a custom generic error, since we've failed to create the error
            logger.error("Error trying to get an XML error string to send to client: " + errorResponse.getError().getDetails());
            xmlErrorResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ns1:errorresponse version=\"1.0\" typename=\"errorresponse\" category=\"CLIENT_ERROR\" xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns:ns1='https://namaraka.com/bizlogic/model/v1_0'><clientrequestid></clientrequestid><namarakarefid></namarakarefid><error><code>FORMAT_ERROR</code><details>Error occurred during processing. And while trying to send back a response, another formatting error occurred. So cause of original error couldnot be delivered</details><additional><info>error</info><info>error</info></additional></error><extension></extension></ns1:errorresponse>";
        }
        
        logger.debug("xml error-response: " + xmlErrorResponse);
        
        //we need to cater for error scenario where we fail to cast exception or to marshal into the XML

        logger.debug("Throwable -getMessage      : " + throwable.getMessage());
        logger.debug("ClassName                  : " + className.getName());
        logger.debug("errorMessage               : " + errorMessage);
        logger.debug("statusCode                 : " + statusCode);
        logger.debug("servletName                : " + servletName);
        logger.debug("requestURI                 : " + requestUri);
        
        response.setStatus(HttpURLConnection.HTTP_OK); //do this for some clients that cannot handle HTTP status code 500    
        
        try (PrintWriter out = response.getWriter()) {
            out.write(xmlErrorResponse);            
            out.close();
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
        processError(request, response);
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
        processError(request, response);
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


/*PrintWriter out = response.getWriter();
            out.write("<html><head><title>Exception/Error Details</title></head><body>");
            if (statusCode != 500) {
            out.write("<h3>Error Details</h3>");
            out.write("<strong>Status Code</strong>:" + statusCode + "<br>");
            out.write("<strong>Requested URI</strong>:" + requestUri);
            } else {
            out.write("<h3>Exception Details</h3>");
            out.write("<ul><li>Servlet Name:" + servletName + "</li>");
            out.write("<li>Exception Name:" + throwable.getClass().getName() + "</li>");
            out.write("<li>Requested URI:" + requestUri + "</li>");
            out.write("<li>Exception Message:" + throwable.getMessage() + "</li>");
            out.write("</ul>");
            }
            
            out.write("<br><br>");
            out.write("<a href=\"index.html\">Home Page</a>");
            out.write("</body></html>");*/

}