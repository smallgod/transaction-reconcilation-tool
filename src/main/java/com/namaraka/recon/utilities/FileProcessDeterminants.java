/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon.utilities;

import com.namaraka.recon.constants.FilePosition;
import com.namaraka.recon.constants.ProcessingEffort;

/**
 *
 * @author smallgod
 */
public class FileProcessDeterminants {
    
    private int noOfRecords;
    private FilePosition filePosition;
    private ProcessingEffort processingEffort;   

    public FileProcessDeterminants() {
    }
        

    public FileProcessDeterminants(int noOfRecords, FilePosition filePosition, ProcessingEffort processingEffort) {
        
        this.noOfRecords = noOfRecords;
        this.filePosition = filePosition;
        this.processingEffort = processingEffort;
    }

    public int getNoOfRecords() {
        return noOfRecords;
    }

    public void setNoOfRecords(int noOfRecords) {
        this.noOfRecords = noOfRecords;
    }

    public FilePosition getFilePosition() {
        return filePosition;
    }

    public void setFilePosition(FilePosition filePosition) {
        this.filePosition = filePosition;
    }

    public ProcessingEffort getProcessingEffort() {
        return processingEffort;
    }

    public void setProcessingEffort(ProcessingEffort processingEffort) {
        this.processingEffort = processingEffort;
    }
}
