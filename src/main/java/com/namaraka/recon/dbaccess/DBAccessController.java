package com.namaraka.recon.dbaccess;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.PrintWriter;
//import java.util.Enumeration;
//import java.util.concurrent.TimeUnit;
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import com.namaraka.dbms.utils.EmptyStringException;
//import com.namaraka.dbms.utils.GeneralUtils;
//import com.namaraka.dbms.utils.Security;
//import com.namaraka.recon.ApplicationPropertyLoader;
//import com.namaraka.recon.config.v1_0.Appconfig;
//import com.namaraka.recon.utilities.BindXmlAndPojo;
//import com.namaraka.recon.utilities.DBMSXMLObject;
//import java.util.List;
//import javax.xml.bind.JAXBException;
//import org.apache.http.HttpEntity;
//import org.apache.http.HttpResponse;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.impl.client.HttpClientBuilder;
//import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//import org.w3c.dom.Node;
//import org.w3c.dom.NodeList;
//import org.xml.sax.SAXException;
//
///**
// *
// * @author smallGod
// */
//public class DBAccessController extends HttpServlet {
//
//    private static final Logger logger = LoggerFactory.getLogger(DBAccessController.class);
//    private static final long serialVersionUID = 2639052820480879305L;
//    private final Appconfig props = ApplicationPropertyLoader.getInstance().getAppProps();
//
//    private static PoolingHttpClientConnectionManager connectionManager;
//
//    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
//            throws ServletException, IOException {
//
//        //response.setContentType(request.getContentType());
//        response.setContentType("application/xml");
//        response.setCharacterEncoding("UTF-8");
//
//        logRequestInfo(request); //debug info
//        printRequesterHeaderInfo(request, response);
//
//        InputStream xmlInputStream = request.getInputStream();
//
//        String requestUri = request.getRequestURI();
//
//        int lastIndex = requestUri.lastIndexOf("/");
//
//        //method name to indentify the operation to call
//        String requestMethodName = requestUri.substring(lastIndex + 1);
//
//        logger.debug("request method name: " + requestMethodName);
//
//        String xmlRequest;
//        String xmlResponse = null;
//
//        try {
//
//            validateBizLogicRequest(request);
//            xmlRequest = GeneralUtils.readXMLStream(xmlInputStream);
//
//        } catch (IOException | EmptyStringException exc) {
//            logger.error("Failed to parse the XML: " + exc.getMessage());
//            response.sendError(400, "Failed to parse the XML");
//            return;
//        } catch (Throwable exc) {
//            logger.error("Error, Failed to parse the XML: " + exc.getMessage());
//            response.sendError(400, "Failed to parse the XML");
//            return;
//        }
//
//        Class classType;
//        try {
//            //classType = GeneralUtils.getClass(rootNodeName);
//            classType = GeneralUtils.getClassType(xmlRequest);
//        } catch (NullPointerException npe) {
//            logger.error("Failed to get a ClassType: " + npe.getMessage());
//            response.sendError(400, "Failed to get a corresponding jaxb class type for the XML request recieved");
//            return;
//        } catch (Throwable exc) {
//            logger.error("Error, Failed to get a ClassType: " + exc.getMessage());
//            response.sendError(400, "Failed to get a corresponding jaxb class type for the XML request recieved");
//
//            return;
//        }
//
//        DBMSXMLObject hibernateObject;
//        try {
//            hibernateObject = BindXmlAndPojo.xmlToObject(xmlRequest, Servicerequest.class);
//        } catch (JAXBException | SAXException | NullPointerException exc) {
//            logger.error("Failed to unmarshal XML string: " + exc.getMessage());
//            response.sendError(400, "Failed to unmarshal XML payload");
//            return;
//        } catch (Throwable exc) {
//            logger.error("Error, Failed to unmarshal XML string: " + exc.getCause().getMessage());
//            response.sendError(400, "Failed to unmarshal XML payload");
//            return;
//        }
//
//        if (requestMethodName.equalsIgnoreCase(props.getManagermethods().getSave().trim())) {
//
//            long objectId = DBManager.persistDatabaseModel(hibernateObject);
//            logger.debug("created objectID = " + objectId);
//
//            Processeable processeableObject = (Processeable) hibernateObject;
//
//            Requestcreatedresponse requestCreatedResponse = new Requestcreatedresponse();
//
//            requestCreatedResponse.setRequestcreated(Requestcreatedtype.CREATED);
//            requestCreatedResponse.setMessage("Request is being pocessed.. send status request to get the final status");
//            //requestCreatedResponse.setClientrequestid(processeableObject.getClientrequestid());
//            //requestCreatedResponse.setNamarakarefid(processeableObject.getNamarakarefid());
//
//            xmlResponse = BindXmlAndPojo.objectToXML(requestCreatedResponse, Requestcreatedresponse.class);
//
//        } else if (requestMethodName.equalsIgnoreCase(props.getManagermethods().getUpdate().trim())) {
//
//            DBManager.updateDatabaseModel(classType, 1L);
//
//        } else if (requestMethodName.equalsIgnoreCase(props.getManagermethods().getDelete().trim())) {
//
//            try {
//                DBManager.deleteDatabaseModel(classType, 3L);
//            } catch (NullPointerException npe) {
//                logger.error("Delete object NOT found: " + npe.getMessage());
//                response.sendError(400, "Sorry delete Object NOT found");
//                return;
//            } catch (Throwable exc) {
//                logger.error("Error, Delete object NOT found: " + exc.getMessage());
//                response.sendError(400, "Sorry delete Object NOT found");
//                return;
//            }
//
//        } else if (requestMethodName.equalsIgnoreCase(props.getManagermethods().getRead().trim())) {
//
//            Servicerequest serv;
//            try {
//                serv = (Servicerequest) DBManager.retrieveDatabaseModel(classType, 1L);
//            } catch (NullPointerException npe) {
//                logger.error("Sorry no object retrieved from database: " + npe.getMessage());
//                response.sendError(400, "sorry No corresponding object found in database");
//                return;
//            } catch (Throwable exc) {
//                logger.error("Error, Sorry no object retrieved from database: " + exc.getMessage());
//                response.sendError(400, "sorry No corresponding object found in database");
//                return;
//            }
//            logger.debug("retrieved, amount: " + serv.getAmount().getAmount());
//
//        } else if (requestMethodName.equalsIgnoreCase(props.getManagermethods().getHandshake().trim())) {
//
//            Usernamestatustype usernameStatus = Usernamestatustype.NOK;
//            Handshake handShakeRequest;
//            String userName;
//
//            try {
//                handShakeRequest = (Handshake) BindXmlAndPojo.xmlToObject(xmlRequest, Handshake.class);
//                userName = handShakeRequest.getUsername();
//
//            } catch (JAXBException | SAXException | NullPointerException exc) {
//                logger.error("Failed to unmarshal XML string: " + exc.getMessage());
//                response.sendError(400, "Failed to unmarshal XML payload");
//                return;
//            } catch (Throwable exc) {
//                logger.error("Error, Failed to unmarshal XML string: " + exc.getCause().getMessage());
//                response.sendError(400, "Failed to unmarshal XML payload");
//                return;
//            }
//
//            List apiClients = DBManager.getRecordsEqualToPropertyValue(Apiclient.class, "username", userName);
//
//            if (apiClients.size() > 1) {
//                //means we have duplicates, more than one row with the same username value
//                //raise an alarm
//                logger.error("duplicate usernames");
//                usernameStatus = Usernamestatustype.NOK;
//            }
//            Apiclient apiClient = (Apiclient) apiClients.get(0);
//
//            //send the full password instead of substringing it... since it is encrypted the risk is very low of being hacked
//            String password = apiClient.getPassword();
//            logger.debug("password before substringing: " + password);             
//            //sub-string the password and only send the last 6 or 4 characters of the password back to BizLogic
//            //password = GeneralUtils.getFarRightSubstring(password, 6); //put the 6 in a configs file parameter so it is adjustable
// 
//            Dbaccessrighttype dbAccessRights = apiClient.getDbaccessright(); //we might want to pass on this info to bizlogic to determine if we can give access to certain method to this guy on their second request
//            String serviceaccesscodes = apiClient.getServiceaccesscodes(); //comma separated codes for diff.. services API user is allowed to access
//
//            Handshakeresponse handShakeResponse = new Handshakeresponse();
//            handShakeResponse.setStatus(usernameStatus);
//            handShakeResponse.setDescription("ok");
//
//            response = Security.setBasicEncoding(response, password, null);
//            xmlResponse = BindXmlAndPojo.objectToXML(handShakeResponse, Handshakeresponse.class);
//
//        } else {
//
//            logger.warn("unknown method called: " + requestMethodName);
//            //throw a MyCustomException so that it can be handled correctly and 
//            //an appropriate error-response object created and sent back to MTN
//
//        }
//
//        try (PrintWriter out = response.getWriter()) {
//
//            out.write(xmlResponse);
//            out.close();
//        }
//
//        if (response.isCommitted()) {
//            logger.info("Response committed!!");
//        } else {
//            logger.info("Response NOT committed!!");
//        }
//
//    }
//
//    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
//    /**
//     * Handles the HTTP <code>GET</code> method.
//     *
//     * @param request servlet request
//     * @param response servlet response
//     * @throws ServletException if a servlet-specific error occurs
//     * @throws IOException if an I/O error occurs
//     */
//    @Override
//    protected void doGet(HttpServletRequest request, HttpServletResponse response)
//            throws ServletException, IOException {
//        processRequest(request, response);
//    }
//
//    /**
//     * Handles the HTTP <code>POST</code> method.
//     *
//     * @param request servlet request
//     * @param response servlet response
//     * @throws ServletException if a servlet-specific error occurs
//     * @throws IOException if an I/O error occurs
//     */
//    @Override
//    protected void doPost(HttpServletRequest request, HttpServletResponse response)
//            throws ServletException, IOException {
//        processRequest(request, response);
//    }
//
//    /**
//     * Returns a short description of the servlet.
//     *
//     * @return a String containing servlet description
//     */
//    @Override
//    public String getServletInfo() {
//        return "servlet handles all incoming request traffic from the MTN-ECW clients";
//    }// </editor-fold>
//
//    /**
//     *
//     * @param reader
//     * @return an xml request string from the MTN ECW client
//     * @throws IOException
//     */
//    private String readIncomingXMLRequest(BufferedReader reader) throws IOException {
//
//        StringBuilder xmlBuilder = new StringBuilder();
//        String xmlRequestString;
//
//        while ((xmlRequestString = reader.readLine()) != null) {
//            xmlBuilder.append(xmlRequestString);
//        }
//        reader.close();
//
//        xmlRequestString = xmlBuilder.toString();
//        logger.info(">>> Got XML: " + xmlRequestString);
//
//        if (xmlBuilder.length() > 0) {
//            logger.info(">>> XML builder length < 0");
//        }
//        return xmlRequestString;
//    }
//
//    /**
//     * Method prints all the http request headers in this request
//     *
//     * @param req
//     * @param res
//     * @throws IOException
//     */
//    public void printRequesterHeaderInfo(HttpServletRequest req, HttpServletResponse res) throws IOException {
//
//        Enumeration<String> headerNames = req.getHeaderNames();
//
//        while (headerNames.hasMoreElements()) {
//
//            String headerName = headerNames.nextElement();
//            logger.info(">>> header name  : " + headerName);
//
//            Enumeration<String> headers = req.getHeaders(headerName);
//            while (headers.hasMoreElements()) {
//                String headerValue = headers.nextElement();
//                logger.info(">>> header value : " + headerValue);
//            }
//            logger.info(">>> -------------------------------------");
//        }
//    }
//
//    private void sendBackResponse(String xmlMessage, String action) {
//
//        HttpClient httpclient = initConnClient();
//        HttpPost httpPost = setHttpPostHeaders();
//
//        HttpEntity xmlEntity = null;
//        InputStream inputStream = null;
//        HttpResponse response;
//
//    }
//
//    private HttpPost setHttpPostHeaders() {
//
//        HttpPost httpPost = new HttpPost("https://google.com");
//        //httpPost.setHeader("Host", "accounts.google.com");
//        //httpPost.setHeader("User-Agent", USER_AGENT);
//        //httpPost.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
//        //httpPost.setHeader("Accept-Language", "en-US,en;q=0.5");
//        //httpPost.setHeader("Cookie", getCookies());
//        //httpPost.setHeader("Connection", "keep-alive");
//        //if (lastConnection){
//        //httpPost.setHeader("Connection", "close"); 
//        //}
//        //httpPost.setHeader("Referer",  "https://accounts.google.com/ServiceLoginAuth");
//        //httpPost.setHeader("Host","staff.ezshift.co.il");
//        //httpPost.setHeader("Connection","keep-alive");
//        //httpPost.setHeader("Content-Length","391");
//        //httpPost.setHeader("Cache-Control","max-age=0");
//        //httpPost.setHeader("Origin","http://staff.ezshift.co.il");
//        //httpPost.setHeader("Keep-Alive","115");
//        //httpPost.setHeader("Proxy-Connection","keep-alive");
//        //httpPost.setHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/21.0.1180.79 Safari/537.1");
//        //httpPost.setHeader("Content-Type","application/x-www-form-urlencoded"); //maybe the cause or 500 when i do get first
//        //httpPost.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8, value");
//        //httpPost.setHeader("Referer","http://staff.ezshift.co.il/appfilesV3/loginHE-il.aspx");
//        //httpPost.setHeader("Accept-Encoding","gzip,deflate,sdch");
//        //httpPost.setHeader("Accept-Language","he-IL,he;q=0.8,en-US;q=0.6,en;q=0.4");
//        //httpPost.setHeader("Accept-Charset","windows-1255,utf-8;q=0.7,*;q=0.3");
//        //httpPost.setHeader("Cookie","
//        //httpPost.setHeader("Connection", "keep-alive");
//        httpPost.setHeader("Connection", "close");
//        httpPost.setHeader("Content-Type", "application/xml");
//        return httpPost;
//    }
//
//    private static HttpClient initConnClient() {
//
//        HttpClient httpclient;
//
//        connectionManager = new PoolingHttpClientConnectionManager();
//        connectionManager.setDefaultMaxPerRoute(5);
//        connectionManager.closeExpiredConnections();
//        // Optionally, close connections that have been idle longer than 30 sec
//        connectionManager.closeIdleConnections(30, TimeUnit.SECONDS);
//        httpclient = HttpClientBuilder.create().setConnectionManager(connectionManager).build();
//
//        //new IdleConnectionMonitorThread(connectionManager).run();
//        return httpclient;
//    }
//
//    void validateBizLogicRequest(HttpServletRequest request) {
//
//        //check if request is coming from the BizLogic IP
//        //check if the bizlogic credentials are correct
//        String serverName = request.getServerName().trim();
//
//        if (props.getAllowedips().getIp().contains(serverName)) {
//
//            //get the username from the header
//            String[] credentialsArray = Security.decodeBasicAuthCredentials(request, 2);
//
//            String bizLogicUsername = credentialsArray[0].trim();
//            String bizLogicPassword = credentialsArray[1].trim();
//
//            //check if credentials given are valid else throw MyCustomException
//        } else {
//            logger.warn("Request from UN-AUTHORISED_CLIENT : " + serverName);
//            // throw new MyCustomException("Client IP NOT allowed", "UN-AUTHORISED_CLIENT", "Request from a client: " + serverName + " - NOT authorised to access this service", "CLIENT_ERROR");
//
//        }
//    }
//
//    private void logRequestInfo(HttpServletRequest request) {
//
//        logger.debug(">>> Request Content-type   : " + request.getContentType());
//        logger.debug(">>> Request Context-path   : " + request.getContextPath());
//        logger.debug(">>> Request Content-type   : " + request.getContentType());
//        logger.debug(">>> Request Content-length : " + request.getContentLength());
//        logger.debug(">>> Request Protocol       : " + request.getProtocol());
//        logger.debug(">>> Request PathInfo       : " + request.getPathInfo());
//        logger.debug(">>> Request Remote Address : " + request.getRemoteAddr());
//        logger.debug(">>> Request Remote Port    : " + request.getRemotePort());
//        logger.debug(">>> Request Server name    : " + request.getServerName());
//        logger.debug(">>> Request Querystring    : " + request.getQueryString());
//        logger.debug(">>> Request URL            : " + request.getRequestURL().toString());
//        logger.debug(">>> Request URI            : " + request.getRequestURI());
//    }
//    /*public void doWork(HttpServletRequest req, HttpServletResponse resp)
//     throws ServletException, IOException {
//
//     String contentType = req.getContentType();
//     if (contentType.equalsIgnoreCase("text/xml")
//     || contentType.equalsIgnoreCase("application/xml")) {
//     InputStream inputStream = req.getInputStream();
//     DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//     try {
//     DocumentBuilder docBuilder = factory.newDocumentBuilder();
//     Document doc = docBuilder.parse(inputStream);
//     if (doc != null) {
//     boolean result = changePassword(doc);
//     if (result == true) {
//     resp.setContentType(contentType);
//     // following code serializes dom to xml file
//
//     OutputFormat of = new OutputFormat(doc);
//     of.setIndenting(false);
//     ServletOutputStream outputStream = resp.getOutputStream();
//     XMLSerializer serializer = new XMLSerializer();
//     serializer.setOutputFormat(of);
//     serializer.setOutputByteStream(outputStream);
//     DOMSerializer domSerializer = serializer.asDOMSerializer();
//     domSerializer.serialize(doc);
//     outputStream.flush();
//     outputStream.close();
//     }
//     }
//     } catch (Exception e) {
//     e.printStackTrace();
//     }
//     }
//
//     }*/
//    public boolean changePassword(Document doc) {
//        Element rootElement = doc.getDocumentElement();
//        if (rootElement != null) {
//            NodeList list = rootElement.getElementsByTagName("password");
//            if (list != null) {
//                int len = list.getLength();
//                for (int i = 0; i < len; i++) {
//                    Node password = list.item(i);
//                    Node passwordText = password.getFirstChild();
//
//                    if (passwordText.getNodeType() != Node.TEXT_NODE) {
//                        return false;
//                    }
//                    String value = passwordText.getNodeValue();
//                    StringBuffer strbuff = new StringBuffer(value);
//                    int valueLen = strbuff.length();
//                    for (int j = 0; j < valueLen; j++) {
//                        char oldchar = strbuff.charAt(j);
//                        strbuff.setCharAt(j, (char) (oldchar + 1));
//                    }
//
//                    passwordText.setNodeValue(strbuff.toString());
//                }
//            }
//        }
//        return true;
//    }
//
//    private boolean writeC2BPaymetToHubDB() {
//
//        boolean isWritten = false;
//
//        return isWritten;
//
//    }
//
//    /*private boolean updateBeepAndBank(String beepMessageContent) {
//
//     String finalStatusCode = null;
//     String statusDescript = null;
//     boolean isChangeStatus = false; //flag when we need to change a txn from ACCEPTED to REJECTED perhaps due to an SDP timeout
//     String bankCode = null;
//     String payerTxnID = null;
//     //transaction has not failed in between before reaching bank, for example wrong username/password at Cellulant prompting 'contact cellulant support'
//     boolean txnPushedToBank = true;
//     boolean isToBeReversedAtBank = false;
//     boolean isToBeAcknowledgedAtBAnk = true;
//
//     logger.info("going to build the BEEPAckPayload object with: " + beepMessageContent);
//     BEEPAckPayLoad beepAckPayload = Utilities.generateBeepAckPayload(beepMessageContent);
//
//     logger.info("original statuscode  : " + beepAckPayload.getOriginalStatusCode());
//     logger.info("original status desc : " + beepAckPayload.getOriginalStatusDesc());
//
//     //maintain the status code as it is (successful or failed) but align it to the status codes BEEP core understands e.g. props.getACCEPTED_STATUS()
//     if (beepAckPayload.getOriginalStatusCode().equalsIgnoreCase(props.getC2BTxnPushed2BankCode())) {
//     //change the final status to a status BEEP core understands, leave the original status intact - as u got it from the recievePay API
//     beepAckPayload.setFinalStatusCode(String.valueOf(props.getACCEPTED_STATUS()));
//     } else if (beepAckPayload.getOriginalStatusCode().equalsIgnoreCase(props.getC2BTxnFailureAtBAnk())) {
//     logger.info("C2B Push txn pushed to bank but NOT successful");
//     beepAckPayload.setFinalStatusCode(String.valueOf(props.getREJECTED_STATUS()));
//     //isToBeAcknowledgedAtBAnk = false; //do not bother acknowledging this txn at bank since txn hit bank but Failed
//     // we should acknowledge this transaction so that CSL can know when to send an SMS to the customer with final status
//     } else {
//     beepAckPayload.setFinalStatusCode(String.valueOf(props.getREJECTED_STATUS()));
//     }
//
//     //let through to MTN i.e. no timeout
//     if (letThroughTheMessage) {
//     logger.info("Payment response has been let through to MTN with original status code/desc");
//     //maintain the status description for the final same as the original description
//     beepAckPayload.setFinalStatusDesc(beepAckPayload.getOriginalStatusDesc());
//
//     } else {
//
//     logger.info("letThroughMessage is FALSE");
//
//     finalStatusCode = String.valueOf(props.getREJECTED_STATUS());
//     statusDescript = props.getMTNSDPTimeoutNarration();
//     isChangeStatus = true;
//
//     //only reverse successful transactions
//     if ((beepAckPayload.getOriginalStatusCode().equalsIgnoreCase(props.getC2BTxnPushed2BankCode()))) {
//     logger.info("Transaction status NOT recieved by MTN, so reversing at bank side");
//     isToBeReversedAtBank = true;
//     }
//     }
//
//     //first check if the transaction reached the bank
//     if (txnPushedToBank) {
//
//     logger.info("calling C2B status notification API for transaction with payer trxnID: " + beepAckPayload.getPayerTransactionID());
//
//     bankCode = props.getBankIDArray()[beepAckPayload.getPropsFilePosition()];
//     payerTxnID = beepAckPayload.getPayerTransactionID();
//
//     //if txn treached bank and was successful acknowledge it, otherwise no need to acknowledge it
//     if (isToBeAcknowledgedAtBAnk) {
//     logger.info("About to acknowledge this transaction status with the bank");
//     Runnable c2bStatusBankNotifyTask = Utilities.notifyC2BPayStatusUpdateFile(bankCode, payerTxnID, isToBeReversedAtBank);
//     MTNWebserviceDaemon.shortLivedTaskExecService.execute(c2bStatusBankNotifyTask);
//     }
//
//                
//     try {
//     Thread.sleep(10000L);
//     } catch (InterruptedException ex) {
//     logger.info("thread sleep exception: " + ex.getMessage());
//     }
//
//     Runnable acknowledgeTask = Utilities.acknowledgeTxnAndUpdateFile(beepAckPayload, finalStatusCode, statusDescript, isChangeStatus, true);
//     MTNWebserviceDaemon.shortLivedTaskExecService.execute(acknowledgeTask);
//
//     } else {
//
//     logger.info("-------------------------------------------------------------------");
//     logger.info("Transaction not posted to the bank, so no need to acknowledge it");
//     logger.info("statusCode at Cellulant : " + beepAckPayload.getOriginalStatusCode());
//     logger.info("statusDesc at Cellulant : " + beepAckPayload.getOriginalStatusDesc());
//     logger.info("-------------------------------------------------------------------");
//     }
//
//        
//
//     return letThroughTheMessage;
//     }*/
//}

public class DBAccessController{
    
}
