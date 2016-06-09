/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.namaraka.recon;

/**
 *
 * @author smallgod
 */
public class CallingFiles {
    
    private String firstFileID;
    private String secondFIleID;

    public CallingFiles(String firstFileID, String secondFIleID) {
        this.firstFileID = firstFileID;
        this.secondFIleID = secondFIleID;
    }

    public String getFirstFileID() {
        return firstFileID;
    }

    public void setFirstFileID(String firstFileID) {
        this.firstFileID = firstFileID;
    }

    public String getSecondFIleID() {
        return secondFIleID;
    }

    public void setSecondFIleID(String secondFIleID) {
        this.secondFIleID = secondFIleID;
    }
    
}
