/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon.feedback;

import com.google.gson.annotations.SerializedName;
import com.namaraka.recon.constants.ReconStatus;
import java.util.Collections;
import java.util.Map;

/**
 *
 * @author smallgod
 */
public class ReconProgress {

    @SerializedName(value = "status")    
    private String status;
    
    @SerializedName(value = "percentage")
    private String percentage;    
    
    @SerializedName(value = "exceptionsfound")
    private String exceptionCount;
    
    @SerializedName(value = "exceptionrate")
    private String exceptionRate;    
    
    @SerializedName(value = "timetaken")
    private String timeTaken;
    
    @SerializedName(value = "timestarted")
    private String timeStarted;
    
    @SerializedName(value = "timecompleted")
    private String timeEnded;
   
    @SerializedName(value = "allfilepath")
    private String finalReconFileName;
   
    @SerializedName(value = "exceptionsfilepath")
    private String exceptionsFilePathName;
    
    @SerializedName(value = "exceptionsfile1")
    private String exceptionsFileA;
   
    @SerializedName(value = "exceptionsfile2")
    private String exceptionsFileB;
   
    @SerializedName(value = "numoffiles")
    private String numOfFiles;
    
    @SerializedName(value = "totalrecordsprocessed")
    private String recordsProcessed;
   
    @SerializedName(value = "totalrecordswritten")
    private String totalFinalRecords;
    
    @SerializedName(value = "message")
    private String message;
    
    @SerializedName(value = "exception_files")
    private Map<String, Object> exceptionFiles;
    
    @SerializedName(value = "teststring")
    private String testString;

    public ReconProgress() {
        
        this.exceptionCount = "0";
        this.exceptionRate = "0";
        this.percentage = "0";
        this.recordsProcessed = "0";
        this.status = ReconStatus.NEW.getValue();//this is NEW because we are still recieving upload files reconciliation/start recon has not yet been called
    }
    
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPercentage() {
        return percentage;
    }

    public void setPercentage(String percentage) {
        this.percentage = percentage;
    }

    public String getRecordsProcessed() {
        return recordsProcessed;
    }

    public void setRecordsProcessed(String recordsProcessed) {
        this.recordsProcessed = recordsProcessed;
    }

    public String getExceptionCount() {
        return exceptionCount;
    }

    public void setExceptionCount(String exceptionCount) {
        this.exceptionCount = exceptionCount;
    }

    public String getExceptionRate() {
        return exceptionRate;
    }

    public void setExceptionRate(String exceptionRate) {
        this.exceptionRate = exceptionRate;
    }

    public String getFinalReconFileName() {
        return finalReconFileName;
    }

    public void setFinalReconFileName(String finalReconFileName) {
        this.finalReconFileName = finalReconFileName;
    }

    public String getExceptionsFilePathName() {
        return exceptionsFilePathName;
    }

    public void setExceptionsFilePathName(String exceptionsFilePathName) {
        this.exceptionsFilePathName = exceptionsFilePathName;
    }

    public String getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(String timeTaken) {
        this.timeTaken = timeTaken;
    }

    public String getTestString() {
        return testString;
    }

    public void setTestString(String testString) {
        this.testString = testString;
    }

    public String getTimeStarted() {
        return timeStarted;
    }

    public void setTimeStarted(String timeStarted) {
        this.timeStarted = timeStarted;
    }

    public String getTimeEnded() {
        return timeEnded;
    }

    public void setTimeEnded(String timeEnded) {
        this.timeEnded = timeEnded;
    }

    public String getTotalFinalRecords() {
        return totalFinalRecords;
    }

    public void setTotalFinalRecords(String totalFinalRecords) {
        this.totalFinalRecords = totalFinalRecords;
    }

    public String getNumOfFiles() {
        return numOfFiles;
    }

    public void setNumOfFiles(String numOfFiles) {
        this.numOfFiles = numOfFiles;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getExceptionsFileA() {
        return exceptionsFileA;
    }

    public void setExceptionsFileA(String exceptionsFileA) {
        this.exceptionsFileA = exceptionsFileA;
    }

    public String getExceptionsFileB() {
        return exceptionsFileB;
    }

    public void setExceptionsFileB(String exceptionsFileB) {
        this.exceptionsFileB = exceptionsFileB;
    }

    public Map<String, Object> getExceptionFiles() {
        return Collections.unmodifiableMap(exceptionFiles);
    }

    public void setExceptionFiles(Map<String, Object> exceptionFiles) {
        this.exceptionFiles = exceptionFiles;
    }
}
