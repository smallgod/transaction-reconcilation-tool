/*
 * To change this license header, choose License Headers bufferedReader Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template bufferedReader the editor.
 */
package com.namaraka.recon.utilities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.namaraka.recon.ApplicationPropertyLoader;
import com.namaraka.recon.IF.TxnRecordIF;
import com.namaraka.recon.InitApp;
import com.namaraka.recon.config.v1_0.Appconfig;
import com.namaraka.recon.constants.CenteServiceType;
import com.namaraka.recon.constants.EquinoxField;
import com.namaraka.recon.constants.ErrorCategory;
import com.namaraka.recon.constants.ErrorCode;
import com.namaraka.recon.constants.FilePosition;
import com.namaraka.recon.constants.FileReconProgressEnum;
import com.namaraka.recon.constants.LinkType;
import com.namaraka.recon.constants.ProcessingEffort;
import com.namaraka.recon.constants.ReconStatus;
import com.namaraka.recon.constants.ReportDetailsJsonKeys;
import com.namaraka.recon.constants.ReportType;
import com.namaraka.recon.constants.TransactionState;
import com.namaraka.recon.dbaccess.DBManager;
import com.namaraka.recon.exceptiontype.MyCustomException;
import com.namaraka.recon.model.v1_0.EquinoxDetails;
import com.namaraka.recon.model.v1_0.Linked;
import com.namaraka.recon.model.v1_0.Linker;
import com.namaraka.recon.model.v1_0.ReconTransactionsTable;
import com.namaraka.recon.model.v1_0.ReconciliationDetails;
import com.namaraka.recon.model.v1_0.ReportDetails;
import com.namaraka.recon.unused.FileWriter;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.hibernate.Criteria;
import org.hibernate.StatelessSession;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author smallgod
 */
public class GeneralUtils { //we need to do a singleton pattern for this class

    private static final Logger logger = LoggerFactory.getLogger(GeneralUtils.class);

    private static final Appconfig props = ApplicationPropertyLoader.loadInstance().getAppProps();

    private static final Type stringMapType = new TypeToken<Map<String, Object>>() {
    }.getType();

    private static final Type mapInMapType = new TypeToken<Map<String, Map<String, String>>>() {
    }.getType();

    public static String getRootNodeName(InputStream xmlInputStream) throws NullPointerException {

        String firstNodeName;

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(false);
        //dbFactory.setIgnoringElementContentWhitespace(true);
        //dbFactory.setValidating(true);       
        DocumentBuilder dBuilder;
        Document doc;

        try {
            dBuilder = dbFactory.newDocumentBuilder();

            InputSource is = new InputSource(xmlInputStream);
            //InputSource is = new InputSource(new StringReader(xmlString));

            //read this - http://stackoverflow.com/questions/13786607/normalization-bufferedReader-dom-parsing-with-java-how-does-it-work
            //doc = dBuilder.parse(xmlFileName);
            doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();
            firstNodeName = doc.getDocumentElement().getNodeName();
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            logger.error("error parsing xml request: " + ex.getMessage());
            throw new NullPointerException("Error while getting xml request");
        }
        return firstNodeName;
    }

    /**
     * *
     *
     * @param xmlString
     * @return XML rootNodeName
     * @throws MyCustomException
     */
    public static String getRootNodeName(String xmlString) throws MyCustomException {

        String rootNodeName;

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(false);
        //dbFactory.setIgnoringElementContentWhitespace(true);
        //dbFactory.setValidating(true);       
        DocumentBuilder dBuilder;
        Document doc;

        try {
            dBuilder = dbFactory.newDocumentBuilder();

            InputSource is = new InputSource(new StringReader(xmlString));

            //read this - http://stackoverflow.com/questions/13786607/normalization-bufferedReader-dom-parsing-with-java-how-does-it-work
            //doc = dBuilder.parse(xmlFileName);
            doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();
            rootNodeName = doc.getDocumentElement().getNodeName();
        } catch (ParserConfigurationException | SAXException ex) {
            logger.error("error parsing xml request: " + ex.getMessage());
            throw new MyCustomException("Error parsing XML request", ErrorCode.BAD_REQUEST_ERR, "Error parsing xml request: " + ex.getMessage(), ErrorCategory.CLIENT_ERR_TYPE);
        } catch (IOException ex) {
            logger.error("error parsing xml request: " + ex.getMessage());
            throw new MyCustomException("Error parsing XML request", ErrorCode.BAD_REQUEST_ERR, "I/O Exception while parsing XML request: " + ex.getMessage(), ErrorCategory.SERVER_ERR_TYPE);
        }

        int rootNodeNameIndex = rootNodeName.lastIndexOf(':');

        String nodeName;

        if (rootNodeNameIndex != -1) {
            nodeName = rootNodeName.substring(rootNodeNameIndex + 1).trim();

            if (nodeName.length() < 1) {
                throw new MyCustomException("Error parsing XML request", ErrorCode.BAD_REQUEST_ERR, "Could not find a root nodename associated with this request", ErrorCategory.CLIENT_ERR_TYPE);
            }

        } else {
            return rootNodeName;
        }

        return nodeName;
    }

    /**
     * *
     *
     * @param request
     * @return
     * @throws MyCustomException
     */
    public static String readXMLStream(HttpServletRequest request) throws MyCustomException {

        BufferedReader bufferedReader = null;
        InputStream xmlInputStream = null;
        String inputLine;
        String xmlResponse = "";

        try {

            xmlInputStream = request.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(xmlInputStream));

            while ((inputLine = bufferedReader.readLine()) != null) {
                xmlResponse += inputLine;
            }
        } catch (IOException ex) {
            throw new MyCustomException("IO Exception", ErrorCode.COMMUNICATION_ERR, "IO Exception reading XML request: " + ex.getMessage(), ErrorCategory.SERVER_ERR_TYPE);
        } finally {

            if (bufferedReader != null) {

                try {
                    bufferedReader.close();
                } catch (IOException ex) {
                    logger.error("IO exception trying to close bufferedReader: " + ex.getMessage());
                }
            }

            if (xmlInputStream != null) {
                try {
                    xmlInputStream.close();
                } catch (IOException ex) {
                    logger.error("IO exception trying to close input stream: " + ex.getMessage());
                }
            }
        }

        if (xmlResponse.trim().isEmpty()) {
            throw new MyCustomException("IO Exception", ErrorCode.BAD_REQUEST_ERR, "XML request is empty", ErrorCategory.SERVER_ERR_TYPE);
        }

        return xmlResponse.trim();
    }

    public static Class getClass(String xmlRootNodeName) throws NullPointerException {

        Class classType = null;

        if (xmlRootNodeName.equalsIgnoreCase(props.getNodenames().getServicerequest())) {
            //classType =  Servicerequest.class;
        } else if (xmlRootNodeName.equalsIgnoreCase(props.getNodenames().getPaymentrequest())) {
            //classType = Paymentrequest.class;
        }

        if (classType == null) {
            throw new NullPointerException("class type corresponding to xml payload is null");
        }
        return classType;
    }

    /**
     *
     * @param xmlToParse
     * @return
     * @throws com.namaraka.bizlogic.exceptiontypes.MyCustomException
     */
    public static Class getClassType(String xmlToParse) throws MyCustomException {

        String xmlRootNodeName = getRootNodeName(xmlToParse);
        logger.debug("rootNode name: " + xmlRootNodeName);

        Class classType = null;

        if (xmlRootNodeName.equalsIgnoreCase(props.getNodenames().getApiclient())) {
            //classType = Apiclient.class;
        } else if (xmlRootNodeName.equalsIgnoreCase(props.getNodenames().getPaymentrequest())) {
            //classType = Paymentrequest.class;
        } else if (xmlRootNodeName.equalsIgnoreCase(props.getNodenames().getBeneficiarydetailsrequest())) {
            //classType = Beneficiarydetailsrequest.class;
        }

        if (classType == null) {

            throw new MyCustomException("Unexpected XML root nodename", ErrorCode.BAD_REQUEST_ERR, "Unexpected XML root nodename: " + xmlRootNodeName + " couldn't find any class implementation", ErrorCategory.CLIENT_ERR_TYPE);
        }
        return classType;

    }

    /**
     * Generate short UUID (13 characters)
     *
     * @return short randomValue
     */
    public static String generateShorterRandomID() {

        UUID uuid = UUID.randomUUID();
        //long longValue = ByteBuffer.wrap(uuid.toString().getBytes()).getLong();
        //randomValue = Long.toString(longValue, Character.MAX_RADIX);
        long lessSignificantBits = uuid.getLeastSignificantBits();
        String randomValue = Long.toString(lessSignificantBits, Character.MAX_RADIX);

        return randomValue;

    }

    /**
     *
     * @return full randomValue
     */
    public static String generateFullRandomID() {

        UUID uuid = UUID.randomUUID();

        long longValue = ByteBuffer.wrap(uuid.toString().getBytes()).getLong();
        String randomValue = Long.toString(longValue, Character.MAX_RADIX);

        return randomValue;
    }

    /**
     * *
     * Convert long value to int or throw exception
     *
     * @param valueToConvert
     * @return
     * @throws MyCustomException
     */
    public static int convertLongToInt(long valueToConvert) throws MyCustomException {

        int intValue;

        //For negative numbers, use MIN_VALUE instead of `MAX_VALUE'
        if (valueToConvert > (long) Integer.MAX_VALUE) {
            throw new MyCustomException("Conversion error", ErrorCode.ARITHMETIC_ERR, valueToConvert + " is too big to convert to an int", ErrorCategory.CLIENT_ERR_TYPE);
        } else {
            intValue = (int) valueToConvert;
        }

        return intValue;
    }

    /**
     * *
     * @param str
     * @param len
     * @return
     * @throws com.namaraka.bizlogic.exceptiontypes.MyCustomException
     * @throws NullPointerException
     */
    public static String getFarRightSubstring(String str, int len) throws MyCustomException {

        if (str == null) {
            throw new MyCustomException("Null exception", ErrorCode.BAD_REQUEST_ERR, "Cannot substring password string, it is null", ErrorCategory.CLIENT_ERR_TYPE);
        }
        /*if (len < 0) {
         return "";
         }*/
        if (str.length() <= len) {
            return str;
        }

        return str.substring(str.length() - len);
    }

    /**
     * *
     *
     * @param pool EexecutorService pool
     * @param timeToWait
     * @param timeUnit
     * @return if process was interrupted (true) or not (false)
     */
    public static boolean shutdownAndAwaitTerminationInMins(final ExecutorService pool, long timeToWait, TimeUnit timeUnit) {

        boolean poolShutdownFlag = false;

        logger.info(">>> shutdownAndAwaitTermination() called, Executor pool waiting for tasks to complete");
        pool.shutdown(); // Disable new tasks from being submitted

        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(timeToWait, timeUnit)) {

                logger.error("Executor pool terminated with tasks unfinished. Unfinished tasks will be retried.");
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(timeToWait, timeUnit)) {
                    logger.error("Executor pool terminated with tasks unfinished. Unfinished tasks will be retried.");
                }
            } else {
                logger.info("Executor pool completed all tasks and has shut down normally");
                poolShutdownFlag = true;
            }
        } catch (InterruptedException ie) {
            logger.error("Executor pool shutdown error: " + ie.getMessage());
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
        return poolShutdownFlag;
    }

