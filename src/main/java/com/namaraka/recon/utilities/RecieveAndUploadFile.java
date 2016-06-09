/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon.utilities;

import com.namaraka.recon.UploadFile;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *    /**
 * This servlet demonstrates how to receive file uploaded from the client
 * without using third-party upload library such as Commons File Upload.
 *
 * @author www.codejava.net
 *
 * @author smallgod
 */
public class RecieveAndUploadFile {

    private static final Logger logger = LoggerFactory.getLogger(RecieveAndUploadFile.class);

    static final String UPLOAD_URL = "http://localhost:8080/recontool/run";

    static final String SAVE_DIR = "/home/smallgod/NetBeansProjects/recoontool/web/WEB-INF/reconfolder/"; //put this in the configs file
    static final int BUFFER_SIZE = 4096; //put this in the configs file

    /**
     * Downloads a file from a URL
     *
     * @param fileURL HTTP URL of the file to be downloaded e.g.
     * http://jdbc.postgresql.org/download/postgresql-9.2-1002.jdbc4.jar
     * @param reconFolderName directory to save the file
     * @throws IOException
     */
    public static void downloadFile(String fileURL, String reconFolderName) throws IOException {

        URL url = new URL(fileURL);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();

        // always check HTTP response code first
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String fileName = "";
            String disposition = httpConn.getHeaderField("Content-Disposition");
            String contentType = httpConn.getContentType();
            int contentLength = httpConn.getContentLength();

            if (disposition != null) {
                // extracts file name from header field
                int index = disposition.indexOf("filename=");
                if (index > 0) {
                    fileName = disposition.substring(index + 10, disposition.length() - 1);
                }
            } else {
                // extracts file name from URL
                fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1, fileURL.length());
            }

            logger.debug("Content-Type = " + contentType);
            logger.debug("Content-Disposition = " + disposition);
            logger.debug("Content-Length = " + contentLength);
            logger.debug("fileName = " + fileName);

            // opens input stream from the HTTP connection
            InputStream inputStream = httpConn.getInputStream();
            //String saveFilePath = saveDir + File.separator + fileName;

            File saveFile = new File(SAVE_DIR + reconFolderName + File.separator + fileName);
            boolean dirsCreated = saveFile.mkdirs(); //create any intermediary dirs that do not exist

            // opens an output stream to save into file
            FileOutputStream outputStream = new FileOutputStream(saveFile);

            int bytesRead = -1;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            logger.debug("File downloaded");
        } else {
            logger.debug("No file to download. Server replied HTTP code: " + responseCode);
        }
        httpConn.disconnect();
    }

    /**
     *
     * @param request
     * @param response
     * @param reconFolderName
     * @param fileName
     * @return
     * @throws ServletException
     * @throws IOException
     */
    public static boolean recieveUploadRequest(HttpServletRequest request, HttpServletResponse response, String reconFolderName, String fileName) throws ServletException, IOException {

        //String fileName = request.getHeader("fileName");
        File saveFile = new File(SAVE_DIR + reconFolderName + File.separator + fileName);
        boolean dirsCreated = saveFile.mkdirs(); //create any intermediary dirs that do not exist
        //throw some errors if dirs not created

        Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String headerName = names.nextElement();
            logger.debug(headerName + " = " + request.getHeader(headerName));
        }

        Part fileToReconcile = request.getPart("fileName"); // Retrieves <input type="file" name="file">
        String file = getFileName(fileToReconcile); // Calls getFilename method

        logger.debug("fileName from multipart: " + file);

        InputStream inputStream = fileToReconcile.getInputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

        // opens input stream of the request for reading data
        //InputStream inputStream = request.getInputStream();
        // opens an output stream for writing file
        FileOutputStream outputStream = new FileOutputStream(saveFile);

        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead = -1;
        logger.debug("Receiving data...");

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        logger.debug("Data received.");
        outputStream.close();
        inputStream.close();

        logger.debug("File written to: " + saveFile.getAbsolutePath());

        // sends response to client
        //response.getWriter().print("UPLOAD DONE");
        return Boolean.TRUE;
    }

    /**
     * Utility method to get file name from HTTP header content-disposition
     */
    private static String getFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        System.out.println("content-disposition header= " + contentDisp);
        String[] tokens = contentDisp.split(";");
        for (String token : tokens) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf("=") + 2, token.length() - 1);
            }
        }
        return "";
    }

    public static boolean recieveFile(HttpServletRequest request, HttpServletResponse response, String reconFolderName, String fileName) throws ServletException, IOException {

        //String fileName = request.getHeader("fileName");
        File saveFile = new File(SAVE_DIR + reconFolderName + "/" + fileName);
        boolean dirsCreated = saveFile.mkdirs(); //create any intermediary dirs that do not exist
        //throw some errors if dirs not created

        Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String headerName = names.nextElement();
            logger.debug(headerName + " = " + request.getHeader(headerName));
        }

        // opens input stream of the request for reading data
        InputStream inputStream = request.getInputStream();

        // opens an output stream for writing file
        FileOutputStream outputStream = new FileOutputStream(saveFile);

        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead = -1;
        logger.debug("Receiving data...");

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        logger.debug("Data received.");
        outputStream.close();
        inputStream.close();

        logger.debug("File written to: " + saveFile.getAbsolutePath());

        // sends response to client
        //response.getWriter().print("UPLOAD DONE");
        return Boolean.TRUE;
    }

    public static void uploadNonFormFile() throws IOException {
        // takes file path from first program's argument
        String filePath = "/home/smallgod/NetBeansProjects/recontool/web/WEB-INF/filetoupload.xls";
        File uploadFile = new File(filePath);

        System.out.println("File to upload: " + filePath);

        // creates a HTTP connection
        URL url = new URL(UPLOAD_URL);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setUseCaches(false);
        httpConn.setDoOutput(true);
        httpConn.setRequestMethod("POST");
        // sets file name as a HTTP header
        httpConn.setRequestProperty("fileName", uploadFile.getName());

        // opens output stream of the HTTP connection for writing data
        OutputStream outputStream = httpConn.getOutputStream();

        // Opens input stream of the file for reading data
        FileInputStream inputStream = new FileInputStream(uploadFile);

        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead = -1;

        System.out.println("Start writing data...");

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        System.out.println("Data was written.");
        outputStream.close();
        inputStream.close();

        // always check HTTP response code from server
        int responseCode = httpConn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // reads server's response
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    httpConn.getInputStream()));
            String response = reader.readLine();
            System.out.println("Server's response: " + response);
        } else {
            System.out.println("Server returned non-OK code: " + responseCode);
        }
    }
}
