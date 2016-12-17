/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon.utilities;

import com.google.gson.reflect.TypeToken;
import com.namaraka.recon.constants.ErrorCategory;
import com.namaraka.recon.constants.ErrorCode;
import com.namaraka.recon.constants.FileExtension;
import com.namaraka.recon.constants.FileReconProgressEnum;
import com.namaraka.recon.constants.LinkType;
import com.namaraka.recon.constants.ReportType;
import com.namaraka.recon.constants.TransactionState;
import com.namaraka.recon.dbaccess.DBManager;
import com.namaraka.recon.exceptiontype.MyCustomException;
import com.namaraka.recon.model.v1_0.ReportDetails;
import com.namaraka.recon.model.v1_0.TemporaryRecords;
import com.opencsv.CSVReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author smallgod
 */
public class FileUtilities {

    private static final Logger logger = LoggerFactory.getLogger(FileUtilities.class);

    private static final int BUFFER_SIZE = 8 * 1024;

    private FileUtilities() {
        //called only once
    }

    private static class FileUtilitiesSingletonHolder {

        private static final FileUtilities INSTANCE = new FileUtilities();
    }

    public static FileUtilities getInstance() {
        return FileUtilitiesSingletonHolder.INSTANCE;
    }

    protected Object readResolve() {
        return getInstance();
    }

    /**
     *
     * @param inputFile
     * @param outputFile
     * @return true | false if file was converted successfuly
     * @throws com.namaraka.recon.exceptiontype.MyCustomException
     */
    public static boolean convertFileToCSV(String inputFile, String outputFile) throws MyCustomException {

        //String command = "ping -c 3 www.google.com";
        String createFile = "touch %s";
        String formattedCmd1 = String.format(createFile, outputFile);
        String conversionCmd = "ssconvert --export-type=Gnumeric_stf:stf_csv %s %s";
        String formattedCmd = String.format(conversionCmd, inputFile, outputFile);

        logger.debug("... executing command $ " + formattedCmd);

        Process proc;
        String line = "";
        int exitValue;

        try {

            //execute the command
            Runtime.getRuntime().exec(formattedCmd1);
            proc = Runtime.getRuntime().exec(formattedCmd);

            //Read the output
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));

            while ((line = reader.readLine()) != null) {
                logger.debug(line + "\n");
            }