//    public static synchronized boolean saveToLocalFile(String xmlData) {
//
//        boolean isFileCreated = isFileCreated(xmlDatak);
//
//        if (isFileCreated) {//write to it
//
//            boolean dataExists = isDataExists(filepath, data);
//
//            if (!dataExists) {
//                writeDataToFile(filepath, data);
//            }
//        }
//    }
    private static synchronized boolean isFileCreated(final String filePathName) {

        boolean isFileCreated = false;

        File file = new File(filePathName);

        if (file.exists()) {
            isFileCreated = true;
        } else {

            FileOutputStream fileOutputStream = null;

            try {
                fileOutputStream = new FileOutputStream(filePathName);
                isFileCreated = true;
            } catch (IOException ex) {
                logger.error("IOException trying to create fileOutputStream: " + ex.getMessage());
            } catch (SecurityException ex) {
                logger.error("Security Exception trying to create fileOutputStream: " + ex.getMessage());
            } catch (Exception ex) {
                logger.error("Exception trying to create fileOutputStream: " + ex.getMessage());
            } finally {

                try {
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                } catch (IOException ex) {
                    logger.error("IOException trying to close fileOutputStream: " + ex.getMessage());
                }
            }
        }
        return isFileCreated;
    }

    private static synchronized void writeDataToFile(final String filepath, final String data) {

        try (PrintWriter pout = new PrintWriter(new FileOutputStream(filepath, true))) {

            pout.println(data);
            pout.flush();

        } catch (FileNotFoundException ex) {
            logger.error("FileNotFoundException writing data: " + ex.getMessage());
        } catch (SecurityException ex) {
            logger.error("SecurityException writing data: " + ex.getMessage());
        } catch (Exception ex) {
            logger.error("Exception writing data: " + ex.getMessage());
        }
    }

    private static boolean isDataExists(final String file, final String dataToCheck) {

        boolean dataExists = true;

        FileInputStream fin;
        DataInputStream in;
        BufferedReader br;

        try {
            fin = new FileInputStream(file);
            in = new DataInputStream(fin);
            br = new BufferedReader(new InputStreamReader(in));

            if (br.readLine() == null) { //file is empty
                return false;
            }

            String data;
            while ((data = br.readLine()) != null) {

                if ((dataToCheck.trim()).equals(data)) {
                    dataExists = true;
                } else {
                    dataExists = false;
                }
            }

            br.close();
            in.close();
            fin.close();

        } catch (FileNotFoundException ex) {
            logger.error("FileNotFound trying to create fileOutputStream: " + ex.getMessage());
        } catch (IOException ex) {
            logger.error("IOException trying to create fileOutputStream: " + ex.getMessage());
        } catch (SecurityException ex) {
            logger.error("Security Exception trying to create fileOutputStream: " + ex.getMessage());
        } catch (Exception ex) {
            logger.error("Exception trying to create fileOutputStream: " + ex.getMessage());
        }
        return dataExists;
    }

    private void deleteQuery(final String filePathName, final String retriedTrxData) {

        List<String> queries = new ArrayList<>(1);
        FileInputStream fin;
        DataInputStream in;
        BufferedReader br;

        try {
            fin = new FileInputStream(filePathName);
            in = new DataInputStream(fin);
            br = new BufferedReader(new InputStreamReader(in));

            String data;
            while ((data = br.readLine()) != null) {
                queries.add(data);
            }

            br.close();
            in.close();
            fin.close();

            // Find a match to the query
            logger.info("Just about to remove this data: "
                    + retriedTrxData + " from file: " + filePathName);

            if (queries.contains(retriedTrxData)) {
                queries.remove(retriedTrxData);
                logger.info("Transaction data has been removed from the "
                        + "failed acks file: " + retriedTrxData + " from file: "
                        + filePathName);
            }

            // Now save the filePathName
            PrintWriter pout = new PrintWriter(
                    new FileOutputStream(filePathName, false));

            for (String newQueries : queries) {
                pout.println(newQueries);
            }

            pout.flush();
            pout.close();

        } catch (FileNotFoundException ex) {
            logger.error("No failed debits/ACKs file, continuing...");
        } catch (IOException ex) {
            logger.error("Unable to read/write to/from failed ACKs file."
                    + " Error message: " + ex.getMessage());
        }
    }

    /**
     * Return JSON string representation of given object
     *
     * @param <T>
     * @param objectToConvert
     * @param objectType
     * @return
     */
    public static <T> String convertToJson(Object objectToConvert, Class<T> objectType) {

        Gson gson = new Gson();
        //Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
        return gson.toJson(objectToConvert, objectType);
    }

    /**
     * Return JSON string representation of given object
     *
     * @param <T>
     * @param objectToConvert
     * @param objectType
     * @return
     */
    public static <T> String convertToJson(Object objectToConvert, Type objectType) {

        Gson gson = new Gson();
        //Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

        return gson.toJson(objectToConvert, objectType);
    }

    /**
     * Return Object from JSON string
     *
     * @param <T>
     * @param stringToConvert
     * @param objectType
     * @return
     * @throws com.namaraka.recon.exceptiontype.MyCustomException
     */
    public static <T> T convertFromJson(String stringToConvert, Class<T> objectType) throws MyCustomException {

        Gson gson = new Gson();
        T returnObj;

        try {
            returnObj = gson.fromJson(stringToConvert.trim(), objectType);

        } catch (JsonSyntaxException jse) {
            throw new MyCustomException("JSON Syntax Error", ErrorCode.INTERNAL_ERR, "Json syntax error converting from JSON: " + jse.getMessage(), ErrorCategory.SERVER_ERR_TYPE);

        }

        return returnObj;
    }

    /**
     *
     * @param <T>
     * @param stringArrayToConvert
     * @param objectType
     * @return a list of converted JSON strings
     * @throws com.namaraka.recon.exceptiontype.MyCustomException
     */
    public static <T> List<T> convertFromJson(List<String> stringArrayToConvert, Type objectType) throws MyCustomException {

        Gson gson = new Gson();

        List list = new ArrayList<>();

        try {
            for (String strToConvert : stringArrayToConvert) {

                list.add(gson.fromJson(strToConvert.trim(), objectType));
            }
        } catch (JsonSyntaxException jse) {
            throw new MyCustomException("JSON Syntax Error", ErrorCode.INTERNAL_ERR, "Json syntax error converting from JSON: " + jse.getMessage(), ErrorCategory.SERVER_ERR_TYPE);

        }
        return list;
    }

    /**
     * Return Object from JSON string
     *
     * @param <T>
     * @param stringToConvert
     * @param objectType
     * @return
     */
    public static <T> T convertFromJson(String stringToConvert, Type objectType) throws JsonSyntaxException {

        Gson gson = new Gson();
        return gson.fromJson(stringToConvert.trim(), objectType);
    }

    /**
     * Convert a JSON string to pretty print version
     *
     * @param jsonString
     * @return a well formatted JSON string
     */
    public static String toPrettyJsonFormat(String jsonString) {
        JsonParser parser = new JsonParser();

        JsonObject json = parser.parse(jsonString).getAsJsonObject();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = gson.toJson(json);

        return prettyJson;
    }

    /**
     * Convert a collection to a String Array
     *
     * @param <E>
     * @param arrayToConvert
     * @return String[]
     */
    public static <E> String[] convertToStringArray(Collection<E> arrayToConvert) {

        Object[] objectArray = arrayToConvert.toArray();
        String[] stringArray = Arrays.copyOf(objectArray, objectArray.length, String[].class);

        return stringArray;

    }

    /**
     * Convert a Collet to a Set
     *
     * @param <E>
     * @param arrayToConvert
     * @return Set<E>
     */
    public static <E> Set<E> convertToSet(Collection<E> arrayToConvert) {

        Set<E> set = new HashSet<>(arrayToConvert);

        return set;
    }

    /**
     *
     * @param <E>
     * @param stringArrayToConvert
     * @return Set<E>
     */
    public static <E> Set<E> convertToSet(E[] stringArrayToConvert) {

        Set<E> set = new HashSet<>(Arrays.asList(stringArrayToConvert));

        return set;
    }

    /**
     * convert a collection to an array
     *
     * @param <E>
     * @param arrayToConvert
     * @return Object[]
     */
    public static <E> Object[] convertToArray(Collection<E> arrayToConvert) {

        Object[] convertedArray = arrayToConvert.toArray();

        return convertedArray;
    }

    /**
     * Get current date-time string
     *
     * @param timeZone
     * @param dateStringFormat
     * @return
     */
    //kampala time zone - 'Africa/Kampala'
    public static String getDateTimeNow(String timeZone, String dateStringFormat) {

        /*DateTime now = new DateTime();
         DateTimeZone kampalaTimeZone1 = DateTimeZone.forID(timeZone);
         DateTime convertedTime1 = now.toDateTime(kampalaTimeZone1);*/
        DateTime dateNow = DateTime.now();
        DateTimeZone kampalaTimeZone = DateTimeZone.forID(timeZone);
        DateTime kampalTimeNow = dateNow.toDateTime(kampalaTimeZone);

        DateTimeFormatter formatter = DateTimeFormat.forPattern(dateStringFormat);
        //DateTime dateTime = formatter.parseDateTime(dateString);
        String formattedDate = formatter.print(kampalTimeNow);

        return formattedDate;
    }

    /**
     * parse dateTime in a formatted string - e.g. Kampala timezone is -
     * "Africa/Kampala" and a string-format can be "ddMMyyyyHHmmss"
     *
     * @param dateTimeToFormat
     * @param timeZone
     * @param dateStringFormat
     * @return
     */
    public static String formatDateTime(DateTime dateTimeToFormat, String timeZone, String dateStringFormat) {

        if (dateTimeToFormat == null) {
            logger.warn("Failed to Convert Date, DateTime to Format is NULL!!");
            return "";
        }
        DateTimeZone dateTimeZone = DateTimeZone.forID(timeZone);
        DateTime newDateTime = dateTimeToFormat.toDateTime(dateTimeZone);
        DateTimeFormatter formatter = DateTimeFormat.forPattern(dateStringFormat);
        //DateTime dateTime = formatter.parseDateTime(dateString);
        String formattedDate = formatter.print(newDateTime);

        return formattedDate;
    }

    /**
     * Replace one or more spaces in a string with given string/xter
     *
     * @param stringToFormat
     * @param replaceWith
     * @return
     */
    public static String replaceSpaces(String stringToFormat, String replaceWith) {

        String formattedString = stringToFormat.replaceAll("\\s+", replaceWith);
        return formattedString;
    }

    /**
     * Time difference between startTime and now
     *
     * @param startTime
     * @return
     */
    public static String timeTakenToNow(DateTime startTime) {

        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .appendYears().appendSuffix(" years ")
                .appendMonths().appendSuffix(" months ")
                .appendWeeks().appendSuffix(" weeks ")
                .appendDays().appendSuffix(" days ")
                .appendHours().appendSuffix(" hours ")
                .appendMinutes().appendSuffix(" minutes ")
                .appendSeconds().appendSuffix(" seconds ")
                .appendMillis().appendSuffix(" milliseconds ")
                //.printZeroNever() //if you don't want to print zeros
                .toFormatter();

        //DateTime myBirthDate = new DateTime(1978, 3, 26, 12, 35, 0, 0);
        DateTime now = new DateTime();
        Period period = new Period(startTime, now);

        String elapsed = formatter.print(period);

        return elapsed;
    }

    /**
     * Time difference between startTime and endTime
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public static String timeTakenToNow(DateTime startTime, DateTime endTime) {

        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .appendYears().appendSuffix(" years  ")
                .appendMonths().appendSuffix(" months  ")
                .appendWeeks().appendSuffix(" weeks  ")
                .appendDays().appendSuffix(" days  ")
                .appendHours().appendSuffix(" hours  ")
                .appendMinutes().appendSuffix(" minutes  ")
                .appendSeconds().appendSuffix(" seconds  ")
                .appendMillis().appendSuffix(" milliseconds  ")
                //.printZeroNever() //if you don't want to print zeros
                .toFormatter();

        //DateTime myBirthDate = new DateTime(1978, 3, 26, 12, 35, 0, 0);
        Period period = new Period(startTime, endTime);

        String elapsed = formatter.print(period);

        return elapsed;
    }

    /**
     * Get the JSON string from an HTTPServerletRequest
     *
     * @param request
     * @return
     * @throws MyCustomException
     */
    public static String getJsonStringFromRequest(HttpServletRequest request) throws MyCustomException {

        StringBuilder sb = new StringBuilder();
        BufferedReader reader = null;
        String s;

        try {

            reader = request.getReader();

            do {

                s = reader.readLine();

                if (s != null) {
                    sb.append(s);
                } else {
                    break;
                }

            } while (true);

        } catch (IOException ex) {
            logger.error("IO Exception, failed to decode JSON string from request: " + ex.getMessage());
            throw new MyCustomException("IO Exception occurred", ErrorCode.CLIENT_ERR, "Failed to decode JSON string from the HTTP request: " + ex.getMessage(), ErrorCategory.CLIENT_ERR_TYPE);

        } catch (Exception ex) {
            logger.error("General Exception, failed to decode JSON string from request: " + ex.getMessage());
            throw new MyCustomException("General Exception occurred", ErrorCode.CLIENT_ERR, "Failed to decode JSON string from the HTTP request: " + ex.getMessage(), ErrorCategory.CLIENT_ERR_TYPE);

        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    logger.error("exception closing buffered reader: " + ex.getMessage());
                }
            }
        }

        return sb.toString();
    }

    /**
     * Write a response to calling server client
     *
     * @param response
     * @param responseToWrite
     * @throws com.namaraka.recon.exceptiontype.MyCustomException
     */
    public static void writeResponse(HttpServletResponse response, String responseToWrite) throws MyCustomException {

        PrintWriter out = null;
        try {

            out = response.getWriter();
            out.write(responseToWrite);
            out.flush();
            response.flushBuffer();

        } catch (IOException ex) {
            throw new MyCustomException("Error writing response to client", ErrorCode.COMMUNICATION_ERR, ex.getMessage(), ErrorCategory.SERVER_ERR_TYPE);

        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    public static void printRequesterHeaderInfo(HttpServletRequest req) throws IOException {

        Enumeration<String> headerNames = req.getHeaderNames();

        while (headerNames.hasMoreElements()) {

            String headerName = headerNames.nextElement();
            logger.debug(">>> header name  : " + headerName);

            Enumeration<String> headers = req.getHeaders(headerName);
            while (headers.hasMoreElements()) {
                String headerValue = headers.nextElement();
                logger.debug(">>> header value : " + headerValue);
            }
            logger.debug(">>> -------------------------------------");
        }
    }

    public static void logRequestInfo(HttpServletRequest request) {

        logger.debug(">>> Request Content-type   : " + request.getContentType());
        logger.debug(">>> Request Context-path   : " + request.getContextPath());
        logger.debug(">>> Request Content-type   : " + request.getContentType());
        logger.debug(">>> Request Content-length : " + request.getContentLength());
        logger.debug(">>> Request Protocol       : " + request.getProtocol());
        logger.debug(">>> Request PathInfo       : " + request.getPathInfo());
        logger.debug(">>> Request Remote Address : " + request.getRemoteAddr());
        logger.debug(">>> Request Remote Port    : " + request.getRemotePort());
        logger.debug(">>> Request Server name    : " + request.getServerName());
        logger.debug(">>> Request Querystring    : " + request.getQueryString());
        logger.debug(">>> Request URL            : " + request.getRequestURL().toString());
        logger.debug(">>> Request URI            : " + request.getRequestURI());
    }

    /**
     * Build the report details object to be saved in DB
     *
     * @param fileDetailsJsonString
     * @return
     * @throws com.namaraka.recon.exceptiontype.MyCustomException
     */
    public static ReportDetails recieveReportFileDetailsOLD(String fileDetailsJsonString) throws MyCustomException {

        logger.info(GeneralUtils.toPrettyJsonFormat(fileDetailsJsonString));

        ReportDetails reportFileDetails = new ReportDetails();

        Map<String, String> fileDetails = GeneralUtils.convertFromJson(fileDetailsJsonString, stringMapType);

        for (String key : fileDetails.keySet()) {
            //logger.debug("key - " + key + " : " + "value - " + fileDetails.get(key));
            buildReportDetailsInstanceHelper(reportFileDetails, key, fileDetails.get(key));
        }

        if (reportFileDetails.getFileReconProgress() == FileReconProgressEnum.NEW) {
            reportFileDetails.setFileReconProgress(FileReconProgressEnum.INPROGRESS);//set file processing to IN-PROGRESS
        } else if (reportFileDetails.getFileReconProgress() == FileReconProgressEnum.EDITED) { //edited file

        }

        String reconTitle = reportFileDetails.getReconTitle();
        String folderName = buildUniqueNameHelper(reconTitle);
        reportFileDetails.setReconFolderName(folderName);

        reportFileDetails.setInvokedByURL(Boolean.TRUE); // a user has invoked this by way of a URL

        String absoluteFilePath = GlobalAttributes.READ_DIR + reportFileDetails.getAbsoluteFilePath(); //using apparoach of mounting folders

        Workbook workBook = FileUtilities.createWorkBook(absoluteFilePath);
        final Sheet sheet = FileUtilities.getSheetAtIndex(workBook, GlobalAttributes.WORKSHEET_INDEX);
        reportFileDetails.setSheet(sheet);

        int numberOfRecords = FileUtilities.getNumRecordsMinusHeaders(sheet);
        reportFileDetails.setNumberOfRecords(numberOfRecords);

        return reportFileDetails;
    }

    public static ReportDetails recieveReportFileDetails(String fileDetailsJsonString) throws MyCustomException {

        logger.info(GeneralUtils.toPrettyJsonFormat(fileDetailsJsonString));

        ReportDetails reportFileDetails = new ReportDetails();

        Map<String, String> fileDetails = GeneralUtils.convertFromJson(fileDetailsJsonString, stringMapType);

        for (String key : fileDetails.keySet()) {
            buildReportDetailsInstanceHelper(reportFileDetails, key, fileDetails.get(key));
        }

        if (reportFileDetails.getFileReconProgress() == FileReconProgressEnum.NEW) {
            reportFileDetails.setFileReconProgress(FileReconProgressEnum.INPROGRESS);//set file processing to IN-PROGRESS
        } else if (reportFileDetails.getFileReconProgress() == FileReconProgressEnum.EDITED) { //edited file

        }

        String reconTitle = reportFileDetails.getReconTitle();
        String folderName = buildUniqueNameHelper(reconTitle);
        reportFileDetails.setReconFolderName(folderName);

        return reportFileDetails;
    }

    /**
     * Build a unique name from the given text replacing spaces with the
     * character '_'
     *
     * @param textToBuildNameFrom
     * @return
     */
    public static String buildUniqueNameHelper(String textToBuildNameFrom) {

        String formattedReconTitle = GeneralUtils.replaceSpaces(textToBuildNameFrom, "_");
        String dateTimeNow = GeneralUtils.getDateTimeNow("Africa/Kampala", "ddMMyyyyHHmmss");

        String uniqueName = formattedReconTitle + dateTimeNow;

        return uniqueName;
    }

    //do we really need to return ReportDetails - we can just build object minus returning it    
    /**
     *
     * @param reportDetails
     * @param jsonKey
     * @param jsonValue
     * @return
     */
    private static ReportDetails buildReportDetailsInstanceHelper(ReportDetails reportDetails, String jsonKey, String jsonValue) {

        ReportDetailsJsonKeys reportDetailsEnum = ReportDetailsJsonKeys.convertToEnum(jsonKey);

        switch (reportDetailsEnum) {

            case REPORT_TYPE:
                reportDetails.setReportType(ReportType.convertToEnum(jsonValue));
                break;

            case LINK_TYPE:
                reportDetails.setLinkType(LinkType.convertToEnum(jsonValue));
                break;

            case REPORT_TITLE:
                reportDetails.setReportTitle(jsonValue);
                break;

            case FILENAME:
                reportDetails.setFileName(jsonValue);
                break;

            case RECON_FILEID:
                reportDetails.setFileID(jsonValue);
                break;

            case RECON_GROUPID:
                reportDetails.setReconGroupID(jsonValue);
                break;

            case IS_MASTER:
                reportDetails.setIsMaster(Boolean.valueOf(jsonValue));
                break;

            case HAS_STATUS:

                boolean hasStatus = false;
                if (!(jsonValue == null || jsonValue.trim().isEmpty())) {
                    hasStatus = Boolean.valueOf(jsonValue);
                }

                reportDetails.setHasStatus(hasStatus);
                break;

            case ID_COL_NAME:
                reportDetails.setIDColumnName(jsonValue);
                break;

            case LINK_ID_COL_NAME:
                reportDetails.setLinkIDColumnName(jsonValue);
                break;

            case STATUS_COL_NAME:
                reportDetails.setStatusColumnName(jsonValue);
                break;

            case SUCCESS_VALUE:
                reportDetails.setSuccessStatusValue(jsonValue);
                break;

            case FAILED_VALUE:
                reportDetails.setFailedStatusValue(jsonValue);
                break;

            case PENDING_VALUE:
                reportDetails.setPendingStatusValue(jsonValue);
                break;

            case FILE_RECON_PROGRESS:
                reportDetails.setFileReconProgress(FileReconProgressEnum.convertToEnum(jsonValue));
                break;

            case RECON_TITLE:
                reportDetails.setReconTitle(jsonValue);
                break;

//            case FILEPATH:
//                reportDetails.setAbsCompleteFilePath(jsonValue);
//                break;
            case AMOUNT_COL_NAME:
                reportDetails.setAmountColumnName(jsonValue);
                break;

            case TXN_DESCRIPTION:
                reportDetails.setDescriptionColumnName(jsonValue);
                break;

            default:
                logger.warn("unknown fileDetails jsonKey: " + jsonKey);
        }

        return reportDetails;
    }

    /**
     *
     * @param reconGroupID
     * @param isToBeReconciled
     * @return
     * @throws com.namaraka.recon.exceptiontype.MyCustomException
     */
    public static boolean isFileProcessingDone(String reconGroupID, boolean isToBeReconciled) throws MyCustomException {

        List<FileReconProgressEnum> fileProgressList = DBManager.fetchOnlyColumn(ReportDetails.class, "fileReconProgress", "reconGroupID", reconGroupID, "isToBeReconciled", isToBeReconciled);

        logger.info("TotalReconProgresses for recon: " + reconGroupID + " - " + fileProgressList.toString());

        Set<FileReconProgressEnum> reconProgressSet = GeneralUtils.convertToSet(fileProgressList);

        if ((reconProgressSet.size() == 1) && (reconProgressSet.contains(FileReconProgressEnum.COMPLETED))) {

            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    /**
     *
     * @param reconDetails
     * @param startedByURL
     * @return
     * @throws MyCustomException
     * @throws java.io.IOException
     */
    public static boolean startRecon(ReconciliationDetails reconDetails, boolean startedByURL) throws MyCustomException, IOException {

        boolean reconStarted;

        String reconGroupID = reconDetails.getReconGroupID();

        synchronized (GlobalAttributes.writeMutexObjects.get(reconGroupID)) {

            reconDetails = GeneralUtils.retrieveReconObjectFromDBHelper(reconGroupID);
            ReconStatus totalReconProgress = reconDetails.getReconStatus();

            logger.info("Recon progress inside Synchronised: " + totalReconProgress);
            logger.info("Started by URL  :: " + startedByURL);

            //recon started by the last file to be read - progress MUST have already been map-to (INVOKED) by the call to /reconcile URL
//            if ((!startedByURL && (totalReconProgress != ReconStatus.INVOKED)) || totalReconProgress == ReconStatus.COMPLETED) {
//
//                logger.info("Returning False -> startedByURL :: " + startedByURL + " - totalReconProgress :: " + totalReconProgress);
//                logger.info("To pass, reconProgress shouldn't be COMPLETED and if startedByURL == false, totalReconProgress shouldn't be INVOKED");
//
//                return false;
//            }
            // all files should be done - COMPLETED
            boolean isFileProcessingDone = GeneralUtils.isFileProcessingDone(reconGroupID, Boolean.TRUE);
            reconStarted = writeFileHelper(isFileProcessingDone, reconDetails);

            if (reconStarted) {
                updateReconProgress(reconDetails, ReconStatus.COMPLETED);
            }
        }
        return reconStarted;
    }

    /**
     * Update the DB recon progress
     *
     * @param reconDetails
     * @param reconProgress
     * @return
     */
    public static ReconciliationDetails updateReconProgress(ReconciliationDetails reconDetails, ReconStatus reconProgress) {

        reconDetails.setReconStatus(reconProgress);
        DBManager.updateDatabaseModel(reconDetails);

        return reconDetails;
    }

    /**
     * Get recon progress at any one time
     *
     * @param reconGroupID
     * @return
     * @throws MyCustomException
     */
    public static ReconStatus getReconProgressFromDB(String reconGroupID) throws MyCustomException {

        ReconStatus reconProgress = ReconStatus.UNKNOWN;
        List<ReconStatus> reconDetailsList = DBManager.fetchOnlyColumn(ReconciliationDetails.class, "reconStatus", "reconGroupID", reconGroupID);

        if (!reconDetailsList.isEmpty()) {
            reconProgress = reconDetailsList.get(0);
        } else {
            logger.warn("Recon Group corresponding to ID: " + reconGroupID + " doesn't exist in the DB");
        }

        return reconProgress;
    }

    /**
     *
     * @param reconDetails
     * @param onlyWriteExceptions
     * @return
     * @throws MyCustomException
     */
    private static boolean writeFileHelper(boolean isFileProcessingDone, ReconciliationDetails reconDetails) throws MyCustomException, IOException {

        if (isFileProcessingDone) {

            ReconciliationDetails reconciliationDetails = updateReconProgress(reconDetails, ReconStatus.INPROGRESS);

            FileWriter fileWriter = new FileWriter(reconciliationDetails);
            fileWriter.writeRecordsToFile();

            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    /**
     *
     * @param reportDetails
     * @param fileIDPropertyName
     * @param fileReconProgress
     */
    public static void setFileReconProgressHelper(ReportDetails reportDetails, String fileIDPropertyName, FileReconProgressEnum fileReconProgress) {

        //improve this logic here - there is too much openning and closing of DB unnecessarily - we can put this in a command pattern
        List<ReportDetails> reportDetailsDB = DBManager.getRecordsEqualToPropertyValue(ReportDetails.class, fileIDPropertyName, reportDetails.getFileID());
        ReportDetails reportFromDB = reportDetailsDB.get(0);
        reportFromDB.setFileReconProgress(fileReconProgress);
        DBManager.updateDatabaseModel(reportFromDB);

        logger.info("ReconProgress for fileID: " + reportDetails.getFileID() + " UPDATED to: " + fileReconProgress.getValue());
    }

    /**
     * Destroy global maps belonging to a recon group
     *
     * @param reconID
     */
    public static void removeReconDetailsFromGlobalMaps(String reconID) {

        GlobalAttributes.fileReadProgressIndicator.remove(reconID);
        GlobalAttributes.fileWriteProgressIndicator.remove(reconID);
        GlobalAttributes.totalUnreconciledRecords.remove(reconID);
        //GlobalAttributes.exceptionsCount.remove(reconID);
        GlobalAttributes.totalRecordsToBeRead.remove(reconID);

        GlobalAttributes.linkerFileRead.remove(reconID);
        GlobalAttributes.linkedFileRead.remove(reconID);
        GlobalAttributes.linkedFileReconciled.remove(reconID);
        GlobalAttributes.globalReconFileDetails.remove(reconID);
        GlobalAttributes.reconDetailsStore.remove(reconID);
        GlobalAttributes.readMutexObjects.remove(reconID);
        GlobalAttributes.writeMutexObjects.remove(reconID);

        //also delete from the DB tables
    }

    public static void addReconDetailsToGlobalMaps(String reconID, ReportDetails reportFileDetails) {

        GlobalAttributes.fileReadProgressIndicator.put(reconID, new AtomicInteger(0));
        GlobalAttributes.fileWriteProgressIndicator.put(reconID, new AtomicInteger(0));
        //GlobalAttributes.totalReconciledToBeWritten.put(reconID, new AtomicInteger(0));
        //GlobalAttributes.totalNumberRecordsIterated.put(reconID, new AtomicInteger(0));
        //GlobalAttributes.exceptionsCount.put(reconID, new AtomicInteger(0));
        GlobalAttributes.totalUnreconciledRecords.put(reconID, new AtomicInteger(reportFileDetails.getNumberOfRecords()));
        GlobalAttributes.totalRecordsToBeRead.put(reconID, new AtomicInteger(reportFileDetails.getCompoundedNumberOfRecords()));

        //GlobalAttributes.linkerFileRead.put(reconID, Boolean.FALSE);
        //GlobalAttributes.linkedFileRead.put(reconID, Boolean.FALSE);
        //GlobalAttributes.linkedFileReconciled.put(reconID, Boolean.FALSE);
        GlobalAttributes.globalReconFileDetails.put(reconID, reportFileDetails);
        GlobalAttributes.readMutexObjects.put(reconID, new ReadMutexClass(reconID));
        GlobalAttributes.writeMutexObjects.put(reconID, new WriteMutexClass(reconID));
    }

    /**
     *
     * @param hashMap
     * @param reconID
     */
    public static void initGlobalMap(ConcurrentHashMap<String, AtomicInteger> hashMap, String reconID) {

        if (hashMap.get(reconID) == null) {
            hashMap.put(reconID, new AtomicInteger(0));
        }
    }

    /**
     *
     * @param reconGroupID
     * @return
     */
    public static Collection<ReconTransactionsTable> getAllDBRecordsInReconGroup(String reconGroupID) {

        List<ReportDetails> reportFileDetailsKeyIDList = DBManager.fetchOnlyColumn(ReportDetails.class, "id", "reconGroupID", reconGroupID);
        Collection<ReconTransactionsTable> recordsList = DBManager.retrieveAllDatabaseRecords(ReconTransactionsTable.class, "reportFileKeyID", reportFileDetailsKeyIDList);

        return recordsList;
    }

    public static void populateExcelFileHeadings(Collection<String> columnHeaders, Collection<String> displayNames, Row row, CellStyle headerStyle) {

        //add first headings to the excel file
        int column = 0;
        for (String columnHeader : columnHeaders) {

            Cell cell = row.createCell(column);
            cell.setCellValue(columnHeader);
            cell.setCellStyle(headerStyle);

            column++;
        }

        //extra fields of displaynames for statuses
        for (String displayName : displayNames) {

            Cell cell = row.createCell(column);
            cell.setCellValue(displayName);
            cell.setCellStyle(headerStyle);

            column++;
        }
    }

    /**
     *
     * @param columnHeaders
     * @param row
     * @param headerStyle
     */
    public static void populateExcelFileHeadings(Collection<String> columnHeaders, Row row, CellStyle headerStyle) {

        //add the first headings to the excel file
        int column = 0;
        for (String columnHeader : columnHeaders) {

            Cell cell = row.createCell(column);
            cell.setCellValue(columnHeader);
            cell.setCellStyle(headerStyle);

            column++;
        }
    }

    /**
     *
     * @param columnHeaders
     * @param sheet
     * @param headerStyle
     */
    public static void populateExcelFileHeadings(Collection<String> columnHeaders, Sheet sheet, CellStyle headerStyle) {

        Row row = sheet.createRow(0); //File headings will all be at zero index

        int column = 0;
        for (String columnHeader : columnHeaders) {

            Cell cell = row.createCell(column);
            cell.setCellValue(columnHeader);
            cell.setCellStyle(headerStyle);

            column++;
        }
    }

    /**
     *
     * @param rowIterator
     * @return
     */
    public static List<String> getCellValuesHelper(Iterator<Row> rowIterator) {

        //if this is the very first row with data in this report, then get all the headers
        //we will use them as the JSON Id-names for our JSON data
        //For each row, iterate through each columns        
        List<String> rowValues = new ArrayList<>();

        Row row = rowIterator.next();

        if (row == null) {
            logger.warn("found a row that is NULL  !!");
            return rowValues; //0 size
        }

        if (row.getRowNum() == GlobalAttributes.FIRST_ROW_TO_GET) {
            GlobalAttributes.NUM_OF_HEADER_COLS = row.getPhysicalNumberOfCells(); //gets the number of defined cells 
        }

        for (int cellNum = 0; cellNum < GlobalAttributes.NUM_OF_HEADER_COLS; cellNum++) {

            Object headerNameOrValue;

            Cell cell = row.getCell(cellNum, Row.RETURN_BLANK_AS_NULL);

            if (cell == null) {

                logger.warn(">>>>>> Found cell value NULL, will make cell empty");
                headerNameOrValue = "";

            } else {

                //int index = cell.getColumnIndex();
                //CellReference cellRef = new CellReference(row.getRowNum(), cell.getColumnIndex());
                cell.setCellType(Cell.CELL_TYPE_STRING); // this removes all cell formating and makes everything text - might want to remove it later especially if we want to know cell types

                //System.out.print(cellRef.formatAsString());
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
                            //headerNameOrValue = cell.getNumericCellValue();
                            headerNameOrValue = BigDecimal.valueOf(cell.getNumericCellValue()).toPlainString();

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
            }

            String stringedValue = String.valueOf(headerNameOrValue);

            if (stringedValue == null || stringedValue.trim().isEmpty()) {
                //stringedValue = "-"; //just put a place holder
                stringedValue = "";
            }

            rowValues.add(String.valueOf(stringedValue));
        }

        return rowValues;
    }

    /**
     *
     * @param rowHeaderNames
     * @param rowHeaderValues
     * @param reportFileDetails
     * @param tempSession
     * @return ReconTransactionsTable
     */
    public static TxnRecordIF addFileFieldsHelper(List<String> rowHeaderNames, List<String> rowHeaderValues, ReportDetails reportFileDetails, StatelessSession tempSession) {

        String fileID = reportFileDetails.getFileID();
        String reconID = reportFileDetails.getReconGroupID();
        ReportType reportType = reportFileDetails.getReportType();
        LinkType linkType = reportFileDetails.getLinkType();

        //add the normal fields
        String statusColumnLabelName = reportFileDetails.getStatusColumnName().trim();
        String IDcolumnLabelName = reportFileDetails.getIDColumnName().trim();
        String linkIDcolumnLabelName = reportFileDetails.getLinkIDColumnName().trim();
        String amountColumnName = reportFileDetails.getAmountColumnName().trim();
        String descriptionColumnName = reportFileDetails.getDescriptionColumnName().trim();

        TxnRecordIF reconTransTableRow;

        if (linkType == LinkType.LINKER) {
            reconTransTableRow = new Linker();
        } else if (linkType == LinkType.LINKED) {
            reconTransTableRow = new Linked();
        } else {
            reconTransTableRow = new ReconTransactionsTable();
        }

        String IDColumnValue = null;
        String linkIDColumnValue = null;
        String statusColumnValue = null;
        String amountColumnValue = null;
        String descriptionColumnValue = null;

        int i = 0;
        for (String rowHeaderName : rowHeaderNames) {

            if (rowHeaderName != null) {

                if (rowHeaderName.equalsIgnoreCase(IDcolumnLabelName)) {
                    IDColumnValue = rowHeaderValues.get(i).trim();
                } else if (rowHeaderName.equalsIgnoreCase(statusColumnLabelName)) {
                    statusColumnValue = rowHeaderValues.get(i).trim();
                } else if (rowHeaderName.equalsIgnoreCase(amountColumnName)) {
                    amountColumnValue = rowHeaderValues.get(i).trim();
                } else if (rowHeaderName.equalsIgnoreCase(descriptionColumnName)) {
                    descriptionColumnValue = rowHeaderValues.get(i).trim();
                } else if (rowHeaderName.equalsIgnoreCase(linkIDcolumnLabelName)) {
                    linkIDColumnValue = rowHeaderValues.get(i).trim();
                }

            } else {
                logger.error("rowHeaderName is NULL.. might cause Recon inconsistences..");
            }
            i++;
        }

        String specialIDValue = null;
        CenteServiceType serviceType = null;

        if (reportType == ReportType.EQUINOX_SPECIAL) {

            serviceType = retrieveServiceType(descriptionColumnValue);

            specialIDValue = extractSpecialIDValue(reportType, IDColumnValue);
            IDColumnValue = extractEquinoxIDFromDescription(serviceType, descriptionColumnValue);
            if (IDColumnValue == null || IDColumnValue.isEmpty()) {
                IDColumnValue = specialIDValue;
            }

        } else if (reportType == ReportType.ELMA) {

            serviceType = retrieveServiceType(descriptionColumnValue);

            specialIDValue = extractSpecialIDValue(reportType, IDColumnValue);
            if (IDColumnValue == null || IDColumnValue.isEmpty()) {
                IDColumnValue = specialIDValue;
            }
        }

        reconTransTableRow.setReportFileKeyID(reportFileDetails.getFileID()); //ID overriden by last file to be read
        reconTransTableRow.setReconGroupID(reconID);

        reconTransTableRow.setCenteServiceType(serviceType);

        reconTransTableRow.setAmount(amountColumnValue);
        reconTransTableRow.setTransactionDescription(descriptionColumnValue);

        reconTransTableRow.setIDValue(IDColumnValue);
        reconTransTableRow.setSpecialIDValue(specialIDValue);
        reconTransTableRow.setLinkIDValue(linkIDColumnValue);

        /*if (linkType == LinkType.LINKED) {

         Criteria criteria = tempSession.createCriteria(Linker.class);
         criteria.add(Restrictions.eq("linkIDValue", IDColumnValue));
         List<?> linkerTransRows = criteria.list();

         if (!linkerTransRows.isEmpty()) {

         Linker transaction = (Linker) linkerTransRows.get(0);
                
         reconTransTableRow.setLinkIDValue(IDColumnValue);
         IDColumnValue = transaction.getIDValue();
         reconTransTableRow.setIDValue(IDColumnValue);
                
         }else {                
         reconTransTableRow.setLinkIDValue(IDColumnValue);
         //reconTransTableRow.setIDValue(null);
         }
         }*/
        String status = assignStatus(statusColumnValue, IDColumnValue, amountColumnValue, reportFileDetails);

        logger.info("status assigned: " + status + " FileID: " + fileID);

        Map<String, String> fileStatusValueMap = new HashMap<>();
        fileStatusValueMap.put(fileID, status);
        String fileStatusValue = GeneralUtils.convertToJson(fileStatusValueMap, stringMapType);

        reconTransTableRow.setTransactionStatuses(fileStatusValue); //set status value - useful if we are inserting and not updating
        reconTransTableRow.setTempTransStatusHolder(statusColumnValue); //sets the temporary status value - useful if we are updating //we could also setup a temp fileID if need be

        reconTransTableRow.setCellValues(new ArrayList<>(rowHeaderValues));

        Map<String, String> fileRecordsDetailsMap = new HashMap<>();

        for (int x = 0; x < rowHeaderNames.size(); x++) {
            fileRecordsDetailsMap.put(rowHeaderNames.get(x), rowHeaderValues.get(x));
        }

        Map<String, Map<String, String>> mappedFileRecords = new HashMap<>();
        mappedFileRecords.put(fileID, fileRecordsDetailsMap);

        String fileRecordsDetails = GeneralUtils.convertToJson(mappedFileRecords, mapInMapType);
        reconTransTableRow.setFileRecordsDetails(fileRecordsDetails);

        if (reportFileDetails.isIsMaster()) {

            reconTransTableRow.setIsRecordExistinMasterFile(Boolean.TRUE);

            //now add comments && isReconStatusStatusInAllFiles
            //reconTransTableRow = setRemainingMasterFieldsHelper(reconTransTableRow, ReconFileStatuses.NOK);
        }

        //create some JSON here
        return reconTransTableRow;
    }

    /**
     *
     * @param IDColumnValue
     * @param descriptionColumnValue
     * @param linkIDColumnValue
     * @param reportType
     * @return
     */
    public static String retrieveID(String IDColumnValue, String descriptionColumnValue, String linkIDColumnValue, ReportType reportType) {

        String specialIDValue = null;
        CenteServiceType serviceType = null;

        if (reportType == ReportType.EQUINOX_SPECIAL) {

            serviceType = retrieveServiceType(descriptionColumnValue);

            specialIDValue = extractSpecialIDValue(reportType, IDColumnValue);
            IDColumnValue = extractEquinoxIDFromDescription(serviceType, descriptionColumnValue);
            if (IDColumnValue == null || IDColumnValue.isEmpty()) {
                IDColumnValue = specialIDValue;
            }

        } else if (reportType == ReportType.ELMA) {

            serviceType = retrieveServiceType(descriptionColumnValue);

            specialIDValue = extractSpecialIDValue(reportType, IDColumnValue);
            if (IDColumnValue == null || IDColumnValue.isEmpty()) {
                IDColumnValue = specialIDValue;
            }
        }

        logger.debug("IDColumnValue     : " + IDColumnValue);
        logger.debug("specialIDValue    : " + specialIDValue);
        logger.debug("linkIDColumnValue : " + linkIDColumnValue);
        logger.debug("Cente serviceType : " + serviceType);

        return IDColumnValue;

    }

    public static TxnRecordIF assignIDfromLinker(TxnRecordIF reconTransTableRow, StatelessSession tempSession) {

        String IDColumnValue = reconTransTableRow.getIDValue();

        Criteria criteria = tempSession.createCriteria(Linker.class);
        criteria.add(Restrictions.eq("linkIDValue", IDColumnValue));
        List<?> linkerTransRows = criteria.list();

        if (!linkerTransRows.isEmpty()) {

            Linker transaction = (Linker) linkerTransRows.get(0);

            reconTransTableRow.setLinkIDValue(IDColumnValue);
            IDColumnValue = transaction.getIDValue();
            reconTransTableRow.setIDValue(IDColumnValue);

        } else {
            reconTransTableRow.setLinkIDValue(IDColumnValue);
            //reconTransTableRow.setIDValue(null);
        }
        return reconTransTableRow;
    }

    /**
     *
     * @param rowHeaderNames
     * @param rowHeaderValues
     * @param reconTransId
     * @param reportDetails
     * @return
     */
    public static EquinoxDetails buildEquinoxDetailsHelper(Collection<String> rowHeaderNames, List<String> rowHeaderValues, String reconTransId, ReportDetails reportDetails) {

        EquinoxDetails equinoxDetails = new EquinoxDetails();
        Map<String, String> allFileColumnsMap = new HashMap<>();
        String description = null;

        int i = 0;
        for (String rowHeaderName : rowHeaderNames) {

            EquinoxField eqionoxColLabel = EquinoxField.convertToEnum(rowHeaderName);
            String columnValue = rowHeaderValues.get(i);

            allFileColumnsMap.put(rowHeaderName, columnValue);

            switch (eqionoxColLabel) {

                case ACCOUNT_ID:
                    equinoxDetails.setAccountNumber(columnValue);
                    break;

                case AMOUNT:
                    equinoxDetails.setAmount(columnValue);
                    break;

                case DESCRIPTION:
                    equinoxDetails.setEquinoxDescription(columnValue);
                    description = columnValue;
                    break;

                case POST_DATE:
                    equinoxDetails.setPostingDateTime(columnValue);
                    break;

                case TRACER_NO:
                    equinoxDetails.setOriginalTracerNo(columnValue);
                    break;

                default:
                    break;
            }

            i++;
        }

        CenteServiceType serviceType = retrieveServiceType(description);
        equinoxDetails.setCenteServiceType(serviceType);

        String allFileColumns = GeneralUtils.convertToJson(allFileColumnsMap, stringMapType);
        equinoxDetails.setAllFileColumns(allFileColumns);

        equinoxDetails.setReconTransID(reconTransId);
        equinoxDetails.setReportDetails(reportDetails);

        return equinoxDetails;
    }

    private static CenteServiceType retrieveServiceType(String description) {

        CenteServiceType serviceType;

        description = description.toUpperCase();

        if (description.contains(CenteServiceType.AIRTIME.getValue())) {
            serviceType = CenteServiceType.AIRTIME;
        } else if (description.contains(CenteServiceType.UMEME.getValue())) {
            serviceType = CenteServiceType.UMEME;
        } else if (description.contains(CenteServiceType.NWSC.getValue())) {
            serviceType = CenteServiceType.NWSC;
        } else if (description.contains(CenteServiceType.DSTV.getValue())) {
            serviceType = CenteServiceType.DSTV;
        } else if (description.contains(CenteServiceType.PULL.getValue())) {
            serviceType = CenteServiceType.PULL;
        } else if (description.contains(CenteServiceType.PUSH.getValue())) {
            serviceType = CenteServiceType.PUSH;
        } else {
            serviceType = null;
        }

        return serviceType;
    }

    private GeneralUtils.ServiceTypeIDValue buildEquinoxServiceTypeIDValue(String description) {

        if (description == null || description.isEmpty()) {
            return new GeneralUtils.ServiceTypeIDValue(null, null);
        }

        CenteServiceType serviceType = retrieveServiceType(description);
        String idValue = extractEquinoxIDFromDescription(serviceType, description);

        return new GeneralUtils.ServiceTypeIDValue(serviceType, idValue);
    }

    private static String extractSpecialIDValue(ReportType reportType, String IDColumnValue) {

        String specialID = null;

        switch (reportType) {

            case EQUINOX_SPECIAL:

                int origTracerNoIndex = IDColumnValue.indexOf(GlobalAttributes.ORIGINAL_TRACER_NO_DELIMETER);
                if (origTracerNoIndex < 0) {
                    break;
                }
                specialID = IDColumnValue.substring(GlobalAttributes.ID_BEGIN_INDEX, origTracerNoIndex); // Original tracer no. are in the form 0901-27Apr2015110427 but we need the first  4 characters
                //statusColumnValue = "Equinox status"; //Equinox reports have no status columns

                break;

            case ELMA:

                int beginIndex = IDColumnValue.length() - GlobalAttributes.ELMA_ID_BEGIN_INDEX;
                if (beginIndex < 0) {
                    break;
                }
                specialID = IDColumnValue.substring(beginIndex); // Elma IDs are usually in the form 990901, we need only the last 4 digits
                break;

            default:
                break;
        }

        return specialID;
    }

    private class ServiceTypeIDValue {

        private final CenteServiceType serviceType;
        private final String idValue;

        public ServiceTypeIDValue(CenteServiceType serviceType, String idValue) {
            this.serviceType = serviceType;
            this.idValue = idValue;
        }

        public CenteServiceType getServiceType() {
            return serviceType;
        }

        public String getIdValue() {
            return idValue;
        }

    }

    private static String extractEquinoxIDFromDescription(CenteServiceType serviceType, String description) {

        if (serviceType == null) {
            return null;
        }

        String idValue = null;

        switch (serviceType) {

            case AIRTIME:  // AIRTIME|************||**********
                idValue = null;
                break;

            case DSTV:  // DSTV|************|1013531645-990901|**********                 
                idValue = ((description.trim().toUpperCase().split("\\|")[2]).split("-"))[1].trim();
                break;

            case NWSC:  // 10718769-937918NWSC
                idValue = ((description.trim().toUpperCase().split("-")[1]).split("NWSC"))[0].trim();
                break;

            case PULL:  // PULL| phone number |256752544391|270645
                idValue = description.trim().toUpperCase().split("\\|")[3].trim();
                break;

            case PUSH:  // PUSH| phone number |256700106832|187957864
                idValue = description.trim().toUpperCase().split("\\|")[3].trim();
                break;

            case UMEME: // UMEME 106040 
                idValue = description.trim().toUpperCase().split(" ")[1].trim();
                break;

            default:
                break;
        }

        return idValue;
    }

    /**
     * Get the enumerated status {OK, NOK, UNKNOWN} from the statusColVal &&
     * idColVal
     *
     * @param statusColumnValue
     * @param IDColumnValue
     * @param amountColumnValue
     * @param reportFileDetails
     * @return
     */
    public static String assignStatus(String statusColumnValue, String IDColumnValue, String amountColumnValue, ReportDetails reportFileDetails) {

        ReportType reportType = reportFileDetails.getReportType();
        boolean hasStatus = reportFileDetails.isHasStatus();
        String status;

        //::::::Check ID:::::://
        if (IDColumnValue == null || IDColumnValue.isEmpty()) { //any txn without ID is considered an exception

            status = TransactionState.MISSING_ID.getValue();
            return status;

        }
        //:::::check Amount::::://
        if (amountColumnValue == null || amountColumnValue.isEmpty()) {//Elma and Equinox reports must have an amount field

            if (reportType == ReportType.ELMA || reportType == ReportType.EQUINOX_SPECIAL) {
                status = TransactionState.MISSING_AMOUNT.getValue();
                logger.error("Report file of type: " + reportType + " has NO amount value");
                return status;
            }
        }

        //::::::check status:::::://
        if (statusColumnValue == null || statusColumnValue.isEmpty()) {

            /*if(hasStatus){
             status = TransactionState.MISSING_STATUS.getValue();
             }else{
             status = TransactionState.SUCCESS.getValue();
             }*/
            if (reportType == ReportType.EQUINOX_SPECIAL) {
                status = TransactionState.SUCCESS.getValue();
            } //equinox report doesn't have a status column - Elma has it but txns like PULL and PUSH have this field empty
            else if (reportType == ReportType.ELMA) {
                status = TransactionState.SUCCESS.getValue();
            } else if (reportType == ReportType.EQUINOX) {
                status = TransactionState.SUCCESS.getValue();
            } else {

                status = TransactionState.MISSING_STATUS.getValue();
            }
            return status;

        } else if (statusColumnValue.equalsIgnoreCase(reportFileDetails.getSuccessStatusValue())) {
            status = TransactionState.SUCCESS.getValue();
        } else if (statusColumnValue.equalsIgnoreCase(reportFileDetails.getFailedStatusValue())) {
            status = TransactionState.FAIL.getValue();
        } else if (statusColumnValue.equalsIgnoreCase(reportFileDetails.getPendingStatusValue())) {
            status = TransactionState.PENDING.getValue();
        } else {
            status = TransactionState.UNKNOWN_STATE.getValue();
        }

        return status;
    }

    /**
     * Get the enumerated status {OK, NOK, UNKNOWN} from the statusColVal &&
     * idColVal
     *
     * @param statusColumnValue
     * @param IDColumnValue
     * @param amountColumnValue
     * @param reportFileDetails
     * @return
     */
    public static TransactionState assignStatusShortCode(String statusColumnValue, String IDColumnValue, String amountColumnValue, ReportDetails reportFileDetails) {

        ReportType reportType = reportFileDetails.getReportType();
        boolean hasStatus = reportFileDetails.isHasStatus();

        TransactionState status;

        //::::::Check ID:::::://
        if (IDColumnValue == null || IDColumnValue.isEmpty()) { //any txn without ID is considered an exception

            status = TransactionState.MISSING_ID;
            return status;

        }
        //:::::check Amount::::://
        if (amountColumnValue == null || amountColumnValue.isEmpty()) {//Elma and Equinox reports must have an amount field

            status = TransactionState.MISSING_AMOUNT;
            return status;

//            if (reportType == ReportType.ELMA || reportType == ReportType.EQUINOX_SPECIAL) {
//                status = TransactionState.MISSING_AMOUNT.getShortCodeValue();
//                logger.error("Report file of type: " + reportType + " has NO amount value");
//                return status;
//            }
        }

        //::::::check status:::::://
        if (statusColumnValue == null || statusColumnValue.isEmpty()) {

            /*if(hasStatus){
             status = TransactionState.MISSING_STATUS.getValue();
             }else{
             status = TransactionState.SUCCESS.getValue();
             }*/
            if (reportType == ReportType.EQUINOX_SPECIAL) {
                status = TransactionState.SUCCESS;
            } //equinox report doesn't have a status column - Elma has it but txns like PULL and PUSH have this field empty
            else if (reportType == ReportType.ELMA) {
                status = TransactionState.SUCCESS;
            } else if (reportType == ReportType.EQUINOX) {
                status = TransactionState.SUCCESS;
            } else {

                status = TransactionState.MISSING_STATUS;
            }
            return status;

        } else if (statusColumnValue.equalsIgnoreCase(reportFileDetails.getSuccessStatusValue())) {
            status = TransactionState.SUCCESS;
        } else if (statusColumnValue.equalsIgnoreCase(reportFileDetails.getFailedStatusValue())) {
            status = TransactionState.FAIL;
        } else if (statusColumnValue.equalsIgnoreCase(reportFileDetails.getPendingStatusValue())) {
            status = TransactionState.PENDING;
        } else {
            status = TransactionState.UNKNOWN_STATE;
        }

        return status;
    }

    /**
     *
     * @param transStatus
     * @return
     */
    public static boolean isFailedOrSuccessful(TransactionState transStatus) {

        boolean isFailedOrSuccessful;

        switch (transStatus) {

            case FAIL:
                isFailedOrSuccessful = Boolean.TRUE;
                break;

            case SUCCESS:
                isFailedOrSuccessful = Boolean.TRUE;
                break;

            default:
                isFailedOrSuccessful = Boolean.FALSE;
                break;
        }
        return isFailedOrSuccessful;
    }

    private static FileProcessDeterminants buildFileProcessDetermHelper(ReportDetails reportFileDetails) {

        boolean isMaster = reportFileDetails.isIsMaster();
        boolean isFirstFile = reportFileDetails.isIsFirstFile();

        FileProcessDeterminants fileProcessDeterminants = new FileProcessDeterminants();

        if (!isFirstFile) {
            if (isMaster) {
                fileProcessDeterminants.setProcessingEffort(ProcessingEffort.MASTER_FILE_EFFORT);
                fileProcessDeterminants.setFilePosition(FilePosition.MASTER_FILE);
            } else {
                fileProcessDeterminants.setProcessingEffort(ProcessingEffort.OTHER_FILES_EFFORT);
                fileProcessDeterminants.setFilePosition(FilePosition.SUBSEQUENT_FILE);
            }
        } else {
            fileProcessDeterminants.setProcessingEffort(ProcessingEffort.FIRST_FILE_EFFORT);
            fileProcessDeterminants.setFilePosition(FilePosition.FIRST_FILE);
        }

        return fileProcessDeterminants;
    }

    private static boolean sendForProcessing(final ReportDetails reportFileDetails) throws IOException, MyCustomException {

        boolean isFirstFile = reportFileDetails.isIsFirstFile();
        String reconID = reportFileDetails.getReconGroupID();
        int numberOfRecords = reportFileDetails.getNumberOfRecords();
        int compoundedRecords = reportFileDetails.getCompoundedNumberOfRecords();
        FileProcessDeterminants fileProcessDeterminants = reportFileDetails.getFileProcessDeterminants();

        if (!isFirstFile) {
            //GlobalFileProcessAttributes.globalReconDetails.get(reconID).get(ReconGlobalDetailKeys.NUM_RECORDS);
            GlobalAttributes.increment(reconID, numberOfRecords, GlobalAttributes.totalUnreconciledRecords);
            GlobalAttributes.increment(reconID, compoundedRecords, GlobalAttributes.totalRecordsToBeRead);

        } else {

            GeneralUtils.addReconDetailsToGlobalMaps(reconID, reportFileDetails);
        }

        logger.info("Records uploaded: " + numberOfRecords);
        logger.info("Compounded total: " + GlobalAttributes.totalRecordsToBeRead.get(reconID));
        logger.info("effort used     : " + fileProcessDeterminants.getProcessingEffort().getProcessingEffortValue());
        logger.info("file position   : " + fileProcessDeterminants.getFilePosition());

        Runnable uploadFileTask = new ReadFileTask(reportFileDetails);

        Future future = GeneralUtils.executeTask(uploadFileTask);
        //future.cancel(Boolean.TRUE);

        return true;
    }

    public static String generateExceptionsFilepath(String reportTitleName, String fileExt) {

        String formattedTitle = GeneralUtils.replaceSpaces(reportTitleName, "_");
        String dateTimeNow = GeneralUtils.getDateTimeNow("Africa/Kampala", "dd-MM-yyyy-HHmmss");
        String exceptionsFile = GlobalAttributes.SAVE_FINAL_DIR + formattedTitle + "_" + dateTimeNow + fileExt; // remove this .xls in the near future

        return exceptionsFile;

    }

    private static ReportDetails buildFullReportDetailsHelper(ReportDetails reportFileDetails) throws IOException, MyCustomException {

        String reportTitle = reportFileDetails.getReportTitle();
        String reconID = reportFileDetails.getReconGroupID();
        LinkType linkType = reportFileDetails.getLinkType();
        int numberOfRecords = reportFileDetails.getNumberOfRecords();

        long count = DBManager.countRecords(ReportDetails.class, "fileID", reportFileDetails.getFileID());

        if (count > 0) {

            if (!(reportFileDetails.getLinkType() == LinkType.LINKED)) {

                reportFileDetails.setIsReconcilable(Boolean.FALSE);
                logger.warn("!!!!!FileID already exists in DB yet Not a LINKED file - Aborting reconciliation!!!!");
                return null;
            }
        }
        reportFileDetails.setIsReconcilable(Boolean.TRUE);

        String exceptionsFile = generateExceptionsFilepath(reportTitle, GlobalAttributes.FILE_EXT_XLS);
        reportFileDetails.setExceptionsFilePath(exceptionsFile);

        boolean isReconIDexists = GlobalAttributes.globalReconFileDetails.containsKey(reconID);
        if (isReconIDexists || linkType == LinkType.LINKED || linkType == LinkType.LINKER) {
            reportFileDetails.setIsFirstFile(Boolean.FALSE);
        } else {
            reportFileDetails.setIsFirstFile(Boolean.TRUE);
        }

        FileProcessDeterminants fileProcessDeterminants = buildFileProcessDetermHelper(reportFileDetails);
        reportFileDetails.setFileProcessDeterminants(fileProcessDeterminants);

        int compoundedRecords = (fileProcessDeterminants.getProcessingEffort().getProcessingEffortValue()) * numberOfRecords;
        reportFileDetails.setCompoundedNumberOfRecords(compoundedRecords);//transient field no need to store in DB            

        return reportFileDetails;
    }

    /**
     *
     * @param rowHeaderNames
     * @return fileHeaderMapString
     */
    public static String getFileHeaderNamesHelper(List<String> rowHeaderNames) {

        Map<String, String> headerNamesMap = new HashMap<>();
        for (String header : rowHeaderNames) {
            headerNamesMap.put(header, "-");
        }
        String fileHeaderMapString = GeneralUtils.convertToJson(headerNamesMap, stringMapType);

        return fileHeaderMapString;
    }

    public static void prepareAndSendForReconciling(ReportDetails reportFileDetails) throws IOException, MyCustomException {

        buildFullReportDetailsHelper(reportFileDetails);
        sendForProcessing(reportFileDetails);
    }

    /**
     *
     * @param <T>
     * @param listToConvert
     * @return
     */
    public static <T> ArrayList<T> convertListToArrayList(List<T> listToConvert) {

        return (new ArrayList<>(listToConvert));

    }

    /**
     *
     * @param reconID
     * @throws IOException
     * @throws com.namaraka.recon.exceptiontype.MyCustomException
     */
    public static void reconcileLinkedFile(String reconID) throws IOException, MyCustomException {

        Object linkedFileReconciled = GlobalAttributes.linkedFileReconciled.get(reconID);
        Object linkerFileRead = GlobalAttributes.linkerFileRead.get(reconID);
        Object linkedFileRead = GlobalAttributes.linkerFileRead.get(reconID);

        boolean isLinkFileReconciled = linkedFileReconciled != null ? (boolean) linkedFileReconciled : Boolean.FALSE;
        boolean isLinkerFileRead = linkerFileRead != null ? (boolean) linkerFileRead : Boolean.FALSE;
        boolean isLinkFileRead = linkedFileRead != null ? (boolean) linkedFileRead : Boolean.FALSE;

        logger.info("isLinkFileReconciled >> " + isLinkFileReconciled);
        logger.info("isLinkerFileRead     >> " + isLinkerFileRead);
        logger.info("isLinkFileRead       >> " + isLinkFileRead);

        if (isLinkFileRead && isLinkerFileRead && !isLinkFileReconciled) {

            //GlobalAttributes.linkedFileReconciled.put(reconID, Boolean.TRUE);
            List<ReportDetails> linkedTypeReportDetails = DBManager.getRecordsEqualToPropertyValue(ReportDetails.class, "reconGroupID", reconID, "linkType", LinkType.LINKED);

            if (!linkedTypeReportDetails.isEmpty()) {
                ReportDetails reportDetails = linkedTypeReportDetails.get(0);
                prepareAndSendForReconciling(reportDetails);
            }

        } else {
            logger.info("Either LinkFile, LinkerFile have not been read or isLinkFile already reconciled");
        }
    }

    public static ReconciliationDetails retrieveReconObjectFromDBHelper(String reconGroupID) throws MyCustomException {

        ReconciliationDetails reconDetailsTable;
        List<ReconciliationDetails> objList = DBManager.getRecordsEqualToPropertyValue(ReconciliationDetails.class, "reconGroupID", reconGroupID);

        try {
            reconDetailsTable = objList.get(0);
        } catch (IndexOutOfBoundsException iobe) {
            logger.error("IndexOutOfBoundsException reading recon object: " + iobe.getMessage());
            throw new MyCustomException("IndexOutOfBoundsException reading recon object", ErrorCode.PROCESSING_ERR, "Recon Details object details for ID: " + reconGroupID + " not found: " + iobe.getMessage(), ErrorCategory.CLIENT_ERR_TYPE);

        } catch (Exception ex) {
            logger.error("Error reading recon object: " + ex.getMessage());
            throw new MyCustomException("Error reading recon object", ErrorCode.PROCESSING_ERR, "An error occurred while retrieving recon Details object details for ID: " + reconGroupID + " not found: " + ex.getMessage(), ErrorCategory.SERVER_ERR_TYPE);

        }

        return reconDetailsTable;
    }

    /**
     *
     * @param reportFileDetails
     * @param columnHeadersFound
     * @throws MyCustomException
     */
    public static void checkRequiredColumns(ReportDetails reportFileDetails, String[] columnHeadersFound) throws MyCustomException {

        String statusColumnName = reportFileDetails.getStatusColumnName().trim();
        String idColumnName = reportFileDetails.getIDColumnName().trim();
        String linkIDcolumnName = reportFileDetails.getLinkIDColumnName().trim();
        String amountColumnName = reportFileDetails.getAmountColumnName().trim();
        String descriptionColumnName = reportFileDetails.getDescriptionColumnName().trim();
        ReportType reportType = reportFileDetails.getReportType();
        LinkType linkType = reportFileDetails.getLinkType();

        List<String> columnHeadersFoundList = Arrays.asList(columnHeadersFound);
        List<String> requiredColumnHeaders = new ArrayList<>();

        requiredColumnHeaders.add(idColumnName); //general 
        requiredColumnHeaders.add(amountColumnName); //general

        if (reportType == ReportType.EQUINOX) {

            //requiredColumnHeaders.add(statusColumnName);
            requiredColumnHeaders.add(descriptionColumnName);

        } else if (reportType == ReportType.EQUINOX_SPECIAL) {

            //requiredColumnHeaders.add(statusColumnName);
            requiredColumnHeaders.add(descriptionColumnName);

        } else if (reportType == ReportType.ELMA) {

            requiredColumnHeaders.add(statusColumnName);
            requiredColumnHeaders.add(descriptionColumnName);

        } else if (reportType == ReportType.DEFAULT) {

        } else {

        }

        //linker reports must have a linker ID field
        if (linkType == LinkType.LINKER) {
            requiredColumnHeaders.add(linkIDcolumnName);
        }

        if (!(columnHeadersFoundList.containsAll(requiredColumnHeaders))) { //some column(s) missing

            throw new MyCustomException("Missing Columns Exception", ErrorCode.BAD_REQUEST_ERR, "Required Columns missing - These are the required columns " + requiredColumnHeaders + " :: Only these found " + columnHeadersFoundList, ErrorCategory.SERVER_ERR_TYPE);
        }
    }

    /**
     *
     * @param generatedIDComponents
     * @return
     * @throws com.namaraka.recon.exceptiontype.MyCustomException
     */
    public static String generateID(String... generatedIDComponents) throws MyCustomException {

//         StringBuilder stringBuilder = new StringBuilder();
//        for(String component : generatedIDComponents){
//            stringBuilder.append(component);
//        }
//  
//        return stringBuilder.toString().trim();
        String generatedID = "";
        for (String component : generatedIDComponents) {

//            if (component == null || component.isEmpty()) {
//                throw new MyCustomException("Missing Arguments Exception", ErrorCode.INTERNAL_ERR, "Failed to generate UniqueID - Some values are missing or null. Please provide - id, amount & shortstatuscode values", ErrorCategory.SERVER_ERR_TYPE);
//            }
            generatedID += component;
        }

        return generatedID.trim();
    }

    /**
     * Set a UNION Set b
     *
     * @param <T>
     * @param a
     * @param b
     * @return
     */
    public static <T> Set<T> union(Set<T> a, Set<T> b) {
        a.addAll(b);
        return a;
    }

    /**
     * Set a INTERSECTION Set b
     *
     * @param <T>
     * @param a
     * @param b
     * @return
     */
    public static <T> Set<T> intersection(Set<T> a, Set<T> b) {
        a.retainAll(b);
        return a;
    }

    /**
     * Set a COMPLEMENT Set b (A n B')
     *
     * @param <T>
     * @param a
     * @param b
     * @return
     */
    public static <T> Set<T> complement(Set<T> a, Set<T> b) {
        a.removeAll(b);
        return a;
    }

    /**
     *
     * @param fileID
     * @param generatedID
     * @param fileDetails
     * @param hashMap
     */
    public static void addNonExistentID(String fileID, String generatedID, String fileDetails, Map<String, Map<String, String>> hashMap) {

        Map<String, String> map = hashMap.get(fileID);

        if (map == null) {
            map = new HashMap<>();
            hashMap.put(fileID, map);
        }

        map.put(generatedID, fileDetails);
    }

    /*public static List<String> convertArrayListToList(ArrayList<String> arrayListToConvert) {
        
     List<String> results = arrayListToConvert.Cast <String> ().ToList();
     return results;
     }*/
    /**
     * Execute a file upload task
     *
     * @param uploadFileTask
     * @return
     * @throws MyCustomException
     */
    public static Future executeTask(Runnable uploadFileTask) throws MyCustomException { //this is the count down latch this thread will await on, it will also create it's own for the next guy in queue to await

        Future futureTask;

        try {
            //InitApp.getFileUploadExecService().execute(uploadFileTask); //blocking method
            futureTask = InitApp.getFileUploadExecService().submit(uploadFileTask);

        } catch (RejectedExecutionException | NullPointerException ex) {

            throw new MyCustomException("Error processing task", ErrorCode.PROCESSING_ERR, ex.getMessage(), ErrorCategory.SERVER_ERR_TYPE);
        }

        return futureTask;
    }
}
