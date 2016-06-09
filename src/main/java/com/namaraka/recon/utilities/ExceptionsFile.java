/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon.utilities;

import com.namaraka.recon.model.v1_0.ReportDetails;

/**
 *
 * @author smallgod
 */
public class ExceptionsFile {
    
    private String fileID;
    private ReportDetails reportDetails;
    private int noOfExceptions;
    private boolean isWrittenToFile;

    public int getNoOfExceptions() {
        return noOfExceptions;
    }

    public void setNoOfExceptions(int noOfExceptions) {
        this.noOfExceptions = noOfExceptions;
    }

    public boolean isIsWrittenToFile() {
        return isWrittenToFile;
    }

    public void setIsWrittenToFile(boolean isWrittenToFile) {
        this.isWrittenToFile = isWrittenToFile;
    }

    public String getFileID() {
        return fileID;
    }

    public void setFileID(String fileID) {
        this.fileID = fileID;
    }

    public ReportDetails getReportDetails() {
        return reportDetails;
    }

    public void setReportDetails(ReportDetails reportDetails) {
        this.reportDetails = reportDetails;
    }
    
}