            exitValue = proc.waitFor();

        } catch (IOException | InterruptedException | SecurityException ex) {
            throw new MyCustomException("Exception", ErrorCode.COMMUNICATION_ERR, "Exception executing shell command ->> $ " + formattedCmd + " ->> with errormsg: " + ex.getMessage(), ErrorCategory.SERVER_ERR_TYPE);

        } catch (Exception ex) {
            throw new MyCustomException("Exception", ErrorCode.COMMUNICATION_ERR, "Exception executing shell command ->> $ " + formattedCmd + " ->> with errormsg: " + ex.getMessage(), ErrorCategory.SERVER_ERR_TYPE);

        }

        if (exitValue == 0X0) { //0 indicates normal termination
            return Boolean.TRUE;
        } else {

            throw new MyCustomException("Abnormal termination", ErrorCode.INTERNAL_ERR, "Exception executing shell command ->> $ " + formattedCmd + " terminated with a non-normal flag:: " + exitValue, ErrorCategory.SERVER_ERR_TYPE);

        }
    }

    public static boolean convertFileToCSV1(String inputFile, String outputFile) {

        try {
            // build the system command we want to run
            List<String> commands = new ArrayList<>();

            String createFile = "touch %s";
            String formattedCmd1 = String.format(createFile, outputFile);
            String conversionCmd = "ssconvert --export-type=Gnumeric_stf:stf_csv %s %s";
            String formattedCmd2 = String.format(conversionCmd, inputFile, outputFile);

            //commands.add("/bin/sh");
            commands.add(formattedCmd1);
            commands.add(formattedCmd2);

            // execute the command
            SystemCommandExecutor commandExecutor = new SystemCommandExecutor(commands);
            int result = commandExecutor.executeCommand();

            // get the stdout and stderr from the command that was run
            StringBuilder stdout = commandExecutor.getStandardOutputFromCommand();
            StringBuilder stderr = commandExecutor.getStandardErrorFromCommand();

            // print the stdout and stderr
            System.out.println("The numeric result of the command was: " + result);
            System.out.println("STDOUT:");
            System.out.println(stdout);
            System.out.println("STDERR:");
            System.out.println(stderr);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(FileUtilities.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(FileUtilities.class.getName()).log(Level.SEVERE, null, ex);
        }

        return Boolean.TRUE;
    }

    /**
     * *
     * Get file extension e.g. txt, xls, ods, odt, csv, xlsl
     *
     * @param fileName
     * @return
     * @throws NullPointerException
     * @throws com.namaraka.recon.exceptiontype.MyCustomException
     */
    public static String getFileExtension(String fileName) throws MyCustomException {

        String fileExtension = null;

        logger.info("fileName::: " + fileName);

        int indexOfDot = fileName.lastIndexOf('.');

        int p = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));

        if (indexOfDot > p) {

            fileExtension = fileName.substring(indexOfDot + 1);
        }

        if (fileExtension == null) {
            throw new MyCustomException("NullPointer Exception", ErrorCode.NOT_SUPPORTED_ERR, "Failed to get file Extension", ErrorCategory.CLIENT_ERR_TYPE);
        }

        logger.info("File Extension got: " + fileExtension);

        return fileExtension.trim();
    }

    /**
     *
     * @param oldFileName
     * @param newFileExtension
     * @return filename with new extension
     * @throws com.namaraka.recon.exceptiontype.MyCustomException
     */
    public static String changeFileTypeToCSV(String oldFileName, String newFileExtension) throws MyCustomException {

        String newFileName = null;

        int indexOfDot = oldFileName.lastIndexOf('.');
        int p = Math.max(oldFileName.lastIndexOf('/'), oldFileName.lastIndexOf('\\'));

        if (indexOfDot > p) {
            newFileName = oldFileName.replaceFirst(oldFileName.substring(indexOfDot + 1), newFileExtension);
        }

        if (newFileName == null) {
            throw new MyCustomException("NullPointer Exception", ErrorCode.NOT_SUPPORTED_ERR, "Failed to create new File Name from:: " + oldFileName, ErrorCategory.SERVER_ERR_TYPE);

        }
        return newFileName.trim();
    }

    /**
     *
     * @param absoluteFileName
     * @return fileNameAndType
     */
    public static String getFileNameAndType(String absoluteFileName) {

        int indexOfFileName = Math.max(absoluteFileName.lastIndexOf('/'), absoluteFileName.lastIndexOf('\\'));
        String fileNameAndType = absoluteFileName.substring(indexOfFileName + 1).trim();

        return fileNameAndType;
    }

    /**
     *
     * @param source
     * @param dest
     * @throws IOException
     */
    public static void copyFile(File source, File dest) throws IOException {
        
        Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     *
     * @param absFileName
     * @return
     * @throws MyCustomException
     */
    public static boolean createFileOnDisk(String absFileName) throws MyCustomException {

        boolean isCreated = false;

        try {

            File newFile = new File(absFileName);
            //newFile.mkdirs();
            isCreated = newFile.createNewFile();

        } catch (IOException ex) {
            throw new MyCustomException("IO Exception", ErrorCode.COMMUNICATION_ERR, "Failed to create new File : " + ex.getMessage() + " --> FileName: " + absFileName, ErrorCategory.SERVER_ERR_TYPE);

        } catch (SecurityException ex) {
            throw new MyCustomException("SecurityException", ErrorCode.SERVER_ERR, "Failed to create new File : " + ex.getMessage() + " --> FileName: " + absFileName, ErrorCategory.SERVER_ERR_TYPE);

        } catch (NullPointerException ex) {
            throw new MyCustomException("NPE", ErrorCode.SERVER_ERR, "Failed to create new File : " + ex.getMessage() + " --> FileName: " + absFileName, ErrorCategory.SERVER_ERR_TYPE);

        } catch (Exception ex) {
            throw new MyCustomException("Exception", ErrorCode.SERVER_ERR, "Failed to create new File : " + ex.getMessage(), ErrorCategory.SERVER_ERR_TYPE);

        }

        return isCreated;
    }

    /**
     * *
     * create an excel work book instance
     *
     * @param fileExtConstant
     * @param fileInputStream
     * @return
     * @throws IOException
     */
    public static Workbook getWorkBookInstanceHelper(FileExtension fileExtConstant, FileInputStream fileInputStream) throws IOException {

        Workbook workBook = null;

        switch (fileExtConstant) {

            case CSV:
                break;

            case XLS:

                workBook = new HSSFWorkbook(fileInputStream);
                break;

            case XLSX:

                workBook = new XSSFWorkbook(fileInputStream);
                break;

            default:
                logger.error("File extension: " + FileExtension.XLS.toString() + " -> not found: ");
                throw new NullPointerException("File extension: " + FileExtension.XLS.toString() + " -> not found: ");
        }

        return workBook;
    }

    /**
     * *
     * create an excel work book instance
     *
     * @param fileExtConstant
     * @return
     * @throws IOException
     */
    public static Workbook getWorkBookInstanceHelper(FileExtension fileExtConstant) throws IOException {

        Workbook workBook = null;

        switch (fileExtConstant) {

            case CSV:
                break;

            case XLS:

                workBook = new HSSFWorkbook();
                break;

            case XLSX:
                workBook = new XSSFWorkbook();
                break;

            default:
                logger.error("File extension: " + FileExtension.XLS.toString() + " -> not found: ");
                throw new NullPointerException("File extension: " + FileExtension.XLS.toString() + " -> not found: ");
        }

        return workBook;
    }

    public static Workbook createWorkBook(String fileToBeReadPath) throws MyCustomException {

        //fileRemoteUrl = new URL(fileToBeReadPath);
        //InputStream in = fileRemoteUrl.openStream(); 
        String fileExtension = FileUtilities.getFileExtension(fileToBeReadPath);
        //fileExtConstant = FileExtension.valueOf(newFileName.toUpperCase(Locale.ENGLISH));
        FileExtension fileExtConstant = FileExtension.convertToEnum(fileExtension);

        Workbook workBook;

        try {
            // Use a file
            workBook = WorkbookFactory.create(new File(fileToBeReadPath));

            // Use an InputStream, needs more memory
            //workBook = WorkbookFactory.create(new FileInputStream("MyExcel.xlsx"));
        } catch (InvalidFormatException ife) {
            logger.error("InvalidFormatException creating workbook object" + ife.getMessage());
            throw new MyCustomException("InvalidFormatException creating workbook object", ErrorCode.PROCESSING_ERR, "Failed to create a workBook object: " + ife.getMessage(), ErrorCategory.SERVER_ERR_TYPE);

        } catch (FileNotFoundException ife) {
            logger.error("FileNotFoundException creating workbook object" + ife.getMessage());
            throw new MyCustomException("FileNotFoundException creating workbook object", ErrorCode.PROCESSING_ERR, "Failed to create a workBook object: " + ife.getMessage(), ErrorCategory.SERVER_ERR_TYPE);

        } catch (IOException ife) {
            logger.error("IOException creating workbook object" + ife.getMessage());
            throw new MyCustomException("IOException creating workbook object", ErrorCode.PROCESSING_ERR, "Failed to create a workBook object: " + ife.getMessage(), ErrorCategory.SERVER_ERR_TYPE);

        }

        /*try (FileInputStream fileInputStream = new FileInputStream(new File(fileToBeReadPath))) {
         workBook = FileUtilities.getWorkBookInstanceHelper(fileExtConstant, fileInputStream);
         }*/
        return workBook;
    }

    /**
     *
     * @param workBook
     * @param sheetIndex
     * @return
     */
    public static Sheet getSheetAtIndex(Workbook workBook, int sheetIndex) {

        int numberOfSheets = workBook.getNumberOfSheets();
        //first sheet from workbook
        Sheet sheet = workBook.getSheetAt(sheetIndex);

        return sheet;
    }

    /**
     *
     * @param sheet
     * @return
     * @throws MyCustomException
     */
    public static int getNumRecordsMinusHeaders(Sheet sheet) throws MyCustomException {

        int noOfPhysicalRows = sheet.getPhysicalNumberOfRows() - 1; //minus HeaderNames row
        if (noOfPhysicalRows < 1) {

            logger.error("Excel sheet is empty");
            throw new MyCustomException("Uploaded file empty", ErrorCode.CLIENT_ERR, "Uploaded file must contain atleast one row of data", ErrorCategory.CLIENT_ERR_TYPE);
        }

        return noOfPhysicalRows;
    }

    /**
     * *
     * @param sheet
     * @return Iterator
     * @throws MyCustomException
     */
    public static Iterator<Row> getWorkBookRowIteratorHelper(Sheet sheet) throws MyCustomException {

        Iterator<Row> rowIterator = sheet.iterator();

        return rowIterator;
    }

    /**
     * Count the number of lines in a file
     *
     * @param fileName
     * @return
     * @throws MyCustomException
     */
    public static int countNoOfLinesInFile1(String fileName) throws MyCustomException {

        LineNumberReader lineNumberReader = null;
        int lineCount = 0;

        try {

            File file = new File(fileName);
            lineNumberReader = new LineNumberReader(new FileReader(file));
            lineNumberReader.skip(Long.MAX_VALUE);
            lineCount = lineNumberReader.getLineNumber();

        } catch (FileNotFoundException ex) {
            throw new MyCustomException("FileNotFound Error", ErrorCode.INTERNAL_ERR, "File NOT found: " + ex.getMessage(), ErrorCategory.SERVER_ERR_TYPE);

        } catch (IOException ex) {
            throw new MyCustomException("IO Error", ErrorCode.COMMUNICATION_ERR, "IO error: " + ex.getMessage(), ErrorCategory.SERVER_ERR_TYPE);

        } finally {

            try {
                if (lineNumberReader != null) {
                    lineNumberReader.close();
                }
            } catch (IOException ex) {
                logger.error("Error while closing LineNumberReader stream: " + ex.getMessage());
            }
        }

        return lineCount;

    }

    /**
     * Counts number of lines in file (**NOT cross-platform)
     *
     * @param fileName
     * @return
     * @throws MyCustomException
     */
    public final static int countNoOfLinesInFile2(String fileName) throws MyCustomException {

        int lineCount = -1;

        try {

            ProcessBuilder builder = new ProcessBuilder("wc", "-l", fileName);
            Process process = builder.start();

            InputStream in = process.getInputStream();
            LineNumberReader reader = new LineNumberReader(new InputStreamReader(in));
            String line = reader.readLine();

            if (line != null) {

                lineCount = Integer.parseInt(line.trim().split(" ")[0]);
            }

        } catch (IOException ex) {
            throw new MyCustomException("IO Error", ErrorCode.COMMUNICATION_ERR, "IO error: " + ex.getMessage(), ErrorCategory.SERVER_ERR_TYPE);

        }
        return lineCount;
    }

    /**
     * Count the number of lines (3* faster)
     *
     * @param fileName
     * @return
     * @throws com.namaraka.recon.exceptiontype.MyCustomException
     */
    public static int countNoOfLinesInFile(String fileName) throws MyCustomException {

        FileInputStream fis = null;
        int lineCount = -1;

        try {

            fis = new FileInputStream(new File(fileName));
            byte[] buffer = new byte[BUFFER_SIZE]; // BUFFER_SIZE = 8 * 1024

            int read;
            while ((read = fis.read(buffer)) != -1) {
                for (int i = 0; i < read; i++) {
                    if (buffer[i] == '\n') {
                        lineCount++;
                    }
                }
            }

        } catch (FileNotFoundException ex) {
            throw new MyCustomException("FileNotFound Error", ErrorCode.INTERNAL_ERR, "File NOT found: " + ex.getMessage(), ErrorCategory.SERVER_ERR_TYPE);

        } catch (IOException ex) {
            throw new MyCustomException("IO Error", ErrorCode.COMMUNICATION_ERR, "IO error: " + ex.getMessage(), ErrorCategory.SERVER_ERR_TYPE);

        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException ex) {
                logger.error("Error while closing FileInputStream stream: " + ex.getMessage());
            }
        }
        return lineCount;
    }
}
